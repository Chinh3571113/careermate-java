package com.fpt.careermate.services.job_services.service.dto.request;

import com.fpt.careermate.common.constant.InterviewType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * DTO for updating an existing interview schedule.
 * All fields are optional - only provided fields will be updated.
 * Used for rescheduling interviews or updating interview details.
 * 
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateInterviewRequest {

    @Future(message = "Scheduled date must be in the future")
    LocalDateTime scheduledDate;

    @Positive(message = "Duration must be positive")
    Integer durationMinutes;

    InterviewType interviewType;

    @Size(max = 500, message = "Location cannot exceed 500 characters")
    String location;

    @Size(max = 200, message = "Interviewer name cannot exceed 200 characters")
    String interviewerName;

    @Size(max = 200, message = "Interviewer email cannot exceed 200 characters")
    String interviewerEmail;

    @Size(max = 20, message = "Interviewer phone cannot exceed 20 characters")
    String interviewerPhone;

    @Size(max = 2000, message = "Preparation notes cannot exceed 2000 characters")
    String preparationNotes;

    @Size(max = 500, message = "Meeting link cannot exceed 500 characters")
    String meetingLink;

    Integer interviewRound;

    @Size(max = 1000, message = "Update reason cannot exceed 1000 characters")
    String updateReason; // Optional reason for the update
}
