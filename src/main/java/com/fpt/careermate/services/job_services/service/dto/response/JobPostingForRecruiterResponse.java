package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostingForRecruiterResponse {

    int id;
    String title;
    String description;
    String address;
    String status;
    LocalDate expirationDate;
    LocalDate postTime;
    Set<JobPostingSkillResponse> skills;
    int yearsOfExperience;
    String workModel;
    String salaryRange;
    String reason;
    String jobPackage;
}
