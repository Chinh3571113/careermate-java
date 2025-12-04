package com.fpt.careermate.services.job_services.service.dto.request;

import com.fpt.careermate.common.constant.InterviewType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * DTO for scheduling a new interview
 * 
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InterviewScheduleRequest {

    @NotNull(message = "Scheduled date is required")
    @FutureOrPresent(message = "Scheduled date must be in the future")
    LocalDateTime scheduledDate;

    @Positive(message = "Duration must be positive")
    Integer durationMinutes; // Defaults to 60 if not provided

    @NotNull(message = "Interview type is required")
    InterviewType interviewType;

    @Size(max = 500, message = "Location cannot exceed 500 characters")
    String location; // Address or virtual meeting link

    @Size(max = 200, message = "Interviewer name cannot exceed 200 characters")
    String interviewerName;

    @Size(max = 200, message = "Interviewer email cannot exceed 200 characters")
    String interviewerEmail;

    @Size(max = 20, message = "Interviewer phone cannot exceed 20 characters")
    String interviewerPhone;

    @Size(max = 2000, message = "Preparation notes cannot exceed 2000 characters")
    String preparationNotes; // Instructions for candidate

    @Size(max = 500, message = "Meeting link cannot exceed 500 characters")
    String meetingLink; // For video interviews

    Integer interviewRound; // Defaults to 1 if not provided

    // Optional - will be auto-filled from JWT token if not provided
    Integer createdByRecruiterId;
}
