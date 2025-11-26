package com.fpt.careermate.services.review_services.domain;

import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Company review left by candidates based on their experience with the company
 * Supports staged reviews: application process, interview, or work experience
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "company_review")
public class CompanyReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    // Core relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private Recruiter recruiter;  // Company being reviewed
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_apply_id", nullable = false)
    private JobApply jobApply;  // The application that qualifies them to review
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPosting;  // Position they applied for
    
    // Review metadata
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType reviewType;  // APPLICATION, INTERVIEW, or WORK_EXPERIENCE
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;
    
    // Review content
    @Column(nullable = false, length = 2000)
    private String reviewText;
    
    @Column(nullable = false)
    private Integer overallRating;  // 1-5 stars
    
    // Aspect-specific ratings (optional, depends on review type)
    private Integer communicationRating;      // All types
    private Integer responsivenessRating;     // APPLICATION, INTERVIEW
    private Integer interviewProcessRating;   // INTERVIEW, WORK_EXPERIENCE
    private Integer workCultureRating;        // WORK_EXPERIENCE only
    private Integer managementRating;         // WORK_EXPERIENCE only
    private Integer benefitsRating;           // WORK_EXPERIENCE only
    private Integer workLifeBalanceRating;    // WORK_EXPERIENCE only
    
    // Metadata
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Column(nullable = false)
    private Boolean isAnonymous;  // Whether to hide candidate name
    
    @Column(nullable = false)
    private Boolean isVerified;  // Verified through system (has JobApply record)
    
    // Moderation
    private Integer flagCount;  // Number of times flagged by users
    private String removalReason;  // If status = REMOVED, why?
    
    // Weaviate integration
    @Column(unique = true)
    private String weaviateId;  // UUID for semantic search/validation
    
    private Double sentimentScore;  // AI-calculated sentiment (-1 to 1)
    
    @Column(nullable = true)
    private String duplicateCheckHash;  // Hash for duplicate detection
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = ReviewStatus.ACTIVE;
        }
        if (isVerified == null) {
            isVerified = true;  // All reviews through system are verified
        }
        if (flagCount == null) {
            flagCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
