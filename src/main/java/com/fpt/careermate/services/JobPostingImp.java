package com.fpt.careermate.services;

import com.fpt.careermate.constant.StatusJobPosting;
import com.fpt.careermate.domain.*;
import com.fpt.careermate.repository.JdSkillRepo;
import com.fpt.careermate.repository.JobDescriptionRepo;
import com.fpt.careermate.repository.JobPostingRepo;
import com.fpt.careermate.repository.RecruiterRepo;
import com.fpt.careermate.services.dto.request.JdSkillRequest;
import com.fpt.careermate.services.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.dto.response.JobPostingForRecruiterResponse;
import com.fpt.careermate.services.dto.response.JobPostingSkillResponse;
import com.fpt.careermate.services.impl.JobPostingService;
import com.fpt.careermate.services.mapper.JobPostingMapper;
import com.fpt.careermate.util.JobPostingValidator;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobPostingImp implements JobPostingService {

    JobPostingRepo jobPostingRepo;
    RecruiterRepo recruiterRepo;
    JdSkillRepo jdSkillRepo;
    JobDescriptionRepo jobDescriptionRepo;
    JobPostingMapper jobPostingMapper;
    AuthenticationImp authenticationImp;
    JobPostingValidator jobPostingValidator;

    // Recruiter create job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void createJobPosting(JobPostingCreationRequest request) {
        // Validate request
        jobPostingValidator.checkDuplicateJobPostingTitle(request.getTitle());
        jobPostingValidator.validateExpirationDate(request.getExpirationDate());

        Recruiter recruiter = getMyRecruiter();

        JobPosting jobPosting = jobPostingMapper.toJobPosting(request);
        jobPosting.setCreateAt(LocalDate.now());
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

        jobPostingRepo.save(jobPosting);
    }

    // Get all job postings of the current recruiter with all status
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public List<JobPostingForRecruiterResponse> getAllJobPostingForRecruiter() {
        Recruiter recruiter = getMyRecruiter();

        return jobPostingMapper
                .toJobPostingForRecruiterResponseList(jobPostingRepo.findAllByRecruiter_Id(recruiter.getId()));
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public JobPostingForRecruiterResponse getJobPostingDetailForRecruiter(int id) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);
        JobPostingForRecruiterResponse jpResponse = jobPostingMapper.toJobPostingDetailForRecruiterResponse(jobPosting);

        Set<JobPostingSkillResponse> jobPostingSkillResponses = jobPostingMapper
                .toJobPostingSkillResponseSet(jobPosting.getJobDescriptions());
        jobPostingSkillResponses.forEach(jobPostingSkillResponse -> {
            jobPosting.getJobDescriptions().forEach(jd -> {
                jobPostingSkillResponse.setName(jd.getJdSkill().getName());
            });
        });

        jpResponse.setSkills(jobPostingSkillResponses);

        return jpResponse;
    }

    // Recruiter update job posting
    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public void updateJobPosting(int id, JobPostingCreationRequest request) {
        JobPosting jobPosting = findJobPostingEntityForRecruiterById(id);

        // Check job posting status
        if (Set.of(
                StatusJobPosting.DELETED,
                StatusJobPosting.ACTIVE,
                StatusJobPosting.PAUSED,
                StatusJobPosting.EXPIRED).contains(jobPosting.getStatus()))
            throw new AppException(ErrorCode.CANNOT_MODIFY_JOB_POSTING);

        // Validate request
        jobPostingValidator.checkDuplicateJobPostingTitleAndNotCurrentRecruiter(request.getTitle(),
                jobPosting.getRecruiter().getId());
        jobPostingValidator.validateExpirationDate(request.getExpirationDate());
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

        jobPostingRepo.save(jobPosting);
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
        Optional<Recruiter> currentRecruiter = recruiterRepo.findByAccount_Id(currentAccount.getId());
        return currentRecruiter.get();
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

        log.info("Updated {} job postings to EXPIRED status.", expiredJobs.size());
    }

    // ========== ADMIN METHODS ==========

    // Admin get all job postings with pagination and filtering
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public org.springframework.data.domain.Page<com.fpt.careermate.services.dto.response.JobPostingForAdminResponse> getAllJobPostingsForAdmin(
            int page, int size, String status, String sortBy, String sortDirection) {

        log.info("Admin fetching job postings - Page: {}, Size: {}, Status: {}", page, size, status);

        org.springframework.data.domain.Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? org.springframework.data.domain.Sort.by(sortBy).ascending()
                : org.springframework.data.domain.Sort.by(sortBy).descending();

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                sort);

        org.springframework.data.domain.Page<JobPosting> jobPostings;

        if (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("ALL")) {
            jobPostings = jobPostingRepo.findAll(pageable);
        } else {
            jobPostings = jobPostingRepo.findAllByStatusOrderByCreateAtDesc(status.toUpperCase(), pageable);
        }

        return jobPostings.map(this::convertToAdminResponse);
    }

    // Admin get specific job posting detail
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public com.fpt.careermate.services.dto.response.JobPostingForAdminResponse getJobPostingDetailForAdmin(int id) {
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
            com.fpt.careermate.services.dto.request.JobPostingApprovalRequest request) {
        log.info("Admin processing approval/rejection for job posting ID: {}", id);

        // Get job posting
        JobPosting jobPosting = jobPostingRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // Only PENDING job postings can be approved/rejected
        if (!jobPosting.getStatus().equals(StatusJobPosting.PENDING)) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        // Get current admin account
        Account admin = authenticationImp.findByEmail();

        String newStatus = request.getStatus().toUpperCase();

        if (newStatus.equals("APPROVED")) {
            // Approve: Set status to ACTIVE
            jobPosting.setStatus(StatusJobPosting.ACTIVE);
            jobPosting.setApprovedBy(admin);
            jobPosting.setRejectionReason(null); // Clear any previous rejection reason
            log.info("Job posting ID: {} APPROVED by admin: {}", id, admin.getEmail());

        } else if (newStatus.equals("REJECTED")) {
            // Reject: Require rejection reason
            if (request.getRejectionReason() == null || request.getRejectionReason().trim().isEmpty()) {
                throw new AppException(ErrorCode.REJECTION_REASON_REQUIRED);
            }
            jobPosting.setStatus(StatusJobPosting.REJECTED);
            jobPosting.setRejectionReason(request.getRejectionReason());
            jobPosting.setApprovedBy(admin);
            log.info("Job posting ID: {} REJECTED by admin: {}", id, admin.getEmail());

        } else {
            throw new AppException(ErrorCode.INVALID_APPROVAL_STATUS);
        }

        jobPostingRepo.save(jobPosting);
    }

    // Admin get all pending job postings (for quick review)
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<com.fpt.careermate.services.dto.response.JobPostingForAdminResponse> getPendingJobPostings() {
        log.info("Admin fetching all pending job postings");

        List<JobPosting> pendingJobs = jobPostingRepo.findAllByStatus(StatusJobPosting.PENDING);

        return pendingJobs.stream()
                .map(this::convertToAdminResponse)
                .toList();
    }

    // Helper method to convert JobPosting to Admin Response
    private com.fpt.careermate.services.dto.response.JobPostingForAdminResponse convertToAdminResponse(
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
        com.fpt.careermate.services.dto.response.RecruiterBasicInfoResponse recruiterInfo = com.fpt.careermate.services.dto.response.RecruiterBasicInfoResponse
                .builder()
                .id(recruiter.getId())
                .companyName(recruiter.getCompanyName())
                .email(recruiter.getAccount().getEmail())
                .phoneNumber("Contact via email") // Phone not available in current schema
                .build();

        return com.fpt.careermate.services.dto.response.JobPostingForAdminResponse.builder()
                .id(jobPosting.getId())
                .title(jobPosting.getTitle())
                .description(jobPosting.getDescription())
                .address(jobPosting.getAddress())
                .status(jobPosting.getStatus())
                .expirationDate(jobPosting.getExpirationDate())
                .createAt(jobPosting.getCreateAt())
                .rejectionReason(jobPosting.getRejectionReason())
                .recruiter(recruiterInfo)
                .approvedByEmail(jobPosting.getApprovedBy() != null ? jobPosting.getApprovedBy().getEmail() : null)
                .skills(skills)
                .build();
    }

}
