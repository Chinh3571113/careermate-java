package com.fpt.careermate.services.resume_services.service.impl;

import com.fpt.careermate.services.resume_services.service.dto.request.ResumeRequest;
import com.fpt.careermate.services.resume_services.service.dto.request.ResumeStatusRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.ResumeResponse;

import java.util.List;

public interface ResumeService {
    ResumeResponse createResume(ResumeRequest resumeRequest);
    List<ResumeResponse> getAllResumesByCandidate();
    ResumeResponse getResumeById(int resumeId);
    void deleteResume(int resumeId);
    ResumeResponse updateResume(int resumeId, ResumeRequest resumeRequest);
    ResumeResponse patchResumeStatus(int resumeId, ResumeStatusRequest request);
}
