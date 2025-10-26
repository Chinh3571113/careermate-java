package com.fpt.careermate.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(nullable = false)
    String title;

    String content;

    boolean isMarked;

    @Column(nullable = false)
    int position;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    Module module;

}
