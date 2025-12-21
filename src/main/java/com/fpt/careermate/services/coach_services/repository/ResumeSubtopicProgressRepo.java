package com.fpt.careermate.services.coach_services.repository;

import com.fpt.careermate.services.coach_services.domain.ResumeSubtopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ResumeSubtopicProgressRepo extends JpaRepository<ResumeSubtopicProgress,Integer> {
    List<ResumeSubtopicProgress> findAllByResumeRoadmap_Id(int resumeRoadmapId);

    Optional<ResumeSubtopicProgress> findByResumeRoadmap_Resume_ResumeIdAndSubtopic_Id(int resumeId, int subtopicId);
}
