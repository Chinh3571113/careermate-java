package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterSchedulingStatsResponse {
    Integer recruiterId;
    LocalDate startDate;
    LocalDate endDate;
    
    // Interview counts
    Integer totalInterviewsScheduled;
    Integer completedInterviews;
    Integer cancelledInterviews;
    Integer noShowInterviews;
    Integer rescheduledInterviews;
    
    // Time metrics
    Integer totalInterviewHours;
    Integer averageInterviewDurationMinutes;
    Double utilizationRate;  // Percentage of working hours used for interviews
    
    // Busiest times
    String busiestDayOfWeek;
    String busiestTimeSlot;
    LocalDate busiestDate;
    
    // Outcomes
    Integer passedInterviews;
    Integer failedInterviews;
    Double passRate;
    
    // Distribution
    Map<String, Integer> interviewsByType;  // Type -> Count
    Map<String, Integer> interviewsByStatus; // Status -> Count
}
