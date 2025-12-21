package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.services.job_services.service.JdJdSkillImp;
import com.fpt.careermate.services.job_services.service.dto.response.JdSkillResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JdSkillController Tests")
class JdSkillControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JdJdSkillImp jdSkillImp;

    @InjectMocks
    private JdSkillController jdSkillController;

    private JdSkillResponse mockResponse;
    private List<JdSkillResponse> mockList;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(jdSkillController).build();

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
    @DisplayName("GET /api/jdskill - Search Skills Autocomplete")
    class SearchSkillsTests {
        @Test
        @DisplayName("Should search skills by keyword successfully")
        void searchSkills_ReturnsSuccess() throws Exception {
            when(jdSkillImp.getAllSkill(anyString(), anyString())).thenReturn(mockList);

            mockMvc.perform(get("/api/jdskill")
                            .param("keyword", "Java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("Should search skills with empty keyword successfully")
        void searchSkills_EmptyKeyword_ReturnsSuccess() throws Exception {
            when(jdSkillImp.getAllSkill(anyString(), anyString())).thenReturn(mockList);

            mockMvc.perform(get("/api/jdskill")
                            .param("keyword", ""))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("Should accept type filter and return success")
        void searchSkills_WithTypeFilter_ReturnsSuccess() throws Exception {
            when(jdSkillImp.getAllSkill(anyString(), eq("core"))).thenReturn(mockList);

            mockMvc.perform(get("/api/jdskill")
                            .param("type", "core"))
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
