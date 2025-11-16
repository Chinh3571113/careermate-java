package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface JobDescriptionRepo extends JpaRepository<JobDescription, Integer> {
    List<JobDescription> findByJobPosting_Id(int id);

    @Query(value = "SELECT jd.skill_id, COUNT(jd.skill_id) as skill_count " +
           "FROM job_description jd " +
           "WHERE jd.id IN (SELECT id FROM job_description ORDER BY id LIMIT 50) " +
           "GROUP BY jd.skill_id " +
           "ORDER BY skill_count DESC", nativeQuery = true)
    List<Object[]> findTopSkillsFromFirst50Records();
}
