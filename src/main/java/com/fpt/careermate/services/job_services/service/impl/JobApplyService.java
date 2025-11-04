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
    JobApplyResponse updateJobApply(int id, StatusJobApply status);
    void deleteJobApply(int id);

    // New method for filtering with pagination
    PageResponse<JobApplyResponse> getJobAppliesByCandidateWithFilter(
            int candidateId,
            StatusJobApply status,
            int page,
            int size);
}
