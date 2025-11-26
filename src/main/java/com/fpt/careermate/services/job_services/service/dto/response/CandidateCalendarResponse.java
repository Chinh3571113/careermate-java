package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateCalendarResponse {
    Integer candidateId;
    LocalDate startDate;
    LocalDate endDate;
    Integer totalInterviews;
    List<InterviewScheduleResponse> upcomingInterviews;
}
