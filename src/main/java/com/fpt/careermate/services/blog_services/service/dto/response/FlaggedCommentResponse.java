package com.fpt.careermate.services.blog_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Enhanced response DTO for flagged comments with moderation metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlaggedCommentResponse {
    Long id;
    Long blogId;
    String blogTitle;
    String content;
    String userEmail;
    String userName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Flagging information
    Boolean isFlagged;
    String flagReason;
    LocalDateTime flaggedAt;
    Boolean reviewedByAdmin;

    // Moderation metadata
    Integer severityScore; // 0-100
    String priorityLevel; // HIGH, MEDIUM, LOW
    Boolean isHidden;

    // Additional context
    Integer userCommentCount;
    Boolean userHasPreviousFlags;
}
