package com.fpt.careermate.services.admin_services.web.rest;

import com.fpt.careermate.services.blog_services.domain.BlogRating;
import com.fpt.careermate.services.blog_services.service.BlogRatingImp;
import com.fpt.careermate.services.blog_services.service.mapper.BlogRatingMapper;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogRatingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminRatingController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminRatingController Tests")
class AdminRatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlogRatingImp blogRatingImp;

    @MockBean
    private BlogRatingMapper blogRatingMapper;

    @Nested
    @DisplayName("GET /api/admin/ratings")
    class GetAllRatingsTests {

        @Test
        @DisplayName("Should get all ratings with pagination")
        void shouldGetAllRatingsWithPagination() throws Exception {
            Page<BlogRating> page = new PageImpl<>(Collections.emptyList());
            when(blogRatingImp.getAllRatingsForAdmin(anyInt(), anyInt(), anyString(), anyString()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/admin/ratings")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk());

            verify(blogRatingImp).getAllRatingsForAdmin(0, 20, "createdAt", "DESC");
        }
    }

    @Nested
    @DisplayName("GET /api/admin/ratings/{ratingId}")
    class GetRatingByIdTests {

        @Test
        @DisplayName("Should get rating by ID")
        void shouldGetRatingById() throws Exception {
            BlogRating rating = new BlogRating();
            BlogRatingResponse response = new BlogRatingResponse();
            when(blogRatingImp.getRatingById(1L)).thenReturn(rating);
            when(blogRatingMapper.toBlogRatingResponse(rating)).thenReturn(response);

            mockMvc.perform(get("/api/admin/ratings/1"))
                    .andExpect(status().isOk());

            verify(blogRatingImp).getRatingById(1L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/ratings/{ratingId}")
    class DeleteRatingTests {

        @Test
        @DisplayName("Should delete rating")
        void shouldDeleteRating() throws Exception {
            doNothing().when(blogRatingImp).deleteRatingAsAdmin(1L);

            mockMvc.perform(delete("/api/admin/ratings/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Rating deleted successfully by admin"));

            verify(blogRatingImp).deleteRatingAsAdmin(1L);
        }
    }
}
