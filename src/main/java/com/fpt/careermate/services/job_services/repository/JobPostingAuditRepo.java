package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.JobPostingAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobPostingAuditRepo extends JpaRepository<JobPostingAudit, Integer> {
    
    /**
     * Find all audit records for a specific job posting
     */
    List<JobPostingAudit> findByJobPostingIdOrderByChangedAtDesc(int jobPostingId);
    
    /**
     * Find all audit records for a recruiter
     */
    Page<JobPostingAudit> findByRecruiterIdOrderByChangedAtDesc(int recruiterId, Pageable pageable);
    
    /**
     * Find audit records by action type
     */
    List<JobPostingAudit> findByActionType(String actionType);
    
    /**
     * Find audit records within a date range
     */
    @Query("SELECT a FROM job_posting_audit a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<JobPostingAudit> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count changes for a job posting
     */
    long countByJobPostingId(int jobPostingId);
}
