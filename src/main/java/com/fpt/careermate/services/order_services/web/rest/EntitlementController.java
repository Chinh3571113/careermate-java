package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.order_services.service.CandidateEntitlementCheckerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Tag(name = "Entitlement", description = "Manage Entitlement")
@RestController
@RequestMapping("/api/entitlement")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EntitlementController {

    CandidateEntitlementCheckerService checkerService;

    @GetMapping("/roadmap-recommendation-checker")
    @Operation(summary = """
            Check if candidate can use Roadmap Recommendation feature
            input: none
            output: boolean
            Need login as candidate to access this API
            Use this API before calling Roadmap Recommendation API
            """)
    public ApiResponse<Boolean> canUseRoadmapRecommendation() {
        return ApiResponse.<Boolean>builder()
                .result(checkerService.canUseRoadmapRecommendation())
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/job-recommendation-checker")
    @Operation(summary = """
            Check if candidate can use Job Recommendation feature
            input: none
            output: boolean
            Need login as candidate to access this API
            Use this API before calling Job Recommendation API
            """)
    public ApiResponse<Boolean> canUseJobRecommendation() {
        return ApiResponse.<Boolean>builder()
                .result(checkerService.canUseJobRecommendation())
                .code(200)
                .message("success")
                .build();
    }
}
