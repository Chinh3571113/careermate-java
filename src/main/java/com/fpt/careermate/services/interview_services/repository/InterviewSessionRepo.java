package com.fpt.careermate.services.interview_services.repository;

import com.fpt.careermate.services.interview_services.domain.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewSessionRepo extends JpaRepository<InterviewSession, Integer> {
    List<InterviewSession> findByCandidateCandidateIdOrderByCreatedAtDesc(int candidateId);

    Optional<InterviewSession> findBySessionIdAndCandidateCandidateId(int sessionId, int candidateId);

    /**
     * Find session by ID with questions eagerly loaded
     */
    @Query("SELECT s FROM interview_session s LEFT JOIN FETCH s.questions q WHERE s.sessionId = :sessionId ORDER BY q.questionNumber")
    Optional<InterviewSession> findByIdWithQuestions(@Param("sessionId") int sessionId);

    /**
     * Find session by ID and candidate ID with questions eagerly loaded
     */
    @Query("SELECT s FROM interview_session s LEFT JOIN FETCH s.questions q WHERE s.sessionId = :sessionId AND s.candidate.candidateId = :candidateId ORDER BY q.questionNumber")
    Optional<InterviewSession> findBySessionIdAndCandidateIdWithQuestions(@Param("sessionId") int sessionId,
            @Param("candidateId") int candidateId);
}
