package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.ForeignLanguageRequest;
import com.fpt.careermate.services.dto.response.ForeignLanguageResponse;

public interface ForeignLanguageService {
    ForeignLanguageResponse addForeignLanguageToResume(ForeignLanguageRequest foreignLanguage);
    void removeForeignLanguageFromResume(int resumeId, int foreignLanguageId);
    ForeignLanguageResponse updateForeignLanguageInResume(int resumeId, int foreignLanguageId, ForeignLanguageRequest foreignLanguage);

}
