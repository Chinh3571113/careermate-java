package com.fpt.careermate.services.recruiter_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterApprovalResponse {
    int recruiterId;
    int accountId;
    String email;
    String username;
    String companyName;
    String website;
    String logoUrl;
    String about;
    Float rating;

    // Organization verification fields for admin review (Only 4 fields)
    String companyEmail;
    String contactPerson;
    String phoneNumber;
    String companyAddress;

    String accountStatus;
    String accountRole; // CANDIDATE (pending) or RECRUITER (approved)
}
