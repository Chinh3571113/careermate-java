package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.WorkExperienceRequest;
import com.fpt.careermate.services.dto.response.WorkExperienceResponse;

public interface WorkExperienceService {
    WorkExperienceResponse addWorkExperienceToResume(WorkExperienceRequest workExperience);
    void removeWorkExperienceFromResume(int resumeId, int workExperienceId);
    WorkExperienceResponse updateWorkExperienceInResume(int resumeId,int workExp, WorkExperienceRequest workExperience);

}
