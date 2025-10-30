package com.fpt.careermate.services.blog_services.domain;

import com.fpt.careermate.services.admin_services.domain.Admin;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "blog")
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 500)
    String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    String content;

    @Column(length = 1000)
    String summary;

    @Column(name = "thumbnail_url", length = 1000)
    String thumbnailUrl;

    @Column(length = 100)
    String category;

    @Column(length = 500)
    String tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BlogStatus status;

    @Column(name = "view_count")
    Integer viewCount;

    @Column(name = "average_rating")
    Double averageRating;

    @Column(name = "rating_count")
    Integer ratingCount;

    @Column(name = "comment_count")
    Integer commentCount;

    @Column(name = "published_at")
    LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminid", nullable = false)
    Admin admin;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (viewCount == null) {
            viewCount = 0;
        }
        if (status == null) {
            status = BlogStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum BlogStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }
}
