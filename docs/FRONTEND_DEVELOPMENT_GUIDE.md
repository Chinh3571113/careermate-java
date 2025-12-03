# CareerMate Frontend Development Guide

## Candidate Application Flow - Next.js Implementation

This guide divides the complete candidate application flow into development phases with specific API endpoints and implementation details.

**Last Updated:** November 29, 2025

---

## 🆔 ID Reference Guide (IMPORTANT FOR FRONTEND)

This section explains **where each ID comes from** and **which endpoints auto-extract IDs from JWT**.

### ID Sources Summary

| ID Type | Where to Get | Notes |
|---------|--------------|-------|
| `userId` | Login response OR JWT claims | Account ID, always present |
| `recruiterId` | Login response OR JWT claims | Only for RECRUITER role |
| `candidateId` | Login response OR JWT claims | Only for CANDIDATE role |
| `jobApplyId` | From application list API response | The job application ID |
| `interviewId` | From interview API response OR from `getInterviewByJobApply` | The interview schedule ID |
| `jobPostingId` | From job posting list/detail API | The job posting ID |

### Endpoints That Auto-Extract ID from JWT (No ID Needed in URL)

These endpoints **automatically get recruiterId/candidateId from JWT token**:

```typescript
// ✅ RECRUITER - No recruiterId needed in URL
GET  /api/interviews/recruiter/upcoming      // Get my upcoming interviews
GET  /api/interviews/recruiter/pending       // Get my pending interviews  
GET  /api/interviews/recruiter/stats         // Get my interview statistics
GET  /api/calendar/recruiter/available-slots // Get my available time slots
GET  /api/calendar/recruiter/daily           // Get my daily calendar
GET  /api/calendar/recruiter/weekly          // Get my weekly calendar
GET  /api/calendar/recruiter/monthly         // Get my monthly calendar
GET  /api/calendar/recruiter/available-dates // Get my available dates
GET  /api/calendar/recruiters/working-hours  // Get my working hours
POST /api/calendar/recruiters/working-hours  // Set my working hours
GET  /api/job-apply/recruiter                // Get all my applications
GET  /api/job-apply/recruiter/filter         // Get filtered applications

// ✅ CANDIDATE - No candidateId needed in URL
GET  /api/interviews/candidate/upcoming      // Get my upcoming interviews
GET  /api/interviews/candidate/past          // Get my past interviews

// ✅ SCHEDULE INTERVIEW - recruiterId auto-filled from JWT
POST /api/job-applies/{jobApplyId}/schedule-interview
// Request body does NOT need createdByRecruiterId - it's auto-filled!
```

### Endpoints That Require IDs in URL/Body

```typescript
// These still need the ID from previous API responses:
GET  /api/job-applies/{jobApplyId}/interview  // Get interview by job application (CANDIDATE/RECRUITER)
PUT  /api/interviews/{interviewId}            // Update/Reschedule interview (RECRUITER only)
POST /api/interviews/{interviewId}/confirm    // Confirm interview (CANDIDATE only)
POST /api/interviews/{interviewId}/complete   // Complete interview (RECRUITER only)
POST /api/interviews/{interviewId}/cancel     // Cancel interview (RECRUITER only)
```

### Frontend Implementation Pattern

```typescript
// 1. On Login - Store IDs from response
const handleLogin = async (email: string, password: string) => {
  const response = await api.post('/api/auth/token', { email, password });
  const { accessToken, userId, recruiterId, candidateId, role, email } = response.data.result;
  
  // Store in localStorage or state management
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('userId', userId?.toString() || '');
  localStorage.setItem('recruiterId', recruiterId?.toString() || '');
  localStorage.setItem('candidateId', candidateId?.toString() || '');
  localStorage.setItem('role', role);
};

// 2. For JWT-based endpoints - Just include token, no ID needed
const getMyUpcomingInterviews = async () => {
  // Backend extracts recruiterId/candidateId from JWT automatically
  const response = await api.get('/api/interviews/recruiter/upcoming');
  return response.data; // { recruiterId: 123, count: 5, interviews: [...] }
};

// 3. For ID-required endpoints - Get ID from previous API response
const rescheduleInterview = async (jobApplyId: number) => {
  // Step 1: Get existing interview (uses jobApplyId from application list)
  const existingInterview = await api.get(`/api/job-applies/${jobApplyId}/interview`);
  const interviewId = existingInterview.data.id;
  
  // Step 2: Update it (uses interviewId from step 1)
  await api.put(`/api/interviews/${interviewId}`, {
    scheduledDate: newDate,
    // ... other fields
  });
};

// 4. Schedule new interview - NO recruiterId needed!
const scheduleInterview = async (jobApplyId: number, data: any) => {
  // createdByRecruiterId is auto-filled from JWT token
  await api.post(`/api/job-applies/${jobApplyId}/schedule-interview`, {
    scheduledDate: data.scheduledDate,
    interviewType: data.interviewType,
    durationMinutes: data.durationMinutes,
    // createdByRecruiterId: NOT NEEDED - auto-filled from JWT!
  });
};
```

### Reschedule Flow - Complete Example

```typescript
// When user clicks "Reschedule" on a candidate card
const handleReschedule = async (application: JobApplyResponse) => {
  const jobApplyId = application.id; // Get from application list API
  
  // Navigate to schedule page with jobApplyId
  router.push(`/recruiter/interviews/schedule?jobApplyId=${jobApplyId}&mode=reschedule`);
};

// On Schedule Page - Load existing interview
useEffect(() => {
  if (mode === 'reschedule' && jobApplyId) {
    // Fetch existing interview to pre-fill form
    const response = await api.get(`/api/job-applies/${jobApplyId}/interview`);
    const existingInterview = response.data;
    
    // Pre-fill form
    setFormData({
      scheduledDate: existingInterview.scheduledDate,
      interviewType: existingInterview.interviewType,
      // ... etc
    });
    
    // Store interviewId for update
    setInterviewId(existingInterview.id);
  }
}, [mode, jobApplyId]);

// On Submit
const handleSubmit = async () => {
  if (mode === 'reschedule' && interviewId) {
    // UPDATE existing interview
    await api.put(`/api/interviews/${interviewId}`, formData);
  } else {
    // CREATE new interview (recruiterId auto-filled from JWT)
    await api.post(`/api/job-applies/${jobApplyId}/schedule-interview`, formData);
  }
};
```

---

## 📊 Backend Status Overview

| Feature | Status | Notes |
|---------|--------|-------|
| Job Applications CRUD | ✅ Ready | Full CRUD with status tracking |
| Status Transitions | ✅ Ready | Validated transitions with auto-withdraw on hire |
| Interview Scheduling | ✅ Ready | Full lifecycle with conflict detection |
| Interview Reminders | ✅ Ready | 24h and 2h automated notifications |
| Calendar/Working Hours | ✅ Ready | Daily/Weekly/Monthly views |
| **Interview Direct Update** | ✅ Ready | `PUT /api/interviews/{id}` for rescheduling after negotiation |
| **Get Interview by JobApply** | ✅ Ready | `GET /api/job-applies/{id}/interview` |
| Contact Visibility | ✅ Ready | Shows only when status >= APPROVED |
| Company Reviews | ✅ Ready | Eligibility-based with multiple review types |
| Notifications | ✅ Ready | Real-time SSE + Kafka |
| Employment Tracking | ✅ Ready | Start/terminate with verification |
| Auto-Withdraw on Hire | ✅ Ready | Withdraws other pending applications |
| **JWT with User IDs** | ✅ Ready | userId, recruiterId, candidateId embedded in JWT |
| **Auto-ID Endpoints** | ✅ Ready | Endpoints that extract ID from JWT token |
| **AI Interview Practice** | ✅ Ready | Gemini-powered mock interviews for candidates |

---

## 🔐 Authentication & JWT Structure

### Login Response
```typescript
interface AuthenticationResponse {
  accessToken: string;
  authenticated: boolean;
  expiresIn: number;          // Token validity in seconds
  tokenType: 'Bearer';
  userId: number;             // Account ID - ALWAYS present
  recruiterId: number | null; // Recruiter profile ID (null if not recruiter)
  candidateId: number | null; // Candidate profile ID (null if not candidate)
  email: string;
  role: 'ADMIN' | 'RECRUITER' | 'CANDIDATE';
}
```

### JWT Token Claims (Decoded)
```typescript
// The JWT token contains these claims:
interface JWTClaims {
  sub: string;           // Email
  iss: string;           // "careermate.com"
  exp: number;           // Expiration timestamp
  iat: number;           // Issued at timestamp
  jti: string;           // Unique token ID
  fullname: string;      // User's full name
  scope: string;         // Roles and permissions
  userId: number;        // Account ID - ALWAYS included
  recruiterId?: number;  // Included if user is a recruiter
  candidateId?: number;  // Included if user is a candidate
}
```

