package com.fpt.careermate.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkExperienceRequest {
    @NotBlank(message = "Job title is required")
    String jobTitle;

    String company;
    LocalDate startDate;
    LocalDate endDate;
    String description;
    String project;
}

