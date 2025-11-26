package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.services.job_services.service.dto.request.EmploymentTerminationRequest;
import com.fpt.careermate.services.job_services.service.dto.request.EmploymentVerificationRequest;
import com.fpt.careermate.services.job_services.service.dto.response.EmploymentVerificationResponse;

import java.util.List;

/**
 * Service interface for employment verification management.
 */
public interface EmploymentVerificationService {
    
    /**
     * Create employment verification when candidate is hired
     */
    EmploymentVerificationResponse createEmploymentVerification(
        Integer jobApplyId,
        EmploymentVerificationRequest request,
        Integer recruiterId
    );
    
    /**
     * Get employment verification by job apply ID
     */
    EmploymentVerificationResponse getByJobApplyId(Integer jobApplyId);
    
    /**
     * Update employment termination details
     */
    EmploymentVerificationResponse terminateEmployment(
        Integer jobApplyId,
        EmploymentTerminationRequest request,
        Integer recruiterId
    );
    
    /**
     * Verify employment status (recruiter confirms still employed)
     */
    EmploymentVerificationResponse verifyEmployment(Integer jobApplyId, Integer recruiterId);
    
    /**
     * Get all active employments for a recruiter
     */
    List<EmploymentVerificationResponse> getActiveEmploymentsByRecruiter(Integer recruiterId);
    
    /**
     * Get employments needing verification reminder
     */
    List<EmploymentVerificationResponse> getEmploymentsNeedingReminder();
}
