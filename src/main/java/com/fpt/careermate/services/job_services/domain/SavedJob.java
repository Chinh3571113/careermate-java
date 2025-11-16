package com.fpt.careermate.services.job_services.domain;

import com.fpt.careermate.services.admin_services.domain.Admin;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(
        name = "saved_job",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"candidate_id", "job_id"})
        })
public class SavedJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    JobPosting jobPosting;

    LocalDateTime savedAt;
}
