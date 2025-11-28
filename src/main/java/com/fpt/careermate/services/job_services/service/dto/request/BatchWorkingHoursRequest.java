package com.fpt.careermate.services.job_services.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Request to set working hours for multiple days at once
 * Useful for setting up a complete weekly schedule in one operation
 * 
 * Note: recruiterId is NO LONGER required in request body
 * Backend automatically gets recruiterId from JWT token
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatchWorkingHoursRequest {
    
    @NotEmpty(message = "At least one working hours configuration is required")
    @Valid
    List<RecruiterWorkingHoursRequest> workingHoursConfigurations;
    
    /**
     * If true, existing configurations not in this request will be marked as non-working days
     * If false, only specified days will be updated
     */
    Boolean replaceAll;
}
