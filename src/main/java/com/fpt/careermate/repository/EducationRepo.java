package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Education;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepo extends JpaRepository<Education, Integer> {
}
