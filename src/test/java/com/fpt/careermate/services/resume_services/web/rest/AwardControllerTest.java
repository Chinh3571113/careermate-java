package com.fpt.careermate.services.resume_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fpt.careermate.services.resume_services.service.AwardImp;
import com.fpt.careermate.services.resume_services.service.dto.request.AwardRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.AwardResponse;
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

@WebMvcTest(AwardController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AwardController Tests")
class AwardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private AwardImp awardImp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    private AwardRequest createValidAwardRequest() {
        return AwardRequest.builder()
                .resumeId(1)
                .name("Best Employee Award")
                .organization("Tech Corp")
                .getDate(LocalDate.of(2023, 6, 15))
                .description("Awarded for outstanding performance")
                .build();
    }

    @Nested
    @DisplayName("POST /api/award")
    class AddAwardTests {

        @Test
        @DisplayName("Should add award successfully")
        void shouldAddAwardSuccessfully() throws Exception {
            AwardRequest request = createValidAwardRequest();
            AwardResponse response = new AwardResponse();

            when(awardImp.addAwardToResume(any(AwardRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/award")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Add award successfully"));

            verify(awardImp).addAwardToResume(any(AwardRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/award/{resumeId}/{awardId}")
    class UpdateAwardTests {

        @Test
        @DisplayName("Should update award successfully")
        void shouldUpdateAwardSuccessfully() throws Exception {
            AwardRequest request = createValidAwardRequest();
            AwardResponse response = new AwardResponse();

            when(awardImp.updateAwardInResume(eq(1), eq(2), any(AwardRequest.class))).thenReturn(response);

            mockMvc.perform(put("/api/award/1/2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Update award successfully"));

            verify(awardImp).updateAwardInResume(eq(1), eq(2), any(AwardRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/award/{awardId}")
    class RemoveAwardTests {

        @Test
        @DisplayName("Should remove award successfully")
        void shouldRemoveAwardSuccessfully() throws Exception {
            doNothing().when(awardImp).removeAwardFromResume(0, 1);

            mockMvc.perform(delete("/api/award/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Remove award successfully"));

            verify(awardImp).removeAwardFromResume(0, 1);
        }
    }
}
