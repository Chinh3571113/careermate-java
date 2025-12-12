package com.fpt.careermate.services.admin_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.common.response.PageResponse;
import com.fpt.careermate.services.authentication_services.service.RegistrationService;
import com.fpt.careermate.services.recruiter_services.service.RecruiterImp;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterApprovalResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminRecruiterController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminRecruiterController Tests")
class AdminRecruiterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecruiterImp recruiterImp;

    @MockBean
    private RegistrationService registrationService;

    @Nested
    @DisplayName("GET /api/admin/recruiters/filter - Filter Recruiters by Status")
    class GetRecruitersByStatusTests {

        @Test
        @DisplayName("UTC001: Filter with valid status PENDING - Normal Case")
        void filterRecruiters_WithValidStatusPending_ReturnsSuccess() throws Exception {
            // Arrange
            List<RecruiterApprovalResponse> content = new ArrayList<>();
            RecruiterApprovalResponse recruiter = new RecruiterApprovalResponse();
            recruiter.setRecruiterId(1);
            recruiter.setCompanyName("Test Company");
            recruiter.setAccountStatus("PENDING");
            content.add(recruiter);

            PageResponse<RecruiterApprovalResponse> pageResponse = new PageResponse<>(
                    content, 0, 10, 1L, 1
            );

            when(recruiterImp.getRecruitersByStatus(eq("PENDING"), eq(0), eq(10), eq("id"), eq("desc")))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/admin/recruiters/filter")
                            .param("status", "PENDING")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sortBy", "id")
                            .param("sortDir", "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.result.content[0].status").value("PENDING"))
                    .andExpect(jsonPath("$.result.totalElements").value(1));

            verify(recruiterImp, times(1)).getRecruitersByStatus("PENDING", 0, 10, "id", "desc");
        }

        @Test
        @DisplayName("UTC002: Filter with status ACTIVE - Normal Case")
        void filterRecruiters_WithStatusActive_ReturnsSuccess() throws Exception {
            // Arrange
            List<RecruiterApprovalResponse> content = new ArrayList<>();
            RecruiterApprovalResponse recruiter1 = new RecruiterApprovalResponse();
            recruiter1.setRecruiterId(1);
            recruiter1.setCompanyName("Active Company 1");
            recruiter1.setAccountStatus("ACTIVE");

            RecruiterApprovalResponse recruiter2 = new RecruiterApprovalResponse();
            recruiter2.setRecruiterId(2);
            recruiter2.setCompanyName("Active Company 2");
            recruiter2.setAccountStatus("ACTIVE");

            content.add(recruiter1);
            content.add(recruiter2);

            PageResponse<RecruiterApprovalResponse> pageResponse = new PageResponse<>(
                    content, 0, 10, 2L, 1
            );

            when(recruiterImp.getRecruitersByStatus(eq("ACTIVE"), eq(0), eq(10), eq("id"), eq("desc")))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/admin/recruiters/filter")
                            .param("status", "ACTIVE")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.content[0].status").value("ACTIVE"))
                    .andExpect(jsonPath("$.result.totalElements").value(2));

            verify(recruiterImp, times(1)).getRecruitersByStatus("ACTIVE", 0, 10, "id", "desc");
        }

        @Test
        @DisplayName("UTC003: Filter without status parameter (get all) - Boundary Case")
        void filterRecruiters_WithoutStatus_ReturnsAllRecruiters() throws Exception {
            // Arrange
            List<RecruiterApprovalResponse> content = new ArrayList<>();
            RecruiterApprovalResponse recruiter1 = new RecruiterApprovalResponse();
            recruiter1.setRecruiterId(1);
            recruiter1.setAccountStatus("PENDING");

            RecruiterApprovalResponse recruiter2 = new RecruiterApprovalResponse();
            recruiter2.setRecruiterId(2);
            recruiter2.setAccountStatus("ACTIVE");

            content.add(recruiter1);
            content.add(recruiter2);

            PageResponse<RecruiterApprovalResponse> pageResponse = new PageResponse<>(
                    content, 0, 10, 2L, 1
            );

            when(recruiterImp.getRecruitersByStatus(isNull(), eq(0), eq(10), eq("id"), eq("desc")))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/admin/recruiters/filter")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.totalElements").value(2));

            verify(recruiterImp, times(1)).getRecruitersByStatus(null, 0, 10, "id", "desc");
        }

        @Test
        @DisplayName("UTC004: Filter with pagination page 2 - Normal Case")
        void filterRecruiters_WithPaginationPage2_ReturnsSuccess() throws Exception {
            // Arrange
            List<RecruiterApprovalResponse> content = new ArrayList<>();
            RecruiterApprovalResponse recruiter = new RecruiterApprovalResponse();
            recruiter.setRecruiterId(11);
            recruiter.setCompanyName("Company Page 2");
            recruiter.setAccountStatus("ACTIVE");
            content.add(recruiter);

            PageResponse<RecruiterApprovalResponse> pageResponse = new PageResponse<>(
                    content, 1, 10, 15L, 2
            );

            when(recruiterImp.getRecruitersByStatus("ACTIVE", 1, 10, "companyName", "asc"))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/admin/recruiters/filter")
                            .param("status", "ACTIVE")
                            .param("page", "1")
                            .param("size", "10")
                            .param("sortBy", "companyName")
                            .param("sortDir", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.pageNumber").value(1))
                    .andExpect(jsonPath("$.result.totalElements").value(15))
                    .andExpect(jsonPath("$.result.totalPages").value(2));

            verify(recruiterImp, times(1)).getRecruitersByStatus("ACTIVE", 1, 10, "companyName", "asc");
        }

        @Test
        @DisplayName("UTC005: Filter returns empty result - Boundary Case")
        void filterRecruiters_WithNoResults_ReturnsEmptyPage() throws Exception {
            // Arrange
            PageResponse<RecruiterApprovalResponse> pageResponse = new PageResponse<>(
                    new ArrayList<>(), 0, 10, 0L, 0
            );

            when(recruiterImp.getRecruitersByStatus("BANNED", 0, 10, "id", "desc"))
                    .thenReturn(pageResponse);

            // Act & Assert
            mockMvc.perform(get("/api/admin/recruiters/filter")
                            .param("status", "BANNED")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.result.content").isEmpty())
                    .andExpect(jsonPath("$.result.totalElements").value(0));

            verify(recruiterImp, times(1)).getRecruitersByStatus("BANNED", 0, 10, "id", "desc");
        }
    }

    @Nested
    @DisplayName("GET /api/admin/recruiters/search - Search Recruiters")
    class SearchRecruitersTests {

        @Test
        @DisplayName("Should search recruiters successfully")
        void searchRecruiters_ReturnsSuccess() throws Exception {
            PageResponse<RecruiterApprovalResponse> pageResponse = new PageResponse<>(
                    new ArrayList<>(), 0, 10, 0L, 0
            );

            when(recruiterImp.searchRecruiters(any(), any(), anyInt(), anyInt(), any(), any()))
                    .thenReturn(pageResponse);

            mockMvc.perform(get("/api/admin/recruiters/search")
                            .param("search", "test")
                            .param("status", "ACTIVE")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(recruiterImp, times(1)).searchRecruiters("ACTIVE", "test", 0, 10, "id", "desc");
        }
    }

    @Nested
    @DisplayName("GET /api/admin/recruiters/{recruiterId} - Get Recruiter by ID")
    class GetRecruiterByIdTests {

        @Test
        @DisplayName("Should get recruiter by ID successfully")
        void getRecruiterById_ReturnsSuccess() throws Exception {
            RecruiterApprovalResponse response = new RecruiterApprovalResponse();
            response.setRecruiterId(1);
            response.setCompanyName("Test Company");

            when(recruiterImp.getRecruiterById(1)).thenReturn(response);

            mockMvc.perform(get("/api/admin/recruiters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.recruiterId").value(1));

            verify(recruiterImp, times(1)).getRecruiterById(1);
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/recruiters/{recruiterId}/approve - Approve Recruiter")
    class ApproveRecruiterTests {

        @Test
        @DisplayName("Should approve recruiter successfully")
        void approveRecruiter_ReturnsSuccess() throws Exception {
            doNothing().when(registrationService).approveRecruiterAccount(1);

            mockMvc.perform(put("/api/admin/recruiters/1/approve"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(registrationService, times(1)).approveRecruiterAccount(1);
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/recruiters/{recruiterId}/reject - Reject Recruiter")
    class RejectRecruiterTests {

        @Test
        @DisplayName("Should reject recruiter successfully")
        void rejectRecruiter_ReturnsSuccess() throws Exception {
            doNothing().when(registrationService).rejectRecruiterAccount(1, "Invalid documents");

            mockMvc.perform(put("/api/admin/recruiters/1/reject")
                            .param("reason", "Invalid documents"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(registrationService, times(1)).rejectRecruiterAccount(1, "Invalid documents");
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/recruiters/account/{accountId}/ban - Ban Recruiter Account")
    class BanRecruiterAccountTests {

        @Test
        @DisplayName("Should ban recruiter account successfully")
        void banRecruiterAccount_ReturnsSuccess() throws Exception {
            doNothing().when(registrationService).banRecruiterAccount(1, "Violation of terms");

            mockMvc.perform(put("/api/admin/recruiters/account/1/ban")
                            .param("reason", "Violation of terms"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(registrationService, times(1)).banRecruiterAccount(1, "Violation of terms");
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/recruiters/account/{accountId}/unban - Unban Recruiter Account")
    class UnbanRecruiterAccountTests {

        @Test
        @DisplayName("Should unban recruiter account successfully")
        void unbanRecruiterAccount_ReturnsSuccess() throws Exception {
            doNothing().when(registrationService).unbanRecruiterAccount(1);

            mockMvc.perform(put("/api/admin/recruiters/account/1/unban"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(registrationService, times(1)).unbanRecruiterAccount(1);
        }
    }
}

