package com.fpt.careermate.services.blog_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.blog_services.service.BlogImp;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogCreationRequest;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogUpdateRequest;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogResponse;
import com.fpt.careermate.services.storage.FirebaseStorageService;
import org.junit.jupiter.api.Disabled;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlogController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("BlogController Tests")
class BlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlogImp blogImp;

    @MockBean
    private FirebaseStorageService firebaseStorageService;

    @Nested
    @DisplayName("POST /api/blogs")
    class CreateBlogTests {

        @Test
        @DisplayName("Should create blog successfully")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldCreateBlogSuccessfully() throws Exception {
            BlogCreationRequest request = new BlogCreationRequest();
            BlogResponse response = new BlogResponse();

            when(blogImp.createBlog(any(BlogCreationRequest.class), anyString())).thenReturn(response);

            mockMvc.perform(post("/api/blogs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(blogImp).createBlog(any(BlogCreationRequest.class), anyString());
        }
    }

    @Nested
    @DisplayName("PUT /api/blogs/{blogId}")
    class UpdateBlogTests {

        @Test
        @DisplayName("Should update blog successfully")
        void shouldUpdateBlogSuccessfully() throws Exception {
            BlogUpdateRequest request = new BlogUpdateRequest();
            BlogResponse response = new BlogResponse();

            when(blogImp.updateBlog(eq(1L), any(BlogUpdateRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/blogs/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(blogImp).updateBlog(eq(1L), any(BlogUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/blogs/{blogId}")
    class DeleteBlogTests {

        @Test
        @DisplayName("Should delete blog successfully")
        void shouldDeleteBlogSuccessfully() throws Exception {
            doNothing().when(blogImp).deleteBlog(1L);

            mockMvc.perform(delete("/api/blogs/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Blog deleted successfully"));

            verify(blogImp).deleteBlog(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/blogs/{blogId}")
    class GetBlogByIdTests {

        @Test
        @DisplayName("Should get blog by ID")
        void shouldGetBlogById() throws Exception {
            BlogResponse response = new BlogResponse();
            when(blogImp.getBlogById(1L)).thenReturn(response);

            mockMvc.perform(get("/api/blogs/1"))
                    .andExpect(status().isOk());

            verify(blogImp).getBlogById(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/blogs")
    class GetAllBlogsTests {

        @Test
        @DisplayName("Should get all blogs with pagination")
        void shouldGetAllBlogsWithPagination() throws Exception {
            Page<BlogResponse> page = new PageImpl<>(Collections.emptyList());
            when(blogImp.getAllBlogs(any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/blogs")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());

            verify(blogImp).getAllBlogs(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/blogs/status/{status}")
    class GetBlogsByStatusTests {

        @Test
        @DisplayName("Should get blogs by status")
        void shouldGetBlogsByStatus() throws Exception {
            Page<BlogResponse> page = new PageImpl<>(Collections.emptyList());
            when(blogImp.getBlogsByStatus(eq("PUBLISHED"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/blogs/status/PUBLISHED"))
                    .andExpect(status().isOk());

            verify(blogImp).getBlogsByStatus(eq("PUBLISHED"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/blogs/category/{category}")
    class GetBlogsByCategoryTests {

        @Test
        @DisplayName("Should get blogs by category")
        void shouldGetBlogsByCategory() throws Exception {
            Page<BlogResponse> page = new PageImpl<>(Collections.emptyList());
            when(blogImp.getBlogsByCategory(eq("Technology"), any(Pageable.class))).thenReturn(page);

            mockMvc.perform(get("/api/blogs/category/Technology"))
                    .andExpect(status().isOk());

            verify(blogImp).getBlogsByCategory(eq("Technology"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/blogs/categories")
    class GetAllCategoriesTests {

        @Test
        @DisplayName("Should get all categories")
        void shouldGetAllCategories() throws Exception {
            List<String> categories = Arrays.asList("Technology", "Career");
            when(blogImp.getAllCategories()).thenReturn(categories);

            mockMvc.perform(get("/api/blogs/categories"))
                    .andExpect(status().isOk());

            verify(blogImp).getAllCategories();
        }
    }

    @Nested
    @DisplayName("PUT /api/blogs/{blogId}/publish")
    class PublishBlogTests {

        @Test
        @DisplayName("Should publish blog")
        void shouldPublishBlog() throws Exception {
            BlogResponse response = new BlogResponse();
            when(blogImp.publishBlog(1L)).thenReturn(response);

            mockMvc.perform(put("/api/blogs/1/publish"))
                    .andExpect(status().isOk());

            verify(blogImp).publishBlog(1L);
        }
    }

    @Nested
    @DisplayName("POST /api/blogs/upload-image")
    class UploadBlogImageTests {

        @Test
        @DisplayName("Should upload blog image successfully")
        void shouldUploadBlogImageSuccessfully() throws Exception {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "test-image.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "https://storage.example.com/image.jpg");
            uploadResult.put("public_id", "blogs/image123");

            when(firebaseStorageService.uploadFile(any(), eq("careermate/blogs"))).thenReturn(uploadResult);

            mockMvc.perform(multipart("/api/blogs/upload-image")
                            .file(imageFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.message").value("Image uploaded successfully"));

            verify(firebaseStorageService).uploadFile(any(), eq("careermate/blogs"));
        }
    }
}
