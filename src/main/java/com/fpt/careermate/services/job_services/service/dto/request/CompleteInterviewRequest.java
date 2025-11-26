package com.fpt.careermate.services.job_services.service.dto.request;

import com.fpt.careermate.common.constant.InterviewOutcome;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO for completing an interview
 * 
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteInterviewRequest {

    @NotNull(message = "Outcome is required")
    InterviewOutcome outcome; // PASS, FAIL, PENDING, NEEDS_SECOND_ROUND

    @Size(max = 2000, message = "Interviewer notes cannot exceed 2000 characters")
    String interviewerNotes;
}
