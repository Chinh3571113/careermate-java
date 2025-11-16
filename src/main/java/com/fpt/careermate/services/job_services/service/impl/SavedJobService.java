package com.fpt.careermate.services.job_services.service.impl;

import com.fpt.careermate.services.job_services.service.dto.response.PageJobPostingForCandidateResponse;
import com.fpt.careermate.services.job_services.service.dto.response.PageSavedJobPostingResponse;

public interface SavedJobService {
    boolean toggleSaveJob(int jobId);
    PageSavedJobPostingResponse getSavedJobs(int page, int size);
    PageJobPostingForCandidateResponse getJobsForCandidate(int page, int size);
}
