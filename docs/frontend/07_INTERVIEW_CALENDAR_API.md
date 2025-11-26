# Interview Calendar API Documentation

**Module**: Calendar & Scheduling Management  
**Base URL**: `/api/calendar`  
**Version**: 1.1  
**Last Updated**: November 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [Data Models](#data-models)
3. [API Endpoints](#api-endpoints)
   - [Working Hours Management](#working-hours-management)
   - [Time-Off Management](#time-off-management)
   - [Conflict Detection](#conflict-detection)
   - [Available Slots](#available-slots)
   - [Calendar Views](#calendar-views)
   - [Statistics](#statistics)
4. [Business Rules](#business-rules)
5. [Error Handling](#error-handling)
6. [Frontend Integration](#frontend-integration)

---

## Overview

The Interview Calendar API provides comprehensive calendar management for interview scheduling. It supports working hours configuration, time-off management, conflict detection, and multiple calendar views (daily/weekly/monthly).

### Key Features
- ✅ Configure recruiter working hours per day of week
- ✅ Manage time-off periods (vacation, sick leave, holidays)
- ✅ Detect scheduling conflicts automatically
- ✅ Calculate available time slots
- ✅ Daily/Weekly/Monthly calendar views
- ✅ Candidate calendar view
- ✅ Buffer time between interviews
- ✅ Daily interview capacity limits
- ✅ Lunch break support
- ✅ Scheduling analytics and statistics

###Real-World Business Rules
- 🕐 **Working Hours**: Different hours per day (e.g., Mon-Thu 9-5, Fri 9-3)
- 🍽️ **Lunch Breaks**: Block calendar during lunch (e.g., 12:00-13:00)
- 🏖️ **Time-Off**: Vacation/sick leave automatically blocks availability
- ⏱️ **Buffer Time**: Enforce minimum gap between interviews (default: 15 min)
- 📊 **Capacity Limits**: Maximum interviews per day (default: 8)
- ⚠️ **Conflict Prevention**: No double-booking for recruiter or candidate
- ✅ **Approval Workflow**: Time-off requires admin approval

---

## Data Models

### Working Hours

#### RecruiterWorkingHoursRequest
```typescript
interface RecruiterWorkingHoursRequest {
  dayOfWeek: DayOfWeek;              // "MONDAY", "TUESDAY", ..., "SUNDAY"
  isWorkingDay: boolean;             // false = day off
  startTime?: string;                 // "09:00" (required if isWorkingDay = true)
  endTime?: string;                   // "17:00" (required if isWorkingDay = true)
  lunchBreakStart?: string;          // "12:00" (optional)
  lunchBreakEnd?: string;            // "13:00" (optional)
  bufferMinutesBetweenInterviews?: number;  // Default: 15
  maxInterviewsPerDay?: number;      // Default: 8
}
```

#### RecruiterWorkingHoursResponse
```typescript
interface RecruiterWorkingHoursResponse {
  id: number;
  recruiterId: number;
  dayOfWeek: DayOfWeek;
  isWorkingDay: boolean;
  startTime: string | null;
  endTime: string | null;
  lunchBreakStart: string | null;
  lunchBreakEnd: string | null;
  bufferMinutesBetweenInterviews: number;
  maxInterviewsPerDay: number;
  totalWorkingMinutes: number;      // Auto-calculated (excludes lunch)
}
```

### Time-Off

#### TimeOffRequest
```typescript
interface TimeOffRequest {
  startDate: string;                // "2024-12-23" (ISO date)
  endDate: string;                  // "2024-12-27" (inclusive)
  timeOffType: TimeOffType;         // "VACATION", "SICK_LEAVE", "PUBLIC_HOLIDAY", etc.
  reason?: string;                   // Optional explanation
}

type TimeOffType =
  | "VACATION"
  | "SICK_LEAVE"
  | "PERSONAL_DAY"
  | "PUBLIC_HOLIDAY"
  | "COMPANY_EVENT"
  | "TRAINING"
  | "OTHER";
```

#### RecruiterTimeOffResponse
```typescript
interface RecruiterTimeOffResponse {
  id: number;
  recruiterId: number;
  startDate: string;
  endDate: string;
  timeOffType: TimeOffType;
  reason: string | null;
  isApproved: boolean;
  approvedByAdminId: number | null;
  approvedAt: string | null;
  createdAt: string;
}
```

### Conflict Detection

#### ConflictCheckRequest
```typescript
interface ConflictCheckRequest {
  recruiterId: number;
  candidateId: number;
  proposedStartTime: string;        // "2024-12-15T10:00:00"
  durationMinutes: number;          // 60
}
```

#### ConflictCheckResponse
```typescript
interface ConflictCheckResponse {
  hasConflict: boolean;
  conflictReason: string | null;    // Summary if hasConflict = true
  conflicts: ConflictDetail[];
}

interface ConflictDetail {
  conflictType: ConflictType;
  conflictStart: string;
  conflictEnd: string;
  conflictingInterviewId: number | null;
  description: string;
}

type ConflictType =
  | "INTERVIEW_OVERLAP"              // Another interview at same time
  | "TIME_OFF"                       // Recruiter has approved time-off
  | "OUTSIDE_WORKING_HOURS"          // Not within configured hours
  | "DURING_LUNCH_BREAK"             // Scheduled during lunch
  | "MAX_INTERVIEWS_REACHED"         // Daily limit exceeded
  | "INSUFFICIENT_BUFFER"            // Not enough gap between interviews
  | "NON_WORKING_DAY";               // Scheduled on day off
```

### Calendar Views

#### DailyCalendarResponse
```typescript
interface DailyCalendarResponse {
  recruiterId: number;
  date: string;                     // "2024-12-15"
  dayOfWeek: string;                // "MONDAY"
  isWorkingDay: boolean;
  workStartTime: string | null;     // "09:00"
  workEndTime: string | null;       // "17:00"
  hasTimeOff: boolean;
  timeOffReason: string | null;
  totalInterviews: number;
  availableSlots: number;           // Count of available time slots
  interviews: InterviewScheduleResponse[];
  availableTimeSlots: string[];     // ["09:00", "10:15", "14:00"]
}
```

#### WeeklyCalendarResponse
```typescript
interface WeeklyCalendarResponse {
  recruiterId: number;
  weekStartDate: string;            // Monday (2024-12-09)
  weekEndDate: string;              // Sunday (2024-12-15)
  totalInterviews: number;
  dailyCalendars: Record<string, DailyCalendarResponse>;  // Date -> Daily view
  allInterviews: InterviewScheduleResponse[];
}
```

#### MonthlyCalendarResponse
```typescript
interface MonthlyCalendarResponse {
  recruiterId: number;
  year: number;                     // 2024
  month: number;                    // 12 (1-12)
  yearMonth: string;                // "2024-12"
  totalInterviews: number;
  interviewCountByDate: Record<string, number>;   // "2024-12-15" -> 5
  workingDays: Record<string, boolean>;           // "2024-12-15" -> true
  timeOffDays: Record<string, boolean>;           // "2024-12-25" -> true
}
```

#### CandidateCalendarResponse
```typescript
interface CandidateCalendarResponse {
  candidateId: number;
  startDate: string;
  endDate: string;
  totalInterviews: number;
  upcomingInterviews: InterviewScheduleResponse[];
}
```

### Statistics

#### RecruiterSchedulingStatsResponse
```typescript
interface RecruiterSchedulingStatsResponse {
  recruiterId: number;
  startDate: string;
  endDate: string;
  
  // Interview counts
  totalInterviewsScheduled: number;
  completedInterviews: number;
  cancelledInterviews: number;
  noShowInterviews: number;
  rescheduledInterviews: number;
  
  // Time metrics
  totalInterviewHours: number;
  averageInterviewDurationMinutes: number;
  utilizationRate: number;          // Percentage (0-100)
  
  // Busiest times
  busiestDayOfWeek: string;         // "WEDNESDAY"
  busiestTimeSlot: string;          // "10:00-11:00"
  busiestDate: string;              // "2024-12-15"
  
  // Outcomes
  passedInterviews: number;
  failedInterviews: number;
  passRate: number;                 // Percentage (0-100)
  
  // Distribution
  interviewsByType: Record<string, number>;      // "VIDEO_CALL" -> 25
  interviewsByStatus: Record<string, number>;    // "COMPLETED" -> 30
}
```

---

## API Endpoints

### Working Hours Management

#### 1. Set Working Hours
**POST** `/api/recruiters/{recruiterId}/working-hours`

Set or update working hours configuration for a specific day of week.

**Request Headers:**
```http
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "dayOfWeek": "MONDAY",
  "isWorkingDay": true,
  "startTime": "09:00",
  "endTime": "17:00",
  "lunchBreakStart": "12:00",
  "lunchBreakEnd": "13:00",
  "bufferMinutesBetweenInterviews": 15,
  "maxInterviewsPerDay": 8
}
```

**Response:** `200 OK`
```json
{
  "id": 123,
  "recruiterId": 456,
  "dayOfWeek": "MONDAY",
  "isWorkingDay": true,
  "startTime": "09:00",
  "endTime": "17:00",
  "lunchBreakStart": "12:00",
  "lunchBreakEnd": "13:00",
  "bufferMinutesBetweenInterviews": 15,
  "maxInterviewsPerDay": 8,
  "totalWorkingMinutes": 420
}
```

**Business Logic:**
- Automatically calculates `totalWorkingMinutes` (excludes lunch)
- Upserts: Creates if doesn't exist, updates if exists
- Monday 9-5 with 1h lunch = 7 hours = 420 minutes

---

#### 2. Get Working Hours
**GET** `/api/recruiters/{recruiterId}/working-hours`

Get all working hours configuration (7 days).

**Response:** `200 OK`
```json
[
  {
    "id": 123,
    "dayOfWeek": "MONDAY",
    "isWorkingDay": true,
    "startTime": "09:00",
    "endTime": "17:00",
    "totalWorkingMinutes": 420
  },
  {
    "id": 124,
    "dayOfWeek": "FRIDAY",
    "isWorkingDay": true,
    "startTime": "09:00",
    "endTime": "15:00",
    "totalWorkingMinutes": 360
  },
  {
    "id": 125,
    "dayOfWeek": "SATURDAY",
    "isWorkingDay": false,
    "startTime": null,
    "endTime": null,
    "totalWorkingMinutes": 0
  }
]
```

---

### Time-Off Management

#### 3. Request Time-Off
**POST** `/api/recruiters/{recruiterId}/time-off`

Submit a time-off request. Requires admin approval before affecting calendar.

**Request Body:**
```json
{
  "startDate": "2024-12-23",
  "endDate": "2024-12-27",
  "timeOffType": "VACATION",
  "reason": "Year-end holidays"
}
```

**Response:** `201 Created`
```json
{
  "id": 789,
  "recruiterId": 456,
  "startDate": "2024-12-23",
  "endDate": "2024-12-27",
  "timeOffType": "VACATION",
  "reason": "Year-end holidays",
  "isApproved": false,
  "approvedByAdminId": null,
  "approvedAt": null,
  "createdAt": "2024-12-10T10:30:00"
}
```

---

#### 4. Get Time-Off Periods
**GET** `/api/recruiters/{recruiterId}/time-off`

Get all time-off periods (approved and pending).

**Query Parameters:**
- `approved`: boolean (optional) - Filter by approval status

**Response:** `200 OK`
```json
[
  {
    "id": 789,
    "startDate": "2024-12-23",
    "endDate": "2024-12-27",
    "timeOffType": "VACATION",
    "isApproved": true,
    "approvedAt": "2024-12-11T09:00:00"
  },
  {
    "id": 790,
    "startDate": "2025-01-15",
    "endDate": "2025-01-15",
    "timeOffType": "PERSONAL_DAY",
    "isApproved": false,
    "approvedAt": null
  }
]
```

---

#### 5. Approve Time-Off (Admin Only)
**POST** `/api/admin/time-off/{timeOffId}/approve`

Admin approves a time-off request.

**Response:** `200 OK`
```json
{
  "id": 789,
  "isApproved": true,
  "approvedByAdminId": 123,
  "approvedAt": "2024-12-11T09:00:00"
}
```

---

#### 6. Cancel Time-Off
**DELETE** `/api/time-off/{timeOffId}`

Cancel a time-off request (before or after approval).

**Response:** `204 No Content`

---

### Conflict Detection

#### 7. Check Scheduling Conflict
**POST** `/api/calendar/check-conflict`

Check if proposed interview time would create any conflicts.

**Request Body:**
```json
{
  "recruiterId": 456,
  "candidateId": 789,
  "proposedStartTime": "2024-12-15T10:00:00",
  "durationMinutes": 60
}
```

**Response (No Conflict):** `200 OK`
```json
{
  "hasConflict": false,
  "conflictReason": null,
  "conflicts": []
}
```

**Response (With Conflicts):** `200 OK`
```json
{
  "hasConflict": true,
  "conflictReason": "Multiple conflicts detected: Interview overlap, Outside working hours",
  "conflicts": [
    {
      "conflictType": "INTERVIEW_OVERLAP",
      "conflictStart": "2024-12-15T10:30:00",
      "conflictEnd": "2024-12-15T11:30:00",
      "conflictingInterviewId": 999,
      "description": "Overlaps with interview for 'Senior Java Developer' position (30 minutes overlap)"
    },
    {
      "conflictType": "OUTSIDE_WORKING_HOURS",
      "conflictStart": "2024-12-15T10:00:00",
      "conflictEnd": "2024-12-15T11:00:00",
      "conflictingInterviewId": null,
      "description": "Interview extends beyond working hours (work ends at 17:00)"
    }
  ]
}
```

**Conflict Detection Rules:**
1. **INTERVIEW_OVERLAP**: Recruiter or candidate has another interview
2. **TIME_OFF**: Recruiter has approved time-off on that date
3. **OUTSIDE_WORKING_HOURS**: Not within recruiter's configured hours
4. **DURING_LUNCH_BREAK**: Falls within lunch break time
5. **MAX_INTERVIEWS_REACHED**: Recruiter already at daily limit
6. **INSUFFICIENT_BUFFER**: Less than required buffer time from another interview
7. **NON_WORKING_DAY**: Scheduled on a day recruiter doesn't work

---

### Available Slots

#### 8. Get Available Time Slots
**GET** `/api/calendar/recruiters/{recruiterId}/available-slots`

Get list of available start times for a specific date.

**Query Parameters:**
- `date`: string (required) - "2024-12-15"
- `duration`: number (required) - Interview duration in minutes

**Response:** `200 OK`
```json
{
  "recruiterId": 456,
  "date": "2024-12-15",
  "durationMinutes": 60,
  "availableSlots": [
    "09:00",
    "10:15",
    "14:00",
    "15:30"
  ],
  "totalSlotsAvailable": 4,
  "workStartTime": "09:00",
  "workEndTime": "17:00",
  "lunchBreakStart": "12:00",
  "lunchBreakEnd": "13:00"
}
```

**Slot Calculation Logic:**
1. Check if date is working day → Return empty if not
2. Check for approved time-off → Return empty if time-off
3. Get working hours for day of week
4. Generate slots from start to end time (in 15-min intervals)
5. Exclude lunch break slots
6. Exclude slots overlapping with existing interviews
7. Apply buffer time between interviews
8. Filter slots where interview would fit before next interview/end time
9. Return remaining available slots

**Example:**
- Working hours: 9:00 - 17:00
- Lunch: 12:00 - 13:00
- Buffer: 15 min
- Existing interviews: 10:30-11:30, 14:00-15:00
- Duration: 60 min

Available slots:
- `09:00` ✅ (ends 10:00, 15min buffer before 10:30 interview)
- `10:15` ❌ (would end 11:15, overlaps with 10:30 interview)
- `11:45` ❌ (ends 12:45, overlaps with lunch)
- `13:00` ❌ (immediately after lunch, no buffer)
- `13:15` ❌ (ends 14:15, overlaps with 14:00 interview)
- `15:15` ✅ (15min buffer after 15:00 interview, ends 16:15)

---

#### 9. Get Available Dates
**GET** `/api/calendar/recruiters/{recruiterId}/available-dates`

Get list of dates that have at least one available slot.

**Query Parameters:**
- `startDate`: string (required) - "2024-12-01"
- `endDate`: string (required) - "2024-12-31"
- `duration`: number (required) - Interview duration in minutes

**Response:** `200 OK`
```json
{
  "recruiterId": 456,
  "startDate": "2024-12-01",
  "endDate": "2024-12-31",
  "durationMinutes": 60,
  "availableDates": [
    "2024-12-02",
    "2024-12-03",
    "2024-12-05",
    "2024-12-09",
    "2024-12-10"
  ],
  "totalDatesAvailable": 5
}
```

**Use Case:** Calendar date picker can disable dates with no availability.

---

### Calendar Views

#### 10. Get Daily Calendar
**GET** `/api/calendar/recruiters/{recruiterId}/daily`

Get detailed view of a single day.

**Query Parameters:**
- `date`: string (required) - "2024-12-15"

**Response:** `200 OK`
```json
{
  "recruiterId": 456,
  "date": "2024-12-15",
  "dayOfWeek": "MONDAY",
  "isWorkingDay": true,
  "workStartTime": "09:00",
  "workEndTime": "17:00",
  "hasTimeOff": false,
  "timeOffReason": null,
  "totalInterviews": 5,
  "availableSlots": 3,
  "interviews": [
    {
      "interviewId": 123,
      "scheduledDate": "2024-12-15T09:00:00",
      "durationMinutes": 60,
      "interviewType": "VIDEO_CALL",
      "candidateName": "John Doe",
      "jobTitle": "Senior Java Developer",
      "status": "CONFIRMED"
    },
    {
      "interviewId": 124,
      "scheduledDate": "2024-12-15T10:30:00",
      "durationMinutes": 90,
      "interviewType": "IN_PERSON",
      "candidateName": "Jane Smith",
      "jobTitle": "DevOps Engineer",
      "status": "SCHEDULED"
    }
  ],
  "availableTimeSlots": [
    "14:00",
    "15:30",
    "16:00"
  ]
}
```

---

#### 11. Get Weekly Calendar
**GET** `/api/calendar/recruiters/{recruiterId}/weekly`

Get 7-day calendar view.

**Query Parameters:**
- `weekStartDate`: string (required) - Monday of the week (e.g., "2024-12-09")

**Response:** `200 OK`
```json
{
  "recruiterId": 456,
  "weekStartDate": "2024-12-09",
  "weekEndDate": "2024-12-15",
  "totalInterviews": 18,
  "dailyCalendars": {
    "2024-12-09": {
      "date": "2024-12-09",
      "dayOfWeek": "MONDAY",
      "totalInterviews": 5,
      "availableSlots": 2,
      "interviews": [...]
    },
    "2024-12-10": {
      "date": "2024-12-10",
      "dayOfWeek": "TUESDAY",
      "totalInterviews": 3,
      "availableSlots": 4,
      "interviews": [...]
    }
  },
  "allInterviews": [
    // All 18 interviews sorted chronologically
  ]
}
```

---

#### 12. Get Monthly Calendar
**GET** `/api/calendar/recruiters/{recruiterId}/monthly`

Get month-level overview.

**Query Parameters:**
- `year`: number (required) - 2024
- `month`: number (required) - 12 (1-12)

**Response:** `200 OK`
```json
{
  "recruiterId": 456,
  "year": 2024,
  "month": 12,
  "yearMonth": "2024-12",
  "totalInterviews": 45,
  "interviewCountByDate": {
    "2024-12-02": 3,
    "2024-12-03": 5,
    "2024-12-04": 2,
    "2024-12-05": 4
  },
  "workingDays": {
    "2024-12-02": true,
    "2024-12-03": true,
    "2024-12-07": false,
    "2024-12-08": false
  },
  "timeOffDays": {
    "2024-12-25": true,
    "2024-12-26": true
  }
}
```

**Frontend Use Case:** Display calendar heatmap:
- Green cells for working days
- Red cells for time-off
- Number badge showing interview count
- Gray cells for non-working days

---

#### 13. Get Candidate Calendar
**GET** `/api/calendar/candidates/{candidateId}/calendar`

Get candidate's upcoming interviews across all companies.

**Query Parameters:**
- `startDate`: string (optional) - Default: today
- `endDate`: string (optional) - Default: 30 days from today

**Response:** `200 OK`
```json
{
  "candidateId": 789,
  "startDate": "2024-12-15",
  "endDate": "2025-01-15",
  "totalInterviews": 3,
  "upcomingInterviews": [
    {
      "interviewId": 123,
      "scheduledDate": "2024-12-18T10:00:00",
      "companyName": "TechCorp",
      "jobTitle": "Senior Java Developer",
      "interviewType": "VIDEO_CALL",
      "status": "CONFIRMED"
    },
    {
      "interviewId": 124,
      "scheduledDate": "2024-12-20T14:00:00",
      "companyName": "StartupXYZ",
      "jobTitle": "Full Stack Developer",
      "interviewType": "IN_PERSON",
      "status": "SCHEDULED"
    }
  ]
}
```

---

### Statistics

#### 14. Get Scheduling Statistics
**GET** `/api/calendar/recruiters/{recruiterId}/statistics`

Get comprehensive scheduling analytics.

**Query Parameters:**
- `startDate`: string (required) - "2024-12-01"
- `endDate`: string (required) - "2024-12-31"

**Response:** `200 OK`
```json
{
  "recruiterId": 456,
  "startDate": "2024-12-01",
  "endDate": "2024-12-31",
  "totalInterviewsScheduled": 85,
  "completedInterviews": 70,
  "cancelledInterviews": 10,
  "noShowInterviews": 5,
  "rescheduledInterviews": 12,
  "totalInterviewHours": 95,
  "averageInterviewDurationMinutes": 65,
  "utilizationRate": 42.5,
  "busiestDayOfWeek": "WEDNESDAY",
  "busiestTimeSlot": "10:00-11:00",
  "busiestDate": "2024-12-15",
  "passedInterviews": 45,
  "failedInterviews": 25,
  "passRate": 64.3,
  "interviewsByType": {
    "VIDEO_CALL": 50,
    "IN_PERSON": 25,
    "PHONE": 10
  },
  "interviewsByStatus": {
    "COMPLETED": 70,
    "CANCELLED": 10,
    "NO_SHOW": 5
  }
}
```

**Utilization Rate Calculation:**
```
utilizationRate = (totalInterviewHours / totalWorkingHours) * 100
Example: (95 hours / 224 hours) * 100 = 42.5%
```

---

## Business Rules

### Working Hours Configuration

| Rule | Description | Enforcement |
|------|-------------|-------------|
| **Unique Day Configuration** | Each recruiter can have only one configuration per day of week | Database unique constraint |
| **Non-Working Days** | If `isWorkingDay = false`, start/end times must be null | Validation on save |
| **Time Validity** | `endTime` must be after `startTime` | Validation |
| **Lunch Break** | If lunch configured, must be within working hours | Validation |
| **Buffer Time** | Must be 0-60 minutes | Validation (default: 15) |
| **Max Interviews** | Must be 1-20 per day | Validation (default: 8) |

### Time-Off Rules

| Rule | Description | Enforcement |
|------|-------------|-------------|
| **Date Range Validity** | `endDate` must be >= `startDate` | Validation |
| **Approval Required** | Time-off doesn't affect calendar until approved | Checked in availability logic |
| **Retroactive Time-Off** | Can request past dates (for record-keeping) | Allowed |
| **Overlapping Time-Off** | Can have overlapping periods (e.g., VACATION + PUBLIC_HOLIDAY) | Allowed |
| **Cancellation** | Can cancel even after approval | Soft delete |

### Conflict Detection Rules

| Conflict Type | Description | Prevention |
|---------------|-------------|------------|
| **INTERVIEW_OVERLAP** | Another interview within time range | Check existing interviews |
| **TIME_OFF** | Approved time-off on date | Query time-off table |
| **OUTSIDE_WORKING_HOURS** | Not within configured hours | Check working hours |
| **DURING_LUNCH_BREAK** | Overlaps with lunch time | Check lunch break config |
| **MAX_INTERVIEWS_REACHED** | Already at daily limit | Count interviews on date |
| **INSUFFICIENT_BUFFER** | Less than buffer time from another interview | Check gaps between interviews |
| **NON_WORKING_DAY** | Day marked as non-working | Check `isWorkingDay` flag |

### Available Slots Calculation

**Algorithm:**
1. **Date Validation**: Check if date is in future
2. **Working Day Check**: Return empty if non-working day
3. **Time-Off Check**: Return empty if approved time-off exists
4. **Get Working Hours**: Retrieve config for day of week
5. **Generate Candidate Slots**: Create 15-minute intervals from start to end
6. **Exclude Lunch**: Remove slots during lunch break
7. **Get Existing Interviews**: Query all interviews on date
8. **Apply Buffer Time**: Add buffer before/after each interview
9. **Check Duration Fit**: Ensure interview fits before next event
10. **Return Available**: Return slots passing all checks

**Example Calculation:**
```
Working Hours: 09:00 - 17:00 (8 hours = 480 minutes)
Lunch: 12:00 - 13:00 (1 hour)
Buffer: 15 minutes
Duration: 60 minutes
Existing: 10:30-11:30, 14:00-15:00

Slot Evaluation:
- 09:00: ✅ Available (ends 10:00, 15min buffer before 10:30)
- 09:15: ❌ No buffer (ends 10:15, only 0min before 10:30)
- 10:30: ❌ Occupied
- 11:45: ❌ Overlaps lunch (ends 12:45)
- 13:00: ❌ No buffer after lunch
- 13:15: ❌ Overlaps 14:00 interview (ends 14:15)
- 15:15: ✅ Available (15min buffer after 15:00, ends 16:15)
- 16:00: ✅ Available (ends 17:00 exactly)
- 16:15: ❌ Extends past end time (would end 17:15)

Result: ["09:00", "15:15", "16:00"]
```

### Calendar View Rules

| View | Data Included | Use Case |
|------|---------------|----------|
| **Daily** | All interviews + available slots | Detailed day planning |
| **Weekly** | 7 days from Monday | Week-at-a-glance |
| **Monthly** | Interview counts per day | High-level overview |
| **Candidate** | Cross-company interviews | Candidate personal calendar |

---

## Error Handling

### Common Error Responses

#### 400 Bad Request - Invalid Time Range
```json
{
  "error": "INVALID_TIME_RANGE",
  "message": "End time must be after start time",
  "field": "endTime",
  "timestamp": "2024-12-15T10:30:00Z"
}
```

#### 400 Bad Request - Invalid Date Range
```json
{
  "error": "INVALID_DATE_RANGE",
  "message": "End date must be on or after start date",
  "field": "endDate"
}
```

#### 409 Conflict - Scheduling Conflict
```json
{
  "error": "SCHEDULING_CONFLICT",
  "message": "Cannot schedule interview: recruiter has another interview at this time",
  "conflictDetails": {
    "conflictType": "INTERVIEW_OVERLAP",
    "conflictingInterviewId": 999,
    "proposedTime": "2024-12-15T10:00:00",
    "conflictingTime": "2024-12-15T10:30:00"
  }
}
```

#### 403 Forbidden - Unauthorized
```json
{
  "error": "FORBIDDEN",
  "message": "Only admins can approve time-off requests"
}
```

#### 404 Not Found - Working Hours Not Configured
```json
{
  "error": "WORKING_HOURS_NOT_CONFIGURED",
  "message": "Recruiter has not configured working hours. Please set up working hours before scheduling interviews.",
  "recruiterId": 456
}
```

---

## Frontend Integration

### Setting Up Working Hours (Admin/Recruiter)

```typescript
// Step 1: Configure working hours for all days
const workingDaysConfig = [
  {
    dayOfWeek: "MONDAY",
    isWorkingDay: true,
    startTime: "09:00",
    endTime: "17:00",
    lunchBreakStart: "12:00",
    lunchBreakEnd: "13:00",
    bufferMinutesBetweenInterviews: 15,
    maxInterviewsPerDay: 8
  },
  {
    dayOfWeek: "FRIDAY",
    isWorkingDay: true,
    startTime: "09:00",
    endTime: "15:00",  // Shorter Friday
    bufferMinutesBetweenInterviews: 15,
    maxInterviewsPerDay: 5
  },
  {
    dayOfWeek: "SATURDAY",
    isWorkingDay: false  // Weekend off
  },
  {
    dayOfWeek: "SUNDAY",
    isWorkingDay: false
  }
];

// POST each day configuration
for (const config of workingDaysConfig) {
  await axios.post(`/api/recruiters/${recruiterId}/working-hours`, config);
}
```

### Building a Time Slot Picker

```typescript
// Component: AvailableTimePicker.tsx
async function loadAvailableSlots(date: string, duration: number) {
  const response = await axios.get(
    `/api/calendar/recruiters/${recruiterId}/available-slots`,
    { params: { date, duration } }
  );
  
  return response.data.availableSlots; // ["09:00", "10:15", "14:00"]
}

// Render time picker with only available slots
<select>
  {availableSlots.map(time => (
    <option key={time} value={time}>
      {time}
    </option>
  ))}
</select>
```

### Checking Conflicts Before Scheduling

```typescript
// Before final submission
async function validateScheduling(
  recruiterId: number,
  candidateId: number,
  dateTime: string,
  duration: number
) {
  const response = await axios.post('/api/calendar/check-conflict', {
    recruiterId,
    candidateId,
    proposedStartTime: dateTime,
    durationMinutes: duration
  });
  
  if (response.data.hasConflict) {
    // Show conflict warnings to user
    alert(`Conflicts detected:\n${response.data.conflicts.map(c => c.description).join('\n')}`);
    return false;
  }
  
  return true;  // Safe to schedule
}
```

### Building a Calendar Grid

```typescript
// Component: RecruiterCalendar.tsx
async function loadMonthlyCalendar(year: number, month: number) {
  const response = await axios.get(
    `/api/calendar/recruiters/${recruiterId}/monthly`,
    { params: { year, month } }
  );
  
  const { interviewCountByDate, workingDays, timeOffDays } = response.data;
  
  // Render calendar grid
  return daysInMonth.map(date => {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(date).padStart(2, '0')}`;
    const count = interviewCountByDate[dateStr] || 0;
    const isWorking = workingDays[dateStr];
    const isTimeOff = timeOffDays[dateStr];
    
    return (
      <CalendarCell
        date={date}
        interviewCount={count}
        isWorkingDay={isWorking}
        hasTimeOff={isTimeOff}
        onClick={() => loadDailyView(dateStr)}
      />
    );
  });
}
```

### Requesting Time-Off

```typescript
// Component: TimeOffRequestForm.tsx
async function submitTimeOff(data: TimeOffRequest) {
  try {
    const response = await axios.post(
      `/api/recruiters/${recruiterId}/time-off`,
      data
    );
    
    // Show success message
    alert('Time-off request submitted. Awaiting admin approval.');
    
    // Response includes isApproved: false (pending)
    return response.data;
  } catch (error) {
    if (error.response?.status === 409) {
      alert('Conflicting time-off period exists');
    }
  }
}
```

### Building Statistics Dashboard

```typescript
// Component: RecruiterStatsDashboard.tsx
async function loadStatistics(startDate: string, endDate: string) {
  const response = await axios.get(
    `/api/calendar/recruiters/${recruiterId}/statistics`,
    { params: { startDate, endDate } }
  );
  
  const stats = response.data;
  
  return (
    <Dashboard>
      <MetricCard 
        title="Total Interviews"
        value={stats.totalInterviewsScheduled}
        trend={calculateTrend(stats)}
      />
      <MetricCard 
        title="Pass Rate"
        value={`${stats.passRate.toFixed(1)}%`}
        color={stats.passRate > 50 ? 'green' : 'red'}
      />
      <MetricCard 
        title="Utilization"
        value={`${stats.utilizationRate.toFixed(1)}%`}
        subtitle="of working hours"
      />
      <ChartCard 
        title="Busiest Times"
        data={{
          day: stats.busiestDayOfWeek,
          time: stats.busiestTimeSlot,
          date: stats.busiestDate
        }}
      />
    </Dashboard>
  );
}
```

---

## Frontend Checklist

### Required UI Components

**Working Hours Setup:**
- [ ] Week view form (7 day configurations)
- [ ] Time picker for start/end times
- [ ] Lunch break toggle and time inputs
- [ ] Buffer time slider (0-60 min)
- [ ] Max interviews per day input (1-20)
- [ ] Save/reset buttons

**Time-Off Management:**
- [ ] Time-off request form (date range, type, reason)
- [ ] Time-off list (pending/approved filter)
- [ ] Admin approval interface
- [ ] Cancel time-off button
- [ ] Visual calendar with time-off overlays

**Interview Scheduling:**
- [ ] Date picker (disable non-available dates)
- [ ] Available time slot dropdown (only show valid times)
- [ ] Conflict warning modal (if detected)
- [ ] Duration selector (30/45/60/90 min)
- [ ] Real-time availability check
- [ ] Validation before submission

**Calendar Views:**
- [ ] Daily schedule view (timeline with interviews)
- [ ] Weekly calendar grid (7 days with interview counts)
- [ ] Monthly calendar heatmap (color-coded by load)
- [ ] Candidate calendar (upcoming interviews list)
- [ ] Filter by status/type
- [ ] Click to view details

**Statistics Dashboard:**
- [ ] Interview count metrics
- [ ] Pass/fail rate charts
- [ ] Utilization gauge
- [ ] Busiest times visualization
- [ ] Type/status distribution pie charts
- [ ] Date range selector

**Edge Cases to Handle:**
- [ ] No working hours configured → Prompt to set up
- [ ] Time-off pending approval → Show warning
- [ ] Daily limit reached → Block new scheduling
- [ ] Lunch break conflict → Suggest alternate times
- [ ] Buffer time violation → Auto-adjust suggested times

---

## Summary

The Interview Calendar API provides a complete solution for real-world interview scheduling with:

✅ **Flexible working hours per day**  
✅ **Time-off management with approval workflow**  
✅ **Comprehensive conflict detection (7 types)**  
✅ **Intelligent available slot calculation**  
✅ **Multiple calendar views (daily/weekly/monthly)**  
✅ **Scheduling analytics and insights**  
✅ **Business rule enforcement (buffer time, daily limits)**  
✅ **Candidate calendar across companies**

**Integration Flow:**
1. Admin/Recruiter configures working hours
2. System calculates available slots automatically
3. Frontend displays only valid time options
4. Conflict check before final scheduling
5. Calendar views provide visibility
6. Analytics track performance

**Next Steps:**
- Implement service layer (`InterviewCalendarServiceImpl`)
- Create REST controller (`InterviewCalendarController`)
- Add database migrations
- Integrate conflict checks into scheduling flow
- Build frontend calendar components
