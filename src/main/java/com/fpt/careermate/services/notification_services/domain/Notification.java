package com.fpt.careermate.services.notification_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Notification entity for storing Kafka notifications in database
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_recipient_created", columnList = "recipient_id, created_at DESC"),
        @Index(name = "idx_recipient_read", columnList = "recipient_id, is_read"),
        @Index(name = "idx_event_id", columnList = "event_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "event_id", unique = true, nullable = false)
    String eventId;

    @Column(name = "event_type", nullable = false)
    String eventType;

    @Column(name = "recipient_id", nullable = false)
    String recipientId;

    @Column(name = "recipient_email")
    String recipientEmail;

    @Column(nullable = false)
    String title;

    String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    String message;

    String category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    Map<String, Object> metadata;

    @Column(nullable = false)
    @Builder.Default
    Integer priority = 2; // 1=HIGH, 2=MEDIUM, 3=LOW (default MEDIUM)

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    Boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "read_at")
    LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
        if (priority == null) {
            priority = 2; // Default to MEDIUM priority
        }
    }
}
