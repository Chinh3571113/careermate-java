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

    @Column(unique = true, nullable = false, length = 600)
    String slug;

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

    /**
     * Calculate reading time based on average 200 words per minute
     */
    @Transient
    public int getReadingTimeMinutes() {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        int wordCount = content.split("\\s+").length;
        int minutes = wordCount / 200;
        return minutes < 1 ? 1 : minutes; // Minimum 1 minute
    }

    /**
     * Generate SEO-friendly slug from title
     */
    public static String generateSlug(String title) {
        if (title == null || title.isEmpty()) {
            return "";
        }
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special characters
                .replaceAll("\\s+", "-")           // Replace spaces with hyphens
                .replaceAll("-+", "-")              // Remove duplicate hyphens
                .replaceAll("^-|-$", "");           // Trim hyphens from start/end
    }

    public enum BlogStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }
}
