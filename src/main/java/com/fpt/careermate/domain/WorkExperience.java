package com.fpt.careermate.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "work_experience")
public class WorkExperience {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int workExperienceId;

    String jobTitle;
    String company;
    LocalDate startDate;
    LocalDate endDate;
    String description;
    String project;

    @ManyToOne
    @JoinColumn(name = "resumeId", nullable = false)
    Resume resume;
}
