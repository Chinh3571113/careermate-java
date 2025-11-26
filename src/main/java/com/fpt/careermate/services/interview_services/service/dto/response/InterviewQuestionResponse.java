package com.fpt.careermate.services.interview_services.service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InterviewQuestionResponse {
    int questionId;
    int questionNumber;
    String question;
    String candidateAnswer;
    Double score;
    String feedback;
    LocalDateTime askedAt;
    LocalDateTime answeredAt;
}
