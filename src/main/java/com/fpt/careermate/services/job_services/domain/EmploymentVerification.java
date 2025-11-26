package com.fpt.careermate.services.job_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Simplified employment tracking for review eligibility only.
 * Privacy-focused: NO salary, contracts, benefits, or verification workflows.
 * Just tracks: started working, stopped working, duration.
 * 
 * @since v3.1 - Simplified Employment Tracking
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "employment_verification")
@Table(indexes = {
    @Index(name = "idx_job_apply_id", columnList = "job_apply_id"),
    @Index(name = "idx_is_active", columnList = "is_active")
})
public class EmploymentVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_apply_id", nullable = false, unique = true)
    JobApply jobApply;
    
    @Column(nullable = false)
    LocalDate startDate;
    
    LocalDate endDate;  // NULL = currently employed
    
    @Column(nullable = false)
    @Builder.Default
    Boolean isActive = true;
    
    Integer daysEmployed;  // Auto-calculated
    
    @Column(nullable = false)
    LocalDateTime createdAt;
    
    LocalDateTime updatedAt;
    
    /**
     * Calculate days employed
     */
    public Integer calculateDaysEmployed() {
        if (startDate == null) return null;
        LocalDate endDateOrNow = endDate != null ? endDate : LocalDate.now();
        return (int) ChronoUnit.DAYS.between(startDate, endDateOrNow);
    }
    
    /**
     * Check if employment is still active
     */
    public boolean isCurrentlyEmployed() {
        return isActive && endDate == null;
    }
    
    /**
     * Check if candidate is eligible for work experience review (30+ days employed)
     */
    public boolean isEligibleForWorkReview() {
        Integer days = daysEmployed != null ? daysEmployed : calculateDaysEmployed();
        return days != null && days >= 30;
    }
}
