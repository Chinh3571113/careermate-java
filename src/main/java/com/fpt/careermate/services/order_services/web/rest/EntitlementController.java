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
    @Operation(description = """
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
    @Operation(description = """
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

    @GetMapping("/ai-analyzer-checker")
    @Operation(description = """
            Check if candidate can use AI Analyzer feature
            input: none
            output: boolean
            Need login as candidate to access this API
            Use this API before calling AI Analyzer API
            """)
    public ApiResponse<Boolean> canUseAIAnalyzer() {
        return ApiResponse.<Boolean>builder()
                .result(checkerService.canUseAIAnalyzer())
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/cv-builder-checker")
    @Operation(description = """
            Check if candidate can use CV Builder feature
            input: none
            output: boolean
            Need login as candidate to access this API
            Use this API before calling CV Builder API
            """)
    public ApiResponse<Boolean> canCreateNewCV() {
        return ApiResponse.<Boolean>builder()
                .result(checkerService.canCreateNewCV())
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/apply-job-checker")
    @Operation(description = """
            Check if candidate can use Apply Job feature
            input: none
            output: boolean
            Need login as candidate to access this API
            Use this API before calling Apply Job API
            """)
    public ApiResponse<Boolean> canApplyJob() {
        return ApiResponse.<Boolean>builder()
                .result(checkerService.canApplyJob())
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/recruiter-info-checker")
    @Operation(description = """
            Check if candidate can use Recruiter Info feature
            input: none
            output: boolean
            Need login as candidate to access this API
            Use this API before calling Recruiter Info API
            """)
    public ApiResponse<Boolean> canReviewRecruiterInfo() {
        return ApiResponse.<Boolean>builder()
                .result(checkerService.canReviewRecruiterInfo())
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/cv-download-checker")
    @Operation(description = """
            Check if candidate can use Download CV feature
            input: none
            output: boolean
            Need login as candidate to access this API
            Use this API before calling Download CV API
            """)
    public ApiResponse<Boolean> canDownloadCV() {
        return ApiResponse.<Boolean>builder()
                .result(checkerService.canDownloadCV())
                .code(200)
                .message("success")
                .build();
    }
}
