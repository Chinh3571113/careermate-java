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
    CandidateProfileImp candidateProfileImp;
    AuthenticationImp authenticationService;

    @Override
    @Transactional
    public ResumeResponse createResume(ResumeRequest resumeRequest) {
        Candidate candidate = candidateProfileImp.generateProfile();

        // Check if resume already exists for this candidate
        if (resumeRepo.findByCandidateCandidateId(candidate.getCandidateId()).isPresent()) {
            throw new AppException(ErrorCode.RESUME_ALREADY_EXISTS);
        }

        // Create new resume
        Resume newResume = new Resume();
        newResume.setCandidate(candidate);
        newResume.setAboutMe(resumeRequest.getAboutMe());

        Resume savedResume = resumeRepo.save(newResume);
        return resumeMapper.toResumeResponse(savedResume);
    }

    @Override
    public ResumeResponse getResumeById() {
        // Get authenticated user's account
        Account account = authenticationService.findByEmail();

        // Find candidate by authenticated account
        Candidate candidate = candidateRepo.findByAccount_Id(account.getId())
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
        Resume resume = generateResume();
        resume.setAboutMe(resumeRequest.getAboutMe());
        return resumeMapper.toResumeResponse(resume);
    }

    public Resume generateResume(){
        return resumeRepo.findByCandidateCandidateId(candidateProfileImp.generateProfile().getCandidateId()).orElseGet(
                () -> {
                    Resume newResume = new Resume();
                    newResume.setCandidate(candidateProfileImp.generateProfile());
                    resumeRepo.save(newResume);
                    return newResume;
                }
        );
    }


}
