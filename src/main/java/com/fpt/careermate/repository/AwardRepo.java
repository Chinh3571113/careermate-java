package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Award;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AwardRepo extends JpaRepository<Award, Integer> {
}
