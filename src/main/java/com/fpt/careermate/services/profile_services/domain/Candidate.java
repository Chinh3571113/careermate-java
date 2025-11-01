package com.fpt.careermate.services.profile_services.domain;

import com.fpt.careermate.services.coach_services.domain.Course;
import com.fpt.careermate.services.job_services.domain.JobFeedback;
import com.fpt.careermate.services.order_services.domain.Package;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.order_services.domain.Order;
import com.fpt.careermate.services.resume_services.domain.Resume;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    // One-to-many with Resume
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resume> resumes;

    // One-to-many with IndustryExperiences
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<IndustryExperiences> industryExperiences;

    // One-to-many with WorkModel
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkModel> workModels;

    // default: EAGER
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    Package currentPackage;

    @OneToMany(mappedBy = "candidate")
    List<Order> orders;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<JobApply> jobApplies = new HashSet<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<JobFeedback> jobFeedbacks = new HashSet<>();
}
