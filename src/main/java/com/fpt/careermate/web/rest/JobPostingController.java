package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.JobPostingImp;
import com.fpt.careermate.services.dto.request.AccountCreationRequest;
import com.fpt.careermate.services.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @GetMapping("/candidate")
    @Operation(summary = "Candidate can view all active job postings of all recruiters")
    ApiResponse<List<JobPostingResponse>> getJobPostingList() {
        return ApiResponse.<List<JobPostingResponse>>builder()
                .result(jobPostingImp.getAllJobPostings())
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/admin")
    @Operation(summary = "Admin can manage all job postings of all recruiters with all status")
    ApiResponse<List<JobPostingForAdminResponse>> getJobPostingListForAdmin() {
        return ApiResponse.<List<JobPostingForAdminResponse>>builder()
                .result(jobPostingImp.getAllJobPostingsForAdmin())
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

}
