package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.constant.InterviewStatus;
import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.job_services.domain.InterviewSchedule;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.domain.JobApplyStatusHistory;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.job_services.repository.InterviewScheduleRepo;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.job_services.repository.JobApplyStatusHistoryRepo;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.job_services.service.dto.request.JobApplyRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobApplyResponse;
import com.fpt.careermate.services.job_services.service.impl.JobApplyService;
import com.fpt.careermate.services.job_services.service.mapper.JobApplyMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.kafka.producer.NotificationProducer;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JobApplyImp implements JobApplyService {

        JobApplyRepo jobApplyRepo;
        JobPostingRepo jobPostingRepo;
        CandidateRepo candidateRepo;
        JobApplyMapper jobApplyMapper;
        NotificationProducer notificationProducer;
        JobApplyStatusHistoryRepo statusHistoryRepo;
        AuthenticationImp authenticationImp;
        RecruiterRepo recruiterRepo;
        InterviewScheduleRepo interviewScheduleRepo;

        @Override
        @Transactional
        @PreAuthorize("hasRole('CANDIDATE')")
        public JobApplyResponse createJobApply(JobApplyRequest request) {
                log.info("Candidate {} applying to job posting ID: {}", request.getCandidateId(),
                                request.getJobPostingId());

                // Validate job posting exists
                JobPosting jobPosting = jobPostingRepo.findById(request.getJobPostingId())
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

                // Validate candidate exists
                Candidate candidate = candidateRepo.findById(request.getCandidateId())
                                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));

                // Check if already applied
                jobApplyRepo.findByJobPostingIdAndCandidateCandidateId(
                                request.getJobPostingId(), request.getCandidateId())
                                .ifPresent(existing -> {
                                        throw new AppException(ErrorCode.ALREADY_APPLIED_TO_JOB_POSTING);
                                });

                // Create new job apply
                JobApply jobApply = JobApply.builder()
                                .jobPosting(jobPosting)
                                .candidate(candidate)
                                .cvFilePath(request.getCvFilePath())
                                .fullName(request.getFullName())
                                .phoneNumber(request.getPhoneNumber())
                                .preferredWorkLocation(request.getPreferredWorkLocation())
                                .coverLetter(request.getCoverLetter())
                                .status(StatusJobApply.SUBMITTED)
                                .createAt(LocalDateTime.now())
                                .build();

                JobApply savedJobApply = jobApplyRepo.save(jobApply);
                log.info("Job application created with ID: {} for job: {}", savedJobApply.getId(),
                                jobPosting.getTitle());

                // Send notification to recruiter about new application
                try {
                        sendApplicationReceivedNotification(savedJobApply, jobPosting, candidate);
                } catch (Exception e) {
                        log.error("Failed to send application received notification: {}", e.getMessage(), e);
                        // Don't fail the application process if notification fails
                }

                return jobApplyMapper.toJobApplyResponse(savedJobApply);
        }

        @Override
        public JobApplyResponse getJobApplyById(int id) {
                JobApply jobApply = jobApplyRepo.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));
                return jobApplyMapper.toJobApplyResponse(jobApply);
        }

        @Override
        public List<JobApplyResponse> getAllJobApplies() {
                return jobApplyRepo.findAll().stream()
                                .map(jobApplyMapper::toJobApplyResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public List<JobApplyResponse> getJobAppliesByJobPosting(int jobPostingId) {
                // Validate job posting exists
                jobPostingRepo.findById(jobPostingId)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

                return jobApplyRepo.findByJobPostingId(jobPostingId).stream()
                                .map(jobApplyMapper::toJobApplyResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public List<JobApplyResponse> getJobAppliesByCandidate(int candidateId) {
                // Validate candidate exists
                candidateRepo.findById(candidateId)
                                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));

                return jobApplyRepo.findByCandidateCandidateId(candidateId).stream()
                                .map(jobApplyMapper::toJobApplyResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
        public PageResponse<JobApplyResponse> getJobAppliesByCandidateWithFilter(
                        int candidateId,
                        StatusJobApply status,
                        int page,
                        int size) {

                // Validate candidate exists
                candidateRepo.findById(candidateId)
                                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));

                // Create pageable with sorting by createAt descending (newest first)
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createAt"));

                // Query with or without status filter
                Page<JobApply> jobApplyPage = jobApplyRepo.findByCandidateIdAndStatus(candidateId, status, pageable);
                if (jobApplyPage.getTotalElements() == 0) {
                        throw new AppException(ErrorCode.JOB_APPLICATION_NOT_FOUND);
                }

                // Map to response
                List<JobApplyResponse> content = jobApplyPage.getContent().stream()
                                .map(jobApplyMapper::toJobApplyResponse)
                                .collect(Collectors.toList());

                return new PageResponse<>(
                                content,
                                jobApplyPage.getNumber(),
                                jobApplyPage.getSize(),
                                jobApplyPage.getTotalElements(),
                                jobApplyPage.getTotalPages());
        }

        @Override
        @Transactional
        @PreAuthorize("hasRole('RECRUITER')")
        public JobApplyResponse updateJobApply(int id, StatusJobApply status) {
                log.info("Recruiter updating job application ID: {} to status: {}", id, status);

                JobApply jobApply = jobApplyRepo.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

                StatusJobApply previousStatus = jobApply.getStatus();

                // Validate status transition
                if (!isValidStatusTransition(previousStatus, status)) {
                        throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
                }

                // Update status and relevant timestamps
                jobApply.setStatus(status);
                jobApply.setStatusChangedAt(LocalDateTime.now());

                // Auto-set timestamps based on status
                updateTimestampsForStatus(jobApply, status);

                JobApply updatedJobApply = jobApplyRepo.save(jobApply);

                // Record status change in history
                recordStatusChange(updatedJobApply, previousStatus, status, null, null);

                log.info("Job application ID: {} updated from {} to {}", id, previousStatus, status);

                // Send notification to candidate about status change
                try {
                        sendApplicationStatusChangeNotification(updatedJobApply, previousStatus, status);
                } catch (Exception e) {
                        log.error("Failed to send application status change notification: {}", e.getMessage(), e);
                        // Don't fail the update process if notification fails
                }

                // Handle business rules when candidate is hired (ACCEPTED status)
                if (status == StatusJobApply.ACCEPTED) {
                        try {
                                handleHireBusinessRules(updatedJobApply);
                        } catch (Exception e) {
                                log.error("Failed to process hire business rules for application {}: {}",
                                                id, e.getMessage(), e);
                                // Don't fail the main update if auto-withdraw fails
                        }
                }

                // Handle interview cancellation when application is manually withdrawn
                if (status == StatusJobApply.WITHDRAWN) {
                        try {
                                cancelInterviewOnManualWithdrawal(updatedJobApply);
                        } catch (Exception e) {
                                log.error("Failed to cancel interview for withdrawn application {}: {}",
                                                id, e.getMessage(), e);
                                // Don't fail the main update if interview cancellation fails
                        }
                }

                return jobApplyMapper.toJobApplyResponse(updatedJobApply);
        }

        @Override
        @Transactional
        public void deleteJobApply(int id) {
                JobApply jobApply = jobApplyRepo.findById(id)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));
                jobApplyRepo.delete(jobApply);
        }

        // ==================== NOTIFICATION HELPER METHODS ====================

        /**
         * Send notification to recruiter when a candidate applies to their job posting
         */
        private void sendApplicationReceivedNotification(JobApply jobApply, JobPosting jobPosting,
                        Candidate candidate) {
                String recruiterEmail = jobPosting.getRecruiter().getAccount().getEmail();
                Integer recruiterId = jobPosting.getRecruiter().getId();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
                String appliedTime = jobApply.getCreateAt().format(formatter);

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("applicationId", jobApply.getId());
                metadata.put("jobPostingId", jobPosting.getId());
                metadata.put("jobTitle", jobPosting.getTitle());
                metadata.put("candidateId", candidate.getCandidateId());
                metadata.put("candidateName", jobApply.getFullName());
                metadata.put("candidateEmail", candidate.getAccount().getEmail());
                metadata.put("appliedDate", appliedTime);
                metadata.put("cvFilePath", jobApply.getCvFilePath());

                String message = String.format(
                                "A new candidate has applied to your job posting!\n\n" +
                                                "Job Posting: %s\n" +
                                                "Candidate: %s\n" +
                                                "Email: %s\n" +
                                                "Phone: %s\n" +
                                                "Preferred Location: %s\n" +
                                                "Applied: %s\n\n" +
                                                "%s\n\n" +
                                                "Please review the application and CV in your recruiter dashboard.\n\n"
                                                +
                                                "Best regards,\n" +
                                                "CareerMate Team",
                                jobPosting.getTitle(),
                                jobApply.getFullName(),
                                candidate.getAccount().getEmail(),
                                jobApply.getPhoneNumber(),
                                jobApply.getPreferredWorkLocation(),
                                appliedTime,
                                jobApply.getCoverLetter() != null && !jobApply.getCoverLetter().isEmpty()
                                                ? "Cover Letter:\n" + jobApply.getCoverLetter()
                                                : "No cover letter provided.");

                NotificationEvent event = NotificationEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(NotificationEvent.EventType.APPLICATION_RECEIVED.name())
                                .recipientId(recruiterEmail) // Use email for SSE connection matching
                                .recipientEmail(recruiterEmail)
                                .title("New Job Application Received")
                                .subject(String.format("New Application for '%s'", jobPosting.getTitle()))
                                .message(message)
                                .category("JOB_APPLICATION")
                                .metadata(metadata)
                                .timestamp(LocalDateTime.now())
                                .priority(2) // MEDIUM priority
                                .build();

                notificationProducer.sendNotification("recruiter-notifications", event);
                log.info("✅ Sent new application notification to recruiter {} for job: {}", recruiterId,
                                jobPosting.getTitle());
        }

        /**
         * Send notification to candidate when recruiter updates their application
         * status
         */
        private void sendApplicationStatusChangeNotification(JobApply jobApply, StatusJobApply previousStatus,
                        StatusJobApply newStatus) {
                Candidate candidate = jobApply.getCandidate();
                JobPosting jobPosting = jobApply.getJobPosting();
                String candidateEmail = candidate.getAccount().getEmail();
                Integer candidateId = candidate.getCandidateId();

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("applicationId", jobApply.getId());
                metadata.put("jobPostingId", jobPosting.getId());
                metadata.put("jobTitle", jobPosting.getTitle());
                metadata.put("previousStatus", previousStatus.name());
                metadata.put("newStatus", newStatus.name());
                metadata.put("companyName", jobPosting.getRecruiter().getCompanyName());

                String title;
                String subject;
                String message;
                Integer priority;

                switch (newStatus) {
                        case APPROVED:
                                title = "Application Accepted! 🎉";
                                subject = String.format("Congratulations! Your Application for '%s' Has Been Accepted",
                                                jobPosting.getTitle());
                                message = String.format(
                                                "Great news! Your application has been accepted!\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n" +
                                                                "Location: %s\n\n" +
                                                                "The recruiter will contact you soon to discuss the next steps.\n\n"
                                                                +
                                                                "What's next?\n" +
                                                                "- Keep an eye on your email and phone for contact from the recruiter\n"
                                                                +
                                                                "- Prepare for potential interviews\n" +
                                                                "- Review the job description and company information\n\n"
                                                                +
                                                                "Best of luck with your interview!\n\n" +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName(),
                                                jobPosting.getAddress());
                                priority = 1; // HIGH priority
                                break;

                        case REJECTED:
                                title = "Application Update";
                                subject = String.format("Update on Your Application for '%s'", jobPosting.getTitle());
                                message = String.format(
                                                "Thank you for your interest in the position.\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n\n" +
                                                                "Unfortunately, after careful consideration, we have decided to move forward with other candidates "
                                                                +
                                                                "whose qualifications more closely match our current needs.\n\n"
                                                                +
                                                                "We appreciate the time you invested in the application process and encourage you to:\n"
                                                                +
                                                                "- Keep exploring other opportunities on CareerMate\n" +
                                                                "- Continue building your skills and experience\n" +
                                                                "- Apply for other positions that match your profile\n\n"
                                                                +
                                                                "We wish you the best in your job search!\n\n" +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName());
                                priority = 2; // MEDIUM priority
                                break;

                        case REVIEWING:
                                title = "Application Under Review";
                                subject = String.format("Your Application for '%s' is Being Reviewed",
                                                jobPosting.getTitle());
                                message = String.format(
                                                "Your application is now under review.\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n\n" +
                                                                "The recruiter is currently reviewing your application and CV. "
                                                                +
                                                                "You will be notified once a decision has been made.\n\n"
                                                                +
                                                                "Thank you for your patience!\n\n" +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName());
                                priority = 3; // LOW priority
                                break;

                        case INTERVIEW_SCHEDULED:
                                // This notification is handled by InterviewScheduleServiceImpl
                                // Skip sending duplicate notification here
                                log.info("Skipping application status notification for INTERVIEW_SCHEDULED - handled by interview service");
                                return;

                        case INTERVIEWED:
                                title = "Interview Completed";
                                subject = String.format("Your Interview for '%s' Has Been Completed",
                                                jobPosting.getTitle());
                                message = String.format(
                                                "Your interview has been marked as completed.\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n\n" +
                                                                "The recruiter is now reviewing the interview results. "
                                                                +
                                                                "You will be notified once a decision has been made.\n\n"
                                                                +
                                                                "Thank you for your patience!\n\n" +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName());
                                priority = 2; // MEDIUM priority
                                break;

                        case WORKING:
                                title = "🎉 Welcome to the Team!";
                                subject = String.format("Congratulations! You've Started Working at '%s'",
                                                jobPosting.getRecruiter().getCompanyName());
                                message = String.format(
                                                "🎉 Congratulations on starting your new job!\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n\n" +
                                                                "Your employment status has been updated to WORKING. " +
                                                                "We're thrilled that CareerMate helped you find this opportunity!\n\n"
                                                                +
                                                                "Tips for your first weeks:\n" +
                                                                "- Be proactive and ask questions\n" +
                                                                "- Build relationships with your colleagues\n" +
                                                                "- Take notes and learn the company culture\n\n" +
                                                                "Best of luck in your new role!\n\n" +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName());
                                priority = 1; // HIGH priority
                                break;

                        case ACCEPTED:
                                title = "🎉 Job Offer Accepted!";
                                subject = String.format("Congratulations! Your Job Offer at '%s' Has Been Confirmed",
                                                jobPosting.getRecruiter().getCompanyName());
                                message = String.format(
                                                "🎉 Congratulations!\n\n" +
                                                                "Your job offer has been officially accepted!\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n\n" +
                                                                "The recruiter will be in touch with details about your start date, "
                                                                +
                                                                "onboarding process, and any documentation needed.\n\n"
                                                                +
                                                                "We're excited for this new chapter in your career!\n\n"
                                                                +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName());
                                priority = 1; // HIGH priority
                                break;

                        case WITHDRAWN:
                                title = "Application Withdrawn";
                                subject = String.format("Your Application for '%s' Has Been Withdrawn",
                                                jobPosting.getTitle());
                                message = String.format(
                                                "Your application has been withdrawn.\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n\n" +
                                                                "If you withdrew this application yourself, no action is needed.\n\n"
                                                                +
                                                                "If this was automatic (e.g., you accepted another job), congratulations "
                                                                +
                                                                "on your new opportunity!\n\n" +
                                                                "Feel free to explore other opportunities on CareerMate.\n\n"
                                                                +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName());
                                priority = 2; // MEDIUM priority
                                break;

                        case NO_RESPONSE:
                                title = "Application Closed - No Response";
                                subject = String.format("Your Application for '%s' Has Been Closed",
                                                jobPosting.getTitle());
                                message = String.format(
                                                "Your application has been closed due to no response.\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n\n" +
                                                                "The recruiter did not receive a response within the expected timeframe. "
                                                                +
                                                                "If you're still interested, you may consider reapplying.\n\n"
                                                                +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName());
                                priority = 3; // LOW priority
                                break;

                        case TERMINATED:
                                title = "Employment Status Updated";
                                subject = String.format("Your Employment Status at '%s' Has Changed",
                                                jobPosting.getRecruiter().getCompanyName());
                                message = String.format(
                                                "Your employment status has been updated.\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n" +
                                                                "Status: Employment Ended\n\n" +
                                                                "We're sorry to see this chapter close. " +
                                                                "CareerMate is here to help you find your next opportunity.\n\n"
                                                                +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName());
                                priority = 2; // MEDIUM priority
                                break;

                        case PROBATION_FAILED:
                                title = "Probation Period Update";
                                subject = String.format("Probation Period Update at '%s'",
                                                jobPosting.getRecruiter().getCompanyName());
                                message = String.format(
                                                "Your employment status has been updated.\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n" +
                                                                "Status: Probation Period Not Completed\n\n" +
                                                                "We encourage you to continue developing your skills " +
                                                                "and exploring new opportunities on CareerMate.\n\n" +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName());
                                priority = 2; // MEDIUM priority
                                break;

                        default:
                                title = "Application Status Updated";
                                subject = String.format("Your Application Status for '%s' Has Been Updated",
                                                jobPosting.getTitle());
                                message = String.format(
                                                "Your application status has been updated.\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n" +
                                                                "New Status: %s\n\n" +
                                                                "You can check your application details in your candidate dashboard.\n\n"
                                                                +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName(),
                                                newStatus.name());
                                priority = 3; // LOW priority
                                break;
                }

                NotificationEvent event = NotificationEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(NotificationEvent.EventType.APPLICATION_STATUS_CHANGED.name())
                                .recipientId(candidateEmail) // Use email for SSE connection matching
                                .recipientEmail(candidateEmail)
                                .title(title)
                                .subject(subject)
                                .message(message)
                                .category("JOB_APPLICATION")
                                .metadata(metadata)
                                .timestamp(LocalDateTime.now())
                                .priority(priority)
                                .build();

                notificationProducer.sendNotification("candidate-notifications", event);
                log.info("✅ Sent application status change notification to candidate {} for status: {}", candidateId,
                                newStatus);
        }

        // ==================== STATUS TRANSITION VALIDATION ====================

        /**
         * Validate if status transition is allowed
         * Prevents invalid state changes (e.g., REJECTED → APPROVED)
         */
        private boolean isValidStatusTransition(StatusJobApply from, StatusJobApply to) {
                // Same status is always allowed (no-op)
                if (from == to) {
                        return true;
                }

                // Define allowed transitions
                switch (from) {
                        case SUBMITTED:
                                // Can move to reviewing, interviewed, approved, rejected, or no response
                                return to == StatusJobApply.REVIEWING
                                                || to == StatusJobApply.INTERVIEW_SCHEDULED
                                                || to == StatusJobApply.APPROVED
                                                || to == StatusJobApply.REJECTED
                                                || to == StatusJobApply.NO_RESPONSE
                                                || to == StatusJobApply.WITHDRAWN;

                        case REVIEWING:
                                // Can move to interview scheduled, approved, rejected
                                return to == StatusJobApply.INTERVIEW_SCHEDULED
                                                || to == StatusJobApply.APPROVED
                                                || to == StatusJobApply.REJECTED
                                                || to == StatusJobApply.WITHDRAWN;

                        case INTERVIEW_SCHEDULED:
                                // Can move to interviewed, approved, rejected
                                return to == StatusJobApply.INTERVIEWED
                                                || to == StatusJobApply.APPROVED
                                                || to == StatusJobApply.REJECTED
                                                || to == StatusJobApply.WITHDRAWN;

                        case INTERVIEWED:
                                // Can move to approved, rejected, or schedule another interview
                                return to == StatusJobApply.INTERVIEW_SCHEDULED
                                                || to == StatusJobApply.APPROVED
                                                || to == StatusJobApply.REJECTED;

                        case APPROVED:
                                // Recruiter marks candidate as WORKING when they start the job
                                // Or rejected if offer declined/changed mind
                                return to == StatusJobApply.WORKING
                                                || to == StatusJobApply.REJECTED
                                                || to == StatusJobApply.WITHDRAWN;

                        case WORKING:
                                // Currently employed - can be terminated or probation failed
                                return to == StatusJobApply.TERMINATED
                                                || to == StatusJobApply.PROBATION_FAILED
                                                || to == StatusJobApply.BANNED;

                        case ACCEPTED:
                                // Legacy status - can transition to WORKING or terminal states
                                return to == StatusJobApply.WORKING
                                                || to == StatusJobApply.BANNED;

                        case REJECTED:
                        case BANNED:
                        case NO_RESPONSE:
                        case WITHDRAWN:
                        case TERMINATED:
                        case PROBATION_FAILED:
                                // Terminal states - cannot transition out
                                return false;

                        default:
                                return false;
                }
        }

        /**
         * Auto-set timestamps when status changes
         */
        private void updateTimestampsForStatus(JobApply jobApply, StatusJobApply newStatus) {
                LocalDateTime now = LocalDateTime.now();

                switch (newStatus) {
                        case INTERVIEW_SCHEDULED:
                                if (jobApply.getInterviewScheduledAt() == null) {
                                        jobApply.setInterviewScheduledAt(now);
                                }
                                jobApply.setLastContactAt(now);
                                break;

                        case INTERVIEWED:
                                if (jobApply.getInterviewedAt() == null) {
                                        jobApply.setInterviewedAt(now);
                                }
                                jobApply.setLastContactAt(now);
                                break;

                        case WORKING:
                                // Candidate started working - set hiredAt timestamp
                                if (jobApply.getHiredAt() == null) {
                                        jobApply.setHiredAt(now);
                                }
                                jobApply.setLastContactAt(now);
                                break;

                        case ACCEPTED:
                                // Legacy status - also set hiredAt
                                if (jobApply.getHiredAt() == null) {
                                        jobApply.setHiredAt(now);
                                }
                                jobApply.setLastContactAt(now);
                                break;

                        case TERMINATED:
                        case PROBATION_FAILED:
                                // Employment ended - set leftAt timestamp
                                if (jobApply.getLeftAt() == null) {
                                        jobApply.setLeftAt(now);
                                }
                                jobApply.setLastContactAt(now);
                                break;

                        case REVIEWING:
                        case APPROVED:
                        case REJECTED:
                                jobApply.setLastContactAt(now);
                                break;

                        case NO_RESPONSE:
                                // Don't update lastContactAt - that's the point
                                break;
                }
        }

        /**
         * Record status change in history table
         */
        private void recordStatusChange(JobApply jobApply, StatusJobApply previousStatus,
                        StatusJobApply newStatus, Integer changedByUserId, String reason) {
                JobApplyStatusHistory history = JobApplyStatusHistory.builder()
                                .jobApply(jobApply)
                                .previousStatus(previousStatus)
                                .newStatus(newStatus)
                                .changedAt(LocalDateTime.now())
                                .changedByUserId(changedByUserId)
                                .changeReason(reason)
                                .build();

                statusHistoryRepo.save(history);
                log.info("📝 Recorded status change: JobApply {} from {} to {}",
                                jobApply.getId(), previousStatus, newStatus);
        }

        // ==================== AUTO-WITHDRAW ON HIRE ====================

        /**
         * Active statuses that should be auto-withdrawn when candidate is hired
         * elsewhere.
         * These represent applications that are still "in progress" and not yet
         * finalized.
         */
        private static final List<StatusJobApply> ACTIVE_PENDING_STATUSES = List.of(
                        StatusJobApply.SUBMITTED,
                        StatusJobApply.REVIEWING,
                        StatusJobApply.INTERVIEW_SCHEDULED,
                        StatusJobApply.INTERVIEWED,
                        StatusJobApply.APPROVED);

        /**
         * Handle business rules when a candidate is hired (status changed to ACCEPTED).
         * This automatically withdraws all other pending applications for the same
         * candidate.
         * 
         * Business Rule: When a candidate accepts a job offer, all their other active
         * applications
         * are automatically withdrawn to prevent conflicts and maintain data integrity.
         * 
         * @param hiredApplication The application that was just marked as
         *                         ACCEPTED/hired
         */
        private void handleHireBusinessRules(JobApply hiredApplication) {
                Integer candidateId = hiredApplication.getCandidate().getCandidateId();
                Integer hiredApplicationId = hiredApplication.getId();
                String hiredJobTitle = hiredApplication.getJobPosting().getTitle();
                String hiredCompanyName = hiredApplication.getJobPosting().getRecruiter().getCompanyName();

                log.info("🎯 Processing hire business rules for candidate {} hired at {}",
                                candidateId, hiredCompanyName);

                // Find all other active/pending applications for this candidate
                List<JobApply> pendingApplications = jobApplyRepo.findActivePendingApplicationsByCandidate(
                                candidateId,
                                hiredApplicationId,
                                ACTIVE_PENDING_STATUSES);

                if (pendingApplications.isEmpty()) {
                        log.info("No pending applications to withdraw for candidate {}", candidateId);
                        return;
                }

                log.info("Found {} pending applications to auto-withdraw for candidate {}",
                                pendingApplications.size(), candidateId);

                int withdrawnCount = 0;
                for (JobApply application : pendingApplications) {
                        try {
                                StatusJobApply previousStatus = application.getStatus();

                                // Update status to WITHDRAWN
                                application.setStatus(StatusJobApply.WITHDRAWN);
                                application.setStatusChangedAt(LocalDateTime.now());
                                jobApplyRepo.save(application);

                                // Record in history with reason
                                String withdrawReason = String.format(
                                                "Auto-withdrawn: Candidate hired for '%s' at %s",
                                                hiredJobTitle, hiredCompanyName);
                                recordStatusChange(application, previousStatus, StatusJobApply.WITHDRAWN,
                                                null, withdrawReason);

                                // Send notification to recruiter about auto-withdrawal
                                sendAutoWithdrawNotificationToRecruiter(application, hiredJobTitle, hiredCompanyName);

                                // Cancel any scheduled interviews for this application
                                cancelInterviewIfExists(application, hiredJobTitle, hiredCompanyName);

                                withdrawnCount++;
                                log.info("✅ Auto-withdrew application {} for job '{}' (was: {})",
                                                application.getId(),
                                                application.getJobPosting().getTitle(),
                                                previousStatus);

                        } catch (Exception e) {
                                log.error("Failed to auto-withdraw application {}: {}",
                                                application.getId(), e.getMessage());
                                // Continue processing other applications
                        }
                }

                log.info("🎉 Auto-withdrew {} of {} pending applications for candidate {}",
                                withdrawnCount, pendingApplications.size(), candidateId);

                // Send summary notification to candidate about auto-withdrawals
                if (withdrawnCount > 0) {
                        sendAutoWithdrawSummaryToCandidate(hiredApplication, withdrawnCount);
                }
        }

        /**
         * Send notification to recruiter when a candidate auto-withdraws from their job
         * posting.
         */
        private void sendAutoWithdrawNotificationToRecruiter(JobApply withdrawnApplication,
                        String hiredJobTitle, String hiredCompanyName) {
                String recruiterEmail = withdrawnApplication.getJobPosting().getRecruiter().getAccount().getEmail();
                Integer recruiterId = withdrawnApplication.getJobPosting().getRecruiter().getId();

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("applicationId", withdrawnApplication.getId());
                metadata.put("jobPostingId", withdrawnApplication.getJobPosting().getId());
                metadata.put("jobTitle", withdrawnApplication.getJobPosting().getTitle());
                metadata.put("candidateName", withdrawnApplication.getFullName());
                metadata.put("reason", "HIRED_ELSEWHERE");
                metadata.put("hiredJobTitle", hiredJobTitle);
                metadata.put("hiredCompanyName", hiredCompanyName);

                String message = String.format(
                                "A candidate has withdrawn their application.\n\n" +
                                                "📋 Job Posting: %s\n" +
                                                "👤 Candidate: %s\n" +
                                                "ℹ️ Reason: Candidate has been hired at another company (%s)\n\n" +
                                                "The application status has been automatically updated to WITHDRAWN.",
                                withdrawnApplication.getJobPosting().getTitle(),
                                withdrawnApplication.getFullName(),
                                hiredCompanyName);

                NotificationEvent event = NotificationEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .recipientEmail(recruiterEmail)
                                .recipientId(String.valueOf(recruiterId))
                                .category("RECRUITER")
                                .eventType("APPLICATION_AUTO_WITHDRAWN")
                                .title("Application Withdrawn - Candidate Hired Elsewhere")
                                .subject("Application Withdrawn: " + withdrawnApplication.getFullName())
                                .message(message)
                                .metadata(metadata)
                                .timestamp(LocalDateTime.now())
                                .build();

                notificationProducer.sendRecruiterNotification(event);
        }

        /**
         * Send summary notification to candidate about their auto-withdrawn
         * applications.
         */
        private void sendAutoWithdrawSummaryToCandidate(JobApply hiredApplication, int withdrawnCount) {
                String candidateEmail = hiredApplication.getCandidate().getAccount().getEmail();
                Integer candidateId = hiredApplication.getCandidate().getCandidateId();
                String hiredCompanyName = hiredApplication.getJobPosting().getRecruiter().getCompanyName();
                String hiredJobTitle = hiredApplication.getJobPosting().getTitle();

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("hiredApplicationId", hiredApplication.getId());
                metadata.put("hiredJobTitle", hiredJobTitle);
                metadata.put("hiredCompanyName", hiredCompanyName);
                metadata.put("withdrawnCount", withdrawnCount);

                String message = String.format(
                                "Congratulations on your new job! 🎉\n\n" +
                                                "You've been hired for the position of '%s' at %s.\n\n" +
                                                "As a result, %d of your other pending application(s) have been automatically withdrawn "
                                                +
                                                "to help you focus on your new opportunity.\n\n" +
                                                "We wish you all the best in your new role!",
                                hiredJobTitle,
                                hiredCompanyName,
                                withdrawnCount);

                NotificationEvent event = NotificationEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .recipientEmail(candidateEmail)
                                .recipientId(String.valueOf(candidateId))
                                .category("CANDIDATE")
                                .eventType("APPLICATIONS_AUTO_WITHDRAWN")
                                .title("Your Applications Have Been Updated")
                                .subject("Congratulations! " + withdrawnCount + " application(s) auto-withdrawn")
                                .message(message)
                                .metadata(metadata)
                                .timestamp(LocalDateTime.now())
                                .build();

                notificationProducer.sendNotification("candidate-notifications", event);
        }

        /**
         * Cancel interview if exists for an application being withdrawn.
         * This ensures ghost interviews don't remain in the system when applications
         * are withdrawn.
         * 
         * @param application      The application being withdrawn
         * @param hiredJobTitle    The job title the candidate was hired for
         * @param hiredCompanyName The company name where candidate was hired
         */
        private void cancelInterviewIfExists(JobApply application, String hiredJobTitle, String hiredCompanyName) {
                try {
                        interviewScheduleRepo.findByJobApplyId(application.getId())
                                        .ifPresent(interview -> {
                                                // Only cancel if interview is not already completed/cancelled/no-show
                                                if (interview.getStatus() == InterviewStatus.SCHEDULED
                                                                || interview.getStatus() == InterviewStatus.CONFIRMED
                                                                || interview.getStatus() == InterviewStatus.RESCHEDULED) {

                                                        InterviewStatus previousStatus = interview.getStatus();
                                                        interview.setStatus(InterviewStatus.CANCELLED);
                                                        interview.setInterviewerNotes(String.format(
                                                                        "Auto-cancelled: Candidate hired for '%s' at %s. Previous status: %s",
                                                                        hiredJobTitle, hiredCompanyName,
                                                                        previousStatus));
                                                        interviewScheduleRepo.save(interview);

                                                        log.info("🗓️ Auto-cancelled interview {} for application {} (was: {})",
                                                                        interview.getId(), application.getId(),
                                                                        previousStatus);

                                                        // Send notification to recruiter about cancelled interview
                                                        sendInterviewCancelledNotification(interview, application,
                                                                        hiredJobTitle, hiredCompanyName);
                                                }
                                        });
                } catch (Exception e) {
                        log.error("Failed to cancel interview for application {}: {}",
                                        application.getId(), e.getMessage());
                        // Don't fail the withdrawal process if interview cancellation fails
                }
        }

        /**
         * Send notification to recruiter when a scheduled interview is auto-cancelled.
         */
        private void sendInterviewCancelledNotification(InterviewSchedule interview, JobApply application,
                        String hiredJobTitle, String hiredCompanyName) {
                String recruiterEmail = application.getJobPosting().getRecruiter().getAccount().getEmail();
                Integer recruiterId = application.getJobPosting().getRecruiter().getId();

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("interviewId", interview.getId());
                metadata.put("applicationId", application.getId());
                metadata.put("jobPostingId", application.getJobPosting().getId());
                metadata.put("jobTitle", application.getJobPosting().getTitle());
                metadata.put("candidateName", application.getFullName());
                metadata.put("scheduledDate", interview.getScheduledDate().toString());
                metadata.put("reason", "CANDIDATE_HIRED_ELSEWHERE");
                metadata.put("hiredJobTitle", hiredJobTitle);
                metadata.put("hiredCompanyName", hiredCompanyName);

                String message = String.format(
                                "⚠️ An interview has been automatically cancelled.\n\n" +
                                                "📋 Job Posting: %s\n" +
                                                "👤 Candidate: %s\n" +
                                                "📅 Scheduled: %s\n" +
                                                "ℹ️ Reason: Candidate has been hired at another company (%s)\n\n" +
                                                "The interview has been removed from your schedule.",
                                application.getJobPosting().getTitle(),
                                application.getFullName(),
                                interview.getScheduledDate()
                                                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")),
                                hiredCompanyName);

                NotificationEvent event = NotificationEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .recipientEmail(recruiterEmail)
                                .recipientId(String.valueOf(recruiterId))
                                .category("RECRUITER")
                                .eventType("INTERVIEW_AUTO_CANCELLED")
                                .title("Interview Cancelled - Candidate Hired Elsewhere")
                                .subject("Interview Cancelled: " + application.getFullName())
                                .message(message)
                                .metadata(metadata)
                                .timestamp(LocalDateTime.now())
                                .build();

                notificationProducer.sendNotification("recruiter-notifications", event);
        }

        /**
         * Cancel interview when candidate manually withdraws their application.
         * Different from auto-withdrawal (hired elsewhere), this is
         * candidate-initiated.
         * 
         * @param application The application being withdrawn by candidate
         */
        private void cancelInterviewOnManualWithdrawal(JobApply application) {
                try {
                        interviewScheduleRepo.findByJobApplyId(application.getId())
                                        .ifPresent(interview -> {
                                                // Only cancel if interview is not already completed/cancelled/no-show
                                                if (interview.getStatus() == InterviewStatus.SCHEDULED
                                                                || interview.getStatus() == InterviewStatus.CONFIRMED
                                                                || interview.getStatus() == InterviewStatus.RESCHEDULED) {

                                                        InterviewStatus previousStatus = interview.getStatus();
                                                        interview.setStatus(InterviewStatus.CANCELLED);
                                                        interview.setInterviewerNotes(String.format(
                                                                        "Cancelled: Candidate withdrew application. Previous status: %s",
                                                                        previousStatus));
                                                        interviewScheduleRepo.save(interview);

                                                        log.info("🗓️ Cancelled interview {} for withdrawn application {} (was: {})",
                                                                        interview.getId(), application.getId(),
                                                                        previousStatus);

                                                        // Notify recruiter about cancelled interview
                                                        sendManualWithdrawInterviewCancelledNotification(interview,
                                                                        application);
                                                }
                                        });
                } catch (Exception e) {
                        log.error("Failed to cancel interview for withdrawn application {}: {}",
                                        application.getId(), e.getMessage());
                }
        }

        /**
         * Send notification to recruiter when interview is cancelled due to candidate
         * withdrawal.
         */
        private void sendManualWithdrawInterviewCancelledNotification(InterviewSchedule interview,
                        JobApply application) {
                String recruiterEmail = application.getJobPosting().getRecruiter().getAccount().getEmail();
                Integer recruiterId = application.getJobPosting().getRecruiter().getId();

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("interviewId", interview.getId());
                metadata.put("applicationId", application.getId());
                metadata.put("jobPostingId", application.getJobPosting().getId());
                metadata.put("jobTitle", application.getJobPosting().getTitle());
                metadata.put("candidateName", application.getFullName());
                metadata.put("scheduledDate", interview.getScheduledDate().toString());
                metadata.put("reason", "CANDIDATE_WITHDREW");

                String message = String.format(
                                "⚠️ An interview has been cancelled.\n\n" +
                                                "📋 Job Posting: %s\n" +
                                                "👤 Candidate: %s\n" +
                                                "📅 Was Scheduled: %s\n" +
                                                "ℹ️ Reason: Candidate withdrew their application\n\n" +
                                                "The interview has been removed from your schedule.",
                                application.getJobPosting().getTitle(),
                                application.getFullName(),
                                interview.getScheduledDate()
                                                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")));

                NotificationEvent event = NotificationEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .recipientEmail(recruiterEmail)
                                .recipientId(String.valueOf(recruiterId))
                                .category("RECRUITER")
                                .eventType("INTERVIEW_CANCELLED")
                                .title("Interview Cancelled - Candidate Withdrew")
                                .subject("Interview Cancelled: " + application.getFullName())
                                .message(message)
                                .metadata(metadata)
                                .timestamp(LocalDateTime.now())
                                .build();

                notificationProducer.sendNotification("recruiter-notifications", event);
        }

        // Get current recruiter helper method
        private Recruiter getMyRecruiter() {
                Account currentAccount = authenticationImp.findByEmail();
                Recruiter recruiter = recruiterRepo.findByAccount_Id(currentAccount.getId())
                                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

                // Check if recruiter is verified (APPROVED status)
                if (!"APPROVED".equals(recruiter.getVerificationStatus())) {
                        throw new AppException(ErrorCode.RECRUITER_NOT_VERIFIED);
                }

                return recruiter;
        }

        @Override
        @PreAuthorize("hasRole('RECRUITER')")
        public List<JobApplyResponse> getJobAppliesByRecruiter() {
                Recruiter recruiter = getMyRecruiter();
                return jobApplyRepo.findByRecruiterId(recruiter.getId()).stream()
                                .map(jobApplyMapper::toJobApplyResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @PreAuthorize("hasRole('RECRUITER')")
        public PageResponse<JobApplyResponse> getJobAppliesByRecruiterWithFilter(
                        StatusJobApply status,
                        int page,
                        int size) {
                Recruiter recruiter = getMyRecruiter();

                Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
                Page<JobApply> jobApplyPage = jobApplyRepo.findByRecruiterId(recruiter.getId(), pageable);

                // Filter by status if provided
                List<JobApplyResponse> filteredList = jobApplyPage.getContent().stream()
                                .filter(ja -> status == null || ja.getStatus() == status)
                                .map(jobApplyMapper::toJobApplyResponse)
                                .collect(Collectors.toList());

                return new PageResponse<>(
                                filteredList,
                                page,
                                size,
                                jobApplyPage.getTotalElements(),
                                jobApplyPage.getTotalPages());
        }
}
