package com.fpt.careermate.services.job_services.service.dto.request;

import com.fpt.careermate.common.constant.TerminationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * DTO for recruiter confirming candidate's status update request.
 * Used when recruiter agrees with the candidate's claim.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterConfirmRequest {
    
    @NotNull(message = "Confirmation is required")
    Boolean confirmed;
    
    /**
     * Actual termination type (may differ from candidate's claim).
     * Recruiter can correct if needed.
     */
    TerminationType actualTerminationType;
    
    /**
     * Actual termination date (may differ from candidate's claim).
     */
    LocalDateTime actualTerminationDate;
    
    /**
     * Recruiter's notes about the confirmation.
     * E.g., "Confirmed. Employee resigned professionally with 2 weeks notice."
     */
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes;
}
