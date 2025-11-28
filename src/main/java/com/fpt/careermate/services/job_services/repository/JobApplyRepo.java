package com.fpt.careermate.services.job_services.repository;


import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.services.job_services.domain.JobApply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplyRepo extends JpaRepository<JobApply,Integer> {
    List<JobApply> findByJobPostingId(int jobPostingId);
    List<JobApply> findByCandidateCandidateId(int candidateId);
    Optional<JobApply> findByJobPostingIdAndCandidateCandidateId(int jobPostingId, int candidateId);
    @Query("SELECT ja FROM job_apply ja WHERE ja.candidate.candidateId = :candidateId " +
            "AND (:status IS NULL OR ja.status = :status)")
    Page<JobApply> findByCandidateIdAndStatus(
            @Param("candidateId") int candidateId,
            @Param("status") StatusJobApply status,
            Pageable pageable);

    @Query("SELECT COUNT(a) FROM job_apply a WHERE a.candidate.candidateId = :candidateId " +
            "AND MONTH(a.createAt) = :month AND YEAR(a.createAt) = :year")
    int countByCandidateAndMonth(@Param("candidateId") int candidateId,
                                 @Param("month") int month,
                                 @Param("year") int year);

    /**
     * Find all pending applications for a candidate that should be auto-withdrawn when hired.
     * Returns applications with status: SUBMITTED, REVIEWING, INTERVIEW_SCHEDULED, INTERVIEWED, APPROVED
     * Excludes the specific hired application.
     */
    @Query("SELECT ja FROM job_apply ja WHERE ja.candidate.candidateId = :candidateId " +
            "AND ja.id != :excludeApplicationId " +
            "AND ja.status IN :activeStatuses")
    List<JobApply> findActivePendingApplicationsByCandidate(
            @Param("candidateId") int candidateId,
            @Param("excludeApplicationId") int excludeApplicationId,
            @Param("activeStatuses") List<StatusJobApply> activeStatuses);

    // Stats methods for recruiter dashboard
    @Query("SELECT COUNT(ja) FROM job_apply ja WHERE ja.jobPosting.recruiter.id = :recruiterId")
    long countByRecruiterId(@Param("recruiterId") int recruiterId);

    @Query("SELECT COUNT(ja) FROM job_apply ja WHERE ja.jobPosting.recruiter.id = :recruiterId AND ja.status = :status")
    long countByRecruiterIdAndStatus(@Param("recruiterId") int recruiterId, @Param("status") StatusJobApply status);

    // Find applications for all job postings of a recruiter
    @Query("SELECT ja FROM job_apply ja WHERE ja.jobPosting.recruiter.id = :recruiterId ORDER BY ja.createAt DESC")
    List<JobApply> findByRecruiterId(@Param("recruiterId") int recruiterId);

    @Query("SELECT ja FROM job_apply ja WHERE ja.jobPosting.recruiter.id = :recruiterId ORDER BY ja.createAt DESC")
    Page<JobApply> findByRecruiterId(@Param("recruiterId") int recruiterId, Pageable pageable);
}
