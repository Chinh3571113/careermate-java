package com.fpt.careermate.services.job_services.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterWorkingHoursRequest {
    
    @NotNull(message = "Day of week is required")
    DayOfWeek dayOfWeek;
    
    @NotNull(message = "Working day flag is required")
    Boolean isWorkingDay;
    
    LocalTime startTime;  // Required if isWorkingDay = true
    LocalTime endTime;    // Required if isWorkingDay = true
    
    LocalTime lunchBreakStart;
    LocalTime lunchBreakEnd;
    
    Integer bufferMinutesBetweenInterviews;  // Default 15 minutes
    Integer maxInterviewsPerDay;             // Default 8 interviews
}
