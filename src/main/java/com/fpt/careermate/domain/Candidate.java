package com.fpt.careermate.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "candidate")
public class Candidate extends BaseUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int candidateId;

    String title;
    String jobLevel;

    @Column(name = "experience")
    Integer experience;

    String link;

    // One-to-one với Account
    @OneToOne
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    // One-to-many with IndustryExperiences
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndustryExperiences> industryExperiences;

    // One-to-many with WorkModel
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkModel> workModels;
}
