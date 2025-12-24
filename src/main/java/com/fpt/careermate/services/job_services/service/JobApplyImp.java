package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.constant.InterviewStatus;
import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.common.util.SecurityUtil;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.job_services.domain.InterviewSchedule;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.domain.EmploymentVerification;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.job_services.repository.InterviewScheduleRepo;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.job_services.repository.EmploymentVerificationRepo;
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

import java.time.LocalDate;
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
        AuthenticationImp authenticationImp;
        RecruiterRepo recruiterRepo;
        InterviewScheduleRepo interviewScheduleRepo;
        SecurityUtil securityUtil;
        EmploymentVerificationRepo employmentVerificationRepo;

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

                // Check if already has an active application (in the flow)
                // Only block if existing application is in an active status
                // Allow re-application if previous was: REJECTED, WITHDRAWN, NO_RESPONSE, TERMINATED
                jobApplyRepo.findByJobPostingIdAndCandidateCandidateId(
                                request.getJobPostingId(), request.getCandidateId())
                                .ifPresent(existing -> {
                                        StatusJobApply existingStatus = existing.getStatus();
                                        // Active statuses that block new applications
                                        boolean isActiveApplication = existingStatus == StatusJobApply.SUBMITTED
                                                        || existingStatus == StatusJobApply.REVIEWING
                                                        || existingStatus == StatusJobApply.INTERVIEW_SCHEDULED
                                                        || existingStatus == StatusJobApply.INTERVIEWED
                                                        || existingStatus == StatusJobApply.APPROVED
                                                        || existingStatus == StatusJobApply.OFFER_EXTENDED
                                                        || existingStatus == StatusJobApply.ACCEPTED
                                                        || existingStatus == StatusJobApply.WORKING
                                                        || existingStatus == StatusJobApply.BANNED;
                                        
                                        if (isActiveApplication) {
                                                throw new AppException(ErrorCode.ALREADY_APPLIED_TO_JOB_POSTING);
                                        }
                                        // If REJECTED, WITHDRAWN, NO_RESPONSE, or TERMINATED - allow re-application
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

                // Flush early so we don't send notifications for a DB write that later fails on commit
                JobApply savedJobApply = jobApplyRepo.saveAndFlush(jobApply);
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

                // Note: Platform allows multiple employments - we don't block offers/hiring
                // Candidates may have multiple jobs (freelance, part-time, consulting, etc.)
                // The platform's role is to connect, not enforce employment exclusivity

                // Update status and relevant timestamps
                jobApply.setStatus(status);
                jobApply.setStatusChangedAt(LocalDateTime.now());

                // Auto-set timestamps based on status
                updateTimestampsForStatus(jobApply, status);

                // Flush early so we don't send notifications for a DB write that later fails on commit
                JobApply updatedJobApply = jobApplyRepo.saveAndFlush(jobApply);

                log.info("Job application ID: {} updated from {} to {}", id, previousStatus, status);

                // Send notification to candidate about status change
                try {
                        sendApplicationStatusChangeNotification(updatedJobApply, previousStatus, status);
                } catch (Exception e) {
                        log.error("Failed to send application status change notification: {}", e.getMessage(), e);
                        // Don't fail the update process if notification fails
                }

                // Note: Auto-withdrawal feature removed - platform is neutral and doesn't
                // make decisions on behalf of candidates. They can manually withdraw if needed.

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
                                // Skip notification for REVIEWING status - too insignificant to notify candidate
                                log.info("Skipping notification for REVIEWING status - insignificant status change");
                                return;

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

                        case OFFER_EXTENDED:
                                title = "🎉 Job Offer Received!";
                                subject = String.format("Congratulations! You've Received a Job Offer from '%s'",
                                                jobPosting.getRecruiter().getCompanyName());
                                message = String.format(
                                                "🎉 Congratulations! You've received a job offer!\n\n" +
                                                                "Job Position: %s\n" +
                                                                "Company: %s\n\n" +
                                                                "The recruiter has extended a job offer to you. " +
                                                                "Please review the offer and confirm your acceptance or decline.\n\n" +
                                                                "⏰ Important: Please respond to this offer as soon as possible.\n\n" +
                                                                "You can confirm or decline the offer from your candidate dashboard.\n\n" +
                                                                "Best regards,\n" +
                                                                "CareerMate Team",
                                                jobPosting.getTitle(),
                                                jobPosting.getRecruiter().getCompanyName());
                                priority = 1; // HIGH priority - urgent action needed
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
                                // Recruiter extends offer to candidate (OFFER_EXTENDED)
                                // Or rejected if changed mind, or candidate withdraws
                                return to == StatusJobApply.OFFER_EXTENDED
                                                || to == StatusJobApply.REJECTED
                                                || to == StatusJobApply.WITHDRAWN;

                        case OFFER_EXTENDED:
                                // Candidate confirms offer → WORKING
                                // Candidate declines → REJECTED
                                // Candidate withdraws → WITHDRAWN
                                return to == StatusJobApply.WORKING
                                                || to == StatusJobApply.REJECTED
                                                || to == StatusJobApply.WITHDRAWN;

                        case WORKING:
                                // Currently employed - can only be terminated or banned
                                return to == StatusJobApply.TERMINATED
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
                                // Employment ended - set leftAt timestamp
                                if (jobApply.getLeftAt() == null) {
                                        jobApply.setLeftAt(now);
                                }
                                jobApply.setLastContactAt(now);
                                break;

                        case REVIEWING:
                        case APPROVED:
                        case OFFER_EXTENDED:
                        case REJECTED:
                                jobApply.setLastContactAt(now);
                                break;

                        case NO_RESPONSE:
                                // Don't update lastContactAt - that's the point
                                break;
                }
        }

        // ==================== MANUAL WITHDRAWAL (CANDIDATE-INITIATED) ====================
        // Note: Auto-withdrawal feature has been removed.
        // Platform philosophy: CareerMate is a job matching platform, not an HR system.
        // The platform should not make decisions on behalf of candidates.
        // Candidates can have multiple jobs (freelance, part-time, consulting, etc.)
        // and should manually manage their applications.

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

        // ==================== CANDIDATE OFFER CONFIRMATION (v3.1) ====================

        /**
         * Candidate confirms a job offer - transitions from OFFER_EXTENDED to WORKING
         * Platform-neutral (v3.2): Allows multiple simultaneous employments.
         * 
         * This validates that:
         * 1. The application belongs to the current authenticated candidate
         * 2. The application is in OFFER_EXTENDED status
         * 
         * Note: No restriction on multiple employments - candidates can have multiple
         * active jobs (freelance, part-time, consulting, contract work, etc.)
         */
        @Override
        @Transactional
        @PreAuthorize("hasRole('CANDIDATE')")
        public JobApplyResponse confirmOffer(int jobApplyId) {
                // Get current authenticated candidate
                Candidate candidate = getMyCandidate();
                
                // Find the job application
                JobApply jobApply = jobApplyRepo.findById(jobApplyId)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));
                
                // Verify this application belongs to the current candidate
                if (jobApply.getCandidate().getCandidateId() != candidate.getCandidateId()) {
                        throw new AppException(ErrorCode.UNAUTHORIZED);
                }
                
                // Verify application is in OFFER_EXTENDED status
                if (jobApply.getStatus() != StatusJobApply.OFFER_EXTENDED) {
                        throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
                }
                
                // Note: Platform allows multiple employments - candidates may have multiple jobs
                // (freelance, part-time, consulting, contract work, etc.)
                // The platform's role is to facilitate connections, not enforce employment exclusivity
                
                StatusJobApply previousStatus = jobApply.getStatus();
                
                // Update to WORKING status
                jobApply.setStatus(StatusJobApply.WORKING);
                jobApply.setStatusChangedAt(LocalDateTime.now());
                jobApply.setHiredAt(LocalDateTime.now());
                jobApply.setLastContactAt(LocalDateTime.now());
                
                JobApply updatedJobApply = jobApplyRepo.save(jobApply);
                
                // Create EmploymentVerification record with startDate = today
                try {
                        // Check if employment verification already exists
                        if (employmentVerificationRepo.findByJobApplyId(jobApplyId).isEmpty()) {
                                // Get recruiter from job posting
                                var recruiter = updatedJobApply.getJobPosting().getRecruiter();
                                
                                EmploymentVerification employmentVerification = EmploymentVerification.builder()
                                        .jobApply(updatedJobApply)
                                        .createdByRecruiter(recruiter)  // Required field - recruiter who posted the job
                                        .startDate(LocalDate.now())
                                        .isActive(true)
                                        .isProbation(false)  // Candidate is not on probation initially
                                        .createdAt(LocalDateTime.now())
                                        .build();
                                employmentVerificationRepo.save(employmentVerification);
                                log.info("✅ Created employment verification for application {} with start date: {}", 
                                        jobApplyId, LocalDate.now());
                        }
                } catch (Exception e) {
                        log.error("Failed to create employment verification for application {}: {}", 
                                jobApplyId, e.getMessage(), e);
                }
                
                log.info("✅ Candidate {} confirmed job offer for application {}. Status: OFFER_EXTENDED → WORKING",
                                candidate.getCandidateId(), jobApplyId);
                
                // Send notification to candidate about employment start
                try {
                        sendApplicationStatusChangeNotification(updatedJobApply, previousStatus, StatusJobApply.WORKING);
                } catch (Exception e) {
                        log.error("Failed to send offer confirmation notification: {}", e.getMessage(), e);
                }
                
                // Note: Auto-withdrawal feature removed - platform is neutral and doesn't
                // make decisions on behalf of candidates. Other applications remain active.
                // Candidates can manually withdraw applications if they choose to.
                
                // Send notification to recruiter that candidate accepted
                try {
                        sendOfferAcceptedNotificationToRecruiter(updatedJobApply);
                } catch (Exception e) {
                        log.error("Failed to send offer accepted notification to recruiter: {}", e.getMessage(), e);
                }
                
                return jobApplyMapper.toJobApplyResponse(updatedJobApply);
        }

        // ==================== CANDIDATE TERMINATE EMPLOYMENT (v3.2) ====================

        /**
         * Candidate ends current employment. Transitions WORKING/ACCEPTED -> TERMINATED and closes EmploymentVerification.
         */
        @Override
        @Transactional
        @PreAuthorize("hasRole('CANDIDATE')")
        public JobApplyResponse terminateEmployment(int jobApplyId) {
                Candidate candidate = getMyCandidate();

                JobApply jobApply = jobApplyRepo.findById(jobApplyId)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

                if (jobApply.getCandidate().getCandidateId() != candidate.getCandidateId()) {
                        throw new AppException(ErrorCode.UNAUTHORIZED);
                }

                if (jobApply.getStatus() != StatusJobApply.WORKING
                                && jobApply.getStatus() != StatusJobApply.ACCEPTED) {
                        throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
                }

                StatusJobApply previousStatus = jobApply.getStatus();

                jobApply.setStatus(StatusJobApply.TERMINATED);
                jobApply.setStatusChangedAt(LocalDateTime.now());
                jobApply.setLeftAt(LocalDateTime.now());
                jobApply.setLastContactAt(LocalDateTime.now());

                JobApply updated = jobApplyRepo.save(jobApply);

                employmentVerificationRepo.findByJobApplyId(jobApplyId).ifPresent(ev -> {
                        ev.setIsActive(false);
                        ev.setEndDate(LocalDate.now());
                        ev.setUpdatedAt(LocalDateTime.now());
                        employmentVerificationRepo.save(ev);
                });

                try {
                        sendApplicationStatusChangeNotification(updated, previousStatus, StatusJobApply.TERMINATED);
                } catch (Exception e) {
                        log.error("Failed to send termination notification: {}", e.getMessage(), e);
                }

                return jobApplyMapper.toJobApplyResponse(updated);
        }

        /**
         * Candidate declines a job offer - transitions from OFFER_EXTENDED to REJECTED
         */
        @Override
        @Transactional
        @PreAuthorize("hasRole('CANDIDATE')")
        public JobApplyResponse declineOffer(int jobApplyId) {
                // Get current authenticated candidate
                Candidate candidate = getMyCandidate();
                
                // Find the job application
                JobApply jobApply = jobApplyRepo.findById(jobApplyId)
                                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));
                
                // Verify this application belongs to the current candidate
                if (jobApply.getCandidate().getCandidateId() != candidate.getCandidateId()) {
                        throw new AppException(ErrorCode.UNAUTHORIZED);
                }
                
                // Verify application is in OFFER_EXTENDED status
                if (jobApply.getStatus() != StatusJobApply.OFFER_EXTENDED) {
                        throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
                }
                
                StatusJobApply previousStatus = jobApply.getStatus();
                
                // Update to WITHDRAWN status (candidate declined)
                jobApply.setStatus(StatusJobApply.WITHDRAWN);
                jobApply.setStatusChangedAt(LocalDateTime.now());
                jobApply.setLastContactAt(LocalDateTime.now());
                
                JobApply updatedJobApply = jobApplyRepo.save(jobApply);
                
                log.info("❌ Candidate {} declined job offer for application {}. Status: OFFER_EXTENDED → WITHDRAWN",
                                candidate.getCandidateId(), jobApplyId);
                
                // Send notification to recruiter that candidate declined
                try {
                        sendOfferDeclinedNotificationToRecruiter(updatedJobApply);
                } catch (Exception e) {
                        log.error("Failed to send offer declined notification to recruiter: {}", e.getMessage(), e);
                }
                
                return jobApplyMapper.toJobApplyResponse(updatedJobApply);
        }

        /**
         * Get the current authenticated candidate
         */
        private Candidate getMyCandidate() {
                Integer accountId = securityUtil.getCurrentUserId();
                return candidateRepo.findByAccount_Id(accountId)
                                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));
        }

        /**
         * Send notification to recruiter when candidate accepts offer
         */
        private void sendOfferAcceptedNotificationToRecruiter(JobApply jobApply) {
                JobPosting jobPosting = jobApply.getJobPosting();
                Recruiter recruiter = jobPosting.getRecruiter();
                String recruiterEmail = recruiter.getAccount().getEmail();
                
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("applicationId", jobApply.getId());
                metadata.put("jobPostingId", jobPosting.getId());
                metadata.put("jobTitle", jobPosting.getTitle());
                metadata.put("candidateId", jobApply.getCandidate().getCandidateId());
                metadata.put("candidateName", jobApply.getFullName());
                metadata.put("status", "OFFER_ACCEPTED");
                
                String message = String.format(
                                "🎉 Great news! Your job offer has been accepted!\n\n" +
                                                "Job Position: %s\n" +
                                                "Candidate: %s\n\n" +
                                                "The candidate has confirmed their acceptance and is now marked as WORKING.\n\n" +
                                                "Next steps:\n" +
                                                "- Coordinate onboarding details with the new hire\n" +
                                                "- Prepare necessary documentation\n" +
                                                "- Welcome them to the team!\n\n" +
                                                "Best regards,\n" +
                                                "CareerMate Team",
                                jobPosting.getTitle(),
                                jobApply.getFullName());
                
                NotificationEvent event = NotificationEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(NotificationEvent.EventType.OFFER_ACCEPTED.name())
                                .recipientId(recruiterEmail)
                                .recipientEmail(recruiterEmail)
                                .title("🎉 Job Offer Accepted!")
                                .subject(String.format("Job Offer Accepted: %s - %s", 
                                                jobPosting.getTitle(), jobApply.getFullName()))
                                .message(message)
                                .category("JOB_APPLICATION")
                                .metadata(metadata)
                                .timestamp(LocalDateTime.now())
                                .priority(1) // HIGH priority
                                .build();
                
                notificationProducer.sendNotification("recruiter-notifications", event);
                log.info("✅ Sent offer accepted notification to recruiter {} for application {}",
                                recruiter.getId(), jobApply.getId());
        }

        /**
         * Send notification to recruiter when candidate declines offer
         */
        private void sendOfferDeclinedNotificationToRecruiter(JobApply jobApply) {
                JobPosting jobPosting = jobApply.getJobPosting();
                Recruiter recruiter = jobPosting.getRecruiter();
                String recruiterEmail = recruiter.getAccount().getEmail();
                
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("applicationId", jobApply.getId());
                metadata.put("jobPostingId", jobPosting.getId());
                metadata.put("jobTitle", jobPosting.getTitle());
                metadata.put("candidateId", jobApply.getCandidate().getCandidateId());
                metadata.put("candidateName", jobApply.getFullName());
                metadata.put("status", "OFFER_DECLINED");
                
                String message = String.format(
                                "Unfortunately, your job offer was declined.\n\n" +
                                                "Job Position: %s\n" +
                                                "Candidate: %s\n\n" +
                                                "The candidate has chosen to decline the offer.\n\n" +
                                                "Don't worry - you can continue reviewing other candidates " +
                                                "or use CareerMate's AI matching to find more suitable candidates.\n\n" +
                                                "Best regards,\n" +
                                                "CareerMate Team",
                                jobPosting.getTitle(),
                                jobApply.getFullName());
                
                NotificationEvent event = NotificationEvent.builder()
                                .eventId(UUID.randomUUID().toString())
                                .eventType(NotificationEvent.EventType.OFFER_DECLINED.name())
                                .recipientId(recruiterEmail)
                                .recipientEmail(recruiterEmail)
                                .title("Job Offer Declined")
                                .subject(String.format("Job Offer Declined: %s - %s", 
                                                jobPosting.getTitle(), jobApply.getFullName()))
                                .message(message)
                                .category("JOB_APPLICATION")
                                .metadata(metadata)
                                .timestamp(LocalDateTime.now())
                                .priority(2) // MEDIUM priority
                                .build();
                
                notificationProducer.sendNotification("recruiter-notifications", event);
                log.info("✅ Sent offer declined notification to recruiter {} for application {}",
                                recruiter.getId(), jobApply.getId());
        }
}
