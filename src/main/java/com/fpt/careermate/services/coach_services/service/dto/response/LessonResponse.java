package com.fpt.careermate.services.coach_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonResponse {
    int id;
    String title;
    int position;
    boolean marked;
}
