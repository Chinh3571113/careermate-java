# AI Interview Chatbot - Frontend Integration Guide

## Overview
This guide explains how to integrate the AI Interview Chatbot feature into your React frontend. The chatbot conducts technical interviews based on job descriptions, provides real-time scoring, and generates comprehensive reports.

## Features
- ✅ Generate 10 interview questions based on job description
- ✅ Real-time answer evaluation and scoring (0-10)
- ✅ AI feedback on each answer
- ✅ Final interview report generation
- ✅ Interview history tracking
- ✅ Token-efficient design (feedback only, no answer modification)

## API Endpoints

### Base URL
```
http://localhost:8080/api/interviews
```

### Authentication
All endpoints require **CANDIDATE** role authentication. Include JWT token in Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## API Documentation

### 1. Start New Interview Session
**POST** `/start`

Starts a new interview session and generates 10 questions based on the provided job description.

**Request Body:**
```json
{
  "jobDescription": "We are looking for a Senior Java Developer with 5+ years experience in Spring Boot, microservices..."
}
```

**Response:**
```json
{
  "code": 0,
  "message": "Interview started successfully",
  "result": {
    "sessionId": 1,
    "candidateId": 5,
    "jobDescription": "We are looking for...",
    "status": "ONGOING",
    "createdAt": "2025-11-26T10:30:00",
    "completedAt": null,
    "finalReport": null,
    "averageScore": null,
    "questions": [
      {
        "questionId": 1,
        "questionNumber": 1,
        "question": "Can you explain the difference between @Component, @Service, and @Repository in Spring?",
        "candidateAnswer": null,
        "score": null,
        "feedback": null,
        "askedAt": "2025-11-26T10:30:01",
        "answeredAt": null
      },
      // ... 9 more questions
    ]
  }
}
```

---

### 2. Get Next Question
**GET** `/sessions/{sessionId}/next-question`

Retrieves the next unanswered question in the interview.

**Response:**
```json
{
  "code": 0,
  "message": "Next question retrieved successfully",
  "result": {
    "questionId": 1,
    "questionNumber": 1,
    "question": "Can you explain the difference between @Component, @Service, and @Repository in Spring?",
    "isLastQuestion": false
  }
}
```

When all questions are completed:
```json
{
  "questionId": -1,
  "questionNumber": -1,
  "question": "All questions completed",
  "isLastQuestion": true
}
```

---

### 3. Submit Answer
**POST** `/sessions/{sessionId}/questions/{questionId}/answer`

Submit an answer for a specific question. The AI will score it (0-10) and provide feedback.

**Request Body:**
```json
{
  "answer": "@Component is the generic stereotype for any Spring-managed component. @Service is a specialization used in the service layer..."
}
```

**Response:**
```json
{
  "code": 0,
  "message": "Answer submitted successfully",
  "result": {
    "questionId": 2,
    "questionNumber": 2,
    "question": "What is dependency injection and how does Spring implement it?",
    "isLastQuestion": false
  }
}
```

> **Note:** The response contains the NEXT question, not the current one. To see the score/feedback, fetch the session details.

---

### 4. Complete Interview
**POST** `/sessions/{sessionId}/complete`

Completes the interview and generates a comprehensive final report.

**Response:**
```json
{
  "code": 0,
  "message": "Interview completed successfully",
  "result": {
    "sessionId": 1,
    "candidateId": 5,
    "jobDescription": "...",
    "status": "COMPLETED",
    "createdAt": "2025-11-26T10:30:00",
    "completedAt": "2025-11-26T11:00:00",
    "averageScore": 7.8,
    "finalReport": "## Overall Performance Summary\n\nThe candidate demonstrated strong knowledge...\n\n## Strengths\n- Excellent understanding of Spring Framework...\n\n## Areas for Improvement\n- Could elaborate more on microservices patterns...\n\n## Recommendation: HIRE",
    "questions": [
      {
        "questionId": 1,
        "questionNumber": 1,
        "question": "Can you explain the difference between @Component, @Service, and @Repository in Spring?",
        "candidateAnswer": "@Component is the generic stereotype...",
        "score": 8.5,
        "feedback": "Good explanation covering the key differences. You correctly identified the specialization hierarchy.",
        "askedAt": "2025-11-26T10:30:01",
        "answeredAt": "2025-11-26T10:32:15"
      },
      // ... remaining questions with scores and feedback
    ]
  }
}
```

