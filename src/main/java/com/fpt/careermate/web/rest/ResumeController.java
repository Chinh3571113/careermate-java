package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.ResumeImp;
import com.fpt.careermate.services.dto.request.ResumeRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.ResumeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Get Resume by Candidate ID", description = "Retrieve a resume by candidate ID")
    ApiResponse<ResumeResponse> getResumeByCandidateId() {
        return ApiResponse.<ResumeResponse>builder()
                .result(resumeImp.getResumeById())
                .message("Get resume successfully")
                .build();
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete Resume", description = "Delete a resume by candidate ID")
    ApiResponse<Void> deleteResume(@PathVariable int id) {
        resumeImp.deleteResume(id);
        return ApiResponse.<Void>builder()
                .message("Delete resume successfully")
                .build();
    }

    @PutMapping
    @Operation(summary = "Update Resume", description = "Update an existing resume")
    ApiResponse<ResumeResponse> updateResume(@RequestBody ResumeRequest resumeRequest) {
        return ApiResponse.<ResumeResponse>builder()
                .result(resumeImp.updateResume(resumeRequest))
                .message("Update resume successfully")
                .build();
    }

}
