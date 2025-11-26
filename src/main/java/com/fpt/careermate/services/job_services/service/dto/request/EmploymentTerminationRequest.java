package com.fpt.careermate.services.job_services.service.dto.request;

import com.fpt.careermate.common.constant.TerminationType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/**
 * Request DTO for updating employment termination details.
 * Used when employment ends.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmploymentTerminationRequest {
    
    @NotNull(message = "Termination type is required")
    TerminationType terminationType;
    
    @NotNull(message = "Termination date is required")
    LocalDate terminationDate;
    
    String reason;  // Optional explanation
}
