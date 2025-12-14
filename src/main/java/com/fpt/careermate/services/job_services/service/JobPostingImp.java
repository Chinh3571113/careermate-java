package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.common.constant.StatusJobPosting;
import com.fpt.careermate.common.constant.StatusRecruiter;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.job_services.domain.SavedJob;
import com.fpt.careermate.services.job_services.repository.JdSkillRepo;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.job_services.repository.JobDescriptionRepo;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
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
    public void updateJobPosting(int id, JobPostingCreationRequest request) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Disallow modifications for DELETED or PAUSED postings
        if (Set.of(
                StatusJobPosting.DELETED,
                StatusJobPosting.PAUSED).contains(jobPosting.getStatus())) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_JOB_POSTING);
        }

        // If posting is ACTIVE or EXPIRED, only allow changing the expiration date.
        // This prevents changing other fields while candidates may apply (ACTIVE) or
        // allows reactivating an expired posting by updating its date (EXPIRED).
        if (jobPosting.getStatus().equals(StatusJobPosting.ACTIVE) ||
                jobPosting.getStatus().equals(StatusJobPosting.EXPIRED)) {
            // Validate new expiration date (must be in the future)
            jobPostingValidator.validateExpirationDate(request.getExpirationDate());

            // Ensure new expiration date is not before the creation date
            if (request.getExpirationDate().isBefore(jobPosting.getCreateAt())) {
                throw new AppException(ErrorCode.INVALID_EXPIRATION_DATE);
            }

            jobPosting.setExpirationDate(request.getExpirationDate());

            // If expired posting date is being updated, change status to ACTIVE
            if (jobPosting.getStatus().equals(StatusJobPosting.EXPIRED)) {
                jobPosting.setStatus(StatusJobPosting.ACTIVE);
            }

            JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);

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

        JobPosting updatedJobPosting = jobPostingRepo.save(jobPosting);

        // Invalidate cache for this job posting
        recruiterJobPostingRedisService.deleteFromCache(id);
        // Clear list cache for this recruiter
        recruiterJobPostingRedisService.clearRecruiterListCache(jobPosting.getRecruiter().getId());

        // Sync with Weaviate: delete old entry and add updated job if it's active
        if (updatedJobPosting.getStatus().equals(StatusJobPosting.ACTIVE)) {
            weaviateImp.deleteJobPosting(id);
            weaviateImp.addJobPostingToWeaviate(updatedJobPosting);
        }
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

    // Recruiter pause job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void pauseJobPosting(int id) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Check job posting status
        if (!jobPosting.getStatus().equals(StatusJobPosting.ACTIVE))
            throw new AppException(ErrorCode.CANNOT_PAUSE_JOB_POSTING);

        jobPosting.setStatus(StatusJobPosting.PAUSED);
        jobPostingRepo.save(jobPosting);

        // Invalidate cache for this job posting
        recruiterJobPostingRedisService.deleteFromCache(id);
        // Clear list cache for this recruiter
        recruiterJobPostingRedisService.clearRecruiterListCache(jobPosting.getRecruiter().getId());
        // Clear candidate list cache (job is no longer publicly visible)
        candidateJobPostingRedisService.clearAllCandidateListCache();

        // Delete from Weaviate
        weaviateImp.deleteJobPosting(id);
    }

    // Recruiter extend job posting expiration date
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

        // Send email notification
        try {
            MailBody mailBody = MailBody.builder()
                    .to(jobPosting.getRecruiter().getAccount().getEmail())
                    .subject("Your Job Posting Has Been Approved")
                    .text(emailMessage)
                    .build();

            emailService.sendSimpleEmail(mailBody);
            log.info("✅ Job posting approval email sent to recruiter for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send job posting approval email for job ID: {}",
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

        // Send email notification
        try {
            MailBody mailBody = MailBody.builder()
                    .to(jobPosting.getRecruiter().getAccount().getEmail())
                    .subject("Your Job Posting Requires Updates")
                    .text(emailMessage)
                    .build();

            emailService.sendSimpleEmail(mailBody);
            log.info("✅ Job posting rejection email sent to recruiter for job ID: {}", jobPosting.getId());
        } catch (Exception e) {
            log.error("❌ Failed to send job posting rejection email for job ID: {}",
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
