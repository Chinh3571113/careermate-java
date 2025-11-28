package com.fpt.careermate.services.job_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JobApplyResponse {
    int id;
    int jobPostingId;
    String jobTitle;
    String jobDescription;
    LocalDate expirationDate;
    int candidateId;
    String cvFilePath;
    String fullName;
    String phoneNumber;
    String preferredWorkLocation;
    String coverLetter;
    String status;
    LocalDateTime createAt;
    
    // Recruiter contact info - only populated when status >= APPROVED
    // These fields enable candidate to contact recruiter after being approved
    String companyName;
    String companyEmail;
    String recruiterPhone;
    String companyAddress;
    String contactPerson;
}
