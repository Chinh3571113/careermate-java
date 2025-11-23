package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.order_services.service.CandidateEntitlementCheckerService;
import com.fpt.careermate.services.order_services.service.RecruiterEntitlementCheckerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "Recruiter - Entitlement", description = "Manage Recruiter Entitlement")
@RestController
@RequestMapping("/api/recruiter-entitlement")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterEntitlementController {

    RecruiterEntitlementCheckerService checkerService;

    @GetMapping("/ai-matching-checker")
    @Operation(description = """
            Check if recruiter can use AI Matching feature
            input: none
            output: boolean
            Need login as recruiter to access this API
            Use this API before calling Ai Matching API
            """)
    public ApiResponse<Boolean> canUseAiMatching() {
        return ApiResponse.<Boolean>builder()
                .result(checkerService.canUseAiMatching())
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/job-posting-checker")
    @Operation(description = """
            Check if recruiter can use Post Job feature
            input: none
            output: boolean
            Need login as recruiter to access this API
            Use this API before calling Jos Posting API
            """)
    public ApiResponse<Boolean> canPostJob() {
        return ApiResponse.<Boolean>builder()
                .result(checkerService.canPostJob())
                .code(200)
                .message("success")
                .build();
    }

}
