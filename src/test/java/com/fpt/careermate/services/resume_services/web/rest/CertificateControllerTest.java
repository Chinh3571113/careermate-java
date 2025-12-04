package com.fpt.careermate.services.resume_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.careermate.services.resume_services.service.CertificateImp;
import com.fpt.careermate.services.resume_services.service.dto.request.CertificateRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.CertificateResponse;
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

@WebMvcTest(CertificateController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CertificateController Tests")
class CertificateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private CertificateImp certificateImp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private CertificateRequest createValidCertificateRequest() {
        return CertificateRequest.builder()
                .resumeId(1)
                .name("AWS Solutions Architect")
                .organization("Amazon Web Services")
                .getDate(LocalDate.of(2023, 5, 10))
                .certificateUrl("https://aws.amazon.com/cert/123")
                .description("Professional level certification")
                .build();
    }

    @Nested
    @DisplayName("POST /api/certificate")
    class AddCertificateTests {

        @Test
        @DisplayName("Should add certificate successfully")
        void shouldAddCertificateSuccessfully() throws Exception {
            CertificateRequest request = createValidCertificateRequest();
            CertificateResponse response = new CertificateResponse();

            when(certificateImp.addCertificationToResume(any(CertificateRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/certificate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Add certificate successfully"));

            verify(certificateImp).addCertificationToResume(any(CertificateRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/certificate/{resumeId}/{certificateId}")
    class UpdateCertificateTests {

        @Test
        @DisplayName("Should update certificate successfully")
        void shouldUpdateCertificateSuccessfully() throws Exception {
            CertificateRequest request = createValidCertificateRequest();
            CertificateResponse response = new CertificateResponse();

            when(certificateImp.updateCertificationInResume(eq(1), eq(2), any(CertificateRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/certificate/1/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Update certificate successfully"));

            verify(certificateImp).updateCertificationInResume(eq(1), eq(2), any(CertificateRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/certificate/{certificateId}")
    class RemoveCertificateTests {

        @Test
        @DisplayName("Should remove certificate successfully")
        void shouldRemoveCertificateSuccessfully() throws Exception {
            doNothing().when(certificateImp).removeCertificationFromResume(1);

            mockMvc.perform(delete("/api/certificate/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Remove certificate successfully"));

            verify(certificateImp).removeCertificationFromResume(1);
        }
    }
}
