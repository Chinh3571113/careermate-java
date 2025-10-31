package com.fpt.careermate.services.authentication_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.authentication_services.service.RegistrationService;
import com.fpt.careermate.services.authentication_services.service.dto.request.RecruiterRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/oauth2/recruiter")
@Tag(name = "OAuth2 Recruiter Registration", description = "Complete recruiter registration flow for Google OAuth users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OAuth2RecruiterController {

    RegistrationService registrationService;

    @PostMapping("/complete-registration")
    @Operation(
        summary = "Complete recruiter registration for OAuth user",
        description = "After Google OAuth login, recruiter must submit organization information to complete registration. " +
                      "Account will have RECRUITER role with PENDING status until admin approval."
    )
    public ApiResponse<String> completeRecruiterRegistration(
            @Valid @RequestBody RecruiterRegistrationRequest.OrganizationInfo orgInfo,
            HttpSession session) {

        // Get email from OAuth session
        String email = (String) session.getAttribute("oauth_email");
        if (email == null) {
            email = (String) session.getAttribute("email");
        }

        Long timestamp = (Long) session.getAttribute("oauth_timestamp");

        log.info("Complete registration attempt - Email: {}, Session age: {} ms",
                 email, timestamp != null ? (System.currentTimeMillis() - timestamp) : "unknown");

        if (email == null) {
            log.error("OAuth session not found or expired");
            return ApiResponse.<String>builder()
                    .code(401)
                    .message("OAuth session expired. Please login with Google again.")
                    .build();
        }

        // Validate session age (30 minutes max)
        if (timestamp != null) {
            long age = System.currentTimeMillis() - timestamp;
            if (age > 1800000) { // 30 minutes in milliseconds
                log.error("OAuth session expired - Age: {} ms", age);
                session.invalidate();
                return ApiResponse.<String>builder()
                        .code(401)
                        .message("OAuth session expired. Please login with Google again.")
                        .build();
            }
        }

        try {
            // Complete recruiter profile (account already has RECRUITER role from OAuth)
            registrationService.completeRecruiterProfileForOAuth(email, orgInfo);

            log.info("Recruiter profile created successfully for: {}", email);

            // Clear session
            session.invalidate();

            return ApiResponse.<String>builder()
                    .code(200)
                    .message("Recruiter registration completed! Your account is pending admin approval. " +
                             "You will receive an email when approved.")
                    .build();
        } catch (Exception e) {
            log.error("Error creating recruiter profile: {}", e.getMessage(), e);
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("Error completing registration: " + e.getMessage())
                    .build();
        }
    }
}

