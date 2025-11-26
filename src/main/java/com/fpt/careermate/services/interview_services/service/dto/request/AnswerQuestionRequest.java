package com.fpt.careermate.services.interview_services.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerQuestionRequest {
    @NotBlank(message = "Answer must not be blank")
    String answer;
}

