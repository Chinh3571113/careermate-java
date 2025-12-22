package com.fpt.careermate.services.blog_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.blog_services.service.BlogCommentImp;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogCommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin controller for content moderation and flagged comment management
 */
@RestController
@RequestMapping("/api/admin/comment-moderation")
@Tag(name = "Admin - Comment Moderation", description = "Admin endpoints for managing flagged and inappropriate comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminCommentModerationController {

        BlogCommentImp blogCommentImp;

        @GetMapping("/flagged")
        @Operation(summary = "Search/Filter Flagged Comments", description = "Search and filter flagged comments with optional filters: user email, blog ID, content, date range. "
                        +
                        "Supports flexible sorting by any field (flaggedAt, createdAt, etc.)")
        public ApiResponse<Page<BlogCommentResponse>> searchFlaggedComments(
                        @RequestParam(required = false) String userEmail,
                        @RequestParam(required = false) Long blogId,
                        @RequestParam(required = false) String content,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "flaggedAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDirection) {

                log.info("Admin searching flagged comments - userEmail: {}, blogId: {}, content: {}, startDate: {}, endDate: {}, page: {}, size: {}",
                                userEmail, blogId, content, startDate, endDate, page, size);

                Sort sort = sortDirection.equalsIgnoreCase("ASC")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<BlogCommentResponse>>builder()
                                .result(blogCommentImp.searchFlaggedComments(userEmail, blogId, content, startDate, endDate, pageable))
                                .message("Retrieved flagged comments")
                                .build();
        }

        @GetMapping("/flagged/all")
        @Operation(summary = "Get All Flagged Comments", description = "Retrieve all flagged comments including those already reviewed by admins")
        public ApiResponse<Page<BlogCommentResponse>> getAllFlaggedComments(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                log.info("Admin requesting all flagged comments - page: {}, size: {}", page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by("flaggedAt").descending());

                return ApiResponse.<Page<BlogCommentResponse>>builder()
                                .result(blogCommentImp.getAllFlaggedComments(pageable))
                                .message("Retrieved all flagged comments")
                                .build();
        }

        @PostMapping("/{commentId}/approve")
        @Operation(summary = "Approve Flagged Comment", description = "Mark flagged comment as reviewed and approved. "
                        +
                        "This will unflag the comment, make it visible, and mark as reviewed.")
        public ApiResponse<BlogCommentResponse> approveFlaggedComment(
                        @PathVariable Long commentId) {

                log.info("Admin approving flagged comment ID: {}", commentId);

                return ApiResponse.<BlogCommentResponse>builder()
                                .result(blogCommentImp.approveFlaggedComment(commentId))
                                .message("Comment approved and unflagged successfully")
                                .build();
        }

        @PostMapping("/{commentId}/reject")
        @Operation(summary = "Reject Flagged Comment", description = "Mark flagged comment as reviewed and rejected. " +
                        "This will hide the comment and mark as reviewed.")
        public ApiResponse<BlogCommentResponse> rejectFlaggedComment(
                        @PathVariable Long commentId) {

                log.info("Admin rejecting flagged comment ID: {}", commentId);

                return ApiResponse.<BlogCommentResponse>builder()
                                .result(blogCommentImp.rejectFlaggedComment(commentId))
                                .message("Comment rejected and hidden successfully")
                                .build();
        }

        @PostMapping("/{commentId}/unflag")
        @Operation(summary = "Manually Unflag Comment", description = "Remove flag from comment without hiding it. " +
                        "Use this when the flag is a false positive.")
        public ApiResponse<BlogCommentResponse> unflagComment(
                        @PathVariable Long commentId) {

                log.info("Admin manually unflagging comment ID: {}", commentId);

                return ApiResponse.<BlogCommentResponse>builder()
                                .result(blogCommentImp.unflagComment(commentId))
                                .message("Comment unflagged successfully")
                                .build();
        }

        @GetMapping("/moderation-statistics")
        @Operation(summary = "Get Moderation Statistics", description = "Retrieve detailed moderation statistics including automation rules and pending reviews")
        public ApiResponse<Object> getModerationStatistics() {
                log.info("Admin requesting moderation statistics");

                return ApiResponse.builder()
                                .result(blogCommentImp.getModerationStatistics())
                                .message("Retrieved moderation statistics")
                                .build();
        }

        @PostMapping("/{commentId}/analyze-toxicity")
        @Operation(summary = "Analyze Comment Toxicity", description = "Run semantic analysis to determine toxicity score for a flagged comment")
        public ApiResponse<Object> analyzeToxicity(@PathVariable Long commentId) {
                log.info("Admin analyzing toxicity for comment ID: {}", commentId);

                return ApiResponse.builder()
                                .result(blogCommentImp.analyzeCommentToxicity(commentId))
                                .message("Toxicity analysis complete")
                                .build();
        }

        @PostMapping("/batch-analyze")
        @Operation(summary = "Batch Analyze Toxicity", description = "Analyze toxicity for multiple flagged comments at once")
        public ApiResponse<Object> batchAnalyzeToxicity(@RequestBody List<Long> commentIds) {
                log.info("Admin batch analyzing {} comments", commentIds.size());

                return ApiResponse.builder()
                                .result(blogCommentImp.batchAnalyzeToxicity(commentIds))
                                .message("Batch analysis complete")
                                .build();
        }

        @PostMapping("/bulk-action")
        @Operation(summary = "Bulk Action on Comments", description = "Perform bulk action (hide/show/delete) on multiple comments based on toxicity scores")
        public ApiResponse<Object> bulkAction(
                        @RequestParam String action,
                        @RequestParam(required = false) Double minToxicity,
                        @RequestParam(required = false) String confidence,
                        @RequestBody List<Long> commentIds) {
                log.info("Admin performing bulk action: {} on {} comments with minToxicity: {}, confidence: {}", 
                        action, commentIds.size(), minToxicity, confidence);

                return ApiResponse.builder()
                                .result(blogCommentImp.bulkActionComments(action, commentIds, minToxicity, confidence))
                                .message("Bulk action completed")
                                .build();
        }
}
