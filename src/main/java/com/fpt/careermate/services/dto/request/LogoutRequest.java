package com.fpt.careermate.services.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LogoutRequest {
    String accessToken;
    String refreshToken;

    // For backward compatibility
    @Deprecated
    String token;
}
