package com.fpt.careermate.services.job_services.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/**
 * Simplified request DTO for creating employment verification record.
 * Used when candidate is hired - just tracks start date.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmploymentVerificationRequest {

    @NotNull(message = "Start date is required")
    LocalDate startDate;
}
