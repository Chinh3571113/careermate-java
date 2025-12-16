package com.fpt.careermate.services.resume_services.service;

import com.fpt.careermate.services.resume_services.domain.Education;
import com.fpt.careermate.services.resume_services.domain.Resume;
import com.fpt.careermate.services.resume_services.repository.EducationRepo;
import com.fpt.careermate.services.resume_services.repository.ResumeRepo;
import com.fpt.careermate.services.resume_services.service.dto.request.EducationRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.EducationResponse;
import com.fpt.careermate.services.resume_services.service.impl.EducationService;
import com.fpt.careermate.services.resume_services.service.mapper.EducationMapper;
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
public class EducationImp implements EducationService {
    EducationRepo educationRepo;
    ResumeImp resumeImp;
    EducationMapper educationMapper;
    ResumeRepo resumeRepo;

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public EducationResponse addEducationToResume(EducationRequest education) {
        Resume resume = resumeImp.getResumeEntityById(education.getResumeId());

        if (educationRepo.countEducationByResumeId(resume.getResumeId()) >= 3) {
            throw new AppException(ErrorCode.OVERLOAD);
        }

        Education educationInfo = educationMapper.toEntity(education);
        educationInfo.setResume(resume);
        resume.getEducations().add(educationInfo);

        Education savedEducation = educationRepo.save(educationInfo);

        resumeImp.syncCandidateProfileByResumeId(resume.getResumeId());

        return educationMapper.toResponse(savedEducation);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public void removeEducationFromResume(int educationId) {
        Education education = educationRepo.findById(educationId)
                .orElseThrow(() -> new AppException(ErrorCode.EDUCATION_NOT_FOUND));
        int resumeId = education.getResume().getResumeId();
        educationRepo.delete(education);
        resumeImp.syncCandidateProfileByResumeId(resumeId);
    }

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public EducationResponse updateEducationInResume(int resumeId, int educationId, EducationRequest education) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        Education existingEducation = educationRepo.findById(educationId)
                .orElseThrow(() -> new AppException(ErrorCode.EDUCATION_NOT_FOUND));

        educationMapper.updateEntity(education, existingEducation);
        Education updated = educationRepo.save(existingEducation);
        resumeImp.syncCandidateProfileByResumeId(resumeId);

        return educationMapper.toResponse(updated);
    }

}
