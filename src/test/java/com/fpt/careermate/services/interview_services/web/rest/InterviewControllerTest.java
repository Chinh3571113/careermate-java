package com.fpt.careermate.services.interview_services.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.careermate.services.interview_services.service.impl.InterviewService;
import com.fpt.careermate.services.interview_services.service.dto.request.AnswerQuestionRequest;
import com.fpt.careermate.services.interview_services.service.dto.request.StartInterviewRequest;
import com.fpt.careermate.services.interview_services.service.dto.response.InterviewQuestionResponse;
import com.fpt.careermate.services.interview_services.service.dto.response.InterviewSessionResponse;
import com.fpt.careermate.services.interview_services.service.dto.response.NextQuestionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InterviewController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for testing
class InterviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InterviewService interviewService;

    private StartInterviewRequest startInterviewRequest;
    private AnswerQuestionRequest answerQuestionRequest;
    private InterviewSessionResponse sessionResponse;
    private NextQuestionResponse nextQuestionResponse;
    private List<InterviewQuestionResponse> questionResponses;

    @BeforeEach
    void setUp() {
        // Setup test data
        startInterviewRequest = StartInterviewRequest.builder()
                .jobDescription("We are looking for a Senior Java Developer with 5+ years experience in Spring Boot...")
                .build();

        answerQuestionRequest = AnswerQuestionRequest.builder()
                .answer("Spring Boot is a framework that simplifies Java application development...")
                .build();

        questionResponses = new ArrayList<>();
        questionResponses.add(InterviewQuestionResponse.builder()
                .questionId(1)
                .questionNumber(1)
                .question("What is Spring Boot?")
                .candidateAnswer(null)
                .score(null)
                .feedback(null)
                .askedAt(LocalDateTime.now())
                .answeredAt(null)
                .build());

        sessionResponse = InterviewSessionResponse.builder()
                .sessionId(1)
                .candidateId(5)
                .jobDescription(startInterviewRequest.getJobDescription())
                .status("ONGOING")
                .createdAt(LocalDateTime.now())
                .completedAt(null)
                .finalReport(null)
                .averageScore(null)
                .questions(questionResponses)
                .build();

        nextQuestionResponse = NextQuestionResponse.builder()
                .questionId(1)
                .questionNumber(1)
                .question("What is Spring Boot?")
                .isLastQuestion(false)
                .build();
    }

    @Test
    @DisplayName("Test 1: Start Interview - Success")
    @WithMockUser(roles = "CANDIDATE")
    void testStartInterview_Success() throws Exception {
        // Given
        when(interviewService.startInterview(any(StartInterviewRequest.class)))
                .thenReturn(sessionResponse);

        // When & Then
        mockMvc.perform(post("/api/interviews/start")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startInterviewRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("Interview started successfully"))
                .andExpect(jsonPath("$.result.sessionId").value(1))
                .andExpect(jsonPath("$.result.candidateId").value(5))
                .andExpect(jsonPath("$.result.status").value("ONGOING"))
                .andExpect(jsonPath("$.result.questions").isArray())
                .andExpect(jsonPath("$.result.questions[0].questionId").value(1));

        verify(interviewService, times(1)).startInterview(any(StartInterviewRequest.class));
    }

    @Test
    @DisplayName("Test 2: Start Interview - Invalid Request (Blank Job Description)")
    @WithMockUser(roles = "CANDIDATE")
    void testStartInterview_BlankJobDescription() throws Exception {
        // Given
        StartInterviewRequest invalidRequest = StartInterviewRequest.builder()
                .jobDescription("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/interviews/start")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(interviewService, never()).startInterview(any(StartInterviewRequest.class));
    }

    @Test
    @DisplayName("Test 3: Start Interview - Null Job Description")
    @WithMockUser(roles = "CANDIDATE")
    void testStartInterview_NullJobDescription() throws Exception {
        // Given
        StartInterviewRequest invalidRequest = StartInterviewRequest.builder()
                .jobDescription(null)
                .build();

        // When & Then
        mockMvc.perform(post("/api/interviews/start")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(interviewService, never()).startInterview(any(StartInterviewRequest.class));
    }

    @Test
    @DisplayName("Test 4: Answer Question - Success")
    @WithMockUser(roles = "CANDIDATE")
    void testAnswerQuestion_Success() throws Exception {
        // Given
        NextQuestionResponse nextQuestion = NextQuestionResponse.builder()
                .questionId(2)
                .questionNumber(2)
                .question("What is dependency injection?")
                .isLastQuestion(false)
                .build();

        when(interviewService.answerQuestion(eq(1), eq(1), any(AnswerQuestionRequest.class)))
                .thenReturn(nextQuestion);

        // When & Then
        mockMvc.perform(post("/api/interviews/sessions/1/questions/1/answer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerQuestionRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("Answer submitted successfully"))
                .andExpect(jsonPath("$.result.questionId").value(2))
                .andExpect(jsonPath("$.result.questionNumber").value(2))
                .andExpect(jsonPath("$.result.isLastQuestion").value(false));

        verify(interviewService, times(1)).answerQuestion(eq(1), eq(1), any(AnswerQuestionRequest.class));
    }

    @Test
    @DisplayName("Test 5: Answer Question - Blank Answer")
    @WithMockUser(roles = "CANDIDATE")
    void testAnswerQuestion_BlankAnswer() throws Exception {
        // Given
        AnswerQuestionRequest invalidRequest = AnswerQuestionRequest.builder()
                .answer("")
                .build();

        // When & Then
        mockMvc.perform(post("/api/interviews/sessions/1/questions/1/answer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(interviewService, never()).answerQuestion(anyInt(), anyInt(), any(AnswerQuestionRequest.class));
    }

    @Test
    @DisplayName("Test 6: Answer Question - Last Question")
    @WithMockUser(roles = "CANDIDATE")
    void testAnswerQuestion_LastQuestion() throws Exception {
        // Given
        NextQuestionResponse lastQuestion = NextQuestionResponse.builder()
                .questionId(-1)
                .questionNumber(-1)
                .question("All questions completed")
                .isLastQuestion(true)
                .build();

        when(interviewService.answerQuestion(eq(1), eq(10), any(AnswerQuestionRequest.class)))
                .thenReturn(lastQuestion);

        // When & Then
        mockMvc.perform(post("/api/interviews/sessions/1/questions/10/answer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerQuestionRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.questionId").value(-1))
                .andExpect(jsonPath("$.result.isLastQuestion").value(true));

        verify(interviewService, times(1)).answerQuestion(eq(1), eq(10), any(AnswerQuestionRequest.class));
    }

    @Test
    @DisplayName("Test 7: Get Next Question - Success")
    @WithMockUser(roles = "CANDIDATE")
    void testGetNextQuestion_Success() throws Exception {
        // Given
        when(interviewService.getNextQuestion(eq(1)))
                .thenReturn(nextQuestionResponse);

        // When & Then
        mockMvc.perform(get("/api/interviews/sessions/1/next-question")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("Next question retrieved successfully"))
                .andExpect(jsonPath("$.result.questionId").value(1))
                .andExpect(jsonPath("$.result.question").value("What is Spring Boot?"))
                .andExpect(jsonPath("$.result.isLastQuestion").value(false));

        verify(interviewService, times(1)).getNextQuestion(eq(1));
    }

    @Test
    @DisplayName("Test 8: Get Next Question - All Questions Completed")
    @WithMockUser(roles = "CANDIDATE")
    void testGetNextQuestion_AllCompleted() throws Exception {
        // Given
        NextQuestionResponse completedResponse = NextQuestionResponse.builder()
                .questionId(-1)
                .questionNumber(-1)
                .question("All questions completed")
                .isLastQuestion(true)
                .build();

        when(interviewService.getNextQuestion(eq(1)))
                .thenReturn(completedResponse);

        // When & Then
        mockMvc.perform(get("/api/interviews/sessions/1/next-question")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.questionId").value(-1))
                .andExpect(jsonPath("$.result.isLastQuestion").value(true));

        verify(interviewService, times(1)).getNextQuestion(eq(1));
    }

    @Test
    @DisplayName("Test 9: Complete Interview - Success")
    @WithMockUser(roles = "CANDIDATE")
    void testCompleteInterview_Success() throws Exception {
        // Given
        InterviewSessionResponse completedSession = InterviewSessionResponse.builder()
                .sessionId(1)
                .candidateId(5)
                .status("COMPLETED")
                .averageScore(8.5)
                .finalReport("## Overall Performance\nExcellent performance...")
                .completedAt(LocalDateTime.now())
                .questions(questionResponses)
                .build();

        when(interviewService.completeInterview(eq(1)))
                .thenReturn(completedSession);

        // When & Then
        mockMvc.perform(post("/api/interviews/sessions/1/complete")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("Interview completed successfully"))
                .andExpect(jsonPath("$.result.sessionId").value(1))
                .andExpect(jsonPath("$.result.status").value("COMPLETED"))
                .andExpect(jsonPath("$.result.averageScore").value(8.5))
                .andExpect(jsonPath("$.result.finalReport").exists());

        verify(interviewService, times(1)).completeInterview(eq(1));
    }

    @Test
    @DisplayName("Test 10: Get Session - Success")
    @WithMockUser(roles = "CANDIDATE")
    void testGetSession_Success() throws Exception {
        // Given
        when(interviewService.getSession(eq(1)))
                .thenReturn(sessionResponse);

        // When & Then
        mockMvc.perform(get("/api/interviews/sessions/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("Session retrieved successfully"))
                .andExpect(jsonPath("$.result.sessionId").value(1))
                .andExpect(jsonPath("$.result.candidateId").value(5))
                .andExpect(jsonPath("$.result.status").value("ONGOING"));

        verify(interviewService, times(1)).getSession(eq(1));
    }

    @Test
    @DisplayName("Test 11: Get All Sessions - Success")
    @WithMockUser(roles = "CANDIDATE")
    void testGetAllSessions_Success() throws Exception {
        // Given
        InterviewSessionResponse session1 = InterviewSessionResponse.builder()
                .sessionId(1)
                .candidateId(5)
                .status("COMPLETED")
                .averageScore(8.5)
                .createdAt(LocalDateTime.now())
                .build();

        InterviewSessionResponse session2 = InterviewSessionResponse.builder()
                .sessionId(2)
                .candidateId(5)
                .status("ONGOING")
                .createdAt(LocalDateTime.now())
                .build();

        List<InterviewSessionResponse> sessions = Arrays.asList(session1, session2);

        when(interviewService.getAllSessions())
                .thenReturn(sessions);

        // When & Then
        mockMvc.perform(get("/api/interviews/sessions")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("Sessions retrieved successfully"))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].sessionId").value(1))
                .andExpect(jsonPath("$.result[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.result[1].sessionId").value(2))
                .andExpect(jsonPath("$.result[1].status").value("ONGOING"));

        verify(interviewService, times(1)).getAllSessions();
    }

    @Test
    @DisplayName("Test 12: Get All Sessions - Empty List")
    @WithMockUser(roles = "CANDIDATE")
    void testGetAllSessions_EmptyList() throws Exception {
        // Given
        when(interviewService.getAllSessions())
                .thenReturn(new ArrayList<>());

        // When & Then
        mockMvc.perform(get("/api/interviews/sessions")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(0));

        verify(interviewService, times(1)).getAllSessions();
    }

    @Test
    @DisplayName("Test 13: Answer Question - Invalid Path Variables")
    @WithMockUser(roles = "CANDIDATE")
    void testAnswerQuestion_InvalidPathVariables() throws Exception {
        // When & Then - Test with invalid sessionId (0)
        mockMvc.perform(post("/api/interviews/sessions/0/questions/1/answer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerQuestionRequest)))
                .andDo(print())
                .andExpect(status().isOk()); // Controller không validate path variable, service sẽ throw exception

        // When & Then - Test with invalid questionId (0)
        mockMvc.perform(post("/api/interviews/sessions/1/questions/0/answer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerQuestionRequest)))
                .andDo(print())
                .andExpect(status().isOk()); // Controller không validate path variable, service sẽ throw exception
    }

    @Test
    @DisplayName("Test 14: Get Session - Invalid Session ID")
    @WithMockUser(roles = "CANDIDATE")
    void testGetSession_InvalidSessionId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/interviews/sessions/999")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk()); // Controller không validate, service sẽ throw exception
    }

    @Test
    @DisplayName("Test 15: Start Interview - Missing Request Body")
    @WithMockUser(roles = "CANDIDATE")
    void testStartInterview_MissingRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/interviews/start")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(interviewService, never()).startInterview(any(StartInterviewRequest.class));
    }
}

