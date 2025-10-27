package com.fpt.careermate.services.coach_services.repository;

import com.fpt.careermate.services.coach_services.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepo extends JpaRepository<Option,Integer> {
}
