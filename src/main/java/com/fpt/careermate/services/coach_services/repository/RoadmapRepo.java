package com.fpt.careermate.services.coach_services.repository;

import com.fpt.careermate.services.coach_services.domain.Course;
import com.fpt.careermate.services.coach_services.domain.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoadmapRepo extends JpaRepository<Roadmap,Integer> {
}
