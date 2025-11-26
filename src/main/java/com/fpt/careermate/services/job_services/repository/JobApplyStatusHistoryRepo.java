package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.JobApplyStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplyStatusHistoryRepo extends JpaRepository<JobApplyStatusHistory, Integer> {
    
    /**
     * Find all status changes for a specific job application, ordered by change time
     */
    List<JobApplyStatusHistory> findByJobApplyIdOrderByChangedAtDesc(int jobApplyId);
    
    /**
     * Get the most recent status change for a job application
     */
    @Query("SELECT h FROM job_apply_status_history h WHERE h.jobApply.id = :jobApplyId " +
           "ORDER BY h.changedAt DESC LIMIT 1")
    JobApplyStatusHistory findLatestByJobApplyId(int jobApplyId);
    
    /**
     * Count status changes for a job application
     */
    long countByJobApplyId(int jobApplyId);
}
