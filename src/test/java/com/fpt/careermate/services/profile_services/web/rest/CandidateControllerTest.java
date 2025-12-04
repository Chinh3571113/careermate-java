package com.fpt.careermate.services.profile_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.careermate.services.profile_services.service.CandidateProfileImp;
import com.fpt.careermate.services.profile_services.service.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.profile_services.service.dto.request.GeneralInfoRequest;
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

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CandidateController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CandidateController Tests")
class CandidateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private CandidateProfileImp candidateProfileImp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private CandidateProfileRequest createValidCandidateProfileRequest() {
        return CandidateProfileRequest.builder()
                .fullName("John Doe")
                .dob(LocalDate.of(1995, 5, 15))
                .phone("0912345678")
                .address("123 Main St, City")
                .gender("Male")
                .title("Software Engineer")
                .build();
    }

    @Nested
    @DisplayName("PUT /api/candidates/profiles - Update Profile")
    class UpdateProfileTests {

        @Test
        @DisplayName("TC001: Update profile returns 200 OK")
        void updateProfile_ReturnsSuccess() throws Exception {
            CandidateProfileRequest request = createValidCandidateProfileRequest();
            
            when(candidateProfileImp.updateCandidateProfile(any(CandidateProfileRequest.class))).thenReturn(null);

            mockMvc.perform(put("/api/candidates/profiles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/candidates/profiles/create - Create Profile")
    class CreateProfileTests {

        @Test
        @DisplayName("TC002: Create profile returns 200 OK")
        void createProfile_ReturnsSuccess() throws Exception {
            CandidateProfileRequest request = createValidCandidateProfileRequest();
            
            when(candidateProfileImp.saveCandidateProfile(any(CandidateProfileRequest.class))).thenReturn(null);

            mockMvc.perform(post("/api/candidates/profiles/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/candidates/profiles - Get All Profiles")
    class GetAllProfilesTests {

        @Test
        @DisplayName("TC003: Get all profiles returns 200 OK")
        void getAllProfiles_ReturnsSuccess() throws Exception {
            when(candidateProfileImp.findAll(any())).thenReturn(null);

            mockMvc.perform(get("/api/candidates/profiles")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/candidates/profiles/{id} - Delete Profile")
    class DeleteProfileTests {

        @Test
        @DisplayName("TC004: Delete profile returns 200 OK")
        void deleteProfile_ReturnsSuccess() throws Exception {
            doNothing().when(candidateProfileImp).deleteProfile(1);

            mockMvc.perform(delete("/api/candidates/profiles/1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/candidates/profiles/{id} - Get Profile by ID")
    class GetProfileByIdTests {

        @Test
        @DisplayName("TC005: Get profile by ID returns 200 OK")
        void getProfileById_ReturnsSuccess() throws Exception {
            when(candidateProfileImp.getCandidateProfileById(1)).thenReturn(null);

            mockMvc.perform(get("/api/candidates/profiles/1"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/candidates/profiles/current - Get Current Profile")
    class GetCurrentProfileTests {

        @Test
        @DisplayName("TC006: Get current profile returns 200 OK")
        void getCurrentProfile_ReturnsSuccess() throws Exception {
            when(candidateProfileImp.getCandidateProfile()).thenReturn(null);

            mockMvc.perform(get("/api/candidates/profiles/current"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PUT /api/candidates/profiles-general-info - Update General Info")
    class UpdateGeneralInfoTests {

        @Test
        @DisplayName("TC007: Update general info returns 200 OK")
        void updateGeneralInfo_ReturnsSuccess() throws Exception {
            GeneralInfoRequest request = GeneralInfoRequest.builder()
                    .build();
            
            when(candidateProfileImp.saveCandidateGeneralInfo(any(GeneralInfoRequest.class))).thenReturn(null);

            mockMvc.perform(put("/api/candidates/profiles-general-info")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/candidates/profiles-general-info - Get General Info")
    class GetGeneralInfoTests {

        @Test
        @DisplayName("TC008: Get general info returns 200 OK")
        void getGeneralInfo_ReturnsSuccess() throws Exception {
            when(candidateProfileImp.getCandidateGeneralInfoById()).thenReturn(null);

            mockMvc.perform(get("/api/candidates/profiles-general-info"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PUT /api/candidates/profiles-general-info/update - Update General Info v2")
    class UpdateGeneralInfoV2Tests {

        @Test
        @DisplayName("TC009: Update general info v2 returns 200 OK")
        void updateGeneralInfoV2_ReturnsSuccess() throws Exception {
            GeneralInfoRequest request = GeneralInfoRequest.builder()
                    .build();
            
            when(candidateProfileImp.updateCandidateGeneralInfo(any(GeneralInfoRequest.class))).thenReturn(null);

            mockMvc.perform(put("/api/candidates/profiles-general-info/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }
}
