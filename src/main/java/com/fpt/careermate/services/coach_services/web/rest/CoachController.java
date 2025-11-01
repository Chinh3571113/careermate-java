package com.fpt.careermate.services.coach_services.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.coach_services.service.CoachImp;
import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
import com.fpt.careermate.services.coach_services.service.dto.response.CourseListResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.CourseResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.QuestionResponse;
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
    @Operation(description = "Generate course by topic")
    public ApiResponse<CourseResponse> generateCourse(@RequestBody CourseCreationRequest request) {
        return ApiResponse.<CourseResponse>builder()
                .result(coachImp.generateCourse(request))
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
    @Operation(description = "Get my courses")
    public ApiResponse<List<CourseListResponse>> getMyCourses() {
        return ApiResponse.<List<CourseListResponse>>builder()
                .result(coachImp.getMyCourses())
                .code(200)
                .message("success")
                .build();
    }

    @PatchMapping("/course/lesson/mark/{lessonId}")
    @Operation(description = "Mark or unmark lesson as completed")
    public ApiResponse<Void> markLesson(@PathVariable int lessonId, @RequestParam boolean marked) {
        coachImp.markLesson(lessonId, marked);

        return ApiResponse.<Void>builder()
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/course/{courseId}")
    @Operation(description = "Get course detail by course ID")
    public ApiResponse<CourseResponse> getCourseById(@PathVariable int courseId) {
        return ApiResponse.<CourseResponse>builder()
                .result(coachImp.getCourseById(courseId))
                .code(200)
                .message("success")
                .build();
    }

    @PostMapping("/course/lesson/question/generation/{lessonId}")
    @Operation(description = "if question not exists, generate question for lesson by lesson ID, return existing question otherwise")
    public ApiResponse<List<QuestionResponse>> generateQuestionList(@PathVariable int lessonId) {
        return ApiResponse.<List<QuestionResponse>>builder()
                .result(coachImp.generateQuestionList(lessonId))
                .code(200)
                .message("success")
                .build();
    }

}
