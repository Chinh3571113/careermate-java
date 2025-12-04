package com.fpt.careermate.services.admin_services.web.rest;

import com.fpt.careermate.services.blog_services.service.BlogCommentImp;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogCommentResponse;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminCommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminCommentController Tests")
class AdminCommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlogCommentImp blogCommentImp;

    @Nested
    @DisplayName("GET /api/admin/comments")
    class GetAllCommentsTests {

        @Test
        @DisplayName("Should get all comments with pagination")
        void shouldGetAllCommentsWithPagination() throws Exception {
            Page<BlogCommentResponse> page = new PageImpl<>(Collections.emptyList());
            when(blogCommentImp.getAllCommentsForAdmin(any(Pageable.class), any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/admin/comments")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk());

            verify(blogCommentImp).getAllCommentsForAdmin(any(Pageable.class), any(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/comments/{commentId}")
    class GetCommentByIdTests {

        @Test
        @DisplayName("Should get comment by ID")
        void shouldGetCommentById() throws Exception {
            BlogCommentResponse response = new BlogCommentResponse();
            when(blogCommentImp.getCommentById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/admin/comments/1"))
                    .andExpect(status().isOk());

            verify(blogCommentImp).getCommentById(1L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/comments/{commentId}")
    class DeleteCommentTests {

        @Test
        @DisplayName("Should delete comment")
        void shouldDeleteComment() throws Exception {
            doNothing().when(blogCommentImp).deleteCommentAsAdmin(1L);

            mockMvc.perform(delete("/api/admin/comments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Comment deleted successfully by admin"));

            verify(blogCommentImp).deleteCommentAsAdmin(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/admin/comments/{commentId}/hide")
    class HideCommentTests {

        @Test
        @DisplayName("Should hide comment")
        void shouldHideComment() throws Exception {
            BlogCommentResponse response = new BlogCommentResponse();
            when(blogCommentImp.hideComment(1L)).thenReturn(response);

            mockMvc.perform(post("/api/admin/comments/1/hide"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Comment hidden successfully"));

            verify(blogCommentImp).hideComment(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/admin/comments/{commentId}/show")
    class ShowCommentTests {

        @Test
        @DisplayName("Should show comment")
        void shouldShowComment() throws Exception {
            BlogCommentResponse response = new BlogCommentResponse();
            when(blogCommentImp.showComment(1L)).thenReturn(response);

            mockMvc.perform(post("/api/admin/comments/1/show"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Comment shown successfully"));

            verify(blogCommentImp).showComment(1L);
        }
    }
}
