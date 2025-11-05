package com.fpt.careermate.services.recruiter_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterUpdateRequestResponse {
    int requestId;
    int recruiterId;
    String recruiterEmail;
    String recruiterUsername;

    // Current values
    String currentCompanyName;
    String currentWebsite;
    String currentLogoUrl;
    String currentAbout;
    String currentCompanyEmail;
    String currentContactPerson;
    String currentPhoneNumber;
    String currentCompanyAddress;

    // Requested changes
    String newCompanyName;
    String newWebsite;
    String newLogoUrl;
    String newAbout;
    String newCompanyEmail;
    String newContactPerson;
    String newPhoneNumber;
    String newCompanyAddress;

    String status; // PENDING, APPROVED, REJECTED
    String adminNote;
    String rejectionReason;

    LocalDateTime createdAt;
    LocalDateTime reviewedAt;
}

