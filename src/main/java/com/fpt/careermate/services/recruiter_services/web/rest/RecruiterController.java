package com.fpt.careermate.services.recruiter_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.recruiter_services.service.RecruiterImp;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterUpdateRequest;
import com.fpt.careermate.services.recruiter_services.service.dto.response.NewRecruiterResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterProfileResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterUpdateRequestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/profile")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(
        summary = "Get recruiter's own profile",
        description = "Recruiter can view their profile including pending update requests"
    )
    public ApiResponse<RecruiterProfileResponse> getMyProfile() {
        log.info("Recruiter fetching own profile");
        return ApiResponse.<RecruiterProfileResponse>builder()
                .result(recruiterImp.getMyProfile())
                .code(200)
                .message("Profile retrieved successfully")
                .build();
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(
        summary = "Request profile update",
        description = "Recruiter submits profile update request for admin approval. " +
                      "Can continue using old info while waiting for approval."
    )
    public ApiResponse<RecruiterUpdateRequestResponse> requestProfileUpdate(@Valid @RequestBody RecruiterUpdateRequest request) {
        log.info("Recruiter requesting profile update");
        return ApiResponse.<RecruiterUpdateRequestResponse>builder()
                .result(recruiterImp.requestProfileUpdate(request))
                .code(200)
                .message("Profile update request submitted. You can continue using your current profile while waiting for admin approval.")
                .build();
    }

    @GetMapping("/profile/update-requests")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(
        summary = "Get my profile update requests",
        description = "Recruiter can view their profile update request history"
    )
    public ApiResponse<List<RecruiterUpdateRequestResponse>> getMyUpdateRequests() {
        log.info("Recruiter fetching profile update requests");
        return ApiResponse.<List<RecruiterUpdateRequestResponse>>builder()
                .result(recruiterImp.getMyUpdateRequests())
                .code(200)
                .message("Update requests retrieved successfully")
                .build();
    }

}
