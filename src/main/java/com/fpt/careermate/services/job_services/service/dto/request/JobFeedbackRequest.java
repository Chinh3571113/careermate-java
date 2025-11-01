package com.fpt.careermate.services.job_services.service.dto.request;

import com.fpt.careermate.common.constant.FeedbackType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobFeedbackRequest {
    @NotNull(message = "Candidate ID is required")
    @Positive(message = "Candidate ID must be positive")
    int candidateId;

    @NotNull(message = "Job ID is required")
    @Positive(message = "Job ID must be positive")
    int jobId;

    @NotBlank(message = "Feedback type is required")
    @Size(max = 20, message = "Feedback type must not exceed 20 characters")
    @Pattern(regexp = "^(like|dislike|save|view)$",
             message = "Feedback type must be one of: like, dislike, save, view")
    String feedbackType;

    @DecimalMin(value = "0.0", message = "Score must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Score must not exceed 1.0")
    Double score;

}
