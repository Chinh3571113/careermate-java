package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.job_services.service.JobPostingImp;
import com.fpt.careermate.services.job_services.service.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForRecruiterResponse;
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

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/jobposting")
@Tag(name = "Job posting", description = "Manage job posting")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class JobPostingController {

    JobPostingImp jobPostingImp;

    @PostMapping
    @Operation(summary = "Recruiter create job posting")
    ApiResponse<String> createJobPosting(@Valid @RequestBody JobPostingCreationRequest request) {
        jobPostingImp.createJobPosting(request);
        return ApiResponse.<String>builder()
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/recruiter")
    @Operation(summary = "Recruiter can manage all job postings of the current recruiter with all status")
    ApiResponse<List<JobPostingForRecruiterResponse>> getJobPostingListForRecruiter() {
        return ApiResponse.<List<JobPostingForRecruiterResponse>>builder()
                .result(jobPostingImp.getAllJobPostingForRecruiter())
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/recruiter/{id}")
    @Operation(summary = "Recruiter can get job postings detail of the current recruiter with all status")
    ApiResponse<JobPostingForRecruiterResponse> getJobPostingDetailForRecruiter(@NotNull @PathVariable int id) {
        return ApiResponse.<JobPostingForRecruiterResponse>builder()
                .result(jobPostingImp.getJobPostingDetailForRecruiter(id))
                .code(200)
                .message("success")
                .build();
    }

    @PutMapping("/recruiter/{id}")
    @Operation(summary = "Recruiter can update job postings detail of the current recruiter with PENDING or REJECTED status, EXPIRED and ACTIVE can only extend expiration date or shorten expiration date")
    ApiResponse<JobPostingForRecruiterResponse> updateJobPostingDetailForRecruiter(
            @NotNull @PathVariable int id,
            @RequestBody JobPostingCreationRequest request
    ) {
        jobPostingImp.updateJobPosting(id, request);

        return ApiResponse.<JobPostingForRecruiterResponse>builder()
                .code(200)
                .message("success")
                .build();
    }

    @DeleteMapping("/recruiter/{id}")
    @Operation(summary = "Recruiter can delete job postings of the current recruiter with PENDING or REJECTED or EXPIRED status")
    ApiResponse<JobPostingForRecruiterResponse> deleteJobPostingForRecruiter(@PathVariable int id) {
        jobPostingImp.deleteJobPosting(id);

        return ApiResponse.<JobPostingForRecruiterResponse>builder()
                .code(200)
                .message("success")
                .build();
    }

    @PatchMapping("/recruiter/{id}/pause")
    @Operation(summary = "Recruiter can pause job postings of the current recruiter with ACTIVE status")
    ApiResponse<String> pauseJobPostingForRecruiter(@PathVariable int id) {
        jobPostingImp.pauseJobPosting(id);

        return ApiResponse.<String>builder()
                .code(200)
                .message("success")
                .build();

    }

}
