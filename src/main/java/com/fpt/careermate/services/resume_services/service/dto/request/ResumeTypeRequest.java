package com.fpt.careermate.services.resume_services.service.dto.request;

import com.fpt.careermate.common.constant.ResumeType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeTypeRequest {
    @NotNull(message = "type must not be null")
    ResumeType type;
}

