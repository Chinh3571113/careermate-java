package com.fpt.careermate.services.authentication_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.authentication_services.service.dto.request.RoleRequest;
import com.fpt.careermate.services.authentication_services.service.dto.response.RoleResponse;
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

@WebMvcTest(RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RoleController Tests")
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleImp roleImp;

    @Nested
    @DisplayName("POST /api/roles")
    class CreateRoleTests {

        @Test
        @DisplayName("Should create role successfully")
        void shouldCreateRoleSuccessfully() throws Exception {
            RoleRequest request = new RoleRequest();
            RoleResponse response = new RoleResponse();

            when(roleImp.create(any(RoleRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(roleImp).create(any(RoleRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/roles")
    class GetAllRolesTests {

        @Test
        @DisplayName("Should get all roles")
        void shouldGetAllRoles() throws Exception {
            List<RoleResponse> roles = Arrays.asList(new RoleResponse());
            when(roleImp.getAll()).thenReturn(roles);

            mockMvc.perform(get("/api/roles"))
                    .andExpect(status().isOk());

            verify(roleImp).getAll();
        }
    }

    @Nested
    @DisplayName("DELETE /api/roles/{role}")
    class DeleteRoleTests {

        @Test
        @DisplayName("Should delete role")
        void shouldDeleteRole() throws Exception {
            doNothing().when(roleImp).delete("ADMIN");

            mockMvc.perform(delete("/api/roles/ADMIN"))
                    .andExpect(status().isOk());

            verify(roleImp).delete("ADMIN");
        }
    }
}
