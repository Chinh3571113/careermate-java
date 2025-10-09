package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateRepo extends JpaRepository<Candidate, Integer> {
    Optional<Candidate> findByAccount_Id(Integer accountId);
}
