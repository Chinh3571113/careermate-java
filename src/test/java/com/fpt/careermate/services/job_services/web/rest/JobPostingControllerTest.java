package com.fpt.careermate.services.job_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.common.constant.WorkModel;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.job_services.service.JobPostingImp;
import com.fpt.careermate.services.job_services.service.dto.request.JdSkillRequest;
import com.fpt.careermate.services.job_services.service.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForRecruiterResponse;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingStatsResponse;
import com.fpt.careermate.services.job_services.service.dto.response.PageJobPostingForRecruiterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for JobPostingController
 * Function Code: JP-CTRL-001 to JP-CTRL-010
 */
@WebMvcTest(JobPostingController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("JobPostingController Tests")
class JobPostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobPostingImp jobPostingImp;

    private JobPostingCreationRequest validRequest;
    private JobPostingForRecruiterResponse testResponse;

    @BeforeEach
    void setUp() {
        validRequest = JobPostingCreationRequest.builder()
                .title("Software Engineer")
                .description("Job description")
                .address("123 Main Street")
                .expirationDate(LocalDate.now().plusDays(30))
                .jdSkills(new HashSet<>())
                .yearsOfExperience(2)
                .workModel(WorkModel.REMOTE)
                .salaryRange("$50000-$70000")
                .reason("Company expansion")
                .jobPackage("Standard")
                .build();

        testResponse = JobPostingForRecruiterResponse.builder()
                .id(1)
                .title("Software Engineer")
                .build();
    }

    @Nested
    @DisplayName("POST /api/jobposting - Create Job Posting")
    class CreateJobPostingTests {

        @Test
        @DisplayName("TC001: Create job posting with valid data returns 200 OK")
        void createJobPosting_WithValidData_Returns200() throws Exception {
            doNothing().when(jobPostingImp).createJobPosting(any(JobPostingCreationRequest.class));

            ResultActions result = mockMvc.perform(post("/api/jobposting")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"));
        }

        @Test
        @DisplayName("TC002: Create job posting with invalid data returns 400")
        void createJobPosting_WithInvalidData_Returns400() throws Exception {
            doThrow(new AppException(ErrorCode.INVALID_KEY))
                    .when(jobPostingImp).createJobPosting(any(JobPostingCreationRequest.class));

            ResultActions result = mockMvc.perform(post("/api/jobposting")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)));

            result.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/jobposting/recruiter - Get Job Postings for Recruiter")
    class GetJobPostingsForRecruiterTests {

        @Test
        @DisplayName("TC003: Get job postings returns 200 OK with list")
        void getJobPostings_Returns200WithList() throws Exception {
            PageJobPostingForRecruiterResponse pageResponse = PageJobPostingForRecruiterResponse.builder()
                    .content(Collections.singletonList(testResponse))
                    .totalPages(1)
                    .totalElements(1L)
                    .build();

            when(jobPostingImp.getAllJobPostingForRecruiter(anyInt(), anyInt(), any()))
                    .thenReturn(pageResponse);

            ResultActions result = mockMvc.perform(get("/api/jobposting/recruiter")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("TC004: Get job postings with keyword filter returns filtered list")
        void getJobPostings_WithKeyword_ReturnsFilteredList() throws Exception {
            PageJobPostingForRecruiterResponse pageResponse = PageJobPostingForRecruiterResponse.builder()
                    .content(Collections.singletonList(testResponse))
                    .totalPages(1)
                    .totalElements(1L)
                    .build();

            when(jobPostingImp.getAllJobPostingForRecruiter(anyInt(), anyInt(), eq("engineer")))
                    .thenReturn(pageResponse);

            ResultActions result = mockMvc.perform(get("/api/jobposting/recruiter")
                    .param("page", "0")
                    .param("size", "10")
                    .param("keyword", "engineer")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/jobposting/recruiter/{id} - Get Job Posting Detail")
    class GetJobPostingDetailTests {

        @Test
        @DisplayName("TC005: Get existing job posting detail returns 200 OK")
        void getJobPostingDetail_Exists_Returns200() throws Exception {
            when(jobPostingImp.getJobPostingDetailForRecruiter(1)).thenReturn(testResponse);

            ResultActions result = mockMvc.perform(get("/api/jobposting/recruiter/1")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.id").value(1));
        }

        @Test
        @DisplayName("TC006: Get non-existent job posting returns 404")
        void getJobPostingDetail_NotExists_Returns404() throws Exception {
            when(jobPostingImp.getJobPostingDetailForRecruiter(999))
                    .thenThrow(new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

            ResultActions result = mockMvc.perform(get("/api/jobposting/recruiter/999")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/jobposting/recruiter/{id} - Update Job Posting")
    class UpdateJobPostingTests {

        @Test
        @DisplayName("TC007: Update job posting returns 200 OK")
        void updateJobPosting_Returns200() throws Exception {
            doNothing().when(jobPostingImp).updateJobPosting(eq(1), any(JobPostingCreationRequest.class));

            ResultActions result = mockMvc.perform(put("/api/jobposting/recruiter/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("TC008: Update non-existent job posting returns 404")
        void updateJobPosting_NotExists_Returns404() throws Exception {
            doThrow(new AppException(ErrorCode.JOB_POSTING_NOT_FOUND))
                    .when(jobPostingImp).updateJobPosting(eq(999), any(JobPostingCreationRequest.class));

            ResultActions result = mockMvc.perform(put("/api/jobposting/recruiter/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)));

            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/jobposting/recruiter/{id} - Delete Job Posting")
    class DeleteJobPostingTests {

        @Test
        @DisplayName("TC009: Delete job posting returns 200 OK")
        void deleteJobPosting_Returns200() throws Exception {
            doNothing().when(jobPostingImp).deleteJobPosting(1);

            ResultActions result = mockMvc.perform(delete("/api/jobposting/recruiter/1")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("TC010: Delete non-existent job posting returns 404")
        void deleteJobPosting_NotExists_Returns404() throws Exception {
            doThrow(new AppException(ErrorCode.JOB_POSTING_NOT_FOUND))
                    .when(jobPostingImp).deleteJobPosting(999);

            ResultActions result = mockMvc.perform(delete("/api/jobposting/recruiter/999")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/jobposting/recruiter/{id}/pause - Pause Job Posting")
    class PauseJobPostingTests {

        @Test
        @DisplayName("TC011: Pause job posting returns 200 OK")
        void pauseJobPosting_Returns200() throws Exception {
            doNothing().when(jobPostingImp).pauseJobPosting(1);

            ResultActions result = mockMvc.perform(patch("/api/jobposting/recruiter/1/pause")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("TC012: Pause non-existent job posting returns 404")
        void pauseJobPosting_NotExists_Returns404() throws Exception {
            doThrow(new AppException(ErrorCode.JOB_POSTING_NOT_FOUND))
                    .when(jobPostingImp).pauseJobPosting(999);

            ResultActions result = mockMvc.perform(patch("/api/jobposting/recruiter/999/pause")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/jobposting/recruiter/stats - Get Job Posting Stats")
    class GetJobPostingStatsTests {

        @Test
        @DisplayName("TC013: Get job posting stats returns 200 OK")
        void getJobPostingStats_Returns200() throws Exception {
            JobPostingStatsResponse statsResponse = JobPostingStatsResponse.builder()
                    .totalJobPostings(10L)
                    .activeJobPostings(5L)
                    .build();

            when(jobPostingImp.getJobPostingStats()).thenReturn(statsResponse);

            ResultActions result = mockMvc.perform(get("/api/jobposting/recruiter/stats")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/jobposting/work-model - Get Work Models")
    class GetWorkModelsTests {

        @Test
        @DisplayName("TC014: Get work models returns 200 OK with array")
        void getWorkModels_Returns200WithArray() throws Exception {
            ResultActions result = mockMvc.perform(get("/api/jobposting/work-model")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result").isArray());
        }
    }
}
