package com.fpt.careermate.services.order_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EntitlementResponse {
    String name;
    String code;
    String unit;
    boolean hasLimit;
    boolean enabled;
    int limitValue;
}

