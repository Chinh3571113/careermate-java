package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionRepo extends JpaRepository<Option,Integer> {
}
