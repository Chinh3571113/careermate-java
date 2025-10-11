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
@Entity(name = "certificate")
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int certificateId;

    String name;
    String organization;
    LocalDate getDate;
    String certificateUrl;
    String description;

    @ManyToOne
    @JoinColumn(name = "resumeId", nullable = false)
    Resume resume;
}

