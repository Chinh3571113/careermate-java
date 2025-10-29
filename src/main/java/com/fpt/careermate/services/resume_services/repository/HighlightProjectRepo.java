package com.fpt.careermate.services.resume_services.repository;

import com.fpt.careermate.services.resume_services.domain.HighlightProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HighlightProjectRepo extends JpaRepository<HighlightProject, Integer> {
    @Query("SELECT COUNT(h) FROM highlight_project h WHERE h.resume.resumeId = :resumeId")
    int countHighlightProjectByResumeId(@Param("resumeId") int resumeId);
}
