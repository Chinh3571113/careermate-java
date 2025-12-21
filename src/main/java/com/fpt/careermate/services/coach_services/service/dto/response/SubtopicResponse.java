package com.fpt.careermate.services.coach_services.service.dto.response;

import com.fpt.careermate.common.constant.ResumeSubtopicProgressStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubtopicResponse {
    int id;
    String name;
    String tags;
    @Builder.Default
    ResumeSubtopicProgressStatus status = ResumeSubtopicProgressStatus.NOT_STARTED;
}