### Frontend Storage Recommendation
```typescript
// After successful login, store the response
const handleLogin = async (email: string, password: string) => {
  const response = await api.post('/api/auth/token', { email, password });
  
  // Store in localStorage or secure cookie
  localStorage.setItem('accessToken', response.data.accessToken);
  localStorage.setItem('userId', response.data.userId?.toString() || '');
  localStorage.setItem('recruiterId', response.data.recruiterId?.toString() || '');
  localStorage.setItem('candidateId', response.data.candidateId?.toString() || '');
  localStorage.setItem('role', response.data.role);
  localStorage.setItem('email', response.data.email);
};
```

> **IMPORTANT**: IDs are now embedded in JWT claims. Most endpoints that require `recruiterId` or `candidateId` now have JWT-based alternatives that extract the ID automatically from the token.

---

## 🔑 Status Enums Reference

### Application Status (`StatusJobApply`)
```typescript
enum StatusJobApply {
  SUBMITTED = 'SUBMITTED',           // Initial submission
  REVIEWING = 'REVIEWING',           // Under review
  INTERVIEW_SCHEDULED = 'INTERVIEW_SCHEDULED', // Interview set
  INTERVIEWED = 'INTERVIEWED',       // Interview done
  APPROVED = 'APPROVED',             // Approved (contact visible)
  ACCEPTED = 'ACCEPTED',             // Hired (legacy - use WORKING)
  WORKING = 'WORKING',               // Currently employed
  PROBATION_FAILED = 'PROBATION_FAILED', // Failed probation
  TERMINATED = 'TERMINATED',         // Employment ended
  REJECTED = 'REJECTED',             // Rejected
  BANNED = 'BANNED',                 // Banned
  NO_RESPONSE = 'NO_RESPONSE',       // No company response 7+ days
  WITHDRAWN = 'WITHDRAWN'            // Candidate withdrew
}
```

### Interview Status
```typescript
enum InterviewStatus {
  SCHEDULED = 'SCHEDULED',
  CONFIRMED = 'CONFIRMED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  NO_SHOW = 'NO_SHOW',
  RESCHEDULED = 'RESCHEDULED'
}
```

### Interview Outcome
```typescript
enum InterviewOutcome {
  PASS = 'PASS',
  FAIL = 'FAIL',
  PENDING = 'PENDING',
  NEEDS_SECOND_ROUND = 'NEEDS_SECOND_ROUND'
}
```

### Interview Type
```typescript
enum InterviewType {
  IN_PERSON = 'IN_PERSON',           // In-person at office
  VIDEO_CALL = 'VIDEO_CALL',         // Zoom, Teams, Google Meet
  PHONE = 'PHONE',                   // Phone interview
  ONLINE_ASSESSMENT = 'ONLINE_ASSESSMENT' // Online test
}
```

### Review Types
```typescript
enum ReviewType {
  APPLICATION_EXPERIENCE = 'APPLICATION_EXPERIENCE', // 7+ days applied, no response/rejected
  INTERVIEW_EXPERIENCE = 'INTERVIEW_EXPERIENCE',     // Completed interview
  WORK_EXPERIENCE = 'WORK_EXPERIENCE'                // 30+ days employed
}
```

---

# 📦 PHASE 1: Job Application Submission

## Overview
Candidate browses jobs, views details, and submits applications.

## API Endpoints

### 1.1 Browse Job Postings
```
GET /api/job-postings
Query: keyword, location, workModel, salaryRange, page, size
```

### 1.2 Get Job Details
```
GET /api/job-postings/{id}
```

### 1.3 Submit Application
```
POST /api/job-apply
Body: {
  jobPostingId: number,
  candidateId: number,
  cvFilePath: string,      // Required
  fullName: string,        // Required
  phoneNumber: string,     // Required
  preferredWorkLocation: string, // Required
  coverLetter?: string
}
Response: JobApplyResponse (status: SUBMITTED)
```

### 1.4 Track My Applications
```
GET /api/job-apply/candidate/{candidateId}/filter
Query: status?, page, size
```

### 1.5 Get Applications for Recruiter (Auto-ID from JWT)
```
GET /api/job-apply/recruiter
Auth: Bearer token (recruiterId extracted from JWT)
Response: List<JobApplyResponse>

GET /api/job-apply/recruiter/filter
Query: status?, page, size
Auth: Bearer token (recruiterId extracted from JWT)
```

## Frontend Pages

### `/jobs` - Job Listings
- Search/filter jobs
- Pagination
- Save job functionality

### `/jobs/[id]` - Job Detail
- Full job description
- Company info
- Apply button (checks if already applied)

### `/applications` - My Applications
- List with status badges
- Filter by status
- Click to view details

## Implementation Notes

```typescript
// Types
interface JobApplyRequest {
  jobPostingId: number;
  candidateId: number;
  cvFilePath: string;
  fullName: string;
  phoneNumber: string;
  preferredWorkLocation: string;
  coverLetter?: string;
}

interface JobApplyResponse {
  id: number;
  jobPostingId: number;
  jobTitle: string;
  jobDescription: string;
  expirationDate: string;       // LocalDate format: "YYYY-MM-DD"
  candidateId: number;
  cvFilePath: string;
  fullName: string;
  phoneNumber: string;
  preferredWorkLocation: string;
  coverLetter: string;
  status: StatusJobApply;
  createAt: string;             // LocalDateTime format
  // Contact info - only when status >= APPROVED
  companyName?: string;
  companyEmail?: string;
  recruiterPhone?: string;
  companyAddress?: string;
  contactPerson?: string;
}
```

---

# 📦 PHASE 2: Recruiter Reviews & Interview Scheduling

## Overview
Recruiter reviews applications, changes status, and schedules interviews.

## API Endpoints

### 2.1 Update Application Status (Recruiter)
```
PUT /api/job-apply/{id}
Body: "REVIEWING" | "INTERVIEW_SCHEDULED" | "APPROVED" | "REJECTED"
```

### 2.2 Schedule Interview
```
POST /api/job-applies/{jobApplyId}/schedule-interview
Body: {
  scheduledDate: string,    // ISO DateTime, must be future
  durationMinutes?: number, // Default: 60
  interviewType: 'IN_PERSON' | 'VIDEO_CALL' | 'PHONE' | 'ONLINE_ASSESSMENT',
  location?: string,
  interviewerName?: string,
  interviewerEmail?: string,
  interviewerPhone?: string,
  preparationNotes?: string,
  meetingLink?: string,
  interviewRound?: number,  // Default: 1
  createdByRecruiterId?: number  // Optional - auto-filled from JWT if not provided
}
```

> **Note**: `createdByRecruiterId` is now **optional**. If not provided, the backend automatically extracts the recruiter ID from the JWT token.

### 2.3 Check Scheduling Conflicts
```
POST /api/calendar/check-conflict
Body: {
  recruiterId: number,
  candidateId: number,
  proposedStartTime: string,
  durationMinutes: number
}
Response: {
  hasConflict: boolean,
  conflictReason?: string,
  conflicts: [{
    conflictType: 'INTERVIEW_OVERLAP' | 'TIME_OFF' | 'OUTSIDE_WORKING_HOURS' | 'MAX_INTERVIEWS_REACHED',
    description: string
  }]
}
```

### 2.4 Get Available Time Slots (Path Parameter)
```
GET /api/calendar/recruiters/{recruiterId}/available-slots
Query: date (YYYY-MM-DD), durationMinutes
Response: {
  recruiterId: number,
  date: string,
  durationMinutes: number,
  availableSlots: string[], // Array of times like "09:00", "10:00"
  totalSlotsAvailable: number
}
```

### 2.5 Get Available Time Slots (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/calendar/recruiter/available-slots
Query: date (YYYY-MM-DD), durationMinutes
Auth: Bearer token (recruiterId extracted from JWT)
Response: Same as above
```

### 2.6 Get Available Dates (Path Parameter)
```
GET /api/calendar/recruiters/{recruiterId}/available-dates
Query: startDate, endDate, durationMinutes
```

### 2.7 Get Available Dates (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/calendar/recruiter/available-dates
Query: startDate, endDate, durationMinutes
Auth: Bearer token (recruiterId extracted from JWT)
```

### 2.8 Get Recruiter's Upcoming Interviews (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/interviews/recruiter/upcoming
Auth: Bearer token (recruiterId extracted from JWT)
Response: { recruiterId: number, count: number, interviews: InterviewScheduleResponse[] }
```

### 2.9 Get Recruiter's Pending Interviews (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/interviews/recruiter/pending
Auth: Bearer token (recruiterId extracted from JWT)
Response: { recruiterId: number, count: number, interviews: InterviewScheduleResponse[] }
```

