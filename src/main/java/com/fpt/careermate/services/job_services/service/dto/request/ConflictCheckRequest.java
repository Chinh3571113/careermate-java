package com.fpt.careermate.services.job_services.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictCheckRequest {
    
    @NotNull(message = "Recruiter ID is required")
    private Integer recruiterId;
    
    @NotNull(message = "Candidate ID is required")
    private Integer candidateId;
    
    @NotNull(message = "Proposed start time is required")
    private LocalDateTime proposedStartTime;
    
    @NotNull(message = "Duration in minutes is required")
    private Integer durationMinutes;
}
