# Hướng Dẫn Tích Hợp AI Interview Chatbot - Frontend React

## Tổng Quan
Tài liệu này hướng dẫn chi tiết cách tích hợp tính năng AI Interview Chatbot vào React frontend. Chatbot sẽ phỏng vấn candidate dựa trên Job Description, chấm điểm real-time, và tạo báo cáo chi tiết.

## Mục Lục
1. [Luồng Hoạt Động (Flow)](#luồng-hoạt-động)
2. [API Endpoints](#api-endpoints)
3. [Hướng Dẫn Implement từng bước](#hướng-dẫn-implement)
4. [Error Handling](#xử-lý-lỗi)
5. [Best Practices](#best-practices)

---

## Luồng Hoạt Động

### 📊 Flow Tổng Quan

```
┌─────────────────────────────────────────────────────────────┐
│                    1. START INTERVIEW                        │
│  Candidate nhập Job Description → AI tạo 10 câu hỏi         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              2. ANSWER QUESTIONS (Lặp 10 lần)               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  a. Lấy câu hỏi tiếp theo (GET next-question)        │   │
│  │  b. Candidate trả lời                                │   │
│  │  c. Submit answer → AI chấm điểm + feedback          │   │
│  │  d. Tự động nhận câu hỏi tiếp theo                   │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   3. COMPLETE INTERVIEW                      │
│  AI tạo báo cáo tổng hợp → Hiển thị kết quả + điểm          │
└─────────────────────────────────────────────────────────────┘
```

### 🔄 Chi Tiết Flow Từng Bước

#### **Bước 1: Bắt Đầu Phỏng Vấn**
```
User Action: Nhập Job Description
    ↓
Frontend: POST /api/interviews/start
    ↓
Backend: 
  1. Lưu session vào DB (status = "ONGOING")
  2. Gọi Gemini AI để tạo 10 câu hỏi
  3. Lưu 10 câu hỏi vào DB
    ↓
Frontend: Nhận sessionId + danh sách 10 câu hỏi
    ↓
Hiển thị: Câu hỏi đầu tiên
```

#### **Bước 2: Trả Lời Câu Hỏi (Lặp 10 lần)**

**Option A: Sử dụng GET next-question (Khuyến nghị)**
```
Frontend: GET /api/interviews/sessions/{sessionId}/next-question
    ↓
Backend: Tìm câu hỏi đầu tiên chưa có answer
    ↓
Frontend: Hiển thị câu hỏi
    ↓
User: Nhập câu trả lời
    ↓
Frontend: POST /sessions/{sessionId}/questions/{questionId}/answer
    ↓
Backend:
  1. Gọi Gemini AI chấm điểm (0-10)
  2. Gemini AI tạo feedback
  3. Lưu answer + score + feedback vào DB
  4. Tự động tìm câu hỏi tiếp theo
    ↓
Frontend: Nhận câu hỏi tiếp theo trong response
    ↓
Lặp lại cho đến hết 10 câu
```

**Option B: Sử dụng local state (Không khuyến nghị)**
```
Frontend: Lưu danh sách 10 câu hỏi từ bước 1
    ↓
Hiển thị câu hỏi thứ N
    ↓
User: Trả lời
    ↓
Frontend: POST answer
    ↓
Frontend: Tự tăng index lên N+1
    ↓
Hiển thị câu hỏi tiếp theo

❌ Vấn đề: Không đồng bộ với server nếu có lỗi
```

#### **Bước 3: Hoàn Thành Phỏng Vấn**
```
Khi đã trả lời đủ 10 câu:
    ↓
Frontend: POST /api/interviews/sessions/{sessionId}/complete
    ↓
Backend:
  1. Tính điểm trung bình
  2. Gọi Gemini AI tạo báo cáo tổng hợp
  3. Cập nhật status = "COMPLETED"
    ↓
Frontend: Nhận full report + tất cả câu hỏi với điểm
    ↓
Hiển thị: Báo cáo chi tiết
```

---

## API Endpoints

### 🔑 Authentication
**Tất cả API đều yêu cầu:**
- Role: `CANDIDATE`
- Header: `Authorization: Bearer <jwt-token>`

### Base URL
```
http://localhost:8080/api/interviews
```

---

### 1️⃣ Bắt Đầu Phỏng Vấn

**POST** `/start`

**Mục đích:** Tạo session mới và generate 10 câu hỏi

**Request:**
```json
{
  "jobDescription": "We are looking for a Senior Java Developer with 5+ years experience..."
}
```

**Validation:**
- `jobDescription`: Không được null, không được blank

**Response Success (200):**
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
      }
      // ... 9 câu hỏi nữa
    ]
  }
}
```

**⏱ Thời gian xử lý:** 5-10 giây (do phải gọi Gemini AI)

**💡 Frontend nên làm gì:**
1. Hiển thị loading "Đang tạo câu hỏi..."
2. Lưu `sessionId` vào state
3. Có thể lưu danh sách questions vào state (optional)
4. Hiển thị câu hỏi đầu tiên

---

### 2️⃣ Lấy Câu Hỏi Tiếp Theo

**GET** `/sessions/{sessionId}/next-question`

**Mục đích:** Lấy câu hỏi tiếp theo chưa được trả lời

**Response khi còn câu hỏi:**
```json
{
  "code": 0,
  "message": "Next question retrieved successfully",
  "result": {
    "questionId": 2,
    "questionNumber": 2,
    "question": "What is dependency injection?",
    "isLastQuestion": false
  }
}
```

**Response khi hết câu hỏi:**
```json
{
  "code": 0,
  "message": "Next question retrieved successfully",
  "result": {
    "questionId": -1,
    "questionNumber": -1,
    "question": "All questions completed",
    "isLastQuestion": true
  }
}
```

**💡 Frontend nên làm gì:**
- Kiểm tra `questionId === -1` → chuyển sang bước Complete
- Hiển thị progress: "Câu {questionNumber}/10"

---

### 3️⃣ Trả Lời Câu Hỏi

**POST** `/sessions/{sessionId}/questions/{questionId}/answer`

**Mục đích:** Submit câu trả lời, AI chấm điểm và trả về câu hỏi tiếp theo

**Request:**
```json
{
  "answer": "@Component is the generic stereotype for any Spring-managed component..."
}
```

**Validation:**
- `answer`: Không được blank

**Response Success (200):**
```json
{
  "code": 0,
  "message": "Answer submitted successfully",
  "result": {
    "questionId": 3,
    "questionNumber": 3,
    "question": "Explain the SOLID principles",
    "isLastQuestion": false
  }
}
```

**⚠️ LƯU Ý QUAN TRỌNG:**
- Response trả về là **câu hỏi TIẾP THEO**, không phải câu hiện tại
- Để xem điểm của câu hiện tại, phải gọi API `GET /sessions/{sessionId}`

**⏱ Thời gian xử lý:** 2-3 giây (AI chấm điểm)

**💡 Frontend nên làm gì:**
1. Hiển thị loading "Đang chấm điểm..."
2. Sau khi nhận response, hiển thị câu hỏi tiếp theo
3. (Optional) Gọi thêm GET session để lấy điểm + feedback của câu vừa trả lời

---

### 4️⃣ Hoàn Thành Phỏng Vấn

**POST** `/sessions/{sessionId}/complete`

**Mục đích:** Tạo báo cáo tổng hợp và kết thúc session

**Request:** Không cần body

**Response Success (200):**
```json
{
  "code": 0,
  "message": "Interview completed successfully",
  "result": {
    "sessionId": 1,
    "candidateId": 5,
    "status": "COMPLETED",
    "createdAt": "2025-11-26T10:30:00",
    "completedAt": "2025-11-26T11:00:00",
    "averageScore": 7.8,
    "finalReport": "## Tổng Quan\n\nThí sinh thể hiện kiến thức vững về Java...\n\n## Điểm Mạnh\n- Hiểu rõ Spring Framework\n- Trả lời logic tốt\n\n## Cần Cải Thiện\n- Nên tìm hiểu thêm về Microservices\n\n## Khuyến Nghị: HIRE",
    "questions": [
      {
        "questionId": 1,
        "questionNumber": 1,
        "question": "Can you explain @Component?",
        "candidateAnswer": "@Component is...",
        "score": 8.5,
        "feedback": "Good explanation. You covered the key points well.",
        "askedAt": "2025-11-26T10:30:01",
        "answeredAt": "2025-11-26T10:32:15"
      }
      // ... 9 câu hỏi khác với điểm + feedback
    ]
  }
}
```

**⏱ Thời gian xử lý:** 5-8 giây (AI tạo báo cáo)

**💡 Frontend nên làm gì:**
1. Hiển thị loading "Đang tạo báo cáo..."
2. Render `finalReport` bằng Markdown
3. Hiển thị bảng điểm cho từng câu hỏi
4. Highlight điểm trung bình

---

### 5️⃣ Xem Chi Tiết Session

**GET** `/sessions/{sessionId}`

**Mục đích:** Lấy đầy đủ thông tin session (dùng để xem điểm từng câu)

**Response:** Giống như response của Complete Interview

**💡 Khi nào dùng:**
- Sau khi submit answer, muốn hiển thị điểm ngay lập tức
- Xem lại session cũ
- Refresh data

---

### 6️⃣ Xem Lịch Sử Phỏng Vấn

**GET** `/sessions`

**Mục đích:** Lấy tất cả session của candidate (sắp xếp mới nhất trước)

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
      "jobDescription": "Senior Java Developer...",
      "questions": [...]
    },
    {
      "sessionId": 2,
      "status": "ONGOING",
      "averageScore": null,
      "createdAt": "2025-11-25T10:00:00"
    }
  ]
}
```

**💡 Frontend nên làm gì:**
- Hiển thị danh sách card
- Phân biệt status: ONGOING (màu vàng), COMPLETED (màu xanh)
- Click vào card → xem chi tiết

---

## Hướng Dẫn Implement

### 📦 Cài Đặt Dependencies

```bash
npm install axios react-markdown
```

### 🎯 Chiến Lược Implement

**Có 2 cách implement:**

#### **Cách 1: Flow Đơn Giản (Khuyến nghị cho MVP)**
```
Start → Answer 10 câu liên tục → Complete → Report
```
- Không cho phép pause giữa chừng
- Đơn giản, dễ implement
- UX tốt hơn (focus vào trả lời)

#### **Cách 2: Flow Linh Hoạt**
```
Start → Answer một số câu → Đóng trình duyệt → 
Quay lại sau → Tiếp tục từ câu chưa trả lời
```
- Cho phép pause và resume
- Phức tạp hơn (cần handle state)
- Cần gọi GET next-question mỗi lần vào lại

**👉 Tôi khuyên dùng Cách 1 cho đơn giản**

---

### 🏗 Cấu Trúc Component

```
InterviewFlow (Main Component)
├── StartInterview (Bước 1)
├── InterviewQuestion (Bước 2)
├── CompleteInterview (Bước 3)
└── InterviewReport (Bước 4)

InterviewHistory (Separate Page)
```

---

### 📝 Code Implementation Chi Tiết

#### **1. API Service (Tạo file: `services/interviewApi.js`)**

```javascript
import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api/interviews';

// Lấy token từ localStorage hoặc Redux
const getAuthHeader = () => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
});

export const interviewApi = {
  // 1. Bắt đầu phỏng vấn
  startInterview: async (jobDescription) => {
    const response = await axios.post(
      `${BASE_URL}/start`,
      { jobDescription },
      { headers: getAuthHeader() }
    );
    return response.data.result;
  },

  // 2. Lấy câu hỏi tiếp theo
  getNextQuestion: async (sessionId) => {
    const response = await axios.get(
      `${BASE_URL}/sessions/${sessionId}/next-question`,
      { headers: getAuthHeader() }
    );
    return response.data.result;
  },

  // 3. Trả lời câu hỏi
  answerQuestion: async (sessionId, questionId, answer) => {
    const response = await axios.post(
      `${BASE_URL}/sessions/${sessionId}/questions/${questionId}/answer`,
      { answer },
      { headers: getAuthHeader() }
    );
    return response.data.result;
  },

  // 4. Hoàn thành phỏng vấn
  completeInterview: async (sessionId) => {
    const response = await axios.post(
      `${BASE_URL}/sessions/${sessionId}/complete`,
      {},
      { headers: getAuthHeader() }
    );
    return response.data.result;
  },

  // 5. Xem chi tiết session
  getSession: async (sessionId) => {
    const response = await axios.get(
      `${BASE_URL}/sessions/${sessionId}`,
      { headers: getAuthHeader() }
    );
    return response.data.result;
  },

  // 6. Xem lịch sử
  getAllSessions: async () => {
    const response = await axios.get(
      `${BASE_URL}/sessions`,
      { headers: getAuthHeader() }
    );
    return response.data.result;
  }
};
```

---

#### **2. Main Flow Component**

```jsx
import React, { useState } from 'react';
import StartInterview from './StartInterview';
import InterviewQuestion from './InterviewQuestion';
import CompleteInterview from './CompleteInterview';
import InterviewReport from './InterviewReport';

const InterviewFlow = () => {
  // State quản lý flow
  const [step, setStep] = useState('start'); // start | interview | complete | report
  const [sessionId, setSessionId] = useState(null);
  const [currentQuestion, setCurrentQuestion] = useState(null);
  const [reportData, setReportData] = useState(null);

  return (
    <div className="interview-container">
      {/* Bước 1: Nhập Job Description */}
      {step === 'start' && (
        <StartInterview
          onSuccess={(sessionId, firstQuestion) => {
            setSessionId(sessionId);
            setCurrentQuestion(firstQuestion);
            setStep('interview');
          }}
        />
      )}

      {/* Bước 2: Trả lời câu hỏi */}
      {step === 'interview' && (
        <InterviewQuestion
          sessionId={sessionId}
          currentQuestion={currentQuestion}
          onNextQuestion={(nextQuestion) => {
            // Nếu hết câu hỏi, chuyển sang Complete
            if (nextQuestion.questionId === -1) {
              setStep('complete');
            } else {
              setCurrentQuestion(nextQuestion);
            }
          }}
        />
      )}

      {/* Bước 3: Generate Report */}
      {step === 'complete' && (
        <CompleteInterview
          sessionId={sessionId}
          onReportReady={(report) => {
            setReportData(report);
            setStep('report');
          }}
        />
      )}

      {/* Bước 4: Hiển thị Report */}
      {step === 'report' && (
        <InterviewReport data={reportData} />
      )}
    </div>
  );
};

export default InterviewFlow;
```

---

#### **3. Start Interview Component**

```jsx
import React, { useState } from 'react';
import { interviewApi } from '../services/interviewApi';

const StartInterview = ({ onSuccess }) => {
  const [jobDescription, setJobDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleStart = async () => {
    // Validation
    if (!jobDescription.trim()) {
      setError('Vui lòng nhập Job Description');
      return;
    }

    if (jobDescription.length < 100) {
      setError('Job Description phải ít nhất 100 ký tự');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // Gọi API start interview
      const result = await interviewApi.startInterview(jobDescription);
      
      // result chứa: sessionId, questions[]
      // Lấy câu hỏi đầu tiên từ danh sách
      const firstQuestion = {
        questionId: result.questions[0].questionId,
        questionNumber: result.questions[0].questionNumber,
        question: result.questions[0].question,
        isLastQuestion: result.questions[0].questionNumber === 10
      };

      // Gọi callback để chuyển sang bước tiếp theo
      onSuccess(result.sessionId, firstQuestion);

    } catch (err) {
      console.error('Start interview error:', err);
      setError(err.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="start-interview">
      <h2>🤖 AI Interview Chatbot</h2>
      <p>Paste Job Description vào đây, AI sẽ tạo 10 câu hỏi phỏng vấn phù hợp</p>

      <textarea
        value={jobDescription}
        onChange={(e) => setJobDescription(e.target.value)}
        placeholder="Ví dụ: We are looking for a Senior Java Developer with 5+ years experience in Spring Boot, microservices architecture..."
        rows={12}
        disabled={loading}
        style={{
          width: '100%',
          padding: '15px',
          fontSize: '14px',
          borderRadius: '8px',
          border: '1px solid #ddd'
        }}
      />

      <div style={{ marginTop: '10px', color: '#666', fontSize: '13px' }}>
        {jobDescription.length} ký tự (tối thiểu 100)
      </div>

      {error && (
        <div style={{ color: 'red', marginTop: '10px' }}>
          ⚠️ {error}
        </div>
      )}

      <button
        onClick={handleStart}
        disabled={loading || !jobDescription.trim()}
        style={{
          marginTop: '20px',
          padding: '12px 30px',
          fontSize: '16px',
          backgroundColor: loading ? '#ccc' : '#4CAF50',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          cursor: loading ? 'not-allowed' : 'pointer'
        }}
      >
        {loading ? '⏳ Đang tạo câu hỏi... (5-10 giây)' : '🚀 Bắt Đầu Phỏng Vấn'}
      </button>
    </div>
  );
};

export default StartInterview;
```

---

#### **4. Interview Question Component**

```jsx
import React, { useState } from 'react';
import { interviewApi } from '../services/interviewApi';

const InterviewQuestion = ({ sessionId, currentQuestion, onNextQuestion }) => {
  const [answer, setAnswer] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmitAnswer = async () => {
    // Validation
    if (!answer.trim()) {
      setError('Vui lòng nhập câu trả lời');
      return;
    }

    if (answer.length < 50) {
      setError('Câu trả lời quá ngắn. Vui lòng trả lời chi tiết hơn (tối thiểu 50 ký tự)');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // Submit answer và nhận câu hỏi tiếp theo
      const nextQuestion = await interviewApi.answerQuestion(
        sessionId,
        currentQuestion.questionId,
        answer
      );

      // Reset answer field
      setAnswer('');

      // Gọi callback để update câu hỏi tiếp theo
      onNextQuestion(nextQuestion);

    } catch (err) {
      console.error('Submit answer error:', err);
      setError(err.response?.data?.message || 'Có lỗi xảy ra. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="interview-question">
      {/* Progress Bar */}
      <div style={{ marginBottom: '20px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '5px' }}>
          <span style={{ fontWeight: 'bold' }}>
            Câu hỏi {currentQuestion.questionNumber}/10
          </span>
          {currentQuestion.isLastQuestion && (
            <span style={{ color: '#ff9800', fontWeight: 'bold' }}>
              🏁 Câu cuối cùng
            </span>
          )}
        </div>
        <div style={{ width: '100%', height: '8px', backgroundColor: '#e0e0e0', borderRadius: '4px' }}>
          <div
            style={{
              width: `${(currentQuestion.questionNumber / 10) * 100}%`,
              height: '100%',
              backgroundColor: '#4CAF50',
              borderRadius: '4px',
              transition: 'width 0.3s'
            }}
          />
        </div>
      </div>

      {/* Question */}
      <div style={{
        padding: '20px',
        backgroundColor: '#f5f5f5',
        borderRadius: '8px',
        marginBottom: '20px'
      }}>
        <h3 style={{ margin: '0 0 10px 0' }}>❓ Câu hỏi:</h3>
        <p style={{ fontSize: '16px', lineHeight: '1.6', margin: 0 }}>
          {currentQuestion.question}
        </p>
      </div>

      {/* Answer Input */}
      <div>
        <label style={{ display: 'block', marginBottom: '10px', fontWeight: 'bold' }}>
          ✍️ Câu trả lời của bạn:
        </label>
        <textarea
          value={answer}
          onChange={(e) => setAnswer(e.target.value)}
          placeholder="Nhập câu trả lời chi tiết của bạn tại đây..."
          rows={8}
          disabled={loading}
          style={{
            width: '100%',
            padding: '15px',
            fontSize: '14px',
            borderRadius: '8px',
            border: '1px solid #ddd',
            resize: 'vertical'
          }}
        />
        <div style={{ marginTop: '5px', color: '#666', fontSize: '13px' }}>
          {answer.length} ký tự (khuyến nghị tối thiểu 50)
        </div>
      </div>

      {error && (
        <div style={{ color: 'red', marginTop: '10px' }}>
          ⚠️ {error}
        </div>
      )}

      <button
        onClick={handleSubmitAnswer}
        disabled={loading || !answer.trim()}
        style={{
          marginTop: '20px',
          padding: '12px 30px',
          fontSize: '16px',
          backgroundColor: loading ? '#ccc' : '#2196F3',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          cursor: loading ? 'not-allowed' : 'pointer',
          width: '100%'
        }}
      >
        {loading ? '⏳ Đang chấm điểm... (2-3 giây)' : '➡️ Gửi Câu Trả Lời'}
      </button>

      {/* Tips */}
      <div style={{
        marginTop: '20px',
        padding: '15px',
        backgroundColor: '#e3f2fd',
        borderRadius: '8px',
        fontSize: '13px'
      }}>
        <strong>💡 Mẹo:</strong>
        <ul style={{ margin: '10px 0 0 0', paddingLeft: '20px' }}>
          <li>Trả lời chi tiết, có ví dụ cụ thể</li>
          <li>Giải thích tư duy, không chỉ nêu kết quả</li>
          <li>AI sẽ chấm điểm dựa trên độ chính xác và chi tiết</li>
        </ul>
      </div>
    </div>
  );
};

export default InterviewQuestion;
```

---

#### **5. Complete Interview Component**

```jsx
import React, { useState, useEffect } from 'react';
import { interviewApi } from '../services/interviewApi';

const CompleteInterview = ({ sessionId, onReportReady }) => {
  const [loading, setLoading] = useState(false);
  const [autoGenerate, setAutoGenerate] = useState(false);

  // Auto generate report khi component mount (tuỳ chọn)
  useEffect(() => {
    if (autoGenerate) {
      handleGenerateReport();
    }
  }, []);

  const handleGenerateReport = async () => {
    setLoading(true);

    try {
      const report = await interviewApi.completeInterview(sessionId);
      onReportReady(report);
    } catch (err) {
      console.error('Complete interview error:', err);
      alert('Có lỗi khi tạo báo cáo. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="complete-interview" style={{ textAlign: 'center', padding: '40px' }}>
      <div style={{ fontSize: '60px', marginBottom: '20px' }}>
        🎉
      </div>
      
      <h2>Chúc Mừng!</h2>
      <p style={{ fontSize: '16px', color: '#666' }}>
        Bạn đã hoàn thành tất cả 10 câu hỏi phỏng vấn
      </p>

      <div style={{
        padding: '20px',
        backgroundColor: '#f5f5f5',
        borderRadius: '8px',
        marginTop: '30px',
        marginBottom: '30px'
      }}>
        <p style={{ margin: 0 }}>
          AI sẽ phân tích câu trả lời của bạn và tạo một báo cáo chi tiết bao gồm:
        </p>
        <ul style={{ textAlign: 'left', display: 'inline-block', marginTop: '15px' }}>
          <li>📊 Điểm số cho từng câu hỏi</li>
          <li>💬 Nhận xét chi tiết từ AI</li>
          <li>⭐ Điểm trung bình tổng thể</li>
          <li>📝 Báo cáo tổng hợp (Điểm mạnh/yếu, khuyến nghị)</li>
        </ul>
      </div>

      <button
        onClick={handleGenerateReport}
        disabled={loading}
        style={{
          padding: '15px 40px',
          fontSize: '18px',
          backgroundColor: loading ? '#ccc' : '#4CAF50',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          cursor: loading ? 'not-allowed' : 'pointer',
          boxShadow: '0 2px 5px rgba(0,0,0,0.2)'
        }}
      >
        {loading ? '⏳ Đang tạo báo cáo... (5-8 giây)' : '📄 Tạo Báo Cáo'}
      </button>
    </div>
  );
};

export default CompleteInterview;
```

---

#### **6. Interview Report Component**

```jsx
import React from 'react';
import ReactMarkdown from 'react-markdown';

const InterviewReport = ({ data }) => {
  const getScoreColor = (score) => {
    if (score >= 8) return '#4CAF50'; // Xanh lá - Excellent
    if (score >= 6) return '#2196F3'; // Xanh dương - Good
    if (score >= 4) return '#FF9800'; // Cam - Average
    return '#F44336'; // Đỏ - Needs Improvement
  };

  const getScoreLabel = (score) => {
    if (score >= 8) return 'Xuất sắc';
    if (score >= 6) return 'Tốt';
    if (score >= 4) return 'Trung bình';
    return 'Cần cải thiện';
  };

  return (
    <div className="interview-report" style={{ maxWidth: '900px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ textAlign: 'center', marginBottom: '40px' }}>
        <h1>📊 Báo Cáo Phỏng Vấn</h1>
        <p style={{ color: '#666' }}>
          Session ID: {data.sessionId} | 
          Hoàn thành: {new Date(data.completedAt).toLocaleString('vi-VN')}
        </p>
      </div>

      {/* Điểm Trung Bình */}
      <div style={{
        textAlign: 'center',
        padding: '30px',
        backgroundColor: '#f5f5f5',
        borderRadius: '12px',
        marginBottom: '40px'
      }}>
        <div style={{ fontSize: '16px', color: '#666', marginBottom: '10px' }}>
          ĐIỂM TRUNG BÌNH
        </div>
        <div style={{
          fontSize: '48px',
          fontWeight: 'bold',
          color: getScoreColor(data.averageScore)
        }}>
          {data.averageScore.toFixed(1)} / 10
        </div>
        <div style={{
          marginTop: '10px',
          padding: '8px 20px',
          backgroundColor: getScoreColor(data.averageScore),
          color: 'white',
          borderRadius: '20px',
          display: 'inline-block'
        }}>
          {getScoreLabel(data.averageScore)}
        </div>
      </div>

      {/* AI Report */}
      <div style={{
        padding: '30px',
        backgroundColor: 'white',
        border: '2px solid #e0e0e0',
        borderRadius: '12px',
        marginBottom: '40px'
      }}>
        <h2 style={{ marginTop: 0 }}>🤖 Đánh Giá Từ AI</h2>
        <div style={{ lineHeight: '1.8', color: '#333' }}>
          <ReactMarkdown>{data.finalReport}</ReactMarkdown>
        </div>
      </div>

      {/* Chi Tiết Từng Câu Hỏi */}
      <h2>📝 Chi Tiết Từng Câu Hỏi</h2>
      {data.questions.map((q, index) => (
        <div
          key={q.questionId}
          style={{
            padding: '25px',
            backgroundColor: 'white',
            border: '1px solid #e0e0e0',
            borderRadius: '12px',
            marginBottom: '20px'
          }}
        >
          {/* Câu hỏi */}
          <div style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'flex-start',
            marginBottom: '15px'
          }}>
            <h3 style={{ margin: 0, flex: 1 }}>
              Câu {q.questionNumber}. {q.question}
            </h3>
            <div
              style={{
                marginLeft: '20px',
                padding: '8px 16px',
                backgroundColor: getScoreColor(q.score),
                color: 'white',
                borderRadius: '8px',
                fontWeight: 'bold',
                whiteSpace: 'nowrap'
              }}
            >
              {q.score}/10
            </div>
          </div>

          {/* Câu trả lời */}
          <div style={{ marginBottom: '15px' }}>
            <strong style={{ color: '#2196F3' }}>✍️ Câu trả lời của bạn:</strong>
            <p style={{
              marginTop: '8px',
              padding: '15px',
              backgroundColor: '#f9f9f9',
              borderRadius: '8px',
              lineHeight: '1.6'
            }}>
              {q.candidateAnswer}
            </p>
          </div>

          {/* Feedback */}
          <div>
            <strong style={{ color: '#4CAF50' }}>💬 Nhận xét từ AI:</strong>
            <p style={{
              marginTop: '8px',
              padding: '15px',
              backgroundColor: '#e8f5e9',
              borderRadius: '8px',
              lineHeight: '1.6',
              borderLeft: '4px solid #4CAF50'
            }}>
              {q.feedback}
            </p>
          </div>

          {/* Timestamp */}
          <div style={{ marginTop: '10px', fontSize: '12px', color: '#999' }}>
            Trả lời lúc: {new Date(q.answeredAt).toLocaleTimeString('vi-VN')}
          </div>
        </div>
      ))}

      {/* Action Buttons */}
      <div style={{
        marginTop: '40px',
        display: 'flex',
        gap: '15px',
        justifyContent: 'center'
      }}>
        <button
          onClick={() => window.print()}
          style={{
            padding: '12px 30px',
            fontSize: '16px',
            backgroundColor: '#2196F3',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer'
          }}
        >
          🖨️ In Báo Cáo
        </button>
        <button
          onClick={() => window.location.href = '/interviews'}
          style={{
            padding: '12px 30px',
            fontSize: '16px',
            backgroundColor: '#4CAF50',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer'
          }}
        >
          🏠 Về Trang Chủ
        </button>
      </div>
    </div>
  );
};

export default InterviewReport;
```

---

## Xử Lý Lỗi

### Error Codes

| Code | Message | Ý Nghĩa | Cách Xử Lý |
|------|---------|---------|------------|
| 9000 | Interview session not found | Session không tồn tại | Redirect về trang chủ |
| 9001 | Interview session already completed | Session đã hoàn thành | Hiển thị report thay vì cho answer |
| 9002 | Interview question not found | Question không tồn tại | Reload session |
| 9003 | Forbidden | Session không phải của user | Redirect về trang chủ |
| 9004 | Question already answered | Câu hỏi đã được trả lời | Skip sang câu tiếp theo |
| 9005 | Interview session not ongoing | Session không ở trạng thái ONGOING | Kiểm tra status |

### Error Handler Helper

```javascript
export const handleInterviewError = (error, navigate) => {
  if (!error.response) {
    alert('Lỗi kết nối. Vui lòng kiểm tra internet.');
    return;
  }

  const { code, message } = error.response.data;

  switch (code) {
    case 9000:
      alert('Phiên phỏng vấn không tồn tại');
      navigate('/interviews');
      break;
    
    case 9001:
      alert('Phiên phỏng vấn này đã hoàn thành');
      // Có thể redirect đến report
      break;
    
    case 9003:
      alert('Bạn không có quyền truy cập phiên phỏng vấn này');
      navigate('/interviews');
      break;
    
    case 9004:
      // Câu hỏi đã được trả lời, tự động lấy câu tiếp theo
      console.log('Question already answered, getting next question');
      break;
    
    default:
      alert(message || 'Có lỗi xảy ra. Vui lòng thử lại.');
  }
};
```

---

## Best Practices

### ✅ Nên Làm

1. **Loading States**
   ```jsx
   {loading && <div>Đang tải...</div>}
   ```
   - Luôn hiển thị loading khi gọi API
   - Disable buttons khi đang xử lý

2. **Validation**
   - Check độ dài câu trả lời (tối thiểu 50 ký tự)
   - Check jobDescription không rỗng

3. **Error Handling**
   - Wrap tất cả API calls trong try-catch
   - Hiển thị message lỗi rõ ràng

4. **Auto-save (Optional)**
   ```jsx
   useEffect(() => {
     localStorage.setItem('draft_answer', answer);
   }, [answer]);
   ```

5. **Progress Indicator**
   - Hiển thị "Câu X/10"
   - Progress bar trực quan

### ❌ Không Nên

1. ❌ Không cache danh sách câu hỏi trong state quá lâu
   - Luôn lấy từ server để đảm bảo đồng bộ

2. ❌ Không cho phép back/forward browser trong khi interview
   - Có thể mất state

3. ❌ Không gọi API quá nhanh liên tục
   - Có thể bị rate limit

4. ❌ Không submit câu trả lời rỗng hoặc quá ngắn
   - AI sẽ cho điểm thấp

---

## Testing Checklist

### Functional Testing

- [ ] **Start Interview**
  - Input JD hợp lệ → Nhận được 10 câu hỏi
  - Input JD ngắn → Hiển thị lỗi validation
  - Loading hiển thị đúng 5-10 giây

- [ ] **Answer Questions**
  - Submit answer hợp lệ → Nhận câu tiếp theo
  - Submit answer ngắn → Hiển thị lỗi
  - Progress bar update đúng

- [ ] **Complete Interview**
  - Sau câu 10 → Chuyển sang Complete screen
  - Generate report thành công
  - Report hiển thị đầy đủ: score, feedback, finalReport

- [ ] **Error Handling**
  - Mất internet → Hiển thị lỗi kết nối
  - Token hết hạn → Redirect đến login
  - Session không tồn tại → Hiển thị lỗi

### UX Testing

- [ ] Loading states rõ ràng
- [ ] Không bị lag khi typing
- [ ] Responsive trên mobile
- [ ] Markdown render đúng trong report

---

## FAQ

### Q1: Có thể pause giữa chừng không?
**A:** Có thể! Session sẽ được lưu trên server với status "ONGOING". 
- Đóng trình duyệt → Quay lại sau
- Gọi `GET /sessions/{sessionId}/next-question` để lấy câu chưa trả lời

### Q2: Làm sao biết đã hết câu hỏi?
**A:** Khi response của `answerQuestion` hoặc `getNextQuestion` trả về:
```json
{
  "questionId": -1,
  "isLastQuestion": true
}
```

### Q3: Có thể xem điểm ngay sau khi trả lời không?
**A:** Có 2 cách:
1. Gọi thêm `GET /sessions/{sessionId}` sau khi submit answer
2. Hoặc đợi đến Complete để xem tất cả điểm cùng lúc

### Q4: Candidate có thể làm lại phỏng vấn không?
**A:** Có! Gọi lại `POST /start` với JD mới → Tạo session mới

### Q5: Làm sao để test với JD ngắn?
**A:** Trong môi trường dev, có thể giảm validation `minLength` trong frontend. Backend vẫn sẽ generate câu hỏi.

---

## Performance Tips

### 🚀 Optimization

1. **Lazy Loading Components**
   ```jsx
   const InterviewReport = lazy(() => import('./InterviewReport'));
   ```

2. **Debounce Auto-save**
   ```jsx
   const debouncedSave = debounce((value) => {
     localStorage.setItem('draft', value);
   }, 500);
   ```

3. **Memoize Expensive Calculations**
   ```jsx
   const averageScore = useMemo(() => 
     questions.reduce((sum, q) => sum + q.score, 0) / questions.length,
     [questions]
   );
   ```

---

## Summary

### Flow Tóm Tắt

```
1. POST /start (với jobDescription)
   → Nhận sessionId + 10 questions
   
2. Lặp 10 lần:
   - Hiển thị câu hỏi thứ N
   - User nhập answer
   - POST /sessions/{sessionId}/questions/{questionId}/answer
   - Nhận câu hỏi tiếp theo trong response
   
3. Khi questionId = -1:
   - POST /sessions/{sessionId}/complete
   - Nhận full report với điểm + feedback
   
4. Render report với Markdown
```

### Key Points

- ✅ Luôn hiển thị loading states
- ✅ Validate input trước khi gọi API
- ✅ Handle errors gracefully
- ✅ Progress indicator rõ ràng
- ✅ Auto-save draft answers (optional)

---

## Liên Hệ Support

Nếu có vấn đề khi integrate:
1. Check console logs xem error code
2. Kiểm tra JWT token còn hạn không
3. Verify API endpoint đúng chưa
4. Contact backend team nếu cần

**Backend Team:**
- API Documentation: `/swagger-ui/index.html`
- Base URL: `http://localhost:8080`

---

Good luck với việc implement! 🚀

