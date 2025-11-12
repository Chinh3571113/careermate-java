package com.fpt.careermate.services.recruiter_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecruiterBasicInfoResponse {

    int id;
    String companyName;
    String email; // Account email
    String companyEmail; // Company/Work email
    String phoneNumber;
}
