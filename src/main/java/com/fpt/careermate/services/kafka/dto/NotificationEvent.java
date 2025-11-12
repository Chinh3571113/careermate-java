package com.fpt.careermate.services.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event DTO for Kafka notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType; // EMAIL, SMS, PUSH, etc.
    private String recipientId;
    private String recipientEmail;
    private String title;
    private String subject;
    private String message;
    private String category;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private Integer priority; // 1=HIGH, 2=MEDIUM, 3=LOW

    // Event types for notification system
    public enum EventType {
        // Job Posting Events
        JOB_POSTING_APPROVED,
        JOB_POSTING_REJECTED,

        // Application Events
        APPLICATION_RECEIVED,
        APPLICATION_STATUS_CHANGED,

        // Profile Events
        PROFILE_VERIFICATION,
        PROFILE_UPDATE_REQUEST,
        PROFILE_UPDATE_APPROVED,
        PROFILE_UPDATE_REJECTED,

        // Account Events
        ACCOUNT_APPROVED,
        ACCOUNT_REJECTED,

        // System Events
        SYSTEM_NOTIFICATION,

        // Test Events
        TEST_ADMIN_NOTIFICATION,
        TEST_RECRUITER_NOTIFICATION
    }
}

