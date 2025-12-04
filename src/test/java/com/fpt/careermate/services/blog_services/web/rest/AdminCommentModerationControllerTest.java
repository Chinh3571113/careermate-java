package com.fpt.careermate.services.blog_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.blog_services.service.BlogCommentImp;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogCommentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCommentModerationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminCommentModerationController Tests")
class AdminCommentModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlogCommentImp blogCommentImp;

    private BlogCommentResponse mockResponse;
    private Page<BlogCommentResponse> mockPage;

    @BeforeEach
    void setUp() {
        mockResponse = BlogCommentResponse.builder()
                .id(1L)
                .content("Test comment")
                .build();

        mockPage = new PageImpl<>(Collections.singletonList(mockResponse));
    }

    @Nested
    @DisplayName("GET /api/admin/comment-moderation/flagged - Search Flagged Comments")
    class SearchFlaggedCommentsTests {
        @Test
        @DisplayName("Should search flagged comments successfully")
        void searchFlaggedComments_ReturnsSuccess() throws Exception {
            when(blogCommentImp.searchFlaggedComments(any(), any(), any(Pageable.class)))
                    .thenReturn(mockPage);

            mockMvc.perform(get("/api/admin/comment-moderation/flagged")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/comment-moderation/flagged/all - Get All Flagged Comments")
    class GetAllFlaggedCommentsTests {
        @Test
        @DisplayName("Should get all flagged comments successfully")
        void getAllFlaggedComments_ReturnsSuccess() throws Exception {
            when(blogCommentImp.getAllFlaggedComments(any(Pageable.class)))
                    .thenReturn(mockPage);

            mockMvc.perform(get("/api/admin/comment-moderation/flagged/all")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/comment-moderation/{commentId}/approve - Approve Flagged Comment")
    class ApproveFlaggedCommentTests {
        @Test
        @DisplayName("Should approve flagged comment successfully")
        void approveFlaggedComment_ReturnsSuccess() throws Exception {
            when(blogCommentImp.approveFlaggedComment(anyLong())).thenReturn(mockResponse);

            mockMvc.perform(post("/api/admin/comment-moderation/1/approve"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/comment-moderation/{commentId}/reject - Reject Flagged Comment")
    class RejectFlaggedCommentTests {
        @Test
        @DisplayName("Should reject flagged comment successfully")
        void rejectFlaggedComment_ReturnsSuccess() throws Exception {
            when(blogCommentImp.rejectFlaggedComment(anyLong())).thenReturn(mockResponse);

            mockMvc.perform(post("/api/admin/comment-moderation/1/reject"))
                    .andExpect(status().isOk());
        }
    }
}
