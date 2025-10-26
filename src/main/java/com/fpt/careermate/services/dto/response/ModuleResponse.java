package com.fpt.careermate.services.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModuleResponse {
    int id;
    String title;
    int position;
    List<LessonResponse> lessons;
}
