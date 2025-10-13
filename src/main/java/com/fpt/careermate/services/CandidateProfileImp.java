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
    public GeneralInfoResponse saveCandidateGeneralInfo(GeneralInfoRequest request) {
        Candidate candidate = generateProfile();

        // Update basic fields
        candidate.setJobLevel(request.getJobLevel());
        candidate.setExperience(request.getExperience());

        // Save candidate first to get the ID
        Candidate savedCandidate = candidateRepo.save(candidate);

        // Handle Industry Experiences
        if (request.getIndustryExperiences() != null) {
            for (GeneralInfoRequest.IndustryExperienceRequest ieRequest : request.getIndustryExperiences()) {
                IndustryExperiences industryExp = IndustryExperiences.builder()
                        .fieldName(ieRequest.getFieldName())
                        .candidateId(savedCandidate.getCandidateId())
                        .candidate(savedCandidate)
                        .build();
                industryExperienceRepo.save(industryExp);
            }
        }

        // Handle Work Models
        if (request.getWorkModels() != null) {
            for (GeneralInfoRequest.WorkModelRequest wmRequest : request.getWorkModels()) {
                WorkModel workModel = WorkModel.builder()
                        .name(wmRequest.getName())
                        .candidateId(savedCandidate.getCandidateId())
                        .candidate(savedCandidate)
                        .build();
                workModelRepo.save(workModel);
            }
        }

        return candidateMapper.toGeneralInfoResponse(savedCandidate);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public CandidateProfileResponse getCandidateProfileById() {
        return candidateMapper.toCandidateProfileResponse(generateProfile());
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public GeneralInfoResponse getCandidateGeneralInfoById() {
        return candidateMapper.toGeneralInfoResponse(generateProfile());
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Transactional
    @Override
    public GeneralInfoResponse updateCandidateGeneralInfo(GeneralInfoRequest request) {
        Candidate candidate = generateProfile();

        // Update basic fields
        candidate.setJobLevel(request.getJobLevel());
        candidate.setExperience(request.getExperience());

        Candidate updatedCandidate = candidateRepo.save(candidate);
        Integer candidateId = updatedCandidate.getCandidateId();

        // Update Industry Experiences - delete old and insert new
        if (request.getIndustryExperiences() != null) {
            // Delete existing industry experiences for this candidate
            industryExperienceRepo.deleteByCandidateId(candidateId);

            // Add new industry experiences
            for (GeneralInfoRequest.IndustryExperienceRequest ieRequest : request.getIndustryExperiences()) {
                IndustryExperiences industryExp = IndustryExperiences.builder()
                        .fieldName(ieRequest.getFieldName())
                        .candidateId(candidateId)
                        .candidate(updatedCandidate)
                        .build();
                industryExperienceRepo.save(industryExp);
            }
        }

        // Update Work Models - delete old and insert new
        if (request.getWorkModels() != null) {
            // Delete existing work models for this candidate
            workModelRepo.deleteByCandidateId(candidateId);

            // Add new work models
            for (GeneralInfoRequest.WorkModelRequest wmRequest : request.getWorkModels()) {
                WorkModel workModel = WorkModel.builder()
                        .name(wmRequest.getName())
                        .candidateId(candidateId)
                        .candidate(updatedCandidate)
                        .build();
                workModelRepo.save(workModel);
            }
        }

        return candidateMapper.toGeneralInfoResponse(updatedCandidate);
    }


    public Candidate generateProfile() {
        Account account = authenticationService.findByEmail();
        return candidateRepo.findByAccount_Id(account.getId())
                .orElseGet(() -> {
                    Candidate newCandidate = new Candidate();
                    newCandidate.setAccount(account);
                    return newCandidate;
                });
    }
}
