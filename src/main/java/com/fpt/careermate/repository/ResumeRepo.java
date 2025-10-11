package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepo extends JpaRepository<Resume, Integer> {
    Optional<Resume> findByCandidateCandidateId(int candidateId);
}