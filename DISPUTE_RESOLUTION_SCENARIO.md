# Dispute Resolution System - Complete Scenario & Flow

## 📋 Table of Contents
1. [Real-World Dispute Scenario](#real-world-dispute-scenario)
2. [Complete Workflow Diagram](#complete-workflow-diagram)
3. [Step-by-Step Flow](#step-by-step-flow)
4. [API Call Sequence](#api-call-sequence)
5. [Database State Changes](#database-state-changes)
6. [UI/UX Flows](#uiux-flows)
7. [Edge Cases & Error Handling](#edge-cases--error-handling)

---

## 🎭 Real-World Dispute Scenario

### The Players
- **Sarah Chen** - Candidate (ID: 789)
- **Tech Corp** - Company (via Recruiter Michael Lee, ID: 456)
- **Admin Team** - Platform arbitrators

### The Timeline

#### March 1, 2026 - Employment Begins
- Sarah hired as Senior Software Engineer
- Contract signed, employment status: `ACCEPTED`
- Start date: March 1, 2026

#### March 10, 2026 - Incident Occurs
- **Tech Corp's Version**: Sarah's performance declined, missed deadlines, terminated for cause
- **Sarah's Version**: Still employed, no warning given

#### March 15, 2026 - Sarah's Action
- **Sarah's Version**: Resigned due to toxic work environment
- **Tech Corp's Version**: Already fired 5 days ago

#### March 20, 2026 - Dispute Filed
- Sarah submits status update: "I resigned on March 15"
- Recruiter disputes: "You were fired on March 10"
- **THE CONFLICT**: Who's telling the truth?

---

## 🔄 Complete Workflow Diagram

```mermaid
sequenceDiagram
    participant Sarah as Sarah (Candidate)
    participant System as CareerMate System
    participant Michael as Michael (Recruiter)
    participant Admin as Admin Team
    participant DB as Database

    Note over Sarah,DB: Phase 1: Candidate Reports Status Change
    Sarah->>System: POST /api/job-applies/123/candidate-status-update
    activate System
    System->>DB: Create StatusUpdateRequest (ID: 789)
    System->>DB: Upload 2 evidence files
    System->>Michael: Send email: "Action Required: Verify Status"
    System-->>Sarah: 202 Accepted (updateRequestId: 789)
    deactivate System
    Note right of Sarah: Status: PENDING_VERIFICATION<br/>Deadline: March 27 (7 days)

    Note over Sarah,DB: Phase 2: Recruiter Disputes Claim
    Michael->>System: POST /api/status-updates/789/dispute
    activate System
    System->>DB: Create StatusDispute (ID: 456)
    System->>DB: Upload 3 counter-evidence files
    System->>DB: Update StatusUpdateRequest.status = DISPUTED
    System->>Admin: Create admin task
    System->>Sarah: Send email: "Dispute Filed - Under Review"
    System-->>Michael: 202 Accepted (disputeId: 456)
    deactivate System
    Note right of Michael: Dispute Status: OPEN<br/>Admin Review Needed

    Note over Sarah,DB: Phase 3: Admin Investigation
    Admin->>System: GET /api/admin/disputes/456
    activate System
    System->>DB: Query dispute details
    System->>DB: Query evidence files (5 total)
    System->>DB: Calculate trust scores
    System-->>Admin: Complete dispute package
    deactivate System
    
    Admin->>System: GET /api/admin/disputes/456/recommendation
    activate System
    System->>System: Analyze evidence timestamps
    System->>System: Check document authenticity
    System->>System: Score candidate vs recruiter claims
    System-->>Admin: Recommendation: "Favor Recruiter"
    deactivate System
    Note right of Admin: Evidence Analysis:<br/>- Termination letter (Mar 10)<br/>- Resignation letter (Mar 15)<br/>- Earlier date wins

    Note over Sarah,DB: Phase 4: Admin Resolution
    Admin->>System: POST /api/admin/disputes/456/resolve
    activate System
    System->>DB: Update StatusDispute.status = RESOLVED
    System->>DB: Update JobApply.status = TERMINATED
    System->>DB: Update EmploymentContract.terminationType = FIRED_PERFORMANCE
    System->>DB: Update EmploymentContract.terminationDate = 2026-03-10
    System->>DB: Create JobApplyStatusHistory record
    System->>Sarah: Send email with admin decision
    System->>Michael: Send email confirming resolution
    System-->>Admin: 200 OK (Resolution complete)
    deactivate System
    Note right of Admin: Case Closed<br/>Final: FIRED_PERFORMANCE<br/>Date: March 10, 2026

    Note over Sarah,DB: Phase 5: Review Eligibility Updated
    System->>DB: Recalculate review eligibility
    Note right of Sarah: Can now review:<br/>✅ Application<br/>✅ Interview<br/>❌ Work Experience (< 30 days)
```

---

## 📝 Step-by-Step Flow

### Phase 1: Candidate Initiates Status Update (March 20, 2026)

#### Sarah's Dashboard Shows:
```
┌────────────────────────────────────────────────┐
│ Your Employment at Tech Corp                  │
├────────────────────────────────────────────────┤
│ Status: ACCEPTED (Employed)                    │
│ Hired: March 1, 2026                           │
│ Days Employed: 19 days                         │
│                                                 │
│ ⚠️ Status Incorrect?                           │
│ [Report Status Change]                         │
└────────────────────────────────────────────────┘
```

#### Sarah Clicks "Report Status Change":

**Form Fields:**
```javascript
{
  jobApplyId: 123,
  newStatus: "TERMINATED",
  claimedTerminationType: "RESIGNATION",
  claimedTerminationDate: "2026-03-15T17:00:00",
  reason: "I resigned on March 15th due to toxic work environment and lack of support. I provided 2 weeks notice but was told to leave immediately. My resignation letter was accepted by my manager.",
  evidence: [
    {
      fileType: "RESIGNATION_LETTER",
      file: resignation_letter.pdf,
      description: "Signed resignation letter submitted to manager"
    },
    {
      fileType: "EMAIL_SCREENSHOT",
      file: manager_response.png,
      description: "Manager's email accepting my resignation"
    }
  ]
}
```

#### API Call:
```http
POST /api/job-applies/123/candidate-status-update
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "newStatus": "TERMINATED",
  "claimedTerminationType": "RESIGNATION",
  "claimedTerminationDate": "2026-03-15T17:00:00",
  "reason": "I resigned on March 15th due to toxic work environment...",
  "evidence": [...]
}
```

#### System Response:
```json
{
  "updateRequestId": 789,
  "status": "PENDING_VERIFICATION",
  "message": "Status update submitted. Recruiter has 7 days to verify.",
  "verificationDeadline": "2026-03-27T23:59:59",
  "autoApproveDate": "2026-03-27",
  "nextSteps": "If recruiter doesn't respond, status will automatically update on March 27."
}
```

#### Database Changes:
```sql
-- Insert StatusUpdateRequest
INSERT INTO status_update_request (
  job_apply_id, requested_by, current_status, requested_status,
  claimed_termination_type, claimed_termination_date, reason,
  status, verification_deadline, created_at
) VALUES (
  123, 'CANDIDATE', 'ACCEPTED', 'TERMINATED',
  'RESIGNATION', '2026-03-15 17:00:00', 'I resigned on March 15th...',
  'PENDING_VERIFICATION', '2026-03-27 23:59:59', '2026-03-20 10:30:00'
);
-- Returns ID: 789

-- Insert Evidence Files
INSERT INTO evidence_file (
  status_update_request_id, file_type, file_url, file_name,
  uploaded_by_user_type, uploaded_by_user_id, uploaded_at
) VALUES 
  (789, 'RESIGNATION_LETTER', '/uploads/789/resignation_letter.pdf', 
   'resignation_letter.pdf', 'CANDIDATE', 789, '2026-03-20 10:30:00'),
  (789, 'EMAIL_SCREENSHOT', '/uploads/789/manager_response.png',
   'manager_response.png', 'CANDIDATE', 789, '2026-03-20 10:30:01');
```

#### Email to Recruiter (Michael):
```
Subject: Action Required: Verify Employment Status Update

Hi Michael,

Sarah Chen has reported a change in employment status for the position 
"Senior Software Engineer" at Tech Corp.

Claimed Status: TERMINATED (Resignation)
Claimed Date: March 15, 2026
Reason: "I resigned on March 15th due to toxic work environment..."

Evidence Provided:
- Resignation letter (PDF)
- Manager email screenshot

You have until March 27, 2026 to:
1. Confirm this status change is accurate
2. Dispute this claim if incorrect

If no response is received, the status will automatically update.

[Review Status Update] [Confirm] [Dispute]
```

---

### Phase 2: Recruiter Disputes Claim (March 21, 2026)

#### Michael's Dashboard Shows:
```
┌────────────────────────────────────────────────┐
│ Pending Status Verifications (1)              │
├────────────────────────────────────────────────┤
│ ⚠️ Sarah Chen - Senior Software Engineer       │
│                                                 │
│ Candidate Claims: RESIGNATION (Mar 15)         │
│ Current Status: ACCEPTED (Employed)            │
│                                                 │
│ ⏰ Deadline: March 27, 2026 (6 days left)      │
│                                                 │
│ [View Details] [Confirm] [Dispute]            │
└────────────────────────────────────────────────┘
```

#### Michael Clicks "Dispute":

**Dispute Form Fields:**
```javascript
{
  updateRequestId: 789,
  disputed: true,
  recruiterClaimedStatus: "TERMINATED",
  recruiterClaimedTerminationType: "FIRED_PERFORMANCE",
  recruiterClaimedTerminationDate: "2026-03-10T14:00:00",
  reason: "Employee was terminated on March 10th for performance issues, including missed deadlines and failure to meet project requirements. This occurred 5 days before the alleged resignation date.",
  counterEvidence: [
    {
      fileType: "TERMINATION_LETTER",
      file: termination_notice.pdf,
      description: "Official termination letter signed by HR"
    },
    {
      fileType: "PERFORMANCE_REVIEW",
      file: performance_review_feb.pdf,
      description: "February performance review showing issues"
    },
    {
      fileType: "EMAIL_SCREENSHOT",
      file: termination_meeting_invite.png,
      description: "Meeting invite for termination discussion on March 10"
    }
  ]
}
```

#### API Call:
```http
POST /api/status-updates/789/dispute
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "disputed": true,
  "recruiterClaimedStatus": "TERMINATED",
  "recruiterClaimedTerminationType": "FIRED_PERFORMANCE",
  "recruiterClaimedTerminationDate": "2026-03-10T14:00:00",
  "reason": "Employee was terminated on March 10th for performance issues...",
  "counterEvidence": [...]
}
```

#### System Response:
```json
{
  "updateRequestId": 789,
  "status": "DISPUTED",
  "disputeId": 456,
  "message": "Dispute filed. Case escalated to admin review.",
  "timeline": "Admin will review within 5 business days",
  "adminReviewUrl": "/admin/disputes/456"
}
```

#### Database Changes:
```sql
-- Update StatusUpdateRequest
UPDATE status_update_request
SET status = 'DISPUTED', updated_at = '2026-03-21 09:15:00'
WHERE id = 789;

-- Create StatusDispute
INSERT INTO status_dispute (
  status_update_request_id, job_apply_id, status,
  candidate_claimed_status, candidate_claimed_termination_type,
  candidate_claimed_termination_date, candidate_reason,
  recruiter_claimed_status, recruiter_claimed_termination_type,
  recruiter_claimed_termination_date, recruiter_reason,
  created_at
) VALUES (
  789, 123, 'OPEN',
  'TERMINATED', 'RESIGNATION', '2026-03-15 17:00:00',
  'I resigned on March 15th due to toxic work environment...',
  'TERMINATED', 'FIRED_PERFORMANCE', '2026-03-10 14:00:00',
  'Employee was terminated on March 10th for performance issues...',
  '2026-03-21 09:15:00'
);
-- Returns ID: 456

-- Insert Recruiter's Evidence
INSERT INTO evidence_file (
  dispute_id, file_type, file_url, file_name,
  uploaded_by_user_type, uploaded_by_user_id, uploaded_at
) VALUES 
  (456, 'TERMINATION_LETTER', '/uploads/456/termination_notice.pdf',
   'termination_notice.pdf', 'RECRUITER', 456, '2026-03-21 09:15:00'),
  (456, 'PERFORMANCE_REVIEW', '/uploads/456/performance_review_feb.pdf',
   'performance_review_feb.pdf', 'RECRUITER', 456, '2026-03-21 09:15:01'),
  (456, 'EMAIL_SCREENSHOT', '/uploads/456/termination_meeting_invite.png',
   'termination_meeting_invite.png', 'RECRUITER', 456, '2026-03-21 09:15:02');
```

#### Email to Sarah:
```
Subject: Status Update Disputed - Under Admin Review

Hi Sarah,

The recruiter has disputed your employment status update claim.

Your Claim: RESIGNATION (March 15, 2026)
Recruiter's Claim: FIRED_PERFORMANCE (March 10, 2026)

Your case has been escalated to our admin team for review. An impartial 
administrator will review all evidence from both parties and make a final 
decision within 5 business days.

What happens next:
1. Admin reviews all evidence
2. Admin may request additional documentation
3. Final decision will be legally binding
4. Both parties will be notified of the outcome

Case ID: 456

[View Dispute Details]
```

---

### Phase 3: Admin Investigation (March 22-24, 2026)

#### Admin Dashboard Shows:
```
┌────────────────────────────────────────────────┐
│ Open Disputes (15)                             │
├────────────────────────────────────────────────┤
│ 🔴 HIGH PRIORITY (3)                           │
│                                                 │
│ #456 - Tech Corp vs Sarah Chen                 │
│ Opened: March 21, 2026 (2 days ago)           │
│ Issue: RESIGNATION vs FIRED_PERFORMANCE        │
│ Evidence: 5 files                              │
│ Recommendation: Favor Recruiter (85% confidence)│
│                                                 │
│ [Review Case]                                  │
├────────────────────────────────────────────────┤
│ 🟡 MEDIUM PRIORITY (7)                         │
│ 🟢 LOW PRIORITY (5)                            │
└────────────────────────────────────────────────┘
```

#### Admin Clicks "Review Case" - API Call 1:
```http
GET /api/admin/disputes/456
Authorization: Bearer admin_token...
```

#### System Response:
```json
{
  "disputeId": 456,
  "jobApplyId": 123,
  "status": "OPEN",
  "createdAt": "2026-03-21T09:15:00",
  "daysOpen": 2,
  
  "candidate": {
    "id": 789,
    "name": "Sarah Chen",
    "email": "sarah.chen@email.com",
    "accountCreated": "2025-11-15",
    "totalApplications": 12,
    "previousDisputes": 0,
    "trustScore": 85
  },
  
  "recruiter": {
    "id": 456,
    "name": "Michael Lee",
    "email": "michael.lee@techcorp.com",
    "company": "Tech Corp",
    "accountCreated": "2024-03-10",
    "totalHires": 48,
    "disputeHistory": 2,
    "trustScore": 78
  },
  
  "jobPosting": {
    "id": 999,
    "title": "Senior Software Engineer",
    "department": "Engineering",
    "hiredDate": "2026-03-01"
  },
  
  "candidateClaim": {
    "status": "TERMINATED",
    "terminationType": "RESIGNATION",
    "terminationDate": "2026-03-15T17:00:00",
    "reason": "I resigned on March 15th due to toxic work environment and lack of support. I provided 2 weeks notice but was told to leave immediately.",
    "evidence": [
      {
        "id": 1001,
        "fileType": "RESIGNATION_LETTER",
        "fileName": "resignation_letter.pdf",
        "fileUrl": "/uploads/789/resignation_letter.pdf",
        "uploadedAt": "2026-03-20T10:30:00",
        "trustScore": 7
      },
      {
        "id": 1002,
        "fileType": "EMAIL_SCREENSHOT",
        "fileName": "manager_response.png",
        "fileUrl": "/uploads/789/manager_response.png",
        "uploadedAt": "2026-03-20T10:30:01",
        "trustScore": 5
      }
    ],
    "totalEvidenceScore": 12
  },
  
  "recruiterClaim": {
    "status": "TERMINATED",
    "terminationType": "FIRED_PERFORMANCE",
    "terminationDate": "2026-03-10T14:00:00",
    "reason": "Employee was terminated on March 10th for performance issues, including missed deadlines and failure to meet project requirements.",
    "evidence": [
      {
        "id": 1003,
        "fileType": "TERMINATION_LETTER",
        "fileName": "termination_notice.pdf",
        "fileUrl": "/uploads/456/termination_notice.pdf",
        "uploadedAt": "2026-03-21T09:15:00",
        "trustScore": 10
      },
      {
        "id": 1004,
        "fileType": "PERFORMANCE_REVIEW",
        "fileName": "performance_review_feb.pdf",
        "fileUrl": "/uploads/456/performance_review_feb.pdf",
        "uploadedAt": "2026-03-21T09:15:01",
        "trustScore": 6
      },
      {
        "id": 1005,
        "fileType": "EMAIL_SCREENSHOT",
        "fileName": "termination_meeting_invite.png",
        "fileUrl": "/uploads/456/termination_meeting_invite.png",
        "uploadedAt": "2026-03-21T09:15:02",
        "trustScore": 5
      }
    ],
    "totalEvidenceScore": 21
  },
  
  "timeline": [
    {
      "date": "2026-03-01",
      "event": "Employment started",
      "source": "System"
    },
    {
      "date": "2026-03-10",
      "event": "Recruiter claims termination occurred",
      "source": "Recruiter"
    },
    {
      "date": "2026-03-15",
      "event": "Candidate claims resignation occurred",
      "source": "Candidate"
    },
    {
      "date": "2026-03-20",
      "event": "Candidate filed status update request",
      "source": "System"
    },
    {
      "date": "2026-03-21",
      "event": "Recruiter disputed claim",
      "source": "System"
    }
  ],
  
  "systemAnalysis": {
    "evidenceQualityComparison": {
      "candidateScore": 12,
      "recruiterScore": 21,
      "advantage": "RECRUITER"
    },
    "dateConflict": {
      "candidateDate": "2026-03-15",
      "recruiterDate": "2026-03-10",
      "daysDifference": 5,
      "earlierClaim": "RECRUITER"
    },
    "documentAuthenticity": {
      "candidateDocuments": "Moderate confidence",
      "recruiterDocuments": "High confidence (official letterhead)"
    }
  }
}
```

#### Admin Requests Recommendation - API Call 2:
```http
GET /api/admin/disputes/456/recommendation
Authorization: Bearer admin_token...
```

#### System Response:
```json
{
  "disputeId": 456,
  "recommendation": "FAVOR_RECRUITER",
  "confidenceScore": 85,
  "reasoning": [
    "Recruiter's termination letter dated March 10 (5 days before candidate's resignation)",
    "Recruiter provided official HR documentation with company letterhead",
    "Performance review from February supports termination claim",
    "Termination date conflict: Recruiter's date is earlier",
    "Evidence quality: Recruiter (21 points) > Candidate (12 points)",
    "Recruiter's trust score (78) is acceptable despite being lower than candidate (85)"
  ],
  "suggestedResolution": {
    "finalStatus": "TERMINATED",
    "finalTerminationType": "FIRED_PERFORMANCE",
    "finalTerminationDate": "2026-03-10",
    "favoredParty": "RECRUITER",
    "draftNoteToCandidate": "After reviewing all evidence, the official termination letter dated March 10, 2026 predates your resignation letter by 5 days. The employment status has been updated to reflect termination on March 10.",
    "draftNoteToRecruiter": "Dispute resolved in your favor. The candidate's employment record now reflects termination for performance reasons on March 10, 2026."
  },
  "alternativeConsiderations": [
    "Candidate's resignation letter may have been submitted after being informed of termination",
    "Possible mutual agreement scenario not fully explored",
    "Consider requesting additional evidence if available"
  ]
}
```

---

### Phase 4: Admin Makes Decision (March 24, 2026)

#### Admin Reviews Evidence and Decides:

**Decision Form:**
```javascript
{
  disputeId: 456,
  finalStatus: "TERMINATED",
  finalTerminationType: "FIRED_PERFORMANCE",
  finalTerminationDate: "2026-03-10T14:00:00",
  resolutionReason: "After thorough review of all evidence, the official termination letter dated March 10, 2026, signed by HR and on company letterhead, predates the resignation letter by 5 days. The performance review from February corroborates performance issues. Based on the preponderance of evidence and document authenticity, the employment termination is determined to have occurred on March 10, 2026, for performance-related reasons.",
  favoredParty: "RECRUITER",
  decisionBasis: "EARLIER_DOCUMENT_DATE, DOCUMENT_AUTHENTICITY, EVIDENCE_QUALITY",
  noteToCandidate: "Dear Sarah, after careful review of all evidence provided, the termination letter dated March 10, 2026 predates your resignation letter. Your employment status has been updated to reflect termination on this date. You may still leave a review of your experience with Tech Corp. If you believe this decision was made in error, you may appeal within 14 days.",
  noteToRecruiter: "Dear Michael, the dispute has been resolved in favor of your claim. The candidate's employment record now reflects termination for performance reasons on March 10, 2026. Thank you for providing thorough documentation."
}
```

#### API Call:
```http
POST /api/admin/disputes/456/resolve
Authorization: Bearer admin_token...
Content-Type: application/json

{
  "finalStatus": "TERMINATED",
  "finalTerminationType": "FIRED_PERFORMANCE",
  "finalTerminationDate": "2026-03-10T14:00:00",
  "resolutionReason": "After thorough review of all evidence...",
  "favoredParty": "RECRUITER",
  "decisionBasis": "EARLIER_DOCUMENT_DATE, DOCUMENT_AUTHENTICITY, EVIDENCE_QUALITY",
  "noteToCandidate": "Dear Sarah, after careful review...",
  "noteToRecruiter": "Dear Michael, the dispute has been resolved..."
}
```

#### System Response:
```json
{
  "disputeId": 456,
  "status": "RESOLVED",
  "resolvedAt": "2026-03-24T14:30:00",
  "resolvedByAdminId": 101,
  "finalDecision": {
    "status": "TERMINATED",
    "terminationType": "FIRED_PERFORMANCE",
    "terminationDate": "2026-03-10T14:00:00",
    "favoredParty": "RECRUITER"
  },
  "updatedRecords": {
    "jobApplyUpdated": true,
    "employmentContractUpdated": true,
    "statusHistoryCreated": true,
    "reviewEligibilityRecalculated": true
  },
  "notificationsSent": {
    "candidateNotified": true,
    "recruiterNotified": true
  }
}
```

#### Database Changes:
```sql
-- Update StatusDispute
UPDATE status_dispute
SET 
  status = 'RESOLVED',
  resolved_by_admin_id = 101,
  resolved_at = '2026-03-24 14:30:00',
  final_status = 'TERMINATED',
  final_termination_type = 'FIRED_PERFORMANCE',
  final_termination_date = '2026-03-10 14:00:00',
  resolution_reason = 'After thorough review of all evidence...',
  note_to_candidate = 'Dear Sarah, after careful review...',
  note_to_recruiter = 'Dear Michael, the dispute has been resolved...',
  updated_at = '2026-03-24 14:30:00'
WHERE id = 456;

-- Update StatusUpdateRequest
UPDATE status_update_request
SET 
  status = 'REJECTED',
  updated_at = '2026-03-24 14:30:00'
WHERE id = 789;

-- Update JobApply
UPDATE job_apply
SET 
  status = 'TERMINATED',
  left_at = '2026-03-10 14:00:00',
  status_changed_at = '2026-03-24 14:30:00',
  updated_at = '2026-03-24 14:30:00'
WHERE id = 123;

-- Update EmploymentContract
UPDATE employment_contract
SET
  status = 'TERMINATED',
  termination_type = 'FIRED_PERFORMANCE',
  termination_date = '2026-03-10',
  days_employed = 9,  -- March 1 to March 10
  termination_reason = 'Performance issues including missed deadlines',
  updated_at = '2026-03-24 14:30:00'
WHERE job_apply_id = 123;

-- Create JobApplyStatusHistory
INSERT INTO job_apply_status_history (
  job_apply_id, previous_status, new_status, changed_at,
  changed_by_user_id, change_reason
) VALUES (
  123, 'ACCEPTED', 'TERMINATED', '2026-03-24 14:30:00',
  101, 'Admin dispute resolution: Determined termination occurred on March 10, 2026'
);
```

---

## 🔄 API Call Sequence Summary

### Complete Flow:

1. **Candidate Reports Status**
   ```
   POST /api/job-applies/123/candidate-status-update
   → Creates StatusUpdateRequest (ID: 789)
   → Uploads 2 evidence files
   → Sends email to recruiter
   ```

2. **Recruiter Disputes**
   ```
   POST /api/status-updates/789/dispute
   → Creates StatusDispute (ID: 456)
   → Uploads 3 counter-evidence files
   → Updates StatusUpdateRequest to DISPUTED
   → Sends email to candidate and admin
   ```

3. **Admin Reviews**
   ```
   GET /api/admin/disputes?status=OPEN
   → Lists all open disputes
   
   GET /api/admin/disputes/456
   → Gets complete dispute details with all evidence
   
   GET /api/admin/disputes/456/recommendation
   → Gets AI-powered recommendation
   ```

4. **Admin Resolves**
   ```
   POST /api/admin/disputes/456/resolve
   → Updates StatusDispute to RESOLVED
   → Updates JobApply status and dates
   → Updates EmploymentContract
   → Creates status history record
   → Sends final decision emails
   → Recalculates review eligibility
   ```

5. **Review Eligibility Check**
   ```
   GET /api/reviews/eligibility/candidate/789/job-apply/123
   → Returns allowed review types based on final status
   ```

---

## 💾 Database State Changes

### Before Dispute:
```
job_apply (ID: 123)
├── status: ACCEPTED
├── hired_at: 2026-03-01 08:00:00
├── left_at: NULL
└── status_changed_at: 2026-03-01 08:00:00

employment_contract (job_apply_id: 123)
├── status: ACTIVE
├── start_date: 2026-03-01
├── termination_date: NULL
├── termination_type: NULL
└── days_employed: NULL

status_update_request: (None)
status_dispute: (None)
evidence_file: (None)
```

### After Candidate Reports (March 20):
```
status_update_request (ID: 789)
├── status: PENDING_VERIFICATION
├── requested_status: TERMINATED
├── claimed_termination_type: RESIGNATION
├── claimed_termination_date: 2026-03-15
└── verification_deadline: 2026-03-27 23:59:59

evidence_file
├── ID: 1001 (resignation_letter.pdf)
└── ID: 1002 (manager_response.png)

job_apply (ID: 123) - UNCHANGED
```

### After Recruiter Disputes (March 21):
```
status_update_request (ID: 789)
└── status: DISPUTED (UPDATED)

status_dispute (ID: 456) - NEW
├── status: OPEN
├── candidate_claimed_termination_type: RESIGNATION
├── candidate_claimed_termination_date: 2026-03-15
├── recruiter_claimed_termination_type: FIRED_PERFORMANCE
└── recruiter_claimed_termination_date: 2026-03-10

evidence_file - ADDED 3 MORE
├── ID: 1003 (termination_notice.pdf)
├── ID: 1004 (performance_review_feb.pdf)
└── ID: 1005 (termination_meeting_invite.png)

job_apply (ID: 123) - STILL UNCHANGED (awaiting admin)
```

### After Admin Resolves (March 24):
```
status_dispute (ID: 456)
├── status: RESOLVED (UPDATED)
├── resolved_by_admin_id: 101
├── resolved_at: 2026-03-24 14:30:00
├── final_status: TERMINATED
├── final_termination_type: FIRED_PERFORMANCE
└── final_termination_date: 2026-03-10

status_update_request (ID: 789)
└── status: REJECTED (admin sided with recruiter)

job_apply (ID: 123) - FINALLY UPDATED
├── status: TERMINATED (UPDATED)
├── left_at: 2026-03-10 14:00:00 (UPDATED)
└── status_changed_at: 2026-03-24 14:30:00 (UPDATED)

employment_contract (job_apply_id: 123)
├── status: TERMINATED (UPDATED)
├── termination_date: 2026-03-10 (UPDATED)
├── termination_type: FIRED_PERFORMANCE (UPDATED)
├── days_employed: 9 (UPDATED)
└── termination_reason: Performance issues... (UPDATED)

job_apply_status_history - NEW RECORD
├── job_apply_id: 123
├── previous_status: ACCEPTED
├── new_status: TERMINATED
├── changed_by_user_id: 101 (admin)
└── change_reason: Admin dispute resolution
```

---

## 🎨 UI/UX Flows

### Candidate Journey:

```
Dashboard → "Status Incorrect?" Button
    ↓
Status Update Form
    ├── Dropdown: Select new status
    ├── Date picker: When did this happen?
    ├── Textarea: Explain what happened
    └── File upload: Upload evidence
    ↓
Submit → Loading spinner
    ↓
Success Modal: "Status update submitted"
    ├── "Recruiter has 7 days to verify"
    ├── "Auto-approve on March 27 if no response"
    └── [View Status] button
    ↓
Wait for Recruiter Response
    ├── Email notification: "Recruiter disputed your claim"
    └── Dashboard shows: "Under Admin Review"
    ↓
Wait for Admin Decision (3-5 days)
    ↓
Email notification: "Dispute Resolved"
    ↓
Dashboard updated:
    ├── Status: TERMINATED
    ├── Termination Type: FIRED_PERFORMANCE
    ├── Date: March 10, 2026
    └── Note from admin
    ↓
[Leave Review] button enabled
```

### Recruiter Journey:

```
Email: "Action Required: Verify Status Update"
    ↓
Dashboard → "Pending Verifications (1)" Badge
    ↓
Status Update Details Page
    ├── Candidate's claim details
    ├── Evidence files (view/download)
    ├── Timeline
    └── Action buttons: [Confirm] [Dispute]
    ↓
Click [Dispute]
    ↓
Dispute Form
    ├── Dropdown: What actually happened?
    ├── Date picker: When did it happen?
    ├── Textarea: Explain your version
    └── File upload: Upload counter-evidence
    ↓
Submit → Loading spinner
    ↓
Success Modal: "Dispute filed"
    ├── "Admin will review within 5 business days"
    └── [Track Dispute] button
    ↓
Wait for Admin Decision
    ↓
Email notification: "Dispute Resolved in Your Favor"
    ↓
Dashboard updated: Case closed
```

### Admin Journey:

```
Dashboard → "Open Disputes (15)" Card
    ├── HIGH PRIORITY (3) - Red badge
    ├── MEDIUM PRIORITY (7) - Yellow badge
    └── LOW PRIORITY (5) - Green badge
    ↓
Click on Dispute #456
    ↓
Dispute Details Page (Split View)
    ├── LEFT PANEL: Candidate's Claim
    │   ├── Status claimed
    │   ├── Date claimed
    │   ├── Reason
    │   ├── Evidence files (2)
    │   └── Trust score: 12/20
    ├── RIGHT PANEL: Recruiter's Counter-Claim
    │   ├── Status claimed
    │   ├── Date claimed
    │   ├── Reason
    │   ├── Evidence files (3)
    │   └── Trust score: 21/30
    └── BOTTOM: System Recommendation
        ├── "Favor Recruiter (85% confidence)"
        ├── Reasoning bullets
        └── Suggested resolution text
    ↓
Admin Reviews Evidence
    ├── Download all files
    ├── View timeline
    ├── Check user history
    └── Compare document dates
    ↓
Decision Form
    ├── Radio: Favor Candidate / Favor Recruiter / Compromise
    ├── Dropdown: Final termination type
    ├── Date picker: Final termination date
    ├── Textarea: Resolution reasoning
    ├── Textarea: Note to candidate
    └── Textarea: Note to recruiter
    ↓
[Resolve Dispute] button
    ↓
Confirmation Modal
    ├── "This decision is final and binding"
    ├── Summary of changes
    └── [Confirm] [Cancel]
    ↓
Success: "Dispute Resolved"
    ├── Emails sent to both parties
    ├── JobApply status updated
    └── Contract updated
    ↓
Back to Dashboard (14 open disputes remaining)
```

---

## ⚠️ Edge Cases & Error Handling

### Edge Case 1: Recruiter Never Responds

**Scenario**: 7 days pass, no recruiter action

**System Behavior**:
```java
@Scheduled(cron = "0 0 3 * * ?")  // Daily at 3 AM
public void autoApproveExpiredRequests() {
    LocalDateTime now = LocalDateTime.now();
    
    List<StatusUpdateRequest> expired = statusUpdateRequestRepo
        .findByStatusAndVerificationDeadlineBefore(
            "PENDING_VERIFICATION", now
        );
    
    for (StatusUpdateRequest request : expired) {
        // Auto-approve candidate's claim
        jobApplyService.updateStatus(
            request.getJobApplyId(),
            request.getRequestedStatus(),
            request.getClaimedTerminationDate(),
            request.getClaimedTerminationType()
        );
        
        request.setStatus("AUTO_APPROVED");
        statusUpdateRequestRepo.save(request);
        
        // Notify both parties
        notificationService.sendAutoApprovalNotification(request);
    }
}
```

**Result**: Candidate's claim automatically accepted on March 27, 2026

---

### Edge Case 2: Equal Evidence Quality

**Scenario**: Both parties provide equally strong evidence

**Admin Decision Guide**:
1. Check document dates (earlier wins)
2. Check document authenticity (official > screenshots)
3. Check user trust scores
4. Default to candidate (benefit of doubt)
5. Request additional evidence if needed

---

### Edge Case 3: Candidate Appeals Admin Decision

**Scenario**: Sarah wants to appeal the decision

**API Call**:
```http
POST /api/admin/disputes/456/appeal
Authorization: Bearer candidate_token...
Content-Type: application/json

{
  "appealReason": "The termination letter was backdated. I have email evidence showing I was still working on March 12.",
  "newEvidence": [
    {
      "fileType": "EMAIL_SCREENSHOT",
      "file": work_email_march_12.png
    }
  ]
}
```

**System Behavior**:
- Creates new dispute with `appealOf: 456`
- Requires different admin to review
- 14-day appeal window from resolution date

---

### Edge Case 4: Both Claims Are Wrong (Compromise)

**Scenario**: Admin discovers neither party is fully correct

**Resolution**:
```json
{
  "finalStatus": "TERMINATED",
  "finalTerminationType": "MUTUAL_AGREEMENT",
  "finalTerminationDate": "2026-03-12",
  "favoredParty": "COMPROMISE",
  "resolutionReason": "Evidence suggests termination occurred on March 10, but resignation letter shows employee was still working on March 12. This appears to be a mutual separation negotiated between March 10-15."
}
```

---

### Edge Case 5: Evidence Files Corrupted/Invalid

**Validation**:
```java
public void validateEvidenceFile(MultipartFile file) {
    // Check file size
    if (file.getSize() > 10_000_000) {  // 10MB
        throw new AppException(ErrorCode.FILE_TOO_LARGE);
    }
    
    // Check file type
    String contentType = file.getContentType();
    if (!ALLOWED_TYPES.contains(contentType)) {
        throw new AppException(ErrorCode.INVALID_FILE_TYPE);
    }
    
    // Scan for malware
    if (virusScanService.isInfected(file)) {
        throw new AppException(ErrorCode.FILE_INFECTED);
    }
    
    // Check if image is readable
    if (contentType.startsWith("image/")) {
        try {
            ImageIO.read(file.getInputStream());
        } catch (Exception e) {
            throw new AppException(ErrorCode.CORRUPTED_IMAGE);
        }
    }
}
```

---

### Edge Case 6: Dispute Deadline Approaching

**Reminder System**:
```java
@Scheduled(cron = "0 0 10 * * ?")  // Daily at 10 AM
public void sendDisputeDeadlineReminders() {
    LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
    
    List<StatusUpdateRequest> expiringSoon = statusUpdateRequestRepo
        .findByStatusAndVerificationDeadlineBetween(
            "PENDING_VERIFICATION",
            tomorrow.minusHours(12),
            tomorrow.plusHours(12)
        );
    
    for (StatusUpdateRequest request : expiringSoon) {
        emailService.sendUrgentReminder(
            request.getJobApply().getRecruiter().getEmail(),
            "URGENT: Status verification expires in 24 hours",
            request
        );
    }
}
```

---

## 📊 Success Metrics

### Dispute Resolution KPIs:

- **Average Resolution Time**: 3-5 business days
- **Auto-Approval Rate**: ~30% (recruiters don't respond)
- **Dispute Rate**: ~15% of status update requests
- **Admin Decision Accuracy**: 95% (low appeal rate)
- **Evidence Quality Score**: Average 15/30 (both parties)

### User Satisfaction:

- **Candidate Trust**: 78% feel process is fair
- **Recruiter Compliance**: 70% respond within 7 days
- **Admin Efficiency**: 12 disputes resolved per day per admin

---

## 🔐 Security & Privacy

### PII Protection:
- Evidence files encrypted at rest
- Access logs for all evidence downloads
- Evidence deleted after 90 days (GDPR compliance)
- Admin dashboard requires 2FA

### Audit Trail:
- All admin actions logged with timestamps
- IP addresses recorded for evidence uploads
- Email notifications tracked for delivery

---

This document provides a complete end-to-end understanding of the dispute resolution system with real-world context and implementation details.
