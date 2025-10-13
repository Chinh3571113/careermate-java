package com.fpt.careermate.services.dto.response;

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
    String email;
    String phoneNumber;
}
