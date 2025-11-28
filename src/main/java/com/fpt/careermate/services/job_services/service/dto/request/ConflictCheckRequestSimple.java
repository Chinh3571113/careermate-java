package com.fpt.careermate.services.job_services.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Simplified conflict check request for JWT-based endpoint.
 * recruiterId is extracted from JWT token, so not required in request body.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictCheckRequestSimple {
    
    @NotNull(message = "Candidate ID is required")
    private Integer candidateId;
    
    @NotNull(message = "Proposed start time is required")
    private LocalDateTime proposedStartTime;
    
    @NotNull(message = "Duration in minutes is required")
    private Integer durationMinutes;
}
