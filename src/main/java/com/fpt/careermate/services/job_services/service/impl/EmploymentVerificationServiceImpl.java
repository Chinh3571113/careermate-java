package com.fpt.careermate.services.job_services.service.impl;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.job_services.domain.EmploymentVerification;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.repository.EmploymentVerificationRepo;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.job_services.service.EmploymentVerificationService;
import com.fpt.careermate.services.job_services.service.dto.request.EmploymentTerminationRequest;
import com.fpt.careermate.services.job_services.service.dto.request.EmploymentVerificationRequest;
import com.fpt.careermate.services.job_services.service.dto.response.EmploymentVerificationResponse;
import com.fpt.careermate.services.job_services.service.mapper.EmploymentVerificationMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simplified employment verification service.
 * Just tracks: started working, stopped working, duration.
 * No verification workflows, no probation, no complex approvals.
 */
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EmploymentVerificationServiceImpl implements EmploymentVerificationService {
    
    EmploymentVerificationRepo employmentVerificationRepo;
    JobApplyRepo jobApplyRepo;
    EmploymentVerificationMapper mapper;
    
    @Override
    @Transactional
    public EmploymentVerificationResponse createEmploymentVerification(
        Integer jobApplyId,
        EmploymentVerificationRequest request,
        Integer recruiterId
    ) {
        // Validate job apply exists
        JobApply jobApply = jobApplyRepo.findById(jobApplyId)
            .orElseThrow(() -> new AppException(ErrorCode.JOB_APPLY_NOT_FOUND));
        
        // Check if employment verification already exists
        if (employmentVerificationRepo.findByJobApplyId(jobApplyId).isPresent()) {
            throw new AppException(ErrorCode.EMPLOYMENT_VERIFICATION_EXISTS);
        }
        
        // Create simplified employment verification
        EmploymentVerification verification = EmploymentVerification.builder()
            .jobApply(jobApply)
            .startDate(request.getStartDate())
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
        
        verification = employmentVerificationRepo.save(verification);
        
        log.info("Created employment verification for JobApply ID: {}", jobApplyId);
        
        return mapper.toResponse(verification);
    }
    
    @Override
    public EmploymentVerificationResponse getByJobApplyId(Integer jobApplyId) {
        EmploymentVerification verification = employmentVerificationRepo.findByJobApplyId(jobApplyId)
            .orElseThrow(() -> new AppException(ErrorCode.EMPLOYMENT_VERIFICATION_NOT_FOUND));
        
        return mapper.toResponse(verification);
    }
    
    @Override
    @Transactional
    public EmploymentVerificationResponse terminateEmployment(
        Integer jobApplyId,
        EmploymentTerminationRequest request,
        Integer recruiterId
    ) {
        // Get employment verification
        EmploymentVerification verification = employmentVerificationRepo.findByJobApplyId(jobApplyId)
            .orElseThrow(() -> new AppException(ErrorCode.EMPLOYMENT_VERIFICATION_NOT_FOUND));
        
        // Verify not already terminated
        if (!verification.getIsActive()) {
            throw new AppException(ErrorCode.EMPLOYMENT_ALREADY_TERMINATED);
        }
        
        // Simple termination: just set end date and mark inactive
        verification.setEndDate(LocalDate.now());
        verification.setIsActive(false);
        verification.setUpdatedAt(LocalDateTime.now());
        
        verification = employmentVerificationRepo.save(verification);
        
        log.info("Terminated employment for JobApply ID: {}", jobApplyId);
        
        return mapper.toResponse(verification);
    }
    
    @Override
    @Transactional
    public EmploymentVerificationResponse verifyEmployment(Integer jobApplyId, Integer recruiterId) {
        // This method is no longer needed in simplified flow - just return current status
        return getByJobApplyId(jobApplyId);
    }
    
    @Override
    public List<EmploymentVerificationResponse> getActiveEmploymentsByRecruiter(Integer recruiterId) {
        // Simplified: just return all active employments
        // (In a real implementation, you'd filter by recruiter through job posting relationship)
        return employmentVerificationRepo.findByIsActiveTrue().stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EmploymentVerificationResponse> getEmploymentsNeedingReminder() {
        // No longer needed in simplified flow - return empty list
        return List.of();
    }
}
