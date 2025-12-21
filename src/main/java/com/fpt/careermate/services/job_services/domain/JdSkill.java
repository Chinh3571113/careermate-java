package com.fpt.careermate.services.job_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "jd_skill")
public class JdSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(length = 100, nullable = false)
    String name;
    String type;

    @OneToMany(mappedBy = "jdSkill")
    Set<JobDescription> jobDescriptions;

}
