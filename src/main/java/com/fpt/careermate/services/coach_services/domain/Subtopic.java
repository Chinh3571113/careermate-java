package com.fpt.careermate.services.coach_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "subtopic")
public class Subtopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String name;
    String tags;
    @Column(columnDefinition = "TEXT")
    String description;
    @Column(columnDefinition = "TEXT")
    String resources;

    // Nhiều Subtopic thuộc về một Topic
    @ManyToOne
    @JoinColumn(name = "topic_id")
    Topic topic;

    @OneToMany(mappedBy = "subtopic", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ResumeSubtopicProgress> resumeSubtopicProgresses = new ArrayList<>();

    public Subtopic(String subtopic, String tags, String resources, String description) {
        this.name = subtopic;
        this.tags = tags;
        this.resources = resources;
        this.description = description;
    }
}
