package com.fpt.careermate.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "industry_experiences")
@IdClass(IndustryExperienceId.class)
public class IndustryExperiences {
    @Id
    @Column(name = "field_name")
    String fieldName;

    @Id
    @Column(name = "candidate_id")
    Integer candidateId;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false, insertable = false, updatable = false)
    Candidate candidate;
}
