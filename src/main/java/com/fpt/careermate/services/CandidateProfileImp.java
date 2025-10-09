package com.fpt.careermate.services;

import com.fpt.careermate.domain.Account;
import com.fpt.careermate.domain.Candidate;
import com.fpt.careermate.domain.IndustryExperiences;
import com.fpt.careermate.domain.WorkModel;
import com.fpt.careermate.repository.CandidateRepo;
import com.fpt.careermate.repository.IndustryExperienceRepo;
import com.fpt.careermate.repository.WorkModelRepo;
import com.fpt.careermate.services.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.dto.request.GeneralInfoRequest;
import com.fpt.careermate.services.dto.response.CandidateProfileResponse;
import com.fpt.careermate.services.dto.response.GeneralInfoResponse;
import com.fpt.careermate.services.dto.response.PageResponse;
import com.fpt.careermate.services.impl.CandidateProfileService;
import com.fpt.careermate.services.mapper.CandidateMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidateProfileImp implements CandidateProfileService {
    CandidateRepo candidateRepo;
    CandidateMapper candidateMapper;
    AuthenticationImp authenticationService;
    WorkModelRepo workModelRepo;
    IndustryExperienceRepo industryExperienceRepo;




    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public PageResponse<CandidateProfileResponse> findAll(Pageable pageable) {
        //role admin can get any profile by id
        Page<Candidate> candidatePage = candidateRepo.findAll(pageable);
        return new PageResponse<>(
                candidatePage.getContent()
                        .stream()
                        .map(candidateMapper::toCandidateProfileResponse)
                        .toList(),
                candidatePage.getNumber(),
                candidatePage.getSize(),
                candidatePage.getTotalElements(),
                candidatePage.getTotalPages()
        );
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public CandidateProfileResponse saveOrUpdateCandidateProfile(CandidateProfileRequest request) {
        Candidate candidate = generateProfile();
        candidateMapper.updateCandidateFromRequest(request, candidate);
        Candidate savedCandidate = candidateRepo.save(candidate);
        return candidateMapper.toCandidateProfileResponse(savedCandidate);

    }

    @Override
    public void deleteProfile(int id) {
        candidateRepo.findById(id).orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));
        candidateRepo.deleteById(id);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Transactional
    @Override
    public GeneralInfoResponse saveOrUpdateCandidateGeneralInfo(GeneralInfoRequest request) {
        Candidate candidate = generateProfile();

        // Clear existing collections (orphanRemoval will delete old records)
        if (candidate.getIndustryExperiences() != null) {
            candidate.getIndustryExperiences().clear();
        } else {
            candidate.setIndustryExperiences(new java.util.ArrayList<>());
        }

        if (candidate.getWorkModels() != null) {
            candidate.getWorkModels().clear();
        } else {
            candidate.setWorkModels(new java.util.ArrayList<>());
        }

        // Create new industry experiences from request
        if (request.getIndustryExperiences() != null && !request.getIndustryExperiences().isEmpty()) {
            for (GeneralInfoRequest.IndustryExperienceRequest fieldName : request.getIndustryExperiences()) {
                IndustryExperiences exp = IndustryExperiences.builder()
                        .fieldName(fieldName.getFieldName())
                        .candidateId(candidate.getCandidateId())
                        .candidate(candidate)
                        .build();
                candidate.getIndustryExperiences().add(exp);
            }
        }

        // Create new work models from request
        if (request.getWorkModels() != null && !request.getWorkModels().isEmpty()) {
            for (GeneralInfoRequest.WorkModelRequest rq : request.getWorkModels()) {
                WorkModel wm = WorkModel.builder()
                        .name(rq.getName())
                        .candidateId(candidate.getCandidateId())
                        .candidate(candidate)
                        .build();
                candidate.getWorkModels().add(wm);
            }
        }

        Candidate savedCandidate = candidateRepo.save(candidate);
        return candidateMapper.toGeneralInfoResponse(savedCandidate);
    }

    private Candidate generateProfile() {
        Account account = authenticationService.findByEmail();
        return candidateRepo.findByAccount_Id(account.getId())
                .orElseGet(() -> {
                    Candidate newCandidate = new Candidate();
                    newCandidate.setAccount(account);
                    return newCandidate;
                });
    }
}
