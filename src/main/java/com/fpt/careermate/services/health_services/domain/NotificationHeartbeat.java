package com.fpt.careermate.services.health_services.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "notification_heartbeat")
public class NotificationHeartbeat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    
    @Column(unique = true, nullable = false)
    String name;
    
    @Column(name = "last_processed_at", nullable = false)
    Instant lastProcessedAt;
    
    @Column(name = "message_count")
    @Builder.Default
    long messageCount = 0L;
    
    @Column(name = "error_count")
    @Builder.Default
    long errorCount = 0L;
    
    @Column(name = "last_error_message")
    String lastErrorMessage;
}
