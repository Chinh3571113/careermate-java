package com.fpt.careermate.repository;

import com.fpt.careermate.domain.IndustryExperienceId;
import com.fpt.careermate.domain.IndustryExperiences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndustryExperiencesRepo extends JpaRepository<IndustryExperiences, IndustryExperienceId> {
}
