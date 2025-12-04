package com.fpt.careermate.services.authentication_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.authentication_services.service.RegistrationService;
import com.fpt.careermate.services.authentication_services.service.dto.request.RecruiterRegistrationRequest;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OAuth2RecruiterController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("OAuth2RecruiterController Tests")
class OAuth2RecruiterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    private RecruiterRegistrationRequest.OrganizationInfo createValidOrganizationInfo() {
        return RecruiterRegistrationRequest.OrganizationInfo.builder()
                .companyName("Tech Company Inc")
                .website("https://techcompany.com")
                .about("A leading technology company")
                .logoUrl("https://techcompany.com/logo.png")
                .build();
    }

    @Nested
    @DisplayName("POST /api/oauth2/recruiter/complete-registration")
    class CompleteRecruiterRegistrationTests {

        @Test
        @DisplayName("Should complete registration successfully with valid OAuth session")
        void shouldCompleteRegistrationSuccessfullyWithValidOAuthSession() throws Exception {
            RecruiterRegistrationRequest.OrganizationInfo orgInfo = createValidOrganizationInfo();
            
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("oauth_email", "recruiter@company.com");
            session.setAttribute("oauth_timestamp", System.currentTimeMillis());

            when(registrationService.completeRecruiterProfileForOAuth(anyString(), any()))
                    .thenReturn(new Recruiter());

            mockMvc.perform(post("/api/oauth2/recruiter/complete-registration")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orgInfo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("Recruiter registration completed! Your account is pending admin approval. You will receive an email when approved."));

            verify(registrationService).completeRecruiterProfileForOAuth(eq("recruiter@company.com"), any());
        }

        @Test
        @DisplayName("Should fail when OAuth session not found")
        void shouldFailWhenOAuthSessionNotFound() throws Exception {
            RecruiterRegistrationRequest.OrganizationInfo orgInfo = createValidOrganizationInfo();
            
            MockHttpSession session = new MockHttpSession();
            // No email in session

            mockMvc.perform(post("/api/oauth2/recruiter/complete-registration")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orgInfo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("OAuth session expired. Please login with Google again."));

            verify(registrationService, never()).completeRecruiterProfileForOAuth(any(), any());
        }

        @Test
        @DisplayName("Should fail when OAuth session expired")
        void shouldFailWhenOAuthSessionExpired() throws Exception {
            RecruiterRegistrationRequest.OrganizationInfo orgInfo = createValidOrganizationInfo();
            
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("oauth_email", "recruiter@company.com");
            // Set timestamp to 31 minutes ago (expired)
            session.setAttribute("oauth_timestamp", System.currentTimeMillis() - 1860000);

            mockMvc.perform(post("/api/oauth2/recruiter/complete-registration")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orgInfo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("OAuth session expired. Please login with Google again."));

            verify(registrationService, never()).completeRecruiterProfileForOAuth(any(), any());
        }

        @Test
        @DisplayName("Should use 'email' attribute if 'oauth_email' not present")
        void shouldUseEmailAttributeIfOAuthEmailNotPresent() throws Exception {
            RecruiterRegistrationRequest.OrganizationInfo orgInfo = createValidOrganizationInfo();
            
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("email", "recruiter@company.com");
            session.setAttribute("oauth_timestamp", System.currentTimeMillis());

            when(registrationService.completeRecruiterProfileForOAuth(anyString(), any()))
                    .thenReturn(new Recruiter());

            mockMvc.perform(post("/api/oauth2/recruiter/complete-registration")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orgInfo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(registrationService).completeRecruiterProfileForOAuth(eq("recruiter@company.com"), any());
        }

        @Test
        @DisplayName("Should handle service exception")
        void shouldHandleServiceException() throws Exception {
            RecruiterRegistrationRequest.OrganizationInfo orgInfo = createValidOrganizationInfo();
            
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("oauth_email", "recruiter@company.com");
            session.setAttribute("oauth_timestamp", System.currentTimeMillis());

            when(registrationService.completeRecruiterProfileForOAuth(anyString(), any()))
                    .thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(post("/api/oauth2/recruiter/complete-registration")
                            .session(session)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orgInfo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("Error completing registration: Database error"));
        }
    }
}
