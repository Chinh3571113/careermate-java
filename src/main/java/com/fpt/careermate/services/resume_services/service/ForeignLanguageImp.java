package com.fpt.careermate.services.resume_services.service;


import com.fpt.careermate.services.resume_services.domain.ForeignLanguage;
import com.fpt.careermate.services.resume_services.domain.Resume;
import com.fpt.careermate.services.resume_services.repository.ForeignLanguageRepo;
import com.fpt.careermate.services.resume_services.repository.ResumeRepo;
import com.fpt.careermate.services.resume_services.service.dto.request.ForeignLanguageRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.ForeignLanguageResponse;
import com.fpt.careermate.services.resume_services.service.impl.ForeignLanguageService;
import com.fpt.careermate.services.resume_services.service.mapper.ForeignLanguageMapper;
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
public class ForeignLanguageImp implements ForeignLanguageService {
    ForeignLanguageRepo foreignLanguageRepo;
    ResumeImp resumeImp;
    ForeignLanguageMapper foreignLanguageMapper;
    ResumeRepo resumeRepo;

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public ForeignLanguageResponse addForeignLanguageToResume(ForeignLanguageRequest foreignLanguage) {
        Resume resume = resumeImp.getResumeEntityById(foreignLanguage.getResumeId());

        if (foreignLanguageRepo.countForeignLanguageByResumeId(resume.getResumeId()) >= 5) {
            throw new AppException(ErrorCode.OVERLOAD);
        }

        ForeignLanguage languageInfo = foreignLanguageMapper.toEntity(foreignLanguage);
        languageInfo.setResume(resume);
        resume.getForeignLanguages().add(languageInfo);

        ForeignLanguage savedLanguage = foreignLanguageRepo.save(languageInfo);

        return foreignLanguageMapper.toResponse(savedLanguage);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public void removeForeignLanguageFromResume(int resumeId, int foreignLanguageId) {
        foreignLanguageRepo.findById(foreignLanguageId)
                .orElseThrow(() -> new AppException(ErrorCode.FOREIGN_LANGUAGE_NOT_FOUND));
        foreignLanguageRepo.deleteById(foreignLanguageId);
    }

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public ForeignLanguageResponse updateForeignLanguageInResume(int resumeId, int foreignLanguageId, ForeignLanguageRequest foreignLanguage) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        ForeignLanguage existingLanguage = foreignLanguageRepo.findById(foreignLanguageId)
                .orElseThrow(() -> new AppException(ErrorCode.FOREIGN_LANGUAGE_NOT_FOUND));

        foreignLanguageMapper.updateEntity(foreignLanguage, existingLanguage);

        return foreignLanguageMapper.toResponse(foreignLanguageRepo.save(existingLanguage));
    }
}
