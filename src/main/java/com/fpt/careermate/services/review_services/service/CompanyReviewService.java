package com.fpt.careermate.services.review_services.service;

import com.fpt.careermate.services.review_services.constant.ReviewType;
import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.service.dto.request.CompanyReviewRequest;
import com.fpt.careermate.services.review_services.service.dto.request.AdminBulkReviewStatusRequest;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewResponse;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewStatsResponse;
import com.fpt.careermate.services.review_services.service.dto.response.PublicCompanyReviewResponse;
import com.fpt.careermate.services.review_services.service.dto.response.ReviewEligibilityResponse;
import com.fpt.careermate.services.review_services.service.dto.response.JobApplicationReviewStatusResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

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
   * Update an existing review (candidate only)
   */
  CompanyReviewResponse updateReview(Integer reviewId, CompanyReviewRequest request, Integer candidateId);

  /**
   * Check if candidate is eligible to review a company
   */
  ReviewEligibilityResponse checkEligibility(Integer candidateId, Integer jobApplyId);

  /**
   * Get public reviews for a company (paginated)
   */
  Page<PublicCompanyReviewResponse> getCompanyReviews(Integer recruiterId, ReviewType reviewType,
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
  PublicCompanyReviewResponse getReviewById(Integer reviewId);

  /**
   * Admin moderation: list reviews with filters.
   */
  Page<CompanyReviewResponse> adminGetReviews(Integer recruiterId,
      ReviewStatus status,
      ReviewType reviewType,
      LocalDateTime from,
      LocalDateTime to,
      int page,
      int size);

  /**
   * Admin moderation: set status for a review.
   */
  void adminSetReviewStatus(Integer reviewId, ReviewStatus status, String reason);

  /**
   * Admin moderation: bulk set status for reviews.
   */
  void adminBulkSetReviewStatus(AdminBulkReviewStatusRequest request);

  /**
   * Get all job applications that are eligible for review but haven't been
   * reviewed yet
   * Used for the "Available to Review" tab in the frontend
   */
  List<ReviewEligibilityResponse> getPendingReviews(Integer candidateId);

  /**
   * Get all job applications with review status for each review type
   * Shows which reviews are submitted, available, or not yet eligible
   */
  List<JobApplicationReviewStatusResponse> getApplicationsWithReviewStatus(Integer candidateId);
}
