package com.fpt.careermate.services.order_services.service.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
class OrderCreationRequest {

    @Positive
    int packageId;

    @Positive
    int candidateId;
}
