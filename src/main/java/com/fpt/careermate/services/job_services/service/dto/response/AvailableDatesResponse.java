package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableDatesResponse {
    private Integer recruiterId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationMinutes;
    private List<LocalDate> availableDates;
    private Integer totalDatesAvailable;
}
