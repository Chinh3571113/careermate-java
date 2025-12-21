package com.fpt.careermate.services.coach_services.repository;

import com.fpt.careermate.services.coach_services.domain.ResumeRoadmap;
import com.fpt.careermate.services.resume_services.domain.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ResumeRoadmapRepo extends JpaRepository<ResumeRoadmap,Integer> {
    Optional<ResumeRoadmap> findByResume_ResumeIdAndRoadmap_Id(
            int resume_id,
            int roadmap_id
    );

    Page<ResumeRoadmap> findByResume_ResumeId(int resumeId, Pageable pageable);

    Page<ResumeRoadmap> findByResume_Candidate_CandidateId(int candidateId, Pageable pageable);
}
