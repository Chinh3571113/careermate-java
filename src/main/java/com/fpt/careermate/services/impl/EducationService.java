package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.EducationRequest;
import com.fpt.careermate.services.dto.response.EducationResponse;

public interface EducationService {
    EducationResponse addEducationToResume( EducationRequest education);
    void removeEducationFromResume( int educationId);
    EducationResponse updateEducationInResume(int resumeId,int educationId, EducationRequest education);

}
