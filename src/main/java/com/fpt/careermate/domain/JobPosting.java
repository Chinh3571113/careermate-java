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

    @NotBlank
    String title;

    @NotBlank
    @Column(columnDefinition = "TEXT")
    String description;

    @NotBlank
    String address;

    @Min(0)
    Integer salaryFrom;

    @Min(0)
    Integer salaryTo;

    @Column(nullable = false)
    String status;

    @Column(nullable = false)
    LocalDate createdAt;

    @Column(nullable = false)
    LocalDate expirationDate;

    @OneToMany(mappedBy = "jobPosting")
    Set<JobDescription> jobDescriptions;

    @ManyToOne
    @JoinColumn(name = "recruiter_id", nullable = false)
    Recruiter recruiter;
}
