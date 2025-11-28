# CareerMate Frontend Development Guide

## Candidate Application Flow - Next.js Implementation

This guide divides the complete candidate application flow into development phases with specific API endpoints and implementation details.

**Last Updated:** November 28, 2025

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
GET  /api/job-applies/{jobApplyId}/interview  // Get interview by job application
PUT  /api/interviews/{interviewId}            // Update interview
POST /api/interviews/{interviewId}/confirm    // Confirm interview
POST /api/interviews/{interviewId}/complete   // Complete interview
POST /api/interviews/{interviewId}/cancel     // Cancel interview
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
| Rescheduling Flow | ✅ Ready | Request/respond with consent |
| **Direct Interview Update** | ✅ Ready | **NEW: PUT /api/interviews/{id} for direct rescheduling** |
| **Get Interview by JobApply** | ✅ Ready | **NEW: GET /api/job-applies/{id}/interview** |
| Contact Visibility | ✅ Ready | Shows only when status >= APPROVED |
| Company Reviews | ✅ Ready | Eligibility-based with multiple review types |
| Notifications | ✅ Ready | Real-time SSE + Kafka |
| Employment Tracking | ✅ Ready | Start/terminate with verification |
| Auto-Withdraw on Hire | ✅ Ready | Withdraws other pending applications |
| **JWT with User IDs** | ✅ Ready | **userId, recruiterId, candidateId embedded in JWT** |
| **Auto-ID Endpoints** | ✅ Ready | **Endpoints that extract ID from JWT token** |

---

## 🔐 Authentication & JWT Structure (UPDATED)

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
// The JWT token now contains these claims:
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

### 3.5 Request Reschedule
```
POST /api/interviews/{interviewId}/reschedule
Body: {
  newRequestedDate: string,   // ISO DateTime
  reason: string,             // 10-1000 chars
  requestedBy: 'CANDIDATE',
  requiresConsent?: boolean   // Default: true
}
```

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
- Request reschedule button
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

# 📦 PHASE 7: Rescheduling Flow

## Overview
Either party can request reschedule, other party responds.

## API Endpoints

### 7.1 Request Reschedule
```
POST /api/interviews/{interviewId}/reschedule
Body: {
  newRequestedDate: string,
  reason: string,
  requestedBy: 'RECRUITER' | 'CANDIDATE',
  requiresConsent: boolean
}
```

### 7.2 Respond to Reschedule Request
```
POST /api/interviews/reschedule-requests/{rescheduleRequestId}/respond
Query: approved=true|false, rejectionReason?
```

## Frontend Pages

### `/candidate/interviews/[id]/reschedule` - Request Reschedule
- Calendar date picker
- Reason input (required)
- Show available slots for new date

### `/recruiter/reschedule-requests` - Pending Requests
- List of pending reschedule requests
- Approve/Reject buttons
- View proposed time

## Implementation Notes

```typescript
interface RescheduleRequest {
  newRequestedDate: string;
  reason: string;
  requestedBy: 'RECRUITER' | 'CANDIDATE';
  requiresConsent?: boolean;
}

// Cannot reschedule within 2 hours of interview
function canRequestReschedule(interview: InterviewScheduleResponse): boolean {
  const hoursUntil = interview.hoursUntilInterview;
  return hoursUntil !== null && hoursUntil > 2;
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
4. **Rescheduling**: Cannot reschedule within 2 hours of interview
5. **Review Eligibility**: Based on qualification level and time requirements
6. **Candidate Control**: Candidate can update their own employment status without recruiter approval
7. **Status Transitions**: Validated - only allowed transitions are permitted

---

# 🆕 NEW: JWT-Based API Endpoints Summary

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

# 📦 PHASE 12: Interview Reschedule (Direct Update)

## Overview
Recruiter can directly reschedule an interview by updating existing interview details. Unlike the consent-based reschedule workflow (Phase 7), this allows immediate changes when the recruiter needs to modify the interview schedule.

## Use Cases
1. **Pre-fill Reschedule Form**: Get existing interview data to populate the schedule form
2. **Direct Update**: Immediately update interview date/time without candidate consent workflow
3. **Update Interview Details**: Modify location, interviewer, meeting link, etc.

## API Endpoints

### 12.1 Get Existing Interview by Job Application
```
GET /api/job-applies/{jobApplyId}/interview
Auth: Bearer token (RECRUITER role required)
Response: InterviewScheduleResponse (or 404 if no interview exists)
```

**Use Case**: When recruiter clicks "Reschedule" on a candidate with `INTERVIEW_SCHEDULED` status, fetch the existing interview to pre-populate the form.

### 12.2 Update Interview (Direct Reschedule)
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

### 12.3 Get Interview by ID (For verification)
```
GET /api/interviews/{interviewId}
Auth: Bearer token (RECRUITER or CANDIDATE role)
Response: InterviewScheduleResponse
```

## Frontend Implementation

### Reschedule Button on Application Card
```typescript
// On candidate card with status INTERVIEW_SCHEDULED
interface CandidateCardProps {
  application: JobApplyResponse;
}

