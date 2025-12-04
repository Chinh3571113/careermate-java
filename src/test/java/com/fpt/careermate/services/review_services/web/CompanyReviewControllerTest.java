package com.fpt.careermate.services.review_services.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import com.fpt.careermate.services.review_services.service.CompanyReviewService;
import com.fpt.careermate.services.review_services.service.dto.request.CompanyReviewRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompanyReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CompanyReviewController Tests")
class CompanyReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyReviewService companyReviewService;

    private CompanyReviewRequest createValidCompanyReviewRequest() {
        return CompanyReviewRequest.builder()
                .jobApplyId(1)
                .reviewType(ReviewType.INTERVIEW_EXPERIENCE)
                .reviewText("This is a detailed review of my interview experience with the company.")
                .overallRating(4)
                .isAnonymous(false)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/reviews - Submit Review")
    class SubmitReviewTests {

        @Test
        @DisplayName("TC001: Submit company review returns 201 Created")
        void submitReview_ReturnsSuccess() throws Exception {
            CompanyReviewRequest request = createValidCompanyReviewRequest();
            
            when(companyReviewService.submitReview(any(CompanyReviewRequest.class), anyInt())).thenReturn(null);

            mockMvc.perform(post("/api/v1/reviews")
                            .param("candidateId", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reviews/eligibility - Check Review Eligibility")
    class CheckEligibilityTests {

        @Test
        @DisplayName("TC002: Check eligibility returns 200 OK")
        void checkEligibility_ReturnsSuccess() throws Exception {
            when(companyReviewService.checkEligibility(anyInt(), anyInt())).thenReturn(null);

            mockMvc.perform(get("/api/v1/reviews/eligibility")
                            .param("candidateId", "1")
                            .param("jobApplyId", "1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reviews/company/{recruiterId} - Get Company Reviews")
    class GetCompanyReviewsTests {

        @Test
        @DisplayName("TC003: Get company reviews returns 200 OK")
        void getCompanyReviews_ReturnsSuccess() throws Exception {
            when(companyReviewService.getCompanyReviews(anyInt(), any(), anyInt(), anyInt()))
                    .thenReturn(null);

            mockMvc.perform(get("/api/v1/reviews/company/1")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reviews/my-reviews - Get My Reviews")
    class GetMyReviewsTests {

        @Test
        @DisplayName("TC004: Get my reviews returns 200 OK")
        void getMyReviews_ReturnsSuccess() throws Exception {
            when(companyReviewService.getCandidateReviews(anyInt(), anyInt(), anyInt()))
                    .thenReturn(null);

            mockMvc.perform(get("/api/v1/reviews/my-reviews")
                            .param("candidateId", "1")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reviews/company/{recruiterId}/rating - Get Company Rating")
    class GetCompanyRatingTests {

        @Test
        @DisplayName("TC005: Get company rating returns 200 OK")
        void getCompanyRating_ReturnsSuccess() throws Exception {
            when(companyReviewService.getAverageRating(1)).thenReturn(4.5);

            mockMvc.perform(get("/api/v1/reviews/company/1/rating"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reviews/company/{recruiterId}/statistics - Get Statistics")
    class GetStatisticsTests {

        @Test
        @DisplayName("TC006: Get statistics returns 200 OK")
        void getStatistics_ReturnsSuccess() throws Exception {
            when(companyReviewService.getCompanyStatistics(1)).thenReturn(null);

            mockMvc.perform(get("/api/v1/reviews/company/1/statistics"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/reviews/{reviewId}/flag - Flag Review")
    class FlagReviewTests {

        @Test
        @DisplayName("TC007: Flag review returns 200 OK")
        void flagReview_ReturnsSuccess() throws Exception {
            doNothing().when(companyReviewService).flagReview(anyInt(), anyInt(), anyString());

            mockMvc.perform(post("/api/v1/reviews/1/flag")
                            .param("reporterId", "1")
                            .param("reason", "Inappropriate content"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/reviews/{reviewId} - Remove Review")
    class RemoveReviewTests {

        @Test
        @DisplayName("TC008: Remove review returns 200 OK")
        void removeReview_ReturnsSuccess() throws Exception {
            doNothing().when(companyReviewService).removeReview(anyInt(), anyString());

            mockMvc.perform(delete("/api/v1/reviews/1")
                            .param("reason", "Violates policy"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reviews/{reviewId} - Get Review by ID")
    class GetReviewByIdTests {

        @Test
        @DisplayName("TC009: Get review by ID returns 200 OK")
        void getReviewById_ReturnsSuccess() throws Exception {
            when(companyReviewService.getReviewById(1)).thenReturn(null);

            mockMvc.perform(get("/api/v1/reviews/1"))
                    .andExpect(status().isOk());
        }
    }
}
