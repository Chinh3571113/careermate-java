package com.fpt.careermate.services.resume_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.careermate.services.resume_services.service.EducationImp;
import com.fpt.careermate.services.resume_services.service.dto.request.EducationRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.EducationResponse;
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

@WebMvcTest(EducationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("EducationController Tests")
class EducationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private EducationImp educationImp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private EducationRequest createValidEducationRequest() {
        return EducationRequest.builder()
                .resumeId(1)
                .school("FPT University")
                .major("Software Engineering")
                .degree("Bachelor")
                .startDate(LocalDate.of(2019, 9, 1))
                .endDate(LocalDate.of(2023, 6, 30))
                .build();
    }

    @Nested
    @DisplayName("POST /api/education")
    class AddEducationTests {

        @Test
        @DisplayName("Should add education successfully")
        void shouldAddEducationSuccessfully() throws Exception {
            EducationRequest request = createValidEducationRequest();
            EducationResponse response = new EducationResponse();

            when(educationImp.addEducationToResume(any(EducationRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/education")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Add education successfully"));

            verify(educationImp).addEducationToResume(any(EducationRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/education/{resumeId}/{educationId}")
    class UpdateEducationTests {

        @Test
        @DisplayName("Should update education successfully")
        void shouldUpdateEducationSuccessfully() throws Exception {
            EducationRequest request = createValidEducationRequest();
            EducationResponse response = new EducationResponse();

            when(educationImp.updateEducationInResume(eq(1), eq(2), any(EducationRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/education/1/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Update education successfully"));

            verify(educationImp).updateEducationInResume(eq(1), eq(2), any(EducationRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/education/{educationId}")
    class RemoveEducationTests {

        @Test
        @DisplayName("Should remove education successfully")
        void shouldRemoveEducationSuccessfully() throws Exception {
            doNothing().when(educationImp).removeEducationFromResume(1);

            mockMvc.perform(delete("/api/education/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Remove education successfully"));

            verify(educationImp).removeEducationFromResume(1);
        }
    }
}