function CandidateCard({ application }: CandidateCardProps) {
  const handleReschedule = async () => {
    // Navigate to schedule form with jobApplyId
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

### Schedule Interview Page with Reschedule Mode
```typescript
// /recruiter/schedule-interview/page.tsx
import { useSearchParams } from 'next/navigation';
import { useEffect, useState } from 'react';

export default function ScheduleInterviewPage() {
  const searchParams = useSearchParams();
  const jobApplyId = searchParams.get('jobApplyId');
  const mode = searchParams.get('mode'); // 'new' or 'reschedule'
  
  const [existingInterview, setExistingInterview] = useState<InterviewScheduleResponse | null>(null);
  const [formData, setFormData] = useState<InterviewScheduleRequest | UpdateInterviewRequest>({});
  const [isLoading, setIsLoading] = useState(false);

  // Fetch existing interview if reschedule mode
  useEffect(() => {
    if (mode === 'reschedule' && jobApplyId) {
      fetchExistingInterview(parseInt(jobApplyId));
    }
  }, [mode, jobApplyId]);

  const fetchExistingInterview = async (jobApplyId: number) => {
    try {
      setIsLoading(true);
      const response = await api.get(`/api/job-applies/${jobApplyId}/interview`);
      const interview = response.data;
      
      setExistingInterview(interview);
      
      // Pre-fill form with existing data
      setFormData({
        scheduledDate: interview.scheduledDate,
        durationMinutes: interview.durationMinutes,
        interviewType: interview.interviewType,
        location: interview.location,
        interviewerName: interview.interviewerName,
        interviewerEmail: interview.interviewerEmail,
        interviewerPhone: interview.interviewerPhone,
        preparationNotes: interview.preparationNotes,
        meetingLink: interview.meetingLink,
        interviewRound: interview.interviewRound,
      });
    } catch (error) {
      if (error.response?.status === 404) {
        // No existing interview - this shouldn't happen in reschedule mode
        toast.error('No existing interview found');
        router.push('/recruiter/applications');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      setIsLoading(true);
      
      if (mode === 'reschedule' && existingInterview) {
        // UPDATE existing interview
        await api.put(`/api/interviews/${existingInterview.id}`, {
          ...formData,
          updateReason: 'Interview rescheduled by recruiter'
        });
        toast.success('Interview rescheduled successfully');
      } else {
        // CREATE new interview
        await api.post(`/api/job-applies/${jobApplyId}/schedule-interview`, formData);
        toast.success('Interview scheduled successfully');
      }
      
      router.push('/recruiter/applications');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to schedule interview');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <h1>{mode === 'reschedule' ? 'Reschedule Interview' : 'Schedule Interview'}</h1>
      
      {existingInterview && (
        <Alert>
          <p>Current interview scheduled for: {formatDate(existingInterview.scheduledDate)}</p>
          <p>Status: {existingInterview.status}</p>
          {existingInterview.candidateConfirmed && (
            <p className="text-amber-600">
              ⚠️ Candidate has confirmed. Changing the date will require re-confirmation.
            </p>
          )}
        </Alert>
      )}
      
      <InterviewForm
        data={formData}
        onChange={setFormData}
        onSubmit={handleSubmit}
        isLoading={isLoading}
        submitLabel={mode === 'reschedule' ? 'Update Interview' : 'Schedule Interview'}
      />
    </div>
  );
}
```

### Interview Form Component
```typescript
interface InterviewFormProps {
  data: UpdateInterviewRequest;
  onChange: (data: UpdateInterviewRequest) => void;
  onSubmit: () => void;
  isLoading: boolean;
  submitLabel: string;
}

function InterviewForm({ data, onChange, onSubmit, isLoading, submitLabel }: InterviewFormProps) {
  return (
    <form onSubmit={(e) => { e.preventDefault(); onSubmit(); }}>
      {/* Date/Time Picker */}
      <DateTimePicker
        value={data.scheduledDate}
        onChange={(date) => onChange({ ...data, scheduledDate: date })}
        minDate={new Date()} // Must be in future
        label="Interview Date & Time"
      />
      
      {/* Duration */}
      <Select
        value={data.durationMinutes}
        onChange={(v) => onChange({ ...data, durationMinutes: parseInt(v) })}
        label="Duration"
        options={[
          { value: 30, label: '30 minutes' },
          { value: 45, label: '45 minutes' },
          { value: 60, label: '1 hour' },
          { value: 90, label: '1.5 hours' },
          { value: 120, label: '2 hours' },
        ]}
      />
      
      {/* Interview Type */}
      <Select
        value={data.interviewType}
        onChange={(v) => onChange({ ...data, interviewType: v as InterviewType })}
        label="Interview Type"
        options={[
          { value: 'IN_PERSON', label: 'In Person' },
          { value: 'VIDEO_CALL', label: 'Video Call' },
          { value: 'PHONE', label: 'Phone' },
          { value: 'ONLINE_ASSESSMENT', label: 'Online Assessment' },
        ]}
      />
      
      {/* Location (for in-person) */}
      {data.interviewType === 'IN_PERSON' && (
        <Input
          value={data.location}
          onChange={(v) => onChange({ ...data, location: v })}
          label="Location"
          placeholder="Office address"
        />
      )}
      
      {/* Meeting Link (for video calls) */}
      {data.interviewType === 'VIDEO_CALL' && (
        <Input
          value={data.meetingLink}
          onChange={(v) => onChange({ ...data, meetingLink: v })}
          label="Meeting Link"
          placeholder="https://zoom.us/j/..."
        />
      )}
      
      {/* Interviewer Details */}
      <Input
        value={data.interviewerName}
        onChange={(v) => onChange({ ...data, interviewerName: v })}
        label="Interviewer Name"
      />
      
      <Input
        value={data.interviewerEmail}
        onChange={(v) => onChange({ ...data, interviewerEmail: v })}
        label="Interviewer Email"
        type="email"
      />
      
      <Input
        value={data.interviewerPhone}
        onChange={(v) => onChange({ ...data, interviewerPhone: v })}
        label="Interviewer Phone"
        type="tel"
      />
      
      {/* Preparation Notes */}
      <Textarea
        value={data.preparationNotes}
        onChange={(v) => onChange({ ...data, preparationNotes: v })}
        label="Preparation Notes for Candidate"
        placeholder="What should the candidate prepare for this interview?"
      />
      
      {/* Interview Round */}
      <Input
        value={data.interviewRound}
        onChange={(v) => onChange({ ...data, interviewRound: parseInt(v) })}
        label="Interview Round"
        type="number"
        min={1}
      />
      
      <Button type="submit" disabled={isLoading}>
        {isLoading ? 'Saving...' : submitLabel}
      </Button>
    </form>
  );
}
```

## Error Handling

```typescript
// Error codes for interview update
const INTERVIEW_ERRORS = {
  9100: 'Interview not found',
  9102: 'Scheduled date must be in the future',
  9109: 'Scheduling conflict detected - time slot not available',
  9114: 'Cannot modify completed, cancelled, or no-show interviews',
};

// Handle errors in form submission
try {
  await api.put(`/api/interviews/${interviewId}`, updateRequest);
} catch (error) {
  const errorCode = error.response?.data?.code;
  const message = INTERVIEW_ERRORS[errorCode] || error.response?.data?.message;
  toast.error(message);
}
```

## Workflow Comparison

| Feature | Direct Update (Phase 12) | Consent Reschedule (Phase 7) |
|---------|-------------------------|------------------------------|
| Endpoint | `PUT /api/interviews/{id}` | `POST /api/interviews/{id}/reschedule` |
| Requires consent | No | Yes (default) |
| When to use | Recruiter needs immediate change | Either party requests |
| Candidate notified | Yes (via notification) | Yes + must respond |
| Confirmation reset | Yes (if date changed) | Yes (if accepted) |

## Best Practices

1. **Always check interview status** before showing reschedule button
2. **Show warning** if candidate has already confirmed
3. **Include conflict check** in date selection UI
4. **Provide update reason** for candidate notification
5. **Handle 404** gracefully when no interview exists
