package com.fpt.careermate.services.resume_services.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeStatusRequest {
    @NotNull(message = "isActive must not be null")
    Boolean isActive;
}

