package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyCalendarResponse {
    Integer recruiterId;
    Integer year;
    Integer month;
    YearMonth yearMonth;
    Integer totalInterviews;
    Map<LocalDate, Integer> interviewCountByDate;  // Date -> Count
    Map<LocalDate, Boolean> workingDays;           // Date -> Is working day
    Map<LocalDate, Boolean> timeOffDays;           // Date -> Has time off
}
