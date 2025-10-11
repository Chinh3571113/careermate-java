package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Award;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AwardRepo extends JpaRepository<Award, Integer> {
    @Query("SELECT COUNT(a) FROM award a WHERE a.resume.resumeId = :resumeId")
    long countAwardByResumeId(int resumeId);
}
