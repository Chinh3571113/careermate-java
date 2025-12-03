# 📅 Calendar & Schedule Feature Guide

## 🔄 Recent Changes

### ❌ Time-Off Feature Removed

The time-off request feature has been **completely removed** from the system. Recruiters now only manage their working hours, which represent when they are available for interviews according to their company schedule.

**Removed Endpoints:**
- ❌ `POST /api/calendar/recruiters/time-off`
- ❌ `GET /api/calendar/recruiters/time-off`
- ❌ `POST /api/calendar/admin/time-off/{timeOffId}/approve`
- ❌ `DELETE /api/calendar/time-off/{timeOffId}`

**Why Removed:**
Recruiters set their working hours to reflect their actual availability. They work according to their company schedule, not personal time-off requests.

---

## 📋 Table of Contents

1. [Working Hours Management](#working-hours-management)
2. [Calendar Views](#calendar-views)
3. [Availability & Scheduling](#availability--scheduling)
4. [Candidate Application Status](#candidate-application-status)
5. [Data Formats](#data-formats)
6. [Common Issues](#common-issues)

---

## 🕐 Working Hours Management

### Purpose
Recruiters configure their weekly work schedule to define when they are available for conducting interviews.

### Endpoints

#### 1️⃣ Set Working Hours (Single Day)
```http
POST /api/calendar/recruiters/working-hours
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{
  "dayOfWeek": "MONDAY",
  "isWorkingDay": true,
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "lunchBreakStart": "12:00:00",
  "lunchBreakEnd": "13:00:00",
  "bufferMinutesBetweenInterviews": 15,
  "maxInterviewsPerDay": 8
}
```

**Response:**
```json
{
  "id": 1,
  "recruiterId": 123,
  "dayOfWeek": "MONDAY",
  "isWorkingDay": true,
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "lunchBreakStart": "12:00:00",
  "lunchBreakEnd": "13:00:00",
  "bufferMinutesBetweenInterviews": 15,
  "maxInterviewsPerDay": 8
}
```

**⚠️ Important:**
- Times must be in `"HH:MM:SS"` string format, NOT objects!
- Backend automatically gets recruiterId from JWT token
- Can update existing working hours by sending new data for same dayOfWeek

#### 2️⃣ Get Working Hours
```http
GET /api/calendar/recruiters/working-hours
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
[
  {
    "id": 1,
    "recruiterId": 123,
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
    "id": 2,
    "recruiterId": 123,
    "dayOfWeek": "TUESDAY",
    "isWorkingDay": true,
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "lunchBreakStart": "12:00:00",
    "lunchBreakEnd": "13:00:00",
    "bufferMinutesBetweenInterviews": 15,
    "maxInterviewsPerDay": 8
  }
]
```

**⚠️ Response Format:**
- Returns array **directly**, NOT wrapped in `{result: [...]}`
- Empty array `[]` if no working hours configured yet
- Use: `const hours = await response.json()` (hours IS the array)

#### 3️⃣ Batch Set Working Hours
```http
POST /api/calendar/recruiters/working-hours/batch
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{
  "workingHoursConfigurations": [
    {
      "dayOfWeek": "MONDAY",
      "isWorkingDay": true,
      "startTime": "09:00:00",
      "endTime": "17:00:00",
      "lunchBreakStart": "12:00:00",
      "lunchBreakEnd": "13:00:00"
    },
    {
      "dayOfWeek": "TUESDAY",
      "isWorkingDay": true,
      "startTime": "09:00:00",
      "endTime": "17:00:00"
    },
    {
      "dayOfWeek": "SATURDAY",
      "isWorkingDay": false
    },
    {
      "dayOfWeek": "SUNDAY",
      "isWorkingDay": false
    }
  ],
  "replaceAll": false
}
```

**⚠️ IMPORTANT - Breaking Change:**
- **NO `recruiterId` in request body!** Backend gets it from JWT automatically
- Just send `workingHoursConfigurations` array directly
- Optional `replaceAll`: true = mark unspecified days as non-working

**Response:**
```json
{
  "recruiterId": 123,
  "totalConfigurations": 4,
  "successfulUpdates": 4,
  "failedUpdates": 0,
  "updatedConfigurations": [
    {
      "id": 1,
      "recruiterId": 123,
      "dayOfWeek": "MONDAY",
      "isWorkingDay": true,
      "startTime": "09:00:00",
      "endTime": "17:00:00",
      "lunchBreakStart": "12:00:00",
      "lunchBreakEnd": "13:00:00",
      "bufferMinutesBetweenInterviews": 15,
      "maxInterviewsPerDay": 8
    }
  ],
  "errors": {}
}
```

---

## 📅 Calendar Views

### 1️⃣ Daily Calendar View

```http
GET /api/calendar/recruiters/{recruiterId}/daily?date=2024-12-25
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
{
  "recruiterId": 123,
  "date": "2024-12-25",
  "dayOfWeek": "MONDAY",
  "isWorkingDay": true,
  "workStartTime": "09:00:00",
  "workEndTime": "17:00:00",
  "totalInterviews": 3,
  "availableSlots": 5,
  "interviews": [
    {
      "id": 1,
      "candidateId": 456,
      "candidateName": "John Doe",
      "startTime": "2024-12-25T09:00:00",
      "endTime": "2024-12-25T10:00:00",
      "status": "SCHEDULED",
      "jobTitle": "Software Engineer"
    }
  ],
  "availableTimeSlots": ["10:15:00", "11:15:00", "14:00:00", "15:00:00", "16:00:00"]
}
```

**Use Case:** View recruiter's schedule for a specific day

### 2️⃣ Weekly Calendar View

```http
GET /api/calendar/recruiters/{recruiterId}/weekly?weekStartDate=2024-12-23
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
{
  "recruiterId": 123,
  "weekStartDate": "2024-12-23",
  "weekEndDate": "2024-12-29",
  "totalInterviews": 12,
  "days": [
    {
      "date": "2024-12-23",
      "dayOfWeek": "MONDAY",
      "isWorkingDay": true,
      "interviewCount": 3,
      "hasAvailability": true
    },
    {
      "date": "2024-12-24",
      "dayOfWeek": "TUESDAY",
      "isWorkingDay": true,
      "interviewCount": 2,
      "hasAvailability": true
    }
  ]
}
```

**Use Case:** Overview of recruiter's week

### 3️⃣ Monthly Calendar View

```http
GET /api/calendar/recruiters/{recruiterId}/monthly?year=2024&month=12
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
{
  "recruiterId": 123,
  "year": 2024,
  "month": 12,
  "yearMonth": "2024-12",
  "totalInterviews": 45,
  "interviewCountByDate": {
    "2024-12-01": 2,
    "2024-12-02": 3,
    "2024-12-03": 1
  },
  "workingDays": {
    "2024-12-01": true,
    "2024-12-02": true,
    "2024-12-03": true,
    "2024-12-07": false,
    "2024-12-08": false
  }
}
```

**Use Case:** Monthly overview for planning

### 4️⃣ Candidate Calendar View

```http
GET /api/calendar/candidates/{candidateId}/calendar?startDate=2024-12-01&endDate=2024-12-31
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
{
  "candidateId": 456,
  "startDate": "2024-12-01",
  "endDate": "2024-12-31",
  "upcomingInterviews": [
    {
      "id": 1,
      "recruiterId": 123,
      "recruiterName": "Jane Smith",
      "companyName": "Tech Corp",
      "jobTitle": "Senior Developer",
      "startTime": "2024-12-25T09:00:00",
      "endTime": "2024-12-25T10:00:00",
      "status": "SCHEDULED",
      "location": "Office - Room 301",
      "meetingLink": "https://meet.google.com/abc-defg-hij"
    }
  ],
  "totalInterviews": 5,
  "completedInterviews": 2,
  "upcomingCount": 3
}
```

**Use Case:** Candidate's interview schedule

---

## 🔍 Availability & Scheduling

### 1️⃣ Check Availability

```http
GET /api/calendar/recruiters/{recruiterId}/available?dateTime=2024-12-25T14:00:00&durationMinutes=60
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
true
```

**Checks:**
- ✅ Is a working day
- ✅ Within working hours
- ✅ Not during lunch break
- ✅ No conflicting interviews
- ✅ Has buffer time between interviews
- ✅ Not exceeded max interviews per day

### 2️⃣ Get Available Time Slots

```http
GET /api/calendar/recruiters/{recruiterId}/available-slots?date=2024-12-25&durationMinutes=60
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
[
  "09:00:00",
  "10:15:00",
  "11:30:00",
  "14:00:00",
  "15:15:00",
  "16:30:00"
]
```

**Algorithm:**
1. Gets working hours for that day
2. Removes lunch break period
3. Removes existing interview slots + buffer
4. Returns available start times

### 3️⃣ Get Available Dates

```http
GET /api/calendar/recruiters/{recruiterId}/available-dates?startDate=2024-12-01&endDate=2024-12-31&durationMinutes=60
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
[
  "2024-12-02",
  "2024-12-03",
  "2024-12-04",
  "2024-12-09",
  "2024-12-10"
]
```

**Returns:** Dates that have at least one available time slot

### 4️⃣ Check Conflict

```http
POST /api/calendar/recruiters/{recruiterId}/check-conflict
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{
  "candidateId": 456,
  "proposedStartTime": "2024-12-25T14:00:00",
  "durationMinutes": 60
}
```

**Response:**
```json
{
  "hasConflict": false,
  "conflicts": [],
  "canSchedule": true,
  "suggestedTimes": ["14:00:00", "15:15:00", "16:30:00"]
}
```

**Or with conflicts:**
```json
{
  "hasConflict": true,
  "conflicts": [
    {
      "conflictType": "OVERLAPPING_INTERVIEW",
      "conflictStart": "2024-12-25T13:30:00",
      "conflictEnd": "2024-12-25T14:30:00",
      "description": "Interview with candidate John Doe"
    },
    {
      "conflictType": "MAX_INTERVIEWS_REACHED",
      "description": "Maximum interviews per day reached (8/8)"
    }
  ],
  "canSchedule": false,
  "suggestedTimes": ["15:30:00", "16:45:00"]
}
```

**Conflict Types:**
- `OUTSIDE_WORKING_HOURS` - Proposed time outside work schedule
- `DURING_LUNCH_BREAK` - Conflicts with lunch break
- `OVERLAPPING_INTERVIEW` - Another interview already scheduled
- `INSUFFICIENT_BUFFER` - Not enough buffer time between interviews
- `MAX_INTERVIEWS_REACHED` - Daily interview limit reached

---

## 📊 Candidate Application Status

### Status Flow

```
APPLIED → SCREENING → INTERVIEWING → OFFER → HIRED
                  ↓
              REJECTED
```

### Status Definitions

| Status | Description | Next Actions |
|--------|-------------|--------------|
| `APPLIED` | Candidate submitted application | Recruiter reviews application |
| `SCREENING` | Application under review | Schedule initial screening call |
| `INTERVIEWING` | In interview process | Schedule/complete interviews |
| `OFFER` | Offer extended to candidate | Candidate accepts/rejects |
| `HIRED` | Candidate accepted and hired | Onboarding process |
| `REJECTED` | Application rejected | No further action |

### Get Candidate Applications

```http
GET /api/candidates/{candidateId}/applications
Authorization: Bearer {JWT_TOKEN}
```

**Response:**
```json
[
  {
    "id": 1,
    "jobPostingId": 789,
    "jobTitle": "Senior Software Engineer",
    "companyName": "Tech Corp",
    "status": "INTERVIEWING",
    "appliedDate": "2024-12-01T10:00:00",
    "lastUpdated": "2024-12-15T14:30:00",
    "scheduledInterviews": [
      {
        "id": 10,
        "startTime": "2024-12-25T09:00:00",
        "endTime": "2024-12-25T10:00:00",
        "status": "SCHEDULED",
        "recruiterName": "Jane Smith"
      }
    ]
  },
  {
    "id": 2,
    "jobPostingId": 790,
    "jobTitle": "Frontend Developer",
    "companyName": "Startup Inc",
    "status": "SCREENING",
    "appliedDate": "2024-12-10T09:00:00",
    "lastUpdated": "2024-12-12T11:00:00",
    "scheduledInterviews": []
  }
]
```

### Update Application Status

```http
PATCH /api/applications/{applicationId}/status
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "INTERVIEWING",
  "notes": "Candidate passed initial screening"
}
```

**Response:**
```json
{
  "id": 1,
  "candidateId": 456,
  "jobPostingId": 789,
  "status": "INTERVIEWING",
  "statusHistory": [
    {
      "status": "APPLIED",
      "timestamp": "2024-12-01T10:00:00",
      "changedBy": "System"
    },
    {
      "status": "SCREENING",
      "timestamp": "2024-12-05T14:30:00",
      "changedBy": "recruiter@company.com",
      "notes": "Resume looks promising"
    },
    {
      "status": "INTERVIEWING",
      "timestamp": "2024-12-15T14:30:00",
      "changedBy": "recruiter@company.com",
      "notes": "Candidate passed initial screening"
    }
  ],
  "lastUpdated": "2024-12-15T14:30:00"
}
```

---

## 📋 Data Formats

### Time Format

**✅ CORRECT - String format:**
```json
{
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "lunchBreakStart": "12:00:00"
}
```

**❌ WRONG - Object format:**
```json
{
  "startTime": { "hour": 9, "minute": 0, "second": 0 },
  "endTime": { "hour": 17, "minute": 0, "second": 0 }
}
```

### Date Format

**Date Only:**
```json
"2024-12-25"
```

**Date with Time (LocalDateTime):**
```json
"2024-12-25T14:30:00"
```

### Day of Week

**Valid Values:**
```json
"MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY"
```

### Interview Status

**Valid Values:**
```json
"SCHEDULED" | "CONFIRMED" | "COMPLETED" | "CANCELLED" | "RESCHEDULED" | "NO_SHOW"
```

### Application Status

**Valid Values:**
```json
"APPLIED" | "SCREENING" | "INTERVIEWING" | "OFFER" | "HIRED" | "REJECTED"
```

---

## 🔧 Frontend Implementation Examples

### TypeScript Helper Functions

```typescript
// Convert time object to string format
const formatTimeForBackend = (time: any): string | undefined => {
  if (!time) return undefined;
  
  // Already a string
  if (typeof time === 'string') {
    return time.includes(':') && time.split(':').length === 3 
      ? time 
      : `${time}:00`;
  }
  
  // Time object with hour/minute/second
  if (time?.hour !== undefined) {
    const hour = String(time.hour).padStart(2, '0');
    const minute = String(time.minute || 0).padStart(2, '0');
    const second = String(time.second || 0).padStart(2, '0');
    return `${hour}:${minute}:${second}`;
  }
  
  return undefined;
};

// Parse time string to display format
const parseTimeForDisplay = (timeString: string): string => {
  const [hour, minute] = timeString.split(':');
  return `${hour}:${minute}`;
};

// Format date for API
const formatDateForAPI = (date: Date): string => {
  return date.toISOString().split('T')[0]; // "2024-12-25"
};

// Format datetime for API
const formatDateTimeForAPI = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  const second = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day}T${hour}:${minute}:${second}`;
};
```

### React Component Example

```typescript
import { useState, useEffect } from 'react';

const RecruiterCalendar = () => {
  const [workingHours, setWorkingHours] = useState([]);
  const [dailyCalendar, setDailyCalendar] = useState(null);
  const [selectedDate, setSelectedDate] = useState(new Date());

  // Load working hours
  useEffect(() => {
    const loadWorkingHours = async () => {
      try {
        const response = await fetch('/api/calendar/recruiters/working-hours', {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        
        if (!response.ok) throw new Error('Failed to load');
        
        const data = await response.json();
        
        // Validate response is array
        if (!Array.isArray(data)) {
          console.error('Expected array but got:', typeof data);
          setWorkingHours([]);
          return;
        }
        
        setWorkingHours(data);
      } catch (error) {
        console.error('Error loading working hours:', error);
        setWorkingHours([]);
      }
    };
    
    loadWorkingHours();
  }, []);

  // Load daily calendar
  useEffect(() => {
    const loadDailyCalendar = async () => {
      try {
        const dateStr = formatDateForAPI(selectedDate);
        const response = await fetch(
          `/api/calendar/recruiters/${recruiterId}/daily?date=${dateStr}`,
          {
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            }
          }
        );
        
        if (!response.ok) throw new Error('Failed to load calendar');
        
        const data = await response.json();
        setDailyCalendar(data);
      } catch (error) {
        console.error('Error loading calendar:', error);
      }
    };
    
    loadDailyCalendar();
  }, [selectedDate, recruiterId]);

  // Save working hours
  const handleSaveWorkingHours = async (config: any) => {
    try {
      const payload = {
        dayOfWeek: config.dayOfWeek,
        isWorkingDay: config.isWorkingDay,
        startTime: formatTimeForBackend(config.startTime),
        endTime: formatTimeForBackend(config.endTime),
        lunchBreakStart: formatTimeForBackend(config.lunchBreakStart),
        lunchBreakEnd: formatTimeForBackend(config.lunchBreakEnd),
        bufferMinutesBetweenInterviews: config.bufferMinutes || 15,
        maxInterviewsPerDay: config.maxInterviews || 8
      };

      console.log('Saving working hours:', payload);

      const response = await fetch('/api/calendar/recruiters/working-hours', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Failed to save');
      }

      const saved = await response.json();
      console.log('Saved successfully:', saved);
      
      // Reload working hours
      await loadWorkingHours();
    } catch (error) {
      console.error('Error saving working hours:', error);
      alert('Failed to save working hours');
    }
  };

  return (
    <div>
      <h1>Recruiter Calendar</h1>
      
      {/* Working Hours Configuration */}
      <section>
        <h2>Working Hours</h2>
        {workingHours.length === 0 ? (
          <p>No working hours configured yet</p>
        ) : (
          <ul>
            {workingHours.map(hours => (
              <li key={hours.id}>
                <strong>{hours.dayOfWeek}</strong>: {' '}
                {hours.isWorkingDay 
                  ? `${parseTimeForDisplay(hours.startTime)} - ${parseTimeForDisplay(hours.endTime)}`
                  : 'Not working'}
              </li>
            ))}
          </ul>
        )}
      </section>

      {/* Daily Calendar */}
      <section>
        <h2>Daily Schedule</h2>
        <input 
          type="date" 
          value={formatDateForAPI(selectedDate)}
          onChange={(e) => setSelectedDate(new Date(e.target.value))}
        />
        
        {dailyCalendar && (
          <div>
            <p>Interviews: {dailyCalendar.totalInterviews}</p>
            <p>Available Slots: {dailyCalendar.availableSlots}</p>
            
            <h3>Scheduled Interviews</h3>
            <ul>
              {dailyCalendar.interviews.map(interview => (
                <li key={interview.id}>
                  {interview.candidateName} - {interview.jobTitle}
                  <br />
                  {new Date(interview.startTime).toLocaleTimeString()} - 
                  {new Date(interview.endTime).toLocaleTimeString()}
                </li>
              ))}
            </ul>
          </div>
        )}
      </section>
    </div>
  );
};

export default RecruiterCalendar;
```

---

## ⚠️ Common Issues & Solutions

### Issue 1: "Cannot read properties of undefined (reading 'map')"

**Cause:** Trying to access nested property when response is direct array

**Solution:**
```typescript
// ❌ Wrong
const data = await response.json();
const hours = data.result.map(...); // data.result is undefined

// ✅ Correct
const hours = await response.json(); // hours IS the array
hours.map(...);
```

### Issue 2: "Cannot deserialize value of type java.time.LocalTime"

**Cause:** Sending time as object instead of string

**Solution:**
```typescript
// ❌ Wrong
{ startTime: { hour: 9, minute: 0 } }

// ✅ Correct
{ startTime: "09:00:00" }
```

### Issue 3: Working hours not saving

**Check:**
1. ✅ Time format is string `"HH:MM:SS"`
2. ✅ JWT token is valid and included
3. ✅ User has RECRUITER role
4. ✅ All required fields included
5. ✅ `dayOfWeek` is valid enum value

### Issue 4: Empty calendar response

**Check:**
1. ✅ Working hours configured for that day
2. ✅ Date format is correct `"YYYY-MM-DD"`
3. ✅ recruiterId is correct
4. ✅ Check if day is marked as working day

### Issue 5: Availability check always returns false

**Check:**
1. ✅ Working hours exist for that day
2. ✅ `isWorkingDay` is true
3. ✅ Time is within working hours
4. ✅ Not during lunch break
5. ✅ No conflicting interviews
6. ✅ Buffer time respected
7. ✅ Max interviews not exceeded

---

## 📞 Support

For additional help or questions about the Calendar & Schedule feature:
1. Check backend logs for detailed error messages
2. Verify JWT token contains correct email and role
3. Ensure database has recruiter record linked to account
4. Test endpoints with Postman/Swagger first
5. Review `FRONTEND_FIX_GUIDE.md` for time format issues

---

## 📝 Summary

### ✅ What Recruiters Can Do:
- Set/update working hours for each day of week
- View daily/weekly/monthly calendar
- Check availability for scheduling
- Get list of available time slots
- View scheduled interviews
- Manage interview conflicts

### ✅ What Candidates Can Do:
- View their upcoming interviews
- See interview schedule by date range
- Track application status
- View interview details (time, location, recruiter)

### ❌ What Was Removed:
- Time-off requests (recruiters don't request time-off)
- Time-off approvals (admin feature removed)
- Time-off in calendar views
- Time-off conflict checks

### 🎯 Key Principles:
1. **Working hours define availability** - Set once, applies weekly
2. **No time-off management** - Company schedule only
3. **JWT-based authentication** - No manual recruiterId needed
4. **String time format** - Always `"HH:MM:SS"`
5. **Direct array responses** - No wrapper objects
