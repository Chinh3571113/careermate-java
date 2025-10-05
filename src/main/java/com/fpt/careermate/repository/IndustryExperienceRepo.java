package com.fpt.careermate.repository;

import com.fpt.careermate.domain.IndustryExperiences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndustryExperienceRepo extends JpaRepository<IndustryExperiences, Integer> {
}
