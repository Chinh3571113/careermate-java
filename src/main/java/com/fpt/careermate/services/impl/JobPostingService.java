package com.fpt.careermate.services.impl;


import com.fpt.careermate.services.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.dto.response.JobPostingForAdminResponse;
import com.fpt.careermate.services.dto.response.JobPostingForRecruiterResponse;
import com.fpt.careermate.services.dto.response.JobPostingResponse;

import java.util.List;

public interface JobPostingService {
    void createJobPosting(JobPostingCreationRequest request);
    List<JobPostingResponse> getAllJobPostings();
    List<JobPostingForAdminResponse> getAllJobPostingsForAdmin();
    List<JobPostingForRecruiterResponse> getAllJobPostingForRecruiter();
}
