package com.fpt.careermate.services.resume_services.service;

import com.fpt.careermate.services.resume_services.domain.Resume;
import com.fpt.careermate.services.resume_services.domain.Skill;
import com.fpt.careermate.services.resume_services.repository.ResumeRepo;
import com.fpt.careermate.services.resume_services.repository.SkillRepo;
import com.fpt.careermate.services.resume_services.service.dto.request.SkillRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.SkillResponse;
import com.fpt.careermate.services.resume_services.service.impl.SkillService;
import com.fpt.careermate.services.resume_services.service.mapper.SkillMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public SkillResponse addSkillToResume(SkillRequest skill) {
        Resume resume = resumeImp.getResumeEntityById(skill.getResumeId());

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
    @PreAuthorize("hasRole('CANDIDATE')")
    public void removeSkillFromResume(int resumeId, int skillId) {
        skillRepo.findById(skillId)
                .orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_FOUND));
        skillRepo.deleteById(skillId);
    }

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public SkillResponse updateSkillInResume(int resumeId, int skillId, SkillRequest skill) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        Skill existingSkill = skillRepo.findById(skillId)
                .orElseThrow(() -> new AppException(ErrorCode.SKILL_NOT_FOUND));

        skillMapper.updateEntity(skill, existingSkill);

        return skillMapper.toResponse(skillRepo.save(existingSkill));
    }
}
