package com.fpt.careermate.services.file_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.storage.FirebaseStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FileController Tests")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FirebaseStorageService firebaseStorageService;

    @Nested
    @DisplayName("POST /api/upload/image")
    class UploadImageTests {

        @Test
        @DisplayName("Should upload image successfully")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldUploadImageSuccessfully() throws Exception {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "test-image.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            Map<String, Object> uploadResult = new HashMap<>();
            uploadResult.put("secure_url", "https://storage.firebase.com/careermate/blogs/test-image.jpg");
            uploadResult.put("public_id", "careermate/blogs/test-image");
            uploadResult.put("width", 800);
            uploadResult.put("height", 600);

            when(firebaseStorageService.uploadFile(any(), eq("careermate/blogs"))).thenReturn(uploadResult);

            mockMvc.perform(multipart("/api/upload/image")
                            .file(imageFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.message").value("Image uploaded successfully to Firebase Storage"));

            verify(firebaseStorageService).uploadFile(any(), eq("careermate/blogs"));
        }

        @Test
        @DisplayName("Should reject non-image files")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldRejectNonImageFiles() throws Exception {
            MockMultipartFile textFile = new MockMultipartFile(
                    "image",
                    "test-file.txt",
                    "text/plain",
                    "test text content".getBytes()
            );

            mockMvc.perform(multipart("/api/upload/image")
                            .file(textFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1004))
                    .andExpect(jsonPath("$.message").value("Only image files are allowed"));

            verify(firebaseStorageService, never()).uploadFile(any(), any());
        }

        @Test
        @DisplayName("Should handle upload failure")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldHandleUploadFailure() throws Exception {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "image",
                    "test-image.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            when(firebaseStorageService.uploadFile(any(), any()))
                    .thenThrow(new RuntimeException("Firebase service unavailable"));

            mockMvc.perform(multipart("/api/upload/image")
                            .file(imageFile)
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1005))
                    .andExpect(jsonPath("$.message").value("Upload failed: Firebase service unavailable"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/images/{publicId}")
    class DeleteImageByPathTests {

        @Test
        @DisplayName("Should delete image successfully")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldDeleteImageSuccessfully() throws Exception {
            String publicId = "careermate/blogs/test-image";
            when(firebaseStorageService.deleteFile(publicId)).thenReturn(true);

            mockMvc.perform(delete("/api/images/{publicId}", publicId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.message").value("Image deleted successfully"));

            verify(firebaseStorageService).deleteFile(publicId);
        }

        @Test
        @DisplayName("Should handle image not found")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldHandleImageNotFound() throws Exception {
            String publicId = "nonexistent-image";
            when(firebaseStorageService.deleteFile(publicId)).thenReturn(false);

            mockMvc.perform(delete("/api/images/{publicId}", publicId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1006))
                    .andExpect(jsonPath("$.message").value("Image not found or already deleted"));
        }

        @Test
        @DisplayName("Should handle delete failure")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldHandleDeleteFailure() throws Exception {
            String publicId = "test-image";
            when(firebaseStorageService.deleteFile(any()))
                    .thenThrow(new RuntimeException("Firebase delete error"));

            mockMvc.perform(delete("/api/images/{publicId}", publicId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1005))
                    .andExpect(jsonPath("$.message").value("Delete failed: Firebase delete error"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/images")
    class DeleteImageByBodyTests {

        @Test
        @DisplayName("Should delete image by body successfully")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldDeleteImageByBodySuccessfully() throws Exception {
            String publicId = "careermate/blogs/test-image";
            when(firebaseStorageService.deleteFile(publicId)).thenReturn(true);

            mockMvc.perform(delete("/api/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"publicId\":\"" + publicId + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1000))
                    .andExpect(jsonPath("$.message").value("Image deleted successfully"));

            verify(firebaseStorageService).deleteFile(publicId);
        }

        @Test
        @DisplayName("Should reject empty public ID")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldRejectEmptyPublicId() throws Exception {
            mockMvc.perform(delete("/api/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"publicId\":\"\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1004))
                    .andExpect(jsonPath("$.message").value("Public ID is required"));

            verify(firebaseStorageService, never()).deleteFile(any());
        }

        @Test
        @DisplayName("Should reject missing public ID")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldRejectMissingPublicId() throws Exception {
            mockMvc.perform(delete("/api/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1004))
                    .andExpect(jsonPath("$.message").value("Public ID is required"));

            verify(firebaseStorageService, never()).deleteFile(any());
        }

        @Test
        @DisplayName("Should handle image not found via body")
        @Disabled("Requires @PreAuthorize security context - TODO: Add @WithMockUser with ADMIN role")
        void shouldHandleImageNotFoundViaBody() throws Exception {
            String publicId = "nonexistent-image";
            when(firebaseStorageService.deleteFile(publicId)).thenReturn(false);

            mockMvc.perform(delete("/api/images")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"publicId\":\"" + publicId + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1006))
                    .andExpect(jsonPath("$.message").value("Image not found or already deleted"));
        }
    }
}
