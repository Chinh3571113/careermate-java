# Status Update Verification API Documentation

**Module**: Bilateral Verification System  
**Base URL**: `/api`  
**Version**: 3.0  
**Last Updated**: November 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [The Problem & Solution](#the-problem--solution)
3. [Data Models](#data-models)
4. [API Endpoints](#api-endpoints)
5. [7-Day Auto-Approval Workflow](#7-day-auto-approval-workflow)
6. [Evidence System](#evidence-system)
7. [Business Rules](#business-rules)
8. [Error Handling](#error-handling)

---

## Overview

The **Bilateral Verification System** allows both candidates and recruiters to initiate employment status updates, preventing situations where one party fails to update the system. This creates accountability and accuracy in employment tracking.

### Why This Exists

**Traditional Problem:**
- Candidate resigns but recruiter never updates status → System thinks they're still employed
- Recruiter fires candidate but doesn't mark contract → Candidate appears "currently employed"
- No accountability when status updates are missed

**Bilateral Solution:**
- ✅ **Candidate can report** status changes (resignation, layoff, etc.)
- ✅ **Recruiter must verify** within 7 days or auto-approve
- ✅ **Evidence upload** (resignation letter, termination notice)
- ✅ **Dispute resolution** when parties disagree
- ✅ **Complete audit trail** of all status changes

---

## The Problem & Solution

### Scenario 1: Recruiter Doesn't Update Status

**Problem:**
```
Timeline:
Day 0:  Candidate resigns with 2 weeks notice
Day 15: Last working day
Day 30: Recruiter still hasn't updated JobApply.status
Result: System thinks candidate still employed → Incorrect review eligibility
```

**Solution:**
```
Timeline:
Day 0:  Candidate submits status update: "I resigned on [date]"
Day 0:  System creates StatusUpdateRequest (status: PENDING_VERIFICATION)
Day 0:  Recruiter receives notification with 7-day deadline
Day 3:  Recruiter confirms → Status updated immediately
OR
Day 7:  No recruiter response → Auto-approved → Status updated automatically
```

### Scenario 2: Conflicting Claims (Dispute)

**Problem:**
```
Recruiter claims: "Employee was FIRED for poor performance"
Candidate claims: "I RESIGNED with proper notice"
Both have evidence → Who is telling the truth?
```

**Solution:**
```
Timeline:
Day 0:  Candidate submits status update: "RESIGNED"
Day 1:  Recruiter disputes: "No, you were FIRED_PERFORMANCE"
Day 1:  System creates Dispute record
Day 1:  Admin receives high-priority notification
Day 2:  Admin reviews evidence from both parties
Day 2:  Admin makes final decision → Status updated accordingly
```

---

## Data Models

### CandidateStatusUpdateRequest
```typescript
interface CandidateStatusUpdateRequest {
  jobApplyId: number;
  newStatus: StatusJobApply;          // "TERMINATED", "WITHDRAWN", etc.
  
  // Termination details (if newStatus = TERMINATED)
  claimedTerminationType?: TerminationType;
  claimedTerminationDate?: string;    // ISO 8601 date
  reason: string;                      // Explanation (required)
  
  // Supporting evidence
  evidence?: EvidenceFile[];
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

interface EvidenceFile {
  type: EvidenceType;
  fileUrl: string;                    // Firebase Storage URL
  fileName: string;
  fileSizeBytes: number;
  mimeType: string;                   // "application/pdf", "image/png"
}

enum EvidenceType {
  RESIGNATION_LETTER = "RESIGNATION_LETTER",
  TERMINATION_LETTER = "TERMINATION_LETTER",
  EMAIL_FROM_HR = "EMAIL_FROM_HR",
  FINAL_PAYSLIP = "FINAL_PAYSLIP",
  PERFORMANCE_REVIEW = "PERFORMANCE_REVIEW",
  EMAIL_SCREENSHOT = "EMAIL_SCREENSHOT",
  PERSONAL_STATEMENT = "PERSONAL_STATEMENT"
}
```

### StatusUpdateResponse
```typescript
interface StatusUpdateResponse {
  id: number;
  jobApplyId: number;
  requestedBy: "CANDIDATE" | "RECRUITER" | "ADMIN";
  
  currentStatus: StatusJobApply;
  requestedStatus: StatusJobApply;
  
  claimedTerminationType?: TerminationType;
  claimedTerminationDate?: string;
  reason: string;
  
  status: UpdateRequestStatus;        // "PENDING_VERIFICATION", "CONFIRMED", "DISPUTED", "AUTO_APPROVED", "REJECTED"
  verificationDeadline: string;       // ISO 8601 datetime (now + 7 days)
  
  hoursUntilAutoApproval?: number;    // Countdown timer
  daysRemaining?: number;
  
  evidence: EvidenceFileResponse[];
  
  createdAt: string;
  updatedAt: string;
  
  confirmedByUserType?: "CANDIDATE" | "RECRUITER" | "ADMIN";
  confirmedAt?: string;
  confirmationNotes?: string;
  
  message: string;                    // User-friendly status message
}

enum UpdateRequestStatus {
  PENDING_VERIFICATION = "PENDING_VERIFICATION",
  CONFIRMED = "CONFIRMED",
  DISPUTED = "DISPUTED",
  AUTO_APPROVED = "AUTO_APPROVED",
  REJECTED = "REJECTED"
}
```

### RecruiterConfirmRequest
```typescript
interface RecruiterConfirmRequest {
  notes?: string;                     // Confirmation notes
  
  // Corrected values (if candidate's dates/type are wrong)
  actualTerminationType?: TerminationType;
  actualTerminationDate?: string;
}
```

### RecruiterDisputeRequest
```typescript
interface RecruiterDisputeRequest {
  // Recruiter's counter-claim
  recruiterClaimedStatus: StatusJobApply;
  recruiterClaimedTerminationType?: TerminationType;
  recruiterClaimedTerminationDate?: string;
  reason: string;                     // Why disputing
  
  // Counter-evidence
  counterEvidence?: EvidenceFile[];
}
```

---

## API Endpoints

### 1. Candidate Submits Status Update

**Endpoint**: `POST /api/job-applies/{jobApplyId}/candidate-status-update`  
**Auth**: `CANDIDATE` role required  
**Description**: Candidate initiates a status update request (e.g., "I resigned but status not updated").

#### Request
```http
POST /api/job-applies/123/candidate-status-update
Authorization: Bearer {candidate_token}
Content-Type: application/json

{
  "newStatus": "TERMINATED",
  "claimedTerminationType": "RESIGNATION",
  "claimedTerminationDate": "2025-11-15",
  "reason": "I resigned on November 15th with 2 weeks notice as per my contract. My last working day was November 29th. The recruiter has not updated my employment status.",
  "evidence": [
    {
      "type": "RESIGNATION_LETTER",
      "fileUrl": "https://firebase.storage/evidence/resignation-123.pdf",
      "fileName": "resignation-letter-signed.pdf",
      "fileSizeBytes": 245678,
      "mimeType": "application/pdf"
    },
    {
      "type": "EMAIL_FROM_HR",
      "fileUrl": "https://firebase.storage/evidence/hr-confirmation.png",
      "fileName": "hr-acceptance-email.png",
      "fileSizeBytes": 89234,
      "mimeType": "image/png"
    }
  ]
}
```

#### Response: `202 Accepted`
```json
{
  "id": 789,
  "jobApplyId": 123,
  "requestedBy": "CANDIDATE",
  "currentStatus": "ACCEPTED",
  "requestedStatus": "TERMINATED",
  "claimedTerminationType": "RESIGNATION",
  "claimedTerminationDate": "2025-11-15",
  "status": "PENDING_VERIFICATION",
  "verificationDeadline": "2025-12-03T23:59:59",
  "hoursUntilAutoApproval": 168,
  "daysRemaining": 7,
  "evidence": [
    {
      "id": 1001,
      "fileType": "RESIGNATION_LETTER",
      "fileName": "resignation-letter-signed.pdf",
      "trustScore": 10,
      "uploadedByUserType": "CANDIDATE"
    }
  ],
  "message": "Status update submitted. Recruiter will be notified to confirm. If no response within 7 days, status will automatically update on 2025-12-03.",
  "createdAt": "2025-11-26T10:00:00"
}
```

#### Business Logic
- ✅ Creates `StatusUpdateRequest` record (status: PENDING_VERIFICATION)
- ✅ Sets verification deadline: `now + 7 days`
- ✅ Uploads evidence files to Firebase Storage
- ✅ Sends notification to recruiter: "Candidate reported status change. Please verify within 7 days."
- ✅ Starts countdown timer for auto-approval
- ❌ Cannot submit if current status is not `ACCEPTED` (must be employed)
- ❌ Cannot submit if already have pending request

---

### 2. Recruiter Confirms Status Update

**Endpoint**: `POST /api/status-updates/{updateRequestId}/confirm`  
**Auth**: `RECRUITER` role required  
**Description**: Recruiter verifies and confirms the candidate's status change claim.

#### Request
```http
POST /api/status-updates/789/confirm
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "notes": "Confirmed. Candidate did resign with proper notice. Thank you for the notification.",
  "actualTerminationType": "RESIGNATION",
  "actualTerminationDate": "2025-11-29"
}
```

#### Response: `200 OK`
```json
{
  "id": 789,
  "status": "CONFIRMED",
  "confirmedByUserType": "RECRUITER",
  "confirmedAt": "2025-11-27T09:30:00",
  "jobApplyStatus": "TERMINATED",
  "employmentVerificationUpdated": true,
  "message": "Status update confirmed. JobApply status changed to TERMINATED.",
  "actions": [
    "JobApply.status updated to TERMINATED",
    "JobApply.leftAt set to 2025-11-29",
    "EmploymentVerification updated with termination details"
  ]
}
```

#### Business Logic
- ✅ Updates `StatusUpdateRequest.status` to `CONFIRMED`
- ✅ **Immediately updates** `JobApply.status` to requested status
- ✅ Updates `JobApply.leftAt` timestamp
- ✅ Updates `EmploymentVerification` with termination details
- ✅ Records status change history
- ✅ Sends confirmation notification to candidate
- ✅ **If recruiter provides corrected dates**: Uses recruiter's values instead
- ❌ Cannot confirm if status is not `PENDING_VERIFICATION`

---

### 3. Recruiter Disputes Status Update

**Endpoint**: `POST /api/status-updates/{updateRequestId}/dispute`  
**Auth**: `RECRUITER` role required  
**Description**: Recruiter challenges the candidate's claim and provides counter-evidence.

#### Request
```http
POST /api/status-updates/789/dispute
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "recruiterClaimedStatus": "TERMINATED",
  "recruiterClaimedTerminationType": "FIRED_PERFORMANCE",
  "recruiterClaimedTerminationDate": "2025-11-10",
  "reason": "Employee was terminated for poor performance on November 10th, not resignation. We have documented performance issues and termination letter.",
  "counterEvidence": [
    {
      "type": "TERMINATION_LETTER",
      "fileUrl": "https://firebase.storage/evidence/termination-letter.pdf",
      "fileName": "termination-letter-november.pdf",
      "fileSizeBytes": 312456,
      "mimeType": "application/pdf"
    },
    {
      "type": "PERFORMANCE_REVIEW",
      "fileUrl": "https://firebase.storage/evidence/performance-review.pdf",
      "fileName": "q3-performance-review.pdf",
      "fileSizeBytes": 198765,
      "mimeType": "application/pdf"
    }
  ]
}
```

#### Response: `202 Accepted`
```json
{
  "id": 789,
  "status": "DISPUTED",
  "disputeId": 555,
  "message": "Dispute filed. Case escalated to admin review.",
  "actions": [
    "StatusUpdateRequest.status set to DISPUTED",
    "StatusDispute record created (ID: 555)",
    "Admin notified for arbitration",
    "JobApply.status remains unchanged until resolution"
  ],
  "nextSteps": "Admin will review evidence from both parties and make final decision.",
  "createdAt": "2025-11-27T10:00:00"
}
```

#### Business Logic
- ✅ Updates `StatusUpdateRequest.status` to `DISPUTED`
- ✅ Creates `StatusDispute` record with both parties' claims
- ✅ Uploads recruiter's counter-evidence
- ✅ Calculates dispute priority score (based on evidence quality + days open)
- ✅ Sends high-priority notification to admin
- ✅ Notifies candidate: "Recruiter disputed your claim. Admin will review."
- ✅ **JobApply.status remains unchanged** until admin resolves
- ❌ Cannot dispute if status is not `PENDING_VERIFICATION`

---

### 4. Get Status Update Request Details

**Endpoint**: `GET /api/status-updates/{updateRequestId}`  
**Auth**: `CANDIDATE`, `RECRUITER`, or `ADMIN` role required  
**Description**: Retrieve full details of a status update request.

#### Response: `200 OK`
```json
{
  "id": 789,
  "jobApplyId": 123,
  "requestedBy": "CANDIDATE",
  "currentStatus": "ACCEPTED",
  "requestedStatus": "TERMINATED",
  "claimedTerminationType": "RESIGNATION",
  "claimedTerminationDate": "2025-11-15",
  "reason": "I resigned on November 15th with 2 weeks notice...",
  "status": "PENDING_VERIFICATION",
  "verificationDeadline": "2025-12-03T23:59:59",
  "hoursUntilAutoApproval": 144,
  "daysRemaining": 6,
  "evidence": [
    {
      "id": 1001,
      "fileType": "RESIGNATION_LETTER",
      "fileUrl": "https://firebase.storage/evidence/resignation-123.pdf",
      "fileName": "resignation-letter-signed.pdf",
      "trustScore": 10,
      "uploadedByUserType": "CANDIDATE",
      "uploadedAt": "2025-11-26T10:00:00"
    }
  ],
  "createdAt": "2025-11-26T10:00:00"
}
```

---

### 5. Get Recruiter's Pending Requests

**Endpoint**: `GET /api/recruiters/{recruiterId}/status-updates/pending`  
**Auth**: `RECRUITER` or `ADMIN` role required  
**Description**: Get all status updates awaiting recruiter verification.

#### Response: `200 OK`
```json
{
  "pendingCount": 3,
  "requests": [
    {
      "id": 789,
      "candidateName": "Jane Smith",
      "jobTitle": "Software Engineer",
      "requestedStatus": "TERMINATED",
      "claimedTerminationType": "RESIGNATION",
      "claimedTerminationDate": "2025-11-15",
      "daysRemaining": 5,
      "hoursUntilAutoApproval": 120,
      "hasEvidence": true,
      "evidenceCount": 2,
      "createdAt": "2025-11-26T10:00:00",
      "verificationDeadline": "2025-12-03T23:59:59",
      "priority": "NORMAL"
    },
    {
      "id": 790,
      "candidateName": "John Doe",
      "jobTitle": "Product Manager",
      "requestedStatus": "TERMINATED",
      "claimedTerminationType": "LAID_OFF",
      "daysRemaining": 1,
      "hoursUntilAutoApproval": 18,
      "priority": "URGENT",
      "warningMessage": "Auto-approves in 18 hours if no action taken"
    }
  ]
}
```

---

### 6. Get Pending Request Count

**Endpoint**: `GET /api/recruiters/{recruiterId}/status-updates/pending/count`  
**Auth**: `RECRUITER` or `ADMIN` role required  
**Description**: Get total number of pending verifications (for dashboard badge).

#### Response: `200 OK`
```json
{
  "pendingCount": 3,
  "urgentCount": 1,
  "expiringWithin24h": 1
}
```

---

### 7. Get Status Update History by Job Apply

**Endpoint**: `GET /api/job-applies/{jobApplyId}/status-updates`  
**Auth**: `CANDIDATE`, `RECRUITER`, or `ADMIN` role required  
**Description**: Get all status update requests for a job application.

#### Response: `200 OK`
```json
{
  "jobApplyId": 123,
  "currentStatus": "TERMINATED",
  "requests": [
    {
      "id": 789,
      "requestedBy": "CANDIDATE",
      "requestedStatus": "TERMINATED",
      "status": "CONFIRMED",
      "confirmedAt": "2025-11-27T09:30:00",
      "daysToConfirm": 1
    },
    {
      "id": 765,
      "requestedBy": "CANDIDATE",
      "requestedStatus": "WITHDRAWN",
      "status": "REJECTED",
      "rejectedAt": "2025-10-15T14:00:00"
    }
  ]
}
```

---

### 8. Get Requests Expiring Soon

**Endpoint**: `GET /api/status-updates/expiring-soon`  
**Auth**: `RECRUITER` or `ADMIN` role required  
**Description**: Get requests within 24 hours of auto-approval.

#### Response: `200 OK`
```json
{
  "count": 2,
  "requests": [
    {
      "id": 790,
      "candidateName": "John Doe",
      "recruiterName": "Tech Corp",
      "hoursUntilAutoApproval": 18,
      "verificationDeadline": "2025-11-28T10:00:00"
    }
  ],
  "message": "These requests will auto-approve if not responded to within 24 hours"
}
```

---

### 9. Manual Trigger Auto-Approval (Admin)

**Endpoint**: `POST /api/admin/status-updates/auto-approve`  
**Auth**: `ADMIN` role required  
**Description**: Manually trigger the auto-approval process (normally runs as scheduled job).

#### Response: `200 OK`
```json
{
  "autoApprovedCount": 5,
  "message": "5 requests auto-approved due to expired verification deadline",
  "approvedRequests": [
    {
      "id": 789,
      "jobApplyId": 123,
      "candidateName": "Jane Smith",
      "newStatus": "TERMINATED"
    }
  ]
}
```

---

## 7-Day Auto-Approval Workflow

### Timeline Visualization

```
Day 0 (Nov 26):  Candidate submits status update
                 ├── StatusUpdateRequest created (PENDING_VERIFICATION)
                 ├── Verification deadline: Dec 3, 23:59:59
                 └── Recruiter notified

Day 1-6:         ⏳ Waiting for recruiter response...
                 └── Daily reminder emails sent

Day 6 (Dec 2):   🚨 Urgent notification: "Auto-approves in 24 hours"

Day 7 (Dec 3):   ⚠️ 2-hour warning: "Auto-approves in 2 hours"

Day 7 23:59:59:  ✅ AUTO-APPROVED
                 ├── StatusUpdateRequest.status = AUTO_APPROVED
                 ├── JobApply.status updated to TERMINATED
                 ├── EmploymentVerification updated
                 └── Both parties notified
```

### Automated Reminder Schedule

| Time Before Expiry | Notification Type | Priority |
|--------------------|-------------------|----------|
| **7 days** | Initial notification | Normal |
| **5 days** | Reminder email | Normal |
| **3 days** | Reminder email | Medium |
| **24 hours** | Urgent email + in-app | High |
| **2 hours** | Final warning | Critical |
| **0 hours** | Auto-approve + notify | Automatic |

### Scheduled Job

```typescript
// Runs daily at 1:00 AM
@Scheduled(cron = "0 0 1 * * *")
public void autoApproveExpiredRequests() {
  // Find all PENDING_VERIFICATION requests past deadline
  List<StatusUpdateRequest> expiredRequests = 
    statusUpdateRepo.findExpiredRequests(
      StatusUpdateRequestStatus.PENDING_VERIFICATION,
      LocalDateTime.now()
    );
  
  for (StatusUpdateRequest request : expiredRequests) {
    // Auto-approve
    request.setStatus(StatusUpdateRequestStatus.AUTO_APPROVED);
    request.setConfirmedAt(LocalDateTime.now());
    request.setConfirmedByUserType(UserType.ADMIN); // System
    
    // Update JobApply
    JobApply jobApply = request.getJobApply();
    jobApply.setStatus(request.getRequestedStatus());
    jobApply.setLeftAt(request.getClaimedTerminationDate());
    
    // Notify both parties
    notificationService.sendAutoApprovalNotification(request);
  }
}
```

---

## Evidence System

### Evidence Trust Scoring

| Evidence Type | Trust Score (1-10) | Description |
|--------------|-------------------|-------------|
| **RESIGNATION_LETTER** | 10 | Official signed document |
| **TERMINATION_LETTER** | 10 | Official company document |
| **FINAL_PAYSLIP** | 9 | Bank/payroll verified |
| **EMAIL_FROM_HR** | 8 | Official HR communication |
| **PERFORMANCE_REVIEW** | 7 | Company records |
| **EMAIL_SCREENSHOT** | 5 | Can be faked |
| **PERSONAL_STATEMENT** | 3 | No external validation |

### Evidence Validation

```typescript
interface EvidenceFileResponse {
  id: number;
  fileType: EvidenceType;
  fileUrl: string;
  fileName: string;
  fileSizeBytes: number;
  mimeType: string;
  
  uploadedByUserType: "CANDIDATE" | "RECRUITER";
  uploadedByUserId: number;
  uploadedAt: string;
  
  trustScore: number;                 // 1-10 based on evidence type
  verifiedByAdmin: boolean;           // Admin manually verified
  isAuthentic?: boolean;              // Admin's authenticity ruling
  
  needsVerification: boolean;         // Requires admin review
}
```

### Admin Evidence Review

When evidence has low trust score (< 5), admin can manually verify:

1. **View Evidence**: Download and inspect files
2. **Mark as Authentic**: Sets `isAuthentic = true`, increases trust score
3. **Mark as Fake**: Sets `isAuthentic = false`, flags request
4. **Request More Evidence**: Send message to uploader

---

## Business Rules

### Eligibility Rules

| Rule | Validation |
|------|------------|
| **Must be employed** | Current status = `ACCEPTED` |
| **No duplicate requests** | Cannot have pending request |
| **Valid status transition** | `ACCEPTED` → `TERMINATED` only |
| **Evidence required** | At least 1 file for termination claims |
| **Minimum 7-day deadline** | Cannot be shorter |

### Confirmation Rules

| Rule | Validation |
|------|------------|
| **Only pending confirmable** | Status must be `PENDING_VERIFICATION` |
| **Recruiter can correct dates** | Use recruiter's values if provided |
| **Updates cascade** | JobApply + EmploymentVerification sync |
| **Audit trail** | All changes logged |

### Dispute Rules

| Rule | Validation |
|------|------------|
| **Counter-evidence required** | Recruiter must upload proof |
| **Admin arbitration mandatory** | Cannot auto-resolve disputes |
| **Status frozen** | JobApply unchanged until resolution |
| **Priority calculation** | Evidence quality + days open |

### Auto-Approval Rules

| Rule | Validation |
|------|------------|
| **7-day deadline** | Fixed, cannot extend |
| **Runs daily at 1 AM** | Scheduled job |
| **Notifies both parties** | Email + in-app notification |
| **No disputes allowed** | Once auto-approved, final |

---

## Error Handling

### Common Error Responses

#### 400 Bad Request - Already Have Pending Request
```json
{
  "error": "PENDING_STATUS_UPDATE_EXISTS",
  "message": "You already have a pending status update request. Please wait for recruiter response or for auto-approval.",
  "existingRequestId": 789,
  "verificationDeadline": "2025-12-03T23:59:59",
  "daysRemaining": 5
}
```

#### 400 Bad Request - Invalid Status Transition
```json
{
  "error": "INVALID_STATUS_TRANSITION",
  "message": "Cannot request status update. Current status must be ACCEPTED (employed).",
  "currentStatus": "INTERVIEW_SCHEDULED",
  "requiredStatus": "ACCEPTED"
}
```

#### 400 Bad Request - Request Already Processed
```json
{
  "error": "STATUS_UPDATE_REQUEST_NOT_PENDING",
  "message": "This status update request has already been processed.",
  "currentStatus": "CONFIRMED",
  "processedAt": "2025-11-27T09:30:00"
}
```

#### 403 Forbidden - Not Authorized
```json
{
  "error": "UNAUTHORIZED_STATUS_UPDATE",
  "message": "You can only update status for your own job applications",
  "jobApplyId": 123
}
```

---

## Frontend Integration Checklist

### Candidate Status Update Form
- [ ] Current status display (read-only)
- [ ] New status selector (TERMINATED, WITHDRAWN)
- [ ] Termination type dropdown (if TERMINATED)
- [ ] Termination date picker
- [ ] Reason textarea (required, min 20 chars)
- [ ] Evidence upload (multiple files, PDF/PNG)
- [ ] Preview uploaded files
- [ ] "Submit Status Update" button
- [ ] Disclaimer: "Recruiter will be notified. If no response in 7 days, status will auto-update."

### Recruiter Pending Requests Dashboard
- [ ] List of all pending requests (sorted by urgency)
- [ ] Countdown timer showing days/hours until auto-approval
- [ ] Priority badges (URGENT if < 24h)
- [ ] Evidence preview (thumbnails/download links)
- [ ] "Confirm" and "Dispute" buttons
- [ ] Filter by job title, candidate name, date
- [ ] Search functionality

### Recruiter Confirmation Modal
- [ ] Display candidate's claim (status, type, date, reason)
- [ ] Show all uploaded evidence
- [ ] Optional: Correct dates/type fields
- [ ] Notes textarea
- [ ] "Confirm Status Update" button
- [ ] Confirmation: "This will immediately update employment status"

### Recruiter Dispute Modal
- [ ] Display candidate's claim
- [ ] Recruiter's counter-claim form (status, type, date, reason)
- [ ] Counter-evidence upload
- [ ] "File Dispute" button
- [ ] Warning: "Case will be escalated to admin review"

### Status Update History Page
- [ ] Timeline view of all status updates
- [ ] Color-coded status badges
- [ ] Evidence icons (show count)
- [ ] Expand/collapse details
- [ ] Download evidence button

---

**End of Status Update Verification API Documentation**
