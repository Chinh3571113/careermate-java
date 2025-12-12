package com.fpt.careermate.services.coach_services.web.rest;

import com.fpt.careermate.services.coach_services.service.RoadmapImp;
import com.fpt.careermate.services.coach_services.service.dto.response.RecommendedRoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.RoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.TopicDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoadmapController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("RoadmapController Tests")
class RoadmapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoadmapImp roadmapImp;

    @Nested
    @DisplayName("GET /api/roadmap - Get Roadmap by Name")
    class GetRoadmapTests {

        @Test
        @DisplayName("Should get roadmap with valid name successfully")
        void getRoadmap_WithValidName_ReturnsSuccess() throws Exception {
            // Arrange
            String roadmapName = "Backend Developer";
            RoadmapResponse response = RoadmapResponse.builder()
                    .name("BACKEND DEVELOPER")
                    .topics(new ArrayList<>())
                    .build();

            when(roadmapImp.getRoadmap(roadmapName)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/roadmap")
                            .param("roadmapName", roadmapName))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.result.name").value("BACKEND DEVELOPER"));

            verify(roadmapImp, times(1)).getRoadmap(roadmapName);
        }

        @Test
        @DisplayName("Should get roadmap with case-insensitive name")
        void getRoadmap_WithCaseInsensitiveName_ReturnsSuccess() throws Exception {
            // Arrange
            String roadmapName = "frontend developer";
            RoadmapResponse response = RoadmapResponse.builder()
                    .name("FRONTEND DEVELOPER")
                    .topics(new ArrayList<>())
                    .build();

            when(roadmapImp.getRoadmap(roadmapName)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/roadmap")
                            .param("roadmapName", roadmapName))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result.name").value("FRONTEND DEVELOPER"));

            verify(roadmapImp, times(1)).getRoadmap(roadmapName);
        }

        @Test
        @DisplayName("Should return error when roadmap not found")
        void getRoadmap_WithNonExistentName_ReturnsError() throws Exception {
            // Arrange
            String roadmapName = "NonExistent Roadmap";

            when(roadmapImp.getRoadmap(roadmapName))
                    .thenThrow(new RuntimeException("Roadmap not found"));

            // Act & Assert
            mockMvc.perform(get("/api/roadmap")
                            .param("roadmapName", roadmapName))
                    .andExpect(status().is5xxServerError());

            verify(roadmapImp, times(1)).getRoadmap(roadmapName);
        }

        @Test
        @DisplayName("Should handle roadmap name with spaces")
        void getRoadmap_WithSpacesInName_ReturnsSuccess() throws Exception {
            // Arrange
            String roadmapName = "  Backend Developer  ";
            RoadmapResponse response = RoadmapResponse.builder()
                    .name("BACKEND DEVELOPER")
                    .topics(new ArrayList<>())
                    .build();

            when(roadmapImp.getRoadmap(roadmapName)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/roadmap")
                            .param("roadmapName", roadmapName))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(roadmapImp, times(1)).getRoadmap(roadmapName);
        }
    }

    @Nested
    @DisplayName("GET /api/roadmap/topic/{topicId} - Get Topic Detail by ID")
    class GetTopicDetailTests {

        @Test
        @DisplayName("Should get topic detail successfully")
        void getTopicDetail_WithValidId_ReturnsSuccess() throws Exception {
            // Arrange
            int topicId = 1;
            TopicDetailResponse response = TopicDetailResponse.builder()
                    .name("Java Basics")
                    .description("Introduction to Java")
                    .resourceResponses(new ArrayList<>())
                    .build();

            when(roadmapImp.getTopicDetail(topicId)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/roadmap/topic/{topicId}", topicId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.result.name").value("Java Basics"));

            verify(roadmapImp, times(1)).getTopicDetail(topicId);
        }
    }

    @Nested
    @DisplayName("GET /api/roadmap/subtopic/{subtopicId} - Get Subtopic Detail by ID")
    class GetSubtopicDetailTests {

        @Test
        @DisplayName("Should get subtopic detail successfully")
        void getSubtopicDetail_WithValidId_ReturnsSuccess() throws Exception {
            // Arrange
            int subtopicId = 1;
            TopicDetailResponse response = TopicDetailResponse.builder()
                    .name("Variables and Data Types")
                    .description("Learn about Java variables")
                    .resourceResponses(new ArrayList<>())
                    .build();

            when(roadmapImp.getSubtopicDetail(subtopicId)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/roadmap/subtopic/{subtopicId}", subtopicId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.result.name").value("Variables and Data Types"));

            verify(roadmapImp, times(1)).getSubtopicDetail(subtopicId);
        }
    }

    @Nested
    @DisplayName("GET /api/roadmap/recommendation - Recommend Roadmap")
    class RecommendRoadmapTests {

        @Test
        @DisplayName("Should recommend roadmaps for valid role successfully")
        void recommendRoadmap_WithValidRole_ReturnsSuccess() throws Exception {
            // Arrange
            String role = "Backend Developer";
            List<RecommendedRoadmapResponse> recommendations = Arrays.asList(
                    RecommendedRoadmapResponse.builder()
                            .title("BACKEND DEVELOPER")
                            .similarityScore(0.95)
                            .build(),
                    RecommendedRoadmapResponse.builder()
                            .title("JAVA DEVELOPER")
                            .similarityScore(0.85)
                            .build(),
                    RecommendedRoadmapResponse.builder()
                            .title("FULL STACK DEVELOPER")
                            .similarityScore(0.75)
                            .build()
            );

            when(roadmapImp.recommendRoadmap(role)).thenReturn(recommendations);

            // Act & Assert
            mockMvc.perform(get("/api/roadmap/recommendation")
                            .param("role", role))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"))
                    .andExpect(jsonPath("$.result").isArray())
                    .andExpect(jsonPath("$.result[0].title").value("BACKEND DEVELOPER"))
                    .andExpect(jsonPath("$.result[0].similarityScore").value(0.95))
                    .andExpect(jsonPath("$.result.length()").value(3));

            verify(roadmapImp, times(1)).recommendRoadmap(role);
        }

        @Test
        @DisplayName("Should recommend roadmaps with lowercase role")
        void recommendRoadmap_WithLowercaseRole_ReturnsSuccess() throws Exception {
            // Arrange
            String role = "frontend developer";
            List<RecommendedRoadmapResponse> recommendations = Arrays.asList(
                    RecommendedRoadmapResponse.builder()
                            .title("FRONTEND DEVELOPER")
                            .similarityScore(0.92)
                            .build()
            );

            when(roadmapImp.recommendRoadmap(role)).thenReturn(recommendations);

            // Act & Assert
            mockMvc.perform(get("/api/roadmap/recommendation")
                            .param("role", role))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result[0].title").value("FRONTEND DEVELOPER"));

            verify(roadmapImp, times(1)).recommendRoadmap(role);
        }

        @Test
        @DisplayName("Should return empty list when no recommendations found")
        void recommendRoadmap_WithNoMatches_ReturnsEmptyList() throws Exception {
            // Arrange
            String role = "Rare Role";
            List<RecommendedRoadmapResponse> recommendations = new ArrayList<>();

            when(roadmapImp.recommendRoadmap(role)).thenReturn(recommendations);

            // Act & Assert
            mockMvc.perform(get("/api/roadmap/recommendation")
                            .param("role", role))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result").isEmpty());

            verify(roadmapImp, times(1)).recommendRoadmap(role);
        }

        @Test
        @DisplayName("Should handle role with spaces")
        void recommendRoadmap_WithSpacesInRole_ReturnsSuccess() throws Exception {
            // Arrange
            String role = "  DevOps Engineer  ";
            List<RecommendedRoadmapResponse> recommendations = Arrays.asList(
                    RecommendedRoadmapResponse.builder()
                            .title("DEVOPS ENGINEER")
                            .similarityScore(0.88)
                            .build()
            );

            when(roadmapImp.recommendRoadmap(role)).thenReturn(recommendations);

            // Act & Assert
            mockMvc.perform(get("/api/roadmap/recommendation")
                            .param("role", role))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.result[0].title").value("DEVOPS ENGINEER"));

            verify(roadmapImp, times(1)).recommendRoadmap(role);
        }
    }
}
