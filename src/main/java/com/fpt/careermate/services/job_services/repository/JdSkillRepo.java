package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.JdSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface JdSkillRepo extends JpaRepository<JdSkill, Integer> {
    Optional<JdSkill> findSkillByName(String name);

    @Query("SELECT s FROM jd_skill s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY s.name ASC")
    List<JdSkill> searchByKeyword(@Param("keyword") String keyword);
}
