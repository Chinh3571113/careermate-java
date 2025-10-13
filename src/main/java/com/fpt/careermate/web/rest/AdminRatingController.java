package com.fpt.careermate.web.rest;

import com.fpt.careermate.domain.BlogRating;
import com.fpt.careermate.services.BlogRatingImp;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.BlogRatingResponse;
import com.fpt.careermate.services.mapper.BlogRatingMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ratings")
@Tag(name = "Admin Rating", description = "Endpoints for managing blog ratings as an admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminRatingController {
    BlogRatingImp blogRatingImp;
    BlogRatingMapper blogRatingMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get All Ratings", description = "Retrieve all ratings with pagination and sorting")
    public ApiResponse<Page<BlogRatingResponse>> getAllRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Admin request to get all ratings - page: {}, size: {}", page, size);

        Page<BlogRating> ratings = blogRatingImp.getAllRatingsForAdmin(page, size, sortBy, sortDirection);
        Page<BlogRatingResponse> ratingResponses = ratings.map(blogRatingMapper::toBlogRatingResponse);

        return ApiResponse.<Page<BlogRatingResponse>>builder()
                .result(ratingResponses)
                .build();
    }

    @GetMapping("/{ratingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Rating by ID", description = "Retrieve a specific rating by its ID")
    public ApiResponse<BlogRatingResponse> getRatingById(@PathVariable Long ratingId) {
        log.info("Admin request to get rating by ID: {}", ratingId);
        BlogRating rating = blogRatingImp.getRatingById(ratingId);
        return ApiResponse.<BlogRatingResponse>builder()
                .result(blogRatingMapper.toBlogRatingResponse(rating))
                .build();
    }

    @DeleteMapping("/{ratingId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Rating by ID", description = "Delete a specific rating by its ID")
    public ApiResponse<Void> deleteRatingAsAdmin(@PathVariable Long ratingId) {
        log.info("Admin request to delete rating ID: {}", ratingId);
        blogRatingImp.deleteRatingAsAdmin(ratingId);
        return ApiResponse.<Void>builder()
                .message("Rating deleted successfully by admin")
                .build();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Rating Statistics", description = "Retrieve statistics about ratings, such as average rating, total ratings, etc.")
    public ApiResponse<Object> getRatingStatistics() {
        log.info("Admin request to get rating statistics");
        return ApiResponse.builder()
                .result(blogRatingImp.getRatingStatistics())
                .build();
    }

    @GetMapping("/blog/{blogId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Blog Rating Summary", description = "Retrieve a summary of ratings for a specific blog post")
    public ApiResponse<Object> getBlogRatingSummary(@PathVariable Long blogId) {
        log.info("Admin request to get rating summary for blog ID: {}", blogId);
        return ApiResponse.builder()
                .result(blogRatingImp.getBlogRatingSummary(blogId))
                .build();
    }
}