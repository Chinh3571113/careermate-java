package com.fpt.careermate.services.recruiter_services.domain;

import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(name = "recruiters")
public class Recruiter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Size(max = 200)
    @Column(nullable = false)
    String companyName;

    @Size(max = 255)
    @Column(nullable = false)
    String website;

    @Size(max = 500)
    @Column(name = "logo_url", nullable = false)
    String logoUrl;

    @Column(columnDefinition = "TEXT", nullable = false)
    String about;

    @Min(0)
    @Max(5)
    @Column(columnDefinition = "FLOAT DEFAULT 0.0")
    Float rating;

    @Size(max = 100)
    @Column(name = "company_email")
    String companyEmail;

    @Size(max = 100)
    @Column(name = "contact_person")
    String contactPerson;

    @Size(max = 20)
    @Column(name = "phone_number")
    String phoneNumber;

    @Size(max = 500)
    @Column(name = "company_address")
    String companyAddress;

    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    String verificationStatus = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "rejection_reason")
    String rejectionReason;

    // One-to-one với Account
    @OneToOne
    @JoinColumn(name = "account_id", unique = true, nullable = false, updatable = false)
    private Account account;

    @OneToMany(mappedBy = "recruiter")
    List<JobPosting> jobPostings;
}
