package com.fpt.careermate.services.admin_services.web.rest;

import com.fpt.careermate.services.blog_services.service.BlogCommentImp;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogCommentResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@Tag(name = "Admin Comment", description = "Endpoints for managing blog comments as an admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminCommentController {
        BlogCommentImp blogCommentImp;

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get All Comments", description = "Retrieve all comments with pagination, sorting, and optional filtering by blog ID, user email, content, or date range")
        public ApiResponse<Page<BlogCommentResponse>> getAllComments(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDirection,
                        @RequestParam(required = false) Long blogId,
                        @RequestParam(required = false) String userEmail,
                        @RequestParam(required = false) String content,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate) {
                log.info("Admin request to get all comments - page: {}, size: {}, content: {}, startDate: {}, endDate: {}", 
                        page, size, content, startDate, endDate);

                Sort sort = sortDirection.equalsIgnoreCase("ASC")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<BlogCommentResponse>>builder()
                                .result(blogCommentImp.getAllCommentsForAdmin(pageable, blogId, userEmail, content, startDate, endDate))
                                .build();
        }

        @GetMapping("/{commentId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get Comment by ID", description = "Retrieve a specific comment by its ID")
        public ApiResponse<BlogCommentResponse> getCommentById(@PathVariable Long commentId) {
                log.info("Admin request to get comment by ID: {}", commentId);
                return ApiResponse.<BlogCommentResponse>builder()
                                .result(blogCommentImp.getCommentById(commentId))
                                .build();
        }

        @DeleteMapping("/{commentId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Delete Comment by ID", description = "Delete a specific comment by its ID")
        public ApiResponse<Void> deleteCommentAsAdmin(@PathVariable Long commentId) {
                log.info("Admin request to delete comment ID: {}", commentId);
                blogCommentImp.deleteCommentAsAdmin(commentId);
                return ApiResponse.<Void>builder()
                                .message("Comment deleted successfully by admin")
                                .build();
        }

        @PostMapping("/{commentId}/hide")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Hide Comment", description = "Hide a specific comment by its ID")
        public ApiResponse<BlogCommentResponse> hideComment(@PathVariable Long commentId) {
                log.info("Admin request to hide comment ID: {}", commentId);
                return ApiResponse.<BlogCommentResponse>builder()
                                .result(blogCommentImp.hideComment(commentId))
                                .message("Comment hidden successfully")
                                .build();
        }

        @PostMapping("/{commentId}/show")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Show Comment", description = "Show a specific comment by its ID")
        public ApiResponse<BlogCommentResponse> showComment(@PathVariable Long commentId) {
                log.info("Admin request to show comment ID: {}", commentId);
                return ApiResponse.<BlogCommentResponse>builder()
                                .result(blogCommentImp.showComment(commentId))
                                .message("Comment shown successfully")
                                .build();
        }

        @GetMapping("/statistics")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get Comment Statistics", description = "Retrieve statistics about comments")
        public ApiResponse<Object> getCommentStatistics() {
                log.info("Admin request to get comment statistics");
                return ApiResponse.builder()
                                .result(blogCommentImp.getCommentStatistics())
                                .build();
        }
}