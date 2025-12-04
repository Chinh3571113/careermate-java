package com.fpt.careermate.services.recruiter_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.recruiter_services.service.RecruiterImp;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterCreationRequest;
import com.fpt.careermate.services.recruiter_services.service.dto.request.RecruiterUpdateRequest;
import com.fpt.careermate.services.recruiter_services.service.dto.response.NewRecruiterResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterProfileResponse;
import com.fpt.careermate.services.recruiter_services.service.dto.response.RecruiterUpdateRequestResponse;
import com.fpt.careermate.services.authentication_services.service.dto.request.RecruiterRegistrationRequest;
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
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecruiterController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RecruiterController Tests")
class RecruiterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecruiterImp recruiterImp;

    private RecruiterCreationRequest createValidRecruiterCreationRequest() {
        return RecruiterCreationRequest.builder()
                .companyName("Tech Company Inc")
                .website("https://techcompany.com")
                .logoUrl("https://techcompany.com/logo.png")
                .about("A leading technology company")
                .build();
    }

    private RecruiterRegistrationRequest.OrganizationInfo createValidOrganizationInfo() {
        return RecruiterRegistrationRequest.OrganizationInfo.builder()
                .companyName("Tech Company Inc")
                .website("https://techcompany.com")
                .about("A leading technology company")
                .build();
    }

    @Nested
    @DisplayName("POST /api/recruiter")
    class CreateRecruiterTests {

        @Test
        @DisplayName("Should create recruiter successfully")
        void shouldCreateRecruiterSuccessfully() throws Exception {
            RecruiterCreationRequest request = createValidRecruiterCreationRequest();
            NewRecruiterResponse response = new NewRecruiterResponse();

            when(recruiterImp.createRecruiter(any(RecruiterCreationRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/recruiter")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Recruiter profile created successfully. Waiting for admin approval to activate recruiter features."));

            verify(recruiterImp).createRecruiter(any(RecruiterCreationRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/recruiter/profile")
    class GetMyProfileTests {

        @Test
        @DisplayName("Should get own profile successfully")
        void shouldGetOwnProfileSuccessfully() throws Exception {
            RecruiterProfileResponse response = new RecruiterProfileResponse();

            when(recruiterImp.getMyProfile()).thenReturn(response);

            mockMvc.perform(get("/api/recruiter/profile"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Profile retrieved successfully"));

            verify(recruiterImp).getMyProfile();
        }
    }

    @Nested
    @DisplayName("PUT /api/recruiter/profile")
    class RequestProfileUpdateTests {

        @Test
        @DisplayName("Should request profile update successfully")
        void shouldRequestProfileUpdateSuccessfully() throws Exception {
            RecruiterUpdateRequest request = new RecruiterUpdateRequest();
            RecruiterUpdateRequestResponse response = new RecruiterUpdateRequestResponse();

            when(recruiterImp.requestProfileUpdate(any(RecruiterUpdateRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/recruiter/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Profile update request submitted. You can continue using your current profile while waiting for admin approval."));

            verify(recruiterImp).requestProfileUpdate(any(RecruiterUpdateRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/recruiter/profile/update-requests")
    class GetMyUpdateRequestsTests {

        @Test
        @DisplayName("Should get update requests successfully")
        void shouldGetUpdateRequestsSuccessfully() throws Exception {
            RecruiterUpdateRequestResponse request1 = new RecruiterUpdateRequestResponse();
            RecruiterUpdateRequestResponse request2 = new RecruiterUpdateRequestResponse();
            List<RecruiterUpdateRequestResponse> requests = Arrays.asList(request1, request2);

            when(recruiterImp.getMyUpdateRequests()).thenReturn(requests);

            mockMvc.perform(get("/api/recruiter/profile/update-requests"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Update requests retrieved successfully"));

            verify(recruiterImp).getMyUpdateRequests();
        }

        @Test
        @DisplayName("Should return empty list when no update requests")
        void shouldReturnEmptyListWhenNoUpdateRequests() throws Exception {
            when(recruiterImp.getMyUpdateRequests()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/recruiter/profile/update-requests"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(recruiterImp).getMyUpdateRequests();
        }
    }

    @Nested
    @DisplayName("PUT /api/recruiter/update-organization")
    class UpdateOrganizationInfoTests {

        @Test
        @DisplayName("Should update organization info successfully")
        void shouldUpdateOrganizationInfoSuccessfully() throws Exception {
            RecruiterRegistrationRequest.OrganizationInfo orgInfo = createValidOrganizationInfo();

            doNothing().when(recruiterImp).updateOrganizationInfo(any(RecruiterRegistrationRequest.OrganizationInfo.class));

            mockMvc.perform(put("/api/recruiter/update-organization")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orgInfo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Organization information updated successfully. Your profile is now pending admin review again."));

            verify(recruiterImp).updateOrganizationInfo(any(RecruiterRegistrationRequest.OrganizationInfo.class));
        }
    }
}
