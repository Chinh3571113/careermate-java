# Frontend Implementation Guide
**Generated:** November 26, 2025  
**API Base URL:** `http://localhost:8080/api`  
**Authentication:** Bearer Token (JWT)

---

## 📚 Table of Contents

1. [Interview Scheduling Pages](#1-interview-scheduling-pages)
2. [Interview Calendar Pages](#2-interview-calendar-pages)
3. [Employment Verification Pages](#3-employment-verification-pages)
4. [Company Review Pages](#4-company-review-pages)
5. [Shared Components](#5-shared-components)
6. [API Error Handling](#6-api-error-handling)
7. [Authentication & Authorization](#7-authentication--authorization)

---

## 1. Interview Scheduling Pages

### 1.1 Recruiter Interview Management Page

**Route:** `/recruiter/interviews`  
**Access:** RECRUITER role required

#### Features
- Schedule new interviews for job applications
- View upcoming interviews
- Manage interview status (complete, cancel, mark no-show)
- Handle reschedule requests
- Adjust interview duration

#### API Endpoints

##### Schedule Interview
```http
POST /api/job-applies/{jobApplyId}/schedule-interview
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "interviewRound": 1,
  "scheduledDate": "2024-12-20T10:00:00",
  "durationMinutes": 60,
  "interviewType": "VIDEO_CALL",  // IN_PERSON, VIDEO_CALL, PHONE, ONLINE_ASSESSMENT
  "location": "https://zoom.us/j/123456789",
  "interviewerName": "John Smith",
  "interviewerEmail": "john.smith@company.com",
  "interviewerPhone": "+1234567890",
  "preparationNotes": "Please review the technical assessment",
  "meetingLink": "https://zoom.us/j/123456789"
}

Response (201 Created):
{
  "id": 1,
  "jobApplyId": 123,
  "interviewRound": 1,
  "scheduledDate": "2024-12-20T10:00:00",
  "durationMinutes": 60,
  "interviewType": "VIDEO_CALL",
  "location": "https://zoom.us/j/123456789",
  "interviewerName": "John Smith",
  "interviewerEmail": "john.smith@company.com",
  "interviewerPhone": "+1234567890",
  "preparationNotes": "Please review the technical assessment",
  "meetingLink": "https://zoom.us/j/123456789",
  "status": "SCHEDULED",  // SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW, RESCHEDULED
  "candidateConfirmed": false,
  "candidateConfirmedAt": null,
  "expectedEndTime": "2024-12-20T11:00:00",
  "hasInterviewTimePassed": false,
  "isInterviewInProgress": false,
  "hoursUntilInterview": 72,
  "reminderSent24h": false,
  "reminderSent2h": false,
  "createdAt": "2024-12-17T14:30:00"
}
```

##### Get Recruiter's Upcoming Interviews
```http
GET /api/interviews/recruiter/{recruiterId}/upcoming
Authorization: Bearer {token}

Response (200 OK):
{
  "count": 5,
  "interviews": [
    {
      "id": 1,
      "jobApplyId": 123,
      "scheduledDate": "2024-12-20T10:00:00",
      "durationMinutes": 60,
      "interviewType": "VIDEO_CALL",
      "status": "CONFIRMED",
      "candidateConfirmed": true,
      "expectedEndTime": "2024-12-20T11:00:00",
      "hoursUntilInterview": 48
    }
    // ... more interviews
  ]
}
```

##### Complete Interview
```http
POST /api/interviews/{interviewId}/complete
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "interviewerNotes": "Candidate demonstrated strong technical skills",
  "outcome": "PASS"  // PASS, FAIL, PENDING, NEEDS_SECOND_ROUND
}

Response (200 OK):
{
  "id": 1,
  "status": "COMPLETED",
  "interviewCompletedAt": "2024-12-20T11:00:00",
  "interviewerNotes": "Candidate demonstrated strong technical skills",
  "outcome": "PASS"
}
```

##### Mark as No-Show
```http
POST /api/interviews/{interviewId}/no-show?notes=Candidate%20did%20not%20attend
Authorization: Bearer {token}

Response (200 OK):
{
  "id": 1,
  "status": "NO_SHOW",
  "interviewerNotes": "Candidate did not attend"
}
```

##### Cancel Interview
```http
POST /api/interviews/{interviewId}/cancel?reason=Position%20filled
Authorization: Bearer {token}

Response (200 OK):
{
  "id": 1,
  "status": "CANCELLED",
  "interviewerNotes": "Cancelled: Position filled"
}
```

##### Adjust Interview Duration
```http
PATCH /api/interviews/{interviewId}/adjust-duration?newDurationMinutes=90
Authorization: Bearer {token}

Response (200 OK):
{
  "id": 1,
  "durationMinutes": 90,
  "expectedEndTime": "2024-12-20T11:30:00"
}
```

#### UI Components

**Interview List Table:**
```jsx
Columns:
- Candidate Name
- Job Title
- Scheduled Date & Time
- Duration
- Type (with icon: 📹 VIDEO_CALL, 🏢 IN_PERSON, 📞 PHONE)
- Status (with badge colors)
- Candidate Confirmed (✓ or ✗)
- Hours Until Interview
- Actions (Complete, Cancel, Reschedule, No-Show)

Filters:
- Status dropdown
- Date range picker
- Interview type filter

Actions:
- "Schedule Interview" button (opens modal)
- Row actions: Complete, Cancel, Mark No-Show, Adjust Duration
```

**Schedule Interview Modal:**
```jsx
Form Fields:
- Job Application (search/select)
- Interview Round (number input, default: 1)
- Date & Time (datetime picker)
- Duration (number input with +/- buttons, default: 60 minutes)
- Interview Type (radio buttons: VIDEO_CALL, IN_PERSON, PHONE, ONLINE_ASSESSMENT)
- Location/Link (text input)
- Interviewer Name (text input)
- Interviewer Email (email input)
- Interviewer Phone (phone input)
- Preparation Notes (textarea)
- Meeting Link (URL input)

Validations:
- Date must be in the future
- Duration minimum: 15 minutes
- Email format validation
- Check for scheduling conflicts (API call)

Submit: POST /api/job-applies/{jobApplyId}/schedule-interview
```

---

### 1.2 Candidate Interview Dashboard

**Route:** `/candidate/interviews`  
**Access:** CANDIDATE role required

#### Features
- View upcoming interviews
- Confirm interview attendance
- Request reschedule
- View past interviews
- Access interview materials

#### API Endpoints

##### Get Candidate's Upcoming Interviews
```http
GET /api/interviews/candidate/{candidateId}/upcoming
Authorization: Bearer {token}

Response (200 OK):
{
  "count": 3,
  "interviews": [
    {
      "id": 1,
      "jobApplyId": 123,
      "companyName": "Tech Corp",
      "jobTitle": "Senior Developer",
      "scheduledDate": "2024-12-20T10:00:00",
      "durationMinutes": 60,
      "interviewType": "VIDEO_CALL",
      "location": "https://zoom.us/j/123456789",
      "interviewerName": "John Smith",
      "interviewerEmail": "john.smith@company.com",
      "preparationNotes": "Please review the technical assessment",
      "meetingLink": "https://zoom.us/j/123456789",
      "status": "SCHEDULED",
      "candidateConfirmed": false,
      "expectedEndTime": "2024-12-20T11:00:00",
      "hoursUntilInterview": 48,
      "reminderSent24h": false
    }
  ]
}
```

##### Confirm Interview
```http
POST /api/interviews/{interviewId}/confirm
Authorization: Bearer {token}

Response (200 OK):
{
  "id": 1,
  "status": "CONFIRMED",
  "candidateConfirmed": true,
  "candidateConfirmedAt": "2024-12-18T09:00:00"
}
```

##### Request Reschedule
```http
POST /api/interviews/{interviewId}/reschedule
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "newRequestedDate": "2024-12-21T14:00:00",
  "reason": "I have a prior commitment at the original time",
  "requestedBy": "CANDIDATE",
  "requiresConsent": true
}

Response (202 Accepted):
{
  "id": 1,
  "status": "RESCHEDULED",
  "rescheduleRequestId": 10,
  "message": "Reschedule request sent to recruiter"
}
```

##### Get Candidate's Past Interviews
```http
GET /api/interviews/candidate/{candidateId}/past
Authorization: Bearer {token}

Response (200 OK):
{
  "count": 5,
  "interviews": [
    {
      "id": 5,
      "companyName": "Tech Corp",
      "jobTitle": "Senior Developer",
      "scheduledDate": "2024-12-10T10:00:00",
      "status": "COMPLETED",
      "outcome": "PASS",
      "interviewCompletedAt": "2024-12-10T11:00:00"
    }
  ]
}
```

#### UI Components

**Upcoming Interviews Card List:**
```jsx
Card Components (one per interview):
- Company Logo
- Job Title & Company Name
- Interview Date & Time (large, prominent)
- Countdown Timer ("In 2 days, 5 hours")
- Interview Type Badge
- Duration
- Interviewer Info (name, email with mailto link)
- Meeting Link (if VIDEO_CALL)
- Location (if IN_PERSON)
- Preparation Notes (expandable)
- Confirmation Status:
  * If not confirmed: "Confirm Attendance" button (prominent)
  * If confirmed: ✓ "Confirmed" badge with timestamp
- Actions:
  * "Request Reschedule" button
  * "Add to Calendar" button (download .ics file)
  * "View Details" button

Sorting: By scheduled date (nearest first)
Empty State: "No upcoming interviews"
```

**Reschedule Request Modal:**
```jsx
Form Fields:
- Current Date & Time (read-only, display)
- New Requested Date & Time (datetime picker)
- Reason (textarea, required, min 20 characters)
- Disclaimer: "Reschedule requires recruiter approval. You'll be notified of the decision."

Validations:
- New date must be at least 24 hours from now
- Cannot reschedule within 2 hours of interview time
- Reason is required

Submit: POST /api/interviews/{interviewId}/reschedule
Success: Show "Request sent" message, disable reschedule button
```

---

### 1.3 Reschedule Management Page

**Route:** `/recruiter/reschedule-requests`  
**Access:** RECRUITER role required

#### Features
- View pending reschedule requests
- Accept or reject reschedules
- View reschedule history

#### API Endpoints

##### Respond to Reschedule Request
```http
POST /api/interviews/reschedule-requests/{rescheduleRequestId}/respond
  ?accepted=true
  &responseNotes=Approved%20as%20requested
Authorization: Bearer {token}

Response (200 OK):
{
  "id": 1,
  "scheduledDate": "2024-12-21T14:00:00",  // Updated date
  "status": "RESCHEDULED",
  "rescheduleAccepted": true
}
```

#### UI Components

**Reschedule Requests Table:**
```jsx
Columns:
- Candidate Name
- Job Title
- Original Date & Time
- Requested New Date & Time
- Reason
- Requested By (CANDIDATE or RECRUITER)
- Expires At (countdown)
- Status (PENDING_CONSENT, ACCEPTED, REJECTED, EXPIRED)
- Actions (Accept, Reject)

Filters:
- Status filter
- Expiring soon (< 24 hours)

Row Actions:
- "Accept" button → POST with accepted=true
- "Reject" button → Opens modal for rejection notes, POST with accepted=false
```

---

## 2. Interview Calendar Pages

### 2.1 Recruiter Calendar Configuration Page

**Route:** `/recruiter/calendar/settings`  
**Access:** RECRUITER role required

#### Features
- Configure working hours for each day of the week
- Set lunch breaks and buffer times
- Manage time-off periods
- View calendar availability

#### API Endpoints

##### Set Working Hours (Individual Day)
```http
POST /api/calendar/recruiters/{recruiterId}/working-hours
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "dayOfWeek": "MONDAY",  // MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
  "isWorkingDay": true,
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "lunchBreakStart": "12:00:00",
  "lunchBreakEnd": "13:00:00",
  "bufferMinutesBetweenInterviews": 15,
  "maxInterviewsPerDay": 8
}

Response (200 OK):
{
  "workingHoursId": 1,
  "recruiterId": 10,
  "dayOfWeek": "MONDAY",
  "isWorkingDay": true,
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "lunchBreakStart": "12:00:00",
  "lunchBreakEnd": "13:00:00",
  "bufferMinutesBetweenInterviews": 15,
  "maxInterviewsPerDay": 8,
  "totalWorkingMinutes": 420  // Auto-calculated (7 hours - 1 hour lunch)
}
```

##### Set Batch Working Hours (All 7 Days) ⭐ NEW
```http
POST /api/calendar/recruiters/working-hours/batch
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "recruiterId": 10,
  "replaceAll": false,  // If true, marks unspecified days as non-working
  "workingHoursConfigurations": [
    {
      "dayOfWeek": "MONDAY",
      "isWorkingDay": true,
      "startTime": "09:00:00",
      "endTime": "17:00:00",
      "lunchBreakStart": "12:00:00",
      "lunchBreakEnd": "13:00:00",
      "bufferMinutesBetweenInterviews": 15,
      "maxInterviewsPerDay": 8
    },
    {
      "dayOfWeek": "TUESDAY",
      "isWorkingDay": true,
      "startTime": "09:00:00",
      "endTime": "17:00:00",
      "lunchBreakStart": "12:00:00",
      "lunchBreakEnd": "13:00:00",
      "bufferMinutesBetweenInterviews": 15,
      "maxInterviewsPerDay": 8
    }
    // ... configure all 7 days
  ]
}

Response (200 OK):
{
  "recruiterId": 10,
  "totalConfigurations": 7,
  "successfulUpdates": 7,
  "failedUpdates": 0,
  "updatedConfigurations": [
    {
      "workingHoursId": 1,
      "dayOfWeek": "MONDAY",
      "totalWorkingMinutes": 420
    }
    // ... all days
  ],
  "errors": null  // Map of day → error message if any failures
}
```

##### Get Working Hours Configuration
```http
GET /api/calendar/recruiters/{recruiterId}/working-hours
Authorization: Bearer {token}

Response (200 OK):
[
  {
    "workingHoursId": 1,
    "recruiterId": 10,
    "dayOfWeek": "MONDAY",
    "isWorkingDay": true,
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "lunchBreakStart": "12:00:00",
    "lunchBreakEnd": "13:00:00",
    "bufferMinutesBetweenInterviews": 15,
    "maxInterviewsPerDay": 8,
    "totalWorkingMinutes": 420
  }
  // ... all 7 days
]
```

##### Request Time-Off
```http
POST /api/calendar/recruiters/{recruiterId}/time-off
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "startDate": "2024-12-23",
  "endDate": "2024-12-27",
  "timeOffType": "VACATION",  // VACATION, SICK_LEAVE, PERSONAL_DAY, PUBLIC_HOLIDAY, COMPANY_EVENT, TRAINING, OTHER
  "reason": "Year-end holidays"
}

Response (201 Created):
{
  "timeOffId": 1,
  "recruiterId": 10,
  "startDate": "2024-12-23",
  "endDate": "2024-12-27",
  "timeOffType": "VACATION",
  "reason": "Year-end holidays",
  "isApproved": false,
  "approvedByAdminId": null,
  "approvedAt": null,
  "createdAt": "2024-12-10T14:00:00"
}
```

##### Get Time-Off Periods
```http
GET /api/calendar/recruiters/{recruiterId}/time-off?approved=true
Authorization: Bearer {token}

Response (200 OK):
[
  {
    "timeOffId": 1,
    "startDate": "2024-12-23",
    "endDate": "2024-12-27",
    "timeOffType": "VACATION",
    "reason": "Year-end holidays",
    "isApproved": true,
    "approvedByAdminId": 100,
    "approvedAt": "2024-12-11T10:00:00"
  }
]
```

#### UI Components

**Weekly Working Hours Grid:**
```jsx
Layout: 7 columns (Mon-Sun), each with:
- Day Name
- Toggle: "Working Day" / "Day Off"
- If working day:
  * Start Time (time picker)
  * End Time (time picker)
  * Lunch Break Start (time picker)
  * Lunch Break End (time picker)
  * Buffer Minutes (number input, 0-60)
  * Max Interviews (number input, 1-20)
  * Total Working Hours (calculated, read-only)

Quick Actions:
- "Copy to All Days" button (copies current day config to all days)
- "Set Weekdays" button (sets Mon-Fri as working, Sat-Sun as off)
- "Reset to Default" button

Save Options:
- "Save This Day" button (single day update)
- "Save All Days" button (batch update) ⭐ NEW - Reduces 7 API calls to 1

Validation:
- End time > Start time
- Lunch break within working hours
- Lunch break end > Lunch break start
- Buffer minutes: 0-60 range
- Max interviews: 1-20 range

Real-time Feedback:
- Show validation errors inline
- Display success/error messages per day after batch save
- Preview total working hours for each day
```

**Time-Off Calendar:**
```jsx
Components:
- Full calendar view (month view)
- Highlighted time-off periods (color-coded by type)
- "Request Time-Off" button → Opens modal

Time-Off Request Modal:
Form Fields:
- Start Date (date picker)
- End Date (date picker)
- Type (dropdown: VACATION, SICK_LEAVE, etc.)
- Reason (textarea)
- Disclaimer: "Requires admin approval"

Validations:
- End date >= Start date
- Cannot overlap existing time-off
- Reason required for SICK_LEAVE, PERSONAL_DAY

Time-Off List (below calendar):
Table showing all time-off requests:
- Date Range
- Type (with icon/badge)
- Reason
- Status (Pending/Approved badge)
- Approved By (if approved)
- Actions: Cancel (if pending)
```

---

### 2.2 Interview Calendar View

**Route:** `/recruiter/calendar`  
**Access:** RECRUITER role required

#### Features
- Daily, weekly, monthly calendar views
- View scheduled interviews
- Check availability
- Find available time slots
- Conflict detection

#### API Endpoints

##### Get Daily Calendar
```http
GET /api/calendar/recruiters/{recruiterId}/daily?date=2024-12-20
Authorization: Bearer {token}

Response (200 OK):
{
  "recruiterId": 10,
  "date": "2024-12-20",
  "dayOfWeek": "FRIDAY",
  "isWorkingDay": true,
  "workingHours": {
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "lunchBreakStart": "12:00:00",
    "lunchBreakEnd": "13:00:00",
    "totalWorkingMinutes": 420
  },
  "scheduledInterviews": [
    {
      "interviewId": 1,
      "startTime": "10:00:00",
      "endTime": "11:00:00",
      "candidateName": "John Doe",
      "jobTitle": "Senior Developer",
      "status": "CONFIRMED"
    }
  ],
  "availableSlots": [
    "09:00:00",
    "09:15:00",
    "09:30:00",
    "09:45:00",
    // ... 15-minute increments
  ],
  "utilizationRate": 0.35,  // 35% of available time booked
  "totalInterviews": 3,
  "maxInterviewsReached": false
}
```

##### Get Weekly Calendar
```http
GET /api/calendar/recruiters/{recruiterId}/weekly?weekStartDate=2024-12-16
Authorization: Bearer {token}

Response (200 OK):
{
  "recruiterId": 10,
  "weekStartDate": "2024-12-16",  // Monday
  "weekEndDate": "2024-12-22",    // Sunday
  "dailyCalendars": [
    {
      "date": "2024-12-16",
      "dayOfWeek": "MONDAY",
      "isWorkingDay": true,
      "totalInterviews": 3,
      "availableSlotsCount": 15,
      "utilizationRate": 0.40
    }
    // ... 7 days
  ],
  "weeklyStats": {
    "totalInterviews": 15,
    "totalWorkingDays": 5,
    "averageUtilization": 0.45,
    "busiestDay": "WEDNESDAY"
  }
}
```

##### Get Monthly Calendar
```http
GET /api/calendar/recruiters/{recruiterId}/monthly?year=2024&month=12
Authorization: Bearer {token}

Response (200 OK):
{
  "recruiterId": 10,
  "year": 2024,
  "month": 12,
  "monthName": "December",
  "dailySummaries": [
    {
      "date": "2024-12-01",
      "dayOfWeek": "SUNDAY",
      "isWorkingDay": false,
      "interviewCount": 0
    },
    {
      "date": "2024-12-02",
      "dayOfWeek": "MONDAY",
      "isWorkingDay": true,
      "interviewCount": 3,
      "isFullyBooked": false
    }
    // ... all days in month
  ],
  "monthlyStats": {
    "totalInterviews": 45,
    "totalWorkingDays": 22,
    "averageInterviewsPerDay": 2.05,
    "busiestWeek": "2024-12-09 to 2024-12-15"
  }
}
```

##### Check Conflict
```http
POST /api/calendar/check-conflict
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "recruiterId": 10,
  "candidateId": 50,
  "proposedStartTime": "2024-12-20T14:00:00",
  "durationMinutes": 60
}

Response (200 OK):
{
  "hasConflict": true,
  "conflictReason": "Interview overlaps with lunch break (12:00-13:00)",
  "conflictType": "LUNCH_BREAK",  // WORKING_HOURS, LUNCH_BREAK, EXISTING_INTERVIEW, TIME_OFF, CANDIDATE_DOUBLE_BOOKING, BUFFER_VIOLATION
  "suggestedAlternatives": [
    "2024-12-20T09:00:00",
    "2024-12-20T13:15:00",
    "2024-12-20T15:30:00"
  ]
}
```

##### Get Available Slots
```http
GET /api/calendar/recruiters/{recruiterId}/available-slots
  ?date=2024-12-20
  &durationMinutes=60
Authorization: Bearer {token}

Response (200 OK):
{
  "recruiterId": 10,
  "date": "2024-12-20",
  "durationMinutes": 60,
  "availableSlots": [
    "09:00:00",
    "10:15:00",
    "13:15:00",
    "14:30:00",
    "15:45:00"
  ],
  "totalSlotsAvailable": 5
}
```

##### Get Available Dates
```http
GET /api/calendar/recruiters/{recruiterId}/available-dates
  ?startDate=2024-12-20
  &endDate=2024-12-31
  &durationMinutes=60
Authorization: Bearer {token}

Response (200 OK):
{
  "recruiterId": 10,
  "startDate": "2024-12-20",
  "endDate": "2024-12-31",
  "durationMinutes": 60,
  "availableDates": [
    "2024-12-20",
    "2024-12-21",
    "2024-12-23",
    "2024-12-24",
    "2024-12-27",
    "2024-12-30"
  ],
  "totalDatesAvailable": 6
}
```

#### UI Components

**Calendar View Switcher:**
```jsx
Tabs: Daily | Weekly | Monthly

Daily View:
- Time grid (9 AM - 5 PM)
- Scheduled interviews (draggable blocks)
- Lunch break (grayed out)
- Available slots (green highlight)
- Click on slot → Schedule interview
- Hover → Show "Schedule Interview" button

Weekly View:
- 7 columns (Mon-Sun)
- Each column shows:
  * Working hours
  * Interview count
  * Utilization bar (colored: green <50%, yellow 50-80%, red >80%)
  * Click → Show daily view

Monthly View:
- Calendar grid
- Each day cell shows:
  * Interview count badge
  * Fully booked indicator
  * Working/Non-working day styling
  * Click → Show daily view

Filters (all views):
- Date navigation (prev/next, today button)
- Status filter
- Conflict highlighting toggle
```

**Available Slots Finder:**
```jsx
Form:
- Date (date picker)
- Duration (dropdown: 30, 45, 60, 90 minutes)
- "Find Slots" button

Result Display:
- List of available time slots
- Each slot:
  * Time (e.g., "9:00 AM - 10:00 AM")
  * "Optimal" badge (for 10 AM, 2 PM slots)
  * "Schedule Interview" button

Empty State: "No available slots on this date. Try another date."
```

---

### 2.3 Admin Time-Off Approval Page

**Route:** `/admin/time-off-requests`  
**Access:** ADMIN role required

#### Features
- View all pending time-off requests
- Approve or reject requests
- View time-off history

#### API Endpoints

##### Approve Time-Off
```http
POST /api/calendar/admin/time-off/{timeOffId}/approve?adminId=100
Authorization: Bearer {token}

Response (200 OK):
{
  "timeOffId": 1,
  "isApproved": true,
  "approvedByAdminId": 100,
  "approvedAt": "2024-12-11T10:00:00"
}
```

##### Cancel Time-Off
```http
DELETE /api/calendar/time-off/{timeOffId}
Authorization: Bearer {token}

Response (204 No Content)
```

#### UI Components

**Time-Off Requests Table:**
```jsx
Columns:
- Recruiter Name
- Date Range
- Days Count
- Type (badge with icon)
- Reason
- Submitted Date
- Status (PENDING badge)
- Actions (Approve, Reject)

Filters:
- Status (Pending/Approved/All)
- Type filter
- Recruiter search

Row Actions:
- "Approve" button (green)
- "Reject" button (red) → Opens modal for rejection reason

Sorting: By submitted date (oldest first)
```

---

## 3. Employment Verification Pages

### 3.1 Recruiter Employment Management

**Route:** `/recruiter/employments`  
**Access:** RECRUITER role required

#### Features
- Create employment verification when candidate is hired
- Terminate employment
- View active employments
- Track employment duration

#### API Endpoints

##### Create Employment Verification
```http
POST /api/employment-verifications/job-apply/{jobApplyId}
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "startDate": "2024-12-20"
}

Response (201 Created):
{
  "code": 201,
  "message": "Employment verification created successfully",
  "result": {
    "id": 1,
    "jobApplyId": 123,
    "startDate": "2024-12-20",
    "endDate": null,
    "isActive": true,
    "daysEmployed": 0,
    "isEligibleForWorkReview": false,  // True after 30 days
    "isCurrentlyEmployed": true,
    "createdAt": "2024-12-20T10:00:00"
  }
}
```

##### Get Employment Verification
```http
GET /api/employment-verifications/job-apply/{jobApplyId}
Authorization: Bearer {token}

Response (200 OK):
{
  "code": 200,
  "result": {
    "id": 1,
    "jobApplyId": 123,
    "candidateName": "John Doe",
    "jobTitle": "Senior Developer",
    "startDate": "2024-12-20",
    "endDate": null,
    "isActive": true,
    "daysEmployed": 45,
    "isEligibleForWorkReview": true,
    "isCurrentlyEmployed": true
  }
}
```

##### Terminate Employment
```http
POST /api/employment-verifications/job-apply/{jobApplyId}/terminate
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "endDate": "2024-12-31"
}

Response (200 OK):
{
  "code": 200,
  "message": "Employment terminated successfully",
  "result": {
    "id": 1,
    "jobApplyId": 123,
    "startDate": "2024-12-20",
    "endDate": "2024-12-31",
    "isActive": false,
    "daysEmployed": 11,
    "isEligibleForWorkReview": false,
    "isCurrentlyEmployed": false
  }
}
```

##### Get Active Employments
```http
GET /api/employment-verifications/recruiter/active
Authorization: Bearer {token}

Response (200 OK):
{
  "code": 200,
  "result": [
    {
      "id": 1,
      "jobApplyId": 123,
      "candidateName": "John Doe",
      "jobTitle": "Senior Developer",
      "startDate": "2024-11-01",
      "daysEmployed": 25,
      "isEligibleForWorkReview": false
    }
  ]
}
```

#### UI Components

**Active Employments Table:**
```jsx
Columns:
- Candidate Name (with avatar)
- Job Title
- Start Date
- Days Employed (with badge: <30 days "New", 30-90 days "Probation", >90 days "Established")
- Review Eligible (✓ if >=30 days, ✗ otherwise)
- Status (Active badge)
- Actions (Terminate, View Details)

Filters:
- Review eligibility filter
- Days employed range

Empty State: "No active employments"

Actions:
- "Create Employment" button → Opens modal with job apply search
- Row action: "Terminate" → Opens confirmation modal with end date picker
```

**Create Employment Modal:**
```jsx
Form:
- Job Application (search/autocomplete)
  * Shows: Candidate name, job title, status
  * Filter: Only ACCEPTED applications without existing employment
- Start Date (date picker, default: today)

Validation:
- Start date cannot be in the future
- Cannot create if employment already exists

Submit: POST /api/employment-verifications/job-apply/{jobApplyId}
Success: Refresh table, show success message
```

---

## 4. Company Review Pages

### 4.1 Company Review Submission Page

**Route:** `/candidate/reviews/submit`  
**Access:** CANDIDATE role required

#### Features
- Check eligibility to review a company
- Submit reviews based on experience stage
- View what can be reviewed
- Anonymous review option

#### API Endpoints

##### Check Review Eligibility
```http
GET /api/v1/reviews/eligibility?candidateId=50&jobApplyId=123
Authorization: Bearer {token}

Response (200 OK):
{
  "code": 200,
  "message": "Eligibility checked successfully",
  "result": {
    "jobApplyId": 123,
    "candidateId": 50,
    "recruiterId": 10,
    "companyName": "Tech Corp",
    "qualification": "HIRED",  // NOT_ELIGIBLE, APPLICANT, INTERVIEWED, HIRED, REJECTED
    "allowedReviewTypes": [
      "APPLICATION_EXPERIENCE",
      "INTERVIEW_EXPERIENCE",
      "WORK_EXPERIENCE"
    ],
    "message": "You've been employed for 45 days. You can review the application process, interview experience, and work culture.",
    "alreadyReviewed": {
      "APPLICATION_EXPERIENCE": false,
      "INTERVIEW_EXPERIENCE": false,
      "WORK_EXPERIENCE": false
    },
    "daysSinceApplication": 60,
    "daysEmployed": 45,
    "canReview": true
  }
}
```

##### Submit Company Review
```http
POST /api/v1/reviews
Authorization: Bearer {token}
Content-Type: application/json

Request Body:
{
  "jobApplyId": 123,
  "reviewType": "WORK_EXPERIENCE",  // APPLICATION_EXPERIENCE, INTERVIEW_EXPERIENCE, WORK_EXPERIENCE
  "reviewText": "Great company culture and supportive management. Work-life balance is excellent.",
  "overallRating": 5,
  "communicationRating": 5,
  "responsivenessRating": 4,
  "interviewProcessRating": 5,
  "workCultureRating": 5,
  "managementRating": 5,
  "benefitsRating": 4,
  "workLifeBalanceRating": 5,
  "isAnonymous": false
}

Response (201 Created):
{
  "code": 201,
  "message": "Review submitted successfully",
  "result": {
    "id": 1,
    "candidateId": 50,
    "candidateName": "John Doe",
    "recruiterId": 10,
    "companyName": "Tech Corp",
    "jobTitle": "Senior Developer",
    "reviewType": "WORK_EXPERIENCE",
    "status": "ACTIVE",
    "reviewText": "Great company culture...",
    "overallRating": 5,
    "workCultureRating": 5,
    "managementRating": 5,
    "benefitsRating": 4,
    "workLifeBalanceRating": 5,
    "isAnonymous": false,
    "isVerified": true,
    "sentimentScore": 0.85,
    "createdAt": "2024-12-20T10:00:00"
  }
}
```

#### UI Components

**Review Submission Form:**
```jsx
Step 1: Select Company (if multiple applications)
- List of eligible companies (from job applications)
- Shows qualification level and what can be reviewed
- "Check Eligibility" button

Step 2: Choose Review Type
- Radio buttons: APPLICATION_EXPERIENCE, INTERVIEW_EXPERIENCE, WORK_EXPERIENCE
- Each shows:
  * Description of what to review
  * Already submitted indicator
  * Lock icon if not eligible

Step 3: Write Review
Form Sections by Review Type:

All Types:
- Overall Rating (5-star rating)
- Communication Rating (5-star, optional)
- Responsiveness Rating (5-star, optional)
- Review Text (textarea, 20-2000 chars, required)
  * Placeholder changes based on review type
  * Character counter
  * Emoji picker option

APPLICATION_EXPERIENCE specific:
- Focus on: Application process, response time, communication

INTERVIEW_EXPERIENCE specific:
- Interview Process Rating (5-star, optional)
- Focus on: Interview experience, questions asked, professionalism

WORK_EXPERIENCE specific:
- Work Culture Rating (5-star, optional)
- Management Rating (5-star, optional)
- Benefits Rating (5-star, optional)
- Work-Life Balance Rating (5-star, optional)
- Focus on: Day-to-day work, culture, benefits, management

Step 4: Privacy Settings
- Checkbox: "Post as anonymous" (default: unchecked)
  * If checked: "Your name will not be displayed"
  * If unchecked: "Your name and profile will be visible"

Validations:
- Review text: 20-2000 characters
- At least one rating must be provided
- Overall rating is required
- Review type must be allowed

Submit: POST /api/v1/reviews
Success: Redirect to "My Reviews" page, show success message
```

---

### 4.2 Company Reviews Display Page

**Route:** `/companies/{recruiterId}/reviews`  
**Access:** PUBLIC (no authentication required)

#### Features
- View all reviews for a company
- Filter by review type
- See rating statistics
- Sort by date/rating

#### API Endpoints

##### Get Company Reviews
```http
GET /api/v1/reviews/company/{recruiterId}
  ?reviewType=WORK_EXPERIENCE
  &page=0
  &size=10
Authorization: None (public endpoint)

Response (200 OK):
{
  "code": 200,
  "message": "Reviews retrieved successfully",
  "result": {
    "content": [
      {
        "id": 1,
        "candidateName": "John Doe",  // null if anonymous
        "companyName": "Tech Corp",
        "jobTitle": "Senior Developer",
        "reviewType": "WORK_EXPERIENCE",
        "reviewText": "Great company culture...",
        "overallRating": 5,
        "workCultureRating": 5,
        "managementRating": 5,
        "benefitsRating": 4,
        "workLifeBalanceRating": 5,
        "isAnonymous": false,
        "isVerified": true,
        "createdAt": "2024-12-20T10:00:00"
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "size": 10,
    "number": 0
  }
}
```

##### Get Company Statistics
```http
GET /api/v1/reviews/company/{recruiterId}/statistics
Authorization: None (public endpoint)

Response (200 OK):
{
  "code": 200,
  "message": "Statistics calculated successfully",
  "result": {
    "recruiterId": 10,
    "companyName": "Tech Corp",
    "totalReviews": 25,
    "averageOverallRating": 4.5,
    "applicationReviews": 10,
    "interviewReviews": 8,
    "workExperienceReviews": 7,
    "avgCommunication": 4.3,
    "avgResponsiveness": 4.2,
    "avgInterviewProcess": 4.6,
    "avgWorkCulture": 4.8,
    "avgManagement": 4.5,
    "avgBenefits": 4.0,
    "avgWorkLifeBalance": 4.7,
    "ratingDistribution": {
      "1": 0,
      "2": 1,
      "3": 3,
      "4": 8,
      "5": 13
    },
    "avgSentimentScore": 0.72,
    "verifiedReviews": 25,
    "anonymousReviews": 5
  }
}
```

##### Get Average Rating
```http
GET /api/v1/reviews/company/{recruiterId}/rating
Authorization: None (public endpoint)

Response (200 OK):
{
  "code": 200,
  "message": "Average rating calculated successfully",
  "result": 4.5
}
```

#### UI Components

**Company Reviews Page:**
```jsx
Header Section:
- Company Name & Logo
- Overall Rating (large, prominent)
  * 4.5 ★ (25 reviews)
- Rating Breakdown:
  * 5 stars: ████████████████ 13 (52%)
  * 4 stars: ████████ 8 (32%)
  * 3 stars: ███ 3 (12%)
  * 2 stars: █ 1 (4%)
  * 1 star: 0 (0%)

Statistics Cards:
- Application Experience: 4.3 ★ (10 reviews)
- Interview Experience: 4.6 ★ (8 reviews)
- Work Experience: 4.8 ★ (7 reviews)

Aspect Ratings (horizontal bars):
- Communication: 4.3 ★ ████████
- Responsiveness: 4.2 ★ ████████
- Interview Process: 4.6 ★ █████████
- Work Culture: 4.8 ★ █████████
- Management: 4.5 ★ █████████
- Benefits: 4.0 ★ ████████
- Work-Life Balance: 4.7 ★ █████████

Filters:
- Review Type dropdown (All, Application, Interview, Work Experience)
- Sort by: Most Recent | Highest Rating | Lowest Rating

Reviews List:
Each review card shows:
- Candidate name (or "Anonymous" with 🕶️ icon)
- Verified badge ✓ (all system reviews)
- Job title
- Review type badge
- Overall rating (5 stars)
- Review date (relative: "2 months ago")
- Review text (expandable if long)
- Aspect ratings (if applicable)
- Helpful buttons: 👍 Helpful | 🚩 Flag
- Flag count (if >0)

Pagination: Show 10 reviews per page

Empty State: "No reviews yet. Be the first to review!"
```

**Flag Review Modal:**
```jsx
Triggered by: Click 🚩 Flag button
Form:
- Reason (dropdown):
  * Spam or fake review
  * Offensive content
  * Violates policies
  * Other
- Additional details (textarea, optional)

Submit: POST /api/v1/reviews/{reviewId}/flag
Success: Show "Thank you for reporting" message, disable flag button
```

---

### 4.3 Candidate's Reviews Page

**Route:** `/candidate/my-reviews`  
**Access:** CANDIDATE role required

#### Features
- View all reviews submitted by candidate
- Edit/update reviews
- See review statistics

#### API Endpoints

##### Get My Reviews
```http
GET /api/v1/reviews/my-reviews?candidateId=50&page=0&size=10
Authorization: Bearer {token}

Response (200 OK):
{
  "code": 200,
  "message": "Your reviews retrieved successfully",
  "result": {
    "content": [
      {
        "id": 1,
        "companyName": "Tech Corp",
        "jobTitle": "Senior Developer",
        "reviewType": "WORK_EXPERIENCE",
        "overallRating": 5,
        "status": "ACTIVE",
        "createdAt": "2024-12-20T10:00:00",
        "isAnonymous": false,
        "flagCount": 0
      }
    ],
    "totalElements": 3,
    "totalPages": 1
  }
}
```

#### UI Components

**My Reviews Page:**
```jsx
Header:
- Total reviews count
- Average rating given
- "Submit New Review" button

Reviews List:
Each review card shows:
- Company name & logo
- Job title
- Review type badge
- Overall rating
- Submission date
- Status badge (ACTIVE, FLAGGED, REMOVED)
- Flag count (if >0)
- Privacy: Anonymous 🕶️ or Public 👤
- Actions: View Full Review | Edit (if not flagged/removed)

Filters:
- Status (All, Active, Flagged, Removed)
- Review type
- Sort by date

Empty State: "You haven't submitted any reviews yet"
```

---

### 4.4 Admin Review Moderation Page

**Route:** `/admin/reviews/flagged`  
**Access:** ADMIN role required

#### Features
- View flagged reviews
- Remove reviews that violate policies
- See flag reasons

#### API Endpoints

##### Remove Review
```http
DELETE /api/v1/reviews/{reviewId}?reason=Spam%20content
Authorization: Bearer {token}

Response (200 OK):
{
  "code": 200,
  "message": "Review removed successfully"
}
```

#### UI Components

**Flagged Reviews Table:**
```jsx
Columns:
- Review ID
- Company Name
- Review Type
- Overall Rating
- Flag Count
- Created Date
- Status (FLAGGED badge)
- Actions (View, Remove)

Filters:
- Flag count (>5, >10)
- Review type
- Company filter

Row Actions:
- "View Full Review" → Opens modal with full review content
- "Remove Review" → Opens confirmation modal
  * Requires removal reason
  * POST to DELETE endpoint
  * Success: Status changes to REMOVED
```

---

## 5. Shared Components

### 5.1 Star Rating Component

```jsx
<StarRating 
  value={4.5} 
  max={5} 
  readonly={true}
  onChange={(value) => setRating(value)}
  size="large"  // small, medium, large
  showValue={true}  // Show numeric value next to stars
/>

Props:
- value: number (0-5, supports decimals for display)
- max: number (default: 5)
- readonly: boolean
- onChange: (value) => void
- size: 'small' | 'medium' | 'large'
- showValue: boolean

Visual:
- Empty stars: ☆
- Full stars: ★
- Half stars: ⯨ (for decimal values in readonly mode)
```

### 5.2 Status Badge Component

```jsx
<StatusBadge status="CONFIRMED" type="interview" />

Interview Statuses:
- SCHEDULED: Blue
- CONFIRMED: Green
- COMPLETED: Gray
- CANCELLED: Red
- NO_SHOW: Orange
- RESCHEDULED: Purple

Review Statuses:
- ACTIVE: Green
- FLAGGED: Orange
- REMOVED: Red
- ARCHIVED: Gray

Employment Statuses:
- ACTIVE: Green
- TERMINATED: Gray
```

### 5.3 Conflict Alert Component

```jsx
<ConflictAlert 
  hasConflict={true}
  conflictReason="Interview overlaps with lunch break"
  conflictType="LUNCH_BREAK"
  suggestedAlternatives={["09:00:00", "13:15:00"]}
/>

Conflict Types:
- WORKING_HOURS: ⚠️ "Outside working hours"
- LUNCH_BREAK: 🍽️ "During lunch break"
- EXISTING_INTERVIEW: 📅 "Conflicts with existing interview"
- TIME_OFF: 🏖️ "Recruiter on time-off"
- CANDIDATE_DOUBLE_BOOKING: 👤 "Candidate has another interview"
- BUFFER_VIOLATION: ⏱️ "Insufficient buffer time"
```

### 5.4 Calendar Event Component

```jsx
<CalendarEvent
  startTime="10:00:00"
  endTime="11:00:00"
  title="Interview: John Doe"
  status="CONFIRMED"
  onClick={() => handleEventClick()}
  isDraggable={true}
/>

Visual: Color-coded block with title, time, and status indicator
```

### 5.5 Eligibility Badge Component

```jsx
<EligibilityBadge 
  qualification="HIRED"
  allowedReviewTypes={["APPLICATION_EXPERIENCE", "INTERVIEW_EXPERIENCE", "WORK_EXPERIENCE"]}
/>

Qualifications:
- NOT_ELIGIBLE: 🔒 Gray "Not eligible yet"
- APPLICANT: 📝 Blue "Can review application"
- INTERVIEWED: 💼 Purple "Can review interview"
- HIRED: ⭐ Gold "Can review everything"
- REJECTED: ⚠️ Orange "Limited review"
```

---

## 6. API Error Handling

### Error Response Format
```json
{
  "code": 400,
  "message": "Error description",
  "errors": {
    "field": "Validation error message"
  }
}
```

### Common Error Codes

#### Interview Scheduling Errors
```
INTERVIEW_ALREADY_SCHEDULED (2001): Interview already scheduled for this job application
INVALID_SCHEDULE_DATE (2002): Scheduled date must be in the future
SCHEDULING_CONFLICT (2003): Time slot conflicts with existing schedule
INTERVIEW_NOT_FOUND (2004): Interview not found
INTERVIEW_ALREADY_CONFIRMED (2005): Interview already confirmed by candidate
CANNOT_RESCHEDULE_COMPLETED_INTERVIEW (2006): Cannot reschedule completed interview
RESCHEDULE_TOO_LATE (2007): Cannot reschedule within 2 hours of interview
INTERVIEW_NOT_YET_COMPLETED (2008): Interview has not ended yet
CANNOT_MARK_NO_SHOW_BEFORE_TIME (2009): Cannot mark no-show before interview time
CANNOT_CANCEL_COMPLETED_INTERVIEW (2010): Cannot cancel completed interview
INVALID_DURATION (2011): Duration must be at least 15 minutes
INTERVIEW_TOO_SHORT (2012): Interview completed too early (less than 50% of duration)
RESCHEDULE_REQUEST_NOT_FOUND (2013): Reschedule request not found
RESCHEDULE_REQUEST_ALREADY_PROCESSED (2014): Reschedule request already processed
RESCHEDULE_REQUEST_EXPIRED (2015): Reschedule request has expired
```

#### Calendar Errors
```
INVALID_WORKING_HOURS (2020): Invalid working hours configuration
LUNCH_BREAK_OUTSIDE_WORKING_HOURS (2021): Lunch break must be within working hours
INVALID_BUFFER_MINUTES (2022): Buffer minutes must be 0-60
INVALID_MAX_INTERVIEWS (2023): Max interviews must be 1-20
TIME_OFF_NOT_FOUND (2024): Time-off period not found
TIME_OFF_OVERLAP (2025): Time-off period overlaps with existing period
TIME_OFF_NOT_APPROVED (2026): Time-off not yet approved
```

#### Review Errors
```
REVIEW_ALREADY_SUBMITTED (3001): Already submitted review for this application
UNAUTHORIZED_REVIEW (3002): Not authorized to review this company
REVIEW_NOT_FOUND (3003): Review not found
NOT_ELIGIBLE_TO_REVIEW (3004): Not eligible to review at this stage
INVALID_REVIEW_CONTENT (3005): Review content too short or inappropriate
```

#### Employment Errors
```
EMPLOYMENT_ALREADY_EXISTS (4001): Employment verification already exists
EMPLOYMENT_NOT_FOUND (4002): Employment verification not found
INVALID_EMPLOYMENT_DATES (4003): End date must be after start date
```

### Frontend Error Handling

```jsx
try {
  const response = await fetch('/api/endpoint', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  });

  if (!response.ok) {
    const error = await response.json();
    
    // Display error message to user
    showToast(error.message, 'error');
    
    // Handle specific error codes
    switch (error.code) {
      case 2003: // SCHEDULING_CONFLICT
        showConflictModal(error.message);
        break;
      case 3004: // NOT_ELIGIBLE_TO_REVIEW
        showEligibilityInfo(error.message);
        break;
      default:
        showGenericError(error.message);
    }
    
    return;
  }

  const result = await response.json();
  // Handle success
} catch (err) {
  // Network error
  showToast('Network error. Please try again.', 'error');
}
```

---

## 7. Authentication & Authorization

### Token Management

```jsx
// Store token after login
localStorage.setItem('token', response.token);
localStorage.setItem('userRole', response.role); // ADMIN, RECRUITER, CANDIDATE
localStorage.setItem('userId', response.userId); // CRITICAL: Store user's INTEGER ID (not email!)
localStorage.setItem('userEmail', response.email); // Store separately for display

// Include in all API requests
const headers = {
  'Authorization': `Bearer ${localStorage.getItem('token')}`,
  'Content-Type': 'application/json'
};

// Check token expiration
const isTokenExpired = () => {
  const token = localStorage.getItem('token');
  if (!token) return true;
  
  // Decode JWT and check expiration
  const payload = JSON.parse(atob(token.split('.')[1]));
  return payload.exp * 1000 < Date.now();
};

// Refresh token before expiration
if (isTokenExpired()) {
  await refreshToken();
}
```

### ⚠️ CRITICAL: Dual User Identification System

**The backend uses TWO different identification systems:**

#### 1. REST API Endpoints → INTEGER User IDs

**ALL REST API endpoints use integer database IDs:**
- Interview endpoints: `/api/interviews/recruiter/{recruiterId}`
- Calendar endpoints: `/api/calendar/recruiters/{recruiterId}`
- Employment verification: Uses integer IDs in request bodies
- Company reviews: Uses integer IDs for reviewer identification

```jsx
// ❌ WRONG - Will cause 400 Bad Request errors
const recruiterId = "recruiter1@gmail.com";  // Email string
const candidateId = "NaN";                   // Invalid conversion

// ✅ CORRECT - Use integer IDs from login response
const recruiterId = parseInt(localStorage.getItem('userId'));  // e.g., 10
const candidateId = 42;  // Integer from database

// Common mistake: Don't use email as ID
fetch(`/api/calendar/recruiters/${userEmail}/working-hours`)  // ❌ WRONG
fetch(`/api/calendar/recruiters/${recruiterId}/working-hours`) // ✅ CORRECT
```

#### 2. SSE Notifications → EMAIL Strings

**Real-time notifications use email addresses (automatic from JWT):**

```javascript
// SSE connection - backend extracts email from JWT automatically
const token = localStorage.getItem('token');
const eventSource = new EventSource(
  `http://localhost:8080/api/notifications/stream?token=${token}`
);

// Backend internally uses email for SSE connections
// No need to pass userId - it's extracted from JWT token subject
eventSource.addEventListener('notification', (event) => {
  const notification = JSON.parse(event.data);
  console.log('New notification:', notification);
});
```

**Why the difference?**
- **REST APIs**: Use integer IDs for efficient database lookups
- **SSE Notifications**: Use emails from JWT for simpler authentication

**Example Login Response:**
```json
{
  "token": "eyJhbGc...",
  "userId": 10,          // ← INTEGER: Use for REST API calls
  "email": "recruiter1@gmail.com",  // ← STRING: Auto-used for SSE
  "role": "RECRUITER",
  "name": "John Recruiter"
}
```

**What Frontend Needs to Do:**
1. Store BOTH `userId` (integer) and `email` (string) from login
2. Use `userId` for all REST API path parameters and request bodies
3. SSE notifications work automatically (no action needed)
4. Validate `userId` is a valid integer before API calls

```javascript
// After login
const loginResponse = await fetch('/api/auth/token', {
  method: 'POST',
  body: JSON.stringify({ email, password })
}).then(r => r.json());

// Store BOTH
localStorage.setItem('userId', loginResponse.result.userId);  // Integer
localStorage.setItem('email', loginResponse.result.email);    // String
localStorage.setItem('token', loginResponse.result.token);

// Validate before using
const userId = parseInt(localStorage.getItem('userId'));
if (!userId || isNaN(userId)) {
  console.error('Invalid user ID - please login again');
  window.location.href = '/login';
  return;
}

// Use integer userId for REST APIs
fetch(`/api/interviews/recruiter/${userId}/upcoming`, {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

### Role-Based Route Protection

```jsx
// Route configuration
const routes = [
  {
    path: '/recruiter/interviews',
    component: RecruiterInterviews,
    roles: ['RECRUITER', 'ADMIN']
  },
  {
    path: '/candidate/interviews',
    component: CandidateInterviews,
    roles: ['CANDIDATE']
  },
  {
    path: '/admin/time-off-requests',
    component: AdminTimeOffRequests,
    roles: ['ADMIN']
  }
];

// Protected Route Component
const ProtectedRoute = ({ component: Component, roles }) => {
  const userRole = localStorage.getItem('userRole');
  
  if (!roles.includes(userRole)) {
    return <Navigate to="/unauthorized" />;
  }
  
  return <Component />;
};
```

### API Authorization Headers

```
ADMIN endpoints:
- POST /api/calendar/admin/time-off/{id}/approve
- DELETE /api/v1/reviews/{id}
- All /admin/* routes

RECRUITER endpoints:
- POST /api/job-applies/{id}/schedule-interview
- POST /api/interviews/{id}/complete
- POST /api/calendar/recruiters/{id}/working-hours
- All /recruiter/* routes

CANDIDATE endpoints:
- POST /api/interviews/{id}/confirm
- POST /api/interviews/{id}/reschedule
- POST /api/v1/reviews
- All /candidate/* routes

PUBLIC endpoints (no auth required):
- GET /api/v1/reviews/company/{id}
- GET /api/v1/reviews/company/{id}/rating
- GET /api/v1/reviews/company/{id}/statistics
```

---

## 📝 Implementation Checklist

### Interview Scheduling
- [ ] Create RecruiterInterviews page with table and modal
- [ ] Create CandidateInterviews page with card list
- [ ] Implement interview confirmation flow
- [ ] Add reschedule request/response modals
- [ ] Create interview completion form
- [ ] Add calendar integration (download .ics)
- [ ] Implement real-time countdown timers

### Interview Calendar
- [ ] Create calendar settings page with 7-day grid
- [ ] Implement batch working hours save (single API call)
- [ ] Add time-off request calendar view
- [ ] Create daily/weekly/monthly calendar views
- [ ] Implement conflict checking before scheduling
- [ ] Add available slots finder
- [ ] Create admin time-off approval page

### Employment Verification
- [ ] Create employment management page for recruiters
- [ ] Add employment creation modal
- [ ] Implement employment termination flow
- [ ] Add active employments table with filters

### Company Reviews
- [ ] Create review submission wizard (multi-step)
- [ ] Implement eligibility checker
- [ ] Add company reviews display page with statistics
- [ ] Create candidate's reviews page
- [ ] Add review flagging functionality
- [ ] Implement admin moderation page

### Shared Components
- [ ] Create reusable StarRating component
- [ ] Build StatusBadge component
- [ ] Implement ConflictAlert component
- [ ] Add CalendarEvent component
- [ ] Create EligibilityBadge component

### Testing
- [ ] Test all API integrations
- [ ] Verify role-based access control
- [ ] Test error handling and validation
- [ ] Perform cross-browser testing
- [ ] Test mobile responsiveness

---

## 8. Common Frontend Errors & Solutions

### Error 1: `MethodArgumentTypeMismatchException: For input string: "NaN"`

**Cause:** Frontend sending `"NaN"` string instead of valid integer ID

**Example Error:**
```
Method parameter 'recruiterId': Failed to convert value of type 'java.lang.String' 
to required type 'java.lang.Integer'; For input string: "NaN"
```

**Solutions:**
```jsx
// ❌ WRONG - Results in NaN
const userId = parseInt(undefined);  // NaN
const userId = parseInt(null);       // NaN
const userId = parseInt("");         // NaN

// ✅ CORRECT - Always validate before parseInt
const userId = localStorage.getItem('userId');
if (!userId || userId === 'null' || userId === 'undefined') {
  // Redirect to login
  window.location.href = '/login';
  return;
}
const recruiterId = parseInt(userId);

// ✅ CORRECT - Provide default or handle error
const recruiterId = parseInt(localStorage.getItem('userId')) || null;
if (!recruiterId) {
  showError('Please log in again');
  return;
}
```

---

### Error 2: `InvalidFormatException: Cannot deserialize value of type Integer from String "email@example.com"`

**Cause:** Frontend sending email address instead of integer ID in request body

**Example Error:**
```
Cannot deserialize value of type `java.lang.Integer` from String "recruiter1@gmail.com": 
not a valid `java.lang.Integer` value
```

**Solutions:**
```jsx
// ❌ WRONG - Using email as ID
const requestBody = {
  recruiterId: "recruiter1@gmail.com",  // String email
  workingHoursConfigurations: [...]
};

// ✅ CORRECT - Use integer ID
const requestBody = {
  recruiterId: 10,  // Integer ID from login
  workingHoursConfigurations: [...]
};

// ✅ CORRECT - Get from user context
const currentUser = JSON.parse(localStorage.getItem('user'));
const requestBody = {
  recruiterId: currentUser.userId,  // Integer ID
  workingHoursConfigurations: [...]
};
```

---

### Error 3: `RecycleRequiredException: null` (✅ FIXED in Backend v1.2)

**Cause:** ~~Backend authentication/SSE handler was throwing exceptions incorrectly, causing response conflicts.~~

**Status:** ✅ **COMPLETELY FIXED** - Two backend fixes applied:

**Fix 1: JwtAuthenticationEntryPoint**
```java
// Before (caused crashes during auth failures):
response.getWriter().write(json);
response.flushBuffer(); // ❌ Commits response, blocks error handling

// After (works correctly):
if (response.isCommitted()) return; // ✅ Check first
response.getWriter().write(json);
// ✅ Let Tomcat handle response lifecycle
```

**Fix 2: SSE Controller**
```java
// Before (caused crashes during SSE connection failures):
throw new AppException(ErrorCode.UNAUTHENTICATED); // ❌ Throws exception

// After (works correctly):
if (!response.isCommitted()) {
    response.setStatus(401);
    response.getWriter().write("{\"error\":\"Authentication required\"}");
}
return null; // ✅ Returns null instead of throwing
```

**What was the problem?**
- SSE endpoints can't throw exceptions like regular REST endpoints
- They use `SseEmitter` which has its own response lifecycle
- Throwing exceptions caused Tomcat to try sending multiple responses
- This triggered `RecycleRequiredException`

**Action Required:** 
- **None for frontend** - This was a backend-only issue
- Login and SSE connections work normally after backend restart
- If errors still occur, verify token is valid and not expired

---

### Error 4: 401 Unauthorized

**Cause:** Missing or expired JWT token

**Solutions:**
```jsx
// Check token before API calls
const token = localStorage.getItem('token');
if (!token || isTokenExpired()) {
  // Redirect to login
  window.location.href = '/login';
  return;
}

// Always include Authorization header
const headers = {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
};
```

---

### Error 5: 403 Forbidden

**Cause:** User role doesn't have permission for the endpoint

**Example:** Candidate trying to access recruiter-only endpoint

**Solutions:**
```jsx
// Check role before navigating
const userRole = localStorage.getItem('userRole');
if (userRole !== 'RECRUITER') {
  showError('Access denied');
  return;
}

// Role-based rendering
{userRole === 'RECRUITER' && (
  <Link to="/recruiter/interviews">My Interviews</Link>
)}

{userRole === 'CANDIDATE' && (
  <Link to="/candidate/interviews">My Interviews</Link>
)}
```

---

### Error 6: 404 Not Found

**Cause:** Using wrong ID or resource doesn't exist

**Solutions:**
```jsx
// Always handle 404 responses
try {
  const response = await fetch(`/api/interviews/${interviewId}`);
  if (response.status === 404) {
    showError('Interview not found');
    return;
  }
  const data = await response.json();
} catch (error) {
  showError('Failed to load interview');
}
```

---

### Error 6: Date/Time Format Issues

**Cause:** Sending date in wrong format

**Required Format:** `YYYY-MM-DDTHH:mm:ss` (ISO 8601 without timezone)

**Solutions:**
```jsx
// ❌ WRONG formats
"12/20/2024"                    // US format
"2024-12-20"                    // Date only (missing time)
"2024-12-20T10:00:00Z"         // UTC with Z suffix
"2024-12-20T10:00:00+07:00"    // With timezone offset

// ✅ CORRECT format
"2024-12-20T10:00:00"          // ISO 8601 local time

// Helper function
const formatDateForAPI = (date) => {
  // date is a JavaScript Date object
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
};

// Usage
const scheduledDate = formatDateForAPI(new Date('2024-12-20 10:00:00'));
// Result: "2024-12-20T10:00:00"
```

---

### Error 7: CORS Issues (Development)

**Cause:** Frontend running on different port than backend

**Backend:** http://localhost:8080  
**Frontend:** http://localhost:3000

**Solution:** Configure proxy in frontend

**React (package.json):**
```json
{
  "proxy": "http://localhost:8080"
}
```

**Vue (vue.config.js):**
```js
module.exports = {
  devServer: {
    proxy: 'http://localhost:8080'
  }
}
```

**Angular (proxy.conf.json):**
```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

---

### Debug Checklist

When API calls fail, check these in order:

**1. Network Tab (Browser DevTools)**
```
✓ Request URL correct?
✓ Request method correct (GET, POST, etc.)?
✓ Authorization header present?
✓ Request payload format correct?
✓ Response status code?
```

**2. Request Headers**
```
Authorization: Bearer eyJhbGc...  ✓ Present?
Content-Type: application/json    ✓ Correct?
```

**3. Request Body (for POST/PUT)**
```json
{
  "recruiterId": 10,              ✓ Integer, not string?
  "scheduledDate": "2024-12-20T10:00:00",  ✓ Correct format?
  "durationMinutes": 60           ✓ Integer, not string?
}
```

**4. User Context**
```jsx
console.log('User ID:', localStorage.getItem('userId'));      // Should be "10", not "NaN"
console.log('User Role:', localStorage.getItem('userRole'));  // Should be "RECRUITER", "CANDIDATE", etc.
console.log('Token:', localStorage.getItem('token')?.substring(0, 20));  // Should exist
```

**5. Type Checking**
```jsx
const recruiterId = parseInt(localStorage.getItem('userId'));
console.log('Recruiter ID:', recruiterId, typeof recruiterId);  
// Should log: "Recruiter ID: 10 number"
// NOT: "Recruiter ID: NaN number"
```

---

**Document Version:** 1.1  
**Last Updated:** November 26, 2025 (Added troubleshooting section)  
**Backend Build:** BUILD SUCCESS (484 files, 0 errors)

**Critical Notes for Frontend Team:**
- ⚠️ All `recruiterId`, `candidateId`, `jobApplyId` parameters MUST be integers
- ⚠️ Never send email addresses where IDs are expected
- ⚠️ Always validate user IDs before API calls to avoid "NaN" errors
- ⚠️ Date format MUST be `YYYY-MM-DDTHH:mm:ss` (no timezone suffix)
