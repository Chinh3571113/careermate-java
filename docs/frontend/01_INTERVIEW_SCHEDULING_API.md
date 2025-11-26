# Interview Scheduling API Documentation

**Module**: Interview Management  
**Base URL**: `/api`  
**Version**: 1.0  
**Last Updated**: November 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [Data Models](#data-models)
3. [API Endpoints](#api-endpoints)
4. [Business Rules](#business-rules)
5. [Error Handling](#error-handling)
6. [Notification Events](#notification-events)

---

## Overview

The Interview Scheduling API allows recruiters to schedule, manage, and track interviews for job applications. It includes automated reminders, time-based validation, and flexible rescheduling with mutual consent.

### Key Features
- ✅ Schedule interviews with date/time/location/interviewer details
- ✅ Candidate confirmation tracking
- ✅ Automated 24h and 2h reminders
- ✅ Flexible interview rescheduling (requires consent)
- ✅ Real-time duration adjustments
- ✅ Early completion support
- ✅ No-show and cancellation tracking
- ✅ Time-based validation (can't mark completed before interview ends)

---

## Data Models

### InterviewScheduleRequest
```typescript
interface InterviewScheduleRequest {
  scheduledDate: string;           // ISO 8601 datetime (e.g., "2025-12-01T14:00:00")
  durationMinutes: number;         // Default: 60
  interviewType: InterviewType;    // "IN_PERSON", "VIDEO_CALL", "PHONE", "ONLINE_ASSESSMENT"
  location?: string;               // Physical address or "Video Call" or "Phone"
  interviewerName: string;
  interviewerEmail: string;
  interviewerPhone?: string;       // Format: "+84-123-456-789"
  preparationNotes?: string;       // What candidate should prepare
  meetingLink?: string;            // Zoom/Teams/Google Meet link
  interviewRound?: number;         // Default: 1 (for multiple rounds)
}

enum InterviewType {
  IN_PERSON = "IN_PERSON",
  VIDEO_CALL = "VIDEO_CALL",
  PHONE = "PHONE",
  ONLINE_ASSESSMENT = "ONLINE_ASSESSMENT"
}
```

### InterviewScheduleResponse
```typescript
interface InterviewScheduleResponse {
  id: number;
  jobApplyId: number;
  candidateName: string;
  jobTitle: string;
  interviewRound: number;
  
  scheduledDate: string;           // ISO 8601 datetime
  durationMinutes: number;
  interviewType: InterviewType;
  location: string;
  
  interviewerName: string;
  interviewerEmail: string;
  interviewerPhone?: string;
  preparationNotes?: string;
  meetingLink?: string;
  
  status: InterviewStatus;         // "SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED", "NO_SHOW", "RESCHEDULED"
  
  candidateConfirmed: boolean;
  candidateConfirmedAt?: string;   // ISO 8601 datetime
  
  reminderSent24h: boolean;
  reminderSent2h: boolean;
  
  interviewCompletedAt?: string;
  interviewerNotes?: string;
  outcome?: InterviewOutcome;      // "PASS", "FAIL", "PENDING", "NEEDS_SECOND_ROUND"
  
  createdByRecruiterId: number;
  createdAt: string;
  updatedAt: string;
  
  // Frontend helpers
  canMarkCompleted: boolean;       // Backend calculates based on time
  timeUntilInterview?: string;     // Human-readable: "5 days 3 hours"
  canReschedule: boolean;          // At least 2 hours before interview
}

enum InterviewStatus {
  SCHEDULED = "SCHEDULED",
  CONFIRMED = "CONFIRMED",
  COMPLETED = "COMPLETED",
  CANCELLED = "CANCELLED",
  NO_SHOW = "NO_SHOW",
  RESCHEDULED = "RESCHEDULED"
}

enum InterviewOutcome {
  PASS = "PASS",
  FAIL = "FAIL",
  PENDING = "PENDING",
  NEEDS_SECOND_ROUND = "NEEDS_SECOND_ROUND"
}
```

### RescheduleInterviewRequest
```typescript
interface RescheduleInterviewRequest {
  newScheduledDate: string;        // ISO 8601 datetime
  reason: string;
  requestedBy: "RECRUITER" | "CANDIDATE";
  requiresConsent: boolean;        // true if < 2h notice
}
```

### CompleteInterviewRequest
```typescript
interface CompleteInterviewRequest {
  interviewerNotes: string;
  outcome: InterviewOutcome;
  nextSteps?: string;              // What happens next
}
```

---

## API Endpoints

### 1. Schedule Interview

**Endpoint**: `POST /api/job-applies/{jobApplyId}/schedule-interview`  
**Auth**: `RECRUITER` role required  
**Description**: Recruiter creates a new interview schedule for a job application.

#### Request
```http
POST /api/job-applies/123/schedule-interview
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "scheduledDate": "2025-12-01T14:00:00",
  "durationMinutes": 60,
  "interviewType": "VIDEO_CALL",
  "location": "Video Call",
  "interviewerName": "John Doe",
  "interviewerEmail": "john.doe@company.com",
  "interviewerPhone": "+84-123-456-789",
  "preparationNotes": "Please prepare a 10-minute portfolio presentation. Dress code: Business casual.",
  "meetingLink": "https://zoom.us/j/123456789",
  "interviewRound": 1
}
```

#### Response: `201 Created`
```json
{
  "id": 789,
  "jobApplyId": 123,
  "candidateName": "Jane Smith",
  "jobTitle": "Software Engineer",
  "scheduledDate": "2025-12-01T14:00:00",
  "status": "SCHEDULED",
  "interviewType": "VIDEO_CALL",
  "meetingLink": "https://zoom.us/j/123456789",
  "candidateConfirmed": false,
  "canMarkCompleted": false,
  "timeUntilInterview": "5 days 3 hours",
  "canReschedule": true,
  "createdAt": "2025-11-26T10:00:00"
}
```

#### Business Logic
- ❌ Cannot schedule if `scheduledDate` is in the past
- ❌ Cannot schedule if job apply status is not `REVIEWING` or `SUBMITTED`
- ✅ Automatically updates `JobApply.status` to `INTERVIEW_SCHEDULED`
- ✅ Sends email notification to candidate with interview details
- ✅ Adds iCal invite to candidate's calendar
- ✅ Schedules automated reminders (24h, 2h before)

---

### 2. Candidate Confirms Interview

**Endpoint**: `POST /api/interviews/{interviewId}/confirm`  
**Auth**: `CANDIDATE` role required  
**Description**: Candidate confirms they will attend the interview.

#### Request
```http
POST /api/interviews/789/confirm
Authorization: Bearer {candidate_token}
```

#### Response: `200 OK`
```json
{
  "id": 789,
  "status": "CONFIRMED",
  "candidateConfirmed": true,
  "candidateConfirmedAt": "2025-11-26T10:15:00"
}
```

#### Business Logic
- ✅ Updates `candidateConfirmed` flag to true
- ✅ Sends confirmation email to recruiter
- ❌ Cannot confirm if interview already CANCELLED or COMPLETED

---

### 3. Reschedule Interview

**Endpoint**: `POST /api/interviews/{interviewId}/reschedule`  
**Auth**: `RECRUITER` or `CANDIDATE` role required  
**Description**: Request to reschedule an interview to a different date/time.

#### Request
```http
POST /api/interviews/789/reschedule
Authorization: Bearer {recruiter_or_candidate_token}
Content-Type: application/json

{
  "newScheduledDate": "2025-12-02T15:00:00",
  "reason": "Interviewer unavailable due to emergency",
  "requestedBy": "RECRUITER",
  "requiresConsent": true
}
```

#### Response: `202 Accepted`
```json
{
  "rescheduleRequestId": 999,
  "status": "PENDING_CONSENT",
  "message": "Candidate will be notified. Reschedule will apply once confirmed.",
  "expiresAt": "2025-11-27T23:59:59"
}
```

#### Business Logic
- ✅ **If > 2h notice**: Reschedule immediately (no consent needed)
- ✅ **If < 2h notice**: Requires consent from other party
- ❌ Cannot reschedule if interview already COMPLETED
- ✅ Creates `InterviewRescheduleRequest` record
- ✅ Sends notification to other party
- ⏰ Consent request expires in 24 hours

---

### 4. Respond to Reschedule Request

**Endpoint**: `POST /api/interviews/reschedule-requests/{rescheduleRequestId}/respond`  
**Auth**: `RECRUITER` or `CANDIDATE` role required  
**Description**: Accept or reject an interview reschedule request.

#### Request
```http
POST /api/interviews/reschedule-requests/999/respond
Authorization: Bearer {candidate_token}
Content-Type: application/json

{
  "accepted": true,
  "responseNotes": "No problem, the new time works for me"
}
```

#### Response: `200 OK`
```json
{
  "rescheduleRequestId": 999,
  "status": "ACCEPTED",
  "interviewUpdated": true,
  "newScheduledDate": "2025-12-02T15:00:00"
}
```

#### Business Logic
- ✅ **If accepted**: Updates interview `scheduledDate` and sends notifications
- ❌ **If rejected**: Keeps original date, notifies requester
- ✅ Updates interview status to `RESCHEDULED` temporarily

---

### 5. Complete Interview

**Endpoint**: `POST /api/interviews/{interviewId}/complete`  
**Auth**: `RECRUITER` role required  
**Description**: Mark interview as completed after it occurs.

#### Request
```http
POST /api/interviews/789/complete
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "interviewerNotes": "Strong technical skills, good communication. Candidate demonstrated excellent problem-solving.",
  "outcome": "PASS",
  "nextSteps": "Schedule technical assessment for next week"
}
```

#### Response: `200 OK`
```json
{
  "id": 789,
  "status": "COMPLETED",
  "interviewCompletedAt": "2025-12-01T15:05:00",
  "jobApplyStatus": "INTERVIEWED",
  "outcome": "PASS"
}
```

#### Validation Error: `400 Bad Request` (if too early)
```json
{
  "error": "INTERVIEW_NOT_YET_COMPLETED",
  "message": "Interview is scheduled until 2025-12-01 15:00. Please wait until after this time.",
  "timeRemaining": "55 minutes",
  "canMarkCompletedAt": "2025-12-01T15:00:00"
}
```

#### Business Logic
- ❌ **Cannot complete before** `scheduledDate + durationMinutes`
- ✅ Updates `JobApply.status` to `INTERVIEWED`
- ✅ Records `JobApply.interviewedAt` timestamp
- ✅ **Auto-schedules review prompt** for 24 hours later
- ✅ Sends notification to candidate about completion

---

### 6. Adjust Interview Duration

**Endpoint**: `PATCH /api/interviews/{interviewId}/adjust-duration`  
**Auth**: `RECRUITER` role required  
**Description**: Update interview duration in real-time (during interview).

#### Request
```http
PATCH /api/interviews/789/adjust-duration
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "newDurationMinutes": 90,
  "reason": "Additional technical questions needed"
}
```

#### Response: `200 OK`
```json
{
  "interviewId": 789,
  "originalDuration": 60,
  "newDuration": 90,
  "newEndTime": "2025-12-01T15:30:00",
  "canMarkCompletedAt": "2025-12-01T15:30:00"
}
```

#### Business Logic
- ✅ Can adjust anytime before completion
- ✅ Updates `newEndTime` calculation for validation
- ⚠️ Sends notification to candidate if extended significantly (> 30 min)

---

### 7. Complete Interview Early

**Endpoint**: `POST /api/interviews/{interviewId}/complete-early`  
**Auth**: `RECRUITER` role required  
**Description**: Mark interview as completed before scheduled end time.

#### Request
```http
POST /api/interviews/789/complete-early
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "interviewerNotes": "All questions answered excellently. Candidate exceeded expectations.",
  "outcome": "PASS",
  "reason": "Interview concluded early - all topics covered"
}
```

#### Response: `200 OK`
```json
{
  "interviewId": 789,
  "status": "COMPLETED",
  "scheduledDuration": 60,
  "actualDuration": 30,
  "completedEarly": true,
  "jobApplyStatus": "INTERVIEWED"
}
```

#### Validation: `400 Bad Request` (if too early)
```json
{
  "error": "INTERVIEW_TOO_SHORT",
  "message": "Interview must last at least 30 minutes (50% of 60 min scheduled)",
  "minimumTime": "2025-12-01T14:30:00",
  "currentTime": "2025-12-01T14:20:00"
}
```

#### Business Logic
- ✅ **Must be at least 50% of scheduled duration**
- ✅ Prevents accidental early completion
- ✅ Same outcome recording as normal completion

---

### 8. Mark as No-Show

**Endpoint**: `POST /api/interviews/{interviewId}/no-show`  
**Auth**: `RECRUITER` role required  
**Description**: Mark candidate as no-show when they don't attend.

#### Request
```http
POST /api/interviews/789/no-show
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "notes": "Candidate did not attend. No response to calls/emails."
}
```

#### Response: `200 OK`
```json
{
  "id": 789,
  "status": "NO_SHOW",
  "jobApplyStatus": "REJECTED",
  "outcome": "FAIL"
}
```

#### Business Logic
- ✅ **Can only mark after** `scheduledDate` has passed
- ✅ Automatically updates `JobApply.status` to `REJECTED`
- ⚠️ Candidate may dispute if they claim technical issues

---

### 9. Cancel Interview

**Endpoint**: `POST /api/interviews/{interviewId}/cancel`  
**Auth**: `RECRUITER` role required  
**Description**: Cancel a scheduled interview.

#### Request
```http
POST /api/interviews/789/cancel
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "reason": "Position has been filled by another candidate"
}
```

#### Response: `200 OK`
```json
{
  "id": 789,
  "status": "CANCELLED",
  "cancellationReason": "Position has been filled by another candidate"
}
```

#### Business Logic
- ✅ Can cancel anytime before interview starts
- ✅ Sends notification to candidate
- ✅ JobApply status remains unchanged (still `INTERVIEW_SCHEDULED`)

---

### 10. Get Interview Details

**Endpoint**: `GET /api/interviews/{interviewId}`  
**Auth**: `RECRUITER` or `CANDIDATE` role required  
**Description**: Get full details of an interview schedule.

#### Response: `200 OK`
```json
{
  "id": 789,
  "jobApplyId": 123,
  "candidateName": "Jane Smith",
  "jobTitle": "Software Engineer",
  "scheduledDate": "2025-12-01T14:00:00",
  "durationMinutes": 60,
  "interviewType": "VIDEO_CALL",
  "meetingLink": "https://zoom.us/j/123456789",
  "status": "SCHEDULED",
  "candidateConfirmed": true,
  "reminderSent24h": true,
  "reminderSent2h": false,
  "canMarkCompleted": false,
  "timeUntilInterview": "5 days 2 hours"
}
```

---

### 11. Get Recruiter's Upcoming Interviews

**Endpoint**: `GET /api/interviews/recruiter/{recruiterId}/upcoming`  
**Auth**: `RECRUITER` role required  
**Description**: Get all upcoming interviews for a recruiter.

#### Query Parameters
- `startDate` (optional): Filter by start date (ISO 8601)
- `endDate` (optional): Filter by end date (ISO 8601)
- `status` (optional): Filter by status

#### Response: `200 OK`
```json
{
  "count": 3,
  "interviews": [
    {
      "id": 789,
      "candidateName": "Jane Smith",
      "jobTitle": "Software Engineer",
      "scheduledDate": "2025-12-01T14:00:00",
      "status": "CONFIRMED",
      "interviewType": "VIDEO_CALL",
      "timeUntilInterview": "5 days 3 hours",
      "canMarkCompleted": false,
      "actions": {
        "reschedule": true,
        "cancel": true,
        "markCompleted": false
      }
    }
  ]
}
```

---

### 12. Get Candidate's Upcoming Interviews

**Endpoint**: `GET /api/interviews/candidate/{candidateId}/upcoming`  
**Auth**: `CANDIDATE` role required  
**Description**: Get all upcoming interviews for a candidate.

#### Response: `200 OK`
```json
{
  "count": 2,
  "upcomingInterviews": [
    {
      "id": 789,
      "companyName": "Tech Corp",
      "jobTitle": "Software Engineer",
      "scheduledDate": "2025-12-01T14:00:00",
      "durationMinutes": 60,
      "interviewType": "VIDEO_CALL",
      "location": "Video Call",
      "interviewerName": "John Doe",
      "interviewerEmail": "john.doe@company.com",
      "meetingLink": "https://zoom.us/j/123456789",
      "preparationNotes": "Prepare portfolio presentation",
      "status": "SCHEDULED",
      "candidateConfirmed": false,
      "addToCalendarLink": "https://api.example.com/calendar/789.ics"
    }
  ]
}
```

---

### 13. Get Candidate's Past Interviews

**Endpoint**: `GET /api/interviews/candidate/{candidateId}/past`  
**Auth**: `CANDIDATE` role required  
**Description**: Get all past interviews (completed, no-show, cancelled).

#### Response: `200 OK`
```json
{
  "count": 5,
  "interviews": [
    {
      "id": 788,
      "companyName": "Tech Corp",
      "jobTitle": "Software Engineer",
      "scheduledDate": "2025-11-20T14:00:00",
      "status": "COMPLETED",
      "outcome": "PASS",
      "interviewCompletedAt": "2025-11-20T15:10:00",
      "canReview": true
    }
  ]
}
```

---

## Business Rules

### Time-Based Validation Matrix

| Action | Time Constraint | Validation |
|--------|----------------|------------|
| **Schedule Interview** | Must be future date/time | `scheduledDate > now` |
| **Mark as INTERVIEWED** | After interview end time | `now > interviewEndTime` |
| **Mark as NO_SHOW** | After interview start time | `now > scheduledDate && !completed` |
| **Reschedule (no consent)** | At least 2 hours before | `scheduledDate - now > 2 hours` |
| **Reschedule (with consent)** | Less than 2 hours before | `scheduledDate - now < 2 hours` |
| **Complete Early** | After 50% of scheduled time | `actualTime > scheduledTime * 0.5` |
| **Adjust Duration** | Anytime during interview | No time restriction |
| **Cancel Interview** | Before interview starts | `now < scheduledDate` |

### Automated Reminder Schedule

| Reminder | When Sent | Frequency |
|----------|-----------|-----------|
| **24-Hour Reminder** | 24 hours before interview | Hourly cron job checks |
| **2-Hour Reminder** | 2 hours before interview | Every 30 minutes |
| **Confirmation Prompt** | After scheduling | Immediate |
| **Reschedule Consent** | When requested | Immediate |
| **Review Prompt** | 24 hours after interview | Scheduled job |

### Status Transitions

```
SCHEDULED → CONFIRMED (candidate confirms)
SCHEDULED → RESCHEDULED (date changes)
SCHEDULED → CANCELLED (recruiter cancels)
CONFIRMED → COMPLETED (after interview time)
CONFIRMED → NO_SHOW (after scheduled time, no attendance)
RESCHEDULED → SCHEDULED (after consent given)
```

---

## Error Handling

### Common Error Responses

#### 400 Bad Request - Interview Not Yet Completed
```json
{
  "error": "INTERVIEW_NOT_YET_COMPLETED",
  "message": "Interview is scheduled until 2025-12-01 15:00. Please wait until after this time.",
  "timeRemaining": "55 minutes",
  "scheduledEndTime": "2025-12-01T15:00:00"
}
```

#### 400 Bad Request - Reschedule Too Late
```json
{
  "error": "RESCHEDULE_TOO_LATE",
  "message": "Cannot reschedule less than 2 hours before interview without consent",
  "requiresConsent": true,
  "scheduledTime": "2025-12-01T14:00:00"
}
```

#### 404 Not Found - Interview Not Found
```json
{
  "error": "INTERVIEW_NOT_FOUND",
  "message": "Interview with ID 789 does not exist"
}
```

#### 403 Forbidden - Cannot Complete Before Time
```json
{
  "error": "INTERVIEW_TOO_SHORT",
  "message": "Interview must last at least 30 minutes (50% of scheduled)",
  "minimumDuration": 30,
  "currentDuration": 15
}
```

---

## Notification Events

### Interview Scheduled
```json
{
  "eventType": "INTERVIEW_SCHEDULED",
  "recipientType": "CANDIDATE",
  "recipientId": "789",
  "data": {
    "interviewId": 789,
    "companyName": "Tech Corp",
    "jobTitle": "Software Engineer",
    "scheduledDate": "2025-12-01T14:00:00",
    "meetingLink": "https://zoom.us/j/123456789",
    "calendarInviteUrl": "https://api.example.com/calendar/789.ics"
  }
}
```

### 24-Hour Reminder
```json
{
  "eventType": "INTERVIEW_REMINDER_24H",
  "recipientType": "CANDIDATE",
  "recipientId": "789",
  "data": {
    "interviewId": 789,
    "scheduledDate": "2025-12-01T14:00:00",
    "meetingLink": "https://zoom.us/j/123456789",
    "preparationNotes": "Prepare portfolio presentation"
  }
}
```

### Interview Completed (Review Prompt)
```json
{
  "eventType": "INTERVIEW_COMPLETED_REVIEW_PROMPT",
  "recipientType": "CANDIDATE",
  "recipientId": "789",
  "data": {
    "interviewId": 789,
    "companyName": "Tech Corp",
    "reviewEligible": true,
    "reviewType": "INTERVIEW_EXPERIENCE",
    "promptMessage": "How was your interview experience with Tech Corp?"
  }
}
```

---

## Frontend Integration Checklist

### Interview Scheduling Page (Recruiter)
- [ ] Form with date/time picker (must be future date)
- [ ] Interview type dropdown (IN_PERSON, VIDEO_CALL, PHONE, ONLINE_ASSESSMENT)
- [ ] Location field (conditional: show only for IN_PERSON)
- [ ] Meeting link field (conditional: show only for VIDEO_CALL)
- [ ] Interviewer details (name, email, phone)
- [ ] Preparation notes textarea
- [ ] Round number selector (for multiple rounds)
- [ ] Validation: Cannot schedule in the past

### Interview Dashboard (Recruiter)
- [ ] Calendar view of upcoming interviews
- [ ] Filter by status (SCHEDULED, CONFIRMED, COMPLETED)
- [ ] Action buttons (Reschedule, Cancel, Mark Completed)
- [ ] **Disable "Mark Completed" button** until `scheduledDate + duration` passes
- [ ] Show countdown timer: "Interview in 5 days 3 hours"
- [ ] Show confirmation status (candidate confirmed: ✅ / ⏳ pending)
- [ ] Quick actions: Adjust Duration, Complete Early

### Interview Confirmation Page (Candidate)
- [ ] Interview details display (date, time, location, interviewer)
- [ ] "Confirm Attendance" button
- [ ] "Add to Calendar" button (downloads .ics file)
- [ ] Meeting link (clickable, opens in new tab)
- [ ] Preparation notes display
- [ ] Reschedule request button

### Interview History Page (Candidate)
- [ ] List of past interviews (COMPLETED, NO_SHOW, CANCELLED)
- [ ] Show outcome (PASS, FAIL, PENDING)
- [ ] "Leave Review" button (if not already reviewed)
- [ ] Filter by company/outcome

---

**End of Interview Scheduling API Documentation**
