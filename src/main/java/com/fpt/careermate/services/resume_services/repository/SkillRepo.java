package com.fpt.careermate.services.resume_services.repository;

import com.fpt.careermate.services.resume_services.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SkillRepo extends JpaRepository<Skill, Integer> {
    @Query("SELECT COUNT(s) FROM skill s WHERE s.resume.resumeId = :resumeId")
    long countSkillByResumeId(int resumeId);

    List<Skill> findByResume_ResumeId(int resumeId);
}
