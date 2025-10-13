package com.fpt.careermate.repository;

import com.fpt.careermate.domain.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkExperienceRepo extends JpaRepository<WorkExperience,Integer> {
    @Query("SELECT COUNT(w) FROM work_experience w WHERE w.resume.resumeId = :resumeId")
    long countWorkExperienceByResumeId(int resumeId);
}
