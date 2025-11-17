package com.fpt.careermate.services.job_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity
@Table(
        name = "job_description",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_posting_id", "skill_id"})
        }
)
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
