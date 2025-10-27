package com.fpt.careermate.services.coach_services.repository;

import com.fpt.careermate.services.coach_services.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepo extends JpaRepository<Module,Integer> {
}
