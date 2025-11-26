package com.fpt.careermate.services.job_services.service.dto.request;

import com.fpt.careermate.services.job_services.domain.RecruiterTimeOff.TimeOffType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimeOffRequest {
    
    @NotNull(message = "Start date is required")
    LocalDate startDate;
    
    @NotNull(message = "End date is required")
    LocalDate endDate;
    
    @NotNull(message = "Time off type is required")
    TimeOffType timeOffType;
    
    String reason;
}
