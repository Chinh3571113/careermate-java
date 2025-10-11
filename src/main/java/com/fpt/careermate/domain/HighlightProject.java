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
@Entity(name = "highlight_project")
public class HighlightProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int highlightProjectId;

    String name;
    LocalDate startDate;
    LocalDate endDate;
    String description;
    String projectUrl;

    @ManyToOne
    @JoinColumn(name = "resumeId", nullable = false)
    Resume resume;
}
