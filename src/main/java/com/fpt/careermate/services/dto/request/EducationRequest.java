package com.fpt.careermate.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EducationRequest {
    @NotBlank(message = "School name is required")
    String school;

    @NotBlank(message = "Major is required")
    String major;
    @NotBlank(message = "Degree is required")
    String degree;
    @NotBlank(message = "Description is required")
    LocalDate startDate;
    @NotBlank(message = "Description is required")
    LocalDate endDate;
}

