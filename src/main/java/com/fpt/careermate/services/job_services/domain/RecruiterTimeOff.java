package com.fpt.careermate.services.job_services.domain;

import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tracks recruiter time-off periods (vacation, sick leave, holidays, etc.)
 * Used to block calendar availability during time-off
 * 
 * @since 1.1 - Calendar Feature
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "recruiter_time_off")
public class RecruiterTimeOff {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "time_off_id")
    Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    Recruiter recruiter;
    
    // Time off period
    @Column(name = "start_date", nullable = false)
    LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    LocalDate endDate;
    
    // Type of time off
    @Enumerated(EnumType.STRING)
    @Column(name = "time_off_type", nullable = false)
    TimeOffType timeOffType;
    
    // Details
    @Column(name = "reason", length = 500)
    String reason;
    
    @Column(name = "is_approved", nullable = false)
    Boolean isApproved;
    
    @Column(name = "approved_by_admin_id")
    Integer approvedByAdminId;
    
    @Column(name = "approved_at")
    LocalDateTime approvedAt;
    
    // Metadata
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isApproved == null) {
            isApproved = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Check if a date falls within this time-off period
     */
    public boolean includesDate(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * Time off types
     */
    public enum TimeOffType {
        VACATION,
        SICK_LEAVE,
        PERSONAL_DAY,
        PUBLIC_HOLIDAY,
        COMPANY_EVENT,
        TRAINING,
        OTHER
    }
}
