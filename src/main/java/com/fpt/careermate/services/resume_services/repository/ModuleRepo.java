package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModuleRepo extends JpaRepository<Module,Integer> {
}