### 2.10 Get Recruiter's Interview Statistics (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/interviews/recruiter/stats
Auth: Bearer token (recruiterId extracted from JWT)
Response: {
  recruiterId: number,
  total: number,
  scheduled: number,
  confirmed: number,
  completed: number,
  cancelled: number,
  noShow: number,
  rescheduled: number,
  upcoming: number
}
```

## Frontend Pages

### `/recruiter/applications` - Application Management
- List all applications for recruiter's jobs
- Status filter
- Actions: Review, Schedule Interview, Reject

### `/recruiter/schedule-interview` - Schedule Interview Modal/Page
- Date picker with available dates highlighted
- Time slot selector
- Interview type selection
- Meeting link input (for video calls)
- Preparation notes

### `/recruiter/dashboard` - Dashboard with Statistics
- Interview statistics from `/api/interviews/recruiter/stats`
- Upcoming interviews count
- Pending interviews requiring action

## Implementation Notes

```typescript
interface InterviewScheduleRequest {
  scheduledDate: string;
  durationMinutes?: number;
  interviewType: 'IN_PERSON' | 'VIDEO_CALL' | 'PHONE' | 'ONLINE_ASSESSMENT';
  location?: string;
  interviewerName?: string;
  interviewerEmail?: string;
  interviewerPhone?: string;
  preparationNotes?: string;
  meetingLink?: string;
  interviewRound?: number;
  createdByRecruiterId?: number; // Optional - auto-filled from JWT if not provided
}

// Get stats using JWT-based endpoint (no ID needed)
async function getRecruiterStats() {
  // JWT token in Authorization header, backend extracts recruiterId
  const response = await api.get('/api/interviews/recruiter/stats');
  return response.data;
  // Returns: { recruiterId, total, scheduled, confirmed, completed, ... }
}

// Schedule interview - no need to pass recruiterId, backend extracts from JWT
async function scheduleInterview(jobApplyId: number, request: InterviewScheduleRequest) {
  // No need for manual conflict check - use available-slots endpoint instead
  // which already filters out occupied time slots
  return api.post(`/api/job-applies/${jobApplyId}/schedule-interview`, request);
}
```

---

# 📦 PHASE 3: Candidate Interview Response

## Overview
Candidate receives notification, views interview details, confirms or withdraws.

## API Endpoints

### 3.1 Get Upcoming Interviews (Path Parameter)
```
GET /api/interviews/candidate/{candidateId}/upcoming
Response: { count: number, interviews: InterviewScheduleResponse[] }
```

### 3.2 Get Upcoming Interviews (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/interviews/candidate/upcoming
Auth: Bearer token (candidateId extracted from JWT)
Response: { candidateId: number, count: number, interviews: InterviewScheduleResponse[] }
```

### 3.3 Confirm Interview
```
POST /api/interviews/{interviewId}/confirm
// No body needed
```

### 3.4 Withdraw Application (Decline Interview)
```
PUT /api/job-apply/{id}
Body: "WITHDRAWN"
```

### 3.5 Request Reschedule (Contact Recruiter Directly)
> **Note**: There is no API endpoint for reschedule requests. Candidates should contact the recruiter directly using the `interviewerEmail` or `interviewerPhone` from the interview details. After negotiating a new time, the recruiter updates the interview using `PUT /api/interviews/{id}` (see Phase 7).

### 3.6 Get Past Interviews (Path Parameter)
```
GET /api/interviews/candidate/{candidateId}/past
Response: { count: number, interviews: InterviewScheduleResponse[] }
```

### 3.7 Get Past Interviews (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/interviews/candidate/past
Auth: Bearer token (candidateId extracted from JWT)
Response: { candidateId: number, count: number, interviews: InterviewScheduleResponse[] }
```

## Frontend Pages

### `/candidate/interviews` - My Interviews
- Upcoming interviews list
- Past interviews list
- Calendar view option

### `/candidate/interviews/[id]` - Interview Detail
- Full interview info
- Confirm button (if not confirmed)
- Contact info for rescheduling (email/phone to contact recruiter)
- Withdraw option
- Add to calendar button (Google, Outlook, iCal)

## Implementation Notes

```typescript
interface InterviewScheduleResponse {
  id: number;
  jobApplyId: number;
  interviewRound: number;
  scheduledDate: string;           // ISO DateTime
  durationMinutes: number;
  interviewType: InterviewType;    // 'IN_PERSON' | 'VIDEO_CALL' | 'PHONE' | 'ONLINE_ASSESSMENT'
  location?: string;
  interviewerName?: string;
  interviewerEmail?: string;
  interviewerPhone?: string;
  preparationNotes?: string;
  meetingLink?: string;
  status: InterviewStatus;
  candidateConfirmed: boolean;
  candidateConfirmedAt?: string;
  reminderSent24h: boolean;
  reminderSent2h: boolean;
  interviewCompletedAt?: string;
  interviewerNotes?: string;
  outcome?: InterviewOutcome;
  createdByRecruiterId: number;
  createdAt: string;
  updatedAt: string;
  // Computed fields
  expectedEndTime: string;
  hasInterviewTimePassed: boolean;
  isInterviewInProgress: boolean;
  hoursUntilInterview?: number;
}

// Calendar integration
function generateCalendarEvent(interview: InterviewScheduleResponse) {
  return {
    title: `Interview - ${interview.interviewType}`,
    start: new Date(interview.scheduledDate),
    end: new Date(interview.expectedEndTime),
    location: interview.location || interview.meetingLink,
    description: interview.preparationNotes
  };
}

// Preferred approach: Use JWT-based endpoint (no ID needed)
async function getMyUpcomingInterviews() {
  // Just include the JWT token in Authorization header
  // Backend extracts candidateId from token automatically
  const response = await api.get('/api/interviews/candidate/upcoming');
  return response.data; // { candidateId, count, interviews }
}
```

---

# 📦 PHASE 4: Candidate Calendar Interface

## Overview
Display interviews on calendar, show reminders, integrate with external calendars.

## API Endpoints

### 4.1 Get Candidate Calendar
```
GET /api/calendar/candidates/{candidateId}/calendar
Query: startDate, endDate (optional, defaults to now + 30 days)
Response: CandidateCalendarResponse
```

### 4.2 Get Past Interviews (Path Parameter)
```
GET /api/interviews/candidate/{candidateId}/past
Response: { count: number, interviews: InterviewScheduleResponse[] }
```

### 4.3 Get Past Interviews (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/interviews/candidate/past
Auth: Bearer token (candidateId extracted from JWT)
Response: { candidateId: number, count: number, interviews: InterviewScheduleResponse[] }
```

## Frontend Pages

### `/candidate/calendar` - Interview Calendar
- Monthly view with interview markers
- Click date to see details
- Color coding by status (Scheduled, Confirmed, Completed)

## Implementation Notes

```typescript
// Use a calendar library like react-big-calendar or fullcalendar
import { Calendar, momentLocalizer } from 'react-big-calendar';

interface CalendarEvent {
  id: number;
  title: string;
  start: Date;
  end: Date;
  status: InterviewStatus;
  resource: InterviewScheduleResponse;
}

// Status colors
const statusColors = {
  SCHEDULED: '#FFA500',  // Orange
  CONFIRMED: '#4CAF50',  // Green
  COMPLETED: '#2196F3',  // Blue
  CANCELLED: '#9E9E9E',  // Gray
  NO_SHOW: '#F44336',    // Red
  RESCHEDULED: '#FF9800' // Amber
};
```

---

# 📦 PHASE 5: Interview Reminders & Notifications

## Overview
Real-time notifications for interview reminders, status changes, and updates.

## API Endpoints

### 5.1 Get Notifications
```
GET /api/notifications
Query: page, size
```

### 5.2 Get Unread Count
```
GET /api/notifications/unread-count
```

### 5.3 Mark as Read
```
PUT /api/notifications/{notificationId}/read
```

### 5.4 Mark All Read
```
PUT /api/notifications/mark-all-read
```

### 5.5 Real-time Stream (SSE)
```
GET /api/notifications/stream
Headers: Authorization: Bearer {token}
// Server-Sent Events connection
```

## Frontend Implementation

### Notification Bell Component
```typescript
// NotificationBell.tsx
import { useEffect, useState } from 'react';

function useNotifications() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    // Initial fetch
    fetchUnreadCount();
    
    // SSE connection for real-time
    const eventSource = new EventSource('/api/notifications/stream', {
      headers: { Authorization: `Bearer ${token}` }
    });
    
    eventSource.onmessage = (event) => {
      const notification = JSON.parse(event.data);
      setNotifications(prev => [notification, ...prev]);
      setUnreadCount(prev => prev + 1);
      
      // Show toast
      toast.info(notification.title);
    };
    
    return () => eventSource.close();
  }, []);

  return { notifications, unreadCount };
}
```

