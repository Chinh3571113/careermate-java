# Batch Working Hours API - Frontend Integration Guide

## Overview

This document provides a complete guide for integrating the **Batch Working Hours** endpoint. This endpoint allows recruiters to set multiple days of working hours in a single request.

**Key Features:**
- ✅ **No recruiterId required** - Backend automatically gets it from JWT token
- ✅ Set multiple days at once (Monday to Friday, or any combination)
- ✅ Option to replace all existing configurations or update specific days
- ✅ Automatic validation and error handling

---

## Authentication

**Required:** JWT token with `RECRUITER` role

```typescript
headers: {
  'Authorization': `Bearer ${jwtToken}`,
  'Content-Type': 'application/json'
}
```

The backend automatically extracts the recruiter's ID from the JWT token, so you **do not** need to provide `recruiterId` in the request body.

---

## API Endpoint

```
POST /api/v1/interview-calendar/recruiters/working-hours/batch
```

---

## Request Format

### TypeScript Interface

```typescript
interface BatchWorkingHoursRequest {
  workingHoursConfigurations: WorkingHoursConfiguration[];
  replaceAll?: boolean;  // Optional, defaults to false
}

interface WorkingHoursConfiguration {
  dayOfWeek: DayOfWeek;
  isWorkingDay: boolean;
  startTime?: string;      // Format: "HH:MM:SS" (e.g., "09:00:00")
  endTime?: string;        // Format: "HH:MM:SS" (e.g., "17:00:00")
  lunchBreakStart?: string; // Format: "HH:MM:SS" (e.g., "12:00:00")
  lunchBreakEnd?: string;   // Format: "HH:MM:SS" (e.g., "13:00:00")
  bufferMinutesBetweenInterviews?: number;  // Default: 15
  maxInterviewsPerDay?: number;             // Default: 8
}

type DayOfWeek = 
  | "MONDAY" 
  | "TUESDAY" 
  | "WEDNESDAY" 
  | "THURSDAY" 
  | "FRIDAY" 
  | "SATURDAY" 
  | "SUNDAY";
```

### Example Request Payloads

#### Example 1: Set All Weekdays (Monday - Friday)

```json
{
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
    },
    {
      "dayOfWeek": "WEDNESDAY",
      "isWorkingDay": true,
      "startTime": "09:00:00",
      "endTime": "17:00:00",
      "lunchBreakStart": "12:00:00",
      "lunchBreakEnd": "13:00:00",
      "bufferMinutesBetweenInterviews": 15,
      "maxInterviewsPerDay": 8
    },
    {
      "dayOfWeek": "THURSDAY",
      "isWorkingDay": true,
      "startTime": "09:00:00",
      "endTime": "17:00:00",
      "lunchBreakStart": "12:00:00",
      "lunchBreakEnd": "13:00:00",
      "bufferMinutesBetweenInterviews": 15,
      "maxInterviewsPerDay": 8
    },
    {
      "dayOfWeek": "FRIDAY",
      "isWorkingDay": true,
      "startTime": "08:00:00",
      "endTime": "17:00:00",
      "lunchBreakStart": "12:00:00",
      "lunchBreakEnd": "13:00:00",
      "bufferMinutesBetweenInterviews": 15,
      "maxInterviewsPerDay": 8
    }
  ],
  "replaceAll": true
}
```

#### Example 2: Update Specific Days Only

```json
{
  "workingHoursConfigurations": [
    {
      "dayOfWeek": "MONDAY",
      "isWorkingDay": true,
      "startTime": "08:00:00",
      "endTime": "16:00:00",
      "lunchBreakStart": "12:00:00",
      "lunchBreakEnd": "13:00:00"
    },
    {
      "dayOfWeek": "FRIDAY",
      "isWorkingDay": true,
      "startTime": "09:00:00",
      "endTime": "15:00:00",
      "lunchBreakStart": "12:00:00",
      "lunchBreakEnd": "13:00:00"
    }
  ],
  "replaceAll": false
}
```

#### Example 3: Set Weekend as Non-Working Days

```json
{
  "workingHoursConfigurations": [
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

---

## Response Format

### Success Response (200 OK)

```typescript
interface BatchWorkingHoursResponse {
  recruiterId: number;
  totalConfigurations: number;
  successfulUpdates: number;
  failedUpdates: number;
  updatedConfigurations: WorkingHoursResponse[];
  errors: { [dayOfWeek: string]: string };
}

