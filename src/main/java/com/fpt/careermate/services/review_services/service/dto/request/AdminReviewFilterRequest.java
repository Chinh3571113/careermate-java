package com.fpt.careermate.services.review_services.service.dto.request;

import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Request for filtering reviews in admin dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewFilterRequest {
    private String companyName; // Search by company name (deprecated, use searchText)
    private String candidateName; // Search by candidate name (deprecated, use searchText)
    private ReviewType reviewType; // Filter by review type
    private ReviewStatus status; // Filter by status

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate; // Filter by date range

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private Integer minRating; // Filter by rating
    private Integer maxRating;

    private Boolean flaggedOnly; // Show only flagged reviews
    private Integer minFlagCount; // Minimum flag count

    private String searchText; // Combined search: company name, candidate name, job title, and review text

    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
