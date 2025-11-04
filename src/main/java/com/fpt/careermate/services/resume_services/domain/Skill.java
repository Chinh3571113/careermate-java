package com.fpt.careermate.services.resume_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "skill")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int skillId;
    @Column(nullable = false, length = 100, unique = true)
    String skillName;
    String skillType;
    Integer yearOfExperience;

    @ManyToOne
    @JoinColumn(name = "resumeId", nullable = false)
    Resume resume;
}
