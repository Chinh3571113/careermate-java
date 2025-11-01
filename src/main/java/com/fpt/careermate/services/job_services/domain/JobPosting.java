package com.fpt.careermate.services.job_services.domain;

import com.fpt.careermate.services.profile_services.domain.WorkModel;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.account_services.domain.Account;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "job_posting")
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

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<JobApply> jobApplies = new HashSet<>();

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<JobFeedback> jobFeedbacks = new HashSet<>();

    @Column(nullable = false)
    int yearsOfExperience;

    String reason;
    String jobPackage;

    @Column(name = "salary_range")
    String salaryRange;

    String workModel;
}
