package com.fpt.careermate.services.interview_services.service.impl;

import com.fpt.careermate.services.interview_services.service.dto.request.AnswerQuestionRequest;
import com.fpt.careermate.services.interview_services.service.dto.request.StartInterviewRequest;
import com.fpt.careermate.services.interview_services.service.dto.response.InterviewSessionResponse;
import com.fpt.careermate.services.interview_services.service.dto.response.NextQuestionResponse;

import java.util.List;

public interface InterviewService {

    /**
     * Start a new interview session with job description
     * Generates 8-10 questions based on JD
     */
    InterviewSessionResponse startInterview(StartInterviewRequest request);

    /**
     * Submit answer for a question
     * LLM scores the answer (0-10) and provides feedback
     */
    NextQuestionResponse answerQuestion(int sessionId, int questionId, AnswerQuestionRequest request);

    /**
     * Get next question in the interview
     */
    NextQuestionResponse getNextQuestion(int sessionId);

    /**
     * Complete interview and generate final report
     */
    InterviewSessionResponse completeInterview(int sessionId);

    /**
     * Get interview session by ID
     */
    InterviewSessionResponse getSession(int sessionId);

    /**
     * Get all interview sessions for candidate
     */
    List<InterviewSessionResponse> getAllSessions();
}

