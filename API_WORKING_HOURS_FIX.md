# Working Hours API Fix - Internal Authentication Pattern

## ✅ Problem Solved

**Issue**: Frontend couldn't call working hours API because:
- API required `recruiterId` in URL path: `/api/calendar/recruiters/{recruiterId}/working-hours`
- JWT only contains email (not integer ID)
- No way for frontend to get recruiter ID from JWT token

**Solution**: Changed to **internal authentication pattern** (same as job posting APIs)
- API now gets recruiter ID from JWT automatically
- Frontend just sends request body
- Consistent with existing job posting endpoints
- More secure (user can only modify their own data)

---

## 🔄 What Changed

### Backend Changes

#### 1. Controller Endpoints (InterviewCalendarController.java)

**BEFORE** (required recruiterId parameter):
```java
// ❌ Old - Frontend blocked
@PostMapping("/recruiters/{recruiterId}/working-hours")
@PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
public ResponseEntity<RecruiterWorkingHoursResponse> setWorkingHours(
    @PathVariable Integer recruiterId,
    @Valid @RequestBody RecruiterWorkingHoursRequest request)
```

**AFTER** (uses internal authentication):
```java
// ✅ New - Frontend works!
@PostMapping("/recruiters/working-hours")
@PreAuthorize("hasRole('RECRUITER')")
public ResponseEntity<RecruiterWorkingHoursResponse> setWorkingHours(
    @Valid @RequestBody RecruiterWorkingHoursRequest request)
```

#### 2. URL Changes

| Endpoint | Old URL | New URL |
|----------|---------|---------|
| Set Working Hours | `POST /api/calendar/recruiters/{recruiterId}/working-hours` | `POST /api/calendar/recruiters/working-hours` |
| Get Working Hours | `GET /api/calendar/recruiters/{recruiterId}/working-hours` | `GET /api/calendar/recruiters/working-hours` |

**Batch endpoint unchanged**: `POST /api/calendar/recruiters/working-hours/batch` (still requires recruiterId for admin operations)

#### 3. Service Layer (InterviewCalendarServiceImpl.java)

Added `getMyRecruiter()` helper method (same pattern as `JobPostingImp`):
```java
private Recruiter getMyRecruiter() {
    Account currentAccount = authenticationImp.findByEmail();
    Recruiter recruiter = recruiterRepo.findByAccount_Id(currentAccount.getId())
        .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

    // Check if recruiter is verified (APPROVED status)
    if (!"APPROVED".equals(recruiter.getVerificationStatus())) {
        throw new AppException(ErrorCode.RECRUITER_NOT_VERIFIED);
    }

    return recruiter;
}
```

---

## 📝 Frontend Integration Guide

### How to Call the Fixed API

#### ✅ Set Working Hours (Single Day)

**Endpoint**: `POST /api/calendar/recruiters/working-hours`

