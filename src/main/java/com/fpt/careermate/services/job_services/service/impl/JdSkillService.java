package com.fpt.careermate.services.job_services.service.impl;

import com.fpt.careermate.services.job_services.service.dto.response.JdSkillResponse;

import java.util.List;

public interface JdSkillService {
    void createSkill(String name);
    List<JdSkillResponse> getAllSkill(String keyword);
    List<JdSkillResponse> getTopUsedSkillsFromFirst50();
}
