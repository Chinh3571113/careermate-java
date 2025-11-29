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

    // Check duplicate title within same recruiter
    Optional<JobPosting> findByTitleAndRecruiterId(String title, int recruiterId);

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

    long countByRecruiterIdAndStatus(int recruiterId, String status);

    Page<JobPosting> findAllByStatusAndRecruiter_VerificationStatus(
            String status, String verificationStatus, Pageable pageable
    );

    @Query(value = "SELECT COUNT(*) FROM job_posting jp WHERE jp.recruiter_id = :recruiterId " +
           "AND EXTRACT(MONTH FROM jp.create_at) = :month AND EXTRACT(YEAR FROM jp.create_at) = :year", nativeQuery = true)
    int countByRecruiterAndMonth(@Param("recruiterId") int recruiterId,
                                 @Param("month") int month,
                                 @Param("year") int year);

    // New: Fetch a job posting with its jobDescriptions and associated jdSkill eagerly to avoid lazy loading issues
    @Query("SELECT DISTINCT jp FROM job_posting jp " +
           "LEFT JOIN FETCH jp.jobDescriptions jd " +
           "LEFT JOIN FETCH jd.jdSkill " +
           "WHERE jp.id = :id")
    Optional<JobPosting> fetchByIdWithSkills(@Param("id") int id);
}
