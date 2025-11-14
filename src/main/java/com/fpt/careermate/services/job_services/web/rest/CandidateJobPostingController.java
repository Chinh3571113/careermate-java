package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.job_services.service.JobPostingImp;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForCandidateResponse;
import com.fpt.careermate.services.job_services.service.dto.response.PageJobPostingForRecruiterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/job-postings")
@Tag(name = "Job Postings", description = "Public API for viewing and searching approved job postings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidateJobPostingController {

    JobPostingImp jobPostingImp;

    @GetMapping
    @Operation(
        summary = "Get All Approved Job Postings",
        description = """
            Retrieve all approved and active job postings that haven't expired.
            Supports search by keyword (searches in title, description, and address).
            Results are paginated and sorted by creation date (newest first).
            
            Query Parameters:
            - keyword: Optional search term
            - page: Page number (default: 0)
            - size: Items per page (default: 10)
            - sortBy: Field to sort by (default: createAt)
            - sortDir: Sort direction - asc or desc (default: desc)
            
            Examples:
            - /api/job-postings?page=0&size=10
            - /api/job-postings?keyword=developer&page=0&size=20
            - /api/job-postings?keyword=java&sortBy=expirationDate&sortDir=asc
            """
    )
    public ApiResponse<PageResponse<JobPostingForCandidateResponse>> getAllJobPostings(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        log.info("Public API: Fetching job postings - keyword: {}, page: {}, size: {}", keyword, page, size);

        // Create sort object
        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        PageResponse<JobPostingForCandidateResponse> response =
            jobPostingImp.getAllApprovedJobPostings(keyword, pageable);

        return ApiResponse.<PageResponse<JobPostingForCandidateResponse>>builder()
                .code(200)
                .message("Job postings retrieved successfully")
                .result(response)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get Job Posting Detail",
        description = """
            Retrieve detailed information about a specific approved job posting.
            Only approved and non-expired job postings can be viewed.
            
            Includes:
            - Complete job description
            - Required skills (must-have and nice-to-have)
            - Company information
            - Salary range and benefits
            - Work model and location
            - Years of experience required
            """
    )
    public ApiResponse<JobPostingForCandidateResponse> getJobPostingDetail(@PathVariable int id) {
        log.info("Public API: Fetching job posting detail for ID: {}", id);

        JobPostingForCandidateResponse response = jobPostingImp.getJobPostingDetailForCandidate(id);

        return ApiResponse.<JobPostingForCandidateResponse>builder()
                .code(200)
                .message("Job posting detail retrieved successfully")
                .result(response)
                .build();
    }

    @GetMapping("/company/list/{recruiterId}")
    @Operation(description = """
            Get Job Postings of a Specific Company
            Retrieve all job postings associated with a specific company (recruiter).
            Supports pagination and optional keyword search.
            Query Parameters:
            - page: Page number
            - size: Items per page
            - keyword: Optional search term to filter job postings
            """)
    public ApiResponse<PageJobPostingForRecruiterResponse> getJobPostingsOfCompany(
            @PathVariable int recruiterId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.<PageJobPostingForRecruiterResponse>builder()
                .code(200)
                .message("List job posting of Company detail retrieved successfully")
                .result(jobPostingImp.getAllJobPostingsPublic(page, size, keyword, recruiterId))
                .build();
    }

    @GetMapping("/company/{recruiterId}")
    @Operation(description = """
            Get Company Detail
            Retrieve detailed information about a specific company (recruiter).
            """)
    public ApiResponse<JobPostingForCandidateResponse.RecruiterCompanyInfo> getCompanyDetail(
            @PathVariable int recruiterId
    ) {
        return ApiResponse.<JobPostingForCandidateResponse.RecruiterCompanyInfo>builder()
                .code(200)
                .message("Company detail retrieved successfully")
                .result(jobPostingImp.getCompanyDetail(recruiterId))
                .build();
    }

}

