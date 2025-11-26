package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestedTimesResponse {
    private Integer recruiterId;
    private LocalDate date;
    private Integer durationMinutes;
    private List<LocalTime> suggestedTimes;
}
