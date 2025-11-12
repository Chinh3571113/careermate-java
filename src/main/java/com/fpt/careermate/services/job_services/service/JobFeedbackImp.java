package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.common.constant.FeedbackType;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.job_services.domain.JobFeedback;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.repository.JobFeedbackRepo;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.job_services.service.dto.request.JobFeedbackRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobFeedbackResponse;
import com.fpt.careermate.services.job_services.service.impl.JobFeedbackService;
import com.fpt.careermate.services.job_services.service.mapper.JobFeedbackMapper;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobFeedbackImp implements JobFeedbackService {

    JobFeedbackRepo jobFeedbackRepo;
    JobPostingRepo jobPostingRepo;
    CandidateRepo candidateRepo;
    JobFeedbackMapper jobFeedbackMapper;

    @Override
    @Transactional
    public JobFeedbackResponse createJobFeedback(JobFeedbackRequest request) {
        log.info("Creating job feedback for candidate {} on job {}", request.getCandidateId(), request.getJobId());

        // Validate job posting exists
        JobPosting jobPosting = jobPostingRepo.findById(request.getJobId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // Validate candidate exists
        Candidate candidate = candidateRepo.findById(request.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));

        // Validate feedback type using enum
        FeedbackType feedbackTypeEnum = validateAndGetFeedbackType(request.getFeedbackType());

        // Check if feedback already exists
        jobFeedbackRepo.findByJobPostingIdAndCandidateCandidateIdAndFeedbackType(
                request.getJobId(), request.getCandidateId(), request.getFeedbackType())
                .ifPresent(existing -> {
                    throw new AppException(ErrorCode.JOB_FEEDBACK_ALREADY_EXISTS);
                });

        // Set default score if not provided
        Double score = request.getScore() != null ? request.getScore() : 1.0;

        // Create new job feedback
        JobFeedback jobFeedback = new JobFeedback();
        jobFeedback.setCandidate(candidate);
        jobFeedback.setJobPosting(jobPosting);
        jobFeedback.setFeedbackType(feedbackTypeEnum.getValue());
        jobFeedback.setScore(score);
        jobFeedback.setCreateAt(LocalDateTime.now());

        JobFeedback savedFeedback = jobFeedbackRepo.save(jobFeedback);
        log.info("Job feedback created successfully with id {}", savedFeedback.getId());

        return jobFeedbackMapper.toJobFeedbackResponse(savedFeedback);
    }

    @Override
    @Transactional
    public void removeJobFeedback(int jobId, int candidateId, String feedbackType) {
        log.info("Removing job feedback for candidate {} on job {} with type {}", candidateId, jobId, feedbackType);

        // Validate feedback exists
        JobFeedback feedback = jobFeedbackRepo.findByJobPostingIdAndCandidateCandidateIdAndFeedbackType(
                jobId, candidateId, feedbackType)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_FEEDBACK_NOT_FOUND));

        jobFeedbackRepo.delete(feedback);
        log.info("Job feedback removed successfully");
    }

    @Override
    public JobFeedbackResponse getJobFeedback(int jobId, int candidateId, String feedbackType) {
        log.info("Getting job feedback for candidate {} on job {} with type {}", candidateId, jobId, feedbackType);

        JobFeedback feedback = jobFeedbackRepo.findByJobPostingIdAndCandidateCandidateIdAndFeedbackType(
                jobId, candidateId, feedbackType)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_FEEDBACK_NOT_FOUND));

        return jobFeedbackMapper.toJobFeedbackResponse(feedback);
    }

    @Override
    public List<JobFeedbackResponse> getJobFeedbacksByJobId(int jobId) {
        log.info("Getting all feedbacks for job {}", jobId);

        // Validate job posting exists
        jobPostingRepo.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

        return jobFeedbackRepo.findByJobPostingId(jobId).stream()
                .map(jobFeedbackMapper::toJobFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobFeedbackResponse> getJobFeedbacksByCandidateId(int candidateId) {
        log.info("Getting all feedbacks for candidate {}", candidateId);

        // Validate candidate exists
        candidateRepo.findById(candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));

        return jobFeedbackRepo.findByCandidateCandidateId(candidateId).stream()
                .map(jobFeedbackMapper::toJobFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobFeedbackResponse> getJobFeedbacksByCandidateIdAndType(int candidateId, String feedbackType) {
        log.info("Getting all feedbacks for candidate {} with type {}", candidateId, feedbackType);

        // Validate candidate exists
        candidateRepo.findById(candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));

        // Validate feedback type using enum
        validateAndGetFeedbackType(feedbackType);

        return jobFeedbackRepo.findByCandidateCandidateIdAndFeedbackType(candidateId, feedbackType).stream()
                .map(jobFeedbackMapper::toJobFeedbackResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<JobFeedbackResponse> getAllJobFeedbacks() {
        log.info("Getting all job feedbacks");

        return jobFeedbackRepo.findAll().stream()
                .map(jobFeedbackMapper::toJobFeedbackResponse)
                .collect(Collectors.toList());
    }

    private FeedbackType validateAndGetFeedbackType(String feedbackType) {
        try {
            return FeedbackType.fromValue(feedbackType);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_FEEDBACK_TYPE);
        }
    }
}
