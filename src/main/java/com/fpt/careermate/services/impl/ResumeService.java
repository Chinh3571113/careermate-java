package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.ResumeRequest;
import com.fpt.careermate.services.dto.response.ResumeResponse;

public interface ResumeService {
    ResumeResponse createResume(ResumeRequest resumeRequest);
    ResumeResponse getResumeById();
    void deleteResume(int resumeId);
    ResumeResponse updateResume(ResumeRequest resumeRequest);
}
