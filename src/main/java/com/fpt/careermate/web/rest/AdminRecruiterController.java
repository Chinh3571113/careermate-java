package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.RecruiterImp;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.RecruiterApprovalResponse;
import io.swagger.v3.oas.annotations.Operation;
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

    @GetMapping("/pending")
    @Operation(summary = "Get all pending recruiter profiles",
               description = "Admin can view all recruiter profiles that are waiting for approval (accounts with CANDIDATE role but have recruiter profiles)")
    public ApiResponse<List<RecruiterApprovalResponse>> getPendingRecruiters() {
        log.info("Admin fetching pending recruiter profiles");
        return ApiResponse.<List<RecruiterApprovalResponse>>builder()
                .result(recruiterImp.getPendingRecruiters())
                .code(200)
                .message("Success")
                .build();
    }

    @PutMapping("/{recruiterId}/approve")
    @Operation(summary = "Approve recruiter profile",
               description = "Admin approves a recruiter profile and changes the account role from CANDIDATE to RECRUITER")
    public ApiResponse<RecruiterApprovalResponse> approveRecruiter(@PathVariable int recruiterId) {
        log.info("Admin approving recruiter profile with ID: {}", recruiterId);
        return ApiResponse.<RecruiterApprovalResponse>builder()
                .result(recruiterImp.approveRecruiter(recruiterId))
                .code(200)
                .message("Recruiter profile approved successfully. Account role changed to RECRUITER.")
                .build();
    }

    @PutMapping("/{recruiterId}/reject")
    @Operation(summary = "Reject recruiter profile",
               description = "Admin rejects a recruiter profile and deletes it. Account remains as CANDIDATE.")
    public ApiResponse<Void> rejectRecruiter(@PathVariable int recruiterId,
                                             @RequestParam(required = false) String reason) {
        log.info("Admin rejecting recruiter profile with ID: {}, Reason: {}", recruiterId, reason);
        recruiterImp.rejectRecruiter(recruiterId, reason);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Recruiter profile rejected and deleted. Reason: " + (reason != null ? reason : "Not specified"))
                .build();
    }

    @GetMapping
    @Operation(summary = "Get all recruiters",
               description = "Admin can view all approved recruiters (accounts with RECRUITER role)")
    public ApiResponse<List<RecruiterApprovalResponse>> getAllRecruiters() {
        log.info("Admin fetching all approved recruiters");
        return ApiResponse.<List<RecruiterApprovalResponse>>builder()
                .result(recruiterImp.getAllRecruiters())
                .code(200)
                .message("Success")
                .build();
    }
}

