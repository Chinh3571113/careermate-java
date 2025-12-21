package com.fpt.careermate.services.coach_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResumeRoadmapListResponse {
    int id;
    int resumeId;
    String roadmapName;
    boolean isActive;
    OffsetDateTime createdAt;
}

