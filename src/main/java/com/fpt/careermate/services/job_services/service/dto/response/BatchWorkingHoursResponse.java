package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;

/**
 * Response for batch working hours update operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BatchWorkingHoursResponse {
    
    Integer recruiterId;
    
    Integer totalConfigurations;
    
    Integer successfulUpdates;
    
    Integer failedUpdates;
    
    List<RecruiterWorkingHoursResponse> updatedConfigurations;
    
    /**
     * Map of day of week to error message for failed updates
     */
    Map<String, String> errors;
}
