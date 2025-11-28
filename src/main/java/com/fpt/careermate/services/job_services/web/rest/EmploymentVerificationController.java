package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.job_services.service.EmploymentVerificationService;
import com.fpt.careermate.services.job_services.service.dto.request.EmploymentTerminationRequest;
import com.fpt.careermate.services.job_services.service.dto.request.EmploymentVerificationRequest;
import com.fpt.careermate.services.job_services.service.dto.response.EmploymentVerificationResponse;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for employment verification management.
 * Handles employment tracking for review eligibility.
 */
@RestController
@RequestMapping("/api/employment-verifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Employment Verification", description = "APIs for employment verification and tracking")
public class EmploymentVerificationController {
    
    EmploymentVerificationService employmentVerificationService;
    AuthenticationImp authenticationImp;
    RecruiterRepo recruiterRepo;
    
    /**
     * Get the current recruiter ID from the authenticated user
     */
    private Integer getCurrentRecruiterId() {
        Account currentAccount = authenticationImp.findByEmail();
        Recruiter recruiter = recruiterRepo.findByAccount_Id(currentAccount.getId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return recruiter.getId();
    }
    
    @PostMapping("/job-apply/{jobApplyId}")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Create employment verification", 
               description = "Create employment verification when candidate is hired")
    public ApiResponse<EmploymentVerificationResponse> createEmploymentVerification(
        @PathVariable Integer jobApplyId,
        @Valid @RequestBody EmploymentVerificationRequest request
    ) {
        Integer recruiterId = getCurrentRecruiterId();
        log.info("Creating employment verification for JobApply ID: {}", jobApplyId);
        
        EmploymentVerificationResponse response = employmentVerificationService
            .createEmploymentVerification(jobApplyId, request, recruiterId);
        
        return ApiResponse.<EmploymentVerificationResponse>builder()
            .code(201)
            .message("Employment verification created successfully")
            .result(response)
            .build();
    }
    
    @GetMapping("/job-apply/{jobApplyId}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'CANDIDATE', 'ADMIN')")
    @Operation(summary = "Get employment verification", 
               description = "Get employment verification details by job apply ID")
    public ApiResponse<EmploymentVerificationResponse> getEmploymentVerification(
        @PathVariable Integer jobApplyId
    ) {
        log.info("Getting employment verification for JobApply ID: {}", jobApplyId);
        
        EmploymentVerificationResponse response = employmentVerificationService
            .getByJobApplyId(jobApplyId);
        
        return ApiResponse.<EmploymentVerificationResponse>builder()
            .code(200)
            .result(response)
            .build();
    }
    
    @PostMapping("/job-apply/{jobApplyId}/terminate")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Terminate employment", 
               description = "Update employment termination details")
    public ApiResponse<EmploymentVerificationResponse> terminateEmployment(
        @PathVariable Integer jobApplyId,
        @Valid @RequestBody EmploymentTerminationRequest request
    ) {
        Integer recruiterId = getCurrentRecruiterId();
        log.info("Terminating employment for JobApply ID: {}", jobApplyId);
        
        EmploymentVerificationResponse response = employmentVerificationService
            .terminateEmployment(jobApplyId, request, recruiterId);
        
        return ApiResponse.<EmploymentVerificationResponse>builder()
            .code(200)
            .message("Employment terminated successfully")
            .result(response)
            .build();
    }
    
    @PostMapping("/job-apply/{jobApplyId}/verify")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Verify employment status", 
               description = "Recruiter confirms employment is still active")
    public ApiResponse<EmploymentVerificationResponse> verifyEmployment(
        @PathVariable Integer jobApplyId
    ) {
        Integer recruiterId = getCurrentRecruiterId();
        log.info("Verifying employment for JobApply ID: {}", jobApplyId);
        
        EmploymentVerificationResponse response = employmentVerificationService
            .verifyEmployment(jobApplyId, recruiterId);
        
        return ApiResponse.<EmploymentVerificationResponse>builder()
            .code(200)
            .message("Employment verified successfully")
            .result(response)
            .build();
    }
    
    @GetMapping("/recruiter/active")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get active employments", 
               description = "Get all active employments for the logged-in recruiter")
    public ApiResponse<List<EmploymentVerificationResponse>> getActiveEmployments() {
        Integer recruiterId = getCurrentRecruiterId();
        log.info("Getting active employments for Recruiter ID: {}", recruiterId);
        
        List<EmploymentVerificationResponse> response = employmentVerificationService
            .getActiveEmploymentsByRecruiter(recruiterId);
        
        return ApiResponse.<List<EmploymentVerificationResponse>>builder()
            .code(200)
            .result(response)
            .build();
    }
    
    @GetMapping("/admin/needing-reminder")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get employments needing reminder", 
               description = "Get employments that need verification reminder (30 days)")
    public ApiResponse<List<EmploymentVerificationResponse>> getEmploymentsNeedingReminder() {
        log.info("Getting employments needing reminder");
        
        List<EmploymentVerificationResponse> response = employmentVerificationService
            .getEmploymentsNeedingReminder();
        
        return ApiResponse.<List<EmploymentVerificationResponse>>builder()
            .code(200)
            .result(response)
            .build();
    }
}