**Request** (no recruiterId needed!):
```javascript
// Just send request body with JWT token in headers
const response = await fetch('/api/calendar/recruiters/working-hours', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${jwtToken}` // Backend extracts recruiter ID from this
  },
  body: JSON.stringify({
    dayOfWeek: 'MONDAY',
    isWorkingDay: true,
    startTime: '09:00:00',
    endTime: '17:00:00',
    lunchBreakStart: '12:00:00',
    lunchBreakEnd: '13:00:00',
    bufferMinutesBetweenInterviews: 15,
    maxInterviewsPerDay: 8
  })
});
```

**Response**:
```json
{
  "id": 123,
  "recruiterId": 456,
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

#### ✅ Get Working Hours

**Endpoint**: `GET /api/calendar/recruiters/working-hours`

**Request** (no recruiterId needed!):
```javascript
const response = await fetch('/api/calendar/recruiters/working-hours', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${jwtToken}` // Backend extracts recruiter ID from this
  }
});
```

**Response**:
```json
[
  {
    "id": 1,
    "recruiterId": 456,
    "dayOfWeek": "MONDAY",
    "isWorkingDay": true,
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    ...
  },
  {
    "id": 2,
    "recruiterId": 456,
    "dayOfWeek": "TUESDAY",
    "isWorkingDay": true,
    ...
  }
  // ... 7 days total
]
```

### React/Vue Example

```javascript
// ✅ Simple and clean!
const saveWorkingHours = async (workingHoursData) => {
  try {
    const response = await axios.post(
      '/api/calendar/recruiters/working-hours',
      workingHoursData,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('jwt')}`
        }
      }
    );
    console.log('Working hours saved:', response.data);
    return response.data;
  } catch (error) {
    console.error('Failed to save working hours:', error);
    throw error;
  }
};

// Usage
await saveWorkingHours({
  dayOfWeek: 'MONDAY',
  isWorkingDay: true,
  startTime: '09:00:00',
  endTime: '17:00:00',
  lunchBreakStart: '12:00:00',
  lunchBreakEnd: '13:00:00',
  bufferMinutesBetweenInterviews: 15,
  maxInterviewsPerDay: 8
});
```

---

## 🔍 Pattern Comparison

### Pattern A: Internal Authentication ✅ (Now Consistent)

Used by: **Job Posting**, **Working Hours** (after fix)

**Flow**:
```
Frontend → POST /api/jobposting (no ID)
         → Backend extracts email from JWT
         → Queries database: email → account ID → recruiter entity
         → Uses recruiter entity in business logic
```

**Pros**:
- ✅ Frontend doesn't need to manage IDs
- ✅ More secure (user can only modify own data)
- ✅ Simpler frontend code
- ✅ JWT stays lightweight (no entity IDs)

**Cons**:
- ⚠️ Less RESTful (no explicit resource ID in URL)
- ⚠️ Admin operations need separate endpoints

### Pattern B: External ID Requirement ❌ (Old Pattern)

Was used by: **Working Hours** (before fix), **Admin endpoints**

**Flow**:
```
Frontend → Must know recruiterId somehow
         → POST /api/calendar/recruiters/{recruiterId}/working-hours
         → Backend uses provided ID
```

**Pros**:
- ✅ RESTful pattern (explicit resource)
- ✅ Allows admin operations

**Cons**:
- ❌ Frontend must get ID first (extra API call)
- ❌ JWT doesn't contain ID (only email)
- ❌ Less secure (need validation to ensure user owns resource)

---

## 🔐 Security Notes

### Why JWT Contains Email (Not ID)

**Design Decision**: Keep JWT lightweight and stable
- Email is unique identifier for authentication
- Email rarely changes (stable claim)
- Integer IDs are database implementation details
- Decouples token from database schema changes

### How Backend Validates Access

```java
@PreAuthorize("hasRole('RECRUITER')")
public RecruiterWorkingHoursResponse setWorkingHours(RecruiterWorkingHoursRequest request) {
    // 1. Spring Security validates JWT signature
    // 2. Extracts email from JWT subject claim
    // 3. getMyRecruiter() queries: email → account → recruiter
    // 4. Verifies recruiter is APPROVED status
    // 5. User can ONLY modify their own data (secure by design)
    Recruiter recruiter = getMyRecruiter();
    // ... business logic
}
```

**Security Benefits**:
- ✅ User cannot provide arbitrary recruiter ID
- ✅ No risk of modifying other users' data
- ✅ Verification status checked automatically
- ✅ Database-level isolation per user

---

## 🧪 Testing the Fix

### 1. Test Login and Get Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "recruiter1@techcorp.com",
    "password": "password123"
  }'
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "authenticated": true
}
```

### 2. Set Working Hours (No ID Needed!)

```bash
TOKEN="your_jwt_token_here"

curl -X POST http://localhost:8080/api/calendar/recruiters/working-hours \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "dayOfWeek": "MONDAY",
    "isWorkingDay": true,
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "lunchBreakStart": "12:00:00",
    "lunchBreakEnd": "13:00:00",
    "bufferMinutesBetweenInterviews": 15,
    "maxInterviewsPerDay": 8
  }'