### Notification Types to Handle
- `APPLICATION_STATUS_CHANGED` - Application status update
- `INTERVIEW_SCHEDULED` - New interview scheduled
- `INTERVIEW_REMINDER_24_HOUR` - 24h before interview
- `INTERVIEW_REMINDER_2_HOUR` - 2h before interview
- `RESCHEDULE_REQUEST` - Reschedule requested
- `RESCHEDULE_APPROVED` / `RESCHEDULE_REJECTED`
- `APPLICATION_AUTO_WITHDRAWN` - Auto-withdrawn due to hire
- `APPLICATIONS_AUTO_WITHDRAWN` - Summary of withdrawals

---

# 📦 PHASE 6: Interview Completion & Outcomes

## Overview
Recruiter completes interview and decides: Hire, Reject, or Another Round.

## API Endpoints

### 6.1 Complete Interview
```
POST /api/interviews/{interviewId}/complete
Body: {
  outcome: 'PASS' | 'FAIL' | 'PENDING' | 'NEEDS_SECOND_ROUND',
  interviewerNotes?: string  // Max 2000 chars
}
```

### 6.2 Complete Interview Early
```
POST /api/interviews/{interviewId}/complete-early
Body: {
  outcome: 'PASS' | 'FAIL' | 'PENDING' | 'NEEDS_SECOND_ROUND',
  interviewerNotes?: string
}
```

### 6.3 Mark No-Show
```
POST /api/interviews/{interviewId}/no-show
Query: notes? (optional)
```

### 6.4 Cancel Interview
```
POST /api/interviews/{interviewId}/cancel
Query: reason (required)
```

### 6.5 Adjust Interview Duration
```
PATCH /api/interviews/{interviewId}/adjust-duration
Query: newDurationMinutes (required)
```

### 6.6 Get Interview Details
```
GET /api/interviews/{interviewId}
```

### 6.7 Schedule Another Round (if NEEDS_SECOND_ROUND)
```
POST /api/job-applies/{jobApplyId}/schedule-interview
Body: {
  ...same as before,
  interviewRound: 2  // Increment round
}
```

### 6.8 Update Application to Hired
```
PUT /api/job-apply/{id}
Body: "ACCEPTED"
// This triggers auto-withdraw of other pending applications!
```

### 6.9 Update Application to Rejected
```
PUT /api/job-apply/{id}
Body: "REJECTED"
```

## Frontend Pages

### `/recruiter/interviews/[id]/complete` - Complete Interview
- Outcome selection (Pass/Fail/Pending/Another Round)
- Notes input
- Decision buttons
- Warning for "Another Round" explaining reschedule

## Implementation Notes

```typescript
interface CompleteInterviewRequest {
  outcome: InterviewOutcome;
  interviewerNotes?: string;
}

// After completing with NEEDS_SECOND_ROUND
async function handleAnotherRound(interviewId: number, jobApplyId: number) {
  // 1. Complete current interview
  await api.post(`/api/interviews/${interviewId}/complete`, {
    outcome: 'NEEDS_SECOND_ROUND',
    interviewerNotes: 'Proceeding to round 2'
  });
  
  // 2. Navigate to schedule new interview
  router.push(`/recruiter/schedule-interview?jobApplyId=${jobApplyId}&round=2`);
}

// When hiring - warn about auto-withdraw
async function handleHire(applicationId: number) {
  const confirmed = await confirm(
    'This will automatically withdraw all other pending applications for this candidate. Continue?'
  );
  if (confirmed) {
    await api.put(`/api/job-apply/${applicationId}`, 'ACCEPTED');
  }
}
```

---

# 📦 PHASE 7: Interview Rescheduling (Direct Update)

## Overview
**Simplified rescheduling flow**: When a candidate needs to reschedule, they contact the recruiter directly (using interviewer email/phone from interview details). After negotiating a new time, the recruiter uses `PUT /api/interviews/{id}` to update the interview directly. No consent-request workflow needed.

## How Rescheduling Works

```
┌─────────────┐     Contact via email/phone     ┌─────────────┐
│  Candidate  │ ─────────────────────────────▶  │  Recruiter  │
└─────────────┘                                 └──────┬──────┘
                                                       │
                   They negotiate new time             │
                   ◀─────────────────────────         │
                                                       │
                                               PUT /api/interviews/{id}
                                                       │
                                                       ▼
                                               ┌─────────────┐
                                               │  Updated!   │
                                               └─────────────┘
```

1. **Candidate wants to reschedule** → Uses contact info from interview details (interviewerEmail, interviewerPhone)
2. **Direct communication** → Candidate and recruiter negotiate via email/phone
3. **Recruiter updates interview** → Uses `PUT /api/interviews/{id}` to change date/time
4. **Candidate gets notified** → Notification sent about the schedule change
5. **Candidate re-confirms** → If date was changed, `candidateConfirmed` is reset to false

## API Endpoints

### 7.1 Get Existing Interview by Job Application
```
GET /api/job-applies/{jobApplyId}/interview
Auth: Bearer token (CANDIDATE or RECRUITER role required)
Response: InterviewScheduleResponse (or 404 if no interview exists)
```

**Use Cases**: 
- **Recruiter**: When clicking "Reschedule" on a candidate with `INTERVIEW_SCHEDULED` status, fetch the existing interview to pre-populate the form.
- **Candidate**: When viewing their application with `INTERVIEW_SCHEDULED` status, fetch interview details to see schedule, location, interviewer contact info.

**Note**: Candidates can only access interviews for their own job applications. Attempting to access another candidate's interview returns `UNAUTHORIZED` error.

### 7.2 Update Interview (Direct Reschedule)
```
PUT /api/interviews/{interviewId}
Auth: Bearer token (RECRUITER role required)
Body: UpdateInterviewRequest
```

**Request Body**:
```typescript
interface UpdateInterviewRequest {
  scheduledDate?: string;        // ISO DateTime - if changed, triggers re-confirmation
  durationMinutes?: number;      // Positive integer
  interviewType?: InterviewType; // 'IN_PERSON' | 'VIDEO_CALL' | 'PHONE' | 'ONLINE_ASSESSMENT'
  location?: string;
  interviewerName?: string;
  interviewerEmail?: string;
  interviewerPhone?: string;
  preparationNotes?: string;
  meetingLink?: string;
  interviewRound?: number;
  updateReason?: string;         // Optional reason shown in notification to candidate
}
```

**Important Notes**:
- All fields are **optional** - only update what's provided
- If `scheduledDate` is changed:
  - Validates it's in the future
  - Checks for scheduling conflicts
  - Resets `candidateConfirmed` to `false`
  - Resets reminder flags
  - Sends notification to candidate about the change
- Cannot update interviews with status: `COMPLETED`, `CANCELLED`, `NO_SHOW`

**Response**: `InterviewScheduleResponse` with updated details

### 7.3 Get Interview by ID
```
GET /api/interviews/{interviewId}
Auth: Bearer token (RECRUITER or CANDIDATE role)
Response: InterviewScheduleResponse
```

## Frontend Implementation

### Reschedule Button on Application Card (Recruiter)
```typescript
function CandidateCard({ application }: { application: JobApplyResponse }) {
  const handleReschedule = async () => {
    router.push(`/recruiter/schedule-interview?jobApplyId=${application.id}&mode=reschedule`);
  };

  return (
    <Card>
      <h3>{application.fullName}</h3>
      <Badge>{application.status}</Badge>
      
      {application.status === 'INTERVIEW_SCHEDULED' && (
        <Button onClick={handleReschedule}>
          Reschedule Interview
        </Button>
      )}
    </Card>
  );
}
```

### Contact Info on Interview Detail (Candidate Side)
```typescript
function InterviewDetail({ interview }: { interview: InterviewScheduleResponse }) {
  return (
    <div>
      <h2>Interview Details</h2>
      <p>Date: {formatDateTime(interview.scheduledDate)}</p>
      <p>Duration: {interview.durationMinutes} minutes</p>
      <p>Type: {interview.interviewType}</p>
      
      {/* Contact info for rescheduling - candidate contacts recruiter directly */}
      <div className="contact-section">
        <h3>Need to Reschedule?</h3>
        <p>Contact the interviewer directly to negotiate a new time:</p>
        {interview.interviewerEmail && (
          <p>📧 Email: <a href={`mailto:${interview.interviewerEmail}`}>{interview.interviewerEmail}</a></p>
        )}
        {interview.interviewerPhone && (
          <p>📞 Phone: <a href={`tel:${interview.interviewerPhone}`}>{interview.interviewerPhone}</a></p>
        )}
      </div>
      
      {!interview.candidateConfirmed && (
        <Button onClick={() => confirmInterview(interview.id)}>
          Confirm Attendance
        </Button>
      )}
    </div>
  );
}
```

