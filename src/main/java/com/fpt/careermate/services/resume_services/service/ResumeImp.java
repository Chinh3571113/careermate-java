package com.fpt.careermate.services.resume_services.service;

import com.fpt.careermate.common.constant.ResumeType;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.profile_services.service.CandidateProfileImp;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.resume_services.repository.ResumeRepo;
import com.fpt.careermate.services.resume_services.service.dto.request.ResumeStatusRequest;
import com.fpt.careermate.services.resume_services.service.dto.request.ResumeTypeRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.ResumeResponse;
import com.fpt.careermate.services.resume_services.service.impl.ResumeService;
import com.fpt.careermate.services.resume_services.service.mapper.ResumeMapper;
import com.fpt.careermate.services.resume_services.domain.Resume;
import com.fpt.careermate.services.resume_services.service.dto.request.ResumeRequest;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResumeResponse createResume(ResumeRequest resumeRequest) {
        Candidate candidate = candidateProfileImp.generateProfile();

        // Create new resume
        Resume newResume = Resume.builder()
                .candidate(candidate)
                .isActive(false)
                .type(resumeRequest.getType())
                .aboutMe(resumeRequest.getAboutMe())
                .build();

        Resume savedResume = resumeRepo.save(newResume);
        return resumeMapper.toResumeResponse(savedResume);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public List<ResumeResponse> getAllResumesByCandidate() {
        // Get authenticated user's candidate profile
        Candidate candidate = candidateProfileImp.generateProfile();

        // Find all resumes for this candidate
        List<Resume> resumes = resumeRepo.findByCandidateCandidateId(candidate.getCandidateId());

        return resumes.stream()
                .map(resumeMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResumeResponse getResumeById(int resumeId) {
        // Get authenticated user's candidate profile
        Candidate candidate = candidateProfileImp.generateProfile();

        // Find resume by ID and ensure it belongs to the authenticated candidate
        Resume resume = resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));

        return resumeMapper.toResumeResponse(resume);
    }

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public void deleteResume(int resumeId) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        resumeRepo.deleteById(resumeId);
    }

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public ResumeResponse updateResume(int resumeId, ResumeRequest resumeRequest) {
        // Get authenticated user's candidate profile
        Candidate candidate = candidateProfileImp.generateProfile();

        // Find resume and ensure it belongs to the authenticated candidate
        Resume resume = resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));

        // Update resume

        resume.setAboutMe(resumeRequest.getAboutMe());
        resume.setType(resumeRequest.getType());
        resume.setResumeUrl(resumeRequest.getResumeUrl());

        Resume updatedResume = resumeRepo.save(resume);

        return resumeMapper.toResumeResponse(updatedResume);
    }

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public ResumeResponse patchResumeStatus(int resumeId, ResumeStatusRequest request) {
        // Get authenticated user's candidate profile
        Candidate candidate = candidateProfileImp.generateProfile();

        // Find resume and ensure it belongs to the authenticated candidate
        Resume resume = resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));

        // Update only isActive field
        resume.setIsActive(request.getIsActive());
        Resume updatedResume = resumeRepo.save(resume);

        return resumeMapper.toResumeResponse(updatedResume);
    }

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public ResumeResponse patchResumeType(int resumeId, ResumeType request) {
        // Get authenticated user's candidate profile
        Candidate candidate = candidateProfileImp.generateProfile();

        // Find resume and ensure it belongs to the authenticated candidate
        Resume resume = resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));

        // Check if resume is active
        if (!resume.getIsActive()) {
            throw new AppException(ErrorCode.RESUME_NOT_ACTIVE);
        }

        // Update only type field
        resume.setType(request);
        Resume updatedResume = resumeRepo.save(resume);

        return resumeMapper.toResumeResponse(updatedResume);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public List<ResumeResponse> getResumesByType(ResumeType type) {
        // Get authenticated user's candidate profile
        Candidate candidate = candidateProfileImp.generateProfile();

        // Find all resumes for this candidate with the specified type
        List<Resume> resumes = resumeRepo.findByCandidateCandidateIdAndType(candidate.getCandidateId(), type);

        // Filter only active resumes
        return resumes.stream()
                .filter(resume -> resume.getIsActive() != null && resume.getIsActive())
                .map(resumeMapper::toResumeResponse)
                .collect(Collectors.toList());
    }

    // Helper method to get resume by ID for other services (used by Education, Certificate, etc.)
    public Resume getResumeEntityById(int resumeId) {
        Candidate candidate = candidateProfileImp.generateProfile();
        return resumeRepo.findByResumeIdAndCandidateCandidateId(resumeId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
    }
}
