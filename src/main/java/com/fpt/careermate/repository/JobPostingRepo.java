package com.fpt.careermate.repository;

import com.fpt.careermate.domain.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface JobPostingRepo extends JpaRepository<JobPosting, Integer> {
    List<JobPosting> findAllByStatus(String status);
    Optional<JobPosting> findByTitle(String title);
    List<JobPosting> findAllByRecruiter_Id(int recruiterId);
}
