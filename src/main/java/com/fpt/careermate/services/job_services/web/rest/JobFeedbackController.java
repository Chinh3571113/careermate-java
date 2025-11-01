package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.job_services.service.dto.request.JobFeedbackRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobFeedbackResponse;
import com.fpt.careermate.services.job_services.service.impl.JobFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-feedback")
@Tag(name = "Job Feedback", description = "Job feedback management APIs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobFeedbackController {

    JobFeedbackService jobFeedbackService;

    @PostMapping
    @Operation(summary = "Create job feedback", description = "Create a new job feedback (like, dislike, save, view)")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<JobFeedbackResponse> createJobFeedback(@Valid @RequestBody JobFeedbackRequest request) {
        log.info("REST request to create job feedback: {}", request);
        JobFeedbackResponse response = jobFeedbackService.createJobFeedback(request);
        return ApiResponse.<JobFeedbackResponse>builder()
                .code(201)
                .message("Job feedback created successfully")
                .result(response)
                .build();
    }

    @DeleteMapping
    @Operation(summary = "Remove job feedback", description = "Remove a job feedback by job ID, candidate ID and feedback type")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ApiResponse<Void> removeJobFeedback(
            @RequestParam int jobId,
            @RequestParam int candidateId,
            @RequestParam String feedbackType) {
        log.info("REST request to remove job feedback - jobId: {}, candidateId: {}, type: {}",
                 jobId, candidateId, feedbackType);
        jobFeedbackService.removeJobFeedback(jobId, candidateId, feedbackType);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Job feedback removed successfully")
                .build();
    }

    @GetMapping
    @Operation(summary = "Get job feedback", description = "Get a specific job feedback by job ID, candidate ID and feedback type")
    public ApiResponse<JobFeedbackResponse> getJobFeedback(
            @RequestParam int jobId,
            @RequestParam int candidateId,
            @RequestParam String feedbackType) {
        log.info("REST request to get job feedback - jobId: {}, candidateId: {}, type: {}",
                 jobId, candidateId, feedbackType);
        JobFeedbackResponse response = jobFeedbackService.getJobFeedback(jobId, candidateId, feedbackType);
        return ApiResponse.<JobFeedbackResponse>builder()
                .code(200)
                .result(response)
                .build();
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get feedbacks by job", description = "Get all feedbacks for a specific job posting")
    public ApiResponse<List<JobFeedbackResponse>> getJobFeedbacksByJobId(@PathVariable int jobId) {
        log.info("REST request to get all feedbacks for job: {}", jobId);
        List<JobFeedbackResponse> responses = jobFeedbackService.getJobFeedbacksByJobId(jobId);
        return ApiResponse.<List<JobFeedbackResponse>>builder()
                .code(200)
                .result(responses)
                .build();
    }

    @GetMapping("/candidate/{candidateId}")
    @Operation(summary = "Get feedbacks by candidate", description = "Get all feedbacks from a specific candidate")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ApiResponse<List<JobFeedbackResponse>> getJobFeedbacksByCandidateId(@PathVariable int candidateId) {
        log.info("REST request to get all feedbacks for candidate: {}", candidateId);
        List<JobFeedbackResponse> responses = jobFeedbackService.getJobFeedbacksByCandidateId(candidateId);
        return ApiResponse.<List<JobFeedbackResponse>>builder()
                .code(200)
                .result(responses)
                .build();
    }

    @GetMapping("/candidate/{candidateId}/type/{feedbackType}")
    @Operation(summary = "Get feedbacks by candidate and type", description = "Get all feedbacks from a specific candidate with specific feedback type")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'ADMIN')")
    public ApiResponse<List<JobFeedbackResponse>> getJobFeedbacksByCandidateIdAndType(
            @PathVariable int candidateId,
            @PathVariable String feedbackType) {
        log.info("REST request to get feedbacks for candidate: {} with type: {}", candidateId, feedbackType);
        List<JobFeedbackResponse> responses = jobFeedbackService.getJobFeedbacksByCandidateIdAndType(candidateId, feedbackType);
        return ApiResponse.<List<JobFeedbackResponse>>builder()
                .code(200)
                .result(responses)
                .build();
    }

    @GetMapping("/all")
    @Operation(summary = "Get all feedbacks", description = "Get all job feedbacks in the system")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<JobFeedbackResponse>> getAllJobFeedbacks() {
        log.info("REST request to get all job feedbacks");
        List<JobFeedbackResponse> responses = jobFeedbackService.getAllJobFeedbacks();
        return ApiResponse.<List<JobFeedbackResponse>>builder()
                .code(200)
                .result(responses)
                .build();
    }
}
