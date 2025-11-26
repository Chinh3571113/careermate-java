package com.fpt.careermate.services.job_services.domain;

import com.fpt.careermate.common.constant.InterviewType;
import com.fpt.careermate.common.constant.InterviewStatus;
import com.fpt.careermate.common.constant.InterviewOutcome;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Entity representing an interview schedule for a job application.
 * Supports interview scheduling, confirmation, reminders, and completion tracking.
 * 
 * @since v3.0 - Interview Scheduling System
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "interview_schedule")
@Table(indexes = {
    @Index(name = "idx_interview_schedule_date", columnList = "scheduled_date"),
    @Index(name = "idx_interview_schedule_status", columnList = "status"),
    @Index(name = "idx_interview_job_apply", columnList = "job_apply_id")
})
public class InterviewSchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_apply_id", nullable = false, unique = true)
    JobApply jobApply;
    
    @Column(nullable = false)
    Integer interviewRound;  // 1, 2, 3, etc. for multiple interview rounds
    
    // Interview details
    @Column(nullable = false)
    LocalDateTime scheduledDate;
    
    @Column(nullable = false)
    Integer durationMinutes;  // Expected duration (e.g., 60 minutes)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    InterviewType interviewType;  // IN_PERSON, VIDEO_CALL, PHONE, ONLINE_ASSESSMENT
    
    @Column(length = 500)
    String location;  // Physical address or video call link
    
    // Instructions
    @Column(length = 255)
    String interviewerName;
    
    @Column(length = 255)
    String interviewerEmail;
    
    @Column(length = 50)
    String interviewerPhone;
    
    @Column(length = 2000)
    String preparationNotes;  // What to bring, dress code, topics to prepare
    
    @Column(length = 500)
    String meetingLink;  // Zoom/Teams/Google Meet link
    
    // Status tracking
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    InterviewStatus status;  // SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW
    
    @Column(nullable = false)
    Boolean candidateConfirmed;
    
    LocalDateTime candidateConfirmedAt;
    
    // Reminders
    @Column(nullable = false)
    Boolean reminderSent24h;
    
    @Column(nullable = false)
    Boolean reminderSent2h;
    
    // Results (filled after interview)
    LocalDateTime interviewCompletedAt;
    
    @Column(length = 2000)
    String interviewerNotes;
    
    @Enumerated(EnumType.STRING)
    InterviewOutcome outcome;  // PASS, FAIL, PENDING, NEEDS_SECOND_ROUND
    
    // Metadata
    @Column(nullable = false)
    LocalDateTime createdAt;
    
    LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_recruiter_id", nullable = false)
    Recruiter createdByRecruiter;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = InterviewStatus.SCHEDULED;
        }
        if (interviewRound == null) {
            interviewRound = 1;
        }
        if (durationMinutes == null) {
            durationMinutes = 60;
        }
        if (candidateConfirmed == null) {
            candidateConfirmed = false;
        }
        if (reminderSent24h == null) {
            reminderSent24h = false;
        }
        if (reminderSent2h == null) {
            reminderSent2h = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Calculate the expected end time of the interview
     */
    public LocalDateTime getExpectedEndTime() {
        if (scheduledDate == null || durationMinutes == null) {
            return null;
        }
        return scheduledDate.plusMinutes(durationMinutes);
    }
    
    /**
     * Check if interview has passed its scheduled time
     */
    public boolean hasInterviewTimePassed() {
        LocalDateTime endTime = getExpectedEndTime();
        return endTime != null && LocalDateTime.now().isAfter(endTime);
    }
    
    /**
     * Check if interview is currently in progress
     */
    public boolean isInterviewInProgress() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = getExpectedEndTime();
        return scheduledDate != null && endTime != null 
            && now.isAfter(scheduledDate) && now.isBefore(endTime);
    }
    
    /**
     * Get hours until interview starts
     */
    public Long getHoursUntilInterview() {
        if (scheduledDate == null) {
            return null;
        }
        return java.time.Duration.between(LocalDateTime.now(), scheduledDate).toHours();
    }
    
    /**
     * Check if interview can be marked as completed
     */
    public boolean canMarkAsCompleted() {
        return status == InterviewStatus.SCHEDULED || status == InterviewStatus.CONFIRMED;
    }
}
