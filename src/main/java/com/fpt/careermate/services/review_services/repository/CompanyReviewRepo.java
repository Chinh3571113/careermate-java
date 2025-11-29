package com.fpt.careermate.services.review_services.repository;

import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import com.fpt.careermate.services.review_services.domain.CompanyReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyReviewRepo extends JpaRepository<CompanyReview, Integer> {
    
    /**
     * Find all reviews for a specific company/recruiter
     */
    Page<CompanyReview> findByRecruiterIdAndStatus(Integer recruiterId, ReviewStatus status, Pageable pageable);
    
    /**
     * Find all reviews by a specific candidate
     */
    Page<CompanyReview> findByCandidateCandidateIdAndStatus(Integer candidateId, ReviewStatus status, Pageable pageable);
    
    /**
     * Check if candidate already reviewed this company for this job application
     */
    boolean existsByJobApplyIdAndReviewType(Integer jobApplyId, ReviewType reviewType);
    
    /**
     * Get candidate's review for specific job application and review type
     */
    Optional<CompanyReview> findByJobApplyIdAndReviewTypeAndCandidateCandidateId(
        Integer jobApplyId, ReviewType reviewType, Integer candidateId
    );
    
    /**
     * Get average rating for a company
     * Uses native query for proper enum comparison
     */
    @Query(value = "SELECT AVG(cr.overall_rating) FROM company_review cr " +
           "WHERE cr.recruiter_id = :recruiterId AND cr.status = 'ACTIVE'", nativeQuery = true)
    Double getAverageRatingByRecruiterId(Integer recruiterId);
    
    /**
     * Get average rating by review type
     * Uses native query for proper enum comparison
     */
    @Query(value = "SELECT AVG(cr.overall_rating) FROM company_review cr " +
           "WHERE cr.recruiter_id = :recruiterId AND cr.review_type = :reviewType AND cr.status = 'ACTIVE'", nativeQuery = true)
    Double getAverageRatingByRecruiterIdAndType(Integer recruiterId, String reviewType);
    
    /**
     * Count reviews for a company
     */
    long countByRecruiterIdAndStatus(Integer recruiterId, ReviewStatus status);
    
    /**
     * Count reviews by type
     */
    long countByRecruiterIdAndReviewTypeAndStatus(Integer recruiterId, ReviewType reviewType, ReviewStatus status);
    
    /**
     * Find reviews flagged for moderation
     */
    List<CompanyReview> findByStatusAndFlagCountGreaterThan(ReviewStatus status, Integer flagThreshold);
    
    /**
     * Find recent reviews for a company
     */
    List<CompanyReview> findTop10ByRecruiterIdAndStatusOrderByCreatedAtDesc(Integer recruiterId, ReviewStatus status);
    
    /**
     * Check for potential duplicate review (same candidate, company, similar timestamp)
     * Uses native query for proper enum comparison
     */
    @Query(value = "SELECT * FROM company_review cr " +
           "WHERE cr.candidate_id = :candidateId " +
           "AND cr.recruiter_id = :recruiterId " +
           "AND cr.review_type = :reviewType " +
           "AND cr.created_at > :since " +
           "AND cr.status = 'ACTIVE'", nativeQuery = true)
    List<CompanyReview> findPotentialDuplicates(
        Integer candidateId, Integer recruiterId, String reviewType, LocalDateTime since
    );
    
    /**
     * Find reviews by duplicate hash (for detecting content plagiarism)
     */
    List<CompanyReview> findByDuplicateCheckHashAndStatusNot(String hash, ReviewStatus excludeStatus);
}
