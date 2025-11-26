# Dispute Resolution API Documentation

**Module**: Admin Arbitration System  
**Base URL**: `/api`  
**Version**: 3.0  
**Last Updated**: November 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [When Disputes Occur](#when-disputes-occur)
3. [Data Models](#data-models)
4. [API Endpoints](#api-endpoints)
5. [Priority Scoring Algorithm](#priority-scoring-algorithm)
6. [Evidence Trust Scoring](#evidence-trust-scoring)
7. [Business Rules](#business-rules)
8. [Error Handling](#error-handling)

---

## Overview

The **Dispute Resolution System** handles conflicts when candidates and recruiters disagree on employment status changes. Admins act as neutral arbitrators, reviewing evidence from both parties and making final binding decisions.

### What This System Does

✅ **Arbitrate Disputes**: Admin reviews conflicting claims  
✅ **Evidence Evaluation**: Trust scoring system rates evidence quality  
✅ **Priority Queue**: High-impact disputes handled first  
✅ **Final Decision**: Admin ruling updates all systems  
✅ **Audit Trail**: Complete history of dispute process  

---

## When Disputes Occur

### Typical Dispute Scenarios

#### Scenario 1: Resignation vs. Termination
```
Candidate claims: "I RESIGNED with proper notice"
Evidence: Resignation letter dated Nov 10

Recruiter claims: "Employee was FIRED for poor performance"
Evidence: Termination letter dated Nov 8, performance reviews

Admin must determine: Who is telling the truth?
```

#### Scenario 2: Termination Date Disagreement
```
Candidate claims: Last working day Nov 30
Evidence: Email from manager confirming last day

Recruiter claims: Last working day Nov 15
Evidence: Final payslip ending Nov 15

Admin must determine: What is the actual termination date?
```

#### Scenario 3: Mutual Agreement vs. Layoff
```
Candidate claims: "Company LAID OFF 50% of engineering team"
Evidence: Company announcement, news articles

Recruiter claims: "Employee agreed to MUTUAL_AGREEMENT"
Evidence: Signed separation agreement

Admin must determine: Was this a layoff or mutual agreement?
```

---

## Data Models

### DisputeResponse
```typescript
interface DisputeResponse {
  id: number;
  statusUpdateRequestId: number;
  
  // Job details
  jobApplyId: number;
  jobTitle: string;
  companyName: string;
  
  // Parties involved
  candidateId: number;
  candidateName: string;
  recruiterId: number;
  recruiterName: string;
  
  // Candidate's claim
  candidateClaimedStatus: StatusJobApply;
  candidateClaimedTerminationType?: TerminationType;
  candidateClaimedTerminationDate?: string;
  candidateReason: string;
  candidateEvidence: EvidenceFileResponse[];
  candidateEvidenceTrustScore: number;  // Average of all evidence
  
  // Recruiter's counter-claim
  recruiterClaimedStatus: StatusJobApply;
  recruiterClaimedTerminationType?: TerminationType;
  recruiterClaimedTerminationDate?: string;
  recruiterReason: string;
  recruiterEvidence: EvidenceFileResponse[];
  recruiterEvidenceTrustScore: number;
  
  // Dispute metadata
  status: DisputeStatus;
  priority: DisputePriority;
  priorityScore: number;               // 0-100 calculated score
  
  // Admin resolution
  resolvedById?: number;
  resolvedByAdminName?: string;
  resolvedAt?: string;
  resolutionNotes?: string;
  
  finalDecision?: {
    rulingInFavorOf: "CANDIDATE" | "RECRUITER";
    finalStatus: StatusJobApply;
    finalTerminationType?: TerminationType;
    finalTerminationDate?: string;
    reasoning: string;
  };
  
  createdAt: string;
  updatedAt: string;
  
  // Calculated fields
  daysOpen: number;
  hoursOpen: number;
  needsUrgentAttention: boolean;       // true if priority = CRITICAL
}

enum DisputeStatus {
  OPEN = "OPEN",                       // Awaiting admin review
  IN_REVIEW = "IN_REVIEW",             // Admin currently reviewing
  RESOLVED_CANDIDATE = "RESOLVED_CANDIDATE",
  RESOLVED_RECRUITER = "RESOLVED_RECRUITER",
  RESOLVED_COMPROMISE = "RESOLVED_COMPROMISE"
}

enum DisputePriority {
  LOW = "LOW",                         // Priority score 0-30
  NORMAL = "NORMAL",                   // Priority score 31-60
  HIGH = "HIGH",                       // Priority score 61-80
  CRITICAL = "CRITICAL"                // Priority score 81-100
}
```

### AdminResolveDisputeRequest
```typescript
interface AdminResolveDisputeRequest {
  rulingInFavorOf: "CANDIDATE" | "RECRUITER" | "COMPROMISE";
  
  // Final decided values
  finalStatus: StatusJobApply;
  finalTerminationType?: TerminationType;
  finalTerminationDate?: string;
  
  // Explanation
  reasoning: string;                   // Required, min 50 chars
  
  // Evidence evaluation
  candidateEvidenceAuthentic: boolean;
  recruiterEvidenceAuthentic: boolean;
  
  // Optional actions
  flagForReview?: {
    targetUserType: "CANDIDATE" | "RECRUITER";
    reason: string;                    // e.g., "Submitted fake documents"
  };
}
```

### DisputeHistoryResponse
```typescript
interface DisputeHistoryResponse {
  disputeId: number;
  jobApplyId: number;
  
  timeline: DisputeTimelineEvent[];
  evidenceUploaded: EvidenceFileResponse[];
  adminNotes: AdminNote[];
  
  finalOutcome: {
    status: DisputeStatus;
    rulingInFavorOf?: "CANDIDATE" | "RECRUITER" | "COMPROMISE";
    finalStatus?: StatusJobApply;
    resolvedAt?: string;
  };
}

interface DisputeTimelineEvent {
  timestamp: string;
  actor: "CANDIDATE" | "RECRUITER" | "ADMIN" | "SYSTEM";
  action: string;
  description: string;
}

interface AdminNote {
  id: number;
  adminName: string;
  note: string;
  createdAt: string;
  isPrivate: boolean;                  // Hidden from candidate/recruiter
}
```

---

## API Endpoints

### 1. Get All Disputes (Admin Dashboard)

**Endpoint**: `GET /api/admin/disputes`  
**Auth**: `ADMIN` role required  
**Description**: Get all disputes, sorted by priority and creation date.

#### Query Parameters
```
?status=OPEN                          // Filter by dispute status
&priority=CRITICAL                    // Filter by priority level
&sortBy=priority_desc                 // Sort options: priority_desc, priority_asc, date_desc, date_asc
&page=0&size=20                       // Pagination
```

#### Response: `200 OK`
```json
{
  "totalCount": 12,
  "openCount": 8,
  "criticalCount": 2,
  "disputes": [
    {
      "id": 555,
      "jobTitle": "Senior Software Engineer",
      "companyName": "Tech Corp",
      "candidateName": "Jane Smith",
      "recruiterName": "John Recruiter",
      
      "status": "OPEN",
      "priority": "CRITICAL",
      "priorityScore": 92,
      "needsUrgentAttention": true,
      
      "daysOpen": 5,
      "createdAt": "2025-11-21T10:00:00",
      
      "claimSummary": {
        "candidate": "RESIGNED",
        "recruiter": "FIRED_PERFORMANCE"
      },
      
      "evidenceQuality": {
        "candidateScore": 9.5,
        "recruiterScore": 8.0
      }
    }
  ],
  "pagination": {
    "currentPage": 0,
    "totalPages": 1,
    "pageSize": 20
  }
}
```

---

### 2. Get Dispute Details

**Endpoint**: `GET /api/admin/disputes/{disputeId}`  
**Auth**: `ADMIN` role required  
**Description**: Get full details of a specific dispute including all evidence.

#### Response: `200 OK`
```json
{
  "id": 555,
  "statusUpdateRequestId": 789,
  "jobApplyId": 123,
  "jobTitle": "Senior Software Engineer",
  "companyName": "Tech Corp",
  
  "candidateId": 456,
  "candidateName": "Jane Smith",
  "candidateClaimedStatus": "TERMINATED",
  "candidateClaimedTerminationType": "RESIGNATION",
  "candidateClaimedTerminationDate": "2025-11-15",
  "candidateReason": "I resigned on November 15th with 2 weeks notice as per my contract. My last working day was November 29th.",
  "candidateEvidence": [
    {
      "id": 1001,
      "fileType": "RESIGNATION_LETTER",
      "fileUrl": "https://firebase.storage/evidence/resignation-123.pdf",
      "fileName": "resignation-letter-signed.pdf",
      "trustScore": 10,
      "uploadedByUserType": "CANDIDATE",
      "uploadedAt": "2025-11-20T10:00:00",
      "verifiedByAdmin": false
    },
    {
      "id": 1002,
      "fileType": "EMAIL_FROM_HR",
      "fileUrl": "https://firebase.storage/evidence/hr-confirmation.png",
      "fileName": "hr-acceptance-email.png",
      "trustScore": 8,
      "uploadedByUserType": "CANDIDATE",
      "uploadedAt": "2025-11-20T10:05:00"
    }
  ],
  "candidateEvidenceTrustScore": 9.0,
  
  "recruiterId": 789,
  "recruiterName": "John Recruiter",
  "recruiterClaimedStatus": "TERMINATED",
  "recruiterClaimedTerminationType": "FIRED_PERFORMANCE",
  "recruiterClaimedTerminationDate": "2025-11-10",
  "recruiterReason": "Employee was terminated for poor performance on November 10th. We have documented performance issues.",
  "recruiterEvidence": [
    {
      "id": 1003,
      "fileType": "TERMINATION_LETTER",
      "fileUrl": "https://firebase.storage/evidence/termination-letter.pdf",
      "fileName": "termination-letter-november.pdf",
      "trustScore": 10,
      "uploadedByUserType": "RECRUITER",
      "uploadedAt": "2025-11-21T14:00:00"
    },
    {
      "id": 1004,
      "fileType": "PERFORMANCE_REVIEW",
      "fileUrl": "https://firebase.storage/evidence/performance-review.pdf",
      "fileName": "q3-performance-review.pdf",
      "trustScore": 7,
      "uploadedByUserType": "RECRUITER",
      "uploadedAt": "2025-11-21T14:05:00"
    }
  ],
  "recruiterEvidenceTrustScore": 8.5,
  
  "status": "OPEN",
  "priority": "CRITICAL",
  "priorityScore": 92,
  "daysOpen": 5,
  "hoursOpen": 120,
  "needsUrgentAttention": true,
  
  "createdAt": "2025-11-21T10:00:00"
}
```

---

### 3. Assign Dispute to Admin (Start Review)

**Endpoint**: `POST /api/admin/disputes/{disputeId}/assign`  
**Auth**: `ADMIN` role required  
**Description**: Mark dispute as being reviewed by current admin.

#### Response: `200 OK`
```json
{
  "id": 555,
  "status": "IN_REVIEW",
  "assignedToAdminId": 999,
  "assignedToAdminName": "Admin Sarah",
  "assignedAt": "2025-11-26T09:00:00",
  "message": "Dispute assigned to you for review"
}
```

---

### 4. Resolve Dispute (Final Decision)

**Endpoint**: `POST /api/admin/disputes/{disputeId}/resolve`  
**Auth**: `ADMIN` role required  
**Description**: Make final ruling on dispute and update all systems.

#### Request
```http
POST /api/admin/disputes/555/resolve
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "rulingInFavorOf": "CANDIDATE",
  "finalStatus": "TERMINATED",
  "finalTerminationType": "RESIGNATION",
  "finalTerminationDate": "2025-11-29",
  "reasoning": "After reviewing evidence from both parties, the candidate's resignation letter is dated November 15th and shows proper 2-week notice. The recruiter's termination letter is dated November 10th, which is BEFORE the resignation date, suggesting retroactive documentation. The candidate's email from HR accepting the resignation further supports their claim. Ruling in favor of candidate.",
  "candidateEvidenceAuthentic": true,
  "recruiterEvidenceAuthentic": false,
  "flagForReview": {
    "targetUserType": "RECRUITER",
    "reason": "Submitted termination letter dated before candidate's resignation, suggesting falsified documentation"
  }
}
```

#### Response: `200 OK`
```json
{
  "id": 555,
  "status": "RESOLVED_CANDIDATE",
  "resolvedAt": "2025-11-26T10:00:00",
  "resolvedByAdminName": "Admin Sarah",
  
  "finalDecision": {
    "rulingInFavorOf": "CANDIDATE",
    "finalStatus": "TERMINATED",
    "finalTerminationType": "RESIGNATION",
    "finalTerminationDate": "2025-11-29",
    "reasoning": "After reviewing evidence..."
  },
  
  "actionsPerformed": [
    "StatusUpdateRequest status changed to CONFIRMED",
    "JobApply.status updated to TERMINATED",
    "JobApply.terminationType set to RESIGNATION",
    "JobApply.leftAt set to 2025-11-29",
    "EmploymentVerification updated",
    "Recruiter flagged for review (falsified documents)",
    "Candidate notified: Dispute resolved in your favor",
    "Recruiter notified: Dispute resolved against you"
  ],
  
  "message": "Dispute resolved successfully. All systems updated."
}
```

#### Business Logic
- ✅ Updates `StatusDispute.status` to `RESOLVED_CANDIDATE`/`RESOLVED_RECRUITER`/`RESOLVED_COMPROMISE`
- ✅ Updates `StatusUpdateRequest.status` to `CONFIRMED` or `REJECTED` based on ruling
- ✅ **Updates JobApply** with final decided values
- ✅ **Updates EmploymentVerification** with termination details
- ✅ Records complete resolution history
- ✅ Sends notifications to both parties
- ✅ **Flags users** if evidence deemed fake/manipulated
- ✅ Audit trail: Logs admin decision + reasoning

---

### 5. Add Admin Note to Dispute

**Endpoint**: `POST /api/admin/disputes/{disputeId}/notes`  
**Auth**: `ADMIN` role required  
**Description**: Add private notes during dispute review.

#### Request
```http
POST /api/admin/disputes/555/notes
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "note": "Candidate's resignation letter appears authentic. Cross-referenced signature with original job application. Recruiter's termination letter has inconsistent date (before resignation date).",
  "isPrivate": true
}
```

#### Response: `201 Created`
```json
{
  "id": 1234,
  "disputeId": 555,
  "adminName": "Admin Sarah",
  "note": "Candidate's resignation letter appears authentic...",
  "isPrivate": true,
  "createdAt": "2025-11-26T09:30:00"
}
```

---

### 6. Get Dispute History

**Endpoint**: `GET /api/disputes/{disputeId}/history`  
**Auth**: `ADMIN`, `CANDIDATE` (if involved), or `RECRUITER` (if involved) role required  
**Description**: Get complete timeline of dispute events.

#### Response: `200 OK`
```json
{
  "disputeId": 555,
  "jobApplyId": 123,
  
  "timeline": [
    {
      "timestamp": "2025-11-20T10:00:00",
      "actor": "CANDIDATE",
      "action": "SUBMITTED_STATUS_UPDATE",
      "description": "Candidate submitted status update: RESIGNED"
    },
    {
      "timestamp": "2025-11-21T14:00:00",
      "actor": "RECRUITER",
      "action": "DISPUTED_STATUS_UPDATE",
      "description": "Recruiter disputed claim: Counter-claim FIRED_PERFORMANCE"
    },
    {
      "timestamp": "2025-11-21T14:01:00",
      "actor": "SYSTEM",
      "action": "DISPUTE_CREATED",
      "description": "Dispute escalated to admin review (Priority: CRITICAL)"
    },
    {
      "timestamp": "2025-11-26T09:00:00",
      "actor": "ADMIN",
      "action": "ASSIGNED_FOR_REVIEW",
      "description": "Dispute assigned to Admin Sarah"
    },
    {
      "timestamp": "2025-11-26T10:00:00",
      "actor": "ADMIN",
      "action": "DISPUTE_RESOLVED",
      "description": "Resolved in favor of CANDIDATE"
    }
  ],
  
  "evidenceUploaded": [
    {
      "timestamp": "2025-11-20T10:00:00",
      "uploadedBy": "CANDIDATE",
      "fileType": "RESIGNATION_LETTER",
      "fileName": "resignation-letter-signed.pdf"
    },
    {
      "timestamp": "2025-11-21T14:00:00",
      "uploadedBy": "RECRUITER",
      "fileType": "TERMINATION_LETTER",
      "fileName": "termination-letter-november.pdf"
    }
  ],
  
  "finalOutcome": {
    "status": "RESOLVED_CANDIDATE",
    "rulingInFavorOf": "CANDIDATE",
    "finalStatus": "TERMINATED",
    "resolvedAt": "2025-11-26T10:00:00"
  }
}
```

---

### 7. Get Open Disputes Count (Dashboard)

**Endpoint**: `GET /api/admin/disputes/count`  
**Auth**: `ADMIN` role required  
**Description**: Get dispute statistics for admin dashboard.

#### Response: `200 OK`
```json
{
  "totalOpen": 8,
  "byPriority": {
    "CRITICAL": 2,
    "HIGH": 3,
    "NORMAL": 2,
    "LOW": 1
  },
  "inReview": 3,
  "avgResolutionTimeHours": 18.5,
  "oldestOpenDisputeDays": 5
}
```

---

### 8. Request Additional Evidence

**Endpoint**: `POST /api/admin/disputes/{disputeId}/request-evidence`  
**Auth**: `ADMIN` role required  
**Description**: Request more evidence from candidate or recruiter.

#### Request
```http
POST /api/admin/disputes/555/request-evidence
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "requestFrom": "CANDIDATE",
  "requestMessage": "Please provide additional documentation showing the date your resignation was accepted by HR. The current evidence shows submission date but not acceptance date.",
  "requestedEvidenceTypes": ["EMAIL_FROM_HR", "RESIGNATION_LETTER"]
}
```

#### Response: `200 OK`
```json
{
  "message": "Evidence request sent to candidate",
  "sentAt": "2025-11-26T10:30:00",
  "disputeId": 555,
  "requestedFrom": "CANDIDATE"
}
```

---

## Priority Scoring Algorithm

### Calculation Formula

```typescript
function calculateDisputePriorityScore(dispute: Dispute): number {
  let score = 0;
  
  // 1. Time factor (max 40 points)
  const daysOpen = dispute.getDaysOpen();
  if (daysOpen >= 7) score += 40;
  else if (daysOpen >= 5) score += 30;
  else if (daysOpen >= 3) score += 20;
  else if (daysOpen >= 1) score += 10;
  
  // 2. Evidence quality difference (max 30 points)
  const evidenceDifference = Math.abs(
    dispute.candidateEvidenceTrustScore - dispute.recruiterEvidenceTrustScore
  );
  if (evidenceDifference <= 2) score += 30;  // Close call, needs careful review
  else if (evidenceDifference <= 4) score += 20;
  else score += 10;                          // Clear winner
  
  // 3. Claim severity (max 20 points)
  const severityScore = calculateClaimSeverity(
    dispute.candidateClaimedTerminationType,
    dispute.recruiterClaimedTerminationType
  );
  score += severityScore;
  
  // 4. Company reputation impact (max 10 points)
  if (dispute.isHighProfileCompany()) score += 10;
  else if (dispute.isVerifiedCompany()) score += 5;
  
  return Math.min(score, 100);  // Cap at 100
}

function calculateClaimSeverity(
  candidateClaim: TerminationType,
  recruiterClaim: TerminationType
): number {
  // Higher score = more severe dispute
  if (candidateClaim === "RESIGNATION" && recruiterClaim === "FIRED_MISCONDUCT") return 20;
  if (candidateClaim === "RESIGNATION" && recruiterClaim === "FIRED_PERFORMANCE") return 15;
  if (candidateClaim === "LAID_OFF" && recruiterClaim === "FIRED_MISCONDUCT") return 18;
  if (candidateClaim === "MUTUAL_AGREEMENT" && recruiterClaim === "FIRED_PERFORMANCE") return 12;
  return 10;  // Default severity
}
```

### Priority Levels

| Priority | Score Range | SLA (Resolution Time) |
|----------|-------------|----------------------|
| **CRITICAL** | 81-100 | Within 24 hours |
| **HIGH** | 61-80 | Within 3 days |
| **NORMAL** | 31-60 | Within 7 days |
| **LOW** | 0-30 | Within 14 days |

---

## Evidence Trust Scoring

### Trust Score Matrix

| Evidence Type | Base Score | Modifiers |
|--------------|-----------|-----------|
| **RESIGNATION_LETTER** | 10 | -3 if unsigned, -2 if no company letterhead |
| **TERMINATION_LETTER** | 10 | -3 if unsigned, -2 if no HR contact info |
| **FINAL_PAYSLIP** | 9 | +1 if from recognized payroll provider |
| **EMAIL_FROM_HR** | 8 | -2 if from personal email, +1 if from company domain |
| **PERFORMANCE_REVIEW** | 7 | -2 if not on company template |
| **EMAIL_SCREENSHOT** | 5 | -2 if low quality, -1 if metadata stripped |
| **PERSONAL_STATEMENT** | 3 | Cannot be sole evidence |

### Evidence Authenticity Flags

Admin can mark evidence as:
- ✅ **Authentic**: Verified as genuine
- ⚠️ **Suspicious**: Needs further review
- ❌ **Fake**: Determined to be falsified

---

## Business Rules

### Dispute Creation Rules

| Rule | Validation |
|------|------------|
| **Must have status update** | Dispute links to StatusUpdateRequest |
| **Both parties must have claims** | Candidate + recruiter claims required |
| **Evidence required** | At least 1 piece of evidence per party |
| **No duplicate disputes** | One active dispute per status update |

### Resolution Rules

| Rule | Validation |
|------|------------|
| **Admin only** | Only ADMIN role can resolve |
| **Reasoning required** | Min 50 characters |
| **Must update systems** | JobApply + EmploymentVerification sync |
| **Notify both parties** | Email + in-app notification |
| **Audit trail** | All actions logged |

### Evidence Request Rules

| Rule | Validation |
|------|------------|
| **Max 3 requests** | Cannot request evidence more than 3 times |
| **7-day deadline** | Requester has 7 days to provide |
| **Auto-resolve** | If no evidence provided, resolve with existing data |

---

## Error Handling

### Common Error Responses

#### 400 Bad Request - Dispute Already Resolved
```json
{
  "error": "DISPUTE_ALREADY_RESOLVED",
  "message": "This dispute has already been resolved",
  "status": "RESOLVED_CANDIDATE",
  "resolvedAt": "2025-11-26T10:00:00"
}
```

#### 403 Forbidden - Not Authorized
```json
{
  "error": "ADMIN_ACCESS_REQUIRED",
  "message": "Only admin users can resolve disputes",
  "requiredRole": "ADMIN"
}
```

#### 404 Not Found - Dispute Not Found
```json
{
  "error": "DISPUTE_NOT_FOUND",
  "message": "Dispute with ID 555 does not exist",
  "disputeId": 555
}
```

#### 400 Bad Request - Insufficient Reasoning
```json
{
  "error": "INSUFFICIENT_REASONING",
  "message": "Resolution reasoning must be at least 50 characters",
  "provided": 32,
  "required": 50
}
```

---

## Frontend Integration Checklist

### Admin Dispute Dashboard
- [ ] List of all disputes sorted by priority
- [ ] Color-coded priority badges (red = CRITICAL, orange = HIGH)
- [ ] Filter by status (OPEN, IN_REVIEW, RESOLVED)
- [ ] Search by candidate/recruiter name, job title
- [ ] "Days Open" indicator
- [ ] Evidence count badges

### Dispute Detail Page
- [ ] Side-by-side comparison view (Candidate vs. Recruiter)
- [ ] Claim details (status, type, date, reason)
- [ ] Evidence viewer (PDF/image preview)
- [ ] Evidence trust scores
- [ ] Download all evidence button
- [ ] Timeline of dispute events

### Dispute Resolution Form
- [ ] Radio buttons: Candidate / Recruiter / Compromise
- [ ] Final status dropdown
- [ ] Final termination type dropdown (if applicable)
- [ ] Final date picker
- [ ] Reasoning textarea (min 50 chars, character counter)
- [ ] Evidence authenticity checkboxes
- [ ] Optional: Flag user checkbox + reason
- [ ] "Resolve Dispute" button
- [ ] Confirmation modal: "This action is final and cannot be undone"

### Admin Notes Panel
- [ ] List of all admin notes (private + public)
- [ ] Add note textarea
- [ ] "Private note" checkbox
- [ ] Timestamp and admin name display
- [ ] Notes are only visible to admins

### Evidence Request Modal
- [ ] Select recipient (Candidate / Recruiter)
- [ ] Request message textarea
- [ ] Checkboxes for requested evidence types
- [ ] "Send Request" button
- [ ] Show previous requests (max 3)

---

**End of Dispute Resolution API Documentation**
