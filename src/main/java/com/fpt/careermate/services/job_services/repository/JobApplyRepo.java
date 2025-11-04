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

    // Filter by candidateId and optional status with pagination
    @Query("SELECT ja FROM job_apply ja WHERE ja.candidate.candidateId = :candidateId " +
           "AND (:status IS NULL OR ja.status = :status)")
    Page<JobApply> findByCandidateIdAndStatus(
            @Param("candidateId") int candidateId,
            @Param("status") StatusJobApply status,
            Pageable pageable);
}
