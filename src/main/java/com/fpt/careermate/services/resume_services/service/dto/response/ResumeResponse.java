package com.fpt.careermate.services.resume_services.service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fpt.careermate.common.constant.ResumeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeResponse {
    int resumeId;
    String aboutMe;
    String resumeUrl;
    ResumeType type;
    Boolean isActive;
    LocalDateTime createdAt;
    int candidateId;

    List<CertificateResponse> certificates;
    List<EducationResponse> educations;
    List<HighlightProjectResponse> highlightProjects;
    List<WorkExperienceResponse> workExperiences;
    List<SkillResponse> skills;
    List<ForeignLanguageResponse> foreignLanguages;
    List<AwardResponse> awards;
}
