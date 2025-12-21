package com.fpt.careermate.services.review_services.controller;

import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.service.AdminReviewService;
import com.fpt.careermate.services.review_services.service.dto.request.AdminBulkReviewActionRequest;
import com.fpt.careermate.services.review_services.service.dto.request.AdminReviewFilterRequest;
import com.fpt.careermate.services.review_services.service.dto.response.AdminReviewResponse;
import com.fpt.careermate.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin endpoints for review management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "Admin Review Management", description = "Admin APIs for managing company reviews")
@SecurityRequirement(name = "bearerAuth")
public class AdminReviewController {

        private final AdminReviewService adminReviewService;

        @PostMapping("/search")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Search and filter reviews (Admin only)")
        public ApiResponse<Page<AdminReviewResponse>> searchReviews(
                        @RequestBody AdminReviewFilterRequest request) {
                log.info("Admin searching reviews with filters: {}", request);
                Page<AdminReviewResponse> reviews = adminReviewService.searchReviews(request);
                return ApiResponse.<Page<AdminReviewResponse>>builder()
                                .result(reviews)
                                .build();
        }

        @PutMapping("/{reviewId}/status")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Update review status (Admin only)")
        public ApiResponse<AdminReviewResponse> updateReviewStatus(
                        @PathVariable Integer reviewId,
                        @RequestParam ReviewStatus newStatus,
                        @RequestParam(required = false) String reason) {
                log.info("Admin updating review {} to status: {}", reviewId, newStatus);
                AdminReviewResponse review = adminReviewService.updateReviewStatus(reviewId, newStatus, reason);
                return ApiResponse.<AdminReviewResponse>builder()
                                .result(review)
                                .build();
        }

        @PostMapping("/bulk-action")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Bulk update review statuses (Admin only)")
        public ApiResponse<String> bulkUpdateReviews(
                        @Valid @RequestBody AdminBulkReviewActionRequest request) {
                log.info("Admin bulk updating {} reviews to status: {}",
                                request.getReviewIds().size(), request.getNewStatus());
                int updated = adminReviewService.bulkUpdateReviewStatus(request);
                return ApiResponse.<String>builder()
                                .result(String.format("Successfully updated %d reviews", updated))
                                .build();
        }

        @GetMapping("/stats")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get review statistics (Admin only)")
        public ApiResponse<Object> getReviewStats() {
                log.info("Admin fetching review statistics");
                Object stats = adminReviewService.getReviewStatistics();
                return ApiResponse.builder()
                                .result(stats)
                                .build();
        }
}
