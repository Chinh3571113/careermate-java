package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.AwardRequest;
import com.fpt.careermate.services.dto.response.*;

public interface AwardService {

    AwardResponse addAwardToResume(AwardRequest award);
    void removeAwardFromResume(int resumeId, int awardId);
    AwardResponse updateAwardInResume(int resumeId,int awardId, AwardRequest award);
}
