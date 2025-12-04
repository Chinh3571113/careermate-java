package com.fpt.careermate.services.resume_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.careermate.services.resume_services.service.HighLightProjectImp;
import com.fpt.careermate.services.resume_services.service.dto.request.HighlightProjectRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.HighlightProjectResponse;
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

@WebMvcTest(HighLightProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("HighLightProjectController Tests")
class HighLightProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private HighLightProjectImp highLightProjectImp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private HighlightProjectRequest createValidHighlightProjectRequest() {
        return HighlightProjectRequest.builder()
                .resumeId(1)
                .name("E-commerce Platform")
                .startDate(LocalDate.of(2022, 1, 1))
                .endDate(LocalDate.of(2023, 6, 30))
                .description("Developed a full-stack e-commerce platform")
                .projectUrl("https://github.com/project")
                .build();
    }

    @Nested
    @DisplayName("POST /api/highlight-project")
    class AddHighlightProjectTests {

        @Test
        @DisplayName("Should add highlight project successfully")
        void shouldAddHighlightProjectSuccessfully() throws Exception {
            HighlightProjectRequest request = createValidHighlightProjectRequest();
            HighlightProjectResponse response = new HighlightProjectResponse();

            when(highLightProjectImp.addHighlightProjectToResume(any(HighlightProjectRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/highlight-project")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Add highlight project successfully"));

            verify(highLightProjectImp).addHighlightProjectToResume(any(HighlightProjectRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/highlight-project/{resumeId}/{highlightProjectId}")
    class UpdateHighlightProjectTests {

        @Test
        @DisplayName("Should update highlight project successfully")
        void shouldUpdateHighlightProjectSuccessfully() throws Exception {
            int resumeId = 1;
            int highlightProjectId = 2;
            HighlightProjectRequest request = createValidHighlightProjectRequest();
            HighlightProjectResponse response = new HighlightProjectResponse();

            when(highLightProjectImp.updateHighlightProjectInResume(
                    eq(resumeId), eq(highlightProjectId), any(HighlightProjectRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/highlight-project/{resumeId}/{highlightProjectId}", resumeId, highlightProjectId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Update highlight project successfully"));

            verify(highLightProjectImp).updateHighlightProjectInResume(
                    eq(resumeId), eq(highlightProjectId), any(HighlightProjectRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/highlight-project/{highlightProjectId}")
    class RemoveHighlightProjectTests {

        @Test
        @DisplayName("Should remove highlight project successfully")
        void shouldRemoveHighlightProjectSuccessfully() throws Exception {
            int highlightProjectId = 1;

            doNothing().when(highLightProjectImp).removeHighlightProjectFromResume(highlightProjectId);

            mockMvc.perform(delete("/api/highlight-project/{highlightProjectId}", highlightProjectId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Remove highlight project successfully"));

            verify(highLightProjectImp).removeHighlightProjectFromResume(highlightProjectId);
        }
    }
}