interface WorkingHoursResponse {
  id: number;
  recruiterId: number;
  dayOfWeek: string;
  isWorkingDay: boolean;
  startTime: string;      // Format: "HH:MM:SS"
  endTime: string;        // Format: "HH:MM:SS"
  lunchBreakStart: string; // Format: "HH:MM:SS"
  lunchBreakEnd: string;   // Format: "HH:MM:SS"
  bufferMinutesBetweenInterviews: number;
  maxInterviewsPerDay: number;
}
```

### Example Success Response

```json
{
  "recruiterId": 123,
  "totalConfigurations": 5,
  "successfulUpdates": 5,
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
    },
    // ... other days
  ],
  "errors": {}
}
```

### Error Response (400 Bad Request)

```json
{
  "message": "Validation failed",
  "errors": {
    "workingHoursConfigurations": "At least one working hours configuration is required"
  },
  "status": 400
}
```

---

## React/TypeScript Implementation

### Complete Integration Example

```typescript
import axios from 'axios';

// API Service
class WorkingHoursService {
  private baseURL = '/api/v1/interview-calendar/recruiters/working-hours';
  
  async setBatchWorkingHours(
    configurations: WorkingHoursConfiguration[],
    replaceAll: boolean = false
  ): Promise<BatchWorkingHoursResponse> {
    const token = localStorage.getItem('jwtToken');
    
    const response = await axios.post(
      `${this.baseURL}/batch`,
      {
        workingHoursConfigurations: configurations,
        replaceAll: replaceAll
      },
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    return response.data;
  }
}

// React Component Example
import React, { useState } from 'react';

interface WorkingHoursFormProps {
  onSuccess?: (response: BatchWorkingHoursResponse) => void;
  onError?: (error: any) => void;
}

const BatchWorkingHoursForm: React.FC<WorkingHoursFormProps> = ({ 
  onSuccess, 
  onError 
}) => {
  const [loading, setLoading] = useState(false);
  const service = new WorkingHoursService();
  
  const handleSaveAllWeekdays = async () => {
    setLoading(true);
    
    try {
      const weekdayConfig: WorkingHoursConfiguration[] = [
        'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'
      ].map(day => ({
        dayOfWeek: day as DayOfWeek,
        isWorkingDay: true,
        startTime: '09:00:00',
        endTime: '17:00:00',
        lunchBreakStart: '12:00:00',
        lunchBreakEnd: '13:00:00',
        bufferMinutesBetweenInterviews: 15,
        maxInterviewsPerDay: 8
      }));
      
      const response = await service.setBatchWorkingHours(
        weekdayConfig, 
        true // Replace all existing configurations
      );
      
      console.log('✅ Success:', response);
      onSuccess?.(response);
      
    } catch (error: any) {
      console.error('❌ Error:', error.response?.data || error.message);
      onError?.(error);
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <button 
      onClick={handleSaveAllWeekdays}
      disabled={loading}
    >
      {loading ? 'Saving...' : 'Set Working Hours (Mon-Fri)'}
    </button>
  );
};

export default BatchWorkingHoursForm;
```

---

## Time Format Helper Functions

### Convert Time Object to String

```typescript
// If you're using a time picker that returns objects like { hour: 9, minute: 0 }
function timeObjectToString(timeObj: { hour: number; minute: number; second?: number }): string {
  const hour = String(timeObj.hour).padStart(2, '0');
  const minute = String(timeObj.minute).padStart(2, '0');
  const second = String(timeObj.second || 0).padStart(2, '0');
  return `${hour}:${minute}:${second}`;
}

// Example usage
const startTime = { hour: 9, minute: 0 };
const startTimeString = timeObjectToString(startTime); // "09:00:00"
```

### Convert Date to Time String

```typescript
// If you're using a Date picker
function dateToTimeString(date: Date): string {
  const hour = String(date.getHours()).padStart(2, '0');
  const minute = String(date.getMinutes()).padStart(2, '0');
  const second = String(date.getSeconds()).padStart(2, '0');
  return `${hour}:${minute}:${second}`;
}

// Example usage
const now = new Date();
const timeString = dateToTimeString(now); // "14:30:00" (current time)
```

---

## Important Notes

### ✅ DO's

1. **Time Format**: Always send times as `"HH:MM:SS"` strings
   ```javascript
   ✅ "09:00:00"  // Correct
   ❌ "9:00"      // Wrong
   ❌ { hour: 9 } // Wrong
   ```

2. **No recruiterId**: Do NOT include `recruiterId` in request body
   ```javascript
   ✅ { workingHoursConfigurations: [...], replaceAll: true }
   ❌ { recruiterId: 123, workingHoursConfigurations: [...] }
   ```

3. **Day Names**: Use UPPERCASE day names
   ```javascript
   ✅ "MONDAY"
   ❌ "Monday"
   ❌ "monday"
   ```

4. **Non-Working Days**: For non-working days, only `isWorkingDay: false` is required
   ```javascript
   {
     "dayOfWeek": "SATURDAY",
     "isWorkingDay": false
     // No need for startTime, endTime, etc.
   }
   ```

### ❌ Common Mistakes

1. **Sending recruiterId in body** → 400 Bad Request (field not allowed)
2. **Wrong time format** → 400 Bad Request (deserialization error)
3. **Lowercase day names** → 400 Bad Request (enum validation error)
4. **Empty configurations array** → 400 Bad Request (validation error)
5. **Missing JWT token** → 401 Unauthorized

---

## replaceAll Parameter

### `replaceAll: true` (Replace All)
- **Deletes** all existing working hours configurations for the recruiter
- **Creates** new configurations from the request
- **Use case**: Initial setup or complete reset

```typescript
// Example: Set up fresh schedule for the week
{
  workingHoursConfigurations: [
    /* Monday through Friday */
  ],
  replaceAll: true  // ← Deletes all old data first
}
```

### `replaceAll: false` (Update Only)
- **Updates** existing configurations for specified days
- **Creates** new configurations if day doesn't exist
- **Keeps** configurations for days not in the request
- **Use case**: Modify specific days without affecting others

```typescript
// Example: Only update Monday and Friday
{
  workingHoursConfigurations: [
    { dayOfWeek: "MONDAY", ... },
    { dayOfWeek: "FRIDAY", ... }
  ],
  replaceAll: false  // ← Keeps Tuesday/Wednesday/Thursday unchanged
}
```

---

## Testing Checklist

Before deploying, verify:

- [ ] JWT token is correctly attached to Authorization header
- [ ] Time strings are in `"HH:MM:SS"` format (not objects)
- [ ] Day names are UPPERCASE (`"MONDAY"`, not `"monday"`)
- [ ] No `recruiterId` field in request body
- [ ] `workingHoursConfigurations` array is not empty
- [ ] Handle both success and error responses
- [ ] Test with `replaceAll: true` and `replaceAll: false`
- [ ] Verify response contains updated configurations

---

## Troubleshooting

### Error: "Validation failed: recruiterId is required"
**Cause**: You're sending `recruiterId` in the request body  
**Solution**: Remove `recruiterId` field from your payload

### Error: "Cannot deserialize value of type java.time.LocalTime"
**Cause**: Time sent as object instead of string  
**Solution**: Convert to `"HH:MM:SS"` format
```javascript
// Wrong: { hour: 9, minute: 0 }
// Right: "09:00:00"
```

### Error: 401 Unauthorized
**Cause**: Missing or invalid JWT token  
**Solution**: Verify JWT token is present in Authorization header

### Error: "At least one working hours configuration is required"
**Cause**: Empty `workingHoursConfigurations` array  
**Solution**: Add at least one configuration to the array

### Success but database shows old data
**Cause**: Used `replaceAll: false` expecting full replacement  
**Solution**: Use `replaceAll: true` to delete old data first

---

## Complete cURL Example

```bash
curl -X POST "http://localhost:8080/api/v1/interview-calendar/recruiters/working-hours/batch" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
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
      }
    ],
    "replaceAll": true
  }'
