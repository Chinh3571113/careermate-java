package com.fpt.careermate.services.blog_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.blog_services.service.BlogCommentImp;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogCommentRequest;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlogCommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BlogCommentController Tests")
class BlogCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlogCommentImp blogCommentImp;

    private BlogCommentResponse mockResponse;
    private BlogCommentRequest validRequest;
    private Page<BlogCommentResponse> mockPage;

    @BeforeEach
    void setUp() {
        mockResponse = BlogCommentResponse.builder()
                .id(1L)
                .content("Test comment")
                .build();

        validRequest = BlogCommentRequest.builder()
                .content("This is a test comment")
                .build();

        mockPage = new PageImpl<>(Collections.singletonList(mockResponse));
    }

    @Nested
    @DisplayName("POST /api/blogs/{blogId}/comments - Create Comment")
    class CreateCommentTests {
        @Test
        @DisplayName("Should create comment successfully")
        void createComment_ReturnsSuccess() throws Exception {
            when(blogCommentImp.createComment(anyLong(), any(BlogCommentRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/blogs/1/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/blogs/{blogId}/comments - Get Comments By Blog ID")
    class GetCommentsByBlogIdTests {
        @Test
        @DisplayName("Should get comments by blog ID successfully")
        void getCommentsByBlogId_ReturnsSuccess() throws Exception {
            when(blogCommentImp.getCommentsByBlogId(anyLong(), any(Pageable.class)))
                    .thenReturn(mockPage);

            mockMvc.perform(get("/api/blogs/1/comments")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PUT /api/blogs/{blogId}/comments/{commentId} - Update Comment")
    class UpdateCommentTests {
        @Test
        @DisplayName("Should update comment successfully")
        void updateComment_ReturnsSuccess() throws Exception {
            when(blogCommentImp.updateComment(anyLong(), any(BlogCommentRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(put("/api/blogs/1/comments/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/blogs/{blogId}/comments/{commentId} - Delete Comment")
    class DeleteCommentTests {
        @Test
        @DisplayName("Should delete comment successfully")
        void deleteComment_ReturnsSuccess() throws Exception {
            doNothing().when(blogCommentImp).deleteComment(anyLong());

            mockMvc.perform(delete("/api/blogs/1/comments/1"))
                    .andExpect(status().isOk());
        }
    }
}
