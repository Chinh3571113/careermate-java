package com.fpt.careermate.services.admin_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.job_services.service.JobPostingImp;
import com.fpt.careermate.services.job_services.service.WeaviateImp;
import com.fpt.careermate.services.job_services.service.dto.request.JobPostingApprovalRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForAdminResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminJobPostingController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminJobPostingController Tests")
class AdminJobPostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobPostingImp jobPostingImp;

    @MockBean
    private WeaviateImp weaviateImp;

    @Nested
    @DisplayName("GET /api/admin/jobpostings")
    class GetAllJobPostingsTests {

        @Test
        @DisplayName("Should get all job postings with pagination")
        void shouldGetAllJobPostingsWithPagination() throws Exception {
            Page<JobPostingForAdminResponse> page = new PageImpl<>(Collections.emptyList());
            when(jobPostingImp.getAllJobPostingsForAdmin(anyInt(), anyInt(), any(), any(), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/admin/jobpostings")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(jobPostingImp).getAllJobPostingsForAdmin(0, 20, null, "createAt", "DESC");
        }
    }

    @Nested
    @DisplayName("GET /api/admin/jobpostings/{id}")
    class GetJobPostingDetailTests {

        @Test
        @DisplayName("Should get job posting detail")
        void shouldGetJobPostingDetail() throws Exception {
            JobPostingForAdminResponse response = new JobPostingForAdminResponse();
            when(jobPostingImp.getJobPostingDetailForAdmin(1)).thenReturn(response);

            mockMvc.perform(get("/api/admin/jobpostings/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(jobPostingImp).getJobPostingDetailForAdmin(1);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/jobpostings/pending")
    class GetPendingJobPostingsTests {

        @Test
        @DisplayName("Should get pending job postings")
        void shouldGetPendingJobPostings() throws Exception {
            List<JobPostingForAdminResponse> list = Arrays.asList(new JobPostingForAdminResponse());
            when(jobPostingImp.getPendingJobPostings()).thenReturn(list);

            mockMvc.perform(get("/api/admin/jobpostings/pending"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(jobPostingImp).getPendingJobPostings();
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/jobpostings/{id}/approval")
    class ApproveOrRejectJobPostingTests {

        @Test
        @DisplayName("Should approve job posting")
        void shouldApproveJobPosting() throws Exception {
            JobPostingApprovalRequest request = new JobPostingApprovalRequest();
            request.setStatus("APPROVED");

            doNothing().when(jobPostingImp).approveOrRejectJobPosting(eq(1), any());

            mockMvc.perform(put("/api/admin/jobpostings/1/approval")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Job posting approved and activated successfully"));

            verify(jobPostingImp).approveOrRejectJobPosting(eq(1), any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/jobpostings/reset")
    class ResetJobPostingsTests {

        @Test
        @DisplayName("Should reset job posting collection")
        void shouldResetJobPostingCollection() throws Exception {
            doNothing().when(weaviateImp).resetJobPostingCollection();

            mockMvc.perform(delete("/api/admin/jobpostings/reset"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(weaviateImp).resetJobPostingCollection();
        }
    }
}
