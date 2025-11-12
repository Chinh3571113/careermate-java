package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.job_services.service.dto.request.JobApplyRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobApplyResponse;
import com.fpt.careermate.services.job_services.service.impl.JobApplyService;
import com.fpt.careermate.services.job_services.service.mapper.JobApplyMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.kafka.producer.NotificationProducer;
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

    @Override
    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    public JobApplyResponse createJobApply(JobApplyRequest request) {
        log.info("Candidate {} applying to job posting ID: {}", request.getCandidateId(), request.getJobPostingId());

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
        log.info("Job application created with ID: {} for job: {}", savedJobApply.getId(), jobPosting.getTitle());

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
        Page<JobApply> jobApplyPage=jobApplyRepo.findByCandidateIdAndStatus(candidateId, status, pageable);
        if(jobApplyPage.getTotalElements() == 0){
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
                jobApplyPage.getTotalPages()
        );
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('RECRUITER')")
    public JobApplyResponse updateJobApply(int id, StatusJobApply status) {
        log.info("Recruiter updating job application ID: {} to status: {}", id, status);

        JobApply jobApply = jobApplyRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        StatusJobApply previousStatus = jobApply.getStatus();

        // Update status if provided
        jobApply.setStatus(status);

        JobApply updatedJobApply = jobApplyRepo.save(jobApply);
        log.info("Job application ID: {} updated from {} to {}", id, previousStatus, status);

        // Send notification to candidate about status change
        try {
            sendApplicationStatusChangeNotification(updatedJobApply, previousStatus, status);
        } catch (Exception e) {
            log.error("Failed to send application status change notification: {}", e.getMessage(), e);
            // Don't fail the update process if notification fails
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
    private void sendApplicationReceivedNotification(JobApply jobApply, JobPosting jobPosting, Candidate candidate) {
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
                "Please review the application and CV in your recruiter dashboard.\n\n" +
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
                        : "No cover letter provided."
        );

        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(NotificationEvent.EventType.APPLICATION_RECEIVED.name())
                .recipientId(String.valueOf(recruiterId))
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
        log.info("✅ Sent new application notification to recruiter {} for job: {}", recruiterId, jobPosting.getTitle());
    }

    /**
     * Send notification to candidate when recruiter updates their application status
     */
    private void sendApplicationStatusChangeNotification(JobApply jobApply, StatusJobApply previousStatus, StatusJobApply newStatus) {
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
                subject = String.format("Congratulations! Your Application for '%s' Has Been Accepted", jobPosting.getTitle());
                message = String.format(
                        "Great news! Your application has been accepted!\n\n" +
                        "Job Position: %s\n" +
                        "Company: %s\n" +
                        "Location: %s\n\n" +
                        "The recruiter will contact you soon to discuss the next steps.\n\n" +
                        "What's next?\n" +
                        "- Keep an eye on your email and phone for contact from the recruiter\n" +
                        "- Prepare for potential interviews\n" +
                        "- Review the job description and company information\n\n" +
                        "Best of luck with your interview!\n\n" +
                        "Best regards,\n" +
                        "CareerMate Team",
                        jobPosting.getTitle(),
                        jobPosting.getRecruiter().getCompanyName(),
                        jobPosting.getAddress()
                );
                priority = 1; // HIGH priority
                break;

            case REJECTED:
                title = "Application Update";
                subject = String.format("Update on Your Application for '%s'", jobPosting.getTitle());
                message = String.format(
                        "Thank you for your interest in the position.\n\n" +
                        "Job Position: %s\n" +
                        "Company: %s\n\n" +
                        "Unfortunately, after careful consideration, we have decided to move forward with other candidates " +
                        "whose qualifications more closely match our current needs.\n\n" +
                        "We appreciate the time you invested in the application process and encourage you to:\n" +
                        "- Keep exploring other opportunities on CareerMate\n" +
                        "- Continue building your skills and experience\n" +
                        "- Apply for other positions that match your profile\n\n" +
                        "We wish you the best in your job search!\n\n" +
                        "Best regards,\n" +
                        "CareerMate Team",
                        jobPosting.getTitle(),
                        jobPosting.getRecruiter().getCompanyName()
                );
                priority = 2; // MEDIUM priority
                break;

            case REVIEWING:
                title = "Application Under Review";
                subject = String.format("Your Application for '%s' is Being Reviewed", jobPosting.getTitle());
                message = String.format(
                        "Your application is now under review.\n\n" +
                        "Job Position: %s\n" +
                        "Company: %s\n\n" +
                        "The recruiter is currently reviewing your application and CV. " +
                        "You will be notified once a decision has been made.\n\n" +
                        "Thank you for your patience!\n\n" +
                        "Best regards,\n" +
                        "CareerMate Team",
                        jobPosting.getTitle(),
                        jobPosting.getRecruiter().getCompanyName()
                );
                priority = 3; // LOW priority
                break;

            default:
                title = "Application Status Updated";
                subject = String.format("Your Application Status for '%s' Has Been Updated", jobPosting.getTitle());
                message = String.format(
                        "Your application status has been updated.\n\n" +
                        "Job Position: %s\n" +
                        "Company: %s\n" +
                        "New Status: %s\n\n" +
                        "You can check your application details in your candidate dashboard.\n\n" +
                        "Best regards,\n" +
                        "CareerMate Team",
                        jobPosting.getTitle(),
                        jobPosting.getRecruiter().getCompanyName(),
                        newStatus.name()
                );
                priority = 3; // LOW priority
                break;
        }

        NotificationEvent event = NotificationEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(NotificationEvent.EventType.APPLICATION_STATUS_CHANGED.name())
                .recipientId(String.valueOf(candidateId))
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
        log.info("✅ Sent application status change notification to candidate {} for status: {}", candidateId, newStatus);
    }
}
