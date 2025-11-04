package com.fpt.careermate.services.job_services.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobApplyRequest {
    int jobPostingId;
    int candidateId;

    @NotBlank(message = "CV file path is required")
    String cvFilePath;
    @NotBlank(message = "Full name is required")
    String fullName;
    @NotBlank(message = "Phone number is required")
    String phoneNumber;
    @NotBlank(message = "Preferred work location is required")
    String preferredWorkLocation;
    String coverLetter;
    @NotBlank(message = "Status is required")
    String status;
}
