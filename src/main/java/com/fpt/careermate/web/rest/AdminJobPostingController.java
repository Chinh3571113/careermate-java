package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.JobPostingImp;
import com.fpt.careermate.services.dto.request.JobPostingApprovalRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.JobPostingForAdminResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin/jobpostings")
@Tag(name = "Admin - Job Posting Management", description = "Admin APIs for managing and approving job postings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminJobPostingController {

    JobPostingImp jobPostingImp;

    @GetMapping
    @Operation(summary = "Get all job postings with pagination and filtering", description = "Admin can view all job postings with pagination, filtering by status, and sorting")
    public ApiResponse<Page<JobPostingForAdminResponse>> getAllJobPostings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        log.info("Admin fetching job postings - Page: {}, Size: {}, Status: {}", page, size, status);

        Page<JobPostingForAdminResponse> jobPostings = jobPostingImp.getAllJobPostingsForAdmin(
                page, size, status, sortBy, sortDirection);

        return ApiResponse.<Page<JobPostingForAdminResponse>>builder()
                .result(jobPostings)
                .code(200)
                .message("Success")
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job posting detail", description = "Admin can view detailed information of a specific job posting")
    public ApiResponse<JobPostingForAdminResponse> getJobPostingDetail(@PathVariable int id) {
        log.info("Admin fetching job posting detail for ID: {}", id);

        JobPostingForAdminResponse jobPosting = jobPostingImp.getJobPostingDetailForAdmin(id);

        return ApiResponse.<JobPostingForAdminResponse>builder()
                .result(jobPosting)
                .code(200)
                .message("Success")
                .build();
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending job postings", description = "Admin can view all job postings that are waiting for approval")
    public ApiResponse<List<JobPostingForAdminResponse>> getPendingJobPostings() {
        log.info("Admin fetching all pending job postings");

        List<JobPostingForAdminResponse> pendingJobs = jobPostingImp.getPendingJobPostings();

        return ApiResponse.<List<JobPostingForAdminResponse>>builder()
                .result(pendingJobs)
                .code(200)
                .message("Success")
                .build();
    }

    @PutMapping("/{id}/approval")
    @Operation(summary = "Approve or reject job posting", description = "Admin can approve or reject a PENDING job posting. "
            +
            "When approving, status changes to ACTIVE. " +
            "When rejecting, a rejection reason must be provided.")
    public ApiResponse<String> approveOrRejectJobPosting(
            @PathVariable int id,
            @Valid @RequestBody JobPostingApprovalRequest request) {

        log.info("Admin processing approval/rejection for job posting ID: {}", id);

        jobPostingImp.approveOrRejectJobPosting(id, request);

        String message = request.getStatus().equalsIgnoreCase("APPROVED")
                ? "Job posting approved and activated successfully"
                : "Job posting rejected successfully";

        return ApiResponse.<String>builder()
                .code(200)
                .message(message)
                .build();
    }
}
