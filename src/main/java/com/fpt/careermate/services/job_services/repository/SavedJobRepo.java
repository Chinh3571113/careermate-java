package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedJobRepo extends JpaRepository<SavedJob, Integer> {
    Optional<SavedJob> findByCandidate_candidateIdAndJobPosting_Id(int candidateId, int jobId);
    Page<SavedJob> findByCandidate_CandidateId(int candidateId, Pageable pageable);
    List<SavedJob> findAllByCandidate_CandidateId(int candidateId);
}
