package com.fpt.careermate.services.admin_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.recruiter_services.service.RecruiterImp;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterUpdateRequestResponse;
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

@Validated
@RestController
@RequestMapping("/api/admin/recruiter-update-requests")
@Tag(name = "Admin - Recruiter Profile Update Requests", description = "Admin APIs for managing recruiter profile update requests")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecruiterUpdateRequestController {

    RecruiterImp recruiterImp;

    @GetMapping
    @Operation(summary = "Get all profile update requests with pagination",
               description = "Admin can view all profile update requests with status filter and pagination")
    public ApiResponse<PageResponse<RecruiterUpdateRequestResponse>> getAllUpdateRequests(
            @Parameter(description = "Status filter (PENDING, APPROVED, REJECTED, or empty for all)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Admin fetching update requests: status={}, page={}, size={}", status, page, size);
        return ApiResponse.<PageResponse<RecruiterUpdateRequestResponse>>builder()
                .result(recruiterImp.getAllUpdateRequests(status, page, size, sortBy, sortDir))
                .code(200)
                .message("Success")
                .build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search profile update requests",
               description = "Admin can search update requests by company name, email, or username")
    public ApiResponse<PageResponse<RecruiterUpdateRequestResponse>> searchUpdateRequests(
            @Parameter(description = "Search keyword")
            @RequestParam(required = false) String search,
            @Parameter(description = "Status filter (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("Admin searching update requests: search='{}', status={}", search, status);
        return ApiResponse.<PageResponse<RecruiterUpdateRequestResponse>>builder()
                .result(recruiterImp.searchUpdateRequests(status, search, page, size, sortBy, sortDir))
                .code(200)
                .message("Success")
                .build();
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Get update request details",
               description = "Admin can view detailed information of a specific update request")
    public ApiResponse<RecruiterUpdateRequestResponse> getUpdateRequestById(
            @Parameter(description = "Update request ID")
            @PathVariable int requestId) {

        log.info("Admin fetching update request details for ID: {}", requestId);
        return ApiResponse.<RecruiterUpdateRequestResponse>builder()
                .result(recruiterImp.getUpdateRequestById(requestId))
                .code(200)
                .message("Success")
                .build();
    }

    @PutMapping("/{requestId}/approve")
    @Operation(summary = "Approve profile update request",
               description = "Admin approves the update request and applies changes to recruiter profile. " +
                           "Notification will be sent to the recruiter.")
    public ApiResponse<Void> approveUpdateRequest(
            @Parameter(description = "Update request ID")
            @PathVariable int requestId,
            @Parameter(description = "Optional admin note")
            @RequestParam(required = false) String adminNote) {

        log.info("Admin approving update request ID: {}", requestId);
        recruiterImp.approveUpdateRequest(requestId, adminNote);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Profile update request approved successfully. Changes have been applied to recruiter profile.")
                .build();
    }

    @PutMapping("/{requestId}/reject")
    @Operation(summary = "Reject profile update request",
               description = "Admin rejects the update request with a reason. " +
                           "Notification will be sent to the recruiter.")
    public ApiResponse<Void> rejectUpdateRequest(
            @Parameter(description = "Update request ID")
            @PathVariable int requestId,
            @Parameter(description = "Rejection reason")
            @RequestParam String rejectionReason) {

        log.info("Admin rejecting update request ID: {}, Reason: {}", requestId, rejectionReason);
        recruiterImp.rejectUpdateRequest(requestId, rejectionReason);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Profile update request rejected. Notification sent to recruiter.")
                .build();
    }
}

