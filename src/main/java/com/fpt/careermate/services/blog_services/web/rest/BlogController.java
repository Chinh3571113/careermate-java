package com.fpt.careermate.services.blog_services.web.rest;

import com.fpt.careermate.services.blog_services.service.BlogImp;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogCreationRequest;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogUpdateRequest;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogResponse;
import com.fpt.careermate.services.storage.FirebaseStorageService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/blogs")
@Tag(name = "Blog", description = "APIs for managing blogs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogController {
        BlogImp blogImp;
        FirebaseStorageService firebaseStorageService;

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
                // Only increment view count for non-admin users (public/guest users)
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                boolean isAdmin = authentication != null && 
                                  authentication.isAuthenticated() && 
                                  authentication.getAuthorities().stream()
                                          .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                
                if (!isAdmin) {
                        blogImp.incrementViewCount(blogId);
                }
                
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
        @Operation(summary = "Search Blogs", description = "Search blog posts by keyword and/or status with pagination and sorting (Legacy endpoint - use /filter for more options)")
        ApiResponse<Page<BlogResponse>> searchBlogs(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                // Map Java field names to database column names
                String dbColumnName = mapFieldToColumn(sortBy);
                
                Sort sort = sortDir.equalsIgnoreCase("ASC")
                                ? Sort.by(dbColumnName).ascending()
                                : Sort.by(dbColumnName).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<BlogResponse>>builder()
                                .result(blogImp.searchBlogs(keyword, status, pageable))
                                .build();
        }

        @GetMapping("/filter")
        @Operation(summary = "Filter Blogs", description = "Filter blog posts by multiple criteria: keyword, status, and/or category with pagination and sorting. All filter parameters are optional and work together.")
        ApiResponse<Page<BlogResponse>> filterBlogs(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String category,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                log.info("=== FILTER BLOGS REQUEST ===");
                log.info("Keyword: '{}' (null: {})", keyword, keyword == null);
                log.info("Status: '{}' (null: {})", status, status == null);
                log.info("Category: '{}' (null: {})", category, category == null);
                log.info("Page: {}, Size: {}, SortBy: {}, SortDir: {}", page, size, sortBy, sortDir);

                try {
                        // Map Java field names to database column names
                        String dbColumnName = mapFieldToColumn(sortBy);
                        log.info("Mapped sortBy '{}' to database column '{}'", sortBy, dbColumnName);
                        
                        Sort sort = sortDir.equalsIgnoreCase("ASC")
                                        ? Sort.by(dbColumnName).ascending()
                                        : Sort.by(dbColumnName).descending();
                        Pageable pageable = PageRequest.of(page, size, sort);

                        Page<BlogResponse> result = blogImp.filterBlogs(keyword, status, category, pageable);
                        log.info("Filter successful - Total elements: {}, Total pages: {}", 
                                 result.getTotalElements(), result.getTotalPages());
                        
                        return ApiResponse.<Page<BlogResponse>>builder()
                                        .result(result)
                                        .build();
                } catch (Exception e) {
                        log.error("Error filtering blogs", e);
                        throw e;
                }
        }

        /**
         * Maps Java entity field names to database column names for native queries
         */
        private String mapFieldToColumn(String fieldName) {
                return switch (fieldName) {
                        case "createdAt" -> "created_at";
                        case "updatedAt" -> "updated_at";
                        case "publishedAt" -> "published_at";
                        case "viewCount" -> "view_count";
                        case "averageRating" -> "average_rating";
                        case "ratingCount" -> "rating_count";
                        case "commentCount" -> "comment_count";
                        case "thumbnailUrl" -> "thumbnail_url";
                        default -> fieldName; // id, title, slug, content, summary, category, tags, status
                };
        }

        @GetMapping("/categories")
        @Operation(summary = "Get All Categories", description = "Retrieve a list of all blog categories")
        ApiResponse<List<String>> getAllCategories() {
                return ApiResponse.<List<String>>builder()
                                .result(blogImp.getAllCategories())
                                .build();
        }

        @GetMapping("/slug/{slug}")
        @Operation(summary = "Get Blog by Slug", description = "Retrieve a blog post by its SEO-friendly slug")
        ApiResponse<BlogResponse> getBlogBySlug(@PathVariable String slug) {
                blogImp.incrementViewCountBySlug(slug);
                return ApiResponse.<BlogResponse>builder()
                                .result(blogImp.getBlogBySlug(slug))
                                .build();
        }

        @GetMapping("/{blogId}/related")
        @Operation(summary = "Get Related Blogs", description = "Retrieve related blog posts based on category and tags")
        ApiResponse<List<BlogResponse>> getRelatedBlogs(
                        @PathVariable Long blogId,
                        @RequestParam(defaultValue = "5") int limit) {
                return ApiResponse.<List<BlogResponse>>builder()
                                .result(blogImp.getRelatedBlogs(blogId, limit))
                                .build();
        }

        @PostMapping("/upload-image")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Upload Blog Image", description = "Upload an image for blog post (thumbnail or content) to Firebase Storage")
        ApiResponse<Map<String, Object>> uploadBlogImage(@RequestParam("image") MultipartFile file) {
                log.info("Uploading blog image: {}", file.getOriginalFilename());

                // Validate file type
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                        return ApiResponse.<Map<String, Object>>builder()
                                        .code(1004)
                                        .message("Only image files are allowed")
                                        .build();
                }

                try {
                        // Upload to Firebase Storage in blogs folder
                        Map<String, Object> uploadResult = firebaseStorageService.uploadFile(file, "careermate/blogs");

                        // Prepare response
                        Map<String, Object> result = new HashMap<>();
                        result.put("imageUrl", uploadResult.get("secure_url"));
                        result.put("publicId", uploadResult.get("public_id"));
                        result.put("fileSize", file.getSize());
                        result.put("originalName", file.getOriginalFilename());

                        log.info("Blog image uploaded successfully: {}", uploadResult.get("public_id"));

                        return ApiResponse.<Map<String, Object>>builder()
                                        .code(1000)
                                        .message("Image uploaded successfully")
                                        .result(result)
                                        .build();

                } catch (IOException e) {
                        log.error("Failed to upload blog image: {}", e.getMessage(), e);
                        return ApiResponse.<Map<String, Object>>builder()
                                        .code(1005)
                                        .message("Failed to upload image: " + e.getMessage())
                                        .build();
                }
        }
}
