package com.fpt.careermate.services.admin_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardStatsResponse {

    // User Statistics
    Long totalUsers;
    Long totalCandidates;
    Long totalRecruiters;
    Long totalAdmins;

    // Account Status Statistics
    Long activeAccounts;
    Long pendingAccounts;
    Long bannedAccounts;
    Long rejectedAccounts;

    // Content Statistics
    Long totalBlogs;
    Long totalJobPostings;
    Long totalApplications;

    // Moderation Statistics
    Long pendingRecruiterApprovals;
    Long flaggedComments;
    Long flaggedRatings;

    // System Health
    String databaseStatus; // "UP" or "DOWN"
    String kafkaStatus; // "UP" or "DOWN"
    String weaviateStatus; // "UP" or "DOWN"
    String emailStatus; // "UP" or "DOWN"
    String firebaseStatus; // "UP" or "DOWN"
    String systemStatus; // "UP" or "DOWN"
}
