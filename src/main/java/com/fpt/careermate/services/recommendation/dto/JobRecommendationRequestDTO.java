package com.fpt.careermate.services.recommendation.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobRecommendationRequestDTO {
    
    int jobPostingId;
    List<String> requiredSkills;
    Integer minYearsExperience;
    Integer maxCandidates;
    Double minMatchScore;
}

