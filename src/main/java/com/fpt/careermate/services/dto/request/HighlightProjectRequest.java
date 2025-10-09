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
public class HighlightProjectRequest {
    @NotBlank(message = "Project name is required")
    String name;
    @NotBlank(message = "Date is required")
    LocalDate startDate;
    @NotBlank(message = "Date is required")
    LocalDate endDate;
    String description;
    String projectUrl;
}

