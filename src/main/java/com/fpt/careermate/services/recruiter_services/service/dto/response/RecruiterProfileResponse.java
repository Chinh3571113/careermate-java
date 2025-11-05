package com.fpt.careermate.services.recruiter_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterProfileResponse {
    int recruiterId;
    int accountId;
    String email;
    String username;

    // Current profile info
    String companyName;
    String website;
    String logoUrl;
    String about;
    Float rating;
    String companyEmail;
    String contactPerson;
    String phoneNumber;
    String companyAddress;

    String verificationStatus; // PENDING, APPROVED, REJECTED
    String rejectionReason;

    // Pending update info (if exists)
    boolean hasPendingUpdate;
    RecruiterUpdateRequestResponse pendingUpdateRequest;
}

