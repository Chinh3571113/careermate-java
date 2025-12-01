package com.fpt.careermate.services.interview_services.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.interview_services.domain.InterviewQuestion;
import com.fpt.careermate.services.interview_services.domain.InterviewSession;
import com.fpt.careermate.services.interview_services.repository.InterviewQuestionRepo;
import com.fpt.careermate.services.interview_services.repository.InterviewSessionRepo;
import com.fpt.careermate.services.interview_services.service.dto.request.AnswerQuestionRequest;
import com.fpt.careermate.services.interview_services.service.dto.request.StartInterviewRequest;
import com.fpt.careermate.services.interview_services.service.dto.response.InterviewSessionResponse;
import com.fpt.careermate.services.interview_services.service.dto.response.NextQuestionResponse;
import com.fpt.careermate.services.interview_services.service.impl.InterviewService;
import com.fpt.careermate.services.interview_services.service.mapper.InterviewMapper;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.service.CandidateProfileImp;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static lombok.AccessLevel.PRIVATE;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class InterviewImp implements InterviewService {

    InterviewSessionRepo sessionRepo;
    InterviewQuestionRepo questionRepo;
    InterviewMapper mapper;
    CandidateProfileImp candidateProfileImp;
    ChatClient chatClient;

    private static final int TOTAL_QUESTIONS = 10;
    private static final String QUESTION_GENERATION_PROMPT = """
            You are an expert technical interviewer. Based on the following job description, generate exactly 10 interview questions.

            Job Description:
            %s

            Requirements:
            - Generate exactly 10 questions
            - Questions should be relevant to the job description
            - Include a mix of technical and behavioral questions
            - Questions should be clear and specific
            - Format: Return only the questions, numbered from 1 to 10, one per line

            Example format:
            1. [Question 1]
            2. [Question 2]
            ...
            10. [Question 10]
            """;

    private static final String SCORING_PROMPT = """
            You are an expert interviewer evaluating a candidate's answer.

            Question: %s
            Candidate's Answer: %s

            Please evaluate this answer and provide:
            1. A score from 0 to 10 (where 0 is completely wrong and 10 is excellent)
            2. Brief feedback on the answer (2-3 sentences focusing on key points, not repeating the answer)

            Format your response exactly as:
            Score: [number]
            Feedback: [your feedback]
            """;

    private static final String REPORT_GENERATION_PROMPT = """
            You are an expert interviewer generating a final interview report.

            Job Description:
            %s

            Interview Results:
            %s

            Generate a comprehensive interview report including:
            1. Overall Performance Summary
            2. Strengths
            3. Areas for Improvement
            4. Recommendation (Hire/Maybe/No Hire)

            Keep it professional and concise.
            """;

    @Override
    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    public InterviewSessionResponse startInterview(StartInterviewRequest request) {
        Candidate candidate = candidateProfileImp.generateProfile();

        // Create new interview session
        InterviewSession session = InterviewSession.builder()
                .candidate(candidate)
                .jobDescription(request.getJobDescription())
                .status("ONGOING")
                .build();

        session = sessionRepo.save(session);

        // Generate questions using Gemini
        String prompt = String.format(QUESTION_GENERATION_PROMPT, request.getJobDescription());
        String questionsText = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // Parse questions
        List<String> questions = parseQuestions(questionsText);

        // Save questions to database
        InterviewSession finalSession = session;
        List<InterviewQuestion> questionEntities = new ArrayList<>();
        for (int i = 0; i < questions.size() && i < TOTAL_QUESTIONS; i++) {
            InterviewQuestion question = InterviewQuestion.builder()
                    .session(finalSession)
                    .questionNumber(i + 1)
                    .question(questions.get(i))
                    .build();
            questionEntities.add(question);
        }
        questionRepo.saveAll(questionEntities);
        questionRepo.flush(); // Force flush to ensure questions are persisted before reload
        log.info("Saved {} questions for session {}", questionEntities.size(), session.getSessionId());

        // Reload session with questions using eager fetch
        session = sessionRepo.findByIdWithQuestions(session.getSessionId())
                .orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));

        log.info("Reloaded session {} with {} questions", session.getSessionId(),
                session.getQuestions() != null ? session.getQuestions().size() : 0);

        return mapper.toSessionResponse(session);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    public NextQuestionResponse answerQuestion(int sessionId, int questionId, AnswerQuestionRequest request) {
        Candidate candidate = candidateProfileImp.generateProfile();

        // Verify session belongs to candidate
        InterviewSession session = validateOngoingSession(sessionId, candidate.getCandidateId());

        // Get question
        InterviewQuestion question = questionRepo.findById(questionId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERVIEW_QUESTION_NOT_FOUND));

        validateQuestionBelongsToSession(question, sessionId);

        if (question.getCandidateAnswer() != null) {
            throw new AppException(ErrorCode.INTERVIEW_QUESTION_ALREADY_ANSWERED);
        }

        // Score answer using Gemini
        String scoringPrompt = String.format(SCORING_PROMPT, question.getQuestion(), request.getAnswer());
        String evaluation = chatClient.prompt()
                .user(scoringPrompt)
                .call()
                .content();

        // Parse score and feedback
        double score = parseScore(evaluation);
        String feedback = parseFeedback(evaluation);

        // Update question with answer, score, and feedback
        question.setCandidateAnswer(request.getAnswer());
        question.setScore(score);
        question.setFeedback(feedback);
        question.setAnsweredAt(LocalDateTime.now());
        questionRepo.save(question);

        // Get next question
        return getNextQuestionResponse(sessionId);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public NextQuestionResponse getNextQuestion(int sessionId) {
        Candidate candidate = candidateProfileImp.generateProfile();

        validateOngoingSession(sessionId, candidate.getCandidateId());

        return getNextQuestionResponse(sessionId);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    public InterviewSessionResponse completeInterview(int sessionId) {
        Candidate candidate = candidateProfileImp.generateProfile();

        InterviewSession session = validateOngoingSession(sessionId, candidate.getCandidateId());

        // Get all questions
        List<InterviewQuestion> questions = getQuestionsBySessionId(sessionId);

        // Calculate average score and build results
        StringBuilder resultsBuilder = new StringBuilder();
        double averageScore = calculateAverageScoreAndBuildResults(questions, resultsBuilder);

        // Generate final report using Gemini
        String reportPrompt = String.format(REPORT_GENERATION_PROMPT,
                session.getJobDescription(), resultsBuilder.toString());
        String finalReport = chatClient.prompt()
                .user(reportPrompt)
                .call()
                .content();

        // Update session
        session.setStatus("COMPLETED");
        session.setCompletedAt(LocalDateTime.now());
        session.setAverageScore(averageScore);
        session.setFinalReport(finalReport);
        sessionRepo.save(session);

        return mapper.toSessionResponse(session);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public InterviewSessionResponse getSession(int sessionId) {
        Candidate candidate = candidateProfileImp.generateProfile();

        InterviewSession session = sessionRepo
                .findBySessionIdAndCandidateIdWithQuestions(sessionId, candidate.getCandidateId())
                .orElseThrow(() -> new AppException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

        return mapper.toSessionResponse(session);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public List<InterviewSessionResponse> getAllSessions() {
        Candidate candidate = candidateProfileImp.generateProfile();

        List<InterviewSession> sessions = sessionRepo
                .findByCandidateCandidateIdOrderByCreatedAtDesc(candidate.getCandidateId());

        return sessions.stream()
                .map(mapper::toSessionResponse)
                .toList();
    }

    private InterviewSession validateOngoingSession(int sessionId, int candidateId) {
        InterviewSession session = sessionRepo.findBySessionIdAndCandidateCandidateId(sessionId, candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

        if (!"ONGOING".equals(session.getStatus())) {
            throw new AppException(ErrorCode.INTERVIEW_SESSION_NOT_ONGOING);
        }

        return session;
    }

    private void validateQuestionBelongsToSession(InterviewQuestion question, int sessionId) {
        if (question.getSession().getSessionId() != sessionId) {
            throw new AppException(ErrorCode.INTERVIEW_SESSION_FORBIDDEN);
        }
    }

    private List<InterviewQuestion> getQuestionsBySessionId(int sessionId) {
        return questionRepo.findBySessionSessionIdOrderByQuestionNumberAsc(sessionId);
    }

    /**
     * Find the first unanswered question and build response
     */
    private NextQuestionResponse getNextQuestionResponse(int sessionId) {
        List<InterviewQuestion> allQuestions = getQuestionsBySessionId(sessionId);

        InterviewQuestion nextQuestion = allQuestions.stream()
                .filter(q -> q.getCandidateAnswer() == null)
                .findFirst()
                .orElse(null);

        if (nextQuestion == null) {
            return NextQuestionResponse.builder()
                    .questionId(-1)
                    .questionNumber(-1)
                    .question("All questions completed")
                    .isLastQuestion(true)
                    .build();
        }

        return NextQuestionResponse.builder()
                .questionId(nextQuestion.getQuestionId())
                .questionNumber(nextQuestion.getQuestionNumber())
                .question(nextQuestion.getQuestion())
                .isLastQuestion(nextQuestion.getQuestionNumber() == TOTAL_QUESTIONS)
                .build();
    }

    /**
     * Calculate average score and build results string for report generation
     */
    private double calculateAverageScoreAndBuildResults(List<InterviewQuestion> questions,
            StringBuilder resultsBuilder) {
        double totalScore = 0;
        int answeredCount = 0;

        for (InterviewQuestion q : questions) {
            if (q.getScore() != null) {
                totalScore += q.getScore();
                answeredCount++;
                resultsBuilder.append(String.format("Q%d: %s\nAnswer: %s\nScore: %.1f\nFeedback: %s\n\n",
                        q.getQuestionNumber(), q.getQuestion(), q.getCandidateAnswer(),
                        q.getScore(), q.getFeedback()));
            }
        }

        return answeredCount > 0 ? totalScore / answeredCount : 0;
    }

    private List<String> parseQuestions(String questionsText) {
        List<String> questions = new ArrayList<>();
        String[] lines = questionsText.split("\n");

        for (String line : lines) {
            line = line.trim();
            // Match patterns like "1. Question" or "1) Question" or "1 Question"
            if (line.matches("^\\d+[.)\\s].*")) {
                // Remove number prefix
                String question = line.replaceFirst("^\\d+[.)\\s]+", "").trim();
                if (!question.isEmpty()) {
                    questions.add(question);
                }
            }
        }

        return questions;
    }

    private double parseScore(String evaluation) {
        Pattern pattern = Pattern.compile("Score:\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(evaluation);
        if (matcher.find()) {
            try {
                double score = Double.parseDouble(matcher.group(1));
                return Math.min(10, Math.max(0, score)); // Clamp between 0 and 10
            } catch (NumberFormatException e) {
                log.error("Failed to parse score from evaluation: {}", evaluation);
            }
        }
        return 5.0; // Default score if parsing fails
    }

    private String parseFeedback(String evaluation) {
        Pattern pattern = Pattern.compile("Feedback:\\s*(.+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(evaluation);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return evaluation; // Return whole evaluation if pattern doesn't match
    }
}
