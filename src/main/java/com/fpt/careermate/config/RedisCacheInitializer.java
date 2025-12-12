package com.fpt.careermate.config;

import com.fpt.careermate.services.job_services.service.AdminJobPostingRedisService;
import com.fpt.careermate.services.job_services.service.CandidateJobPostingRedisService;
import com.fpt.careermate.services.job_services.service.RecruiterJobPostingRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Initialize Redis cache on application startup
 * Clear old cache data to prevent deserialization issues after config changes
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Order(100) // Run after seeders
public class RedisCacheInitializer implements CommandLineRunner {

    private final RecruiterJobPostingRedisService recruiterJobPostingRedisService;
    private final CandidateJobPostingRedisService candidateJobPostingRedisService;
    private final AdminJobPostingRedisService adminJobPostingRedisService;

    @Override
    public void run(String... args) {
        try {
            log.info("Initializing Redis cache...");
            candidateJobPostingRedisService.clearAllCandidateListCache();
            adminJobPostingRedisService.clearAllAdminListCache();
            recruiterJobPostingRedisService.clearAllRecruiterListCache();
        } catch (Exception e) {
            log.warn("Failed to initialize Redis cache: {}", e.getMessage());
            // Don't fail application startup if Redis is not available
        }
    }
}

