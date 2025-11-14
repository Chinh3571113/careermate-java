package com.fpt.careermate.services.order_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateOrderResponse {
    int id;
    String candidateName;
    long amount;
    String status;
    boolean isActive;
    LocalDate startDate;
    LocalDate endDate;
    String packageName;
}
