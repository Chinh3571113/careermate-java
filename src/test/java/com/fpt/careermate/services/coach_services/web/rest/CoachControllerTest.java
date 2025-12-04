package com.fpt.careermate.services.coach_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.coach_services.service.CourseImp;
import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
import com.fpt.careermate.services.coach_services.service.dto.response.CoursePageResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.RecommendedCourseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CoachController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CoachController Tests")
class CoachControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseImp courseImp;

    @Nested
    @DisplayName("GET /api/coach/course/recommendation")
    class RecommendCoursesTests {

        @Test
        @DisplayName("Should recommend courses for a role")
        void shouldRecommendCoursesForRole() throws Exception {
            String role = "backend developer";
            RecommendedCourseResponse course1 = new RecommendedCourseResponse();
            RecommendedCourseResponse course2 = new RecommendedCourseResponse();
            List<RecommendedCourseResponse> courses = Arrays.asList(course1, course2);

            when(courseImp.recommendCourse(role)).thenReturn(courses);

            mockMvc.perform(get("/api/coach/course/recommendation")
                            .param("role", role))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"));

            verify(courseImp).recommendCourse(role);
        }

        @Test
        @DisplayName("Should return empty list when no courses found")
        void shouldReturnEmptyListWhenNoCoursesFound() throws Exception {
            String role = "unknown role";
            when(courseImp.recommendCourse(role)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/coach/course/recommendation")
                            .param("role", role))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(courseImp).recommendCourse(role);
        }
    }

    @Nested
    @DisplayName("POST /api/coach/course")
    class AddCourseTests {

        @Test
        @DisplayName("Should add course successfully")
        void shouldAddCourseSuccessfully() throws Exception {
            CourseCreationRequest request = new CourseCreationRequest();

            doNothing().when(courseImp).addCourse(any(CourseCreationRequest.class));

            mockMvc.perform(post("/api/coach/course")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"));

            verify(courseImp).addCourse(any(CourseCreationRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/coach/course/marked")
    class GetMarkedCoursesTests {

        @Test
        @DisplayName("Should get marked courses with pagination")
        void shouldGetMarkedCoursesWithPagination() throws Exception {
            CoursePageResponse response = new CoursePageResponse();

            when(courseImp.getMyCoursesWithMarkedStatus(0, 10)).thenReturn(response);

            mockMvc.perform(get("/api/coach/course/marked")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"));

            verify(courseImp).getMyCoursesWithMarkedStatus(0, 10);
        }
    }

    @Nested
    @DisplayName("GET /api/coach/course/unmarked")
    class GetUnmarkedCoursesTests {

        @Test
        @DisplayName("Should get unmarked courses with pagination")
        void shouldGetUnmarkedCoursesWithPagination() throws Exception {
            CoursePageResponse response = new CoursePageResponse();

            when(courseImp.getMyCoursesWithUnMarkedStatus(0, 10)).thenReturn(response);

            mockMvc.perform(get("/api/coach/course/unmarked")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"));

            verify(courseImp).getMyCoursesWithUnMarkedStatus(0, 10);
        }
    }

    @Nested
    @DisplayName("PATCH /api/coach/course/marked/{courseId}")
    class MarkCourseTests {

        @Test
        @DisplayName("Should mark course successfully")
        void shouldMarkCourseSuccessfully() throws Exception {
            int courseId = 1;
            boolean marked = true;

            doNothing().when(courseImp).markCourse(courseId, marked);

            mockMvc.perform(patch("/api/coach/course/marked/{courseId}", courseId)
                            .param("courseId", String.valueOf(courseId))
                            .param("marked", String.valueOf(marked)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("success"));

            verify(courseImp).markCourse(courseId, marked);
        }

        @Test
        @DisplayName("Should unmark course successfully")
        void shouldUnmarkCourseSuccessfully() throws Exception {
            int courseId = 1;
            boolean marked = false;

            doNothing().when(courseImp).markCourse(courseId, marked);

            mockMvc.perform(patch("/api/coach/course/marked/{courseId}", courseId)
                            .param("courseId", String.valueOf(courseId))
                            .param("marked", String.valueOf(marked)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(courseImp).markCourse(courseId, marked);
        }
    }
}
