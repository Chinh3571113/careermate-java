# 🚀 Frontend Integration Guide - Working Hours API

## ❌ Current Frontend Error

```
POST http://localhost:8080/api/calendar/recruiters/working-hours 400 (Bad Request)
Error: Cannot deserialize value of type `java.time.LocalTime` from Object value
```

**Root Cause**: Frontend is sending time values as **objects** instead of **strings**!

```javascript
// ❌ WRONG - Sending time as object
{
  startTime: { hour: 9, minute: 0, second: 0 }  // Backend can't parse this!
}

// ✅ CORRECT - Send time as string
{
  startTime: "09:00:00"  // Backend expects this format!
}
```

**Problems**:
1. ❌ Time format: Frontend sends objects `{hour:9}` but backend needs strings `"09:00:00"`
2. ❌ Wrong URL (if using batch): Should be `/recruiters/working-hours/batch` not `/recruiters/me/...`
3. ❌ Missing `recruiterId` (if using batch endpoint)

---

## ✅ QUICK FIX - Your Frontend Code Changes

### 🔴 FIX 1: Convert Time Objects to Strings (CRITICAL!)

Backend expects time as **strings in "HH:MM:SS" format**, not objects!

**❌ Current (Wrong) - Sending objects**:
```typescript
{
  dayOfWeek: "MONDAY",
  startTime: { hour: 9, minute: 0, second: 0 },  // ❌ Backend can't parse!
  endTime: { hour: 17, minute: 0, second: 0 }
}
```

**✅ Fixed - Send strings**:
```typescript
{
  dayOfWeek: "MONDAY",
  startTime: "09:00:00",  // ✅ Correct format!
  endTime: "17:00:00"
}
```

### Helper Function to Convert Time

Add this to your frontend code:

```typescript
// Convert time object to string format "HH:MM:SS"
const formatTimeForBackend = (time: any): string => {
  if (typeof time === 'string') {
    // Already a string, ensure it has seconds
    return time.includes(':') && time.split(':').length === 3 
      ? time 
      : `${time}:00`;
  }
  
  if (time?.hour !== undefined) {
    // Time picker object {hour: 9, minute: 0}
    const hour = String(time.hour).padStart(2, '0');
    const minute = String(time.minute || 0).padStart(2, '0');
    const second = String(time.second || 0).padStart(2, '0');
    return `${hour}:${minute}:${second}`;
  }
  
  return "09:00:00";  // Default fallback
};

// Use it before sending to backend
const dayConfig = {
  dayOfWeek: "MONDAY",
  isWorkingDay: true,
  startTime: formatTimeForBackend(startTimeValue),      // Convert here!
  endTime: formatTimeForBackend(endTimeValue),          // Convert here!
  lunchBreakStart: formatTimeForBackend(lunchStartValue),
  lunchBreakEnd: formatTimeForBackend(lunchEndValue)
};
```

### Fix 2: Change URL (Remove `/me`)

**❌ Current (Wrong)**:
```typescript
POST http://localhost:8080/api/calendar/recruiters/me/working-hours/batch
```

**✅ Fixed**:
```typescript
POST http://localhost:8080/api/calendar/recruiters/working-hours/batch
```

### Fix 3: Add recruiterId to Batch Request

**❌ Current (Missing recruiterId)**:
```typescript
{
  workingHoursConfigurations: [...]
}
```

**✅ Fixed (Include recruiterId)**:
```typescript
{
  recruiterId: 456,  // ⚠️ REQUIRED!
  replaceAll: true,
  workingHoursConfigurations: [...]
}
```

### Fix 3: Get recruiterId First

Since JWT doesn't contain ID, get it from existing working hours:

```typescript
// Step 1: Get recruiter ID from existing data
const getMyRecruiterId = async (): Promise<number> => {
  const response = await fetch('http://localhost:8080/api/calendar/recruiters/working-hours', {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${getJwtToken()}`
    }
  });
  
  const workingHours = await response.json();
  
  if (workingHours.length > 0) {
    return workingHours[0].recruiterId;  // Extract ID from response
  }
  
  throw new Error('Could not determine recruiter ID');
};

