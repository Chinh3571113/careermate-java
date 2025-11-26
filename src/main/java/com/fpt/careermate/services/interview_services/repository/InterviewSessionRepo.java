package com.fpt.careermate.services.interview_services.repository;

import com.fpt.careermate.services.interview_services.domain.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSessionRepo extends JpaRepository<InterviewSession, Integer> {
    List<InterviewSession> findByCandidateCandidateIdOrderByCreatedAtDesc(int candidateId);
    Optional<InterviewSession> findBySessionIdAndCandidateCandidateId(int sessionId, int candidateId);
}

