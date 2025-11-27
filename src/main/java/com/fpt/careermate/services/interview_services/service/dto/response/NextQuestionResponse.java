package com.fpt.careermate.services.interview_services.service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NextQuestionResponse {
    int questionId;
    int questionNumber;
    String question;
    boolean isLastQuestion;
}