### Schedule/Reschedule Page (Recruiter)
```typescript
export default function ScheduleInterviewPage() {
  const searchParams = useSearchParams();
  const jobApplyId = searchParams.get('jobApplyId');
  const mode = searchParams.get('mode'); // 'new' or 'reschedule'
  
  const [existingInterview, setExistingInterview] = useState<InterviewScheduleResponse | null>(null);
  const [formData, setFormData] = useState<UpdateInterviewRequest>({});

  // Fetch existing interview if reschedule mode
  useEffect(() => {
    if (mode === 'reschedule' && jobApplyId) {
      fetchExistingInterview(parseInt(jobApplyId));
    }
  }, [mode, jobApplyId]);

  const fetchExistingInterview = async (jobApplyId: number) => {
    const response = await api.get(`/api/job-applies/${jobApplyId}/interview`);
    setExistingInterview(response.data);
    // Pre-fill form with existing data
    setFormData({
      scheduledDate: response.data.scheduledDate,
      durationMinutes: response.data.durationMinutes,
      interviewType: response.data.interviewType,
      location: response.data.location,
      // ... other fields
    });
  };

  const handleSubmit = async () => {
    if (mode === 'reschedule' && existingInterview) {
      await api.put(`/api/interviews/${existingInterview.id}`, {
        ...formData,
        updateReason: 'Interview rescheduled after discussion with candidate'
      });
      toast.success('Interview rescheduled successfully');
    } else {
      await api.post(`/api/job-applies/${jobApplyId}/schedule-interview`, formData);
      toast.success('Interview scheduled successfully');
    }
    router.push('/recruiter/applications');
  };

  return (
    <div>
      <h1>{mode === 'reschedule' ? 'Reschedule Interview' : 'Schedule Interview'}</h1>
      
      {existingInterview?.candidateConfirmed && (
        <Alert type="warning">
          ⚠️ Candidate has confirmed. Changing the date will require re-confirmation.
        </Alert>
      )}
      
      <InterviewForm data={formData} onChange={setFormData} onSubmit={handleSubmit} />
    </div>
  );
}
```

---

# 📦 PHASE 8: Employment Tracking

## Overview
Track hired candidates' employment status, handle termination.

## API Endpoints

### 8.1 Create Employment Record (when hired)
```
POST /api/employment-verifications/job-apply/{jobApplyId}
Auth: Bearer token (RECRUITER role required)
Body: {
  startDate: string  // YYYY-MM-DD
}
```

### 8.2 Get Employment Record
```
GET /api/employment-verifications/job-apply/{jobApplyId}
Auth: Bearer token (RECRUITER, CANDIDATE, or ADMIN role)
```

### 8.3 Terminate Employment (Recruiter)
```
POST /api/employment-verifications/job-apply/{jobApplyId}/terminate
Auth: Bearer token (RECRUITER role required)
Body: {
  terminationType: 'RESIGNATION' | 'FIRED_PERFORMANCE' | 'FIRED_MISCONDUCT' | 
                   'CONTRACT_END' | 'MUTUAL_AGREEMENT' | 'PROBATION_FAILED' | 
                   'COMPANY_CLOSURE' | 'LAYOFF',
  terminationDate: string,
  reason?: string
}
```

### 8.4 Verify Employment Status (Recruiter)
```
POST /api/employment-verifications/job-apply/{jobApplyId}/verify
Auth: Bearer token (RECRUITER role required)
```

### 8.5 Update Application Status (Candidate controls)
```
PUT /api/job-apply/{id}
Body: "WORKING" | "TERMINATED"
// Candidate can update their own status without recruiter approval
```

### 8.6 Get Active Employments (Recruiter)
```
GET /api/employment-verifications/recruiter/active
Auth: Bearer token (RECRUITER role required, ID from JWT)
```

### 8.7 Get Employments Needing Reminder (Admin)
```
GET /api/employment-verifications/admin/needing-reminder
Auth: Bearer token (ADMIN role required)
```

## Frontend Pages

### `/candidate/employment` - My Employment
- Current employment status
- Employment history
- Self-update status option

### `/recruiter/employees` - Employee Management
- Active employees list
- Employment duration
- Terminate button
- Verify employment button

## Implementation Notes

```typescript
interface EmploymentVerificationRequest {
  startDate: string;  // YYYY-MM-DD format
}

interface EmploymentTerminationRequest {
  terminationType: TerminationType;
  terminationDate: string;
  reason?: string;
}

interface EmploymentVerificationResponse {
  id: number;
  jobApplyId: number;
  startDate: string;
  endDate?: string;
  isActive: boolean;
  daysEmployed: number;
  isEligibleForWorkReview: boolean;  // 30+ days
  isCurrentlyEmployed: boolean;
}

// Candidate can change status independently
// This is a privacy-focused design
async function updateMyEmploymentStatus(applicationId: number, status: 'WORKING' | 'TERMINATED') {
  return api.put(`/api/job-apply/${applicationId}`, status);
}

// Recruiter creates employment record when hiring
async function createEmploymentRecord(jobApplyId: number, startDate: string) {
  return api.post(`/api/employment-verifications/job-apply/${jobApplyId}`, {
    startDate
  });
}
```

---

# 📦 PHASE 9: Company Reviews

## Overview
Candidates can review companies based on their application/interview/work experience.

## API Endpoints

### 9.1 Check Review Eligibility
```
GET /api/v1/reviews/eligibility
Query: candidateId, jobApplyId
Auth: Bearer token (CANDIDATE role required)
Response: ApiResponse<ReviewEligibilityResponse>
```

### 9.2 Submit Review
```
POST /api/v1/reviews
Query: candidateId
Auth: Bearer token (CANDIDATE role required)
Body: {
  jobApplyId: number,
  reviewType: ReviewType,
  reviewText: string,         // 20-2000 chars
  overallRating: number,      // 1-5
  // Optional aspect ratings (1-5)
  communicationRating?: number,
  responsivenessRating?: number,
  interviewProcessRating?: number,
  workCultureRating?: number,
  managementRating?: number,
  benefitsRating?: number,
  workLifeBalanceRating?: number,
  isAnonymous: boolean
}
Response: ApiResponse<CompanyReviewResponse> (code: 201)
```

### 9.3 Get Company Reviews (Public)
```
GET /api/v1/reviews/company/{recruiterId}
Query: reviewType?, page (default: 0), size (default: 10)
Response: ApiResponse<Page<CompanyReviewResponse>>
```

### 9.4 Get Company Rating (Public)
```
GET /api/v1/reviews/company/{recruiterId}/rating
Response: ApiResponse<{ averageRating: number, totalReviews: number }>
```

### 9.5 Get Company Statistics (Public)
```
GET /api/v1/reviews/company/{recruiterId}/statistics
Response: ApiResponse<CompanyReviewStatsResponse>
```

### 9.6 Get My Reviews
```
GET /api/v1/reviews/my-reviews
Query: candidateId, page? (default: 0), size? (default: 10)
Auth: Bearer token (CANDIDATE role required)
Response: ApiResponse<Page<CompanyReviewResponse>>
```

## Review Eligibility Rules

| Review Type | Requirement |
|-------------|-------------|
| APPLICATION_EXPERIENCE | Applied 7+ days ago AND (NO_RESPONSE OR REJECTED at application stage) |
| INTERVIEW_EXPERIENCE | Completed at least one interview (any outcome) |
| WORK_EXPERIENCE | Employed for 30+ days (current or former) |

## Frontend Pages

### `/applications/[id]/review` - Write Review
- Check eligibility first
- Show allowed review types
- Star rating component
- Aspect ratings (conditional based on type)
- Review text
- Anonymous toggle

### `/companies/[id]/reviews` - Company Reviews
- Overall rating display
- Rating breakdown
- Review list with filters
- Pagination

## Implementation Notes

```typescript
interface CompanyReviewRequest {
  jobApplyId: number;
  reviewType: ReviewType;
  reviewText: string;
  overallRating: number;
  communicationRating?: number;
  responsivenessRating?: number;
  interviewProcessRating?: number;
  workCultureRating?: number;
  managementRating?: number;
  benefitsRating?: number;
  workLifeBalanceRating?: number;
  isAnonymous: boolean;
}

interface ReviewEligibilityResponse {
  canReview: boolean;
  allowedReviewTypes: ReviewType[];
  alreadyReviewed: { [key: string]: boolean };
  qualification: 'APPLICANT' | 'INTERVIEWED' | 'EMPLOYED' | 'FORMER_EMPLOYEE';
  message: string;
}

// Check eligibility before showing review button
async function canReview(candidateId: number, jobApplyId: number) {
  const response = await api.get('/api/v1/reviews/eligibility', {
    params: { candidateId, jobApplyId }
  });
  return response.data.result.canReview;
}

// Show appropriate ratings based on review type
function getAspectRatings(reviewType: ReviewType) {
  switch (reviewType) {
    case 'APPLICATION_EXPERIENCE':
      return ['communicationRating', 'responsivenessRating'];
    case 'INTERVIEW_EXPERIENCE':
      return ['communicationRating', 'interviewProcessRating'];
    case 'WORK_EXPERIENCE':
      return ['workCultureRating', 'managementRating', 'benefitsRating', 'workLifeBalanceRating'];
  }
}
```

