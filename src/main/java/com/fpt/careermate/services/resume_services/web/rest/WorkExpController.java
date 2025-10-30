package com.fpt.careermate.services.resume_services.web.rest;

import com.fpt.careermate.services.resume_services.service.WorkExperienceImp;
import com.fpt.careermate.services.resume_services.service.dto.request.WorkExperienceRequest;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.resume_services.service.dto.response.WorkExperienceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/work-exp")
@Tag(name = "Work Experience", description = "work-exp API")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WorkExpController {
    WorkExperienceImp workExperienceImp;

    @PostMapping
    @Operation(summary = "Add Work Experience", description = "Add work experience to resume")
    public ApiResponse<WorkExperienceResponse> addWorkExperience(@RequestBody @Valid WorkExperienceRequest workExperienceRequest) {
         workExperienceImp.addWorkExperienceToResume(workExperienceRequest);
        return ApiResponse.<WorkExperienceResponse>builder()
                .message("Add work experience successfully")
                .build();
    }

    @PutMapping("/{resumeId}/{workExpId}")
    @Operation(summary = "Update Work Experience", description = "Update work experience in resume")
    public ApiResponse<WorkExperienceResponse> updateWorkExperience(@PathVariable int resumeId,
                                                                 @PathVariable int workExpId,
                                                                 @RequestBody @Valid WorkExperienceRequest workExperienceRequest) {
        return ApiResponse.<WorkExperienceResponse>builder()
                .message("Update work experience successfully")
                .result(workExperienceImp.updateWorkExperienceInResume(resumeId, workExpId, workExperienceRequest))
                .build();
    }

    @DeleteMapping("/{workExpId}")
    @Operation(summary = "Remove Work Experience", description = "Remove work experience from resume")
    public ApiResponse<Void> removeWorkExperience(@PathVariable int workExpId) {
        workExperienceImp.removeWorkExperienceFromResume(workExpId);
        return ApiResponse.<Void>builder()
                .message("Remove work experience successfully")
                .build();
    }

}
