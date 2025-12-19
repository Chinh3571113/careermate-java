package com.fpt.careermate.services.review_services.service;

import com.fpt.careermate.services.review_services.service.dto.response.AdminReviewResponse;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * Service for recruiter review viewing operations (read-only)
 */
public interface RecruiterReviewService {
    /**
     * Get reviews for recruiter's company
     */
    Page<AdminReviewResponse> getRecruiterCompanyReviews(
            Integer recruiterId, 
            int page, 
            int size,
            String reviewType,
            String startDate,
            String endDate,
            Integer rating,
            Integer maxRating,
            String searchText);
    
    /**
     * Get review statistics for recruiter's company
     */
    Map<String, Object> getRecruiterReviewStatistics(Integer recruiterId);
}
