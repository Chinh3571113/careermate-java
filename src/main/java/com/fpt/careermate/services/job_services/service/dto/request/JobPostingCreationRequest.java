package com.fpt.careermate.services.job_services.service.dto.request;

import com.fpt.careermate.common.constant.WorkModel;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostingCreationRequest {

    @Size(max = 100)
    @NotBlank
    String title;

    @Size(max = 5000)
    @NotBlank
    String description;

    @NotBlank
    String address;

    @NotNull
    LocalDate expirationDate;

    @NotNull
    Set<JdSkillRequest> jdSkills;

    @NotNull
    @Min(0)
    @Max(100)
    int yearsOfExperience;

    @NotNull
    WorkModel workModel;

    @NotBlank
    String salaryRange;

    @NotBlank
    String reason;

    @NotBlank
    String jobPackage;
}
