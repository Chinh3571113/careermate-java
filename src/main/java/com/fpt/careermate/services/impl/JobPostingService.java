package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.JobPostingApprovalRequest;
import com.fpt.careermate.services.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.dto.response.JobPostingForAdminResponse;
import com.fpt.careermate.services.dto.response.JobPostingForRecruiterResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface JobPostingService {
    // Recruiter methods
    void createJobPosting(JobPostingCreationRequest request);

    List<JobPostingForRecruiterResponse> getAllJobPostingForRecruiter();

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
}
