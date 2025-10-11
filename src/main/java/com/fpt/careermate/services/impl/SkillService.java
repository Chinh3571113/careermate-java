package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.SkillRequest;
import com.fpt.careermate.services.dto.response.SkillResponse;

public interface SkillService {
    SkillResponse addSkillToResume(SkillRequest skill);
    void removeSkillFromResume(int resumeId, int skillId);
    SkillResponse updateSkillInResume(int resumeId,int skillId, SkillRequest skill);

}
