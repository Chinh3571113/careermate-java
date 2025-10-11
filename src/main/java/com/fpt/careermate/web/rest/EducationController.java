package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.EducationImp;
import com.fpt.careermate.services.dto.request.EducationRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.EducationResponse;
import com.fpt.careermate.services.impl.EducationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/education")
@Tag(name = "Education", description = "Education API")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EducationController {
    EducationImp educationImp;

    @PostMapping
    @Operation(summary = "Add Education", description = "Add education to resume")
    public ApiResponse<EducationResponse> addEducation(@RequestBody @Valid EducationRequest educationRequest) {
        return ApiResponse.<EducationResponse>builder()
                .message("Add education successfully")
                .result(educationImp.addEducationToResume(educationRequest))
                .build();
    }

    @PutMapping("/{resumeId}/{educationId}")
    @Operation(summary = "Update Education", description = "Update education in resume")
    public ApiResponse<EducationResponse> updateEducation(@PathVariable int resumeId,
                                                          @PathVariable int educationId,
                                                          @RequestBody @Valid EducationRequest educationRequest) {
        return ApiResponse.<EducationResponse>builder()
                .message("Update education successfully")
                .result(educationImp.updateEducationInResume(resumeId,educationId, educationRequest))
                .build();
    }

    @DeleteMapping("/{educationId}")
    @Operation(summary = "Remove Education", description = "Remove education from resume")
    public ApiResponse<Void> removeEducation(@PathVariable int educationId) {
        educationImp.removeEducationFromResume(educationId);
        return ApiResponse.<Void>builder()
                .message("Remove education successfully")
                .build();
    }
}
