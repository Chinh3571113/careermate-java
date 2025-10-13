package com.fpt.careermate.services;

import com.fpt.careermate.domain.Education;
import com.fpt.careermate.domain.Resume;
import com.fpt.careermate.repository.EducationRepo;
import com.fpt.careermate.repository.ResumeRepo;
import com.fpt.careermate.services.dto.request.EducationRequest;
import com.fpt.careermate.services.dto.response.EducationResponse;
import com.fpt.careermate.services.impl.EducationService;
import com.fpt.careermate.services.mapper.EducationMapper;
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
public class EducationImp implements EducationService {
    EducationRepo educationRepo;
    ResumeImp resumeImp;
    EducationMapper educationMapper;
    ResumeRepo resumeRepo;

    @Transactional
    @Override
    public EducationResponse addEducationToResume(EducationRequest education) {
        Resume resume = resumeImp.generateResume();



        if (educationRepo.countEducationByResumeId(resume.getResumeId()) >= 3) {
            throw new AppException(ErrorCode.OVERLOAD);
        }

        Education educationInfo = educationMapper.toEntity(education);
        educationInfo.setResume(resume);
        resume.getEducations().add(educationInfo);

        Education savedEducation = educationRepo.save(educationInfo);

        return educationMapper.toResponse(savedEducation);
    }

    @Override
    public void removeEducationFromResume(int educationId) {
        educationRepo.findById(educationId)
                .orElseThrow(() -> new AppException(ErrorCode.EDUCATION_NOT_FOUND));
        educationRepo.deleteById(educationId);
    }

    @Transactional
    @Override
    public EducationResponse updateEducationInResume(int resumeId, int educationId, EducationRequest education) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        Education existingEducation = educationRepo.findById(educationId)
                .orElseThrow(() -> new AppException(ErrorCode.EDUCATION_NOT_FOUND));

        educationMapper.updateEntity(education, existingEducation);

        return educationMapper.toResponse(educationRepo.save(existingEducation));
    }

}
