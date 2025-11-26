package com.fpt.careermate.services.job_services.service.dto.request;

import com.fpt.careermate.services.job_services.domain.InterviewRescheduleRequest.RequestedBy;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * DTO for requesting to reschedule an interview
 * 
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RescheduleInterviewRequest {

    @NotNull(message = "New requested date is required")
    @Future(message = "New date must be in the future")
    LocalDateTime newRequestedDate;

    @NotNull(message = "Reason is required")
    @Size(min = 10, max = 1000, message = "Reason must be between 10 and 1000 characters")
    String reason;

    @NotNull(message = "Requested by is required")
    RequestedBy requestedBy; // RECRUITER or CANDIDATE

    Boolean requiresConsent; // Defaults to true if not provided
}
