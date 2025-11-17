package com.fpt.careermate.services.resume_services.service.dto.request;

import com.fpt.careermate.common.constant.ResumeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeRequest {
    @NotNull(message = "About me must not be null")
    String aboutMe;
    @NotNull(message = "Resume URL must not be null")
    String resumeUrl;
    @NotNull(message = "Resume type must not be null")
    ResumeType type;
    @NotBlank(message = "isActive must not be blank")
    Boolean isActive;
}
