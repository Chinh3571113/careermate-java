package com.fpt.careermate.services.job_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Audit trail for job posting changes.
 * Tracks all modifications to job postings for compliance and transparency.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "job_posting_audit")
@Table(name = "job_posting_audit")
public class JobPostingAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    
    @Column(nullable = false)
    int jobPostingId;
    
    @Column(nullable = false)
    String jobTitle;
    
    @Column(nullable = false)
    int recruiterId;
    
    @Column(nullable = false)
    String actionType; // CREATE, UPDATE_FULL, UPDATE_EXPIRATION, DELETE, PAUSE, RESUME, APPROVE, REJECT
    
    @Column(columnDefinition = "TEXT")
    String fieldChanged; // Which field was changed (e.g., "expirationDate", "title", "skills")
    
    @Column(columnDefinition = "TEXT")
    String oldValue;
    
    @Column(columnDefinition = "TEXT")
    String newValue;
    
    @Column(nullable = false)
    LocalDateTime changedAt;
    
    @Column
    int applicantsCountAtChange; // Number of applicants when change was made
    
    @Column(columnDefinition = "TEXT")
    String reason; // Optional reason for the change
}
