package com.fpt.careermate.services.authentication_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.RegistrationService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RegistrationController Tests")
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegistrationService registrationService;

    private RecruiterRegistrationRequest createValidRecruiterRequest() {
        return RecruiterRegistrationRequest.builder()
                .username("recruiter123")
                .email("recruiter@company.com")
                .password("Password123!")
                .organizationInfo(RecruiterRegistrationRequest.OrganizationInfo.builder()
                        .companyName("Tech Company Inc")
                        .website("https://techcompany.com")
                        .about("A leading technology company")
                        .build())
                .build();
    }

    @Nested
    @DisplayName("POST /api/registration/recruiter")
    class RegisterRecruiterTests {

        @Test
        @DisplayName("Should register recruiter successfully")
        void shouldRegisterRecruiterSuccessfully() throws Exception {
            RecruiterRegistrationRequest request = createValidRecruiterRequest();
            Account account = new Account();
            account.setId(1);

            when(registrationService.registerRecruiter(any(RecruiterRegistrationRequest.class))).thenReturn(account);

            mockMvc.perform(post("/api/registration/recruiter")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result").value("Account ID: 1"));

            verify(registrationService).registerRecruiter(any(RecruiterRegistrationRequest.class));
        }
    }
}
