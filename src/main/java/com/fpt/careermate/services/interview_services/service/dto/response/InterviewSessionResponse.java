package com.fpt.careermate.services.interview_services.service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterviewSessionResponse {
    int sessionId;
    int candidateId;
    String jobDescription;
    String status;
    LocalDateTime createdAt;
    LocalDateTime completedAt;
    String finalReport;
    Double averageScore;
    List<InterviewQuestionResponse> questions;
}
