package com.fpt.careermate.services.job_services.domain;

import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * Defines working hours, breaks, and time-off for recruiters
 * Supports calendar scheduling and availability calculation
 * 
 * @since 1.1 - Calendar Feature
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "recruiter_working_hours")
public class RecruiterWorkingHours {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "working_hours_id")
    Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    Recruiter recruiter;
    
    // Day of week configuration
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    DayOfWeek dayOfWeek;  // MONDAY, TUESDAY, etc.
    
    @Column(nullable = false)
    Boolean isWorkingDay;  // false = day off
    
    // Working hours for the day
    @Column(name = "start_time")
    LocalTime startTime;  // e.g., 09:00
    
    @Column(name = "end_time")
    LocalTime endTime;  // e.g., 17:00
    
    // Break times (optional)
    @Column(name = "lunch_break_start")
    LocalTime lunchBreakStart;  // e.g., 12:00
    
    @Column(name = "lunch_break_end")
    LocalTime lunchBreakEnd;  // e.g., 13:00
    
    // Scheduling constraints
    @Column(name = "buffer_minutes")
    Integer bufferMinutesBetweenInterviews;  // e.g., 15 min between interviews
    
    @Column(name = "max_interviews_per_day")
    Integer maxInterviewsPerDay;  // e.g., 8 interviews max
    
    // Metadata
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if a time falls within working hours
     */
    public boolean isWithinWorkingHours(LocalTime time) {
        if (!isWorkingDay || startTime == null || endTime == null) {
            return false;
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
    
    /**
     * Check if a time falls within lunch break
     */
    public boolean isDuringLunchBreak(LocalTime time) {
        if (lunchBreakStart == null || lunchBreakEnd == null) {
            return false;
        }
        return !time.isBefore(lunchBreakStart) && !time.isAfter(lunchBreakEnd);
    }
    
    /**
     * Calculate total working hours for the day
     */
    public Integer getTotalWorkingMinutes() {
        if (!isWorkingDay || startTime == null || endTime == null) {
            return 0;
        }
        
        int totalMinutes = (int) java.time.Duration.between(startTime, endTime).toMinutes();
        
        // Subtract lunch break if configured
        if (lunchBreakStart != null && lunchBreakEnd != null) {
            int lunchMinutes = (int) java.time.Duration.between(lunchBreakStart, lunchBreakEnd).toMinutes();
            totalMinutes -= lunchMinutes;
        }
        
        return totalMinutes;
    }
}