// Step 2: Use it in batch request
const setWorkingHoursBatch = async (configs: any[]) => {
  const recruiterId = await getMyRecruiterId();  // Get ID first!
  
  const response = await fetch('http://localhost:8080/api/calendar/recruiters/working-hours/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getJwtToken()}`
    },
    body: JSON.stringify({
      recruiterId: recruiterId,  // ✅ Now included!
      replaceAll: true,
      workingHoursConfigurations: configs
    })
  });
  
  return await response.json();
};
```

---

## 🎯 RECOMMENDED SOLUTION (Simpler!)

**Instead of using batch**, use the **single day endpoint** which doesn't need recruiterId:

### Option A: Single Day Updates (EASIER!)

```typescript
// ✅ SIMPLER: No recruiterId needed!
const setWorkingHours = async (dayConfig: any) => {
  const response = await fetch('http://localhost:8080/api/calendar/recruiters/working-hours', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getJwtToken()}`
    },
    body: JSON.stringify({
      dayOfWeek: dayConfig.dayOfWeek,
      isWorkingDay: dayConfig.isWorkingDay,
      startTime: dayConfig.startTime,
      endTime: dayConfig.endTime,
      lunchBreakStart: dayConfig.lunchBreakStart,
      lunchBreakEnd: dayConfig.lunchBreakEnd,
      bufferMinutesBetweenInterviews: 15,
      maxInterviewsPerDay: 8
    })
  });
  
  return await response.json();
};

// Save entire week by calling single endpoint 7 times
const saveWholeWeek = async (weekSchedule: any[]) => {
  for (const dayConfig of weekSchedule) {
    await setWorkingHours(dayConfig);
  }
};
```

**Benefits**:
- ✅ No recruiterId management needed
- ✅ Simpler code
- ✅ More secure (backend controls ID from JWT)
- ✅ Works immediately

---

## 📝 Complete Code Examples

### calendar-api.ts (Fixed)

```typescript
const API_BASE_URL = 'http://localhost:8080/api/calendar';

const getAuthHeaders = () => ({
  'Authorization': `Bearer ${localStorage.getItem('jwtToken')}`,
  'Content-Type': 'application/json'
});

// 🔧 CRITICAL: Convert time to string format
const formatTimeForBackend = (time: any): string | undefined => {
  if (!time) return undefined;
  
  if (typeof time === 'string') {
    // Already string, ensure it has seconds
    return time.includes(':') && time.split(':').length === 3 
      ? time 
      : `${time}:00`;
  }
  
  if (time?.hour !== undefined) {
    // Time object from picker {hour: 9, minute: 0}
    const hour = String(time.hour).padStart(2, '0');
    const minute = String(time.minute || 0).padStart(2, '0');
    const second = String(time.second || 0).padStart(2, '0');
    return `${hour}:${minute}:${second}`;
  }
  
  return undefined;
};

// ✅ OPTION 1: Single day (RECOMMENDED - simpler)
export const setWorkingHours = async (config: any) => {
  try {
    // 🔧 Convert time objects to strings BEFORE sending
    const payload = {
      dayOfWeek: config.dayOfWeek,
      isWorkingDay: config.isWorkingDay,
      startTime: formatTimeForBackend(config.startTime),
      endTime: formatTimeForBackend(config.endTime),
      lunchBreakStart: formatTimeForBackend(config.lunchBreakStart),
      lunchBreakEnd: formatTimeForBackend(config.lunchBreakEnd),
      bufferMinutesBetweenInterviews: config.bufferMinutesBetweenInterviews || 15,
      maxInterviewsPerDay: config.maxInterviewsPerDay || 8
    };
    
    console.log('📤 [SET WORKING HOURS] Sending payload:', payload);
    
    const response = await fetch(`${API_BASE_URL}/recruiters/working-hours`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload)
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to set working hours');
    }
    
    return await response.json();
  } catch (error: any) {
    console.error('❌ [SET WORKING HOURS] Error:', error);
    throw error;
  }
};

// ✅ OPTION 2: Batch (requires recruiterId)
export const setWorkingHoursBatch = async (configs: any[]) => {
  try {
    // Step 1: Get recruiter ID first
    const existingResponse = await fetch(`${API_BASE_URL}/recruiters/working-hours`, {
      method: 'GET',
      headers: getAuthHeaders()
    });
    
    const existingHours = await existingResponse.json();
    const recruiterId = existingHours.length > 0 ? existingHours[0].recruiterId : null;
    
    if (!recruiterId) {
      throw new Error('Could not determine recruiter ID');
    }
    
    // Step 2: Convert all time values to strings
    const formattedConfigs = configs.map(config => ({
      dayOfWeek: config.dayOfWeek,
      isWorkingDay: config.isWorkingDay,
      startTime: formatTimeForBackend(config.startTime),
      endTime: formatTimeForBackend(config.endTime),
      lunchBreakStart: formatTimeForBackend(config.lunchBreakStart),
      lunchBreakEnd: formatTimeForBackend(config.lunchBreakEnd),
      bufferMinutesBetweenInterviews: config.bufferMinutesBetweenInterviews || 15,
      maxInterviewsPerDay: config.maxInterviewsPerDay || 8
    }));
    
    const payload = {
      recruiterId: recruiterId,  // ✅ Required!
      replaceAll: true,
      workingHoursConfigurations: formattedConfigs  // ✅ Times converted to strings!
    };
    
    console.log('📤 [SET WORKING HOURS BATCH] Sending payload:', payload);
    
    // Step 3: Call batch endpoint
    const response = await fetch(`${API_BASE_URL}/recruiters/working-hours/batch`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload)
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to set batch working hours');
    }
    
    return await response.json();
  } catch (error: any) {
    console.error('❌ [SET WORKING HOURS BATCH] Error:', error);
    throw error;
  }
};

// Get working hours
export const getWorkingHours = async () => {
  try {
    const response = await fetch(`${API_BASE_URL}/recruiters/working-hours`, {
      method: 'GET',
      headers: getAuthHeaders()
    });
    
    if (!response.ok) {
      throw new Error('Failed to get working hours');
    }
    
    return await response.json();
  } catch (error: any) {
    console.error('❌ [GET WORKING HOURS] Error:', error);
    throw error;
  }
};
```

### settings/page.tsx (Fixed)

```typescript
'use client';

import { useState } from 'react';
import { setWorkingHours, setWorkingHoursBatch } from '@/api/calendar-api';

export default function SettingsPage() {
  const [loading, setLoading] = useState(false);
  const [weekSchedule, setWeekSchedule] = useState([
    { dayOfWeek: 'MONDAY', isWorkingDay: true, startTime: '09:00:00', endTime: '17:00:00' },
    { dayOfWeek: 'TUESDAY', isWorkingDay: true, startTime: '09:00:00', endTime: '17:00:00' },
    // ... rest of week
  ]);

  // ✅ OPTION 1: Save using single endpoint (RECOMMENDED)
  const handleSaveWorkingHours = async () => {
    try {
      setLoading(true);
      console.log('💾 [SETTINGS] Saving working hours...');
      
      // Save each day individually
      for (const dayConfig of weekSchedule) {
        await setWorkingHours(dayConfig);
      }
      
      console.log('✅ [SETTINGS] All days saved successfully');
      alert('Working hours saved successfully!');
    } catch (error: any) {
      console.error('🔴 [SETTINGS] Failed to save working hours:', error);
      alert(`Failed to save: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  // ✅ OPTION 2: Save using batch endpoint (more complex)
  const handleSaveWorkingHoursBatch = async () => {
    try {
      setLoading(true);
      console.log('💾 [SETTINGS] Saving batch working hours...');
      
      const result = await setWorkingHoursBatch(weekSchedule);
      
      console.log('✅ [SETTINGS] Batch saved:', result);
      alert(`Saved ${result.successfulUpdates} days successfully!`);
    } catch (error: any) {
      console.error('🔴 [SETTINGS] Failed to save batch:', error);
      alert(`Failed to save: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-6">Working Hours Settings</h1>
      
      {/* Your schedule UI here */}
      
      {/* Option 1: Simple save (recommended) */}
      <button
        onClick={handleSaveWorkingHours}
        disabled={loading}
        className="bg-blue-500 text-white px-6 py-3 rounded"
      >
        {loading ? 'Saving...' : 'Save Working Hours'}
      </button>
      
      {/* Option 2: Batch save */}
      <button
        onClick={handleSaveWorkingHoursBatch}
        disabled={loading}
        className="bg-green-500 text-white px-6 py-3 rounded ml-4"
      >
        {loading ? 'Saving...' : 'Save Batch (Faster)'}
      </button>
    </div>
  );
}
```

---

## 🔍 API Endpoints Summary

| Endpoint | URL | Method | recruiterId Needed? |
|----------|-----|--------|---------------------|
| Set Single Day | `/recruiters/working-hours` | POST | ❌ No (from JWT) |
| Get Schedule | `/recruiters/working-hours` | GET | ❌ No (from JWT) |
| Batch Update | `/recruiters/working-hours/batch` | POST | ⚠️ Yes (in body) |

---

## 🧪 Test Your Fix

### Test Time Format First!

```typescript
// Test your formatTimeForBackend function
console.log(formatTimeForBackend({ hour: 9, minute: 0 }));        // "09:00:00" ✅
console.log(formatTimeForBackend({ hour: 17, minute: 30 }));      // "17:30:00" ✅
console.log(formatTimeForBackend("09:00"));                        // "09:00:00" ✅
console.log(formatTimeForBackend("09:00:00"));                     // "09:00:00" ✅
```

### Test with cURL:

```bash
# 1. Login first
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"recruiter1@techcorp.com","password":"password123"}' \
  | jq -r '.token')

# 2. Test single day with CORRECT time format (strings!)
curl -X POST http://localhost:8080/api/calendar/recruiters/working-hours \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "dayOfWeek": "MONDAY",
    "isWorkingDay": true,
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "lunchBreakStart": "12:00:00",
    "lunchBreakEnd": "13:00:00"
  }'

# ❌ WRONG - This will fail (time as object):
# "startTime": {"hour": 9, "minute": 0}  // DON'T DO THIS!

# ✅ CORRECT - Use string format:
# "startTime": "09:00:00"  // DO THIS!

# 3. Get working hours (includes recruiterId in response)
curl -X GET http://localhost:8080/api/calendar/recruiters/working-hours \
  -H "Authorization: Bearer $TOKEN"

# Response will show recruiterId:
# [{"id":1,"recruiterId":456,"dayOfWeek":"MONDAY",...}]
```

---

## ⚠️ Key Takeaways

1. **🔴 CRITICAL - Time Format**: Send times as **strings** `"09:00:00"` NOT objects `{hour:9}`
2. **URL Change**: Remove `/me` from URL path
3. **Batch Requires ID**: Must include `recruiterId` in request body
4. **Get ID First**: Call GET endpoint to extract recruiterId from response
5. **Simpler Option**: Use single day endpoint instead (no ID management)

### Time Format Examples

| Format | Correct? | Example |
|--------|----------|---------|
| String "HH:MM:SS" | ✅ YES | `"09:00:00"` |
| String "HH:MM" | ⚠️ Add :00 | `"09:00:00"` |
| Object {hour,minute} | ❌ NO | Convert to string first! |
| Number timestamp | ❌ NO | Convert to string first! |
| Date object | ❌ NO | Extract time, convert to string! |

---

## 💡 Why This Design?

- **JWT contains email only** (not ID) - by design for security
- **Backend converts** email → ID internally for single endpoints
- **Batch endpoint needs ID** because it's also used by admins
- **Single endpoint is simpler** for most use cases

**Recommendation**: Use single day endpoint unless you absolutely need atomic batch updates!

---

## 🆘 Still Getting Errors?

### Common Backend Errors:

| Error | Cause | Fix |
|-------|-------|-----|
| `Cannot deserialize value of type java.time.LocalTime` | Time sent as object instead of string | Convert time to `"HH:MM:SS"` string format |
| `400 Bad Request` | Missing `recruiterId` in batch body (working hours only) | Add recruiterId to batch request |
| `404 Not Found` | Using old URL with `{recruiterId}` | Use new URL without recruiterId |
| `401 Unauthorized` | JWT token expired or missing | Check Authorization header |
| `403 Forbidden` | User is not a RECRUITER | Verify user role in JWT |

### Debug Checklist:

```typescript
// Add this before sending to backend:
console.log('📤 Payload being sent:', JSON.stringify(payload, null, 2));

// Check the payload format:
{
  "dayOfWeek": "MONDAY",        // ✅ String
  "isWorkingDay": true,          // ✅ Boolean
  "startTime": "09:00:00",       // ✅ String (not object!)
  "endTime": "17:00:00"          // ✅ String (not object!)
}
```

Check backend logs for detailed error messages!

---

## 🐛 Common Frontend Error: "Cannot read properties of undefined (reading 'map')"

### Error Details:
```
TypeError: Cannot read properties of undefined (reading 'map')
    at loadSettings (page.tsx:200:42)
```

This happens after saving working hours when trying to reload the settings.

### Root Cause:

The backend returns data **directly as an array**, NOT wrapped in an object:

**❌ WRONG - Assuming wrapped response:**
```typescript
const loadSettings = async () => {
  const response = await fetch('/api/calendar/recruiters/working-hours');
  const data = await response.json();
  
  // ❌ Trying to access nested property that doesn't exist:
  const hours = data.result.map(...);  // data.result is undefined!
  // or
  const hours = data.data.map(...);    // data.data is undefined!
};
```

**✅ CORRECT - Use data directly:**
```typescript
const loadSettings = async () => {
  const response = await fetch('/api/calendar/recruiters/working-hours', {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  });
  
  if (!response.ok) {
    throw new Error('Failed to load working hours');
  }
  
  const data = await response.json();
  
  // ✅ data IS the array directly!
  console.log('📥 Response:', data);  // Logs: [{id: 1, dayOfWeek: "MONDAY", ...}, ...]
  
  // ✅ Use data directly with .map()
  const hours = data.map(item => ({
    dayOfWeek: item.dayOfWeek,
    startTime: item.startTime,
    // ... etc
  }));
  
  return hours;
};
```

### Backend Response Format:

**GET `/api/calendar/recruiters/working-hours`** returns:
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
    ...
  }
]
```

**NOT wrapped** like this:
```json
{
  "code": 1000,
  "message": "Success",
  "result": [...]  // ❌ There is NO wrapper!
}
```

### Quick Fix:

```typescript
// Before (causing error):
const data = await response.json();
const hours = data.result?.map(...) || [];  // ❌ data.result is undefined

// After (working):
const data = await response.json();
const hours = Array.isArray(data) ? data.map(...) : [];  // ✅ Check if data is array

// Or even simpler:
const hours = await response.json();  // ✅ hours is the array directly
```

### Defensive Coding:

```typescript
const loadSettings = async () => {
  try {
    const response = await fetch('/api/calendar/recruiters/working-hours', {
      headers: getAuthHeaders()
    });
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    
    const data = await response.json();
    
    // ✅ Validate response is array
    if (!Array.isArray(data)) {
      console.error('❌ Expected array but got:', typeof data, data);
      return [];  // Return empty array as fallback
    }
    
    console.log(`✅ Loaded ${data.length} working hours`);
    return data;
    
  } catch (error) {
    console.error('❌ Failed to load settings:', error);
    return [];  // Return empty array on error
  }
};
```
