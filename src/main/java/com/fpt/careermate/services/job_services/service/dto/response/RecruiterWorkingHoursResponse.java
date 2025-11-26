package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterWorkingHoursResponse {
    Integer id;
    Integer recruiterId;
    DayOfWeek dayOfWeek;
    Boolean isWorkingDay;
    LocalTime startTime;
    LocalTime endTime;
    LocalTime lunchBreakStart;
    LocalTime lunchBreakEnd;
    Integer bufferMinutesBetweenInterviews;
    Integer maxInterviewsPerDay;
    Integer totalWorkingMinutes;
}
