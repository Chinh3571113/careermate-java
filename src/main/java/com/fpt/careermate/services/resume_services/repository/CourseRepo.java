package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepo extends JpaRepository<Course,Integer> {
    Optional<List<Course>> findByCandidate_CandidateId(Integer integer);
    Optional<Course> findByIdAndCandidate_CandidateId(Integer courseId, Integer candidateId);
}
