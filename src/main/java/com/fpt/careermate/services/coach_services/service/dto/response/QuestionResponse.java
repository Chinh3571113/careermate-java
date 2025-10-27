package com.fpt.careermate.services.coach_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuestionResponse {

    int id;
    String title;
    String explanation;
    List<OptionResponse> options;
    OptionResponse correctOption;

}
