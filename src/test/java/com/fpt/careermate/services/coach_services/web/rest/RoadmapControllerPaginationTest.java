package com.fpt.careermate.services.coach_services.web.rest;

import com.fpt.careermate.services.coach_services.service.RoadmapImp;
import com.fpt.careermate.services.coach_services.service.dto.response.ResumeRoadmapPageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RoadmapControllerPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoadmapImp roadmapImp;

    @Test
    @WithMockUser(roles = "CANDIDATE")
    public void testGetResumeRoadmaps_WithDefaultParameters() throws Exception {
        // Arrange
        ResumeRoadmapPageResponse mockResponse = ResumeRoadmapPageResponse.builder()
                .content(new ArrayList<>())
                .number(0)
                .size(10)
                .totalElements(0)
                .totalPages(0)
                .first(true)
                .last(true)
                .build();

        when(roadmapImp.getMyRoadmapListOfAResume(anyInt(), anyInt(), anyInt(), anyString()))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/roadmap/resume-roadmaps")
                        .param("resumeId", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.result.number").value(0))
                .andExpect(jsonPath("$.result.size").value(10))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.last").value(true));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    public void testGetResumeRoadmaps_WithCustomParameters() throws Exception {
        // Arrange
        ResumeRoadmapPageResponse mockResponse = ResumeRoadmapPageResponse.builder()
                .content(new ArrayList<>())
                .number(1)
                .size(20)
                .totalElements(25)
                .totalPages(2)
                .first(false)
                .last(true)
                .build();

        when(roadmapImp.getMyRoadmapListOfAResume(123, 1, 20, "roadmapname_asc"))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/roadmap/resume-roadmaps")
                        .param("resumeId", "123")
                        .param("page", "1")
                        .param("size", "20")
                        .param("sortBy", "roadmapname_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.result.number").value(1))
                .andExpect(jsonPath("$.result.size").value(20))
                .andExpect(jsonPath("$.result.totalElements").value(25))
                .andExpect(jsonPath("$.result.totalPages").value(2))
                .andExpect(jsonPath("$.result.first").value(false))
                .andExpect(jsonPath("$.result.last").value(true));
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    public void testGetResumeRoadmaps_WithWrongRole_ShouldFail() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/roadmap/resume-roadmaps")
                        .param("resumeId", "123"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetResumeRoadmaps_WithoutAuthentication_ShouldFail() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/roadmap/resume-roadmaps")
                        .param("resumeId", "123"))
                .andExpect(status().isUnauthorized());
    }
}

