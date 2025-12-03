package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.services.job_services.service.JdJdSkillImp;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.job_services.service.dto.response.JdSkillResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/jdskill")
@Tag(name = "JdSkill", description = "Manage jdSkill")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JdSkillController {

    JdJdSkillImp jdSkillImp;

    @PostMapping
    @Operation(summary = "Create jdSkill")
    ApiResponse<String> createJdSkill(
            @RequestParam
            @NotBlank
            String name
    ) {
        jdSkillImp.createSkill(name);
        return ApiResponse.<String>builder()
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping
    @Operation(summary = "Get jdSkill list with optional autocomplete search",
               description = "Returns all skills if no keyword is provided, otherwise returns skills matching the keyword (case-insensitive partial match)")
    ApiResponse<List<JdSkillResponse>> getSkillList(
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.<List<JdSkillResponse>>builder()
                .result(jdSkillImp.getAllSkill(keyword))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/top-used")
    @Operation(summary = "Get top used skills from first 50 job descriptions",
               description = "Returns a list of the most frequently used skills from the first 50 job description records, ordered by usage count (descending)")
    ApiResponse<List<JdSkillResponse>> getTopUsedSkills() {
        return ApiResponse.<List<JdSkillResponse>>builder()
                .result(jdSkillImp.getTopUsedSkillsFromFirst50())
                .code(200)
                .message("Top used skills retrieved successfully")
                .build();
    }

}
