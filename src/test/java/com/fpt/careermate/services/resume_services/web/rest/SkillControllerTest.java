package com.fpt.careermate.services.resume_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.resume_services.service.SkillImp;
import com.fpt.careermate.services.resume_services.service.dto.request.SkillRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.SkillResponse;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SkillController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SkillController Tests")
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private SkillImp skillImp;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private SkillRequest createValidSkillRequest() {
        return SkillRequest.builder()
                .resumeId(1)
                .skillType("Technical")
                .skillName("Java")
                .yearOfExperience(3)
                .build();
    }

    @Nested
    @DisplayName("POST /api/skill")
    class AddSkillTests {

        @Test
        @DisplayName("Should add skill successfully")
        void shouldAddSkillSuccessfully() throws Exception {
            SkillRequest request = createValidSkillRequest();
            SkillResponse response = new SkillResponse();

            when(skillImp.addSkillToResume(any(SkillRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/skill")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Add skill successfully"));

            verify(skillImp).addSkillToResume(any(SkillRequest.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/skill")
    class UpdateSkillTests {

        @Test
        @DisplayName("Should update skill successfully")
        void shouldUpdateSkillSuccessfully() throws Exception {
            SkillRequest request = createValidSkillRequest();
            SkillResponse response = new SkillResponse();

            when(skillImp.updateSkillInResume(eq(1), eq(2), any(SkillRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/skill")
                            .param("resumeId", "1")
                            .param("skillId", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Update skill successfully"));

            verify(skillImp).updateSkillInResume(eq(1), eq(2), any(SkillRequest.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/skill/{resumeId}/{skillId}")
    class RemoveSkillTests {

        @Test
        @DisplayName("Should remove skill successfully")
        void shouldRemoveSkillSuccessfully() throws Exception {
            doNothing().when(skillImp).removeSkillFromResume(1, 2);

            mockMvc.perform(delete("/api/skill/1/2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Remove skill successfully"));

            verify(skillImp).removeSkillFromResume(1, 2);
        }
    }
}
