package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobFeedbackResponse {
    int id;
    int candidateId;
    String candidateName;
    int jobId;
    String jobTitle;
    String feedbackType;
    Double score;
    LocalDateTime createdAt;
}
