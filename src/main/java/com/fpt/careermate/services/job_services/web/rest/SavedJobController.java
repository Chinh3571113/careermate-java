package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.job_services.service.JobPostingImp;
import com.fpt.careermate.services.job_services.service.SavedJobImp;
import com.fpt.careermate.services.job_services.service.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.job_services.service.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/saved-jobs")
@Tag(name = "Candidate - Saved Job Posting", description = "Manage job posting")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SavedJobController {

   SavedJobImp savedJobImp;

    @PostMapping("/toggle/{jobId}")
    @Operation(summary = "Candidate can save or unsave a job posting")
    ApiResponse<Boolean> toggleSaveJob(@PathVariable int jobId) {
        boolean isSaved = savedJobImp.toggleSaveJob(jobId);
        return ApiResponse.<Boolean>builder()
                .result(isSaved)
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping
    @Operation(summary = "Candidate can get all saved job postings")
    ApiResponse<PageSavedJobPostingResponse> getSavedJobs(
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "5") int size
    ) {
        return ApiResponse.<PageSavedJobPostingResponse>builder()
                .result(savedJobImp.getSavedJobs(page, size))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/jobs-for-candidate")
    @Operation(summary = "Get Jobs")
    public ApiResponse<PageJobPostingForCandidateResponse> getJobsForCandidate(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ApiResponse.<PageJobPostingForCandidateResponse>builder()
                .result(savedJobImp.getJobsForCandidate(page, size))
                .code(200)
                .message("success")
                .build();
    }
}
