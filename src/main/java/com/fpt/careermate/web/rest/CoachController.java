package com.fpt.careermate.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.careermate.services.CoachImp;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.CourseListResponse;
import com.fpt.careermate.services.dto.response.CourseResponse;
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

    @PostMapping("/course/generation")
    public ApiResponse<CourseResponse> generateCourse(@RequestParam String topic) {
        return ApiResponse.<CourseResponse>builder()
                .result(coachImp.generateCourse(topic))
                .code(200)
                .message("success")
                .build();
    }

    @PostMapping("/course/lesson/generation/{lessonId}")
    @Operation(description = """
            Generate lesson content by lesson ID if not exists, return existing lesson content otherwise
            """)
    public ApiResponse<String> generateLessonContent(@PathVariable int lessonId) throws JsonProcessingException {
        return ApiResponse.<String>builder()
                .result(coachImp.generateLesson(lessonId))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/course")
    public ApiResponse<List<CourseListResponse>> getMyCourses() {
        return ApiResponse.<List<CourseListResponse>>builder()
                .result(coachImp.getMyCourses())
                .code(200)
                .message("success")
                .build();
    }

}
