package com.fpt.careermate.repository;

import com.fpt.careermate.domain.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkExperienceRepo extends JpaRepository<WorkExperience,Integer> {
}
