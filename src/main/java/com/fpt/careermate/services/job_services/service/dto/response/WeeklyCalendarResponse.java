package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeeklyCalendarResponse {
    Integer recruiterId;
    LocalDate weekStartDate;
    LocalDate weekEndDate;
    Integer totalInterviews;
    Map<LocalDate, DailyCalendarResponse> dailyCalendars;  // Date -> Daily view
    List<InterviewScheduleResponse> allInterviews;
}
