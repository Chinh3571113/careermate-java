package com.fpt.careermate.services.job_services.repository;


import com.fpt.careermate.services.job_services.domain.JobApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplyRepo extends JpaRepository<JobApply,Integer> {
    List<JobApply> findByJobPostingId(int jobPostingId);
    List<JobApply> findByCandidateCandidateId(int candidateId);
    Optional<JobApply> findByJobPostingIdAndCandidateCandidateId(int jobPostingId, int candidateId);
}
