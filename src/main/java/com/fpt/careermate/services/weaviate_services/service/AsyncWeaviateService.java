package com.fpt.careermate.services.weaviate_services.service;

import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.service.WeaviateImp;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AsyncWeaviateService {

    WeaviateImp weaviateImp;

    /**
     * Add job posting to Weaviate asynchronously
     * This improves API response time by 1-3 seconds
     * Uses dedicated weaviateTaskExecutor thread pool
     */
    @Async("weaviateTaskExecutor")
    public void addJobPostingToWeaviateAsync(JobPosting jobPosting) {
        try {
            weaviateImp.addJobPostingToWeaviate(jobPosting);
        } catch (Exception e) {
            log.error("Failed to add job posting ID: {} to Weaviate", jobPosting.getId(), e);
        }
    }

    /**
     * Update job posting in Weaviate asynchronously
     * Uses dedicated weaviateTaskExecutor thread pool
     */
    @Async("weaviateTaskExecutor")
    public void updateJobPostingAsync(int jobPostingId, JobPosting jobPosting) {
        try {
            weaviateImp.deleteJobPosting(jobPostingId);
            weaviateImp.addJobPostingToWeaviate(jobPosting);
        } catch (Exception e) {
            log.error("Failed to update job posting ID: {} in Weaviate", jobPostingId, e);
        }
    }

    /**
     * Delete job posting from Weaviate asynchronously
     * Uses dedicated weaviateTaskExecutor thread pool
     */
    @Async("weaviateTaskExecutor")
    public void deleteJobPostingAsync(int jobPostingId) {
        try {
            weaviateImp.deleteJobPosting(jobPostingId);
        } catch (Exception e) {
            log.error("Failed to delete job posting ID: {} from Weaviate", jobPostingId, e);
        }
    }
}

