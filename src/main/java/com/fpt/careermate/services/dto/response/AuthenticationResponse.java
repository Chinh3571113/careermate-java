package com.fpt.careermate.services.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String accessToken;

    @JsonIgnore // This won't be sent to frontend, only used internally
    String refreshToken;

    boolean authenticated;
    Long expiresIn;
    String tokenType;
}