---

### 5. Get Session Details
**GET** `/sessions/{sessionId}`

Retrieves complete details of a specific interview session.

**Response:** Same structure as "Complete Interview" response.

---

### 6. Get All Sessions
**GET** `/sessions`

Retrieves all interview sessions for the authenticated candidate (ordered by newest first).

**Response:**
```json
{
  "code": 0,
  "message": "Sessions retrieved successfully",
  "result": [
    {
      "sessionId": 3,
      "status": "COMPLETED",
      "averageScore": 8.2,
      "createdAt": "2025-11-26T14:00:00",
      // ... full session details
    },
    {
      "sessionId": 2,
      "status": "ONGOING",
      "averageScore": null,
      "createdAt": "2025-11-25T10:00:00",
      // ... full session details
    }
  ]
}
```

---

## React Implementation Example

### State Management
```typescript
interface InterviewState {
  sessionId: number | null;
  currentQuestion: Question | null;
  questions: Question[];
  status: 'idle' | 'ongoing' | 'completed';
  averageScore: number | null;
  finalReport: string | null;
}

interface Question {
  questionId: number;
  questionNumber: number;
  question: string;
  candidateAnswer?: string;
  score?: number;
  feedback?: string;
  isLastQuestion?: boolean;
}
```

### Step 1: Start Interview Component
```tsx
import { useState } from 'react';
import axios from 'axios';

const StartInterview = ({ onStarted }: { onStarted: (sessionId: number) => void }) => {
  const [jobDescription, setJobDescription] = useState('');
  const [loading, setLoading] = useState(false);

  const handleStart = async () => {
    setLoading(true);
    try {
      const response = await axios.post('/api/interviews/start', {
        jobDescription
      }, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      
      onStarted(response.data.result.sessionId);
    } catch (error) {
      console.error('Failed to start interview:', error);
      alert('Failed to start interview. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="start-interview">
      <h2>Start AI Interview</h2>
      <textarea
        value={jobDescription}
        onChange={(e) => setJobDescription(e.target.value)}
        placeholder="Paste the job description here..."
        rows={10}
        style={{ width: '100%', padding: '10px' }}
      />
      <button 
        onClick={handleStart} 
        disabled={!jobDescription.trim() || loading}
      >
        {loading ? 'Generating Questions...' : 'Start Interview'}
      </button>
    </div>
  );
};
```

### Step 2: Interview Question Component
```tsx
import { useState, useEffect } from 'react';
import axios from 'axios';

const InterviewQuestion = ({ 
  sessionId, 
  onComplete 
}: { 
  sessionId: number; 
  onComplete: () => void;
}) => {
  const [currentQuestion, setCurrentQuestion] = useState<Question | null>(null);
  const [answer, setAnswer] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadNextQuestion();
  }, [sessionId]);

  const loadNextQuestion = async () => {
    try {
      const response = await axios.get(
        `/api/interviews/sessions/${sessionId}/next-question`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      
      const question = response.data.result;
      
      if (question.questionId === -1) {
        // All questions completed
        onComplete();
      } else {
        setCurrentQuestion(question);
        setAnswer('');
      }
    } catch (error) {
      console.error('Failed to load question:', error);
    }
  };

  const handleSubmitAnswer = async () => {
    if (!currentQuestion || !answer.trim()) return;
    
    setLoading(true);
    try {
      await axios.post(
        `/api/interviews/sessions/${sessionId}/questions/${currentQuestion.questionId}/answer`,
        { answer },
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      
      // Load next question
      await loadNextQuestion();
    } catch (error) {
      console.error('Failed to submit answer:', error);
      alert('Failed to submit answer. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!currentQuestion) {
    return <div>Loading question...</div>;
  }

  return (
    <div className="interview-question">
      <div className="question-header">
        <h3>Question {currentQuestion.questionNumber} of 10</h3>
        {currentQuestion.isLastQuestion && <span className="badge">Final Question</span>}
      </div>
      
      <div className="question-content">
        <p>{currentQuestion.question}</p>
      </div>
      
      <textarea
        value={answer}
        onChange={(e) => setAnswer(e.target.value)}
        placeholder="Type your answer here..."
        rows={6}
        style={{ width: '100%', padding: '10px', marginTop: '20px' }}
      />
      
      <div className="actions">
        <button 
          onClick={handleSubmitAnswer}
          disabled={!answer.trim() || loading}
        >
          {loading ? 'Submitting...' : 'Submit Answer'}
        </button>
      </div>
    </div>
  );
};
```

