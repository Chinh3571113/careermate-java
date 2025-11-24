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
        @Operation(summary = "Search/Filter Flagged Comments", description = "Search and filter flagged comments with optional filters: user email, blog ID. "
                        +
                        "Supports flexible sorting by any field (flaggedAt, createdAt, etc.)")
        public ApiResponse<Page<BlogCommentResponse>> searchFlaggedComments(
                        @RequestParam(required = false) String userEmail,
                        @RequestParam(required = false) Long blogId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "flaggedAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDirection) {

                log.info("Admin searching flagged comments - userEmail: {}, blogId: {}, page: {}, size: {}",
                                userEmail, blogId, page, size);

                Sort sort = sortDirection.equalsIgnoreCase("ASC")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<BlogCommentResponse>>builder()
                                .result(blogCommentImp.searchFlaggedComments(userEmail, blogId, pageable))
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
}
