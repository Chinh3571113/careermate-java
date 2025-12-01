package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyCalendarResponse {
    Integer recruiterId;
    LocalDate date;
    String dayOfWeek;
    Boolean isWorkingDay;
    LocalTime workStartTime;
    LocalTime workEndTime;
    LocalTime lunchBreakStart;
    LocalTime lunchBreakEnd;
    Boolean hasTimeOff;
    String timeOffReason;
    Integer totalInterviews;
    Integer availableSlots;
    List<InterviewScheduleResponse> interviews;
    List<LocalTime> availableTimeSlots;
}
