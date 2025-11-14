package com.fpt.careermate.services.job_services.service.impl;

import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.job_services.service.dto.request.JobPostingApprovalRequest;
import com.fpt.careermate.services.job_services.service.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForAdminResponse;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForCandidateResponse;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForRecruiterResponse;
import com.fpt.careermate.services.job_services.service.dto.response.PageJobPostingForRecruiterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobPostingService {
    // Recruiter methods
    void createJobPosting(JobPostingCreationRequest request);

    PageJobPostingForRecruiterResponse getAllJobPostingForRecruiter(int page, int size, String keyword);

    JobPostingForRecruiterResponse getJobPostingDetailForRecruiter(int id);

    void updateJobPosting(int id, JobPostingCreationRequest request);

    void deleteJobPosting(int id);

    void pauseJobPosting(int id);

    // Admin methods
    Page<JobPostingForAdminResponse> getAllJobPostingsForAdmin(int page, int size, String status, String sortBy,
            String sortDirection);

    JobPostingForAdminResponse getJobPostingDetailForAdmin(int id);

    void approveOrRejectJobPosting(int id, JobPostingApprovalRequest request);

    List<JobPostingForAdminResponse> getPendingJobPostings();

    // Candidate methods - view approved job postings only
    PageResponse<JobPostingForCandidateResponse> getAllApprovedJobPostings(String keyword, Pageable pageable);

    JobPostingForCandidateResponse getJobPostingDetailForCandidate(int id);
}
