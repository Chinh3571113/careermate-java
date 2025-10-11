package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.ForeignLanguageImp;
import com.fpt.careermate.services.dto.request.ForeignLanguageRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.ForeignLanguageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/foreign-language")
@Tag(name = "Foreign Language", description = "foreign Language API")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ForeignLanguageController {
    ForeignLanguageImp foreignLanguageImp;

    @PostMapping()
    @Operation(summary = "Add Foreign Language", description = "Add foreign language to resume")
    public ApiResponse<ForeignLanguageResponse> addForeignLanguage(@RequestBody @Valid ForeignLanguageRequest foreignLanguageRequest) {
        return ApiResponse.<ForeignLanguageResponse>builder()
                .message("Add foreign language successfully")
                .result(foreignLanguageImp.addForeignLanguageToResume(foreignLanguageRequest))
                .build();
    }

    @PutMapping("/{resumeId}/{foreignLanguageId}")
    @Operation(summary = "Update Foreign Language", description = "Update foreign language in resume")
    public ApiResponse<ForeignLanguageResponse> updateForeignLanguage(@PathVariable int resumeId,
                                                                     @PathVariable int foreignLanguageId,
                                                                     @RequestBody @Valid ForeignLanguageRequest foreignLanguageRequest) {
        return ApiResponse.<ForeignLanguageResponse>builder()
                .message("Update foreign language successfully")
                .result(foreignLanguageImp.updateForeignLanguageInResume(resumeId, foreignLanguageId, foreignLanguageRequest))
                .build();
    }

    @DeleteMapping("/{foreignLanguageId}")
    @Operation(summary = "Remove Foreign Language", description = "Remove foreign language from resume")
    public ApiResponse<Void> removeForeignLanguage(@PathVariable int foreignLanguageId) {
        foreignLanguageImp.removeForeignLanguageFromResume(0, foreignLanguageId);
        return ApiResponse.<Void>builder()
                .message("Remove foreign language successfully")
                .build();
    }

}
