package com.fpt.careermate.services.resume_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.resume_services.service.HighLightProjectImp;
import com.fpt.careermate.services.resume_services.service.dto.request.HighlightProjectRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.HighlightProjectResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/highlight-project")
@Tag(name = "Highlight Project", description = "Highlight Project API")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class HighLightProjectController {
    HighLightProjectImp highLightProjectImp;

    @PostMapping
    @Operation(summary = "Add Highlight Project", description = "Add highlight project to resume")
    public ApiResponse<HighlightProjectResponse> addHighlightProject(@RequestBody @Valid HighlightProjectRequest highlightProjectRequest) {
        return ApiResponse.<HighlightProjectResponse>builder()
                .message("Add highlight project successfully")
                .result(highLightProjectImp.addHighlightProjectToResume(highlightProjectRequest))
                .build();
    }

    @PutMapping("/{resumeId}/{highlightProjectId}")
    @Operation(summary = "Update Highlight Project", description = "Update highlight project in resume")
    public ApiResponse<HighlightProjectResponse> updateHighlightProject(@PathVariable int resumeId,
                                                                        @PathVariable int highlightProjectId,
                                                                        @RequestBody @Valid HighlightProjectRequest highlightProjectRequest) {
        return ApiResponse.<HighlightProjectResponse>builder()
                .message("Update highlight project successfully")
                .result(highLightProjectImp.updateHighlightProjectInResume(resumeId, highlightProjectId, highlightProjectRequest))
                .build();
    }

    @DeleteMapping("/{highlightProjectId}")
    @Operation(summary = "Remove Highlight Project", description = "Remove highlight project from resume")
    public ApiResponse<Void> removeHighlightProject(@PathVariable int highlightProjectId) {
        highLightProjectImp.removeHighlightProjectFromResume(highlightProjectId);
        return ApiResponse.<Void>builder()
                .message("Remove highlight project successfully")
                .build();
    }
}
