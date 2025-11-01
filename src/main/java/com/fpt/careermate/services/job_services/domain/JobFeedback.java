package com.fpt.careermate.services.job_services.domain;

import com.fpt.careermate.services.profile_services.domain.Candidate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "job_feedback")
public class JobFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPosting jobPosting;


    @Size(max = 20)
    @NotNull
    @Column(name = "feedback_type", nullable = false, length = 20)
    private String feedbackType;

    @ColumnDefault("1.0")
    @Column(name = "score")
    private Double score;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "create_at")
    private LocalDateTime createAt;

}