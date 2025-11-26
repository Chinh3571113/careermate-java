package com.fpt.careermate.services.interview_services.repository;

import com.fpt.careermate.services.interview_services.domain.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewQuestionRepo extends JpaRepository<InterviewQuestion, Integer> {
    List<InterviewQuestion> findBySessionSessionIdOrderByQuestionNumberAsc(int sessionId);
}
