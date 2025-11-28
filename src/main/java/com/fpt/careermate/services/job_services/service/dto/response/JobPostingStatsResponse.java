package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostingStatsResponse {
    
    // Job posting counts by status
    long totalJobPostings;
    long pendingJobPostings;
    long activeJobPostings;
    long rejectedJobPostings;
    long pausedJobPostings;
    long expiredJobPostings;
    long deletedJobPostings;
    
    // Application counts
    long totalApplications;
    long submittedApplications;
    long reviewingApplications;
    long approvedApplications;
    long rejectedApplications;
    long interviewScheduledApplications;
    long hiredApplications;
    long withdrawnApplications;
}