---

# 📦 PHASE 10: Contact Visibility

## Overview
Recruiter contact info only visible after application is approved.

## Implementation

The backend automatically handles this - `JobApplyResponse` only includes contact fields when status >= APPROVED.

```typescript
interface JobApplyResponse {
  // ... basic fields always present
  
  // These are only populated when status is APPROVED, ACCEPTED, or WORKING
  companyName?: string;
  companyEmail?: string;
  recruiterPhone?: string;
  companyAddress?: string;
  contactPerson?: string;
}
```

## Frontend Implementation

```typescript
function ApplicationDetail({ application }: { application: JobApplyResponse }) {
  const canSeeContact = ['APPROVED', 'ACCEPTED', 'WORKING'].includes(application.status);
  
  return (
    <div>
      {/* Basic info always visible */}
      <h2>{application.jobTitle}</h2>
      <p>Status: {application.status}</p>
      
      {/* Contact section - only when approved */}
      {canSeeContact && application.companyEmail && (
        <div className="contact-info">
          <h3>Company Contact</h3>
          <p>Company: {application.companyName}</p>
          <p>Contact Person: {application.contactPerson}</p>
          <p>Email: {application.companyEmail}</p>
          <p>Phone: {application.recruiterPhone}</p>
          <p>Address: {application.companyAddress}</p>
        </div>
      )}
      
      {!canSeeContact && (
        <div className="contact-locked">
          <p>Contact information will be available after your application is approved.</p>
        </div>
      )}
    </div>
  );
}
```

---

# 📦 PHASE 11: Recruiter Calendar Setup

## Overview
Recruiters configure their working hours and availability.

## API Endpoints

### 11.1 Set Working Hours (Single Day) - JWT Auto-ID
```
POST /api/calendar/recruiters/working-hours
Auth: Bearer token (recruiterId extracted from JWT)
Body: {
  dayOfWeek: 'MONDAY' | 'TUESDAY' | ... | 'SUNDAY',
  isWorkingDay: boolean,
  startTime?: string,        // "09:00"
  endTime?: string,          // "17:00"
  lunchBreakStart?: string,  // "12:00"
  lunchBreakEnd?: string,    // "13:00"
  bufferMinutesBetweenInterviews?: number, // Default: 15
  maxInterviewsPerDay?: number             // Default: 8
}
```

### 11.2 Set Working Hours (Batch) - JWT Auto-ID
```
POST /api/calendar/recruiters/working-hours/batch
Auth: Bearer token (recruiterId extracted from JWT)
Body: {
  workingHoursConfigurations: RecruiterWorkingHoursRequest[],
  replaceAll?: boolean  // Replace all existing or update only specified
}
```

### 11.3 Get Working Hours - JWT Auto-ID
```
GET /api/calendar/recruiters/working-hours
Auth: Bearer token (recruiterId extracted from JWT)
Response: RecruiterWorkingHoursResponse[] (7 days)
```

### 11.4 Get Daily Calendar (Path Parameter)
```
GET /api/calendar/recruiters/{recruiterId}/daily?date=YYYY-MM-DD
```

### 11.5 Get Daily Calendar (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/calendar/recruiter/daily?date=YYYY-MM-DD
Auth: Bearer token (recruiterId extracted from JWT)
```

### 11.6 Get Weekly Calendar (Path Parameter)
```
GET /api/calendar/recruiters/{recruiterId}/weekly?weekStartDate=YYYY-MM-DD
```

### 11.7 Get Weekly Calendar (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/calendar/recruiter/weekly?weekStartDate=YYYY-MM-DD
Auth: Bearer token (recruiterId extracted from JWT)
```

### 11.8 Get Monthly Calendar (Path Parameter)
```
GET /api/calendar/recruiters/{recruiterId}/monthly?year=2024&month=12
```

### 11.9 Get Monthly Calendar (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/calendar/recruiter/monthly?year=2024&month=12
Auth: Bearer token (recruiterId extracted from JWT)
```

### 11.10 Get Available Slots (Path Parameter)
```
GET /api/calendar/recruiters/{recruiterId}/available-slots?date=YYYY-MM-DD&durationMinutes=60
```

### 11.11 Get Available Slots (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/calendar/recruiter/available-slots?date=YYYY-MM-DD&durationMinutes=60
Auth: Bearer token (recruiterId extracted from JWT)
```

### 11.12 Get Available Dates (Path Parameter)
```
GET /api/calendar/recruiters/{recruiterId}/available-dates
Query: startDate, endDate, durationMinutes
```

### 11.13 Get Available Dates (JWT Auto-ID) ⭐ RECOMMENDED
```
GET /api/calendar/recruiter/available-dates
Query: startDate, endDate, durationMinutes
Auth: Bearer token (recruiterId extracted from JWT)
```

## Frontend Pages

### `/recruiter/settings/working-hours` - Working Hours Setup
- 7-day week view
- Toggle working/non-working days
- Set start/end times
- Lunch break configuration
- Buffer between interviews
- Max interviews per day

### `/recruiter/calendar` - Recruiter Calendar
- Monthly view with interviews
- Daily detail view
- Available slots highlighted
- Color coding for interview status

## Implementation Notes

```typescript
interface RecruiterWorkingHoursRequest {
  dayOfWeek: DayOfWeek;
  isWorkingDay: boolean;
  startTime?: string;
  endTime?: string;
  lunchBreakStart?: string;
  lunchBreakEnd?: string;
  bufferMinutesBetweenInterviews?: number;
  maxInterviewsPerDay?: number;
}

// Batch setup for initial configuration (uses JWT, no recruiterId needed)
async function setupWeeklySchedule(configs: RecruiterWorkingHoursRequest[]) {
  return api.post('/api/calendar/recruiters/working-hours/batch', {
    workingHoursConfigurations: configs,
    replaceAll: true
  });
}

// Get calendar using JWT-based endpoint (no recruiterId in URL)
async function getMyMonthlyCalendar(year: number, month: number) {
  return api.get('/api/calendar/recruiter/monthly', {
    params: { year, month }
  });
}

// Calendar grayout for non-working hours
function getBlockedTimeSlots(workingHours: RecruiterWorkingHoursResponse[]) {
  // See CALENDAR_UI_GRAYOUT_GUIDE.md for implementation details
}
```

---

# 🎯 Development Priority Order

## Sprint 1: Core Application Flow (Phases 1-2)
1. Job browsing and application submission
2. Application tracking dashboard
3. Recruiter application review
4. Basic status updates

## Sprint 2: Interview System (Phases 3-4)
1. Interview scheduling
2. Conflict detection
3. Candidate confirmation
4. Calendar views

## Sprint 3: Notifications & Reminders (Phase 5)
1. Notification bell component
2. Real-time SSE integration
3. Interview reminders display

## Sprint 4: Interview Outcomes (Phases 6-7)
1. Interview completion flow
2. Hire/Reject/Another Round
3. Rescheduling flow

## Sprint 5: Employment & Reviews (Phases 8-9)
1. Employment tracking
2. Review eligibility
3. Review submission
4. Company reviews display

## Sprint 6: Calendar & Polish (Phases 10-11)
1. Contact visibility
2. Recruiter working hours setup
3. Full calendar integration
4. Polish and testing

---

# 📚 Additional Resources

- `BATCH_WORKING_HOURS_INTEGRATION.md` - Working hours batch API details
- `CALENDAR_UI_GRAYOUT_GUIDE.md` - Calendar grayout implementation
- `IMPLEMENTATION_GAP_ANALYSIS.md` - Backend implementation status
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

# 🔔 Key Business Rules Summary

1. **Contact Visibility**: Recruiter contact only visible when status >= APPROVED
2. **Auto-Withdraw**: When hired, all other pending applications are auto-withdrawn
3. **Interview Reminders**: Automatic at 24h and 2h before interview
4. **Rescheduling**: Candidate contacts recruiter via email/phone, recruiter updates directly using `PUT /api/interviews/{id}`
5. **Review Eligibility**: Based on qualification level and time requirements
6. **Candidate Control**: Candidate can update their own employment status without recruiter approval
7. **Status Transitions**: Validated - only allowed transitions are permitted

---

# 🆕 JWT-Based API Endpoints Summary

The following endpoints automatically extract user IDs from JWT token claims. **No need to pass IDs in URL or store them in localStorage**.

