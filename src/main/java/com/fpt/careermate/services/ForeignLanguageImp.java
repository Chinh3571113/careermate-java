package com.fpt.careermate.services;


import com.fpt.careermate.domain.ForeignLanguage;
import com.fpt.careermate.domain.Resume;
import com.fpt.careermate.repository.ForeignLanguageRepo;
import com.fpt.careermate.repository.ResumeRepo;
import com.fpt.careermate.services.dto.request.ForeignLanguageRequest;
import com.fpt.careermate.services.dto.response.ForeignLanguageResponse;
import com.fpt.careermate.services.impl.ForeignLanguageService;
import com.fpt.careermate.services.mapper.ForeignLanguageMapper;
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
public class ForeignLanguageImp implements ForeignLanguageService {
    ForeignLanguageRepo foreignLanguageRepo;
    ResumeImp resumeImp;
    ForeignLanguageMapper foreignLanguageMapper;
    ResumeRepo resumeRepo;

    @Transactional
    @Override
    public ForeignLanguageResponse addForeignLanguageToResume(ForeignLanguageRequest foreignLanguage) {
        Resume resume = resumeImp.generateResume();

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
    public void removeForeignLanguageFromResume(int resumeId, int foreignLanguageId) {
        foreignLanguageRepo.findById(foreignLanguageId)
                .orElseThrow(() -> new AppException(ErrorCode.FOREIGN_LANGUAGE_NOT_FOUND));
        foreignLanguageRepo.deleteById(foreignLanguageId);
    }

    @Transactional
    @Override
    public ForeignLanguageResponse updateForeignLanguageInResume(int resumeId, int foreignLanguageId, ForeignLanguageRequest foreignLanguage) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        ForeignLanguage existingLanguage = foreignLanguageRepo.findById(foreignLanguageId)
                .orElseThrow(() -> new AppException(ErrorCode.FOREIGN_LANGUAGE_NOT_FOUND));

        foreignLanguageMapper.updateEntity(foreignLanguage, existingLanguage);

        return foreignLanguageMapper.toResponse(foreignLanguageRepo.save(existingLanguage));
    }
}
