package com.fpt.careermate.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkillRequest {
    @NotBlank(message = "Skill type is required")
    String skillType;

    Integer yearOfExperience;
}

