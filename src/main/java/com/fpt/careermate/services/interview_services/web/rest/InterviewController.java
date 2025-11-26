package com.fpt.careermate.services.interview_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.interview_services.service.dto.request.AnswerQuestionRequest;
import com.fpt.careermate.services.interview_services.service.dto.request.StartInterviewRequest;
import com.fpt.careermate.services.interview_services.service.dto.response.InterviewSessionResponse;
import com.fpt.careermate.services.interview_services.service.dto.response.NextQuestionResponse;
import com.fpt.careermate.services.interview_services.service.impl.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@Tag(name = "Interview Quiz", description = "APIs for AI-powered interview sessions")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class InterviewController {

    InterviewService interviewService;

    @PostMapping("/start")
    @Operation(summary = "Start a new interview session",
            description = "Creates a new interview session and generates 10 questions based on the job description using AI")
    public ApiResponse<InterviewSessionResponse> startInterview(
            @Valid @RequestBody StartInterviewRequest request) {
        return ApiResponse.<InterviewSessionResponse>builder()
                .result(interviewService.startInterview(request))
                .message("Interview started successfully")
                .build();
    }

    @PostMapping("/sessions/{sessionId}/questions/{questionId}/answer")
    public ApiResponse<NextQuestionResponse> answerQuestion(
            @PathVariable int sessionId,
            @PathVariable int questionId,
            @Valid @RequestBody AnswerQuestionRequest request) {
        return ApiResponse.<NextQuestionResponse>builder()
                .result(interviewService.answerQuestion(sessionId, questionId, request))
                .message("Answer submitted successfully")
                .build();
    }

    @GetMapping("/sessions/{sessionId}/next-question")
    public ApiResponse<NextQuestionResponse> getNextQuestion(@PathVariable int sessionId) {
        return ApiResponse.<NextQuestionResponse>builder()
                .result(interviewService.getNextQuestion(sessionId))
                .message("Next question retrieved successfully")
                .build();
    }

    @PostMapping("/sessions/{sessionId}/complete")
    public ApiResponse<InterviewSessionResponse> completeInterview(@PathVariable int sessionId) {
        return ApiResponse.<InterviewSessionResponse>builder()
                .result(interviewService.completeInterview(sessionId))
                .message("Interview completed successfully")
                .build();
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<InterviewSessionResponse> getSession(@PathVariable int sessionId) {
        return ApiResponse.<InterviewSessionResponse>builder()
                .result(interviewService.getSession(sessionId))
                .message("Session retrieved successfully")
                .build();
    }

    @GetMapping("/sessions")
    public ApiResponse<List<InterviewSessionResponse>> getAllSessions() {
        return ApiResponse.<List<InterviewSessionResponse>>builder()
                .result(interviewService.getAllSessions())
                .message("Sessions retrieved successfully")
                .build();
    }
}
