package com.fpt.careermate.services.coach_services.domain;

import com.fpt.careermate.common.constant.ResumeSubtopicProgressStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "resume_subtopic_progress")
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"resume_roadmap_id", "subtopic_id"})
})
public class ResumeSubtopicProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    @JoinColumn(name = "resume_roadmap_id", nullable = false)
    ResumeRoadmap resumeRoadmap;

    @ManyToOne
    @JoinColumn(name = "subtopic_id", nullable = false)
    Subtopic subtopic;

    OffsetDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ResumeSubtopicProgressStatus status;

}
