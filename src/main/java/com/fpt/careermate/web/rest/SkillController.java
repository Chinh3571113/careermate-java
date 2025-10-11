package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.SkillImp;
import com.fpt.careermate.services.dto.request.SkillRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.SkillResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skill")
@Tag(name = "Skill", description = "Skill API")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SkillController {
    SkillImp skillImp;

    @PostMapping
    @Operation(summary = "Add Skill", description = "Add skill to resume")
    public ApiResponse<SkillResponse> addSkill(@RequestBody @Valid SkillRequest skillRequest) {
        return ApiResponse.<SkillResponse>builder()
                .message("Add skill successfully")
                .result(skillImp.addSkillToResume(skillRequest))
                .build();
    }

    @PutMapping
    @Operation(summary = "Update Skill", description = "Update skill in resume")
    public ApiResponse<SkillResponse> updateSkill(@RequestParam int resumeId,
                                                 @RequestParam int skillId,
                                                 @RequestBody @Valid SkillRequest skillRequest) {
        return ApiResponse.<SkillResponse>builder()
                .message("Update skill successfully")
                .result(skillImp.updateSkillInResume(resumeId, skillId, skillRequest))
                .build();
    }

    @DeleteMapping("/{resumeId}/{skillId}")
    @Operation(summary = "Remove Skill", description = "Remove skill from resume")
    public ApiResponse<Void> removeSkill(@PathVariable int resumeId,
                                        @PathVariable int skillId) {
        skillImp.removeSkillFromResume(resumeId, skillId);
        return ApiResponse.<Void>builder()
                .message("Remove skill successfully")
                .build();
    }

}
