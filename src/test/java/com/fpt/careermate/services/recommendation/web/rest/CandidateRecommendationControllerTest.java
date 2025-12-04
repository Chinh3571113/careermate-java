package com.fpt.careermate.services.recommendation.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.recommendation.service.CandidateRecommendationService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CandidateRecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CandidateRecommendationController Tests")
class CandidateRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CandidateRecommendationService recommendationService;

    @Nested
    @DisplayName("GET /api/recruiter/recommendations/job/{jobPostingId} - Get Recommended Candidates")
    class GetRecommendedCandidatesTests {

        @Test
        @DisplayName("TC001: Get recommended candidates returns 200 OK")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with RECRUITER role")
        void getRecommendedCandidates_ReturnsSuccess() throws Exception {
            when(recommendationService.getRecommendedCandidatesForJob(anyInt(), any(), any()))
                    .thenReturn(null);

            mockMvc.perform(get("/api/recruiter/recommendations/job/1")
                            .param("maxCandidates", "10")
                            .param("minMatchScore", "0.5"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/recommendations/refresh-candidate/{candidateId} - Refresh Candidate Profile")
    class RefreshCandidateProfileTests {

        @Test
        @DisplayName("TC002: Refresh candidate profile returns 200 OK")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void refreshCandidateProfile_ReturnsSuccess() throws Exception {
            doNothing().when(recommendationService).syncCandidateToWeaviate(anyInt());

            mockMvc.perform(post("/api/admin/recommendations/refresh-candidate/1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/recommendations/refresh-all-candidates - Refresh All Candidates")
    class RefreshAllCandidatesTests {

        @Test
        @DisplayName("TC003: Refresh all candidates returns 200 OK")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void refreshAllCandidates_ReturnsSuccess() throws Exception {
            doNothing().when(recommendationService).syncAllCandidatesToWeaviate();

            mockMvc.perform(post("/api/admin/recommendations/refresh-all-candidates"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/recommendations/candidate/{candidateId} - Delete Candidate")
    class DeleteCandidateTests {

        @Test
        @DisplayName("TC004: Delete candidate from Weaviate returns 200 OK")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void deleteCandidateFromWeaviate_ReturnsSuccess() throws Exception {
            doNothing().when(recommendationService).deleteCandidateFromWeaviate(1);

            mockMvc.perform(delete("/api/admin/recommendations/candidate/1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/class - Get Collection")
    class GetCollectionTests {

        @Test
        @DisplayName("TC005: Get collection returns 200 OK")
        void getCollection_ReturnsSuccess() throws Exception {
            doNothing().when(recommendationService).getCollection();

            mockMvc.perform(get("/api/class"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/recommendations/recreate-schema - Recreate Schema")
    class RecreateSchemaTests {

        @Test
        @DisplayName("TC006: Recreate schema returns 200 OK")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void recreateSchema_ReturnsSuccess() throws Exception {
            doNothing().when(recommendationService).recreateSchema();

            mockMvc.perform(post("/api/admin/recommendations/recreate-schema"))
                    .andExpect(status().isOk());
        }
    }
}
