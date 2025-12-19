package com.fpt.careermate.services.review_services.controller;

import com.fpt.careermate.services.review_services.service.RecruiterReviewService;
import com.fpt.careermate.services.review_services.service.dto.response.AdminReviewResponse;
import com.fpt.careermate.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Recruiter endpoints for viewing reviews (read-only)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/recruiter/reviews")
@RequiredArgsConstructor
@Tag(name = "Recruiter Review Viewing", description = "Recruiter APIs for viewing company reviews (read-only)")
@SecurityRequirement(name = "bearerAuth")
public class RecruiterReviewController {

    private final RecruiterReviewService recruiterReviewService;

    @GetMapping("/my-company")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get all reviews for recruiter's company (read-only)")
    public ApiResponse<Page<AdminReviewResponse>> getMyCompanyReviews(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String reviewType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer maxRating,
            @RequestParam(required = false) String searchText) {

        // JWT claims store numbers as Long, need to convert to Integer
        Object recruiterIdObj = jwt.getClaim("recruiterId");
        if (recruiterIdObj == null) {
            log.error("Recruiter ID not found in JWT claims");
            throw new RuntimeException(
                    "Recruiter ID not found in authentication token. Please ensure your recruiter profile is properly set up.");
        }

        Integer recruiterId = recruiterIdObj instanceof Long
                ? ((Long) recruiterIdObj).intValue()
                : (Integer) recruiterIdObj;

        log.info("Recruiter {} viewing their company reviews", recruiterId);

        Page<AdminReviewResponse> reviews = recruiterReviewService.getRecruiterCompanyReviews(
                recruiterId, page, size, reviewType, startDate, endDate, rating, maxRating, searchText);

        return ApiResponse.<Page<AdminReviewResponse>>builder()
                .result(reviews)
                .build();
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get review statistics for recruiter's company")
    public ApiResponse<Object> getMyCompanyStats(
            @AuthenticationPrincipal Jwt jwt) {
        // JWT claims store numbers as Long, need to convert to Integer
        Object recruiterIdObj = jwt.getClaim("recruiterId");
        if (recruiterIdObj == null) {
            log.error("Recruiter ID not found in JWT claims");
            throw new RuntimeException(
                    "Recruiter ID not found in authentication token. Please ensure your recruiter profile is properly set up.");
        }

        Integer recruiterId = recruiterIdObj instanceof Long
                ? ((Long) recruiterIdObj).intValue()
                : (Integer) recruiterIdObj;

        log.info("Recruiter {} fetching company review statistics", recruiterId);
        Object stats = recruiterReviewService.getRecruiterReviewStatistics(recruiterId);
        return ApiResponse.builder()
                .result(stats)
                .build();
    }
}
