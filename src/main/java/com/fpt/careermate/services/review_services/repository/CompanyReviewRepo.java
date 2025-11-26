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
     */
    @Query("SELECT AVG(cr.overallRating) FROM company_review cr " +
           "WHERE cr.recruiter.id = :recruiterId AND cr.status = 'ACTIVE'")
    Double getAverageRatingByRecruiterId(Integer recruiterId);
    
    /**
     * Get average rating by review type
     */
    @Query("SELECT AVG(cr.overallRating) FROM company_review cr " +
           "WHERE cr.recruiter.id = :recruiterId AND cr.reviewType = :reviewType AND cr.status = 'ACTIVE'")
    Double getAverageRatingByRecruiterIdAndType(Integer recruiterId, ReviewType reviewType);
    
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
     */
    @Query("SELECT cr FROM company_review cr " +
           "WHERE cr.candidate.candidateId = :candidateId " +
           "AND cr.recruiter.id = :recruiterId " +
           "AND cr.reviewType = :reviewType " +
           "AND cr.createdAt > :since " +
           "AND cr.status = 'ACTIVE'")
    List<CompanyReview> findPotentialDuplicates(
        Integer candidateId, Integer recruiterId, ReviewType reviewType, LocalDateTime since
    );
    
    /**
     * Find reviews by duplicate hash (for detecting content plagiarism)
     */
    List<CompanyReview> findByDuplicateCheckHashAndStatusNot(String hash, ReviewStatus excludeStatus);
}
