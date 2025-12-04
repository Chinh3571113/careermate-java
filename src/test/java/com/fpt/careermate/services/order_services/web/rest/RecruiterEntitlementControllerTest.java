package com.fpt.careermate.services.order_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.order_services.service.RecruiterEntitlementCheckerService;
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

@WebMvcTest(RecruiterEntitlementController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RecruiterEntitlementController Tests")
class RecruiterEntitlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecruiterEntitlementCheckerService checkerService;

    @Nested
    @DisplayName("GET /api/recruiter-entitlement/ai-matching-checker - Check AI Matching")
    class CheckAiMatchingTests {

        @Test
        @DisplayName("TC001: Check AI matching returns 200 OK")
        void checkAiMatching_ReturnsSuccess() throws Exception {
            when(checkerService.canUseAiMatching()).thenReturn(true);

            mockMvc.perform(get("/api/recruiter-entitlement/ai-matching-checker"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/recruiter-entitlement/job-posting-checker - Check Job Post")
    class CheckJobPostTests {

        @Test
        @DisplayName("TC002: Check job post returns 200 OK")
        void checkJobPost_ReturnsSuccess() throws Exception {
            when(checkerService.canPostJob()).thenReturn(true);

            mockMvc.perform(get("/api/recruiter-entitlement/job-posting-checker"))
                    .andExpect(status().isOk());
        }
    }
}
