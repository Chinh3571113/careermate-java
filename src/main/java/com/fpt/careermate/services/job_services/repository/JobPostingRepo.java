package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobPostingRepo extends JpaRepository<JobPosting, Integer> {
    List<JobPosting> findAllByStatus(String status);

    Optional<JobPosting> findByTitle(String title);

    Page<JobPosting> findAllByRecruiterId(int recruiterId, Pageable pageable);

    List<JobPosting> findByExpirationDateBeforeAndStatusNotIn(
            LocalDate date, List<String> statuses);

    // Admin methods
    Page<JobPosting> findAllByStatusOrderByCreateAtDesc(String status, Pageable pageable);

    @Query("SELECT COUNT(jp) FROM job_posting jp WHERE jp.status = :status")
    Long countByStatus(@Param("status") String status);

    // Candidate methods - only see APPROVED job postings
    Page<JobPosting> findAllByStatusAndExpirationDateAfterOrderByCreateAtDesc(
            String status, LocalDate currentDate, Pageable pageable);

    @Query("SELECT jp FROM job_posting jp WHERE jp.status = :status " +
           "AND jp.expirationDate > :currentDate " +
           "AND (LOWER(jp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(jp.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(jp.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY jp.createAt DESC")
    Page<JobPosting> searchApprovedJobPostings(
            @Param("status") String status,
            @Param("currentDate") LocalDate currentDate,
            @Param("keyword") String keyword,
            Pageable pageable);

    Optional<JobPosting> findByIdAndStatus(int id, String status);

    Page<JobPosting> findByRecruiterIdAndTitleContainingIgnoreCase(
            int recruiterId, String keyword, Pageable pageable
    );
}
