package com.fpt.careermate.services.coach_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.coach_services.service.CoachImp;
import com.fpt.careermate.services.coach_services.service.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/coach")
@Tag(name = "Coach", description = "Generate course")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CoachController {

    CoachImp coachImp;

    @GetMapping("/course/recommendation")
    @Operation(description = """
                Recommend courses based on user's role
                input: role (e.g., 'data science', 'backend developer', 'frontend developer', etc.)
                output: list of recommended courses including course title and similarity score
                Do not need login to access this API
            """)
    public ApiResponse<List<RecommendedCourseResponse>> recommendCourses(@RequestParam String role) {
        return ApiResponse.<List<RecommendedCourseResponse>>builder()
                .result(coachImp.recommendCourse(role))
                .code(200)
                .message("success")
                .build();
    }

}
