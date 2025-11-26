# Employment Contract API Documentation

**Module**: Employment Contract Management  
**Base URL**: `/api`  
**Version**: 1.0  
**Last Updated**: November 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [Data Models](#data-models)
3. [API Endpoints](#api-endpoints)
4. [Contract Lifecycle](#contract-lifecycle)
5. [Business Rules](#business-rules)
6. [Error Handling](#error-handling)

---

## Overview

The Employment Contract API manages the complete lifecycle of employment contracts from draft creation through signing, activation, and eventual termination. It provides digital signature capabilities, contract versioning, and automated status tracking.

### Key Features
- ✅ Digital contract creation with all employment terms
- ✅ Multi-party signature workflow (candidate + company)
- ✅ Contract status tracking (DRAFT → ACTIVE → TERMINATED)
- ✅ Probation period management
- ✅ Salary and benefits tracking (privacy-focused)
- ✅ Contract document storage (Firebase URLs)
- ✅ Automated expiration handling
- ✅ Termination reason tracking

---

## Data Models

### EmploymentContractRequest
```typescript
interface EmploymentContractRequest {
  // Contract identification
  contractType: ContractType;          // "FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP", "TEMPORARY", "FREELANCE"
  
  // Job details
  jobTitle: string;
  department?: string;
  reportsTo?: string;                  // Manager name
  
  // Duration
  startDate: string;                   // ISO 8601 date (e.g., "2026-01-15")
  endDate?: string;                    // Required for CONTRACT/TEMPORARY
  probationMonths?: number;            // Default: 3 months
  
  // Compensation (stored encrypted)
  salaryAmount: number;                // Annual or hourly based on paymentFrequency
  salaryCurrency: string;              // "USD", "VND", etc.
  paymentFrequency: PaymentFrequency;  // "MONTHLY", "BI_WEEKLY", "WEEKLY", "HOURLY"
  bonusDetails?: string;               // Bonus structure description
  
  // Benefits
  benefits?: string;                   // Comma-separated or JSON string
  annualLeaveDays: number;            // Default: 12-20 days
  
  // Work arrangement
  hoursPerWeek: number;               // Default: 40
  workLocation: string;               // Office address or "Remote"
  remoteWorkPolicy?: string;          // Hybrid/remote details
  
  // Notice period
  noticePeriodDays: number;           // Default: 30 days
  
  // Additional
  additionalTerms?: string;           // Custom clauses
  notes?: string;                     // Internal notes
}

enum ContractType {
  FULL_TIME = "FULL_TIME",
  PART_TIME = "PART_TIME",
  CONTRACT = "CONTRACT",
  INTERNSHIP = "INTERNSHIP",
  TEMPORARY = "TEMPORARY",
  FREELANCE = "FREELANCE"
}

enum PaymentFrequency {
  MONTHLY = "MONTHLY",
  BI_WEEKLY = "BI_WEEKLY",
  WEEKLY = "WEEKLY",
  HOURLY = "HOURLY"
}
```

### EmploymentContractResponse
```typescript
interface EmploymentContractResponse {
  contractId: number;
  jobApplyId: number;
  contractNumber: string;              // Auto-generated: "CONTRACT-2025-001"
  
  contractType: ContractType;
  status: ContractStatus;              // "DRAFT", "PENDING_SIGNATURE", "ACTIVE", "COMPLETED", "TERMINATED", "DECLINED", "EXPIRED"
  
  // Job details
  jobTitle: string;
  department?: string;
  companyName: string;
  candidateName: string;
  
  // Duration
  startDate: string;
  endDate?: string;
  probationMonths?: number;
  probationEndDate?: string;           // Auto-calculated
  
  // Compensation (visible only to authorized users)
  salaryAmount?: number;               // Redacted for non-owner
  salaryCurrency?: string;
  paymentFrequency?: PaymentFrequency;
  bonusDetails?: string;
  benefits?: string;
  
  // Work details
  hoursPerWeek: number;
  annualLeaveDays: number;
  noticePeriodDays: number;
  workLocation: string;
  remoteWorkPolicy?: string;
  reportsTo?: string;
  
  // Documents
  documentUrl?: string;                // Signed contract PDF
  
  // Signature tracking
  sentAt?: string;                     // When sent to candidate
  signedAt?: string;                   // When candidate signed
  candidateSignature?: string;         // Digital signature data
  companySignatory?: string;           // Who signed for company
  companySignedAt?: string;            // When company signed
  
  // Termination
  terminationReason?: string;
  endedAt?: string;
  
  // Additional
  additionalTerms?: string;
  notes?: string;
  
  // Metadata
  createdAt: string;
  updatedAt: string;
  
  // Frontend helpers
  isFullySigned: boolean;              // Both parties signed
  daysUntilStart?: number;
  daysActive?: number;
  canSign: boolean;                    // Based on user role and status
  canTerminate: boolean;
}

enum ContractStatus {
  DRAFT = "DRAFT",                     // Being created
  PENDING_SIGNATURE = "PENDING_SIGNATURE", // Sent to candidate
  ACTIVE = "ACTIVE",                   // Fully signed and active
  COMPLETED = "COMPLETED",             // Contract ended naturally
  TERMINATED = "TERMINATED",           // Ended early
  DECLINED = "DECLINED",               // Candidate declined
  EXPIRED = "EXPIRED"                  // Offer expired unsigned
}
```

---

## API Endpoints

### 1. Create Employment Contract

**Endpoint**: `POST /api/job-applies/{jobApplyId}/employment-contract`  
**Auth**: `RECRUITER` role required  
**Description**: Create a new employment contract for a job application.

#### Request
```http
POST /api/job-applies/123/employment-contract
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "contractType": "FULL_TIME",
  "jobTitle": "Senior Software Engineer",
  "department": "Engineering",
  "startDate": "2026-01-15",
  "probationMonths": 3,
  "salaryAmount": 120000.00,
  "salaryCurrency": "USD",
  "paymentFrequency": "MONTHLY",
  "bonusDetails": "10% annual performance bonus + stock options",
  "benefits": "Health insurance, dental, 401k matching, gym membership",
  "hoursPerWeek": 40,
  "annualLeaveDays": 20,
  "noticePeriodDays": 30,
  "workLocation": "Office - San Francisco, CA",
  "remoteWorkPolicy": "Hybrid - 3 days office, 2 days remote",
  "reportsTo": "John Smith - VP Engineering",
  "additionalTerms": "Non-compete clause: 1 year. Confidentiality agreement included.",
  "notes": "Candidate negotiated higher base salary"
}
```

#### Response: `201 Created`
```json
{
  "contractId": 456,
  "jobApplyId": 123,
  "contractNumber": "CONTRACT-2025-001",
  "contractType": "FULL_TIME",
  "status": "DRAFT",
  "jobTitle": "Senior Software Engineer",
  "companyName": "Tech Corp",
  "candidateName": "Jane Smith",
  "startDate": "2026-01-15",
  "probationEndDate": "2026-04-15",
  "salaryAmount": 120000.00,
  "isFullySigned": false,
  "canSign": false,
  "createdAt": "2025-11-26T10:00:00"
}
```

#### Business Logic
- ✅ Auto-generates unique `contractNumber`
- ✅ Calculates `probationEndDate` = startDate + probationMonths
- ❌ Cannot create if contract already exists for this job apply
- ✅ JobApply must be in `APPROVED` status
- ⚠️ Salary data stored encrypted

---

### 2. Send Contract for Signature

**Endpoint**: `POST /api/employment-contracts/{contractId}/send-for-signature`  
**Auth**: `RECRUITER` role required  
**Description**: Send contract to candidate for review and signature.

#### Request
```http
POST /api/employment-contracts/456/send-for-signature
Authorization: Bearer {recruiter_token}
```

#### Response: `200 OK`
```json
{
  "contractId": 456,
  "status": "PENDING_SIGNATURE",
  "sentAt": "2025-11-26T10:30:00",
  "message": "Contract sent to candidate for signature",
  "expiresAt": "2025-12-10T23:59:59"
}
```

#### Business Logic
- ✅ Updates status: `DRAFT` → `PENDING_SIGNATURE`
- ✅ Records `sentAt` timestamp
- ✅ Sends email notification to candidate with contract link
- ✅ Sets expiration: 14 days from sent date
- ❌ Cannot send if status is not `DRAFT`

---

### 3. Candidate Signs Contract

**Endpoint**: `POST /api/employment-contracts/{contractId}/sign`  
**Auth**: `CANDIDATE` role required  
**Description**: Candidate reviews and signs the employment contract.

#### Request
```http
POST /api/employment-contracts/456/sign
Authorization: Bearer {candidate_token}
Content-Type: application/json

{
  "candidateSignature": "data:image/png;base64,iVBORw0KGgoAAAANSUh...",
  "agreedToTerms": true
}
```

#### Response: `200 OK`
```json
{
  "contractId": 456,
  "status": "ACTIVE",
  "signedAt": "2025-11-27T09:00:00",
  "isFullySigned": true,
  "message": "Contract signed successfully. Employment starts on 2026-01-15."
}
```

#### Business Logic
- ✅ Updates status: `PENDING_SIGNATURE` → `ACTIVE`
- ✅ Records `signedAt` timestamp and signature data
- ✅ Updates `JobApply.status` to `ACCEPTED`
- ✅ Updates `JobApply.hiredAt` timestamp
- ✅ Creates `EmploymentVerification` record (for review eligibility)
- ✅ Sends confirmation email to both parties
- ❌ Cannot sign if status is not `PENDING_SIGNATURE`
- ❌ Cannot sign if past expiration date

---

### 4. Candidate Declines Contract

**Endpoint**: `POST /api/employment-contracts/{contractId}/decline`  
**Auth**: `CANDIDATE` role required  
**Description**: Candidate declines the employment offer.

#### Request
```http
POST /api/employment-contracts/456/decline
Authorization: Bearer {candidate_token}
Content-Type: application/json

{
  "reason": "Accepted another offer with better compensation package"
}
```

#### Response: `200 OK`
```json
{
  "contractId": 456,
  "status": "DECLINED",
  "declinedAt": "2025-11-27T09:00:00",
  "message": "Contract declined. Thank you for your time."
}
```

#### Business Logic
- ✅ Updates status: `PENDING_SIGNATURE` → `DECLINED`
- ✅ Updates `JobApply.status` to `WITHDRAWN`
- ✅ Notifies recruiter of decline
- ❌ Cannot decline if already signed

---

### 5. Record Company Signature

**Endpoint**: `POST /api/employment-contracts/{contractId}/company-signature`  
**Auth**: `RECRUITER` role required  
**Description**: Record company representative's signature on contract.

#### Request
```http
POST /api/employment-contracts/456/company-signature
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "signatory": "Michael Johnson - CEO",
  "signatureData": "data:image/png;base64,iVBORw0KGgoAAAANSUh..."
}
```

#### Response: `200 OK`
```json
{
  "contractId": 456,
  "companySignatory": "Michael Johnson - CEO",
  "companySignedAt": "2025-11-27T10:00:00",
  "isFullySigned": true
}
```

---

### 6. Terminate Contract

**Endpoint**: `POST /api/employment-contracts/{contractId}/terminate`  
**Auth**: `RECRUITER` role required  
**Description**: Terminate an active employment contract early.

#### Request
```http
POST /api/employment-contracts/456/terminate
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "terminationReason": "Employee resigned with 30 days notice. Last working day: 2026-03-15.",
  "terminationDate": "2026-03-15",
  "exitInterviewCompleted": true
}
```

#### Response: `200 OK`
```json
{
  "contractId": 456,
  "status": "TERMINATED",
  "endedAt": "2026-03-15",
  "terminationReason": "Employee resigned with 30 days notice.",
  "daysActive": 60,
  "eligibleForWorkReview": true
}
```

#### Business Logic
- ✅ Updates status: `ACTIVE` → `TERMINATED`
- ✅ Updates `JobApply.status` to `TERMINATED`
- ✅ Updates `JobApply.leftAt` timestamp
- ✅ Updates `EmploymentVerification` with termination details
- ✅ **If < 30 days employed**: Not eligible for work review
- ✅ **If >= 30 days employed**: Eligible for work review
- ❌ Cannot terminate if status is not `ACTIVE`

---

### 7. Update Contract (Draft Only)

**Endpoint**: `PUT /api/employment-contracts/{contractId}`  
**Auth**: `RECRUITER` role required  
**Description**: Update contract details (only allowed for DRAFT contracts).

#### Request
```http
PUT /api/employment-contracts/456
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "salaryAmount": 125000.00,
  "bonusDetails": "15% annual performance bonus + stock options",
  "notes": "Candidate counter-offered. Approved by management."
}
```

#### Response: `200 OK`
```json
{
  "contractId": 456,
  "status": "DRAFT",
  "salaryAmount": 125000.00,
  "updatedAt": "2025-11-26T14:30:00"
}
```

#### Business Logic
- ✅ Can only update if status is `DRAFT`
- ❌ Cannot modify salary/terms after sent for signature
- ⚠️ All changes logged for audit trail

---

### 8. Upload Contract Document

**Endpoint**: `POST /api/employment-contracts/{contractId}/upload-document`  
**Auth**: `RECRUITER` role required  
**Description**: Upload the signed contract document (PDF).

#### Request
```http
POST /api/employment-contracts/456/upload-document
Authorization: Bearer {recruiter_token}
Content-Type: application/json

{
  "documentUrl": "https://firebase.storage/contracts/CONTRACT-2025-001-signed.pdf"
}
```

#### Response: `200 OK`
```json
{
  "contractId": 456,
  "documentUrl": "https://firebase.storage/contracts/CONTRACT-2025-001-signed.pdf",
  "uploadedAt": "2025-11-27T11:00:00"
}
```

---

### 9. Get Contract Details

**Endpoint**: `GET /api/employment-contracts/{contractId}`  
**Auth**: `RECRUITER` or `CANDIDATE` role required  
**Description**: Get full details of an employment contract.

#### Response: `200 OK`
```json
{
  "contractId": 456,
  "jobApplyId": 123,
  "contractNumber": "CONTRACT-2025-001",
  "contractType": "FULL_TIME",
  "status": "ACTIVE",
  "jobTitle": "Senior Software Engineer",
  "companyName": "Tech Corp",
  "candidateName": "Jane Smith",
  "startDate": "2026-01-15",
  "probationEndDate": "2026-04-15",
  "salaryAmount": 120000.00,
  "isFullySigned": true,
  "daysUntilStart": 50,
  "createdAt": "2025-11-26T10:00:00"
}
```

#### Privacy Rules
- ✅ **Candidate**: Can only see their own contracts
- ✅ **Recruiter**: Can see contracts for their company
- ❌ **Salary redacted** if requester is not contract owner

---

### 10. Get Candidate's Contracts

**Endpoint**: `GET /api/employment-contracts/candidate/{candidateId}`  
**Auth**: `CANDIDATE` role required  
**Description**: Get all employment contracts for a candidate.

#### Response: `200 OK`
```json
{
  "count": 3,
  "contracts": [
    {
      "contractId": 456,
      "companyName": "Tech Corp",
      "jobTitle": "Senior Software Engineer",
      "contractType": "FULL_TIME",
      "status": "ACTIVE",
      "startDate": "2026-01-15",
      "isFullySigned": true
    },
    {
      "contractId": 423,
      "companyName": "StartupXYZ",
      "jobTitle": "Tech Lead",
      "contractType": "CONTRACT",
      "status": "DECLINED",
      "startDate": "2025-12-01"
    }
  ]
}
```

---

### 11. Get Company's Contracts

**Endpoint**: `GET /api/employment-contracts/company/{companyId}`  
**Auth**: `RECRUITER` role required  
**Description**: Get all employment contracts for a company.

#### Query Parameters
- `status` (optional): Filter by status
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 10)

#### Response: `200 OK`
```json
{
  "count": 25,
  "contracts": [
    {
      "contractId": 456,
      "candidateName": "Jane Smith",
      "jobTitle": "Senior Software Engineer",
      "status": "ACTIVE",
      "startDate": "2026-01-15",
      "daysActive": 60
    }
  ]
}
```

---

### 12. Get Active Contracts by Company

**Endpoint**: `GET /api/employment-contracts/company/{companyId}/active`  
**Auth**: `RECRUITER` role required  
**Description**: Get all currently active employment contracts.

#### Response: `200 OK`
```json
{
  "count": 15,
  "contracts": [
    {
      "contractId": 456,
      "candidateName": "Jane Smith",
      "jobTitle": "Senior Software Engineer",
      "startDate": "2026-01-15",
      "daysActive": 60,
      "isProbation": false,
      "probationEndDate": "2026-04-15"
    }
  ]
}
```

---

### 13. Get Contracts by Status (Admin)

**Endpoint**: `GET /api/employment-contracts/status/{status}`  
**Auth**: `ADMIN` role required  
**Description**: Get all contracts with a specific status (admin analytics).

#### Response: `200 OK`
```json
{
  "status": "PENDING_SIGNATURE",
  "count": 8,
  "contracts": [...]
}
```

---

## Contract Lifecycle

### Status Transition Diagram

```
DRAFT
  ├── (send for signature) → PENDING_SIGNATURE
  │                              ├── (candidate signs) → ACTIVE
  │                              ├── (candidate declines) → DECLINED
  │                              └── (14 days no action) → EXPIRED
  │
  └── (delete draft) → [Deleted]

ACTIVE
  ├── (terminate early) → TERMINATED
  └── (end date reached) → COMPLETED
```

### Timeline Example

```
Day 0:  DRAFT created by recruiter
Day 1:  Sent for signature → PENDING_SIGNATURE
Day 3:  Candidate signs → ACTIVE
Day 15: Start date reached → Employment begins
Day 105: Probation ends (90 days)
Day 250: Employee resigns → TERMINATED
```

---

## Business Rules

### Contract Creation Rules

| Rule | Validation |
|------|------------|
| **One contract per job apply** | Cannot create duplicate |
| **JobApply must be APPROVED** | Candidate accepted offer |
| **Start date must be future** | Cannot backdate contracts |
| **End date required for CONTRACT/TEMPORARY** | Fixed-term contracts |
| **Probation max 6 months** | Legal limit in most countries |
| **Salary must be > 0** | Positive compensation |

### Signature Rules

| Rule | Validation |
|------|------------|
| **14-day expiration** | Auto-expire if not signed |
| **Cannot modify after sending** | Terms locked |
| **Both signatures required** | Candidate + Company rep |
| **Digital signature stored** | Base64 image data |
| **Email confirmation sent** | Both parties notified |

### Termination Rules

| Rule | Validation |
|------|------------|
| **Only ACTIVE contracts terminable** | Cannot terminate draft/pending |
| **Must provide reason** | Audit trail |
| **< 30 days employed** | Not eligible for work review |
| **>= 30 days employed** | Eligible for work review |
| **Updates EmploymentVerification** | Sync termination data |

### Privacy Rules

| Data | Visibility |
|------|-----------|
| **Salary/Benefits** | Owner + Admin only |
| **Contract terms** | Owner + Company + Admin |
| **Signature data** | Internal only (not exposed) |
| **Termination reason** | Owner + Company + Admin |
| **Performance notes** | Admin only |

---

## Error Handling

### Common Error Responses

#### 400 Bad Request - Contract Already Exists
```json
{
  "error": "CONTRACT_ALREADY_EXISTS",
  "message": "An employment contract already exists for this job application",
  "existingContractId": 456
}
```

#### 400 Bad Request - Cannot Modify Sent Contract
```json
{
  "error": "CONTRACT_LOCKED",
  "message": "Cannot modify contract after it has been sent for signature",
  "status": "PENDING_SIGNATURE"
}
```

#### 400 Bad Request - Contract Expired
```json
{
  "error": "CONTRACT_EXPIRED",
  "message": "This contract offer has expired. Please contact the recruiter.",
  "expiredAt": "2025-12-10T23:59:59"
}
```

#### 403 Forbidden - Cannot Terminate Non-Active
```json
{
  "error": "INVALID_CONTRACT_STATUS",
  "message": "Cannot terminate contract with status: PENDING_SIGNATURE",
  "currentStatus": "PENDING_SIGNATURE",
  "requiredStatus": "ACTIVE"
}
```

#### 404 Not Found - Contract Not Found
```json
{
  "error": "CONTRACT_NOT_FOUND",
  "message": "Employment contract with ID 456 does not exist"
}
```

---

## Frontend Integration Checklist

### Contract Creation Page (Recruiter)
- [ ] Form with all contract fields (job title, salary, benefits, etc.)
- [ ] Contract type selector (FULL_TIME, PART_TIME, CONTRACT, etc.)
- [ ] Date pickers (start date, end date for fixed-term)
- [ ] Probation period selector (0-6 months)
- [ ] Salary input with currency selector
- [ ] Payment frequency dropdown
- [ ] Benefits textarea (multi-line)
- [ ] Work location input (office/remote/hybrid)
- [ ] Additional terms textarea
- [ ] "Save as Draft" and "Send for Signature" buttons

### Contract Review Page (Candidate)
- [ ] Display all contract terms in readable format
- [ ] Salary/benefits prominently displayed
- [ ] Start date and probation period highlighted
- [ ] Digital signature pad (canvas or library)
- [ ] "I agree to the terms" checkbox
- [ ] "Sign Contract" and "Decline Offer" buttons
- [ ] Download PDF button
- [ ] Expiration countdown timer

### Contract Management Dashboard (Recruiter)
- [ ] List of all contracts (tabs: All, Active, Pending, Terminated)
- [ ] Search/filter by candidate name, job title, status
- [ ] Status badges (color-coded)
- [ ] Action buttons: View, Terminate, Upload Document
- [ ] Expiring soon alerts (contracts unsigned for > 10 days)
- [ ] Active employee count

### Contract History Page (Candidate)
- [ ] List of all contracts (current and past)
- [ ] Status indicator
- [ ] Download signed contract button
- [ ] View details button

---

**End of Employment Contract API Documentation**
