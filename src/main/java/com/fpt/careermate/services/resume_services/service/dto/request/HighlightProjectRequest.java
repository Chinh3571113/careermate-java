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
public class HighlightProjectRequest {
    @NotNull(message = "Resume ID is required")
    Integer resumeId;

    @NotBlank(message = "Project name is required")
    String name;

    @NotNull(message = "Start date is required")
    LocalDate startDate;

    @NotNull(message = "End date is required")
    LocalDate endDate;

    String description;
    String projectUrl;
}
