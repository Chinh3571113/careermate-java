package com.fpt.careermate.services.coach_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.coach_services.service.CourseImp;
import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
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

    CourseImp courseImp;

    @GetMapping("/course/recommendation")
    @Operation(description = """
                Recommend courses based on user's role
                input: role (e.g., 'data science', 'backend developer', 'frontend developer', etc.)
                output: list of recommended courses including course title and similarity score
                Do not need login to access this API
            """)
    public ApiResponse<List<RecommendedCourseResponse>> recommendCourses(@RequestParam String role) {
        return ApiResponse.<List<RecommendedCourseResponse>>builder()
                .result(courseImp.recommendCourse(role))
                .code(200)
                .message("success")
                .build();
    }

    @PostMapping("/course")
    @Operation(description = """
                Add a new course to Postgres when candidate select a course from recommendation list
                input: CourseCreationRequest including title and url
                output: success message
                Need to login as candidate to access this API
            """)
    public ApiResponse<String> addCourse(@RequestBody CourseCreationRequest request) {
        courseImp.addCourse(request);
        return ApiResponse.<String>builder()
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/course/marked")
    @Operation(description = """
                Get my courses with marked status
                input: none
                output: list of my courses including course id title, url, marked data
                Need to login as CANDIDATE to access this API
                
                int number: The current page number (starts from 0)
                int size: The number of elements per page (page size)
                long totalElements: The total number of elements across all pages
                int totalPages: The total number of available pages
                boolean first: Indicates whether this is the first page
                boolean last: Indicates whether this is the last page
            """)
    public ApiResponse<CoursePageResponse> getMyCourseWithMarkedStatus(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return ApiResponse.<CoursePageResponse>builder()
                .result(courseImp.getMyCoursesWithMarkedStatus(page, size))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/course/unmarked")
    @Operation(description = """
                Get my courses with unmarked status
                
                input: none
                output: list of my courses including course id title, url, marked data
                Need to login as CANDIDATE to access this API
                
                int number: The current page number (starts from 0)
                int size: The number of elements per page (page size)
                long totalElements: The total number of elements across all pages
                int totalPages: The total number of available pages
                boolean first: Indicates whether this is the first page
                boolean last: Indicates whether this is the last page
            """)
    public ApiResponse<CoursePageResponse> getMyCourseWithUnMarkedStatus(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return ApiResponse.<CoursePageResponse>builder()
                .result(courseImp.getMyCoursesWithUnMarkedStatus(page, size))
                .code(200)
                .message("success")
                .build();
    }

    @PatchMapping("/course/marked/{courseId}")
    @Operation(description = """
                Mark or unmark a course
                input: courseId, marked (true for marked, false for unmarked)
                output: none
                Need to login as CANDIDATE to access this API
            """)
    public ApiResponse<CoursePageResponse> markCourse(
            @RequestParam int courseId,
            @RequestParam boolean marked
    ) {
        courseImp.markCourse(courseId, marked);

        return ApiResponse.<CoursePageResponse>builder()
                .code(200)
                .message("success")
                .build();
    }

}