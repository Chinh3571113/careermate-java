package com.fpt.careermate.services.authentication_services.service.dto.response;

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

    @JsonIgnore // This won't be sent to frontend for web, only used internally
    String refreshToken;

    boolean authenticated;
    Long expiresIn;
    String tokenType;
    
    // User identification - CRITICAL for frontend API calls
    Integer userId;           // Account ID
    Integer recruiterId;      // Recruiter profile ID (null if not a recruiter)
    Integer candidateId;      // Candidate profile ID (null if not a candidate)
    String email;
    String role;              // Primary role: ADMIN, RECRUITER, CANDIDATE
}
