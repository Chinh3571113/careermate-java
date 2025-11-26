package com.fpt.careermate.services.job_services.domain;

import com.fpt.careermate.common.constant.StatusJobApply;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Tracks all status changes for job applications to provide audit trail
 * and support review eligibility calculations
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "job_apply_status_history")
public class JobApplyStatusHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_apply_id", nullable = false)
    private JobApply jobApply;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private StatusJobApply previousStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusJobApply newStatus;
    
    @Column(nullable = false)
    private LocalDateTime changedAt;
    
    @Column(nullable = true)
    private Integer changedByUserId;  // User who made the change (recruiter/admin)
    
    @Column(length = 500)
    private String changeReason;  // Optional reason for status change
    
    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}