## Recruiter Endpoints (JWT Auto-ID)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/interviews/recruiter/upcoming` | GET | Get recruiter's upcoming interviews |
| `/api/interviews/recruiter/pending` | GET | Get recruiter's pending interviews |
| `/api/interviews/recruiter/stats` | GET | Get recruiter's interview statistics |
| `/api/calendar/recruiter/available-slots` | GET | Get available time slots |
| `/api/calendar/recruiter/daily` | GET | Get daily calendar view |
| `/api/calendar/recruiter/weekly` | GET | Get weekly calendar view |
| `/api/calendar/recruiter/monthly` | GET | Get monthly calendar view |
| `/api/calendar/recruiter/available-dates` | GET | Get available dates |
| `/api/calendar/recruiters/working-hours` | GET/POST | Get/Set working hours |
| `/api/calendar/recruiters/working-hours/batch` | POST | Set batch working hours |
| `/api/job-apply/recruiter` | GET | Get all applications for recruiter |
| `/api/job-apply/recruiter/filter` | GET | Get filtered applications |

## Candidate Endpoints (JWT Auto-ID)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/interviews/candidate/upcoming` | GET | Get candidate's upcoming interviews |
| `/api/interviews/candidate/past` | GET | Get candidate's past interviews |

## Example Usage

```typescript
// OLD WAY (still works but not recommended):
const recruiterId = localStorage.getItem('recruiterId');
const response = await api.get(`/api/interviews/recruiter/${recruiterId}/upcoming`);

// NEW WAY (recommended - ID extracted from JWT):
const response = await api.get('/api/interviews/recruiter/upcoming');
// Returns: { recruiterId: 123, count: 5, interviews: [...] }
```

## Response Format for JWT-Based Endpoints

All JWT-based endpoints include the extracted ID in the response:

```typescript
// Recruiter endpoint response
{
  "recruiterId": 123,    // Extracted from JWT
  "count": 5,
  "interviews": [...]
}

// Candidate endpoint response  
{
  "candidateId": 456,    // Extracted from JWT
  "count": 3,
  "interviews": [...]
}

// Stats endpoint response
{
  "recruiterId": 123,
  "total": 50,
  "scheduled": 10,
  "confirmed": 8,
  "completed": 25,
  "cancelled": 5,
  "noShow": 2,
  "rescheduled": 0,
  "upcoming": 10
}
```

---

# 📦 PHASE 12: AI Interview Practice

## Overview

The **AI Interview Practice** feature allows candidates to practice mock interviews powered by **Gemini AI**. The system generates interview questions based on the job description, evaluates candidate answers in real-time, and provides comprehensive feedback with scores.

> **Note**: This is separate from the real interview scheduling system (Phase 3-7). This is a practice/quiz feature for candidates to prepare for actual interviews.

## How It Works

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        AI INTERVIEW PRACTICE FLOW                               │
└─────────────────────────────────────────────────────────────────────────────────┘

1. Start Session
   ┌──────────┐  POST /api/interviews/start   ┌──────────────┐
   │ Candidate │ ─────────────────────────────▶│ Create Session│
   └──────────┘  (with job description)        └──────┬───────┘
                                                      │
                                                      ▼
                                               ┌──────────────┐
                                               │ Gemini AI    │
                                               │ Generates    │
                                               │ Questions    │
                                               └──────┬───────┘
                                                      │
2. Answer Questions                                   ▼
   ┌──────────┐  POST .../questions/{id}/answer ┌─────────────┐
   │ Candidate │ ────────────────────────────────│ AI Evaluates│
   └──────────┘  (submit answer)                 │ Response    │
       │                                         └──────┬──────┘
       │                                                │
       │ GET .../next-question                          ▼
       │◀────────────────────────────────────────[Score + Feedback]
       │
3. Complete Session
   ┌──────────┐  POST .../complete              ┌─────────────┐
   │ Candidate │ ─────────────────────────────▶ │ Final Report│
   └──────────┘                                 │ Generated   │
                                                └─────────────┘
```

## TypeScript Interfaces

```typescript
// Request Types
interface StartInterviewRequest {
  jobDescription: string; // Required - description of the job to practice for
}

interface AnswerQuestionRequest {
  answer: string; // Required - candidate's answer to the question
}

// Response Types
interface InterviewQuestionResponse {
  questionId: number;
  questionNumber: number;
  question: string;
  candidateAnswer: string | null;
  score: number | null;         // 0-100, null if not yet answered
  feedback: string | null;      // AI feedback, null if not yet answered
  askedAt: string;              // ISO datetime
  answeredAt: string | null;    // ISO datetime, null if not yet answered
}

interface InterviewSessionResponse {
  sessionId: number;
  candidateId: number;
  jobDescription: string;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED';
  createdAt: string;            // ISO datetime
  completedAt: string | null;   // ISO datetime
  finalReport: string | null;   // AI-generated comprehensive feedback
  averageScore: number | null;  // Average of all question scores
  questions: InterviewQuestionResponse[];
}
```

## API Endpoints

### 12.1 Start New Interview Session

**POST** `/api/interviews/start`

Starts a new AI interview practice session. Gemini AI will generate relevant interview questions based on the provided job description.

**Request:**
```json
{
  "jobDescription": "Senior Java Developer position requiring 5+ years experience with Spring Boot, microservices architecture, and cloud deployment (AWS/Azure). Strong problem-solving skills and team collaboration required."
}
```

**Response:**
```json
{
  "sessionId": 1,
  "candidateId": 456,
  "jobDescription": "Senior Java Developer position...",
  "status": "IN_PROGRESS",
  "createdAt": "2025-11-29T10:00:00",
  "completedAt": null,
  "finalReport": null,
  "averageScore": null,
  "questions": [
    {
      "questionId": 1,
      "questionNumber": 1,
      "question": "Can you explain the key principles of microservices architecture and how you've implemented them in your previous projects?",
      "candidateAnswer": null,
      "score": null,
      "feedback": null,
      "askedAt": "2025-11-29T10:00:00",
      "answeredAt": null
    }
  ]
}
```

### 12.2 Submit Answer to Question

**POST** `/api/interviews/sessions/{sessionId}/questions/{questionId}/answer`

Submit an answer to a specific question. AI will evaluate the answer and provide score + feedback.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | number | The interview session ID |
| questionId | number | The question ID to answer |

**Request:**
```json
{
  "answer": "Microservices architecture is about breaking down a monolithic application into smaller, independently deployable services. Key principles include single responsibility, loose coupling, and high cohesion. In my previous project, I designed a payment processing system with separate services for authentication, order management, payment gateway integration, and notification. Each service had its own database and communicated via REST APIs and message queues for async operations."
}
```

**Response:**
```json
{
  "questionId": 1,
  "questionNumber": 1,
  "question": "Can you explain the key principles of microservices architecture...",
  "candidateAnswer": "Microservices architecture is about breaking down...",
  "score": 85.0,
  "feedback": "Excellent answer! You correctly identified the core principles of microservices. Your practical example demonstrates real-world application. To improve, you could mention service discovery, API gateway patterns, and containerization technologies like Docker/Kubernetes.",
  "askedAt": "2025-11-29T10:00:00",
  "answeredAt": "2025-11-29T10:05:00"
}
```

### 12.3 Get Next Question

**GET** `/api/interviews/sessions/{sessionId}/next-question`

Retrieves the next unanswered question in the session.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | number | The interview session ID |

**Response:**
```json
{
  "questionId": 2,
  "questionNumber": 2,
  "question": "How do you handle database transactions in a distributed microservices environment?",
  "candidateAnswer": null,
  "score": null,
  "feedback": null,
  "askedAt": "2025-11-29T10:00:00",
  "answeredAt": null
}
```

**Note:** Returns `404` if all questions have been answered.

### 12.4 Complete Interview Session

**POST** `/api/interviews/sessions/{sessionId}/complete`

Completes the interview session and generates a comprehensive final report with overall assessment.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | number | The interview session ID |

**Response:**
```json
{
  "sessionId": 1,
  "candidateId": 456,
  "jobDescription": "Senior Java Developer position...",
  "status": "COMPLETED",
  "createdAt": "2025-11-29T10:00:00",
  "completedAt": "2025-11-29T10:30:00",
  "finalReport": "## Interview Performance Summary\n\n**Overall Score: 82/100**\n\n### Strengths:\n- Strong understanding of microservices architecture\n- Good practical examples from real projects\n- Clear communication style\n\n### Areas for Improvement:\n- Deepen knowledge of distributed transaction patterns\n- Learn more about cloud-native technologies\n\n### Recommendations:\n- Study the Saga pattern and event sourcing\n- Get hands-on with Kubernetes\n- Practice system design questions",
  "averageScore": 82.0,
  "questions": [
    {
      "questionId": 1,
      "questionNumber": 1,
      "question": "...",
      "candidateAnswer": "...",
      "score": 85.0,
      "feedback": "...",
      "askedAt": "2025-11-29T10:00:00",
      "answeredAt": "2025-11-29T10:05:00"
    },
    {
      "questionId": 2,
      "questionNumber": 2,
      "question": "...",
      "candidateAnswer": "...",
      "score": 79.0,
      "feedback": "...",
      "askedAt": "2025-11-29T10:00:00",
      "answeredAt": "2025-11-29T10:15:00"
    }
  ]
}
```

### 12.5 Get Session Details

**GET** `/api/interviews/sessions/{sessionId}`

Retrieves complete details of an interview session including all questions and answers.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| sessionId | number | The interview session ID |

**Response:** Same as Complete Interview Session response.

### 12.6 Get All Sessions (Candidate)

**GET** `/api/interviews/sessions`

Retrieves all interview practice sessions for the authenticated candidate.

**Response:**
```json
[
  {
    "sessionId": 1,
    "candidateId": 456,
    "jobDescription": "Senior Java Developer...",
    "status": "COMPLETED",
    "createdAt": "2025-11-29T10:00:00",
    "completedAt": "2025-11-29T10:30:00",
    "finalReport": "...",
    "averageScore": 82.0,
    "questions": [...]
  },
  {
    "sessionId": 2,
    "candidateId": 456,
    "jobDescription": "Full Stack Engineer...",
    "status": "IN_PROGRESS",
    "createdAt": "2025-11-29T14:00:00",
    "completedAt": null,
    "finalReport": null,
    "averageScore": null,
    "questions": [...]
  }
]
```

## Frontend Implementation Example

### Interview Practice Page

```typescript
// pages/InterviewPractice.tsx
import React, { useState } from 'react';
import api from '../services/api';

