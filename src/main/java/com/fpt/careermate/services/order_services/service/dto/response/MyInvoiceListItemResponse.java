package com.fpt.careermate.services.order_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MyInvoiceListItemResponse {
    int id;
    String packageName;
    Long amount;
    String status;
    LocalDate startDate;
    LocalDate endDate;
    LocalDate cancelledAt;
    boolean isActive;
}
