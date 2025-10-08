package com.fpt.careermate.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "job_descriptions")
public class JobDescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false)
    boolean mustToHave;

    @ManyToOne
    @JoinColumn(name = "job_posting_id", nullable = false)
    JobPosting jobPosting;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    JdSkill jdSkill;
}
