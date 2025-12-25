package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.common.constant.StatusJobPosting;
import com.fpt.careermate.common.constant.StatusRecruiter;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.job_services.domain.SavedJob;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.repository.JdSkillRepo;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.job_services.repository.JobDescriptionRepo;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.job_services.repository.JobPostingAuditRepo;
import com.fpt.careermate.services.job_services.repository.SavedJobRepo;
import com.fpt.careermate.services.job_services.service.dto.response.*;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.admin_services.domain.Admin;
import com.fpt.careermate.services.admin_services.repository.AdminRepo;
import com.fpt.careermate.services.job_services.service.dto.request.JdSkillRequest;
import com.fpt.careermate.services.job_services.service.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.job_services.service.dto.request.JobPostingApprovalRequest;
import com.fpt.careermate.services.job_services.service.impl.JobPostingService;
import com.fpt.careermate.services.job_services.domain.JdSkill;
import com.fpt.careermate.services.job_services.domain.JobDescription;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.domain.JobPostingAudit;
import com.fpt.careermate.services.job_services.service.mapper.JobPostingMapper;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterBasicInfoResponse;
import com.fpt.careermate.common.util.JobPostingValidator;
import com.fpt.careermate.common.util.MailBody;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.email_services.service.impl.EmailService;
import com.fpt.careermate.services.email_services.service.AsyncEmailService;
import com.fpt.careermate.services.weaviate_services.service.AsyncWeaviateService;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.kafka.producer.NotificationProducer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobPostingImp implements JobPostingService {

    JobPostingRepo jobPostingRepo;
    JobApplyRepo jobApplyRepo;
    RecruiterRepo recruiterRepo;
    AdminRepo adminRepo;
    JdSkillRepo jdSkillRepo;
    JobDescriptionRepo jobDescriptionRepo;
    JobPostingMapper jobPostingMapper;
    AuthenticationImp authenticationImp;
    JobPostingValidator jobPostingValidator;
    WeaviateImp weaviateImp;
    EmailService emailService;
    NotificationProducer notificationProducer;
    SavedJobRepo savedJobRepo;
    AsyncEmailService asyncEmailService;
    AsyncWeaviateService asyncWeaviateService;
    RecruiterJobPostingRedisService recruiterJobPostingRedisService;
    AdminJobPostingRedisService adminJobPostingRedisService;
    CandidateJobPostingRedisService candidateJobPostingRedisService;
    JobPostingAuditRepo jobPostingAuditRepo;

    // Recruiter create job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void createJobPosting(JobPostingCreationRequest request) {
        // Get current recruiter first (needed for duplicate check)
        Recruiter recruiter = getMyRecruiter();

        // Validate request - check duplicate title within same recruiter only
        jobPostingValidator.checkDuplicateJobPostingTitle(request.getTitle(), recruiter.getId());
        jobPostingValidator.validateExpirationDate(request.getExpirationDate());

        JobPosting jobPosting = jobPostingMapper.toJobPosting(request);
        jobPosting.setCreateAt(LocalDate.now());
        jobPosting.setWorkModel(request.getWorkModel().getDisplayName());
        jobPosting.setRecruiter(recruiter);
        jobPosting.setStatus(StatusJobPosting.PENDING);

        jobPostingRepo.save(jobPosting);

        Set<JobDescription> jobDescriptions = new HashSet<>();
        for (JdSkillRequest skillReq : request.getJdSkills()) {
            // a) find jdSkill by id
            Optional<JdSkill> exstingJdSkill = jdSkillRepo.findById(skillReq.getId());
            JdSkill jdSkill = exstingJdSkill.get();

            // b) Create JobDescription link
            JobDescription jd = new JobDescription();
            jd.setJobPosting(jobPosting);
            jd.setJdSkill(jdSkill);
            jd.setMustToHave(skillReq.isMustToHave());

            jobDescriptions.add(jd);
        }

        // Save all JobDescription
        jobDescriptionRepo.saveAll(jobDescriptions);

        jobPosting.setJobDescriptions(jobDescriptions);

        // Save to postgres
        JobPosting savedPostgres = jobPostingRepo.save(jobPosting);

        // Clear list cache for this recruiter (new job posting added)
        recruiterJobPostingRedisService.clearRecruiterListCache(recruiter.getId());

        // Clear pending jobs cache (new pending job added)
        adminJobPostingRedisService.clearPendingJobsCache();

        // Clear admin list cache (new job posting affects admin views)
        adminJobPostingRedisService.clearAllAdminListCache();

        // Send notification to admin about new job posting pending approval
        sendJobPostingPendingNotification(savedPostgres);
    }

    // Get all job postings of the current recruiter with all status
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public PageJobPostingForRecruiterResponse getAllJobPostingForRecruiter(
            int page, int size, String keyword) {
        return gellAllJobPostings(page, size, keyword, 0,0);
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public JobPostingForRecruiterResponse getJobPostingDetailForRecruiter(int id) {
        // Try to get from cache first
        JobPostingForRecruiterResponse cachedResponse = recruiterJobPostingRedisService.getFromCache(id);
        if (cachedResponse != null) {
            log.debug("Returning cached job posting detail for ID: {}", id);
            return cachedResponse;
        }

        // If not in cache, fetch from database
        log.debug("Fetching job posting detail from database for ID: {}", id);
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);
        JobPostingForRecruiterResponse jpResponse = jobPostingMapper.toJobPostingDetailForRecruiterResponse(jobPosting);

        // Get skills
        Set<JobPostingSkillResponse> skills = new HashSet<>();
        jobPosting.getJobDescriptions().forEach(jd -> {
            skills.add(
                    JobPostingSkillResponse.builder()
                            .id(jd.getJdSkill().getId())
                            .name(jd.getJdSkill().getName())
                            .mustToHave(jd.isMustToHave())
                            .build());
        });

        jpResponse.setSkills(skills);

        // Save to cache for future requests
        recruiterJobPostingRedisService.saveToCache(id, jpResponse);
        log.debug("Saved job posting detail to cache for ID: {}", id);

        return jpResponse;
    }

    // Recruiter update job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    @Transactional
    public void updateJobPosting(int id, JobPostingCreationRequest request) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Disallow modifications for DELETED or PAUSED postings
        if (Set.of(
                StatusJobPosting.DELETED,
                StatusJobPosting.PAUSED).contains(jobPosting.getStatus())) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_JOB_POSTING);
        }

        // Store old values for audit and notification
        LocalDate oldExpirationDate = jobPosting.getExpirationDate();
        String oldTitle = jobPosting.getTitle();
        String oldDescription = jobPosting.getDescription();
        String oldAddress = jobPosting.getAddress();

        // Count current applicants for ACTIVE jobs
        int applicantCount = 0;
        if (jobPosting.getStatus().equals(StatusJobPosting.ACTIVE)) {
            applicantCount = jobApplyRepo.countByJobPostingId(jobPosting.getId());
        }

        // If posting is ACTIVE or EXPIRED, only allow changing the expiration date.
        // Rule: ACTIVE jobs with applicants CANNOT modify content (title, description, skills)
        if (jobPosting.getStatus().equals(StatusJobPosting.ACTIVE) ||
                jobPosting.getStatus().equals(StatusJobPosting.EXPIRED)) {
            
            // Validate new expiration date (must be in the future)
            jobPostingValidator.validateExpirationDate(request.getExpirationDate());

            // Ensure new expiration date is not before the creation date
            if (request.getExpirationDate().isBefore(jobPosting.getCreateAt())) {
                throw new AppException(ErrorCode.INVALID_EXPIRATION_DATE);
            }

            // Rule: Limit expiration date changes for ACTIVE jobs with applicants
            if (jobPosting.getStatus().equals(StatusJobPosting.ACTIVE) && applicantCount > 0) {
                long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(oldExpirationDate, request.getExpirationDate());
                
                // Cannot shorten deadline by more than 7 days
                if (daysDifference < -7) {
                    throw new AppException(ErrorCode.EXPIRATION_DATE_TOO_SHORT);
                }
                
                // Cannot extend deadline by more than 60 days
                if (daysDifference > 60) {
                    throw new AppException(ErrorCode.EXPIRATION_DATE_TOO_LONG);
                }
            }

            boolean expirationDateChanged = !oldExpirationDate.equals(request.getExpirationDate());
            boolean wasExpired = jobPosting.getStatus().equals(StatusJobPosting.EXPIRED);
            
            jobPosting.setExpirationDate(request.getExpirationDate());

            // If expired posting date is being updated, change status to ACTIVE
            if (wasExpired) {
                jobPosting.setStatus(StatusJobPosting.ACTIVE);
            }

            JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);

            // Create audit log for expiration date change
            if (expirationDateChanged) {
                createAuditLog(jobPosting, wasExpired ? "EXTEND_EXPIRED_JOB" : "UPDATE_EXPIRATION_DATE", "expirationDate", 
                    oldExpirationDate.toString(), request.getExpirationDate().toString(), applicantCount);
                
                // Notify recruiter if they extended an expired job
                if (wasExpired) {
                    sendJobPostingExtendedNotification(updatedJobPosting, request.getExpirationDate());
                }
                
                // Notify candidates of deadline change
                if (applicantCount > 0) {
                    notifyApplicantsOfDeadlineChange(jobPosting, oldExpirationDate, request.getExpirationDate());
                }
                notifySavedJobCandidatesOfDeadlineChange(jobPosting, oldExpirationDate, request.getExpirationDate());
            }

            // Invalidate cache for this job posting
            recruiterJobPostingRedisService.deleteFromCache(id);
            // Clear list cache for this recruiter
            recruiterJobPostingRedisService.clearRecruiterListCache(jobPosting.getRecruiter().getId());

            // Clear candidate list cache if job is ACTIVE (publicly visible)
            if (updatedJobPosting.getStatus().equals(StatusJobPosting.ACTIVE)) {
                candidateJobPostingRedisService.clearAllCandidateListCache();
            }

            // Sync with Weaviate: delete old entry and add updated job
            if (updatedJobPosting.getStatus().equals(StatusJobPosting.ACTIVE)) {
                weaviateImp.deleteJobPosting(id);
                weaviateImp.addJobPostingToWeaviate(updatedJobPosting);
            }
            return;
        }

        // For PENDING or REJECTED postings allow full update
        // Track if this was a rejected job being resubmitted
        boolean wasRejected = jobPosting.getStatus().equals(StatusJobPosting.REJECTED);
        
        // Validate request - check duplicate title within same recruiter, excluding current job
        jobPostingValidator.checkDuplicateJobPostingTitleForUpdate(request.getTitle(),
                jobPosting.getRecruiter().getId(), jobPosting.getId());
        jobPostingValidator.validateExpirationDate(request.getExpirationDate());

        // Ensure expiration date is not before the creation date
        if (request.getExpirationDate().isBefore(jobPosting.getCreateAt())) {
            throw new AppException(ErrorCode.INVALID_EXPIRATION_DATE);
        }

        // Validate JdSkill exist
        jobPostingValidator.validateJdSkill(request.getJdSkills());

        // Remove all old job descriptions
        List<JobDescription> jobDescriptions = jobDescriptionRepo.findByJobPosting_Id(jobPosting.getId());
        jobDescriptionRepo.deleteAll(jobDescriptions);

        // Update job posting
        jobPosting.setTitle(request.getTitle());
        jobPosting.setDescription(request.getDescription());
        jobPosting.setAddress(request.getAddress());
        jobPosting.setExpirationDate(request.getExpirationDate());

        // Add new job descriptions
        Set<JobDescription> newJobDescriptions = new HashSet<>();
        request.getJdSkills().forEach(jd -> {
            Optional<JdSkill> existingJdSkill = jdSkillRepo.findById(jd.getId());
            JdSkill jdSkill = existingJdSkill.get();

            JobDescription jobDescription = new JobDescription();
            jobDescription.setJobPosting(jobPosting);
            jobDescription.setJdSkill(jdSkill);
            jobDescription.setMustToHave(jd.isMustToHave());

            newJobDescriptions.add(jobDescription);
        });
        jobPosting.setJobDescriptions(newJobDescriptions);

        // If this was a REJECTED job, set status back to PENDING for admin re-review
        if (wasRejected) {
            jobPosting.setStatus(StatusJobPosting.PENDING);
            jobPosting.setRejectionReason(null);  // Clear the old rejection reason
            log.info("Rejected job posting {} resubmitted for review. Status: REJECTED → PENDING", id);
        }

        JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);

        // Create audit log for full update
        String actionType = wasRejected ? "RESUBMIT" : "FULL_UPDATE";
        createAuditLog(jobPosting, actionType, "multiple", 
            String.format("title=%s, description=%s", oldTitle, oldDescription),
            String.format("title=%s, description=%s", request.getTitle(), request.getDescription()), 0);

        // Invalidate cache for this job posting
        recruiterJobPostingRedisService.deleteFromCache(id);
        // Clear list cache for this recruiter
        recruiterJobPostingRedisService.clearRecruiterListCache(jobPosting.getRecruiter().getId());

        // If resubmitted, clear pending jobs cache and notify admin
        if (wasRejected) {
            adminJobPostingRedisService.clearPendingJobsCache();
            adminJobPostingRedisService.clearAllAdminListCache();
            sendJobPostingPendingNotification(updatedJobPosting);
        }

        // Sync with Weaviate: delete old entry and add updated job if it's active
        if (updatedJobPosting.getStatus().equals(StatusJobPosting.ACTIVE)) {
            weaviateImp.deleteJobPosting(id);
            weaviateImp.addJobPostingToWeaviate(updatedJobPosting);
        }
    }
    
    /**
     * Create an audit log entry for job posting changes
     */
    private void createAuditLog(JobPosting jobPosting, String actionType, String fieldChanged,
                                 String oldValue, String newValue, int applicantCount) {
        try {
            JobPostingAudit audit = JobPostingAudit.builder()
                    .jobPostingId(jobPosting.getId())
                    .jobTitle(jobPosting.getTitle())
                    .recruiterId(jobPosting.getRecruiter().getId())
                    .actionType(actionType)
                    .fieldChanged(fieldChanged)
                    .oldValue(oldValue != null ? oldValue.substring(0, Math.min(oldValue.length(), 1000)) : null)
                    .newValue(newValue != null ? newValue.substring(0, Math.min(newValue.length(), 1000)) : null)
                    .changedAt(LocalDateTime.now())
                    .applicantsCountAtChange(applicantCount)
                    .build();
            jobPostingAuditRepo.save(audit);
        } catch (Exception e) {
            log.warn("Failed to create audit log for job posting {}: {}", jobPosting.getId(), e.getMessage());
        }
    }
    
    /**
     * Notify applicants (SUBMITTED/REVIEWING status) about deadline changes
     */
    private void notifyApplicantsOfDeadlineChange(JobPosting jobPosting, LocalDate oldDate, LocalDate newDate) {
        try {
            List<StatusJobApply> eligibleStatuses = List.of(StatusJobApply.SUBMITTED, StatusJobApply.REVIEWING);
            List<JobApply> applications = jobApplyRepo.findByJobPostingIdAndStatusIn(jobPosting.getId(), eligibleStatuses);
            
            for (JobApply application : applications) {
                NotificationEvent notification = NotificationEvent.builder()
                        .eventType(NotificationEvent.EventType.APPLICATION_STATUS_CHANGED.name())
                        .recipientId(String.valueOf(application.getCandidate().getCandidateId()))
                        .title("Job Deadline Changed")
                        .subject("Job Deadline Changed")
                        .message(String.format("The deadline for '%s' at %s has been changed from %s to %s",
                                jobPosting.getTitle(),
                                jobPosting.getRecruiter().getCompanyName(),
                                oldDate.toString(),
                                newDate.toString()))
                        .metadata(Map.of(
                                "jobTitle", jobPosting.getTitle(),
                                "companyName", jobPosting.getRecruiter().getCompanyName(),
                                "oldDeadline", oldDate.toString(),
                                "newDeadline", newDate.toString(),
                                "jobId", String.valueOf(jobPosting.getId())
                        ))
                        .priority(2)
                        .build();
                notificationProducer.sendRecruiterNotification(notification);
            }
            log.info("Sent deadline change notifications to {} applicants for job {}", applications.size(), jobPosting.getId());
        } catch (Exception e) {
            log.warn("Failed to notify applicants of deadline change for job {}: {}", jobPosting.getId(), e.getMessage());
        }
    }
    
    /**
     * Notify candidates who saved the job about deadline changes
     */
    private void notifySavedJobCandidatesOfDeadlineChange(JobPosting jobPosting, LocalDate oldDate, LocalDate newDate) {
        try {
            List<SavedJob> savedJobs = savedJobRepo.findByJobPostingId(jobPosting.getId());
            
            for (SavedJob savedJob : savedJobs) {
                NotificationEvent notification = NotificationEvent.builder()
                        .eventType(NotificationEvent.EventType.APPLICATION_STATUS_CHANGED.name())
                        .recipientId(String.valueOf(savedJob.getCandidate().getCandidateId()))
                        .title("Saved Job Deadline Changed")
                        .subject("Saved Job Deadline Changed")
                        .message(String.format("The deadline for saved job '%s' at %s has been changed from %s to %s",
                                jobPosting.getTitle(),
                                jobPosting.getRecruiter().getCompanyName(),
                                oldDate.toString(),
                                newDate.toString()))
                        .metadata(Map.of(
                                "jobTitle", jobPosting.getTitle(),
                                "companyName", jobPosting.getRecruiter().getCompanyName(),
                                "oldDeadline", oldDate.toString(),
                                "newDeadline", newDate.toString(),
                                "jobId", String.valueOf(jobPosting.getId())
                        ))
                        .priority(3)
                        .build();
                notificationProducer.sendRecruiterNotification(notification);
            }
            log.info("Sent deadline change notifications to {} candidates who saved job {}", savedJobs.size(), jobPosting.getId());
        } catch (Exception e) {
            log.warn("Failed to notify saved job candidates of deadline change for job {}: {}", jobPosting.getId(), e.getMessage());
        }
    }

    /**
     * Pause an ACTIVE job posting
     */
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    @Transactional
    public void pauseJobPosting(int id) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Validate that job is ACTIVE
        if (!jobPosting.getStatus().equals(StatusJobPosting.ACTIVE)) {
            throw new AppException(ErrorCode.CANNOT_PAUSE_NON_ACTIVE);
        }

        // Update status to PAUSED
        jobPosting.setStatus(StatusJobPosting.PAUSED);
        JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);

        // Create audit log
        createAuditLog(jobPosting, "PAUSE", "status", 
            StatusJobPosting.ACTIVE, StatusJobPosting.PAUSED, 
            jobApplyRepo.countByJobPostingId(jobPosting.getId()));

        // Invalidate cache
        recruiterJobPostingRedisService.deleteFromCache(id);
        recruiterJobPostingRedisService.clearRecruiterListCache(jobPosting.getRecruiter().getId());
        candidateJobPostingRedisService.clearAllCandidateListCache();

        // Remove from Weaviate (paused jobs should not appear in search)
        weaviateImp.deleteJobPosting(id);

        log.info("Job posting {} paused by recruiter {}", id, jobPosting.getRecruiter().getId());
    }

    /**
     * Resume a PAUSED job posting back to ACTIVE
     */
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    @Transactional
    public void resumeJobPosting(int id) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Validate that job is PAUSED
        if (!jobPosting.getStatus().equals(StatusJobPosting.PAUSED)) {
            throw new AppException(ErrorCode.CANNOT_RESUME_NON_PAUSED);
        }

        // Check if expiration date is still valid
        if (jobPosting.getExpirationDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_EXPIRATION_DATE);
        }

        // Update status to ACTIVE
        jobPosting.setStatus(StatusJobPosting.ACTIVE);
        JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);

        // Create audit log
        createAuditLog(jobPosting, "RESUME", "status", 
            StatusJobPosting.PAUSED, StatusJobPosting.ACTIVE, 
            jobApplyRepo.countByJobPostingId(jobPosting.getId()));

        // Invalidate cache
        recruiterJobPostingRedisService.deleteFromCache(id);
        recruiterJobPostingRedisService.clearRecruiterListCache(jobPosting.getRecruiter().getId());
        candidateJobPostingRedisService.clearAllCandidateListCache();

        // Add back to Weaviate
        weaviateImp.addJobPostingToWeaviate(updatedJobPosting);

        log.info("Job posting {} resumed by recruiter {}", id, jobPosting.getRecruiter().getId());
    }

    /**
     * Close an ACTIVE job posting (position filled)
     */
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    @Transactional
    public void closeJobPosting(int id) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Validate that job is ACTIVE
        if (!jobPosting.getStatus().equals(StatusJobPosting.ACTIVE)) {
            throw new AppException(ErrorCode.CANNOT_CLOSE_NON_ACTIVE);
        }

        // Update status to CLOSED
        jobPosting.setStatus(StatusJobPosting.CLOSED);
        JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);

        // Create audit log
        createAuditLog(jobPosting, "CLOSE", "status", 
            StatusJobPosting.ACTIVE, StatusJobPosting.CLOSED, 
            jobApplyRepo.countByJobPostingId(jobPosting.getId()));

        // Invalidate cache
        recruiterJobPostingRedisService.deleteFromCache(id);
        recruiterJobPostingRedisService.clearRecruiterListCache(jobPosting.getRecruiter().getId());
        candidateJobPostingRedisService.clearAllCandidateListCache();

        // Remove from Weaviate (closed jobs should not appear in search)
        weaviateImp.deleteJobPosting(id);

        log.info("Job posting {} closed by recruiter {}", id, jobPosting.getRecruiter().getId());
    }

    // Recruiter delete job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void deleteJobPosting(int id) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Check job posting status
        if (Set.of(
                StatusJobPosting.DELETED,
                StatusJobPosting.ACTIVE,
                StatusJobPosting.PAUSED).contains(jobPosting.getStatus()))
            throw new AppException(ErrorCode.CANNOT_DELETE_JOB_POSTING);

        jobPosting.setStatus(StatusJobPosting.DELETED);
        jobPostingRepo.save(jobPosting);

        // Invalidate cache for this job posting
        recruiterJobPostingRedisService.deleteFromCache(id);
        // Clear list cache for this recruiter
        recruiterJobPostingRedisService.clearRecruiterListCache(jobPosting.getRecruiter().getId());
        // Note: ACTIVE jobs cannot be deleted (validation above), but clear candidate cache for safety
        candidateJobPostingRedisService.clearAllCandidateListCache();

        // Delete from Weaviate
        weaviateImp.deleteJobPosting(id);
    }

    // Recruiter extend job posting expiration date
    @Transactional
    @PreAuthorize("hasRole('RECRUITER')")
    public void extendJobPosting(int id, String expirationDateStr) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Only allow extending ACTIVE or EXPIRED job postings
        if (!jobPosting.getStatus().equals(StatusJobPosting.ACTIVE) &&
            !jobPosting.getStatus().equals(StatusJobPosting.EXPIRED)) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_JOB_POSTING);
        }

        // Parse and validate the new expiration date
        LocalDate newExpirationDate;
        try {
            newExpirationDate = LocalDate.parse(expirationDateStr);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_DATE_FORMAT);
        }

        // Validate new expiration date (must be in the future)
        jobPostingValidator.validateExpirationDate(newExpirationDate);

        // Ensure new expiration date is not before the creation date
        if (newExpirationDate.isBefore(jobPosting.getCreateAt())) {
            throw new AppException(ErrorCode.INVALID_EXPIRATION_DATE);
        }

        // Update expiration date
        jobPosting.setExpirationDate(newExpirationDate);

        // If expired posting is being extended, change status back to ACTIVE
        if (jobPosting.getStatus().equals(StatusJobPosting.EXPIRED)) {
            jobPosting.setStatus(StatusJobPosting.ACTIVE);
        }

        JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);

        // Invalidate cache for this job posting
        recruiterJobPostingRedisService.deleteFromCache(id);
        // Clear list cache for this recruiter
        recruiterJobPostingRedisService.clearRecruiterListCache(updatedJobPosting.getRecruiter().getId());

        // Sync with Weaviate: delete old entry and add updated job
        if (updatedJobPosting.getStatus().equals(StatusJobPosting.ACTIVE)) {
            weaviateImp.deleteJobPosting(id);
            weaviateImp.addJobPostingToWeaviate(updatedJobPosting);
        }

        log.info("Job posting ID {} extended to {}", id, newExpirationDate);
    }

    // Get job posting stats for recruiter dashboard
    @PreAuthorize("hasRole('RECRUITER')")
    public JobPostingStatsResponse getJobPostingStats() {
        Recruiter recruiter = getMyRecruiter();
        int recruiterId = recruiter.getId();

        // Count job postings by status
        long totalJobPostings = jobPostingRepo.findAllByRecruiterId(recruiterId, PageRequest.of(0, 1)).getTotalElements();
        long pendingJobPostings = jobPostingRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobPosting.PENDING);
        long activeJobPostings = jobPostingRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobPosting.ACTIVE);
        long rejectedJobPostings = jobPostingRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobPosting.REJECTED);
        long pausedJobPostings = jobPostingRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobPosting.PAUSED);
        long expiredJobPostings = jobPostingRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobPosting.EXPIRED);
        long deletedJobPostings = jobPostingRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobPosting.DELETED);

        // Count applications by status
        long totalApplications = jobApplyRepo.countByRecruiterId(recruiterId);
        long submittedApplications = jobApplyRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobApply.SUBMITTED);
        long reviewingApplications = jobApplyRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobApply.REVIEWING);
        long approvedApplications = jobApplyRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobApply.APPROVED);
        long rejectedApplications = jobApplyRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobApply.REJECTED);
        long interviewScheduledApplications = jobApplyRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobApply.INTERVIEW_SCHEDULED);
        long hiredApplications = jobApplyRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobApply.WORKING) +
                                 jobApplyRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobApply.ACCEPTED);
        long withdrawnApplications = jobApplyRepo.countByRecruiterIdAndStatus(recruiterId, StatusJobApply.WITHDRAWN);

        return JobPostingStatsResponse.builder()
                .totalJobPostings(totalJobPostings)
                .pendingJobPostings(pendingJobPostings)
                .activeJobPostings(activeJobPostings)
                .rejectedJobPostings(rejectedJobPostings)
                .pausedJobPostings(pausedJobPostings)
                .expiredJobPostings(expiredJobPostings)
                .deletedJobPostings(deletedJobPostings)
                .totalApplications(totalApplications)
                .submittedApplications(submittedApplications)
                .reviewingApplications(reviewingApplications)
                .approvedApplications(approvedApplications)
                .rejectedApplications(rejectedApplications)
                .interviewScheduledApplications(interviewScheduledApplications)
                .hiredApplications(hiredApplications)
                .withdrawnApplications(withdrawnApplications)
                .build();
    }

    private JobPosting findJobPostingEntityForRecruiterById(int id) {
        Recruiter recruiter = getMyRecruiter();

        // Check job posting exist
        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // Check job posting belong to current recruiter
        if (jobPosting.getRecruiter().getId() != recruiter.getId()) {
            throw new AppException(ErrorCode.JOB_POSTING_FORBIDDEN);
        }

        return jobPosting;
    }

    // Get current recruiter
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

    // Scheduler to update job posting status to EXPIRED if expiration date is
    // before today and status is not EXPIRED or DELETED
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void updateExpiredJobPostings() {
        LocalDate today = LocalDate.now();

        // Get all job postings that need to be expired
        List<JobPosting> expiredJobs = jobPostingRepo
                .findByExpirationDateBeforeAndStatusNotIn(today,
                        List.of(StatusJobPosting.EXPIRED, StatusJobPosting.DELETED));

        if (expiredJobs.isEmpty()) {
            log.info("No job postings to expire today.");
            return;
        }

        // Update status to EXPIRED
        expiredJobs.forEach(jp -> jp.setStatus(StatusJobPosting.EXPIRED));

        // Save all updated job postings in batch
        jobPostingRepo.saveAll(expiredJobs);

        // Send expiration notifications to recruiters
        expiredJobs.forEach(jp -> {
            try {
                sendJobPostingExpiredNotification(jp);
            } catch (Exception e) {
                log.error("❌ Failed to send expiration notification for job ID: {}", jp.getId(), e);
            }
        });

        // Invalidate cache for all expired job postings
        expiredJobs.forEach(jp -> {
            recruiterJobPostingRedisService.deleteFromCache(jp.getId());
            recruiterJobPostingRedisService.clearRecruiterListCache(jp.getRecruiter().getId());
        });

        // Clear candidate list cache once (jobs removed from public view)
        candidateJobPostingRedisService.clearAllCandidateListCache();

        // Clear admin list cache (status changed for multiple jobs)
        adminJobPostingRedisService.clearAllAdminListCache();

        // Delete expired jobs from Weaviate
        expiredJobs.forEach(jp -> {
            try {
                weaviateImp.deleteJobPosting(jp.getId());
                log.info("Deleted expired job posting from Weaviate - ID: {}", jp.getId());
            } catch (Exception e) {
                log.error("Failed to delete job posting from Weaviate - ID: {}", jp.getId(), e);
            }
        });

        log.info("Updated {} job postings to EXPIRED status and removed from Weaviate.", expiredJobs.size());
    }

    // ========== ADMIN METHODS ==========

    // Admin get all job postings with pagination and filtering
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public Page<JobPostingForAdminResponse> getAllJobPostingsForAdmin(
            int page, int size, String status, String sortBy, String sortDirection) {

        long startTime = System.currentTimeMillis();

        // Try to get from cache first
        Page<?> cachedResponse =
                adminJobPostingRedisService.getAdminListFromCache(
                        page, size, status, sortBy, sortDirection
                );

        if (cachedResponse != null) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("getAllJobPostingsForAdmin - Redis - Response time: {}ms", responseTime);

            @SuppressWarnings("unchecked")
            Page<JobPostingForAdminResponse> typedResponse =
                    (Page<JobPostingForAdminResponse>) cachedResponse;

            return typedResponse;
        }

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<JobPosting> jobPostings;

        if (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("ALL")) {
            jobPostings = jobPostingRepo.findAll(pageable);
        } else {
            jobPostings = jobPostingRepo.findAllByStatusOrderByCreateAtDesc(status.toUpperCase(), pageable);
        }

        Page<JobPostingForAdminResponse> result = jobPostings.map(this::convertToAdminResponse);

        // Save to cache for future requests
        adminJobPostingRedisService.saveAdminListToCache(
                page, size, status, sortBy, sortDirection, result);

        long responseTime = System.currentTimeMillis() - startTime;
        log.info("getAllJobPostingsForAdmin - (DB query + cache save) - Response time: {}ms ",responseTime);

        return result;
    }

    // Admin get specific job posting detail
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public JobPostingForAdminResponse getJobPostingDetailForAdmin(int id) {
        log.info("Admin fetching job posting detail for ID: {}", id);

        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        return convertToAdminResponse(jobPosting);
    }

    // Admin approve or reject job posting
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Override
    public void approveOrRejectJobPosting(int id,
            JobPostingApprovalRequest request) {
        log.info("Admin processing approval/rejection for job posting ID: {}", id);

        // Get job posting
        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // Only PENDING job postings can be approved/rejected
        if (!jobPosting.getStatus().equals(StatusJobPosting.PENDING)) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // Get current admin account and admin entity
        Account adminAccount = authenticationImp.findByEmail();
        Admin admin = adminRepo.findByAccount_Id(adminAccount.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String newStatus = request.getStatus().toUpperCase();

        if (newStatus.equals("APPROVED")) {
            // Approve: Set status to ACTIVE
            jobPosting.setStatus(StatusJobPosting.ACTIVE);
            jobPosting.setApprovedBy(admin);
            jobPosting.setRejectionReason(null); // Clear any previous rejection reason
            log.info("Job posting ID: {} APPROVED by admin: {}", id, admin.getAccount().getEmail());

            // Send approval notification to recruiter (ASYNC for better performance)
            sendJobPostingApprovedNotificationAsync(jobPosting);
        } else if (newStatus.equals("REJECTED")) {
            // Reject: Require rejection reason
            if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
                throw new AppException(ErrorCode.REJECTION_REASON_REQUIRED);
            }
            jobPosting.setStatus(StatusJobPosting.REJECTED);
            jobPosting.setRejectionReason(request.getRejectionReason());
            jobPosting.setApprovedBy(admin);
            log.info("Job posting ID: {} REJECTED by admin: {}", id, admin.getAccount().getEmail());

            // Send rejection notification to recruiter (ASYNC for better performance)
            sendJobPostingRejectedNotificationAsync(jobPosting);

        } else {
            throw new AppException(ErrorCode.INVALID_APPROVAL_STATUS);
        }

        JobPosting savedPostgres = jobPostingRepo.save(jobPosting);

        // Invalidate cache for this job posting
        recruiterJobPostingRedisService.deleteFromCache(id);

        // Clear list cache for this recruiter
        recruiterJobPostingRedisService.clearRecruiterListCache(savedPostgres.getRecruiter().getId());

        // Clear candidate list cache if job is approved (becomes publicly visible)
        if(savedPostgres.getStatus().equals(StatusJobPosting.ACTIVE)) {
            candidateJobPostingRedisService.clearAllCandidateListCache();
        }

        // Clear admin list cache (status changed, affects all admin views)
        adminJobPostingRedisService.clearAllAdminListCache();

        // Clear pending jobs cache (job is no longer pending)
        adminJobPostingRedisService.clearPendingJobsCache();

        // Add to Weaviate ASYNCHRONOUSLY if approved - prevents blocking response
        if(savedPostgres.getStatus().equals(StatusJobPosting.ACTIVE)) {
            asyncWeaviateService.addJobPostingToWeaviateAsync(savedPostgres);
        }
    }

    // Admin get all pending job postings (for quick review)
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<JobPostingForAdminResponse> getPendingJobPostings() {
        long startTime = System.currentTimeMillis();

        // Try to get from cache first
        List<?> cachedResponse = adminJobPostingRedisService.getPendingJobsFromCache();

        if (cachedResponse != null) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("getPendingJobPostings - Redis - Response time: {}ms", responseTime);

            @SuppressWarnings("unchecked")
            List<JobPostingForAdminResponse> typedResponse =
                    (List<JobPostingForAdminResponse>) cachedResponse;

            return typedResponse;
        }

        List<JobPosting> pendingJobs = jobPostingRepo.findAllByStatus(StatusJobPosting.PENDING);

        List<JobPostingForAdminResponse> result = pendingJobs.stream()
                .map(this::convertToAdminResponse)
                .toList();

        // Save to cache for future requests
        adminJobPostingRedisService.savePendingJobsToCache(result);

        long responseTime = System.currentTimeMillis() - startTime;
        log.info("getPendingJobPostings - (DB query + cache save) - Response time: {}ms", responseTime);

        return result;
    }

    // Helper method to convert JobPosting to Admin Response
    private JobPostingForAdminResponse convertToAdminResponse(
            JobPosting jobPosting) {
        // Get skills
        List<JobDescription> descriptions = jobDescriptionRepo.findByJobPosting_Id(jobPosting.getId());
        Set<JobPostingSkillResponse> skills = new HashSet<>();

        for (JobDescription desc : descriptions) {
            skills.add(JobPostingSkillResponse.builder()
                    .id(desc.getJdSkill().getId())
                    .name(desc.getJdSkill().getName())
                    .mustToHave(desc.isMustToHave())
                    .build());
        }

        // Build recruiter info
        Recruiter recruiter = jobPosting.getRecruiter();
        RecruiterBasicInfoResponse recruiterInfo = RecruiterBasicInfoResponse
                .builder()
                .id(recruiter.getId())
                .companyName(recruiter.getCompanyName())
                .email(recruiter.getAccount().getEmail())
                .companyEmail(recruiter.getCompanyEmail())
                .phoneNumber(recruiter.getPhoneNumber())
                .build();

        return JobPostingForAdminResponse.builder()
                .id(jobPosting.getId())
                .title(jobPosting.getTitle())
                .description(jobPosting.getDescription())
                .address(jobPosting.getAddress())
                .status(jobPosting.getStatus())
                .expirationDate(jobPosting.getExpirationDate())
                .createAt(jobPosting.getCreateAt())
                .rejectionReason(jobPosting.getRejectionReason())
                .recruiter(recruiterInfo)
                .approvedByEmail(
                        jobPosting.getApprovedBy() != null ? jobPosting.getApprovedBy().getAccount().getEmail() : null)
                .skills(skills)
                .build();
    }

    // ======================== CANDIDATE METHODS ========================

    // Public API: Get all approved and active job postings with search
    @Override
    public PageResponse<JobPostingForCandidateResponse> getAllApprovedJobPostings(
            String keyword, Pageable pageable) {
        long startTime = System.currentTimeMillis();

        // Try to get from cache first
        PageResponse<?> cachedResponse =
                candidateJobPostingRedisService.getCandidateListFromCache(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        keyword);

        if (cachedResponse != null) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("getAllApprovedJobPostings - Redis - Response time: {}ms", responseTime);

            @SuppressWarnings("unchecked")
            PageResponse<JobPostingForCandidateResponse> typedResponse =
                    (PageResponse<JobPostingForCandidateResponse>) cachedResponse;

            return typedResponse;
        }

        Page<JobPosting> jobPostingPage;
        LocalDate currentDate = LocalDate.now();

        if (keyword != null && !keyword.trim().isEmpty()) {
            // Search with keyword
            jobPostingPage = jobPostingRepo.searchApprovedJobPostings(
                    StatusJobPosting.ACTIVE,
                    currentDate,
                    keyword.trim(),
                    pageable);
        } else {
            // Get all approved job postings that haven't expired
            jobPostingPage = jobPostingRepo.findAllByStatusAndExpirationDateAfterOrderByCreateAtDesc(
                    StatusJobPosting.ACTIVE,
                    currentDate,
                    pageable);
        }

        List<JobPostingForCandidateResponse> responses = jobPostingPage.getContent()
                .stream()
                .map(this::convertToCandidateResponse)
                .toList();

        PageResponse<JobPostingForCandidateResponse> pageResponse = new PageResponse<>(
                responses,
                jobPostingPage.getNumber(),
                jobPostingPage.getSize(),
                jobPostingPage.getTotalElements(),
                jobPostingPage.getTotalPages());

        // Save to cache for future requests
        candidateJobPostingRedisService.saveCandidateListToCache(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                keyword,
                pageResponse);

        long responseTime = System.currentTimeMillis() - startTime;
        log.info("getAllApprovedJobPostings - (DB query + cache save) - Response time: {}ms ", responseTime);

        return pageResponse;
    }

    // Public API: Get job posting detail by ID (only approved ones)
    @Override
    public JobPostingForCandidateResponse getJobPostingDetailForCandidate(int id) {
        log.info("Public API: Fetching approved job posting detail for ID: {}", id);

        JobPosting jobPosting = jobPostingRepo.findByIdAndStatus(id, StatusJobPosting.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // Check if job posting has expired
        if (jobPosting.getExpirationDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.JOB_POSTING_EXPIRED);
        }

        return convertToCandidateResponse(jobPosting);
    }

    // Helper method to convert JobPosting entity to candidate response DTO
    private JobPostingForCandidateResponse convertToCandidateResponse(JobPosting jobPosting) {
        // Get skills
        Set<JobPostingSkillResponse> skills = new HashSet<>();
        if (jobPosting.getJobDescriptions() != null) {
            jobPosting.getJobDescriptions().forEach(jd -> {
                skills.add(JobPostingSkillResponse.builder()
                        .id(jd.getJdSkill().getId())
                        .name(jd.getJdSkill().getName())
                        .mustToHave(jd.isMustToHave())
                        .build());
            });
        }

        // Build recruiter company info
        Recruiter recruiter = jobPosting.getRecruiter();
        JobPostingForCandidateResponse.RecruiterCompanyInfo recruiterInfo = JobPostingForCandidateResponse.RecruiterCompanyInfo
                .builder()
                .recruiterId(recruiter.getId())
                .companyName(recruiter.getCompanyName())
                .website(recruiter.getWebsite())
                .logoUrl(recruiter.getLogoUrl())
                .about(recruiter.getAbout())
                .build();

        return JobPostingForCandidateResponse.builder()
                .id(jobPosting.getId())
                .title(jobPosting.getTitle())
                .description(jobPosting.getDescription())
                .address(jobPosting.getAddress())
                .expirationDate(jobPosting.getExpirationDate())
                .postTime(jobPosting.getCreateAt())
                .yearsOfExperience(jobPosting.getYearsOfExperience())
                .workModel(jobPosting.getWorkModel())
                .salaryRange(jobPosting.getSalaryRange())
                .jobPackage(jobPosting.getJobPackage())
                .reason(jobPosting.getReason())
                .skills(skills)
                .recruiterInfo(recruiterInfo)
                .build();
    }

    // ======================== KAFKA NOTIFICATION METHODS ========================

    /**
     * Send notification to admin when a new job posting is created (PENDING status)
     */
    private void sendJobPostingPendingNotification(JobPosting jobPosting) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("jobPostingId", jobPosting.getId());
            metadata.put("jobTitle", jobPosting.getTitle());
            metadata.put("companyName", jobPosting.getRecruiter().getCompanyName());
            metadata.put("recruiterId", jobPosting.getRecruiter().getId());
            metadata.put("createdAt", jobPosting.getCreateAt().toString());

            NotificationEvent event = NotificationEvent.builder()
                    .eventType(NotificationEvent.EventType.SYSTEM_NOTIFICATION.name())
                    .recipientId("ADMIN")
                    .recipientEmail("admin@careermate.com")
                    .title("New Job Posting Pending Approval")
                    .subject("Job Posting Requires Review")
                    .message(String.format(
                            "A new job posting '%s' from company '%s' requires your review and approval.",
                            jobPosting.getTitle(),
                            jobPosting.getRecruiter().getCompanyName()))
                    .category("JOB_POSTING_APPROVAL")
                    .metadata(metadata)
                    .priority(2) // MEDIUM priority
                    .build();

            notificationProducer.sendAdminNotification(event);
            log.info("✅ Sent pending job posting notification to admin for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send pending job posting notification to admin for job ID: {}",
                    jobPosting.getId(), e);
        }
    }

    /**
     * Send notification to recruiter when their job posting is approved
     */
    private void sendJobPostingApprovedNotification(JobPosting jobPosting) {
        String emailMessage = String.format(
                "Great news! Your job posting '%s' has been approved and is now live on CareerMate.\n\n" +
                        "Job Details:\n" +
                        "- Title: %s\n" +
                        "- Location: %s\n" +
                        "- Expiration Date: %s\n\n" +
                        "Candidates can now view and apply to your job posting.\n\n" +
                        "Best regards,\n" +
                        "CareerMate Team",
                jobPosting.getTitle(),
                jobPosting.getTitle(),
                jobPosting.getAddress(),
                jobPosting.getExpirationDate());

        try {
            // Send Kafka notification for in-app notification
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("jobPostingId", jobPosting.getId());
            metadata.put("jobTitle", jobPosting.getTitle());
            metadata.put("approvedBy", jobPosting.getApprovedBy().getAccount().getEmail());
            metadata.put("status", jobPosting.getStatus());

            NotificationEvent event = NotificationEvent.builder()
                    .eventType(NotificationEvent.EventType.JOB_POSTING_APPROVED.name())
                    .recipientId(jobPosting.getRecruiter().getAccount().getEmail()) // Use email for SSE
                    .recipientEmail(jobPosting.getRecruiter().getAccount().getEmail())
                    .title("Job Posting Approved")
                    .subject("Your Job Posting Has Been Approved")
                    .message(emailMessage)
                    .category("JOB_POSTING_STATUS")
                    .metadata(metadata)
                    .priority(2) // MEDIUM priority
                    .build();

            notificationProducer.sendRecruiterNotification(event);
            log.info("✅ Sent approval notification to recruiter for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send approval notification to recruiter for job ID: {}",
                    jobPosting.getId(), e);
        }

        // Send email notification (async - non-blocking)
        try {
            MailBody mailBody = MailBody.builder()
                    .to(jobPosting.getRecruiter().getAccount().getEmail())
                    .subject("Your Job Posting Has Been Approved")
                    .text(emailMessage)
                    .build();

            emailService.sendSimpleEmailAsync(mailBody);
            log.info("✅ Job posting approval email queued for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to queue job posting approval email for job ID: {}",
                    jobPosting.getId(), e);
        }
    }

    /**
     * Send notification to recruiter when their job posting expires
     */
    private void sendJobPostingExpiredNotification(JobPosting jobPosting) {
        String emailMessage = String.format(
                "Your job posting '%s' has expired and is no longer visible to candidates.\n\n" +
                        "Job Details:\n" +
                        "- Title: %s\n" +
                        "- Location: %s\n" +
                        "- Expired on: %s\n\n" +
                        "You can extend the expiration date to reactivate this job posting from your dashboard.\n\n" +
                        "Best regards,\n" +
                        "CareerMate Team",
                jobPosting.getTitle(),
                jobPosting.getTitle(),
                jobPosting.getAddress(),
                jobPosting.getExpirationDate());

        try {
            // Send Kafka notification for in-app notification
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("jobPostingId", jobPosting.getId());
            metadata.put("jobTitle", jobPosting.getTitle());
            metadata.put("expirationDate", jobPosting.getExpirationDate().toString());
            metadata.put("status", jobPosting.getStatus());

            NotificationEvent event = NotificationEvent.builder()
                    .eventType(NotificationEvent.EventType.JOB_POSTING_EXPIRED.name())
                    .recipientId(jobPosting.getRecruiter().getAccount().getEmail())
                    .recipientEmail(jobPosting.getRecruiter().getAccount().getEmail())
                    .title("Job Posting Expired")
                    .subject("Your Job Posting Has Expired")
                    .message(emailMessage)
                    .category("JOB_POSTING_STATUS")
                    .metadata(metadata)
                    .priority(2) // MEDIUM priority
                    .build();

            notificationProducer.sendRecruiterNotification(event);
            log.info("✅ Sent expiration notification to recruiter for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send expiration notification to recruiter for job ID: {}",
                    jobPosting.getId(), e);
        }

        // Send email notification (async - non-blocking)
        try {
            MailBody mailBody = MailBody.builder()
                    .to(jobPosting.getRecruiter().getAccount().getEmail())
                    .subject("Your Job Posting Has Expired")
                    .text(emailMessage)
                    .build();

            emailService.sendSimpleEmailAsync(mailBody);
            log.info("✅ Job posting expiration email queued for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to queue job posting expiration email for job ID: {}",
                    jobPosting.getId(), e);
        }
    }

    /**
     * Send notification to recruiter when they extend an expired job posting
     */
    private void sendJobPostingExtendedNotification(JobPosting jobPosting, LocalDate newExpirationDate) {
        String emailMessage = String.format(
                "Your expired job posting '%s' has been successfully extended and reactivated!\n\n" +
                        "Job Details:\n" +
                        "- Title: %s\n" +
                        "- Location: %s\n" +
                        "- New Expiration Date: %s\n\n" +
                        "Your job posting is now ACTIVE and visible to candidates again.\n\n" +
                        "Best regards,\n" +
                        "CareerMate Team",
                jobPosting.getTitle(),
                jobPosting.getTitle(),
                jobPosting.getAddress(),
                newExpirationDate);

        try {
            // Send Kafka notification for in-app notification
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("jobPostingId", jobPosting.getId());
            metadata.put("jobTitle", jobPosting.getTitle());
            metadata.put("newExpirationDate", newExpirationDate.toString());
            metadata.put("status", jobPosting.getStatus());

            NotificationEvent event = NotificationEvent.builder()
                    .eventType(NotificationEvent.EventType.JOB_POSTING_EXTENDED.name())
                    .recipientId(jobPosting.getRecruiter().getAccount().getEmail())
                    .recipientEmail(jobPosting.getRecruiter().getAccount().getEmail())
                    .title("Job Posting Extended & Reactivated")
                    .subject("Your Job Posting Has Been Extended")
                    .message(emailMessage)
                    .category("JOB_POSTING_STATUS")
                    .metadata(metadata)
                    .priority(2) // MEDIUM priority
                    .build();

            notificationProducer.sendRecruiterNotification(event);
            log.info("✅ Sent extension notification to recruiter for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send extension notification to recruiter for job ID: {}",
                    jobPosting.getId(), e);
        }

        // Send email notification (async - non-blocking)
        try {
            MailBody mailBody = MailBody.builder()
                    .to(jobPosting.getRecruiter().getAccount().getEmail())
                    .subject("Your Job Posting Has Been Extended")
                    .text(emailMessage)
                    .build();

            emailService.sendSimpleEmailAsync(mailBody);
            log.info("✅ Job posting extension email queued for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to queue job posting extension email for job ID: {}",
                    jobPosting.getId(), e);
        }
    }

    /**
     * Send notification to recruiter when their job posting is rejected
     */
    private void sendJobPostingRejectedNotification(JobPosting jobPosting) {
        String emailMessage = String.format(
                "Your job posting '%s' was not approved and requires updates.\n\n" +
                        "Rejection Reason:\n%s\n\n" +
                        "Please review the feedback above and resubmit your job posting after making the necessary changes.\n\n"
                        +
                        "If you have any questions, please contact our support team.\n\n" +
                        "Best regards,\n" +
                        "CareerMate Team",
                jobPosting.getTitle(),
                jobPosting.getRejectionReason() != null ? jobPosting.getRejectionReason()
                        : "No specific reason provided");

        try {
            // Send Kafka notification for in-app notification
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("jobPostingId", jobPosting.getId());
            metadata.put("jobTitle", jobPosting.getTitle());
            metadata.put("rejectionReason", jobPosting.getRejectionReason());
            metadata.put("rejectedBy", jobPosting.getApprovedBy().getAccount().getEmail());
            metadata.put("status", jobPosting.getStatus());

            NotificationEvent event = NotificationEvent.builder()
                    .eventType(NotificationEvent.EventType.JOB_POSTING_REJECTED.name())
                    .recipientId(jobPosting.getRecruiter().getAccount().getEmail()) // Use email for SSE
                    .recipientEmail(jobPosting.getRecruiter().getAccount().getEmail())
                    .title("Job Posting Rejected")
                    .subject("Your Job Posting Requires Updates")
                    .message(emailMessage)
                    .category("JOB_POSTING_STATUS")
                    .metadata(metadata)
                    .priority(2) // MEDIUM priority
                    .build();

            notificationProducer.sendRecruiterNotification(event);
            log.info("✅ Sent rejection notification to recruiter for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send rejection notification to recruiter for job ID: {}",
                    jobPosting.getId(), e);
        }

        // Send email notification (async - non-blocking)
        try {
            MailBody mailBody = MailBody.builder()
                    .to(jobPosting.getRecruiter().getAccount().getEmail())
                    .subject("Your Job Posting Requires Updates")
                    .text(emailMessage)
                    .build();

            emailService.sendSimpleEmailAsync(mailBody);
            log.info("✅ Job posting rejection email queued for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to queue job posting rejection email for job ID: {}",
                    jobPosting.getId(), e);
        }
    }

    private PageJobPostingForRecruiterResponse gellAllJobPostings(
            int page, int size, String keyword, int recruiterId, int candidateId) {

        long startTime = System.currentTimeMillis();

        if (recruiterId == 0) {
            Recruiter recruiter = getMyRecruiter();
            recruiterId = recruiter.getId();
        }

        // Try to get from cache first (only for non-candidate requests)
        if (candidateId == 0) {
            PageJobPostingForRecruiterResponse cachedResponse =
                    recruiterJobPostingRedisService.getListFromCache(recruiterId, page, size, keyword);
            if (cachedResponse != null) {
                long responseTime = System.currentTimeMillis() - startTime;
                log.info("getAllJobPostingForRecruiter - Redis - Response time: {}ms", responseTime);
                return cachedResponse;
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").ascending());

        Page<JobPosting> pageJobPosting;
        if (keyword == null || keyword.isEmpty()) {
            // Lấy tất cả
            pageJobPosting = jobPostingRepo.findAllByRecruiterId(recruiterId, pageable);
        } else {
            // Lọc theo keyword (ví dụ search trong title)
            pageJobPosting = jobPostingRepo
                    .findByRecruiterIdAndTitleContainingIgnoreCase(recruiterId, keyword, pageable);
        }

        List<JobPostingForRecruiterResponse> jobPostingForRecruiterResponses = pageJobPosting
                .stream()
                .map(jobPostingMapper::toJobPostingDetailForRecruiterResponse)
                .collect(Collectors.toList());

        // Thêm skills
        jobPostingForRecruiterResponses.forEach(jobPostingForRecruiterResponse -> {
            Set<JobPostingSkillResponse> skills = new HashSet<>();
            pageJobPosting.getContent().forEach(jobPostingContent -> {
                jobPostingContent.getJobDescriptions().forEach(jobDescription -> {
                    if (jobPostingForRecruiterResponse.getId() == jobPostingContent.getId()) {
                        skills.add(
                                JobPostingSkillResponse.builder()
                                        .id(jobDescription.getJdSkill().getId())
                                        .name(jobDescription.getJdSkill().getName())
                                        .mustToHave(jobDescription.isMustToHave())
                                        .build());
                    }
                });
            });
            jobPostingForRecruiterResponse.setSkills(skills);
        });

        // Nếu candidateId != 0 thì đánh dấu đã lưu hay chưa
        if(candidateId != 0) {
            List<SavedJob> savedJobs = savedJobRepo.findAllByCandidate_CandidateId(candidateId);
            savedJobs.forEach(savedJob -> {
                jobPostingForRecruiterResponses.forEach(jobPostingForRecruiterResponse -> {
                    if(savedJob.getJobPosting().getId() == jobPostingForRecruiterResponse.getId()) {
                        jobPostingForRecruiterResponse.setSaved(true);
                    }
                });
            });
        }
        else {
            jobPostingForRecruiterResponses.forEach(jobPostingForRecruiterResponse -> {
                jobPostingForRecruiterResponse.setSaved(false);
            });
        }

        PageJobPostingForRecruiterResponse pageResponse = jobPostingMapper
                .toPageJobPostingForRecruiterResponse(pageJobPosting);
        pageResponse.setContent(jobPostingForRecruiterResponses);

        // Save to cache for future requests (only for non-candidate requests)
        if (candidateId == 0) {
            recruiterJobPostingRedisService.saveListToCache(recruiterId, page, size, keyword, pageResponse);

            long responseTime = System.currentTimeMillis() - startTime;
            log.info("getAllJobPostingForRecruiter - (DB query + cache save) - Response time: {}ms", responseTime);
        } else {
            long responseTime = System.currentTimeMillis() - startTime;
            log.info("for candidate view (no cache) - Response time: {}ms", responseTime);
        }

        return pageResponse;
    }

    @Override
    public PageJobPostingForRecruiterResponse getAllJobPostingsPublic(
            int page, int size, String keyword, int recruiterId, int candidateId) {
        return gellAllJobPostings(
                page, size, keyword, recruiterId, candidateId
        );
    }

    @Override
    public JobPostingForCandidateResponse.RecruiterCompanyInfo getCompanyDetail(int recruiterId) {
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        return jobPostingMapper.toRecruiterCompanyInfo(recruiter);
    }

    @Override
    public PageRecruiterResponse getCompanies(int page, int size, String companyAddress) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("companyName").ascending());
        Page<Recruiter> pageRecruiter = null;
        // Logic nếu CompanyAddress có giá trị thì lọc theo địa chỉ, nếu không thì lấy tất cả
        if (companyAddress == null || companyAddress.isEmpty()) {
            pageRecruiter = recruiterRepo.findAllByVerificationStatus(
                    StatusRecruiter.APPROVED, pageable
            );
        } else {
            pageRecruiter = recruiterRepo.findAllByVerificationStatusAndCompanyAddressContainingIgnoreCase(
                    StatusRecruiter.APPROVED, companyAddress, pageable
            );
        }

        List<Recruiter> recruiters = pageRecruiter.getContent();
        List<RecruiterResponse> recruiterResponses = jobPostingMapper.toRecruiterResponseList(recruiters);
        // Thêm số lượng job postings cho mỗi recruiter
        recruiterResponses.forEach(recruiterResponse -> {
            long jobCount = jobPostingRepo.countByRecruiterIdAndStatus(recruiterResponse.getId(), StatusJobPosting.ACTIVE);
            recruiterResponse.setJobCount(jobCount);
        });

        // Map to PageRecruiterResponse
        PageRecruiterResponse pageRecruiterResponse = jobPostingMapper.toPageRecruiterResponse(pageRecruiter);

        // Map to RecruiterResponse DTOs and set content
        pageRecruiterResponse.setContent(recruiterResponses);

        return pageRecruiterResponse;
    }

    @Override
    public List<String> getAddresses(String keyword, int limit) {
        // Nếu keyword null hoặc rỗng thì tìm tất cả
        String searchKeyword = (keyword == null || keyword.isEmpty()) ? "" : keyword;

        // Giới hạn số lượng kết quả
        Pageable pageable = PageRequest.of(0, limit);

        List<String> addresses = recruiterRepo.findDistinctCompanyAddressByKeyword(
                StatusRecruiter.APPROVED,
                searchKeyword,
                pageable
        );

        return addresses;
    }

    /**
     * Send approval notification asynchronously (optimized for performance)
     * Reduces response time by 2-10 seconds by sending email in background
     */
    private void sendJobPostingApprovedNotificationAsync(JobPosting jobPosting) {
        String emailMessage = String.format(
                "Great news! Your job posting '%s' has been approved and is now live on CareerMate.\n\n" +
                        "Job Details:\n" +
                        "- Title: %s\n" +
                        "- Location: %s\n" +
                        "- Expiration Date: %s\n\n" +
                        "Candidates can now view and apply to your job posting.\n\n" +
                        "Best regards,\n" +
                        "CareerMate Team",
                jobPosting.getTitle(),
                jobPosting.getTitle(),
                jobPosting.getAddress(),
                jobPosting.getExpirationDate());

        // Send Kafka notification (keep synchronous as it's fast)
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("jobPostingId", jobPosting.getId());
            metadata.put("jobTitle", jobPosting.getTitle());
            metadata.put("approvedBy", jobPosting.getApprovedBy().getAccount().getEmail());
            metadata.put("status", jobPosting.getStatus());

            NotificationEvent event = NotificationEvent.builder()
                    .eventType(NotificationEvent.EventType.JOB_POSTING_APPROVED.name())
                    .recipientId(jobPosting.getRecruiter().getAccount().getEmail())
                    .recipientEmail(jobPosting.getRecruiter().getAccount().getEmail())
                    .title("Job Posting Approved")
                    .subject("Your Job Posting Has Been Approved")
                    .message(emailMessage)
                    .category("JOB_POSTING_STATUS")
                    .metadata(metadata)
                    .priority(2)
                    .build();

            notificationProducer.sendRecruiterNotification(event);
            log.info("✅ Sent approval notification to Kafka for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send Kafka notification for job ID: {}", jobPosting.getId(), e);
        }

        // Send email notification ASYNCHRONOUSLY to avoid blocking
        MailBody mailBody = MailBody.builder()
                .to(jobPosting.getRecruiter().getAccount().getEmail())
                .subject("Your Job Posting Has Been Approved")
                .text(emailMessage)
                .build();

        asyncEmailService.sendEmailAsync(mailBody);
        log.info("📧 Queued approval email for async sending to job ID: {}", jobPosting.getId());
    }

    /**
     * Send rejection notification asynchronously (optimized for performance)
     * Reduces response time by 2-10 seconds by sending email in background
     */
    private void sendJobPostingRejectedNotificationAsync(JobPosting jobPosting) {
        String emailMessage = String.format(
                "Your job posting '%s' was not approved and requires updates.\n\n" +
                        "Rejection Reason:\n%s\n\n" +
                        "Please review the feedback above and resubmit your job posting after making the necessary changes.\n\n" +
                        "If you have any questions, please contact our support team.\n\n" +
                        "Best regards,\n" +
                        "CareerMate Team",
                jobPosting.getTitle(),
                jobPosting.getRejectionReason() != null ? jobPosting.getRejectionReason()
                        : "No specific reason provided");

        // Send Kafka notification (keep synchronous as it's fast)
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("jobPostingId", jobPosting.getId());
            metadata.put("jobTitle", jobPosting.getTitle());
            metadata.put("rejectionReason", jobPosting.getRejectionReason());
            metadata.put("rejectedBy", jobPosting.getApprovedBy().getAccount().getEmail());
            metadata.put("status", jobPosting.getStatus());

            NotificationEvent event = NotificationEvent.builder()
                    .eventType(NotificationEvent.EventType.JOB_POSTING_REJECTED.name())
                    .recipientId(jobPosting.getRecruiter().getAccount().getEmail())
                    .recipientEmail(jobPosting.getRecruiter().getAccount().getEmail())
                    .title("Job Posting Rejected")
                    .subject("Your Job Posting Requires Updates")
                    .message(emailMessage)
                    .category("JOB_POSTING_STATUS")
                    .metadata(metadata)
                    .priority(2)
                    .build();

            notificationProducer.sendRecruiterNotification(event);
            log.info("✅ Sent rejection notification to Kafka for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send Kafka notification for job ID: {}", jobPosting.getId(), e);
        }

        // Send email notification ASYNCHRONOUSLY to avoid blocking
        MailBody mailBody = MailBody.builder()
                .to(jobPosting.getRecruiter().getAccount().getEmail())
                .subject("Your Job Posting Requires Updates")
                .text(emailMessage)
                .build();

        asyncEmailService.sendEmailAsync(mailBody);
        log.info("📧 Queued rejection email for async sending to job ID: {}", jobPosting.getId());
    }
}
