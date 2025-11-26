package com.fpt.careermate.services.review_services.service;

import com.fpt.careermate.services.review_services.constant.ReviewType;
import com.fpt.careermate.services.review_services.service.dto.request.CompanyReviewRequest;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewResponse;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewStatsResponse;
import com.fpt.careermate.services.review_services.service.dto.response.ReviewEligibilityResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for company review operations
 */
public interface CompanyReviewService {
    
    /**
     * Submit a new company review
     */
    CompanyReviewResponse submitReview(CompanyReviewRequest request, Integer candidateId);
    
    /**
     * Check if candidate is eligible to review a company
     */
    ReviewEligibilityResponse checkEligibility(Integer candidateId, Integer jobApplyId);
    
    /**
     * Get all reviews for a company (paginated)
     */
    Page<CompanyReviewResponse> getCompanyReviews(Integer recruiterId, ReviewType reviewType,
 int page, int size);
    
    /**
     * Get candidate's own reviews
     */
    Page<CompanyReviewResponse> getCandidateReviews(Integer candidateId, int page, int size);
    
    /**
     * Get average rating for a company
     */
    Double getAverageRating(Integer recruiterId);
    
    /**
     * Get review statistics for a company
     */
    CompanyReviewStatsResponse getCompanyStatistics(Integer recruiterId);
    
    /**
     * Flag a review for moderation
     */
    void flagReview(Integer reviewId, Integer reporterId, String reason);
    
    /**
     * Remove a review (admin/moderator only)
     */
    void removeReview(Integer reviewId, String reason);
    
    /**
     * Get a single review by ID
     */
    CompanyReviewResponse getReviewById(Integer reviewId);
}
