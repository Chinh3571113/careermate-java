package com.fpt.careermate.services.interview_services.domain;

import com.fpt.careermate.services.profile_services.domain.Candidate;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "interview_session")
public class InterviewSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int sessionId;

    @ManyToOne
    @JoinColumn(name = "candidate_id", nullable = false)
    Candidate candidate;

    @Column(columnDefinition = "TEXT", nullable = false)
    String jobDescription;

    @Column(nullable = false)
    String status; // ONGOING, COMPLETED

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime createdAt;

    LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    String finalReport;

    Double averageScore;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<InterviewQuestion> questions = new ArrayList<>();
}
