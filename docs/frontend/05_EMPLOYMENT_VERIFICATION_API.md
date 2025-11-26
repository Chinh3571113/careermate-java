# Employment Verification API Documentation

**Module**: Employment Tracking & Verification System  
**Base URL**: `/api`  
**Version**: 3.0  
**Last Updated**: November 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [Employment Lifecycle](#employment-lifecycle)
3. [Data Models](#data-models)
4. [API Endpoints](#api-endpoints)
5. [Reminder System](#reminder-system)
6. [Review Eligibility Logic](#review-eligibility-logic)
7. [Business Rules](#business-rules)
8. [Error Handling](#error-handling)

---

## Overview

The **Employment Verification System** tracks the complete lifecycle of employment relationships, from start date to termination. It enforces review eligibility rules and sends automated reminders to candidates to verify their employment status.

### Key Features

✅ **30-Day Check-In**: Verify candidate is still employed after 30 days  
✅ **90-Day Milestone**: Confirm continued employment after probation period  
✅ **Termination Tracking**: Record termination type, date, and reason  
✅ **Review Eligibility**: Auto-calculate review eligibility based on employment duration  
✅ **Automated Reminders**: Email reminders at 30/90 days for status verification  

---

## Employment Lifecycle

### Lifecycle Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                     EMPLOYMENT LIFECYCLE                         │
└─────────────────────────────────────────────────────────────────┘

Day 0: Contract Signed
       └── EmploymentVerification created (isActive=true, startDate=today)
       
Day 30: First Check-In
       ├── System sends reminder: "Are you still employed?"
       ├── Candidate confirms: verified30Days=true, verifiedAt30Days=now
       └── Review eligibility: ELIGIBLE (if confirmed by Day 37)
       OR
       └── No response: Review eligibility remains NOT_ELIGIBLE

Day 90: Second Check-In  
       ├── System sends reminder: "Are you still employed?"
       ├── Candidate confirms: verified90Days=true, verifiedAt90Days=now
       └── Review eligibility: ELIGIBLE (if confirmed by Day 97)

Day X: Termination
       ├── Candidate/Recruiter reports termination
       ├── EmploymentVerification updated:
       │   ├── isActive = false
       │   ├── endDate = termination date
       │   ├── terminationType = RESIGNATION/FIRED/LAYOFF/etc.
       │   └── reasonForLeaving = explanation
       └── Review eligibility: ELIGIBLE (if employed >= 30 days)
```

### Status Transitions

| From State | To State | Trigger |
|-----------|----------|---------|
| **Active (Day 0-29)** | Active (Day 30+) | 30-day verification confirmed |
| **Active (Day 0-29)** | Active (Not Verified) | 30-day verification missed |
| **Active (Day 30+)** | Active (Day 90+) | 90-day verification confirmed |
| **Active (Any)** | Terminated | Termination reported |
| **Terminated** | N/A | Final state (immutable) |

---

## Data Models

### EmploymentVerificationResponse
```typescript
interface EmploymentVerificationResponse {
  id: number;
  jobApplyId: number;
  
  // Job details
  jobTitle: string;
  companyName: string;
  candidateName: string;
  recruiterName: string;
  
  // Employment dates
  startDate: string;                  // ISO 8601 date (contract start)
  endDate?: string;                   // ISO 8601 date (termination date, null if active)
  
  // Employment status
  isActive: boolean;                  // true = currently employed
  
  // Verification checkpoints
  verified30Days: boolean;            // Confirmed at 30-day mark
  verifiedAt30Days?: string;          // Timestamp of 30-day confirmation
  verified90Days: boolean;            // Confirmed at 90-day mark
  verifiedAt90Days?: string;          // Timestamp of 90-day confirmation
  
  // Termination details (if applicable)
  terminationType?: TerminationType;
  reasonForLeaving?: string;
  
  // Review eligibility
  reviewEligibility: ReviewEligibilityStatus;
  reviewEligibilityReason: string;
  daysEmployed: number;               // Total days employed (today - startDate OR endDate - startDate)
  
  // Reminder tracking
  reminder30DaySent: boolean;
  reminder30DaySentAt?: string;
  reminder90DaySent: boolean;
  reminder90DaySentAt?: string;
  
  // Metadata
  createdAt: string;
  updatedAt: string;
}

enum ReviewEligibilityStatus {
  ELIGIBLE = "ELIGIBLE",              // Can submit review
  NOT_ELIGIBLE = "NOT_ELIGIBLE",      // Cannot submit review
  PENDING_VERIFICATION = "PENDING_VERIFICATION"  // Waiting for 30/90 day confirmation
}

enum TerminationType {
  RESIGNATION = "RESIGNATION",
  FIRED_PERFORMANCE = "FIRED_PERFORMANCE",
  FIRED_MISCONDUCT = "FIRED_MISCONDUCT",
  LAID_OFF = "LAID_OFF",
  MUTUAL_AGREEMENT = "MUTUAL_AGREEMENT",
  PROBATION_FAILED = "PROBATION_FAILED",
  END_OF_CONTRACT = "END_OF_CONTRACT"
}
```

### ConfirmEmploymentRequest
```typescript
interface ConfirmEmploymentRequest {
  stillEmployed: boolean;             // true = still working, false = terminated
  
  // If stillEmployed = false, provide termination details
  terminationType?: TerminationType;
  terminationDate?: string;           // ISO 8601 date
  reasonForLeaving?: string;          // Optional explanation
}
```

### TerminateEmploymentRequest
```typescript
interface TerminateEmploymentRequest {
  terminationDate: string;            // ISO 8601 date (required)
  terminationType: TerminationType;   // Required
  reasonForLeaving?: string;          // Optional explanation
}
```

---

## API Endpoints

### 1. Create Employment Verification (Auto)

**Endpoint**: `POST /api/employment-verifications` (Auto-triggered)  
**Auth**: System/Internal  
**Description**: Automatically created when employment contract is signed (both parties).

#### Trigger
```
ContractSignatureController.signContract()
└── If both candidate + company signed:
    └── EmploymentVerificationService.createVerification(jobApplyId, startDate)
```

#### Auto-Generated Record
```json
{
  "id": 1001,
  "jobApplyId": 123,
  "startDate": "2025-11-26",
  "isActive": true,
  "verified30Days": false,
  "verified90Days": false,
  "reviewEligibility": "NOT_ELIGIBLE",
  "reviewEligibilityReason": "Employment duration less than 30 days",
  "daysEmployed": 0,
  "reminder30DaySent": false,
  "reminder90DaySent": false
}
```

---

### 2. Get Employment Verification Details

**Endpoint**: `GET /api/employment-verifications/{verificationId}`  
**Auth**: `CANDIDATE` (if own), `RECRUITER` (if own), or `ADMIN` role required  
**Description**: Get full details of employment verification record.

#### Response: `200 OK`
```json
{
  "id": 1001,
  "jobApplyId": 123,
  "jobTitle": "Senior Software Engineer",
  "companyName": "Tech Corp",
  "candidateName": "Jane Smith",
  "recruiterName": "John Recruiter",
  
  "startDate": "2025-10-01",
  "endDate": null,
  "isActive": true,
  
  "verified30Days": true,
  "verifiedAt30Days": "2025-11-02T10:30:00",
  "verified90Days": false,
  "verifiedAt90Days": null,
  
  "terminationType": null,
  "reasonForLeaving": null,
  
  "reviewEligibility": "ELIGIBLE",
  "reviewEligibilityReason": "Employed for 30+ days and verified at 30-day checkpoint",
  "daysEmployed": 56,
  
  "reminder30DaySent": true,
  "reminder30DaySentAt": "2025-10-31T09:00:00",
  "reminder90DaySent": false,
  
  "createdAt": "2025-10-01T14:00:00",
  "updatedAt": "2025-11-02T10:30:00"
}
```

---

### 3. Get Employment Verification by Job Apply

**Endpoint**: `GET /api/job-applies/{jobApplyId}/employment-verification`  
**Auth**: `CANDIDATE` (if own), `RECRUITER` (if own), or `ADMIN` role required  
**Description**: Get employment verification for specific job application.

#### Response: `200 OK`
```json
{
  "id": 1001,
  "jobApplyId": 123,
  "isActive": true,
  "daysEmployed": 56,
  "reviewEligibility": "ELIGIBLE",
  "verificationStatus": {
    "day30Verified": true,
    "day90Verified": false,
    "nextCheckpoint": "90_DAY",
    "daysUntilNextCheckpoint": 34
  }
}
```

#### 404 Not Found
```json
{
  "error": "EMPLOYMENT_VERIFICATION_NOT_FOUND",
  "message": "No employment verification found for this job application",
  "jobApplyId": 123
}
```

---

### 4. Confirm 30-Day Employment Status

**Endpoint**: `POST /api/employment-verifications/{verificationId}/confirm-30-days`  
**Auth**: `CANDIDATE` role required  
**Description**: Candidate confirms they are still employed at 30-day mark.

#### Request
```http
POST /api/employment-verifications/1001/confirm-30-days
Authorization: Bearer {candidate_token}
Content-Type: application/json

{
  "stillEmployed": true
}
```

#### Response: `200 OK`
```json
{
  "id": 1001,
  "verified30Days": true,
  "verifiedAt30Days": "2025-11-02T10:30:00",
  "reviewEligibility": "ELIGIBLE",
  "reviewEligibilityReason": "Employed for 30+ days and verified at 30-day checkpoint",
  "message": "Employment status confirmed. You are now eligible to submit a review."
}
```

#### Alternative: Terminated During 30-Day Period
```http
POST /api/employment-verifications/1001/confirm-30-days
Content-Type: application/json

{
  "stillEmployed": false,
  "terminationType": "RESIGNATION",
  "terminationDate": "2025-10-25",
  "reasonForLeaving": "Found better opportunity"
}
```

#### Response: `200 OK`
```json
{
  "id": 1001,
  "isActive": false,
  "endDate": "2025-10-25",
  "terminationType": "RESIGNATION",
  "daysEmployed": 25,
  "reviewEligibility": "NOT_ELIGIBLE",
  "reviewEligibilityReason": "Employment duration less than 30 days",
  "message": "Employment termination recorded. You are not eligible to review (employed < 30 days)."
}
```

---

### 5. Confirm 90-Day Employment Status

**Endpoint**: `POST /api/employment-verifications/{verificationId}/confirm-90-days`  
**Auth**: `CANDIDATE` role required  
**Description**: Candidate confirms they are still employed at 90-day mark.

#### Request
```http
POST /api/employment-verifications/1001/confirm-90-days
Authorization: Bearer {candidate_token}
Content-Type: application/json

{
  "stillEmployed": true
}
```

#### Response: `200 OK`
```json
{
  "id": 1001,
  "verified90Days": true,
  "verifiedAt90Days": "2025-12-30T11:00:00",
  "reviewEligibility": "ELIGIBLE",
  "reviewEligibilityReason": "Employed for 90+ days and verified at 90-day checkpoint",
  "message": "90-day employment milestone confirmed."
}
```

---

### 6. Terminate Employment

**Endpoint**: `POST /api/employment-verifications/{verificationId}/terminate`  
**Auth**: `CANDIDATE` or `RECRUITER` role required  
**Description**: Record employment termination (resignation, firing, layoff, etc.).

#### Request
```http
POST /api/employment-verifications/1001/terminate
Authorization: Bearer {candidate_token}
Content-Type: application/json

{
  "terminationDate": "2025-12-15",
  "terminationType": "RESIGNATION",
  "reasonForLeaving": "Relocating to another city"
}
```

#### Response: `200 OK`
```json
{
  "id": 1001,
  "isActive": false,
  "endDate": "2025-12-15",
  "terminationType": "RESIGNATION",
  "reasonForLeaving": "Relocating to another city",
  "daysEmployed": 75,
  "reviewEligibility": "ELIGIBLE",
  "reviewEligibilityReason": "Employed for 30+ days with verified 30-day checkpoint",
  "message": "Employment terminated. Review eligibility: ELIGIBLE"
}
```

#### Business Logic
- ✅ Sets `isActive = false`
- ✅ Sets `endDate` to termination date
- ✅ Recalculates `daysEmployed` (endDate - startDate)
- ✅ Recalculates `reviewEligibility` based on duration + verification status
- ✅ Updates related `JobApply` record (status = TERMINATED)
- ✅ Triggers status update notifications
- ❌ Cannot terminate if already terminated

---

## Reminder System

### Automated Reminder Schedule

#### 30-Day Reminder
```typescript
// Runs daily at 9:00 AM
@Scheduled(cron = "0 0 9 * * *")
public void send30DayReminders() {
  // Find all verifications at exactly 30 days (startDate + 30 days)
  List<EmploymentVerification> dueFor30DayCheck = 
    employmentVerificationRepo.findBy30DayReminderDue(
      LocalDate.now().minusDays(30),
      false  // reminder30DaySent = false
    );
  
  for (EmploymentVerification verification : dueFor30DayCheck) {
    // Send email + in-app notification
    notificationService.send30DayVerificationReminder(verification);
    
    // Mark reminder sent
    verification.setReminder30DaySent(true);
    verification.setReminder30DaySentAt(LocalDateTime.now());
    employmentVerificationRepo.save(verification);
  }
}
```

#### 90-Day Reminder
```typescript
// Runs daily at 9:00 AM
@Scheduled(cron = "0 0 9 * * *")
public void send90DayReminders() {
  // Find all verifications at exactly 90 days
  List<EmploymentVerification> dueFor90DayCheck = 
    employmentVerificationRepo.findBy90DayReminderDue(
      LocalDate.now().minusDays(90),
      true,  // verified30Days = true (must have passed 30-day check)
      false  // reminder90DaySent = false
    );
  
  for (EmploymentVerification verification : dueFor90DayCheck) {
    notificationService.send90DayVerificationReminder(verification);
    verification.setReminder90DaySent(true);
    verification.setReminder90DaySentAt(LocalDateTime.now());
    employmentVerificationRepo.save(verification);
  }
}
```

### Reminder Content

#### 30-Day Reminder Email
```
Subject: [CareerMate] 30-Day Employment Check-In: Are you still at Tech Corp?

Hi Jane,

It's been 30 days since you started your role as Senior Software Engineer at Tech Corp!

Please confirm your current employment status:
✅ Still employed → Click here to confirm
❌ No longer employed → Report termination

Why this matters:
- Confirms accurate employment records
- Enables you to submit company reviews
- Helps maintain platform integrity

You have 7 days to respond. If no response, you may lose review eligibility.

Confirm Employment Status: [Link]

Best regards,
CareerMate Team
```

#### 90-Day Reminder Email
```
Subject: [CareerMate] 90-Day Employment Milestone: Confirm your status

Hi Jane,

Congratulations on 90 days at Tech Corp! 🎉

Please confirm you are still employed:
✅ Still employed → Click here to confirm

This helps us maintain accurate records and ensures your review remains valid.

Confirm Employment Status: [Link]
```

---

## Review Eligibility Logic

### Eligibility Calculation

```typescript
function calculateReviewEligibility(verification: EmploymentVerification): ReviewEligibilityStatus {
  const daysEmployed = verification.getDaysEmployed();
  
  // Rule 1: Must be employed for at least 30 days
  if (daysEmployed < 30) {
    return {
      status: "NOT_ELIGIBLE",
      reason: "Employment duration less than 30 days"
    };
  }
  
  // Rule 2: If currently active and reached 30 days, must verify
  if (verification.isActive && daysEmployed >= 30 && daysEmployed < 37) {
    if (!verification.verified30Days) {
      return {
        status: "PENDING_VERIFICATION",
        reason: "Awaiting 30-day employment verification"
      };
    }
  }
  
  // Rule 3: If missed 30-day verification window (Day 37+), NOT ELIGIBLE
  if (verification.isActive && daysEmployed >= 37 && !verification.verified30Days) {
    return {
      status: "NOT_ELIGIBLE",
      reason: "Missed 30-day verification deadline (Day 37)"
    };
  }
  
  // Rule 4: If verified at 30 days, ELIGIBLE
  if (verification.verified30Days) {
    return {
      status: "ELIGIBLE",
      reason: "Employed for 30+ days and verified at 30-day checkpoint"
    };
  }
  
  // Rule 5: If terminated before 30 days, NOT ELIGIBLE
  if (!verification.isActive && daysEmployed < 30) {
    return {
      status: "NOT_ELIGIBLE",
      reason: "Employment duration less than 30 days"
    };
  }
  
  // Rule 6: If terminated after 30+ days with verification, ELIGIBLE
  if (!verification.isActive && daysEmployed >= 30 && verification.verified30Days) {
    return {
      status: "ELIGIBLE",
      reason: "Employed for 30+ days with verified 30-day checkpoint"
    };
  }
  
  // Default: NOT ELIGIBLE
  return {
    status: "NOT_ELIGIBLE",
    reason: "Does not meet review eligibility criteria"
  };
}
```

### Eligibility Matrix

| Scenario | Days Employed | Verified 30 Days | Verified 90 Days | Eligibility |
|---------|--------------|-----------------|-----------------|-------------|
| Active employee, < 30 days | 15 | ❌ | ❌ | ❌ NOT_ELIGIBLE |
| Active employee, 30-36 days | 32 | ❌ | ❌ | ⏳ PENDING_VERIFICATION |
| Active employee, 30-36 days | 32 | ✅ | ❌ | ✅ ELIGIBLE |
| Active employee, 37+ days | 40 | ❌ | ❌ | ❌ NOT_ELIGIBLE (missed deadline) |
| Active employee, 37+ days | 40 | ✅ | ❌ | ✅ ELIGIBLE |
| Active employee, 90+ days | 95 | ✅ | ✅ | ✅ ELIGIBLE |
| Terminated, < 30 days | 25 | ❌ | ❌ | ❌ NOT_ELIGIBLE |
| Terminated, 30+ days | 45 | ✅ | ❌ | ✅ ELIGIBLE |
| Terminated, 90+ days | 120 | ✅ | ✅ | ✅ ELIGIBLE |

---

## Business Rules

### Employment Verification Creation Rules

| Rule | Validation |
|------|------------|
| **Auto-created on contract signing** | Triggered when both parties sign contract |
| **One per JobApply** | Cannot have duplicate verifications |
| **Start date = contract date** | Uses `EmploymentContract.startDate` |

### Verification Confirmation Rules

| Rule | Validation |
|------|------------|
| **30-day window: Day 30-36** | Candidate has 7 days to confirm |
| **90-day window: Day 90-96** | Candidate has 7 days to confirm |
| **Cannot verify early** | Must wait until Day 30/90 |
| **Cannot verify late** | After Day 37/97, missed deadline |

### Termination Rules

| Rule | Validation |
|------|------------|
| **Cannot terminate twice** | Must be active employment |
| **Termination date <= today** | Cannot be future date |
| **Termination date >= start date** | Cannot be before employment started |
| **Updates JobApply** | Syncs status to TERMINATED |

### Review Eligibility Rules

| Rule | Validation |
|------|------------|
| **Minimum 30 days employed** | Absolute requirement |
| **Must verify at 30 days** | Or lose eligibility |
| **7-day grace period** | Day 30-36 to verify |
| **Recalculated on every update** | Always up-to-date |

---

## Error Handling

### Common Error Responses

#### 400 Bad Request - Already Verified
```json
{
  "error": "EMPLOYMENT_ALREADY_VERIFIED_30_DAYS",
  "message": "You have already verified your 30-day employment status",
  "verifiedAt": "2025-11-02T10:30:00"
}
```

#### 400 Bad Request - Too Early
```json
{
  "error": "VERIFICATION_TOO_EARLY",
  "message": "Cannot verify 30-day status yet. You can verify on or after Day 30.",
  "daysEmployed": 22,
  "verificationAvailableDate": "2025-11-08"
}
```

#### 400 Bad Request - Verification Deadline Passed
```json
{
  "error": "VERIFICATION_DEADLINE_PASSED",
  "message": "30-day verification deadline has passed (Day 37). You are no longer eligible for review.",
  "daysEmployed": 40,
  "deadlineDate": "2025-11-07"
}
```

#### 400 Bad Request - Employment Already Terminated
```json
{
  "error": "EMPLOYMENT_ALREADY_TERMINATED",
  "message": "This employment has already been terminated",
  "terminatedOn": "2025-12-15",
  "terminationType": "RESIGNATION"
}
```

#### 403 Forbidden - Not Authorized
```json
{
  "error": "UNAUTHORIZED_EMPLOYMENT_VERIFICATION",
  "message": "You can only verify your own employment status",
  "verificationId": 1001
}
```

---

## Frontend Integration Checklist

### 30-Day Verification Prompt
- [ ] In-app notification at Day 30 (banner/toast)
- [ ] Email reminder sent automatically
- [ ] Modal: "Are you still employed at [Company]?"
- [ ] Radio buttons: "Yes, still employed" / "No, I left"
- [ ] If "No": Show termination details form (type, date, reason)
- [ ] Countdown: "Verify by [Date] or lose review eligibility"
- [ ] "Confirm" button

### 90-Day Verification Prompt
- [ ] In-app notification at Day 90
- [ ] Email reminder sent automatically
- [ ] Modal: "90-day milestone! Confirm you're still employed"
- [ ] Radio buttons: "Yes, still employed" / "No, I left"
- [ ] If "No": Termination details form
- [ ] "Confirm" button

### Employment Verification Status Widget (Candidate Dashboard)
- [ ] Display current employment status (Active / Terminated)
- [ ] Show days employed counter
- [ ] Show verification checkpoints (✅ Day 30 verified, ⏳ Day 90 pending)
- [ ] Review eligibility badge (ELIGIBLE / NOT_ELIGIBLE / PENDING)
- [ ] Countdown to next checkpoint

### Termination Reporting Form
- [ ] Termination date picker (cannot be future date)
- [ ] Termination type dropdown (7 types)
- [ ] Reason textarea (optional)
- [ ] "Report Termination" button
- [ ] Confirmation: "This will mark your employment as ended"

### Admin Employment Verification Dashboard
- [ ] List all verifications (filter by active/terminated)
- [ ] Search by candidate/company name
- [ ] Show verification status (Day 30/90 checkmarks)
- [ ] Export to CSV button

---

**End of Employment Verification API Documentation**
