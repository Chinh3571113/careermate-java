package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Simplified response DTO for employment verification.
 * Privacy-focused: Only essential employment tracking for review eligibility.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmploymentVerificationResponse {

    Integer id;

    Integer jobApplyId;

    LocalDate startDate;

    LocalDate endDate;

    Boolean isActive;

    Integer daysEmployed;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    // Computed fields
    Boolean isEligibleForWorkReview;

    Boolean isCurrentlyEmployed;
}
