package com.fpt.careermate.services.resume_services.service;

import com.fpt.careermate.services.resume_services.domain.Resume;
import com.fpt.careermate.services.resume_services.domain.WorkExperience;
import com.fpt.careermate.services.resume_services.repository.ResumeRepo;
import com.fpt.careermate.services.resume_services.repository.WorkExperienceRepo;
import com.fpt.careermate.services.resume_services.service.dto.request.WorkExperienceRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.WorkExperienceResponse;
import com.fpt.careermate.services.resume_services.service.impl.WorkExperienceService;
import com.fpt.careermate.services.resume_services.service.mapper.WorkExperienceMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WorkExperienceImp implements WorkExperienceService {
    WorkExperienceRepo workExperienceRepo;
    ResumeImp resumeImp;
    WorkExperienceMapper workExperienceMapper;
    ResumeRepo resumeRepo;

    @Transactional
    @Override
    public WorkExperienceResponse addWorkExperienceToResume(WorkExperienceRequest workExperience) {
        Resume resume = resumeImp.getResumeEntityById(workExperience.getResumeId());

        if (workExperienceRepo.countWorkExperienceByResumeId(resume.getResumeId()) >= 5) {
            throw new AppException(ErrorCode.OVERLOAD);
        }

        WorkExperience workExpInfo = workExperienceMapper.toEntity(workExperience);
        workExpInfo.setResume(resume);
        resume.getWorkExperiences().add(workExpInfo);

        WorkExperience savedWorkExp = workExperienceRepo.save(workExpInfo);

        return workExperienceMapper.toResponse(savedWorkExp);
    }

    @Override
    public void removeWorkExperienceFromResume(int workExperienceId) {
        workExperienceRepo.findById(workExperienceId)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_EXPERIENCE_NOT_FOUND));
        workExperienceRepo.deleteById(workExperienceId);
    }

    @Transactional
    @Override
    public WorkExperienceResponse updateWorkExperienceInResume(int resumeId, int workExp, WorkExperienceRequest workExperience) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        WorkExperience existingWorkExp = workExperienceRepo.findById(workExp)
                .orElseThrow(() -> new AppException(ErrorCode.WORK_EXPERIENCE_NOT_FOUND));

        workExperienceMapper.updateEntity(workExperience, existingWorkExp);

        return workExperienceMapper.toResponse(workExperienceRepo.save(existingWorkExp));
    }
}
