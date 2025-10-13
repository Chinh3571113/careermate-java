package com.fpt.careermate.repository;

import com.fpt.careermate.domain.JobPosting;
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

    List<JobPosting> findAllByRecruiter_Id(int recruiterId);

    List<JobPosting> findByExpirationDateBeforeAndStatusNotIn(
            LocalDate date, List<String> statuses);

    // Admin methods
    Page<JobPosting> findAllByStatusOrderByCreateAtDesc(String status, Pageable pageable);

    @Query("SELECT COUNT(jp) FROM job_postings jp WHERE jp.status = :status")
    Long countByStatus(@Param("status") String status);
}