interface Question {
  questionId: number;
  questionNumber: number;
  question: string;
  candidateAnswer: string | null;
  score: number | null;
  feedback: string | null;
}

interface Session {
  sessionId: number;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED';
  jobDescription: string;
  finalReport: string | null;
  averageScore: number | null;
  questions: Question[];
}

const InterviewPractice: React.FC = () => {
  const [session, setSession] = useState<Session | null>(null);
  const [currentQuestion, setCurrentQuestion] = useState<Question | null>(null);
  const [answer, setAnswer] = useState('');
  const [loading, setLoading] = useState(false);
  const [jobDescription, setJobDescription] = useState('');

  // Start new session
  const startSession = async () => {
    setLoading(true);
    try {
      const response = await api.post('/api/interviews/start', {
        jobDescription
      });
      setSession(response.data);
      setCurrentQuestion(response.data.questions[0]);
    } catch (error) {
      console.error('Failed to start session:', error);
    } finally {
      setLoading(false);
    }
  };

  // Submit answer
  const submitAnswer = async () => {
    if (!session || !currentQuestion) return;
    
    setLoading(true);
    try {
      // Submit answer
      const answerResponse = await api.post(
        `/api/interviews/sessions/${session.sessionId}/questions/${currentQuestion.questionId}/answer`,
        { answer }
      );
      
      // Update current question with feedback
      setCurrentQuestion({
        ...currentQuestion,
        candidateAnswer: answer,
        score: answerResponse.data.score,
        feedback: answerResponse.data.feedback
      });
      
      // Try to get next question
      try {
        const nextResponse = await api.get(
          `/api/interviews/sessions/${session.sessionId}/next-question`
        );
        setCurrentQuestion(nextResponse.data);
        setAnswer('');
      } catch (error: any) {
        if (error.response?.status === 404) {
          // No more questions - show complete button
          setCurrentQuestion(null);
        }
      }
    } catch (error) {
      console.error('Failed to submit answer:', error);
    } finally {
      setLoading(false);
    }
  };

  // Complete session
  const completeSession = async () => {
    if (!session) return;
    
    setLoading(true);
    try {
      const response = await api.post(
        `/api/interviews/sessions/${session.sessionId}/complete`
      );
      setSession(response.data);
    } catch (error) {
      console.error('Failed to complete session:', error);
    } finally {
      setLoading(false);
    }
  };

  // Render start form
  if (!session) {
    return (
      <div className="interview-practice">
        <h1>🎯 AI Interview Practice</h1>
        <p>Practice your interview skills with AI-powered mock interviews!</p>
        
        <div className="start-form">
          <label>Paste the job description you want to practice for:</label>
          <textarea
            value={jobDescription}
            onChange={(e) => setJobDescription(e.target.value)}
            placeholder="e.g., Senior Java Developer with 5+ years experience..."
            rows={6}
          />
          <button 
            onClick={startSession} 
            disabled={loading || !jobDescription.trim()}
          >
            {loading ? 'Starting...' : 'Start Practice Interview'}
          </button>
        </div>
      </div>
    );
  }

  // Render completed session
  if (session.status === 'COMPLETED') {
    return (
      <div className="interview-complete">
        <h1>🎉 Interview Complete!</h1>
        <div className="score-display">
          <h2>Your Score: {session.averageScore?.toFixed(0)}/100</h2>
        </div>
        <div className="final-report">
          <h3>AI Feedback Report</h3>
          <div dangerouslySetInnerHTML={{ __html: session.finalReport || '' }} />
        </div>
        <div className="questions-review">
          <h3>Question Review</h3>
          {session.questions.map((q) => (
            <div key={q.questionId} className="question-review-item">
              <p><strong>Q{q.questionNumber}:</strong> {q.question}</p>
              <p><em>Your answer:</em> {q.candidateAnswer}</p>
              <p><strong>Score:</strong> {q.score}/100</p>
              <p><strong>Feedback:</strong> {q.feedback}</p>
            </div>
          ))}
        </div>
        <button onClick={() => setSession(null)}>Start New Practice</button>
      </div>
    );
  }

  // Render question (in progress)
  return (
    <div className="interview-in-progress">
      <h1>🎤 Interview in Progress</h1>
      
      {currentQuestion ? (
        <div className="question-card">
          <h2>Question {currentQuestion.questionNumber}</h2>
          <p className="question-text">{currentQuestion.question}</p>
          
          {currentQuestion.score !== null ? (
            // Show feedback for answered question
            <div className="feedback-section">
              <p><strong>Score:</strong> {currentQuestion.score}/100</p>
              <p><strong>Feedback:</strong> {currentQuestion.feedback}</p>
              <button onClick={() => setCurrentQuestion(null)}>
                Next Question
              </button>
            </div>
          ) : (
            // Show answer form
            <div className="answer-form">
              <textarea
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                placeholder="Type your answer here..."
                rows={8}
              />
              <button 
                onClick={submitAnswer} 
                disabled={loading || !answer.trim()}
              >
                {loading ? 'Submitting...' : 'Submit Answer'}
              </button>
            </div>
          )}
        </div>
      ) : (
        // All questions answered
        <div className="complete-section">
          <p>You've answered all questions!</p>
          <button onClick={completeSession} disabled={loading}>
            {loading ? 'Generating Report...' : 'Get Final Report'}
          </button>
        </div>
      )}
    </div>
  );
};

export default InterviewPractice;
```

### Session History Page

```typescript
// pages/InterviewHistory.tsx
import React, { useEffect, useState } from 'react';
import api from '../services/api';

const InterviewHistory: React.FC = () => {
  const [sessions, setSessions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchSessions = async () => {
      try {
        const response = await api.get('/api/interviews/sessions');
        setSessions(response.data);
      } catch (error) {
        console.error('Failed to fetch sessions:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchSessions();
  }, []);

  if (loading) return <div>Loading...</div>;

  return (
    <div className="interview-history">
      <h1>📚 Practice History</h1>
      
      {sessions.length === 0 ? (
        <p>No practice sessions yet. Start your first one!</p>
      ) : (
        <div className="sessions-list">
          {sessions.map((session) => (
            <div key={session.sessionId} className="session-card">
              <div className="session-header">
                <span className={`status ${session.status.toLowerCase()}`}>
                  {session.status}
                </span>
                <span className="date">
                  {new Date(session.createdAt).toLocaleDateString()}
                </span>
              </div>
              <p className="job-desc">{session.jobDescription.slice(0, 100)}...</p>
              {session.averageScore && (
                <p className="score">Score: {session.averageScore.toFixed(0)}/100</p>
              )}
              <button onClick={() => window.location.href = `/practice/${session.sessionId}`}>
                View Details
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default InterviewHistory;
```

## Error Handling

| Status Code | Scenario | Action |
|-------------|----------|--------|
| 400 | Invalid request (empty job description/answer) | Show validation error |
| 401 | Not authenticated | Redirect to login |
| 403 | Accessing another candidate's session | Show access denied |
| 404 | Session/question not found, or no more questions | Handle gracefully |
| 500 | AI service error | Show retry option |

## Best Practices

1. **Save Answers Locally**: Auto-save answer drafts to localStorage in case of connection issues
2. **Loading States**: Show clear loading indicators during AI processing (can take 2-5 seconds)
3. **Progressive Display**: Stream the feedback if possible for better UX
4. **Session Recovery**: Allow resuming in-progress sessions
5. **Markdown Rendering**: The `finalReport` is in Markdown format - render it properly
