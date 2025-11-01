package com.fpt.careermate.services.profile_services.web.rest;

import com.fpt.careermate.services.profile_services.service.CandidateProfileImp;
import com.fpt.careermate.services.profile_services.service.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.profile_services.service.dto.request.GeneralInfoRequest;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.profile_services.service.dto.response.CandidateProfileResponse;
import com.fpt.careermate.services.profile_services.service.dto.response.GeneralInfoResponse;
import com.fpt.careermate.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
@Tag(name = "Candidate Profile", description = "Manage candidate profile")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidateController {
    CandidateProfileImp candidateProfileImp;

    @Operation(summary = "Update candidate profile", description = "Create or Update candidate profile")
    @PutMapping("/profiles")
    public ApiResponse<CandidateProfileResponse> updateCandidateProfile(@RequestBody @Valid CandidateProfileRequest request) {
        CandidateProfileResponse response = candidateProfileImp.updateCandidateProfile(request);
        return ApiResponse.<CandidateProfileResponse>builder()
                .code(200)
                .message("Successfully created candidate profile")
                .result(response)
                .build();
    }
    @Operation(summary = "Create candidate profile", description = "Create candidate profile")
    @PostMapping("/profiles/create")
    public ApiResponse<CandidateProfileResponse> saveCandidateProfile(@RequestBody @Valid CandidateProfileRequest request) {
        CandidateProfileResponse response = candidateProfileImp.saveCandidateProfile(request);
        return ApiResponse.<CandidateProfileResponse>builder()
                .code(200)
                .message("Successfully created candidate profile")
                .result(response)
                .build();
    }

    @Operation(summary = "Get all candidate profiles", description = "Retrieve all candidate profiles with pagination")
    @GetMapping("/profiles")
    public ApiResponse<PageResponse<CandidateProfileResponse>> findAll(@RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<CandidateProfileResponse> result = candidateProfileImp.findAll(pageable);
        return ApiResponse.<PageResponse<CandidateProfileResponse>>builder()
                .code(200)
                .message("Successfully retrieved candidate profiles")
                .result(result)
                .build();
    }

    @DeleteMapping("/profiles/{id}")
    @Operation(summary = "Delete candidate profile", description = "Delete a candidate profile by ID")
    public ApiResponse<Void> delete(@PathVariable int id) {
        candidateProfileImp.deleteProfile(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Successfully deleted candidate profile")
                .build();
    }

    @Operation(summary = "Create or Update candidate general info", description = "Create or Update a candidate " +
            "profile")
    @PutMapping("/profiles-general-info")
    public ApiResponse<GeneralInfoResponse> saveCandidateProfile(@RequestBody @Valid GeneralInfoRequest request) {
        GeneralInfoResponse response = candidateProfileImp.saveCandidateGeneralInfo(request);
        return ApiResponse.<GeneralInfoResponse>builder()
                .code(200)
                .message("Successfully created candidate profile")
                .result(response)
                .build();
    }

    @GetMapping("/profiles-general-info")
    @Operation(summary = "Get candidate general info by ID", description = "Retrieve candidate general info by ID")
    public ApiResponse<GeneralInfoResponse> getCandidateGeneralInfoById() {
        GeneralInfoResponse response = candidateProfileImp.getCandidateGeneralInfoById();
        return ApiResponse.<GeneralInfoResponse>builder()
                .code(200)
                .message("Successfully retrieved candidate general info")
                .result(response)
                .build();
    }

    @GetMapping("/profiles/current")
    @Operation(summary = "Get current candidate profile", description = "Retrieve the currently authenticated candidate's profile")
    public ApiResponse<CandidateProfileResponse> getCandidateProfileById() {
        CandidateProfileResponse response = candidateProfileImp.getCandidateProfileById();
        return ApiResponse.<CandidateProfileResponse>builder()
                .code(200)
                .message("Successfully retrieved candidate profile")
                .result(response)
                .build();
    }

    @PutMapping("/profiles-general-info/update")
    @Operation(summary = "Update candidate general info", description = "Update candidate general info by ID")
    public ApiResponse<GeneralInfoResponse> updateCandidateGeneralInfo(@RequestBody @Valid GeneralInfoRequest request) {
        GeneralInfoResponse response = candidateProfileImp.updateCandidateGeneralInfo(request);
        return ApiResponse.<GeneralInfoResponse>builder()
                .code(200)
                .message("Successfully updated candidate general info")
                .result(response)
                .build();
    }


}
