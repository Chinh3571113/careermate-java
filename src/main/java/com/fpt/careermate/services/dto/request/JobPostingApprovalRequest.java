package com.fpt.careermate.services.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostingApprovalRequest {

    @NotBlank(message = "Status is required")
    String status; // APPROVED or REJECTED

    @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
    String rejectionReason; // Required if status is REJECTED
}
