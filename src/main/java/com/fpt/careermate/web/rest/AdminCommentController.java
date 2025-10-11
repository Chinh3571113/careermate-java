package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.BlogCommentImp;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.BlogCommentResponse;
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

@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminCommentController {
        BlogCommentImp blogCommentImp;

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<Page<BlogCommentResponse>> getAllComments(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDirection,
                        @RequestParam(required = false) Long blogId,
                        @RequestParam(required = false) String userEmail) {
                log.info("Admin request to get all comments - page: {}, size: {}", page, size);

                Sort sort = sortDirection.equalsIgnoreCase("ASC")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<BlogCommentResponse>>builder()
                                .result(blogCommentImp.getAllCommentsForAdmin(pageable, blogId, userEmail))
                                .build();
        }

        @GetMapping("/{commentId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<BlogCommentResponse> getCommentById(@PathVariable Long commentId) {
                log.info("Admin request to get comment by ID: {}", commentId);
                return ApiResponse.<BlogCommentResponse>builder()
                                .result(blogCommentImp.getCommentById(commentId))
                                .build();
        }

        @DeleteMapping("/{commentId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<Void> deleteCommentAsAdmin(@PathVariable Long commentId) {
                log.info("Admin request to delete comment ID: {}", commentId);
                blogCommentImp.deleteCommentAsAdmin(commentId);
                return ApiResponse.<Void>builder()
                                .message("Comment deleted successfully by admin")
                                .build();
        }

        @PostMapping("/{commentId}/hide")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<BlogCommentResponse> hideComment(@PathVariable Long commentId) {
                log.info("Admin request to hide comment ID: {}", commentId);
                return ApiResponse.<BlogCommentResponse>builder()
                                .result(blogCommentImp.hideComment(commentId))
                                .message("Comment hidden successfully")
                                .build();
        }

        @PostMapping("/{commentId}/show")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<BlogCommentResponse> showComment(@PathVariable Long commentId) {
                log.info("Admin request to show comment ID: {}", commentId);
                return ApiResponse.<BlogCommentResponse>builder()
                                .result(blogCommentImp.showComment(commentId))
                                .message("Comment shown successfully")
                                .build();
        }

        @GetMapping("/statistics")
        @PreAuthorize("hasRole('ADMIN')")
        public ApiResponse<Object> getCommentStatistics() {
                log.info("Admin request to get comment statistics");
                return ApiResponse.builder()
                                .result(blogCommentImp.getCommentStatistics())
                                .build();
        }
}