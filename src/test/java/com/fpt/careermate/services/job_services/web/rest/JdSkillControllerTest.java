package com.fpt.careermate.services.job_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.job_services.service.JdJdSkillImp;
import com.fpt.careermate.services.job_services.service.dto.response.JdSkillResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JdSkillController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("JdSkillController Tests")
class JdSkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JdJdSkillImp jdSkillImp;

    private JdSkillResponse mockResponse;
    private List<JdSkillResponse> mockList;

    @BeforeEach
    void setUp() {
        mockResponse = JdSkillResponse.builder()
                .id(1)
                .name("Java")
                .build();

        mockList = Collections.singletonList(mockResponse);
    }

    @Nested
    @DisplayName("POST /api/jdskill - Create JD Skill")
    class CreateJdSkillTests {
        @Test
        @DisplayName("Should create JD skill successfully")
        void createJdSkill_ReturnsSuccess() throws Exception {
            doNothing().when(jdSkillImp).createSkill(anyString());

            mockMvc.perform(post("/api/jdskill")
                            .param("name", "Java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/jdskill - Get Skill List")
    class GetSkillListTests {
        @Test
        @DisplayName("Should get skill list successfully")
        void getSkillList_ReturnsSuccess() throws Exception {
            when(jdSkillImp.getAllSkill()).thenReturn(mockList);

            mockMvc.perform(get("/api/jdskill"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("GET /api/jdskill/top-used - Get Top Used Skills")
    class GetTopUsedSkillsTests {
        @Test
        @DisplayName("Should get top used skills successfully")
        void getTopUsedSkills_ReturnsSuccess() throws Exception {
            when(jdSkillImp.getTopUsedSkillsFromFirst50()).thenReturn(mockList);

            mockMvc.perform(get("/api/jdskill/top-used"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
