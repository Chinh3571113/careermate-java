package com.fpt.careermate.services.job_services.service.impl;

import com.fpt.careermate.services.job_services.service.dto.request.JobFeedbackRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobFeedbackResponse;
import java.util.List;

public interface JobFeedbackService {
    JobFeedbackResponse createJobFeedback(JobFeedbackRequest request);
    void removeJobFeedback(int jobId, int candidateId, String feedbackType);
    JobFeedbackResponse getJobFeedback(int jobId, int candidateId, String feedbackType);
    List<JobFeedbackResponse> getJobFeedbacksByJobId(int jobId);
    List<JobFeedbackResponse> getJobFeedbacksByCandidateId(int candidateId);
    List<JobFeedbackResponse> getJobFeedbacksByCandidateIdAndType(int candidateId, String feedbackType);
    List<JobFeedbackResponse> getAllJobFeedbacks();
}
