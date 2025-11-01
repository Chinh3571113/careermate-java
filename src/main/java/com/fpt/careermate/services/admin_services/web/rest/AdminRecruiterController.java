package com.fpt.careermate.services.admin_services.web.rest;

import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.authentication_services.service.RegistrationService;
import com.fpt.careermate.services.recruiter_services.service.RecruiterImp;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterApprovalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/recruiters")
@Tag(name = "Admin - Recruiter Management", description = "Admin APIs for managing and approving recruiter profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecruiterController {

    RecruiterImp recruiterImp;
    RegistrationService registrationService;

    // ========== NEW PAGINATED & SEARCHABLE ENDPOINTS ==========

    @GetMapping("/filter")
    @Operation(summary = "Get recruiters by status with pagination",
               description = "Admin can filter recruiters by status (PENDING, ACTIVE, BANNED) with pagination and sorting. " +
                           "Leave status empty to get all recruiters.")
    public ApiResponse<PageResponse<RecruiterApprovalResponse>> getRecruitersByStatus(
            @Parameter(description = "Account status filter (PENDING, ACTIVE, BANNED, or empty for all)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (e.g., companyName, createdAt)")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Admin filtering recruiters: status={}, page={}, size={}", status, page, size);
        return ApiResponse.<PageResponse<RecruiterApprovalResponse>>builder()
                .result(recruiterImp.getRecruitersByStatus(status, page, size, sortBy, sortDir))
                .code(200)
                .message("Success")
                .build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search recruiters with filters",
               description = "Admin can search recruiters by company name, email, or username with status filter and pagination. " +
                           "Supports filtering by status (PENDING, ACTIVE, BANNED) and text search.")
    public ApiResponse<PageResponse<RecruiterApprovalResponse>> searchRecruiters(
            @Parameter(description = "Search keyword (searches in company name, email, username)")
            @RequestParam(required = false) String search,
            @Parameter(description = "Account status filter (PENDING, ACTIVE, BANNED, or empty for all)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (e.g., companyName, createdAt)")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Admin searching recruiters: search='{}', status={}, page={}, size={}", search, status, page, size);
        return ApiResponse.<PageResponse<RecruiterApprovalResponse>>builder()
                .result(recruiterImp.searchRecruiters(status, search, page, size, sortBy, sortDir))
                .code(200)
                .message("Success")
                .build();
    }

    @GetMapping("/{recruiterId}")
    @Operation(summary = "Get recruiter details by ID",
               description = "Admin can view detailed information of a specific recruiter including organization info")
    public ApiResponse<RecruiterApprovalResponse> getRecruiterById(
            @Parameter(description = "Recruiter ID")
            @PathVariable int recruiterId) {

        log.info("Admin fetching recruiter details for ID: {}", recruiterId);
        return ApiResponse.<RecruiterApprovalResponse>builder()
                .result(recruiterImp.getRecruiterById(recruiterId))
                .code(200)
                .message("Success")
                .build();
    }

    // ========== LEGACY ENDPOINTS (kept for backward compatibility) ==========

    @GetMapping("/pending")
    @Operation(summary = "[Legacy] Get all pending recruiter accounts",
               description = "Admin can view all recruiter accounts with PENDING status waiting for approval. " +
                           "Consider using /filter endpoint with status=PENDING for pagination support.")
    public ApiResponse<List<RecruiterApprovalResponse>> getPendingRecruiters() {
        log.info("Admin fetching pending recruiter accounts");
        return ApiResponse.<List<RecruiterApprovalResponse>>builder()
                .result(recruiterImp.getPendingRecruiters())
                .code(200)
                .message("Success")
                .build();
    }

    @GetMapping
    @Operation(summary = "[Legacy] Get all approved recruiters",
               description = "Admin can view all approved recruiters (accounts with RECRUITER role). " +
                           "Consider using /filter endpoint with status=ACTIVE for pagination support.")
    public ApiResponse<List<RecruiterApprovalResponse>> getAllRecruiters() {
        log.info("Admin fetching all approved recruiters");
        return ApiResponse.<List<RecruiterApprovalResponse>>builder()
                .result(recruiterImp.getAllRecruiters())
                .code(200)
                .message("Success")
                .build();
    }

    // ========== RECRUITER MANAGEMENT ACTIONS ==========

    @PutMapping("/{recruiterId}/approve")
    @Operation(summary = "Approve recruiter account",
               description = "Admin approves recruiter account and changes status from PENDING to ACTIVE (role remains RECRUITER)")
    public ApiResponse<Void> approveRecruiter(@PathVariable int recruiterId) {
        log.info("Admin approving recruiter account with ID: {}", recruiterId);
        registrationService.approveRecruiterAccount(recruiterId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Recruiter account approved successfully. Account status changed from PENDING to ACTIVE.")
                .build();
    }

    @PutMapping("/{recruiterId}/reject")
    @Operation(summary = "Reject recruiter account",
               description = "Admin rejects and deletes both recruiter profile and account permanently")
    public ApiResponse<Void> rejectRecruiter(@PathVariable int recruiterId,
                                             @RequestParam(required = false) String reason) {
        log.info("Admin rejecting recruiter account with ID: {}, Reason: {}", recruiterId, reason);
        registrationService.rejectRecruiterAccount(recruiterId, reason);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Recruiter account rejected and deleted. Reason: " + (reason != null ? reason : "Not specified"))
                .build();
    }

    @PutMapping("/account/{accountId}/ban")
    @Operation(summary = "Ban recruiter account",
               description = "Admin bans a recruiter account, preventing them from logging in or accessing the system")
    public ApiResponse<Void> banRecruiterAccount(@PathVariable int accountId,
                                                  @RequestParam(required = false) String reason) {
        log.info("Admin banning account with ID: {}, Reason: {}", accountId, reason);
        registrationService.banRecruiterAccount(accountId, reason);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Account banned successfully. Reason: " + (reason != null ? reason : "Not specified"))
                .build();
    }

    @PutMapping("/account/{accountId}/unban")
    @Operation(summary = "Unban recruiter account",
               description = "Admin unbans a previously banned recruiter account, allowing them to access the system again")
    public ApiResponse<Void> unbanRecruiterAccount(@PathVariable int accountId) {
        log.info("Admin unbanning account with ID: {}", accountId);
        registrationService.unbanRecruiterAccount(accountId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Account unbanned successfully")
                .build();
    }
}