### Step 3: Complete Interview Component
```tsx
import { useState } from 'react';
import axios from 'axios';

const CompleteInterview = ({ 
  sessionId,
  onCompleted 
}: { 
  sessionId: number;
  onCompleted: (report: any) => void;
}) => {
  const [loading, setLoading] = useState(false);

  const handleComplete = async () => {
    setLoading(true);
    try {
      const response = await axios.post(
        `/api/interviews/sessions/${sessionId}/complete`,
        {},
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      
      onCompleted(response.data.result);
    } catch (error) {
      console.error('Failed to complete interview:', error);
      alert('Failed to generate report. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="complete-interview">
      <h2>All Questions Completed!</h2>
      <p>Click below to generate your final interview report with scores and feedback.</p>
      <button onClick={handleComplete} disabled={loading}>
        {loading ? 'Generating Report...' : 'Generate Final Report'}
      </button>
    </div>
  );
};
```

### Step 4: Interview Report Component
```tsx
import ReactMarkdown from 'react-markdown';

const InterviewReport = ({ session }: { session: any }) => {
  return (
    <div className="interview-report">
      <h2>Interview Report</h2>
      
      <div className="summary">
        <div className="score-card">
          <h3>Average Score</h3>
          <div className="score">{session.averageScore.toFixed(1)} / 10</div>
        </div>
        
        <div className="metadata">
          <p><strong>Completed:</strong> {new Date(session.completedAt).toLocaleString()}</p>
          <p><strong>Questions Answered:</strong> {session.questions.length}</p>
        </div>
      </div>
      
      <div className="final-report">
        <h3>AI Evaluation</h3>
        <ReactMarkdown>{session.finalReport}</ReactMarkdown>
      </div>
      
      <div className="questions-review">
        <h3>Question-by-Question Review</h3>
        {session.questions.map((q: any) => (
          <div key={q.questionId} className="question-review">
            <h4>Q{q.questionNumber}: {q.question}</h4>
            
            <div className="answer-section">
              <strong>Your Answer:</strong>
              <p>{q.candidateAnswer}</p>
            </div>
            
            <div className="score-section">
              <span className={`score-badge ${getScoreClass(q.score)}`}>
                Score: {q.score}/10
              </span>
            </div>
            
            <div className="feedback-section">
              <strong>AI Feedback:</strong>
              <p>{q.feedback}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

const getScoreClass = (score: number) => {
  if (score >= 8) return 'excellent';
  if (score >= 6) return 'good';
  if (score >= 4) return 'average';
  return 'needs-improvement';
};
```

### Step 5: Main Interview Flow Component
```tsx
import { useState } from 'react';

const InterviewFlow = () => {
  const [stage, setStage] = useState<'start' | 'interview' | 'complete' | 'report'>('start');
  const [sessionId, setSessionId] = useState<number | null>(null);
  const [reportData, setReportData] = useState<any>(null);

  return (
    <div className="interview-flow">
      {stage === 'start' && (
        <StartInterview 
          onStarted={(id) => {
            setSessionId(id);
            setStage('interview');
          }}
        />
      )}
      
      {stage === 'interview' && sessionId && (
        <InterviewQuestion
          sessionId={sessionId}
          onComplete={() => setStage('complete')}
        />
      )}
      
      {stage === 'complete' && sessionId && (
        <CompleteInterview
          sessionId={sessionId}
          onCompleted={(report) => {
            setReportData(report);
            setStage('report');
          }}
        />
      )}
      
      {stage === 'report' && reportData && (
        <InterviewReport session={reportData} />
      )}
    </div>
  );
};

export default InterviewFlow;
```

