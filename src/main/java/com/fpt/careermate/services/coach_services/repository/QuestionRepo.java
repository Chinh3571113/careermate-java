package com.fpt.careermate.services.coach_services.repository;

import com.fpt.careermate.services.coach_services.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepo extends JpaRepository<Question,Integer> {
}
