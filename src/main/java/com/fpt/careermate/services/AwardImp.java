package com.fpt.careermate.services;

import com.fpt.careermate.domain.Award;
import com.fpt.careermate.domain.Resume;
import com.fpt.careermate.repository.AwardRepo;
import com.fpt.careermate.repository.ResumeRepo;
import com.fpt.careermate.services.dto.request.AwardRequest;
import com.fpt.careermate.services.dto.response.AwardResponse;
import com.fpt.careermate.services.impl.AwardService;
import com.fpt.careermate.services.mapper.AwardMapper;
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
public class AwardImp implements AwardService {
    AwardRepo awardRepo;
    ResumeImp resumeImp;
    AwardMapper awardMapper;
    ResumeRepo resumeRepo;

    @Transactional
    @Override
    public AwardResponse addAwardToResume(AwardRequest award) {
        Resume resume = resumeImp.generateResume();

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
    public void removeAwardFromResume(int resumeId, int awardId) {
        awardRepo.findById(awardId)
                .orElseThrow(() -> new AppException(ErrorCode.AWARD_NOT_FOUND));
        awardRepo.deleteById(awardId);
    }

    @Transactional
    @Override
    public AwardResponse updateAwardInResume(int resumeId, int awardId, AwardRequest award) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        Award existingAward = awardRepo.findById(awardId)
                .orElseThrow(() -> new AppException(ErrorCode.AWARD_NOT_FOUND));

        awardMapper.updateEntity(award, existingAward);

        return awardMapper.toResponse(awardRepo.save(existingAward));
    }
}
