package com.fpt.careermate.services.authentication_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleResponse {
    String email;
    String accessToken;
    String refreshToken;
    Boolean recruiter;
    Boolean profileCompleted;
}
