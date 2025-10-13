package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.AwardImp;
import com.fpt.careermate.services.dto.request.AwardRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.AwardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/award")
@Tag(name = "Award", description = "Award API")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AwardController {
    AwardImp awardImp;

    @PostMapping
    @Operation(summary = "Add Award", description = "Add award to resume")
    public ApiResponse<AwardResponse> addAward(@RequestBody @Valid AwardRequest awardRequest) {
        return ApiResponse.<AwardResponse>builder()
                .message("Add award successfully")
                .result(awardImp.addAwardToResume(awardRequest))
                .build();
    }

    @PutMapping("/{resumeId}/{awardId}")
    @Operation(summary = "Update Award", description = "Update award in resume")
    public ApiResponse<AwardResponse> updateAward(@PathVariable int resumeId,
                                                 @PathVariable int awardId,
                                                 @RequestBody @Valid AwardRequest awardRequest) {
        return ApiResponse.<AwardResponse>builder()
                .message("Update award successfully")
                .result(awardImp.updateAwardInResume(resumeId, awardId, awardRequest))
                .build();
    }

    @DeleteMapping("/{awardId}")
    @Operation(summary = "Remove Award", description = "Remove award from resume")
    public ApiResponse<Void> removeAward(@PathVariable int awardId) {
        awardImp.removeAwardFromResume(0, awardId);
        return ApiResponse.<Void>builder()
                .message("Remove award successfully")
                .build();
    }

}