```

### 3. Get Working Hours (No ID Needed!)

```bash
curl -X GET http://localhost:8080/api/calendar/recruiters/working-hours \
  -H "Authorization: Bearer $TOKEN"
```

### Test Credentials (From test-data.sql)

All passwords: `password123`

**Recruiters**:
- `recruiter1@techcorp.com` (TechCorp Solutions)
- `recruiter2@innovate.com` (Innovate Labs)
- `recruiter3@globaltech.com` (GlobalTech International)

---

## 📊 API Consistency Matrix

| Feature | Endpoint | Pattern | Requires ID? | Status |
|---------|----------|---------|--------------|--------|
| Job Posting - Create | `POST /api/jobposting` | Internal Auth | ❌ No | ✅ Consistent |
| Job Posting - Update | `PUT /api/jobposting/recruiter/{id}` | Explicit ID | ⚠️ Yes (own ID) | ✅ Consistent |
| Working Hours - Set | `POST /api/calendar/recruiters/working-hours` | Internal Auth | ❌ No | ✅ **FIXED** |
| Working Hours - Get | `GET /api/calendar/recruiters/working-hours` | Internal Auth | ❌ No | ✅ **FIXED** |
| Working Hours - Batch | `POST /api/calendar/recruiters/working-hours/batch` | Admin/Explicit | ⚠️ Yes (for admin) | ✅ Unchanged |

---

## 🎯 Migration Checklist

### Backend ✅ Complete
- [x] Updated `InterviewCalendarController.java` - removed `{recruiterId}` from URL
- [x] Updated `InterviewCalendarService.java` - interface methods
- [x] Updated `InterviewCalendarServiceImpl.java` - implementation
- [x] Added `getMyRecruiter()` helper method
- [x] Added `AuthenticationImp` dependency
- [x] Compiled successfully (BUILD SUCCESS)

### Frontend 🔧 Action Required
- [ ] Update API endpoints (remove recruiterId from URL)
- [ ] Update API call code (remove recruiterId parameter)
- [ ] Test login → save working hours flow
- [ ] Test get working hours functionality
- [ ] Update any API documentation/Swagger

### Example Frontend Changes

**Before** (❌ Broken):
```javascript
// Had to get recruiterId somehow (impossible!)
const recruiterId = ???; // JWT doesn't contain this!
await axios.post(`/api/calendar/recruiters/${recruiterId}/working-hours`, data);
```

**After** (✅ Works):
```javascript
// Just send data with JWT token
await axios.post('/api/calendar/recruiters/working-hours', data);
```

---

## 🚀 Next Steps

1. **Frontend Team**: Update API calls to new endpoints
2. **Test**: Verify working hours save/load functionality
3. **Document**: Update API documentation (Swagger/OpenAPI)
4. **Deploy**: No database migrations needed (backend-only change)

---

## 📞 Questions?

**Q: Do I need to update the database?**  
A: No! This is a backend-only change. No database migrations required.

**Q: Will this break existing frontend code?**  
A: Yes, if frontend already calls the old endpoints with recruiterId in URL. Update to new URLs.

**Q: What about the batch endpoint?**  
A: Batch endpoint (`/recruiters/working-hours/batch`) unchanged - still requires recruiterId for admin operations.

**Q: Can admin set working hours for any recruiter?**  
A: Use the batch endpoint for admin operations. The new endpoints are recruiter-only (self-service).

**Q: Why not add recruiterId to JWT?**  
A: JWT should be stable and lightweight. Entity IDs are database implementation details that may change.

---

## ✅ Verification

**Compile Status**: ✅ BUILD SUCCESS  
**Pattern**: ✅ Consistent with Job Posting  
**Security**: ✅ User can only modify own data  
**Frontend**: ✅ No ID management needed  

**The save working hours button should now work! 🎉**
