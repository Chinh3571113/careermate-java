package com.fpt.careermate.services.blog_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.blog_services.service.BlogRatingImp;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogRatingRequest;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogRatingResponse;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlogRatingController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BlogRatingController Tests")
class BlogRatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlogRatingImp blogRatingImp;

    private BlogRatingResponse mockResponse;
    private BlogRatingRequest validRequest;

    @BeforeEach
    void setUp() {
        mockResponse = BlogRatingResponse.builder()
                .id(1L)
                .rating(5)
                .build();

        validRequest = BlogRatingRequest.builder()
                .rating(5)
                .build();
    }

    @Nested
    @DisplayName("POST /api/blogs/{blogId}/ratings - Rate Blog")
    class RateBlogTests {
        @Test
        @DisplayName("Should rate blog successfully")
        void rateBlog_ReturnsSuccess() throws Exception {
            when(blogRatingImp.rateBlog(anyLong(), any(BlogRatingRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/blogs/1/ratings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/blogs/{blogId}/ratings/my-rating - Get User Rating")
    class GetUserRatingTests {
        @Test
        @DisplayName("Should get user rating successfully")
        void getUserRating_ReturnsSuccess() throws Exception {
            when(blogRatingImp.getUserRating(anyLong())).thenReturn(mockResponse);

            mockMvc.perform(get("/api/blogs/1/ratings/my-rating"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/blogs/{blogId}/ratings - Delete Rating")
    class DeleteRatingTests {
        @Test
        @DisplayName("Should delete rating successfully")
        void deleteRating_ReturnsSuccess() throws Exception {
            doNothing().when(blogRatingImp).deleteRating(anyLong());

            mockMvc.perform(delete("/api/blogs/1/ratings"))
                    .andExpect(status().isOk());
        }
    }
}
