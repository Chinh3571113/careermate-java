package com.fpt.careermate.services.authentication_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.RegistrationService;
import com.fpt.careermate.services.authentication_services.service.dto.request.RecruiterRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration")
@Tag(name = "Registration", description = "Recruiter registration API")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RegistrationController {

    RegistrationService registrationService;

    @PostMapping("/recruiter")
    @Operation(
        summary = "Register as Recruiter (with Organization Info)",
        description = "**Complete Recruiter Registration Flow:**\n" +
                      "1. User fills out this form with account info + organization details\n" +
                      "2. System creates account with RECRUITER role and PENDING status\n" +
                      "3. Admin reviews via GET /api/admin/recruiters/pending\n" +
                      "4. Admin approves via PUT /api/admin/recruiters/{id}/approve\n" +
                      "5. System changes account status: PENDING → ACTIVE\n" +
                      "6. User can now login and access all recruiter features\n\n" +
                      "**Note:** \n" +
                      "- User cannot login while status is PENDING\n" +
                      "- Role is RECRUITER from the start (not CANDIDATE)\n" +
                      "- Logo URL is optional. If not provided, a placeholder will be used."
    )
    public ApiResponse<String> registerRecruiter(@Valid @RequestBody RecruiterRegistrationRequest request) {
        Account account = registrationService.registerRecruiter(request);

        return ApiResponse.<String>builder()
                .code(200)
                .message("Recruiter account created successfully with PENDING status. " +
                         "Your account is awaiting admin approval. You will be able to login after approval.")
                .result("Account ID: " + account.getId())
                .build();
    }

}
