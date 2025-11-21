package com.fpt.careermate.services.resume_services.service;

import com.fpt.careermate.services.resume_services.domain.Award;
import com.fpt.careermate.services.resume_services.domain.Resume;
import com.fpt.careermate.services.resume_services.repository.AwardRepo;
import com.fpt.careermate.services.resume_services.repository.ResumeRepo;
import com.fpt.careermate.services.resume_services.service.dto.request.AwardRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.AwardResponse;
import com.fpt.careermate.services.resume_services.service.impl.AwardService;
import com.fpt.careermate.services.resume_services.service.mapper.AwardMapper;
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
public class AwardImp implements AwardService {
    AwardRepo awardRepo;
    ResumeImp resumeImp;
    AwardMapper awardMapper;
    ResumeRepo resumeRepo;

    @Transactional
    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public AwardResponse addAwardToResume(AwardRequest award) {
        Resume resume = resumeImp.getResumeEntityById(award.getResumeId());

        if (awardRepo.countAwardByResumeId(resume.getResumeId()) >= 5) {
            throw new AppException(ErrorCode.OVERLOAD);
        }

        Award awardInfo = awardMapper.toEntity(award);
        awardInfo.setResume(resume);
        resume.getAwards().add(awardInfo);

        Award savedAward = awardRepo.save(awardInfo);

        return awardMapper.toResponse(savedAward);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public void removeAwardFromResume(int resumeId, int awardId) {
        awardRepo.findById(awardId)
                .orElseThrow(() -> new AppException(ErrorCode.AWARD_NOT_FOUND));
        awardRepo.deleteById(awardId);
    }

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public AwardResponse updateAwardInResume(int resumeId, int awardId, AwardRequest award) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        Award existingAward = awardRepo.findById(awardId)
                .orElseThrow(() -> new AppException(ErrorCode.AWARD_NOT_FOUND));

        awardMapper.updateEntity(award, existingAward);

        return awardMapper.toResponse(awardRepo.save(existingAward));
    }
}
