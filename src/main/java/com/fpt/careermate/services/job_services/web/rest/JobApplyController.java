package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.job_services.service.JobApplyImp;
import com.fpt.careermate.services.job_services.service.dto.request.JobApplyRequest;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.job_services.service.dto.response.JobApplyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-apply")
@Tag(name = "Job Application", description = "Manage Job Applications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobApplyController {

        JobApplyImp jobApplyImp;

        @PostMapping
        @Operation(summary = "Create Job Application", description = "Create a new job application")
        public ApiResponse<JobApplyResponse> createJobApply(@RequestBody @Valid JobApplyRequest request) {
                return ApiResponse.<JobApplyResponse>builder()
                                .result(jobApplyImp.createJobApply(request))
                                .message("Job application created successfully")
                                .build();
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get Job Application by ID", description = "Retrieve a specific job application by ID")
        public ApiResponse<JobApplyResponse> getJobApplyById(@PathVariable int id) {
                return ApiResponse.<JobApplyResponse>builder()
                                .result(jobApplyImp.getJobApplyById(id))
                                .message("Job application retrieved successfully")
                                .build();
        }

        @GetMapping
        @Operation(summary = "Get All Job Applications", description = "Retrieve all job applications")
        public ApiResponse<List<JobApplyResponse>> getAllJobApplies() {
                return ApiResponse.<List<JobApplyResponse>>builder()
                                .result(jobApplyImp.getAllJobApplies())
                                .message("Job applications retrieved successfully")
                                .build();
        }

        @GetMapping("/job-posting/{jobPostingId}")
        @Operation(summary = "Get Job Applications by Job Posting", description = "Retrieve all applications for a specific job posting")
        public ApiResponse<List<JobApplyResponse>> getJobAppliesByJobPosting(@PathVariable int jobPostingId) {
                return ApiResponse.<List<JobApplyResponse>>builder()
                                .result(jobApplyImp.getJobAppliesByJobPosting(jobPostingId))
                                .message("Job applications for job posting retrieved successfully")
                                .build();
        }

        @GetMapping("/candidate/{candidateId}")
        @Operation(summary = "Get Job Applications by Candidate", description = "Retrieve all applications submitted by a specific candidate")
        public ApiResponse<List<JobApplyResponse>> getJobAppliesByCandidate(@PathVariable int candidateId) {
                return ApiResponse.<List<JobApplyResponse>>builder()
                                .result(jobApplyImp.getJobAppliesByCandidate(candidateId))
                                .message("Job applications for candidate retrieved successfully")
                                .build();
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update Job Application Status", description = "Update job application status (SUBMITTED, REVIEWING, APPROVED, REJECTED)")
        public ApiResponse<JobApplyResponse> updateJobApply(
                        @PathVariable int id,
                        @RequestBody @Valid StatusJobApply status) {
                return ApiResponse.<JobApplyResponse>builder()
                                .result(jobApplyImp.updateJobApply(id, status))
                                .message("Job application status updated successfully")
                                .build();
        }

        @GetMapping("/candidate/{candidateId}/filter")
        @Operation(summary = "Get Job Applications by Candidate with Filter and Pagination", description = """
                        Retrieve job applications for a specific candidate with optional status filter and pagination.

                        Parameters:
                        - candidateId: ID of the candidate (required, path variable)
                        - status: Filter by application status (optional). Valid values: SUBMITTED, REVIEWING, APPROVED, REJECTED
                        - page: Page number, starts from 0 (default: 0)
                        - size: Number of items per page (default: 10)

                        The results are sorted by creation date (newest first).

                        Examples:
                        - GET /api/job-apply/candidate/1/filter?page=0&size=10 (get all applications)
                        - GET /api/job-apply/candidate/1/filter?status=SUBMITTED&page=0&size=10 (filter by SUBMITTED)
                        - GET /api/job-apply/candidate/1/filter?status=REVIEWING&page=0&size=10 (filter by REVIEWING)
                        """)
        public ApiResponse<PageResponse<JobApplyResponse>> getJobAppliesByCandidateWithFilter(
                        @PathVariable int candidateId,
                        @RequestParam(required = false) StatusJobApply status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {

                return ApiResponse.<PageResponse<JobApplyResponse>>builder()
                                .result(jobApplyImp.getJobAppliesByCandidateWithFilter(candidateId, status, page, size))
                                .message("Job applications retrieved successfully")
                                .build();
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete Job Application", description = "Delete a job application by ID")
        public ApiResponse<Void> deleteJobApply(@PathVariable int id) {
                jobApplyImp.deleteJobApply(id);
                return ApiResponse.<Void>builder()
                                .message("Job application deleted successfully")
                                .build();
        }

        @GetMapping("/recruiter")
        @Operation(summary = "Get Job Applications for Recruiter", 
                   description = "Retrieve all job applications for all job postings belonging to the current recruiter")
        public ApiResponse<List<JobApplyResponse>> getJobAppliesByRecruiter() {
                return ApiResponse.<List<JobApplyResponse>>builder()
                                .result(jobApplyImp.getJobAppliesByRecruiter())
                                .message("Job applications retrieved successfully")
                                .build();
        }

        @GetMapping("/recruiter/filter")
        @Operation(summary = "Get Job Applications for Recruiter with Filter", 
                   description = """
                        Retrieve job applications for the current recruiter with optional status filter and pagination.
                        
                        Parameters:
                        - status: Filter by application status (optional). Valid values: SUBMITTED, REVIEWING, INTERVIEW_SCHEDULED, INTERVIEWED, APPROVED, OFFER_EXTENDED, WORKING, REJECTED, etc.
                        - page: Page number, starts from 0 (default: 0)
                        - size: Number of items per page (default: 10)
                        
                        The results are sorted by creation date (newest first).
                        """)
        public ApiResponse<PageResponse<JobApplyResponse>> getJobAppliesByRecruiterWithFilter(
                        @RequestParam(required = false) StatusJobApply status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size) {
                return ApiResponse.<PageResponse<JobApplyResponse>>builder()
                                .result(jobApplyImp.getJobAppliesByRecruiterWithFilter(status, page, size))
                                .message("Job applications retrieved successfully")
                                .build();
        }

        // ==================== CANDIDATE OFFER CONFIRMATION ENDPOINTS (v3.1) ====================

        @PostMapping("/{id}/confirm-offer")
        @Operation(summary = "Confirm Job Offer (Candidate)", 
                   description = """
                        Candidate confirms a job offer. This transitions the application from OFFER_EXTENDED to WORKING.
                        
                        Prerequisites:
                        - Application must be in OFFER_EXTENDED status
                        - Must be called by the candidate who owns the application
                        - Candidate must not already be employed elsewhere (ACCEPTED/WORKING status)
                        
                        Effects:
                        - Application status changes to WORKING
                        - All other pending applications are automatically withdrawn
                        - Recruiter is notified of the acceptance
                        """)
        public ApiResponse<JobApplyResponse> confirmOffer(@PathVariable int id) {
                return ApiResponse.<JobApplyResponse>builder()
                                .result(jobApplyImp.confirmOffer(id))
                                .message("Job offer confirmed successfully! You are now employed.")
                                .build();
        }

        @PostMapping("/{id}/decline-offer")
        @Operation(summary = "Decline Job Offer (Candidate)", 
                   description = """
                        Candidate declines a job offer. This transitions the application from OFFER_EXTENDED to WITHDRAWN.
                        
                        Prerequisites:
                        - Application must be in OFFER_EXTENDED status
                        - Must be called by the candidate who owns the application
                        
                        Effects:
                        - Application status changes to WITHDRAWN
                        - Recruiter is notified of the decline
                        """)
        public ApiResponse<JobApplyResponse> declineOffer(@PathVariable int id) {
                return ApiResponse.<JobApplyResponse>builder()
                                .result(jobApplyImp.declineOffer(id))
                                .message("Job offer declined. Application withdrawn.")
                                .build();
        }

        @PostMapping("/{id}/terminate")
        @Operation(summary = "Terminate Employment (Candidate)",
                   description = "Candidate ends current employment. Transitions WORKING/ACCEPTED to TERMINATED and closes employment verification.")
        public ApiResponse<JobApplyResponse> terminateEmployment(@PathVariable int id) {
                return ApiResponse.<JobApplyResponse>builder()
                                .result(jobApplyImp.terminateEmployment(id))
                                .message("Employment terminated successfully.")
                                .build();
        }
}
