package com.fpt.careermate.services.admin_services.service;

import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.admin_services.service.dto.response.DashboardStatsResponse;
import com.fpt.careermate.services.blog_services.repository.BlogCommentRepo;
import com.fpt.careermate.services.blog_services.repository.BlogRepo;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.job_services.repository.JobPostingRepo;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterProfileUpdateRequestRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminDashboardService {

    AccountRepo accountRepo;
    BlogRepo blogRepo;
    JobPostingRepo jobPostingRepo;
    JobApplyRepo jobApplyRepo;
    RecruiterProfileUpdateRequestRepo recruiterUpdateRequestRepo;
    BlogCommentRepo blogCommentRepo;
    Map<String, HealthIndicator> healthIndicators;

    public DashboardStatsResponse getAllDashboardStats() {
        log.info("Fetching comprehensive dashboard statistics");

        try {
            return DashboardStatsResponse.builder()
                    // User counts by role
                    .totalUsers(accountRepo.count())
                    .totalCandidates(countByRole("CANDIDATE"))
                    .totalRecruiters(countByRole("RECRUITER"))
                    .totalAdmins(countByRole("ADMIN"))

                    // Account status counts
                    .activeAccounts(countByStatus("ACTIVE"))
                    .pendingAccounts(countByStatus("PENDING"))
                    .bannedAccounts(countByStatus("BANNED"))
                    .rejectedAccounts(countByStatus("REJECTED"))

                    // Content counts
                    .totalBlogs(blogRepo.count())
                    .totalJobPostings(jobPostingRepo.count())
                    .totalApplications(jobApplyRepo.count())

                    // Moderation counts
                    .pendingRecruiterApprovals(recruiterUpdateRequestRepo.countByStatus("PENDING"))
                    .flaggedComments(blogCommentRepo.countByIsFlaggedTrue())
                    .flaggedRatings(0L) // BlogRating doesn't have flagged field

                    // System health
                    .databaseStatus(getComponentStatus("db"))
                    .kafkaStatus(getComponentStatus("kafka"))
                    .weaviateStatus(getComponentStatus("weaviate"))
                    .emailStatus(getComponentStatus("email"))
                    .firebaseStatus(getComponentStatus("firebase"))
                    .systemStatus(isSystemHealthy() ? "UP" : "DOWN")
                    .build();

        } catch (Exception e) {
            log.error("Error fetching dashboard statistics", e);
            throw new RuntimeException("Failed to fetch dashboard statistics: " + e.getMessage());
        }
    }

    private Long countByRole(String roleName) {
        try {
            return accountRepo.countByRoleName(roleName);
        } catch (Exception e) {
            log.error("Error counting users by role: {}", roleName, e);
            return 0L;
        }
    }

    private Long countByStatus(String status) {
        try {
            return accountRepo.countByStatus(status);
        } catch (Exception e) {
            log.error("Error counting users by status: {}", status, e);
            return 0L;
        }
    }

    private String getComponentStatus(String componentName) {
        try {
            // For database, do a simple repository check instead of health indicator
            if ("db".equals(componentName)) {
                accountRepo.count(); // If this works, DB is up
                return "UP";
            }

            // For other components, use health indicators
            if (healthIndicators.containsKey(componentName)) {
                var health = healthIndicators.get(componentName).health();
                return health.getStatus().getCode();
            }
            return "UNKNOWN";
        } catch (Exception e) {
            log.error("Error checking {} health", componentName, e);
            return "DOWN";
        }
    }

    private boolean isSystemHealthy() {
        // System is healthy if all critical components are UP
        try {
            String dbStatus = getComponentStatus("db");
            String kafkaStatus = getComponentStatus("kafka");
            String weaviateStatus = getComponentStatus("weaviate");
            String emailStatus = getComponentStatus("email");
            String firebaseStatus = getComponentStatus("firebase");

            // All critical services must be UP
            return "UP".equals(dbStatus) &&
                    "UP".equals(kafkaStatus) &&
                    "UP".equals(weaviateStatus) &&
                    "UP".equals(emailStatus) &&
                    "UP".equals(firebaseStatus);
        } catch (Exception e) {
            log.error("Error checking system health", e);
            return false;
        }
    }
}
