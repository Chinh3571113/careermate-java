package com.fpt.careermate.services.resume_services.web.rest;

import com.fpt.careermate.common.constant.ResumeType;
import com.fpt.careermate.services.resume_services.service.ResumeImp;
import com.fpt.careermate.services.resume_services.service.dto.request.ResumeRequest;
import com.fpt.careermate.services.resume_services.service.dto.request.ResumeStatusRequest;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.resume_services.service.dto.response.ResumeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
@Tag(name = "Resume", description = "Resume API")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ResumeController {
    ResumeImp resumeImp;

    @PostMapping
    @Operation(summary = "Create Resume", description = "Create a new resume")
    ApiResponse<ResumeResponse> createResume(@RequestBody ResumeRequest resumeRequest) {
        return ApiResponse.<ResumeResponse>builder()
                .result(resumeImp.createResume(resumeRequest))
                .message("Create resume successfully")
                .build();
    }

    @GetMapping
    @Operation(summary = "Get All Resumes by Candidate", description = "Retrieve all resumes for the authenticated candidate")
    ApiResponse<List<ResumeResponse>> getAllResumesByCandidate() {
        return ApiResponse.<List<ResumeResponse>>builder()
                .result(resumeImp.getAllResumesByCandidate())
                .message("Get resumes successfully")
                .build();
    }

    @GetMapping("/{resumeId}")
    @Operation(summary = "Get Resume by ID", description = "Retrieve a specific resume by ID")
    ApiResponse<ResumeResponse> getResumeById(@PathVariable int resumeId) {
        return ApiResponse.<ResumeResponse>builder()
                .result(resumeImp.getResumeById(resumeId))
                .message("Get resume successfully")
                .build();
    }

    @DeleteMapping("/{resumeId}")
    @Operation(summary = "Delete Resume", description = "Delete a resume by ID")
    ApiResponse<Void> deleteResume(@PathVariable int resumeId) {
        resumeImp.deleteResume(resumeId);
        return ApiResponse.<Void>builder()
                .message("Delete resume successfully")
                .build();
    }

    @PutMapping("/{resumeId}")
    @Operation(summary = "Update Resume", description = "Update an existing resume")
    ApiResponse<ResumeResponse> updateResume(@PathVariable int resumeId, @RequestBody ResumeRequest resumeRequest) {
        return ApiResponse.<ResumeResponse>builder()
                .result(resumeImp.updateResume(resumeId, resumeRequest))
                .message("Update resume successfully")
                .build();
    }

    @PatchMapping("/{resumeId}/status")
    @Operation(summary = "Update Resume Status", description = "Update only the isActive status of a resume")
    ApiResponse<ResumeResponse> patchResumeStatus(@PathVariable int resumeId, @Valid @RequestBody ResumeStatusRequest request) {
        return ApiResponse.<ResumeResponse>builder()
                .result(resumeImp.patchResumeStatus(resumeId, request))
                .message("Update resume status successfully")
                .build();
    }

    @PatchMapping("/{resumeId}/type/{type}")
    @Operation(summary = "Update Resume Type", description = "Update only the type of a resume (WEB, UPLOAD, DRAFT)")
    ApiResponse<ResumeResponse> patchResumeType(@PathVariable int resumeId, @Valid @PathVariable ResumeType type) {
        return ApiResponse.<ResumeResponse>builder()
                .result(resumeImp.patchResumeType(resumeId, type))
                .message("Update resume type successfully")
                .build();
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get Resumes by Type", description = "Retrieve all resumes for the authenticated candidate filtered by type (WEB, UPLOAD, DRAFT)")
    ApiResponse<List<ResumeResponse>> getResumesByType(@PathVariable ResumeType type) {
        return ApiResponse.<List<ResumeResponse>>builder()
                .result(resumeImp.getResumesByType(type))
                .message("Get resumes by type successfully")
                .build();
    }
}
