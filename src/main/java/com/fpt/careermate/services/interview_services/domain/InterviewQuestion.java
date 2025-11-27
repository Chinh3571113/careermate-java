package com.fpt.careermate.services.interview_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "interview_question")
public class InterviewQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int questionId;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    InterviewSession session;

    @Column(nullable = false)
    int questionNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    String question;

    @Column(columnDefinition = "TEXT")
    String candidateAnswer;

    Double score; // 0-10

    @Column(columnDefinition = "TEXT")
    String feedback;

    @CreationTimestamp
    @Column(updatable = false)
    LocalDateTime askedAt;

    LocalDateTime answeredAt;
}
