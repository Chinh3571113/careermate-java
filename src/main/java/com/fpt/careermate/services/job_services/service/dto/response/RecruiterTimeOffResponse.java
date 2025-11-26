package com.fpt.careermate.services.job_services.service.dto.response;

import com.fpt.careermate.services.job_services.domain.RecruiterTimeOff.TimeOffType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterTimeOffResponse {
    Integer id;
    Integer recruiterId;
    LocalDate startDate;
    LocalDate endDate;
    TimeOffType timeOffType;
    String reason;
    Boolean isApproved;
    Integer approvedByAdminId;
    LocalDateTime approvedAt;
    LocalDateTime createdAt;
}
