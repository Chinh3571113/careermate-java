package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillRepo extends JpaRepository<Skill, Integer> {
}
