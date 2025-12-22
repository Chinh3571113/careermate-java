package com.fpt.careermate.services.recommendation.dto;

import com.fpt.careermate.common.constant.StatusJobApply;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CandidateRecommendationDTO {
    
    int candidateId;
    String candidateName;
    String email;
    double matchScore;
    List<String> matchedSkills;
    List<String> missingSkills;
    int totalYearsExperience;
    String profileSummary;

    // Additional qualification details
    String educationLevel;
    int certificatesCount;
    int projectsCount;
    int awardsCount;
    int languagesCount;

    // Score breakdown for explainability
    Map<String, Double> scoreBreakdown;
    
    // Application details for recruiter
    int applicationId;
    StatusJobApply applicationStatus;
    String cvFilePath;
    String phoneNumber;
    String avatarUrl;
    String preferredWorkLocation;
    LocalDateTime appliedAt;
    String coverLetter;
}

