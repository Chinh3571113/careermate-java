package com.fpt.careermate.services.resume_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.common.constant.ResumeType;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.resume_services.service.ResumeImp;
import com.fpt.careermate.services.resume_services.service.dto.request.ResumeRequest;
import com.fpt.careermate.services.resume_services.service.dto.request.ResumeStatusRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.ResumeResponse;
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
 * Controller tests for ResumeController
 * Function Code: RES-CTRL-001 to RES-CTRL-014
 */
@WebMvcTest(ResumeController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ResumeController Tests")
class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ResumeImp resumeImp;

    private ResumeRequest validRequest;
    private ResumeResponse testResponse;

    @BeforeEach
    void setUp() {
        validRequest = ResumeRequest.builder()
                .aboutMe("Professional summary")
                .resumeUrl("https://example.com/resume.pdf")
                .type(ResumeType.WEB)
                .isActive(true)
                .build();

        testResponse = ResumeResponse.builder()
                .resumeId(1)
                .aboutMe("Professional summary")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("POST /api/resume - Create Resume")
    class CreateResumeTests {

        @Test
        @DisplayName("TC001: Create resume with valid data returns 200 OK")
        void createResume_WithValidData_Returns200() throws Exception {
            when(resumeImp.createResume(any(ResumeRequest.class))).thenReturn(testResponse);

            ResultActions result = mockMvc.perform(post("/api/resume")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Create resume successfully"));
        }

        @Test
        @DisplayName("TC002: Create resume with unauthorized user returns 401")
        void createResume_Unauthorized_Returns401() throws Exception {
            when(resumeImp.createResume(any(ResumeRequest.class)))
                    .thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

            ResultActions result = mockMvc.perform(post("/api/resume")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)));

            result.andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/resume - Get All Resumes by Candidate")
    class GetAllResumesTests {

        @Test
        @DisplayName("TC003: Get all resumes returns 200 OK with list")
        void getAllResumes_Returns200WithList() throws Exception {
            List<ResumeResponse> responses = Arrays.asList(testResponse);
            when(resumeImp.getAllResumesByCandidate()).thenReturn(responses);

            ResultActions result = mockMvc.perform(get("/api/resume")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.message").value("Get resumes successfully"));
        }

        @Test
        @DisplayName("TC004: Get all resumes when empty returns empty list")
        void getAllResumes_Empty_ReturnsEmptyList() throws Exception {
            when(resumeImp.getAllResumesByCandidate()).thenReturn(Collections.emptyList());

            ResultActions result = mockMvc.perform(get("/api/resume")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/resume/{resumeId} - Get Resume by ID")
    class GetResumeByIdTests {

        @Test
        @DisplayName("TC005: Get existing resume returns 200 OK")
        void getResume_Exists_Returns200() throws Exception {
            when(resumeImp.getResumeById(1)).thenReturn(testResponse);

            ResultActions result = mockMvc.perform(get("/api/resume/1")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.resumeId").value(1))
                    .andExpect(jsonPath("$.message").value("Get resume successfully"));
        }

        @Test
        @DisplayName("TC006: Get non-existent resume returns 400 Bad Request")
        void getResume_NotExists_Returns400() throws Exception {
            when(resumeImp.getResumeById(999))
                    .thenThrow(new AppException(ErrorCode.RESUME_NOT_FOUND));

            ResultActions result = mockMvc.perform(get("/api/resume/999")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/resume/{resumeId} - Delete Resume")
    class DeleteResumeTests {

        @Test
        @DisplayName("TC007: Delete resume returns 200 OK")
        void deleteResume_Returns200() throws Exception {
            doNothing().when(resumeImp).deleteResume(1);

            ResultActions result = mockMvc.perform(delete("/api/resume/1")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Delete resume successfully"));
        }

        @Test
        @DisplayName("TC008: Delete non-existent resume returns 400 Bad Request")
        void deleteResume_NotExists_Returns400() throws Exception {
            doThrow(new AppException(ErrorCode.RESUME_NOT_FOUND))
                    .when(resumeImp).deleteResume(999);

            ResultActions result = mockMvc.perform(delete("/api/resume/999")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/resume/{resumeId} - Update Resume")
    class UpdateResumeTests {

        @Test
        @DisplayName("TC009: Update resume returns 200 OK")
        void updateResume_Returns200() throws Exception {
            when(resumeImp.updateResume(eq(1), any(ResumeRequest.class))).thenReturn(testResponse);

            ResultActions result = mockMvc.perform(put("/api/resume/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Update resume successfully"));
        }

        @Test
        @DisplayName("TC010: Update non-existent resume returns 400 Bad Request")
        void updateResume_NotExists_Returns400() throws Exception {
            when(resumeImp.updateResume(eq(999), any(ResumeRequest.class)))
                    .thenThrow(new AppException(ErrorCode.RESUME_NOT_FOUND));

            ResultActions result = mockMvc.perform(put("/api/resume/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validRequest)));

            result.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/resume/{resumeId}/status - Update Resume Status")
    class UpdateResumeStatusTests {

        @Test
        @DisplayName("TC011: Update resume status returns 200 OK")
        void updateResumeStatus_Returns200() throws Exception {
            ResumeStatusRequest statusRequest = new ResumeStatusRequest();
            statusRequest.setIsActive(true);
            when(resumeImp.patchResumeStatus(eq(1), any(ResumeStatusRequest.class))).thenReturn(testResponse);

            ResultActions result = mockMvc.perform(patch("/api/resume/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(statusRequest)));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Update resume status successfully"));
        }

        @Test
        @DisplayName("TC012: Update status of non-existent resume returns 400 Bad Request")
        void updateResumeStatus_NotExists_Returns400() throws Exception {
            ResumeStatusRequest statusRequest = new ResumeStatusRequest();
            statusRequest.setIsActive(true);
            when(resumeImp.patchResumeStatus(eq(999), any(ResumeStatusRequest.class)))
                    .thenThrow(new AppException(ErrorCode.RESUME_NOT_FOUND));

            ResultActions result = mockMvc.perform(patch("/api/resume/999/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(statusRequest)));

            result.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/resume/{resumeId}/type/{type} - Update Resume Type")
    class UpdateResumeTypeTests {

        @Test
        @DisplayName("TC013: Update resume type returns 200 OK")
        void updateResumeType_Returns200() throws Exception {
            when(resumeImp.patchResumeType(eq(1), eq(ResumeType.WEB))).thenReturn(testResponse);

            ResultActions result = mockMvc.perform(patch("/api/resume/1/type/WEB")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Update resume type successfully"));
        }
    }

    @Nested
    @DisplayName("GET /api/resume/type/{type} - Get Resumes by Type")
    class GetResumesByTypeTests {

        @Test
        @DisplayName("TC014: Get resumes by type WEB returns 200 OK")
        void getResumesByTypeWEB_Returns200() throws Exception {
            List<ResumeResponse> responses = Arrays.asList(testResponse);
            when(resumeImp.getResumesByType(ResumeType.WEB)).thenReturn(responses);

            ResultActions result = mockMvc.perform(get("/api/resume/type/WEB")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.message").value("Get resumes by type successfully"));
        }

        @Test
        @DisplayName("TC015: Get resumes by type UPLOAD returns 200 OK")
        void getResumesByTypeUPLOAD_Returns200() throws Exception {
            List<ResumeResponse> responses = Arrays.asList(testResponse);
            when(resumeImp.getResumesByType(ResumeType.UPLOAD)).thenReturn(responses);

            ResultActions result = mockMvc.perform(get("/api/resume/type/UPLOAD")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray());
        }

        @Test
        @DisplayName("TC016: Get resumes by type DRAFT returns 200 OK")
        void getResumesByTypeDRAFT_Returns200() throws Exception {
            List<ResumeResponse> responses = Arrays.asList(testResponse);
            when(resumeImp.getResumesByType(ResumeType.DRAFT)).thenReturn(responses);

            ResultActions result = mockMvc.perform(get("/api/resume/type/DRAFT")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").isArray());
        }
    }
}
