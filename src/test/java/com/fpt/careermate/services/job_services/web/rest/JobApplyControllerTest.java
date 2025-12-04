package com.fpt.careermate.services.job_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.job_services.service.JobApplyImp;
import com.fpt.careermate.services.job_services.service.dto.request.JobApplyRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobApplyResponse;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for JobApplyController
 * Function Code: JA-CTRL-001 to JA-CTRL-015
 */
@WebMvcTest(JobApplyController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("JobApplyController Tests")
class JobApplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobApplyImp jobApplyImp;

    private JobApplyRequest validRequest;
    private JobApplyResponse testResponse;

    @BeforeEach
    void setUp() {
        validRequest = JobApplyRequest.builder()
                .jobPostingId(1)
                .candidateId(1)
                .cvFilePath("/path/to/cv.pdf")
                .fullName("Test User")
                .phoneNumber("1234567890")
                .preferredWorkLocation("Remote")
                .status("SUBMITTED")
                .build();

        testResponse = JobApplyResponse.builder()
                .id(1)
                .status("SUBMITTED")
                .build();
    }

    @Nested
    @DisplayName("POST /api/job-apply - Create Job Application")
    class CreateJobApplyTests {

        @Test
        @DisplayName("TC001: Create job application with valid data returns 200 OK")
        void createJobApply_WithValidData_Returns200() throws Exception {
            when(jobApplyImp.createJobApply(any(JobApplyRequest.class))).thenReturn(testResponse);

            ResultActions result = mockMvc.perform(post("/api/job-apply")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Job application created successfully"));
        }

        @Test
        @DisplayName("TC002: Create job application for non-existent job returns 404")
        void createJobApply_JobNotExists_Returns404() throws Exception {
            when(jobApplyImp.createJobApply(any(JobApplyRequest.class)))
                    .thenThrow(new AppException(ErrorCode.JOB_POSTING_NOT_FOUND));

            ResultActions result = mockMvc.perform(post("/api/job-apply")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)));

            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/job-apply/{id} - Get Job Application by ID")
    class GetJobApplyByIdTests {

        @Test
        @DisplayName("TC003: Get existing job application returns 200 OK")
        void getJobApply_Exists_Returns200() throws Exception {
            when(jobApplyImp.getJobApplyById(1)).thenReturn(testResponse);

            ResultActions result = mockMvc.perform(get("/api/job-apply/1")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value(1));
        }

        @Test
        @DisplayName("TC004: Get non-existent job application returns 404")
        void getJobApply_NotExists_Returns404() throws Exception {
            when(jobApplyImp.getJobApplyById(999))
                    .thenThrow(new AppException(ErrorCode.JOB_APPLY_NOT_FOUND));

            ResultActions result = mockMvc.perform(get("/api/job-apply/999")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/job-apply - Get All Job Applications")
    class GetAllJobAppliesTests {

        @Test
        @DisplayName("TC005: Get all job applications returns 200 OK with list")
        void getAllJobApplies_Returns200WithList() throws Exception {
            List<JobApplyResponse> responses = Arrays.asList(testResponse);
            when(jobApplyImp.getAllJobApplies()).thenReturn(responses);

            ResultActions result = mockMvc.perform(get("/api/job-apply")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray());
        }

        @Test
        @DisplayName("TC006: Get all job applications when empty returns empty list")
        void getAllJobApplies_Empty_ReturnsEmptyList() throws Exception {
            when(jobApplyImp.getAllJobApplies()).thenReturn(Collections.emptyList());

            ResultActions result = mockMvc.perform(get("/api/job-apply")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/job-apply/job-posting/{jobPostingId} - Get Applications by Job Posting")
    class GetJobAppliesByJobPostingTests {

        @Test
        @DisplayName("TC007: Get applications by job posting returns 200 OK")
        void getByJobPosting_Returns200() throws Exception {
            List<JobApplyResponse> responses = Arrays.asList(testResponse);
            when(jobApplyImp.getJobAppliesByJobPosting(1)).thenReturn(responses);

            ResultActions result = mockMvc.perform(get("/api/job-apply/job-posting/1")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/job-apply/candidate/{candidateId} - Get Applications by Candidate")
    class GetJobAppliesByCandidateTests {

        @Test
        @DisplayName("TC008: Get applications by candidate returns 200 OK")
        void getByCandidate_Returns200() throws Exception {
            List<JobApplyResponse> responses = Arrays.asList(testResponse);
            when(jobApplyImp.getJobAppliesByCandidate(1)).thenReturn(responses);

            ResultActions result = mockMvc.perform(get("/api/job-apply/candidate/1")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/job-apply/{id} - Update Job Application Status")
    class UpdateJobApplyTests {

        @Test
        @DisplayName("TC009: Update application status returns 200 OK")
        void updateJobApply_Returns200() throws Exception {
            when(jobApplyImp.updateJobApply(eq(1), any(StatusJobApply.class))).thenReturn(testResponse);

            ResultActions result = mockMvc.perform(put("/api/job-apply/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(StatusJobApply.REVIEWING)));

            result.andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC010: Update non-existent application returns 404")
        void updateJobApply_NotExists_Returns404() throws Exception {
            when(jobApplyImp.updateJobApply(eq(999), any(StatusJobApply.class)))
                    .thenThrow(new AppException(ErrorCode.JOB_APPLY_NOT_FOUND));

            ResultActions result = mockMvc.perform(put("/api/job-apply/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(StatusJobApply.REVIEWING)));

            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/job-apply/candidate/{candidateId}/filter - Get Applications with Filter")
    class GetJobAppliesWithFilterTests {

        @Test
        @DisplayName("TC011: Get applications with filter returns 200 OK")
        void getWithFilter_Returns200() throws Exception {
            PageResponse<JobApplyResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(testResponse), 0, 10, 1L, 1);
            when(jobApplyImp.getJobAppliesByCandidateWithFilter(eq(1), any(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            ResultActions result = mockMvc.perform(get("/api/job-apply/candidate/1/filter")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.content").isArray());
        }

        @Test
        @DisplayName("TC012: Get applications with status filter returns filtered list")
        void getWithStatusFilter_ReturnsFiltered() throws Exception {
            PageResponse<JobApplyResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(testResponse), 0, 10, 1L, 1);
            when(jobApplyImp.getJobAppliesByCandidateWithFilter(eq(1), eq(StatusJobApply.SUBMITTED), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            ResultActions result = mockMvc.perform(get("/api/job-apply/candidate/1/filter")
                    .param("status", "SUBMITTED")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/job-apply/{id} - Delete Job Application")
    class DeleteJobApplyTests {

        @Test
        @DisplayName("TC013: Delete application returns 200 OK")
        void deleteJobApply_Returns200() throws Exception {
            doNothing().when(jobApplyImp).deleteJobApply(1);

            ResultActions result = mockMvc.perform(delete("/api/job-apply/1")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Job application deleted successfully"));
        }

        @Test
        @DisplayName("TC014: Delete non-existent application returns 404")
        void deleteJobApply_NotExists_Returns404() throws Exception {
            doThrow(new AppException(ErrorCode.JOB_APPLY_NOT_FOUND))
                    .when(jobApplyImp).deleteJobApply(999);

            ResultActions result = mockMvc.perform(delete("/api/job-apply/999")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/job-apply/recruiter - Get Applications for Recruiter")
    class GetJobAppliesByRecruiterTests {

        @Test
        @DisplayName("TC015: Get recruiter applications returns 200 OK")
        void getRecruiterApplications_Returns200() throws Exception {
            List<JobApplyResponse> responses = Arrays.asList(testResponse);
            when(jobApplyImp.getJobAppliesByRecruiter()).thenReturn(responses);

            ResultActions result = mockMvc.perform(get("/api/job-apply/recruiter")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/job-apply/recruiter/filter - Get Recruiter Applications with Filter")
    class GetRecruiterApplicationsWithFilterTests {

        @Test
        @DisplayName("TC016: Get recruiter applications with filter returns 200 OK")
        void getRecruiterWithFilter_Returns200() throws Exception {
            PageResponse<JobApplyResponse> pageResponse = new PageResponse<>(
                    Collections.singletonList(testResponse), 0, 10, 1L, 1);
            when(jobApplyImp.getJobAppliesByRecruiterWithFilter(any(), anyInt(), anyInt()))
                    .thenReturn(pageResponse);

            ResultActions result = mockMvc.perform(get("/api/job-apply/recruiter/filter")
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.content").isArray());
        }
    }
}