### Step 6: Interview History Component
```tsx
import { useState, useEffect } from 'react';
import axios from 'axios';

const InterviewHistory = () => {
  const [sessions, setSessions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSessions();
  }, []);

  const loadSessions = async () => {
    try {
      const response = await axios.get('/api/interviews/sessions', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      setSessions(response.data.result);
    } catch (error) {
      console.error('Failed to load sessions:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading history...</div>;

  return (
    <div className="interview-history">
      <h2>Interview History</h2>
      {sessions.length === 0 ? (
        <p>No interview sessions yet.</p>
      ) : (
        <div className="sessions-list">
          {sessions.map((session) => (
            <div key={session.sessionId} className="session-card">
              <div className="session-header">
                <h3>Session #{session.sessionId}</h3>
                <span className={`status-badge ${session.status.toLowerCase()}`}>
                  {session.status}
                </span>
              </div>
              
              <div className="session-info">
                <p><strong>Date:</strong> {new Date(session.createdAt).toLocaleDateString()}</p>
                {session.averageScore && (
                  <p><strong>Score:</strong> {session.averageScore.toFixed(1)}/10</p>
                )}
              </div>
              
              <div className="session-preview">
                <p>{session.jobDescription.substring(0, 150)}...</p>
              </div>
              
              <button onClick={() => viewSession(session.sessionId)}>
                View Details
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
```

---

## CSS Styling Example

```css
.interview-flow {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.interview-question {
  background: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.score-badge.excellent { background: #4caf50; color: white; }
.score-badge.good { background: #2196f3; color: white; }
.score-badge.average { background: #ff9800; color: white; }
.score-badge.needs-improvement { background: #f44336; color: white; }

.question-review {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
}

.final-report {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin: 20px 0;
}
```

---

## Error Handling

### Error Codes
- `9000`: Interview session not found
- `9001`: Interview session already completed
- `9002`: Interview question not found
- `9003`: Forbidden (not your interview)
- `9004`: Question already answered
- `9005`: Interview session not ongoing

### Example Error Handler
```typescript
const handleApiError = (error: any) => {
  if (error.response) {
    const errorCode = error.response.data.code;
    
    switch (errorCode) {
      case 9000:
        alert('Interview session not found');
        break;
      case 9001:
        alert('This interview is already completed');
        break;
      case 9004:
        alert('This question has already been answered');
        break;
      default:
        alert(error.response.data.message || 'An error occurred');
    }
  }
};
```

---

## Tips for Best User Experience

1. **Auto-save answers**: Save draft answers to localStorage before submission
2. **Progress indicator**: Show question X of 10 prominently
3. **Timer (optional)**: Add a timer per question if desired
4. **Mobile responsive**: Ensure components work well on mobile
5. **Markdown support**: Use a markdown renderer for the final report
6. **Print/Export**: Add option to export report as PDF
7. **Real-time validation**: Check answer length (minimum words)
8. **Loading states**: Show clear loading indicators for AI processing

---

## Testing the Feature

1. **Start Interview**: Use a realistic job description (100+ words)
2. **Answer Questions**: Provide technical answers (50+ words recommended)
3. **View Feedback**: Check that scores and feedback are reasonable
4. **Complete Interview**: Verify final report generation
5. **View History**: Check that sessions are saved correctly

---

## Notes

- **Token Efficiency**: The AI only provides feedback, not corrections, to save tokens
- **Response Time**: Question generation takes 5-10 seconds, scoring takes 2-3 seconds
- **Gemini API**: Make sure `GOOGLE_API_KEY` is set in environment variables
- **Rate Limits**: Be mindful of API rate limits (add delays if needed)

---

## Database Schema

The interview data is stored in two tables:

**interview_session**
- session_id (PK)
- candidate_id (FK)
- job_description (TEXT)
- status (VARCHAR) - ONGOING/COMPLETED
- created_at (TIMESTAMP)
- completed_at (TIMESTAMP)
- final_report (TEXT)
- average_score (DOUBLE)

**interview_question**
- question_id (PK)
- session_id (FK)
- question_number (INT)
- question (TEXT)
- candidate_answer (TEXT)
- score (DOUBLE) - 0-10
- feedback (TEXT)
- asked_at (TIMESTAMP)
- answered_at (TIMESTAMP)

---

## Future Enhancements

- [ ] Video interview support (record answers)
- [ ] Voice-to-text for answers
- [ ] Real-time answer suggestions
- [ ] Interview analytics dashboard
- [ ] Share interview results with recruiters
- [ ] Custom question templates
- [ ] Multiple language support

---

For questions or issues, please contact the backend team or refer to the Swagger documentation at `/swagger-ui/index.html`.

