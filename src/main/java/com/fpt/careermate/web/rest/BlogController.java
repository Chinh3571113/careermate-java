package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.BlogImp;
import com.fpt.careermate.services.dto.request.BlogCreationRequest;
import com.fpt.careermate.services.dto.request.BlogUpdateRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.BlogResponse;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@Tag(name = "Blog", description = "APIs for managing blogs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogController {
        BlogImp blogImp;

        // ADMIN ONLY - Blog Management Endpoints

        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Create Blog", description = "Create a new blog post (Admin only)")
        ApiResponse<BlogResponse> createBlog(@RequestBody BlogCreationRequest request) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String email = authentication.getName();

                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.createBlog(request, email))
                                .build();
        }

        @PutMapping("/{blogId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Update Blog", description = "Update an existing blog post (Admin only)")
        ApiResponse<BlogResponse> updateBlog(
                        @PathVariable Long blogId,
                        @RequestBody BlogUpdateRequest request) {
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.updateBlog(blogId, request))
                                .build();
        }

        @DeleteMapping("/{blogId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Delete Blog", description = "Delete a blog post by its ID (Admin only)")
        ApiResponse<Void> deleteBlog(@PathVariable Long blogId) {
                blogImp.deleteBlog(blogId);
                return ApiResponse.<Void>builder()
                                .message("Blog deleted successfully")
                                .build();
        }

        @PutMapping("/{blogId}/publish")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Publish Blog", description = "Publish a blog post (Admin only)")
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
        @Operation(summary = "Archive Blog", description = "Archive a blog post (Admin only)")
        ApiResponse<BlogResponse> archiveBlog(@PathVariable Long blogId) {
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.archiveBlog(blogId))
                                .build();
        }

        @PutMapping("/{blogId}/unarchive")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Unarchive Blog", description = "Unarchive a blog post (Admin only)")
        ApiResponse<BlogResponse> unarchiveBlog(@PathVariable Long blogId) {
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.unarchiveBlog(blogId))
                                .build();
        }

        // PUBLIC - Read-Only Endpoints (No Authentication Required)

        @GetMapping("/{blogId}")
        @Operation(summary = "Get Blog by ID", description = "Retrieve a blog post by its ID")
        ApiResponse<BlogResponse> getBlogById(@PathVariable Long blogId) {
                blogImp.incrementViewCount(blogId);
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.getBlogById(blogId))
                                .build();
        }

        @GetMapping
        @Operation(summary = "Get All Blogs", description = "Retrieve all blog posts with pagination and sorting")
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
        @Operation(summary = "Get Blogs by Status", description = "Retrieve blog posts filtered by status with pagination and sorting")
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
        @Operation(summary = "Get Blogs by Category", description = "Retrieve blog posts filtered by category with pagination and sorting")
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
        @Operation(summary = "Get Blogs by Author", description = "Retrieve blog posts filtered by author ID with pagination and sorting")
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
        @Operation(summary = "Search Blogs", description = "Search blog posts by keyword and/or status with pagination and sorting")
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
        @Operation(summary = "Get All Categories", description = "Retrieve a list of all blog categories")
        ApiResponse<List<String>> getAllCategories() {
                return ApiResponse.<List<String>>builder()
                                .result(blogImp.getAllCategories())
                                .build();
        }
}
