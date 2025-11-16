package com.fpt.careermate.services.notification_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long id;
    String eventId;
    String eventType;
    String recipientId;
    String title;
    String message;
    String category;
    Map<String, Object> metadata;
    Integer priority; // 1=HIGH, 2=MEDIUM, 3=LOW
    Boolean isRead;
    LocalDateTime createdAt;
    LocalDateTime readAt;
}
