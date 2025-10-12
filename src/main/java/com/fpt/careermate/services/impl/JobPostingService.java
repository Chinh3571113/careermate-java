package com.fpt.careermate.services.impl;


import com.fpt.careermate.services.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.dto.response.JobPostingForRecruiterResponse;

import java.util.List;

public interface JobPostingService {
    void createJobPosting(JobPostingCreationRequest request);
    List<JobPostingForRecruiterResponse> getAllJobPostingForRecruiter();
}
