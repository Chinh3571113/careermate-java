package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.constant.StatusJobApply;
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
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class JobApplyImp implements JobApplyService {

    JobApplyRepo jobApplyRepo;
    JobPostingRepo jobPostingRepo;
    CandidateRepo candidateRepo;
    JobApplyMapper jobApplyMapper;

    @Override
    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    public JobApplyResponse createJobApply(JobApplyRequest request) {
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
        JobApply jobApply = jobApplyRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // Update status if provided
       jobApply.setStatus(request);

        JobApply updatedJobApply = jobApplyRepo.save(jobApply);
        return jobApplyMapper.toJobApplyResponse(updatedJobApply);
    }

    @Override
    @Transactional
    public void deleteJobApply(int id) {
        JobApply jobApply = jobApplyRepo.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));
        jobApplyRepo.delete(jobApply);
    }
}
