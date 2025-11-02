package com.fpt.careermate.services.coach_services.repository;

import com.fpt.careermate.services.coach_services.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepo extends JpaRepository<Course,Integer> {
    Optional<List<Course>> findByCandidate_CandidateId(Integer integer);
    Optional<Course> findByIdAndCandidate_CandidateId(Integer courseId, Integer candidateId);
    Optional<Course> findByTitleAndCandidate_CandidateId(String title, Integer candidateId);
}
