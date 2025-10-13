package com.fpt.careermate.services.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobPostingForAdminResponse {

    int id;
    String title;
    String description;
    String address;
    String status;
    LocalDate expirationDate;
    LocalDate createAt;
    String rejectionReason;
    RecruiterBasicInfoResponse recruiter;
    String approvedByEmail;
    Set<JobPostingSkillResponse> skills;
}
