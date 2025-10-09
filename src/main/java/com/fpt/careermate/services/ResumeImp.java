package com.fpt.careermate.services;

import com.fpt.careermate.domain.*;
import com.fpt.careermate.repository.CandidateRepo;
import com.fpt.careermate.repository.ResumeRepo;
import com.fpt.careermate.services.dto.request.*;
import com.fpt.careermate.services.dto.response.ResumeResponse;
import com.fpt.careermate.services.impl.ResumeService;
import com.fpt.careermate.services.mapper.ResumeMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ResumeImp implements ResumeService {

    ResumeRepo resumeRepo;
    CandidateRepo candidateRepo;
    ResumeMapper resumeMapper;
    AuthenticationImp authenticationService;

    @Override
    public ResumeResponse createResume(ResumeRequest resumeRequest) {
        return null;
    }

    @Override
    public ResumeResponse getResumeById() {
        // Get authenticated user's account
        Account account = authenticationService.findByEmail();

        // Find candidate by authenticated account
        Candidate candidate = candidateRepo.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));


        Resume resume = resumeRepo.findByCandidateCandidateId(candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND)); // You may want to add RESUME_NOT_FOUND

        return resumeMapper.toResumeResponse(resume);
    }

    @Transactional
    @Override
    public void deleteResume(int resumeId) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        resumeRepo.deleteById(resumeId);
    }

    @Transactional
    @Override
    public ResumeResponse updateResume(ResumeRequest resumeRequest) {


        return null;
    }


}
