package com.fpt.careermate.services.review_services.repository;

import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import com.fpt.careermate.services.review_services.domain.CompanyReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyReviewRepo
        extends JpaRepository<CompanyReview, Integer>, JpaSpecificationExecutor<CompanyReview> {

    /**
     * Find all reviews for a specific company/recruiter
     */
    Page<CompanyReview> findByRecruiterIdAndStatus(Integer recruiterId, ReviewStatus status, Pageable pageable);

    /**
     * Find all reviews for a specific company/recruiter by type.
     */
    Page<CompanyReview> findByRecruiterIdAndReviewTypeAndStatus(Integer recruiterId, ReviewType reviewType,
            ReviewStatus status, Pageable pageable);

    /**
     * Find all reviews by a specific candidate
     */
    Page<CompanyReview> findByCandidateCandidateIdAndStatus(Integer candidateId, ReviewStatus status,
            Pageable pageable);

    /**
     * Check if candidate already reviewed this company for this review type.
     * (Prevents spamming multiple reviews by applying multiple times.)
     */
    boolean existsByCandidateCandidateIdAndRecruiterIdAndReviewType(Integer candidateId,
            Integer recruiterId,
            ReviewType reviewType);

    /**
     * Check if candidate already submitted this review type for a specific job
     * application.
     * Each job application can have 3 review types: APPLICATION, INTERVIEW, WORK.
     */
    boolean existsByJobApplyIdAndReviewType(Integer jobApplyId, ReviewType reviewType);

    /**
     * Get average rating for a company
     * Uses native query for proper enum comparison
     */
    @Query(value = "SELECT AVG(cr.overall_rating) FROM company_review cr " +
            "WHERE cr.recruiter_id = :recruiterId AND cr.status = 'ACTIVE'", nativeQuery = true)
    Double getAverageRatingByRecruiterId(Integer recruiterId);

    /**
     * Get average rating by recruiter and status
     */
    @Query(value = "SELECT AVG(cr.overall_rating) FROM company_review cr " +
            "WHERE cr.recruiter_id = :recruiterId AND cr.status = :status", nativeQuery = true)
    Double getAverageRatingByRecruiterAndStatus(@Param("recruiterId") Integer recruiterId,
            @Param("status") String status);

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
     * Count reviews by status
     */
    long countByStatus(ReviewStatus status);

    /**
     * Count reviews by type
     */
    long countByReviewType(ReviewType reviewType);

    /**
     * Count reviews created after a certain date
     */
    long countByCreatedAtAfter(LocalDateTime date);

    /**
     * Count reviews by recruiter, status and created after
     */
    long countByRecruiterIdAndStatusAndCreatedAtAfter(Integer recruiterId, ReviewStatus status, LocalDateTime date);

    /**
     * Find reviews flagged for moderation
     */
    List<CompanyReview> findByStatusAndFlagCountGreaterThan(ReviewStatus status, Integer flagThreshold);

    /**
     * Find recent reviews for a company
     */
    List<CompanyReview> findTop10ByRecruiterIdAndStatusOrderByCreatedAtDesc(Integer recruiterId, ReviewStatus status);

    /**
     * Check for potential duplicate review (same candidate, company, similar
     * timestamp)
     * Uses native query for proper enum comparison
     */
    @Query(value = "SELECT * FROM company_review cr " +
            "WHERE cr.candidate_id = :candidateId " +
            "AND cr.recruiter_id = :recruiterId " +
            "AND cr.review_type = :reviewType " +
            "AND cr.created_at > :since " +
            "AND cr.status = 'ACTIVE'", nativeQuery = true)
    List<CompanyReview> findPotentialDuplicates(
            Integer candidateId, Integer recruiterId, String reviewType, LocalDateTime since);

    /**
     * Find reviews by duplicate hash (for detecting content plagiarism)
     */
    List<CompanyReview> findByDuplicateCheckHashAndStatusNot(String hash, ReviewStatus excludeStatus);

    /**
     * Find all reviews by a candidate for a specific job application
     * Used to check which review types have already been submitted
     */
    List<CompanyReview> findByCandidateCandidateIdAndJobApplyId(Integer candidateId, Integer jobApplyId);

    /**
     * Admin moderation list with optional filters.
     * Note: JPQL uses the entity name defined by @Entity(name = "company_review").
     */
    @Query("SELECT cr FROM company_review cr " +
            "WHERE (:recruiterId IS NULL OR cr.recruiter.id = :recruiterId) " +
            "AND (:status IS NULL OR cr.status = :status) " +
            "AND (:reviewType IS NULL OR cr.reviewType = :reviewType) " +
            "AND (:from IS NULL OR cr.createdAt >= :from) " +
            "AND (:to IS NULL OR cr.createdAt <= :to)")
    Page<CompanyReview> adminFindByFilters(
            @Param("recruiterId") Integer recruiterId,
            @Param("status") ReviewStatus status,
            @Param("reviewType") ReviewType reviewType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
