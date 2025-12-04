package com.fpt.careermate.services.resume_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.careermate.services.resume_services.service.WorkExperienceImp;
import com.fpt.careermate.services.resume_services.service.dto.request.WorkExperienceRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.WorkExperienceResponse;
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

@WebMvcTest(WorkExpController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("WorkExpController Tests")
class WorkExpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private WorkExperienceImp workExperienceImp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private WorkExperienceRequest createValidWorkExperienceRequest() {
        return WorkExperienceRequest.builder()
                .resumeId(1)
                .jobTitle("Software Engineer")
                .company("Tech Corp")
                .startDate(LocalDate.of(2021, 1, 1))
                .endDate(LocalDate.of(2023, 12, 31))
                .description("Developed software applications")
                .project("E-commerce Platform")
                .build();
    }

    @Nested
    @DisplayName("POST /api/work-exp")
    class AddWorkExperienceTests {

        @Test
        @DisplayName("Should add work experience successfully")
        void shouldAddWorkExperienceSuccessfully() throws Exception {
            WorkExperienceRequest request = createValidWorkExperienceRequest();
            WorkExperienceResponse response = new WorkExperienceResponse();

            when(workExperienceImp.addWorkExperienceToResume(any(WorkExperienceRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/work-exp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Add work experience successfully"));

            verify(workExperienceImp).addWorkExperienceToResume(any(WorkExperienceRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/work-exp/{resumeId}/{workExpId}")
    class UpdateWorkExperienceTests {

        @Test
        @DisplayName("Should update work experience successfully")
        void shouldUpdateWorkExperienceSuccessfully() throws Exception {
            WorkExperienceRequest request = createValidWorkExperienceRequest();
            WorkExperienceResponse response = new WorkExperienceResponse();

            when(workExperienceImp.updateWorkExperienceInResume(eq(1), eq(2), any(WorkExperienceRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/work-exp/1/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Update work experience successfully"));

            verify(workExperienceImp).updateWorkExperienceInResume(eq(1), eq(2), any(WorkExperienceRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/work-exp/{workExpId}")
    class RemoveWorkExperienceTests {

        @Test
        @DisplayName("Should remove work experience successfully")
        void shouldRemoveWorkExperienceSuccessfully() throws Exception {
            doNothing().when(workExperienceImp).removeWorkExperienceFromResume(1);

            mockMvc.perform(delete("/api/work-exp/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Remove work experience successfully"));

            verify(workExperienceImp).removeWorkExperienceFromResume(1);
        }
    }
}
