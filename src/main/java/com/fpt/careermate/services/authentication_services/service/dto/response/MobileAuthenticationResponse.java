package com.fpt.careermate.services.authentication_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MobileAuthenticationResponse {
    String accessToken;
    String refreshToken; // Visible for mobile clients
    boolean authenticated;
    Long expiresIn;
    String tokenType;
}

