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
@Entity(name = "topic")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String name;
    String tags;
    @Column(columnDefinition = "TEXT")
    String description;
    @Column(columnDefinition = "TEXT")
    String resources;

    // Nhiều Topic thuộc về một Roadmap
    @ManyToOne
    @JoinColumn(name = "roadmap_id")
    Roadmap roadmap;

    // Một Topic có nhiều Subtopic
    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Subtopic> subtopics = new ArrayList<>();

    public Topic(String topic, String tags, String resources, String description, Roadmap roadmap) {
        this.name = topic;
        this.tags = tags;
        this.description = description;
        this.resources = resources;
        this.roadmap = roadmap;
    }
}
