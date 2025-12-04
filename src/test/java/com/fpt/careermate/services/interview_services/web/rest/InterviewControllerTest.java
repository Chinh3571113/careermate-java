package com.fpt.careermate.services.interview_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.interview_services.service.dto.request.AnswerQuestionRequest;
import com.fpt.careermate.services.interview_services.service.dto.request.StartInterviewRequest;
import com.fpt.careermate.services.interview_services.service.dto.response.InterviewSessionResponse;
import com.fpt.careermate.services.interview_services.service.dto.response.NextQuestionResponse;
import com.fpt.careermate.services.interview_services.service.impl.InterviewService;
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

@WebMvcTest(InterviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("InterviewController Tests")
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InterviewService interviewService;

    @Nested
    @DisplayName("POST /api/interviews/start")
    class StartInterviewTests {

        @Test
        @DisplayName("Should start interview successfully")
        void shouldStartInterviewSuccessfully() throws Exception {
            StartInterviewRequest request = StartInterviewRequest.builder()
                    .jobDescription("Senior Java Developer position")
                    .build();
            InterviewSessionResponse response = new InterviewSessionResponse();

            when(interviewService.startInterview(any(StartInterviewRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/interviews/start")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Interview started successfully"));

            verify(interviewService).startInterview(any(StartInterviewRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /api/interviews/sessions/{sessionId}/questions/{questionId}/answer")
    class AnswerQuestionTests {

        @Test
        @DisplayName("Should answer question successfully")
        void shouldAnswerQuestionSuccessfully() throws Exception {
            int sessionId = 1;
            int questionId = 1;
            AnswerQuestionRequest request = AnswerQuestionRequest.builder()
                    .answer("My answer to the question")
                    .build();
            NextQuestionResponse response = new NextQuestionResponse();

            when(interviewService.answerQuestion(eq(sessionId), eq(questionId), any(AnswerQuestionRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/interviews/sessions/{sessionId}/questions/{questionId}/answer", sessionId, questionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Answer submitted successfully"));

            verify(interviewService).answerQuestion(eq(sessionId), eq(questionId), any(AnswerQuestionRequest.class));
        }
    }

    @Nested
    @DisplayName("GET /api/interviews/sessions/{sessionId}/next-question")
    class GetNextQuestionTests {

        @Test
        @DisplayName("Should get next question successfully")
        void shouldGetNextQuestionSuccessfully() throws Exception {
            int sessionId = 1;
            NextQuestionResponse response = new NextQuestionResponse();

            when(interviewService.getNextQuestion(sessionId)).thenReturn(response);

            mockMvc.perform(get("/api/interviews/sessions/{sessionId}/next-question", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Next question retrieved successfully"));

            verify(interviewService).getNextQuestion(sessionId);
        }
    }

    @Nested
    @DisplayName("POST /api/interviews/sessions/{sessionId}/complete")
    class CompleteInterviewTests {

        @Test
        @DisplayName("Should complete interview successfully")
        void shouldCompleteInterviewSuccessfully() throws Exception {
            int sessionId = 1;
            InterviewSessionResponse response = new InterviewSessionResponse();

            when(interviewService.completeInterview(sessionId)).thenReturn(response);

            mockMvc.perform(post("/api/interviews/sessions/{sessionId}/complete", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Interview completed successfully"));

            verify(interviewService).completeInterview(sessionId);
        }
    }

    @Nested
    @DisplayName("GET /api/interviews/sessions/{sessionId}")
    class GetSessionTests {

        @Test
        @DisplayName("Should get session successfully")
        void shouldGetSessionSuccessfully() throws Exception {
            int sessionId = 1;
            InterviewSessionResponse response = new InterviewSessionResponse();

            when(interviewService.getSession(sessionId)).thenReturn(response);

            mockMvc.perform(get("/api/interviews/sessions/{sessionId}", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Session retrieved successfully"));

            verify(interviewService).getSession(sessionId);
        }
    }

    @Nested
    @DisplayName("GET /api/interviews/sessions")
    class GetAllSessionsTests {

        @Test
        @DisplayName("Should get all sessions successfully")
        void shouldGetAllSessionsSuccessfully() throws Exception {
            InterviewSessionResponse session1 = new InterviewSessionResponse();
            InterviewSessionResponse session2 = new InterviewSessionResponse();
            List<InterviewSessionResponse> sessions = Arrays.asList(session1, session2);

            when(interviewService.getAllSessions()).thenReturn(sessions);

            mockMvc.perform(get("/api/interviews/sessions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Sessions retrieved successfully"));

            verify(interviewService).getAllSessions();
        }

        @Test
        @DisplayName("Should return empty list when no sessions")
        void shouldReturnEmptyListWhenNoSessions() throws Exception {
            when(interviewService.getAllSessions()).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/interviews/sessions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Sessions retrieved successfully"));

            verify(interviewService).getAllSessions();
        }
    }
}
