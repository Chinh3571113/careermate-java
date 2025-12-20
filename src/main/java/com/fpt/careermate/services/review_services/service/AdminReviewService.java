package com.fpt.careermate.services.review_services.service;

import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.service.dto.request.AdminBulkReviewActionRequest;
import com.fpt.careermate.services.review_services.service.dto.request.AdminReviewFilterRequest;
import com.fpt.careermate.services.review_services.service.dto.response.AdminReviewResponse;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * Service for admin review management operations
 */
public interface AdminReviewService {
    /**
     * Search reviews with dynamic filters
     */
    Page<AdminReviewResponse> searchReviews(AdminReviewFilterRequest request);
    
    /**
     * Update single review status
     */
    AdminReviewResponse updateReviewStatus(Integer reviewId, ReviewStatus newStatus, String reason);
    
    /**
     * Bulk update review statuses (for handling spam/bombing)
     */
    int bulkUpdateReviewStatus(AdminBulkReviewActionRequest request);
    
    /**
     * Get review statistics dashboard data
     */
    Map<String, Object> getReviewStatistics();
}