```

---

## Summary

| Field | Required | Format | Example |
|-------|----------|--------|---------|
| `workingHoursConfigurations` | ✅ Yes | Array | `[{...}]` |
| `dayOfWeek` | ✅ Yes | UPPERCASE string | `"MONDAY"` |
| `isWorkingDay` | ✅ Yes | boolean | `true` |
| `startTime` | ⚠️ If working | `"HH:MM:SS"` | `"09:00:00"` |
| `endTime` | ⚠️ If working | `"HH:MM:SS"` | `"17:00:00"` |
| `lunchBreakStart` | ❌ Optional | `"HH:MM:SS"` | `"12:00:00"` |
| `lunchBreakEnd` | ❌ Optional | `"HH:MM:SS"` | `"13:00:00"` |
| `bufferMinutesBetweenInterviews` | ❌ Optional | number | `15` |
| `maxInterviewsPerDay` | ❌ Optional | number | `8` |
| `replaceAll` | ❌ Optional | boolean | `false` |
| `recruiterId` | ⛔ **NOT ALLOWED** | - | - |

---

## Support

For backend implementation details, see:
- `CALENDAR_SCHEDULE_GUIDE.md` - Complete calendar & scheduling documentation
- `InterviewCalendarController.java` - Controller implementation
- `InterviewCalendarServiceImpl.java` - Service logic with `getMyRecruiter()` helper

**Last Updated**: November 27, 2025
