package com.fpt.careermate.services.resume_services.service.dto.request;

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
public class WorkExperienceRequest {
    @NotNull(message = "Resume ID is required")
    Integer resumeId;

    @NotBlank(message = "Job title is required")
    String jobTitle;
    @NotBlank(message = "Company name is required")
    String company;
    @NotBlank(message = "Start Date is required")
    LocalDate startDate;
    @NotBlank(message = "End Date is required")
    LocalDate endDate;
    String description;
    String project;
}
