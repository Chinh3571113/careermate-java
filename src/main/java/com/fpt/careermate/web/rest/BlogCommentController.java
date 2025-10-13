package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.BlogCommentImp;
import com.fpt.careermate.services.dto.request.BlogCommentRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.BlogCommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/blogs/{blogId}/comments")
@Tag(name = "Blog Comment", description = "Endpoints for managing blog comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogCommentController {
    BlogCommentImp blogCommentImp;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation (summary = "Create Comment", description = "Create a new comment for a specific blog post")
    public ApiResponse<BlogCommentResponse> createComment(
            @PathVariable Long blogId,
            @Valid @RequestBody BlogCommentRequest request) {
        log.info("REST request to create comment for blog ID: {}", blogId);
        return ApiResponse.<BlogCommentResponse>builder()
                .result(blogCommentImp.createComment(blogId, request))
                .build();
    }

    @GetMapping
    @Operation(summary = "Get Comments by Blog ID", description = "Retrieve comments for a specific blog post with pagination and sorting")
    public ApiResponse<Page<BlogCommentResponse>> getCommentsByBlogId(
            @PathVariable Long blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("REST request to get comments for blog ID: {}", blogId);

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ApiResponse.<Page<BlogCommentResponse>>builder()
                .result(blogCommentImp.getCommentsByBlogId(blogId, pageable))
                .build();
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update Comment", description = "Update an existing comment by its ID")
    public ApiResponse<BlogCommentResponse> updateComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId,
            @Valid @RequestBody BlogCommentRequest request) {
        log.info("REST request to update comment ID: {}", commentId);
        return ApiResponse.<BlogCommentResponse>builder()
                .result(blogCommentImp.updateComment(commentId, request))
                .build();
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete Comment", description = "Delete a comment by its ID")
    public ApiResponse<Void> deleteComment(
            @PathVariable Long blogId,
            @PathVariable Long commentId) {
        log.info("REST request to delete comment ID: {}", commentId);
        blogCommentImp.deleteComment(commentId);
        return ApiResponse.<Void>builder()
                .message("Comment deleted successfully")
                .build();
    }
}

