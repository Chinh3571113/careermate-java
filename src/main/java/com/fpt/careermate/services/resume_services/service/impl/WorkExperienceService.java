package com.fpt.careermate.services.resume_services.service.impl;

import com.fpt.careermate.services.resume_services.service.dto.request.WorkExperienceRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.WorkExperienceResponse;

public interface WorkExperienceService {
    WorkExperienceResponse addWorkExperienceToResume(WorkExperienceRequest workExperience);
    void removeWorkExperienceFromResume(int workExperienceId);
    WorkExperienceResponse updateWorkExperienceInResume(int resumeId,int workExp, WorkExperienceRequest workExperience);

}
