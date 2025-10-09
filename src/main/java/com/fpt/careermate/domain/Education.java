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
@Entity(name = "education")
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int educationId;

    String school;
    String major;
    String degree;
    LocalDate startDate;
    LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "resumeId", nullable = false)
    Resume resume;
}
