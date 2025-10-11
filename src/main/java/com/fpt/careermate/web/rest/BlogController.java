package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.BlogImp;
import com.fpt.careermate.services.dto.request.BlogCreationRequest;
import com.fpt.careermate.services.dto.request.BlogUpdateRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.BlogResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogController {
        BlogImp blogImp;

        // ADMIN ONLY - Blog Management Endpoints

        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        ApiResponse<BlogResponse> createBlog(@RequestBody BlogCreationRequest request) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String email = authentication.getName();

                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.createBlog(request, email))
                                .build();
        }

        @PutMapping("/{blogId}")
        @PreAuthorize("hasRole('ADMIN')")
        ApiResponse<BlogResponse> updateBlog(
                        @PathVariable Long blogId,
                        @RequestBody BlogUpdateRequest request) {
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.updateBlog(blogId, request))
                                .build();
        }

        @DeleteMapping("/{blogId}")
        @PreAuthorize("hasRole('ADMIN')")
        ApiResponse<Void> deleteBlog(@PathVariable Long blogId) {
                blogImp.deleteBlog(blogId);
                return ApiResponse.<Void>builder()
                                .message("Blog deleted successfully")
                                .build();
        }

        @PutMapping("/{blogId}/publish")
        @PreAuthorize("hasRole('ADMIN')")
        ApiResponse<BlogResponse> publishBlog(@PathVariable Long blogId) {
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.publishBlog(blogId))
                                .build();
        }

        @PutMapping("/{blogId}/unpublish")
        @PreAuthorize("hasRole('ADMIN')")
        ApiResponse<BlogResponse> unpublishBlog(@PathVariable Long blogId) {
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.unpublishBlog(blogId))
                                .build();
        }

        @PutMapping("/{blogId}/archive")
        @PreAuthorize("hasRole('ADMIN')")
        ApiResponse<BlogResponse> archiveBlog(@PathVariable Long blogId) {
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.archiveBlog(blogId))
                                .build();
        }

        @PutMapping("/{blogId}/unarchive")
        @PreAuthorize("hasRole('ADMIN')")
        ApiResponse<BlogResponse> unarchiveBlog(@PathVariable Long blogId) {
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.unarchiveBlog(blogId))
                                .build();
        }

        // PUBLIC - Read-Only Endpoints (No Authentication Required)

        @GetMapping("/{blogId}")
        ApiResponse<BlogResponse> getBlogById(@PathVariable Long blogId) {
                blogImp.incrementViewCount(blogId);
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.getBlogById(blogId))
                                .build();
        }

        @GetMapping
        ApiResponse<Page<BlogResponse>> getAllBlogs(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("ASC")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<BlogResponse>>builder()
                                .result(blogImp.getAllBlogs(pageable))
                                .build();
        }

        @GetMapping("/status/{status}")
        ApiResponse<Page<BlogResponse>> getBlogsByStatus(
                        @PathVariable String status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("ASC")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<BlogResponse>>builder()
                                .result(blogImp.getBlogsByStatus(status, pageable))
                                .build();
        }

        @GetMapping("/category/{category}")
        ApiResponse<Page<BlogResponse>> getBlogsByCategory(
                        @PathVariable String category,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("ASC")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<BlogResponse>>builder()
                                .result(blogImp.getBlogsByCategory(category, pageable))
                                .build();
        }

        @GetMapping("/author/{authorId}")
        ApiResponse<Page<BlogResponse>> getBlogsByAuthor(
                        @PathVariable int authorId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("ASC")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<BlogResponse>>builder()
                                .result(blogImp.getBlogsByAuthor(authorId, pageable))
                                .build();
        }

        @GetMapping("/search")
        ApiResponse<Page<BlogResponse>> searchBlogs(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase("ASC")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<BlogResponse>>builder()
                                .result(blogImp.searchBlogs(keyword, status, pageable))
                                .build();
        }

        @GetMapping("/categories")
        ApiResponse<List<String>> getAllCategories() {
                return ApiResponse.<List<String>>builder()
                                .result(blogImp.getAllCategories())
                                .build();
        }
}
