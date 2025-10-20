package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.RecruiterImp;
import com.fpt.careermate.services.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/recruiter")
@Tag(name = "Recruiter", description = "Recruiter profile management")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterController {

    RecruiterImp recruiterImp;

    @Operation(
        summary = "Create recruiter profile (Step 2 of recruiter registration)",
        description = "**Recruiter Registration Flow:**\n\n" +
                      "1. User signs up via POST /api/users (gets CANDIDATE role)\n" +
                      "2. User creates recruiter profile via this endpoint (still CANDIDATE role)\n" +
                      "3. Admin reviews profile via GET /api/admin/recruiters/pending\n" +
                      "4. Admin approves via PUT /api/admin/recruiters/{id}/approve (role changes to RECRUITER)\n\n" +
                      "**Note:** After sign up, users can immediately create their recruiter profile with organization info. " +
                      "The profile will be pending until admin approval."
    )
    @PostMapping
    public ApiResponse<NewRecruiterResponse> createRecruiter(@Valid @RequestBody RecruiterCreationRequest request) {
        return ApiResponse.<NewRecruiterResponse>builder()
                .result(recruiterImp.createRecruiter(request))
                .code(200)
                .message("Recruiter profile created successfully. Waiting for admin approval to activate recruiter features.")
                .build();
    }

}
