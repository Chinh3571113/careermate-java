package com.fpt.careermate.services.job_services.service.impl;

import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.job_services.service.dto.request.JobApplyRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobApplyResponse;

import java.util.List;

public interface JobApplyService {
    JobApplyResponse createJobApply(JobApplyRequest request);
    JobApplyResponse getJobApplyById(int id);
    List<JobApplyResponse> getAllJobApplies();
    List<JobApplyResponse> getJobAppliesByJobPosting(int jobPostingId);
    List<JobApplyResponse> getJobAppliesByCandidate(int candidateId);
    JobApplyResponse updateJobApply(int id, StatusJobApply request);
    void deleteJobApply(int id);
    PageResponse<JobApplyResponse> getJobAppliesByCandidateWithFilter(
            int candidateId,
            StatusJobApply status,
            int page,
            int size);
    
    // Recruiter-specific methods
    List<JobApplyResponse> getJobAppliesByRecruiter();
    PageResponse<JobApplyResponse> getJobAppliesByRecruiterWithFilter(
            StatusJobApply status,
            int page,
            int size);
}
