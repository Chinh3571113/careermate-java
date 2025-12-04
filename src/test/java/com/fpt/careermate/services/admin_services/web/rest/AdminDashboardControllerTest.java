package com.fpt.careermate.services.admin_services.web.rest;

import com.fpt.careermate.services.admin_services.service.AdminDashboardService;
import com.fpt.careermate.services.admin_services.service.dto.response.DashboardStatsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminDashboardController Tests")
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminDashboardService adminDashboardService;

    @Nested
    @DisplayName("GET /api/admin/dashboard/stats")
    class GetAllDashboardStatsTests {

        @Test
        @DisplayName("Should get all dashboard statistics")
        void shouldGetAllDashboardStatistics() throws Exception {
            DashboardStatsResponse response = new DashboardStatsResponse();
            when(adminDashboardService.getAllDashboardStats()).thenReturn(response);

            mockMvc.perform(get("/api/admin/dashboard/stats"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Dashboard statistics retrieved successfully"));

            verify(adminDashboardService).getAllDashboardStats();
        }
    }
}
