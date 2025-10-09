package com.fpt.careermate.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ForeignLanguageRequest {
    @NotNull(message = "Language is required")
    String language;
    @NotNull(message = "Proficiency level is required")
    String level;
}

