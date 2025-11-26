package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.EmploymentVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Simplified repository for employment verification.
 * Only basic queries for employment tracking.
 */
@Repository
public interface EmploymentVerificationRepo extends JpaRepository<EmploymentVerification, Integer> {
    
    /**
     * Find employment verification by job apply ID
     */
    Optional<EmploymentVerification> findByJobApplyId(Integer jobApplyId);
    
    /**
     * Find all active employments
     */
    List<EmploymentVerification> findByIsActiveTrue();
    
    /**
     * Find employments eligible for work experience review (30+ days)
     */
    @Query("SELECT ev FROM employment_verification ev " +
           "WHERE ev.isActive = true " +
           "AND ev.startDate <= :thirtyDaysAgo")
    List<EmploymentVerification> findEmploymentsEligibleForReview(@Param("thirtyDaysAgo") LocalDate thirtyDaysAgo);
}
