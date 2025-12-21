package com.fpt.careermate.services.coach_services.domain;

import com.fpt.careermate.services.resume_services.domain.Resume;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "resume_roadmap")
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"resume_id", "roadmap_id"})
})
public class ResumeRoadmap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    Resume resume;

    @ManyToOne
    @JoinColumn(name = "roadmap_id", nullable = false)
    Roadmap roadmap;

    boolean isActive;

    @CreationTimestamp
    OffsetDateTime createdAt;

    @OneToMany(mappedBy = "resumeRoadmap", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ResumeSubtopicProgress> resumeSubtopicProgresses  = new ArrayList<>();
}
