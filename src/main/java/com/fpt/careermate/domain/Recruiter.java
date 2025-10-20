package com.fpt.careermate.domain;

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
@Entity(name = "recruiters")
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
    @Column(name = "business_license")
    String businessLicense;

    @Size(max = 100)
    @Column(name = "contact_person")
    String contactPerson;

    @Size(max = 20)
    @Column(name = "phone_number")
    String phoneNumber;

    @Size(max = 500)
    @Column(name = "company_address")
    String companyAddress;

    // One-to-one với Account
    @OneToOne
    @JoinColumn(name = "account_id", unique = true, nullable = false, updatable = false)
    private Account account;

    @OneToMany(mappedBy = "recruiter")
    List<JobPosting> jobPostings;
}
