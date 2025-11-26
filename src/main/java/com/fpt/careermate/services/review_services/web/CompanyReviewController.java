package com.fpt.careermate.services.review_services.web;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import com.fpt.careermate.services.review_services.service.dto.request.CompanyReviewRequest;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewResponse;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewStatsResponse;
import com.fpt.careermate.services.review_services.service.dto.response.ReviewEligibilityResponse;
import com.fpt.careermate.services.review_services.service.CompanyReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for company review operations
 * Handles review submission, retrieval, and moderation
 * 
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company Reviews", description = "Endpoints for managing company reviews")
@SecurityRequirement(name = "bearerAuth")
public class CompanyReviewController {

    private final CompanyReviewService companyReviewService;

    /**
     * Submit a new company review
     * Only candidates who have applied can submit reviews
     */
    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
        summary = "Submit company review",
        description = "Submit a review for a company based on application, interview, or work experience"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Review submitted successfully",
            content = @Content(schema = @Schema(implementation = CompanyReviewResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request or already submitted review"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Not authorized to review this company"
        )
    })
    public ResponseEntity<ApiResponse<CompanyReviewResponse>> submitReview(
            @Valid @RequestBody CompanyReviewRequest request,
            @Parameter(description = "Candidate ID from authentication context")
            @RequestParam Integer candidateId) {
        
        log.info("POST /api/v1/reviews - Candidate {} submitting review", candidateId);
        CompanyReviewResponse response = companyReviewService.submitReview(request, candidateId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CompanyReviewResponse>builder()
                        .code(HttpStatus.CREATED.value())
                        .message("Review submitted successfully")
                        .result(response)
                        .build());
    }

    /**
     * Check if candidate is eligible to review a company
     */
    @GetMapping("/eligibility")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
        summary = "Check review eligibility",
        description = "Check if a candidate is eligible to review a company for a specific job application"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Eligibility checked successfully",
            content = @Content(schema = @Schema(implementation = ReviewEligibilityResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Job application not found"
        )
    })
    public ResponseEntity<ApiResponse<ReviewEligibilityResponse>> checkEligibility(
            @Parameter(description = "Candidate ID from authentication context")
            @RequestParam Integer candidateId,
            @Parameter(description = "Job application ID to check eligibility for")
            @RequestParam Integer jobApplyId) {
        
        log.debug("GET /api/v1/reviews/eligibility - Candidate {} checking for job apply {}", 
            candidateId, jobApplyId);
        ReviewEligibilityResponse response = companyReviewService.checkEligibility(candidateId, jobApplyId);
        return ResponseEntity.ok(ApiResponse.<ReviewEligibilityResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Eligibility checked successfully")
                .result(response)
                .build());
    }

    /**
     * Get all reviews for a company (paginated)
     */
    @GetMapping("/company/{recruiterId}")
    @Operation(
        summary = "Get company reviews",
        description = "Get all reviews for a company with optional filtering by review type"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Reviews retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        )
    })
    public ResponseEntity<ApiResponse<Page<CompanyReviewResponse>>> getCompanyReviews(
            @Parameter(description = "Recruiter/Company ID")
            @PathVariable Integer recruiterId,
            @Parameter(description = "Filter by review type (optional)")
            @RequestParam(required = false) ReviewType reviewType,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {
        
        log.debug("GET /api/v1/reviews/company/{} - Page {} Size {} Type {}", 
            recruiterId, page, size, reviewType);
        Page<CompanyReviewResponse> reviews = companyReviewService.getCompanyReviews(
            recruiterId, reviewType, page, size);
        return ResponseEntity.ok(ApiResponse.<Page<CompanyReviewResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Reviews retrieved successfully")
                .result(reviews)
                .build());
    }

    /**
     * Get candidate's own reviews
     */
    @GetMapping("/my-reviews")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(
        summary = "Get my reviews",
        description = "Get all reviews submitted by the authenticated candidate"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Reviews retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        )
    })
    public ResponseEntity<ApiResponse<Page<CompanyReviewResponse>>> getMyReviews(
            @Parameter(description = "Candidate ID from authentication context")
            @RequestParam Integer candidateId,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {
        
        log.debug("GET /api/v1/reviews/my-reviews - Candidate {} Page {} Size {}", 
            candidateId, page, size);
        Page<CompanyReviewResponse> reviews = companyReviewService.getCandidateReviews(
            candidateId, page, size);
        return ResponseEntity.ok(ApiResponse.<Page<CompanyReviewResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Your reviews retrieved successfully")
                .result(reviews)
                .build());
    }

    /**
     * Get average rating for a company
     */
    @GetMapping("/company/{recruiterId}/rating")
    @Operation(
        summary = "Get company average rating",
        description = "Get the average overall rating for a company across all reviews"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Average rating calculated successfully"
        )
    })
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            @Parameter(description = "Recruiter/Company ID")
            @PathVariable Integer recruiterId) {
        
        log.debug("GET /api/v1/reviews/company/{}/rating", recruiterId);
        Double avgRating = companyReviewService.getAverageRating(recruiterId);
        return ResponseEntity.ok(ApiResponse.<Double>builder()
                .code(HttpStatus.OK.value())
                .message("Average rating calculated successfully")
                .result(avgRating)
                .build());
    }

    /**
     * Get comprehensive statistics for a company
     */
    @GetMapping("/company/{recruiterId}/statistics")
    @Operation(
        summary = "Get company review statistics",
        description = "Get comprehensive review statistics including rating distribution and aspect averages"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Statistics calculated successfully",
            content = @Content(schema = @Schema(implementation = CompanyReviewStatsResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<CompanyReviewStatsResponse>> getCompanyStatistics(
            @Parameter(description = "Recruiter/Company ID")
            @PathVariable Integer recruiterId) {
        
        log.debug("GET /api/v1/reviews/company/{}/statistics", recruiterId);
        CompanyReviewStatsResponse stats = companyReviewService.getCompanyStatistics(recruiterId);
        return ResponseEntity.ok(ApiResponse.<CompanyReviewStatsResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Statistics calculated successfully")
                .result(stats)
                .build());
    }

    /**
     * Flag a review for moderation
     */
    @PostMapping("/{reviewId}/flag")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
    @Operation(
        summary = "Flag review",
        description = "Flag a review for inappropriate content or policy violations"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Review flagged successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Review not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> flagReview(
            @Parameter(description = "Review ID to flag")
            @PathVariable Integer reviewId,
            @Parameter(description = "Reporter ID from authentication context")
            @RequestParam Integer reporterId,
            @Parameter(description = "Reason for flagging")
            @RequestParam String reason) {
        
        log.info("POST /api/v1/reviews/{}/flag - Reporter {} Reason: {}", 
            reviewId, reporterId, reason);
        companyReviewService.flagReview(reviewId, reporterId, reason);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Review flagged successfully")
                .build());
    }

    /**
     * Remove a review (admin/moderator only)
     */
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Remove review",
        description = "Remove a review from public view (admin/moderator only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Review removed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Review not found"
        )
    })
    public ResponseEntity<ApiResponse<Void>> removeReview(
            @Parameter(description = "Review ID to remove")
            @PathVariable Integer reviewId,
            @Parameter(description = "Reason for removal")
            @RequestParam String reason) {
        
        log.info("DELETE /api/v1/reviews/{} - Reason: {}", reviewId, reason);
        companyReviewService.removeReview(reviewId, reason);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Review removed successfully")
                .build());
    }

    /**
     * Get a single review by ID
     */
    @GetMapping("/{reviewId}")
    @Operation(
        summary = "Get review by ID",
        description = "Get detailed information about a specific review"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Review retrieved successfully",
            content = @Content(schema = @Schema(implementation = CompanyReviewResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Review not found"
        )
    })
    public ResponseEntity<ApiResponse<CompanyReviewResponse>> getReviewById(
            @Parameter(description = "Review ID")
            @PathVariable Integer reviewId) {
        
        log.debug("GET /api/v1/reviews/{}", reviewId);
        CompanyReviewResponse review = companyReviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ApiResponse.<CompanyReviewResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Review retrieved successfully")
                .result(review)
                .build());
    }
}
