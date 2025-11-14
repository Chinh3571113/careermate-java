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
public class RecommendationResponseDTO {
    
    int jobPostingId;
    String jobTitle;
    int totalCandidatesFound;
    List<CandidateRecommendationDTO> recommendations;
    long processingTimeMs;
}

