package com.fpt.careermate.services.authentication_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.authentication_services.service.dto.request.PermissionRequest;
import com.fpt.careermate.services.authentication_services.service.dto.response.PermissionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PermissionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PermissionController Tests")
class PermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PermissionImp permissionService;

    @Nested
    @DisplayName("POST /api/permissions")
    class CreatePermissionTests {

        @Test
        @DisplayName("Should create permission successfully")
        void shouldCreatePermissionSuccessfully() throws Exception {
            PermissionRequest request = new PermissionRequest();
            PermissionResponse response = new PermissionResponse();

            when(permissionService.create(any(PermissionRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/permissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(permissionService).create(any(PermissionRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/permissions")
    class GetAllPermissionsTests {

        @Test
        @DisplayName("Should get all permissions")
        void shouldGetAllPermissions() throws Exception {
            List<PermissionResponse> permissions = Arrays.asList(new PermissionResponse());
            when(permissionService.getAll()).thenReturn(permissions);

            mockMvc.perform(get("/api/permissions"))
                    .andExpect(status().isOk());

            verify(permissionService).getAll();
        }
    }

    @Nested
    @DisplayName("DELETE /api/permissions/{permission}")
    class DeletePermissionTests {

        @Test
        @DisplayName("Should delete permission")
        void shouldDeletePermission() throws Exception {
            doNothing().when(permissionService).delete("CREATE_USER");

            mockMvc.perform(delete("/api/permissions/CREATE_USER"))
                    .andExpect(status().isOk());

            verify(permissionService).delete("CREATE_USER");
        }
    }
}
