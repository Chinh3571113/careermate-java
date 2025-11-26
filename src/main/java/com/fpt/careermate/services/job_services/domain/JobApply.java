package com.fpt.careermate.services.job_services.domain;

import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "job_apply")
public class JobApply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String cvFilePath;

    // Thông tin cá nhân
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String preferredWorkLocation;

    // Cover letter (optional)
    @Column(length = 500)
    private String coverLetter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobId", nullable = false)
    private JobPosting jobPosting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusJobApply status;
    
    // Original timestamps
    private LocalDateTime createAt;
    
    // Status transition timestamps for review eligibility tracking
    private LocalDateTime interviewScheduledAt;  // When interview was scheduled
    private LocalDateTime interviewedAt;         // When interview actually occurred
    private LocalDateTime hiredAt;               // When candidate was hired/employed
    private LocalDateTime leftAt;                // When employment ended
    private LocalDateTime lastContactAt;         // Last communication from company
    private LocalDateTime statusChangedAt;       // Last status update timestamp
    
    // Helper method to calculate days employed
    public Integer getDaysEmployed() {
        if (hiredAt == null) return null;
        LocalDateTime endDate = leftAt != null ? leftAt : LocalDateTime.now();
        return (int) java.time.Duration.between(hiredAt, endDate).toDays();
    }
    
    // Helper method to calculate days since application
    public Integer getDaysSinceApplication() {
        if (createAt == null) return null;
        return (int) java.time.Duration.between(createAt, LocalDateTime.now()).toDays();
    }
    
    // Helper method to check if candidate qualifies for application review
    public boolean canReviewApplication() {
        return getDaysSinceApplication() != null && getDaysSinceApplication() >= 7 
            && (status == StatusJobApply.SUBMITTED || status == StatusJobApply.NO_RESPONSE);
    }
    
    // Helper method to check if candidate qualifies for interview review
    public boolean canReviewInterview() {
        return interviewedAt != null;
    }
    
    // Helper method to check if candidate qualifies for work experience review
    public boolean canReviewWorkExperience() {
        return status == StatusJobApply.ACCEPTED 
            && getDaysEmployed() != null 
            && getDaysEmployed() >= 30;
    }
}
