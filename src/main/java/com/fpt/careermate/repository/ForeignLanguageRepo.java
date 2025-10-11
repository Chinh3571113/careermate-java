package com.fpt.careermate.repository;

import com.fpt.careermate.domain.ForeignLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ForeignLanguageRepo extends JpaRepository<ForeignLanguage, Integer> {
    @Query("SELECT COUNT(f) FROM foreign_language f WHERE f.resume.resumeId = :resumeId")
    long countForeignLanguageByResumeId(int resumeId);
}
