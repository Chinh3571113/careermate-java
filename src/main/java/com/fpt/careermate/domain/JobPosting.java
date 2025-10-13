package com.fpt.careermate.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "job_postings")
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false, unique = true)
    String title;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    String description;

    @Column(nullable = false)
    String address;

    /**
     * Job posting status:
     * PENDING - waiting for admin review
     * APPROVED - approved by admin
     * REJECTED - rejected by admin
     * ACTIVE - visible to candidates
     * PAUSED - temporarily hidden
     * EXPIRED - past expiration date
     */
    @Column(nullable = false)
    String status;

    @Column(nullable = false)
    LocalDate expirationDate;

    @Column(nullable = false)
    LocalDate createAt;

    @Column(columnDefinition = "TEXT")
    String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    Account approvedBy;

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL)
    Set<JobDescription> jobDescriptions;

    @ManyToOne
    @JoinColumn(name = "recruiter_id", nullable = false)
    Recruiter recruiter;
}
