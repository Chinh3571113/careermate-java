package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.constant.StatusJobPosting;
import com.fpt.careermate.common.constant.StatusRecruiter;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.domain.SavedJob;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.job_services.repository.SavedJobRepo;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForCandidateResponse;
import com.fpt.careermate.services.job_services.service.dto.response.PageJobPostingForCandidateResponse;
import com.fpt.careermate.services.job_services.service.dto.response.PageSavedJobPostingResponse;
import com.fpt.careermate.services.job_services.service.dto.response.SavedJobPostingResponse;
import com.fpt.careermate.services.job_services.service.impl.SavedJobService;
import com.fpt.careermate.services.job_services.service.mapper.JobPostingMapper;
import com.fpt.careermate.services.job_services.service.mapper.SavedJobMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SavedJobImp implements SavedJobService {

    SavedJobRepo savedJobRepo;
    JobPostingRepo jobPostingRepo;
    CoachUtil coachUtil;
    SavedJobMapper savedJobMapper;
    JobPostingMapper jobPostingMapper;

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public boolean toggleSaveJob(int jobId){
        int candidateId = coachUtil.getCurrentCandidate().getCandidateId();
        Optional<SavedJob> savedJobOpt =
                savedJobRepo.findByCandidate_candidateIdAndJobPosting_Id(candidateId, jobId);
        // Check if job posting exists
        Optional<JobPosting> jobPosting = jobPostingRepo.findById(jobId);
        if (jobPosting.isEmpty()) {
            throw new AppException(ErrorCode.JOB_POSTING_NOT_FOUND);
        }

        if (savedJobOpt.isPresent()) {
            savedJobRepo.delete(savedJobOpt.get());
            return false; // Job unsaved
        } else {
            SavedJob savedJob = new SavedJob();
            savedJob.setCandidate(coachUtil.getCurrentCandidate());
            savedJob.setJobPosting(jobPosting.get());
            savedJob.setSavedAt(LocalDateTime.now());
            savedJobRepo.save(savedJob);
            return true; // Job saved
        }
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public PageSavedJobPostingResponse getSavedJobs(int page, int size) {
        // Implementation for retrieving saved jobs with pagination
        int candidateId = coachUtil.getCurrentCandidate().getCandidateId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("savedAt").descending());
        Page<SavedJob> savedJobPage = savedJobRepo.findByCandidate_CandidateId(candidateId, pageable);

        // Convert to DTO response
        List<SavedJobPostingResponse> savedJobResponses = new ArrayList<>();
        savedJobPage.getContent().forEach(savedJob -> {
            SavedJobPostingResponse savedJobPostingResponse = savedJobMapper.toSavedJobPostingResponse(savedJob);

            // Lấy tất cả skill name của jobDescriptions và ghép thành 1 string
            String skills = savedJob.getJobPosting().getJobDescriptions().stream()
                    .map(jd -> jd.getJdSkill().getName())
                    .distinct() // loại bỏ trùng lặp
                    .sorted()   // sắp xếp alphabet
                    .reduce((s1, s2) -> s1 + ", " + s2) // ghép thành 1 chuỗi
                    .orElse(""); // nếu không có skill nào

            savedJobPostingResponse.setSkills(skills);

            savedJobResponses.add(savedJobPostingResponse);
        });

        PageSavedJobPostingResponse pageSavedJobPostingResponse = savedJobMapper.toPageSavedJobPostingResponse(savedJobPage);
        pageSavedJobPostingResponse.setContent(savedJobResponses);

        return pageSavedJobPostingResponse;
    }

    // Get all job postings for candidate (Job Tab)
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public PageJobPostingForCandidateResponse getJobsForCandidate(int page, int size) {
        int candidateId = coachUtil.getCurrentCandidate().getCandidateId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
        Page<JobPosting> jobPostingPage = jobPostingRepo.findAllByStatusAndRecruiter_VerificationStatus(
                StatusJobPosting.ACTIVE, StatusRecruiter.APPROVED, pageable
        );

        // Convert to DTO response using mapper
        List<JobPostingForCandidateResponse> jobPostingResponses = new ArrayList<>();
        jobPostingPage.getContent().forEach(jobPosting -> {
            JobPostingForCandidateResponse jobPostingResponse = jobPostingMapper.toJobPostingForCandidateResponse(jobPosting);

            // Set recruiter info
            jobPostingResponse.setRecruiterInfo(jobPostingMapper.toRecruiterCompanyInfo(jobPosting.getRecruiter()));

            // Set skills from job descriptions
            jobPostingResponse.setSkills(jobPostingMapper.toJobPostingSkillResponseSet(jobPosting.getJobDescriptions()));

            // Check if the job is saved by the candidate
            jobPostingResponse.setSaved(
                savedJobRepo.findByCandidate_candidateIdAndJobPosting_Id(candidateId, jobPosting.getId()).isPresent()
            );

            jobPostingResponses.add(jobPostingResponse);
        });

        PageJobPostingForCandidateResponse pageJobPostingForCandidateResponse = jobPostingMapper.toPageJobPostingForCandidateResponse(jobPostingPage);
        pageJobPostingForCandidateResponse.setContent(jobPostingResponses);

        return pageJobPostingForCandidateResponse;
    }

}
