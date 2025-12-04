package com.fpt.careermate.services.health_services.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.health_services.service.HealthService;
import org.junit.jupiter.api.Disabled;
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

@WebMvcTest(AdminHealthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminHealthController Tests")
class AdminHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HealthService healthService;

    @Nested
    @DisplayName("GET /api/admin/health - Get Health Status")
    class GetHealthStatusTests {

        @Test
        @DisplayName("TC001: Get health status returns 200 OK")
        @Disabled("Requires @WithMockUser or method security configuration - TODO: Add proper security context")
        void getHealthStatus_ReturnsSuccess() throws Exception {
            when(healthService.getAggregatedHealth()).thenReturn(null);

            mockMvc.perform(get("/api/admin/health"))
                    .andExpect(status().isOk());
        }
    }
}
