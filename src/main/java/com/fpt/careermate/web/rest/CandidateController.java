package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.CandidateProfileImp;
import com.fpt.careermate.services.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.dto.request.GeneralInfoRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.CandidateProfileResponse;
import com.fpt.careermate.services.dto.response.GeneralInfoResponse;
import com.fpt.careermate.services.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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

    @Operation(summary = "Create or Update candidate profile", description = "Create or Update candidate profile")
    @PostMapping("/profiles")
    public ApiResponse<CandidateProfileResponse> saveOrUpdateCandidateProfile(@RequestBody CandidateProfileRequest request) {
        CandidateProfileResponse response = candidateProfileImp.saveOrUpdateCandidateProfile(request);
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
    public ApiResponse<GeneralInfoResponse> saveOrUpdateCandidateProfile(@RequestBody GeneralInfoRequest request) {
        GeneralInfoResponse response = candidateProfileImp.saveOrUpdateCandidateGeneralInfo(request);
        return ApiResponse.<GeneralInfoResponse>builder()
                .code(200)
                .message("Successfully created candidate profile")
                .result(response)
                .build();
    }


}
