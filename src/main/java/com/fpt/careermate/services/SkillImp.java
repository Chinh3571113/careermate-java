package com.fpt.careermate.services;

import com.fpt.careermate.domain.Resume;
import com.fpt.careermate.domain.Skill;
import com.fpt.careermate.repository.ResumeRepo;
import com.fpt.careermate.repository.SkillRepo;
import com.fpt.careermate.services.dto.request.SkillRequest;
import com.fpt.careermate.services.dto.response.SkillResponse;
import com.fpt.careermate.services.impl.SkillService;
import com.fpt.careermate.services.mapper.SkillMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SkillImp implements SkillService {
    SkillRepo skillRepo;
    ResumeImp resumeImp;
    SkillMapper skillMapper;
    ResumeRepo resumeRepo;

    @Transactional
    @Override
    public SkillResponse addSkillToResume(SkillRequest skill) {
        Resume resume = resumeImp.generateResume();

        if (skillRepo.countSkillByResumeId(resume.getResumeId()) >= 10) {
            throw new AppException(ErrorCode.OVERLOAD);
        }

        Skill skillInfo = skillMapper.toEntity(skill);
        skillInfo.setResume(resume);
        resume.getSkills().add(skillInfo);

        Skill savedSkill = skillRepo.save(skillInfo);

        return skillMapper.toResponse(savedSkill);
    }

    @Override
    public void removeSkillFromResume(int resumeId, int skillId) {
        skillRepo.findById(skillId)
                .orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_FOUND));
        skillRepo.deleteById(skillId);
    }

    @Transactional
    @Override
    public SkillResponse updateSkillInResume(int resumeId, int skillId, SkillRequest skill) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        Skill existingSkill = skillRepo.findById(skillId)
                .orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_FOUND));

        skillMapper.updateEntity(skill, existingSkill);

        return skillMapper.toResponse(skillRepo.save(existingSkill));
    }
}
