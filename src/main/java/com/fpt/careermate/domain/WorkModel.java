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
@Entity(name = "work_models")
@IdClass(WorkModelId.class)
public class WorkModel {
    @Id
    @Column(name = "name")
    String name;

    @Id
    @Column(name = "candidate_id")
    Integer candidateId;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false, insertable = false, updatable = false)
    Candidate candidate;
}
