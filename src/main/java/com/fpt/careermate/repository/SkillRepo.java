package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SkillRepo extends JpaRepository<Skill, Integer> {
    @Query("SELECT COUNT(s) FROM skill s WHERE s.resume.resumeId = :resumeId")
    long countSkillByResumeId(int resumeId);
}
