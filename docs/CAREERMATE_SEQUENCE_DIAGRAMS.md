# CareerMate Sequence Diagrams

This document contains Mermaid sequence diagrams for the core workflows in the CareerMate job application platform.

---

## 1. Job Application Submission Flow

```mermaid
sequenceDiagram
    actor C as Candidate
    participant JA as JobApplyController
    participant JAS as JobApplyService
    participant RS as ResumeService
    participant RE as RecruiterEntitlementService
    participant NP as NotificationProducer
    participant DB as Database
    
    C->>JA: POST /api/job-apply/apply
    activate JA
    
    JA->>JAS: applyForJob(jobPostingId, resumeId)
    activate JAS
    
    JAS->>DB: Check existing application
    DB-->>JAS: No duplicate found
    
    JAS->>RS: validateResumeCompleteness(resumeId)
    activate RS
    RS->>DB: Get resume details
    DB-->>RS: Resume data
    RS-->>JAS: Resume valid (completeness >= 70%)
    deactivate RS
    
    JAS->>RE: checkRecruiterPostingLimit()
    activate RE
    RE-->>JAS: Posting active & within limits
    deactivate RE
    
    JAS->>DB: Create JobApply entity
    Note over DB: status = SUBMITTED<br/>appliedAt = now()
    DB-->>JAS: JobApply created
    
    JAS->>NP: publishEvent("JOB_APPLICATION_SUBMITTED")
    activate NP
    NP->>NP: Send to Kafka topic
    NP-->>JAS: Event published
    deactivate NP
    
    JAS-->>JA: ApplicationResponse
    deactivate JAS
    
    JA-->>C: 201 Created
    deactivate JA
    
    Note over NP: Async notification processing
    NP->>C: Email: "Application submitted successfully"
    NP->>C: In-app notification
```

---

## 2. Interview Scheduling & Management Flow

```mermaid
sequenceDiagram
    actor R as Recruiter
    actor C as Candidate
    participant ISC as InterviewScheduleController
    participant ISS as InterviewScheduleService
    participant ICS as InterviewCalendarService
    participant JAS as JobApplyService
    participant NP as NotificationProducer
    participant DB as Database
    participant Scheduler as InterviewReminderScheduler
    
    R->>ISC: POST /api/interview/schedule
    activate ISC
    
    ISC->>ISS: scheduleInterview(jobApplyId, dateTime, meetingLink)
    activate ISS
    
    ISS->>DB: Get JobApply
    DB-->>ISS: JobApply (status=REVIEWING)
    
    ISS->>ICS: checkAvailability(recruiterId, dateTime)
    activate ICS
    ICS->>DB: Query existing interviews
    DB-->>ICS: No conflicts
    ICS-->>ISS: Time slot available
    deactivate ICS
    
    ISS->>DB: Create InterviewSchedule
    Note over DB: status = SCHEDULED<br/>scheduledDate = dateTime<br/>meetingLink = link
    DB-->>ISS: Interview created
    
    ISS->>JAS: updateStatus(jobApplyId, INTERVIEW_SCHEDULED)
    activate JAS
    JAS->>DB: Update JobApply.status
    JAS->>DB: Record status history
    DB-->>JAS: Status updated
    JAS-->>ISS: Success
    deactivate JAS
    
    ISS->>NP: publishEvent("INTERVIEW_SCHEDULED")
    activate NP
    NP-->>ISS: Event published
    deactivate NP
    
    ISS-->>ISC: InterviewResponse
    deactivate ISS
    ISC-->>R: 201 Created
    deactivate ISC
    
    NP->>C: Email + SMS: "Interview scheduled"
    NP->>C: Calendar invite with meeting link
    
    Note over Scheduler: 24 hours before interview
    Scheduler->>NP: Send 24h reminder
    NP->>C: Email + SMS: "Interview tomorrow"
    
    Note over Scheduler: 2 hours before interview
    Scheduler->>NP: Send 2h reminder
    NP->>C: Email + SMS: "Interview in 2 hours"
    
    Note over C,R: Interview conducted
    
    R->>ISC: PATCH /api/interview/{id}/complete
    activate ISC
    ISC->>ISS: completeInterview(interviewId, outcome)
    activate ISS
    
    ISS->>DB: Update interview status
    Note over DB: status = COMPLETED<br/>outcome = PASS/FAIL/NEEDS_SECOND_ROUND
    
    alt outcome = PASS
        ISS->>JAS: updateStatus(jobApplyId, APPROVED)
    else outcome = FAIL
        ISS->>JAS: updateStatus(jobApplyId, REJECTED)
    else outcome = NEEDS_SECOND_ROUND
        ISS->>JAS: updateStatus(jobApplyId, REVIEWING)
        ISS->>NP: publishEvent("SCHEDULE_SECOND_INTERVIEW")
    end
    
    ISS-->>ISC: Success
    deactivate ISS
    ISC-->>R: 200 OK
    deactivate ISC
    
    NP->>C: Email: Interview result notification
```

---

## 3. Bilateral Status Verification Flow (Dispute Resolution)

```mermaid
sequenceDiagram
    actor C as Candidate
    actor R as Recruiter
    actor A as Admin
    participant SUC as StatusUpdateController
    participant SUS as StatusUpdateService
    participant EFS as EvidenceFileService
    participant SDS as StatusDisputeService
    participant EVS as EmploymentVerificationService
    participant NP as NotificationProducer
    participant DB as Database
    participant Scheduler as StatusVerificationScheduler
    
    C->>SUC: POST /api/status-update/request
    Note over C: Claims TERMINATED status<br/>with resignation letter
    activate SUC
    
    SUC->>SUS: requestStatusUpdate(requestDto)
    activate SUS
    
    SUS->>DB: Create StatusUpdateRequest
    Note over DB: status = PENDING_VERIFICATION<br/>requestedBy = CANDIDATE<br/>currentStatus = WORKING<br/>requestedStatus = TERMINATED<br/>verificationDeadline = now() + 7 days
    DB-->>SUS: Request created
    
    SUS->>EFS: uploadEvidenceFiles(requestId, files)
    activate EFS
    
    loop For each evidence file
        EFS->>DB: Save evidence file metadata
        Note over DB: Calculate trust score (3-10)<br/>RESIGNATION_LETTER: 8<br/>EMAIL_SCREENSHOT: 5<br/>PERSONAL_STATEMENT: 3
    end
    
    EFS-->>SUS: Evidence uploaded
    deactivate EFS
    
    SUS->>NP: publishEvent("STATUS_UPDATE_VERIFICATION_REQUIRED")
    NP->>R: Email: "Status verification needed (7-day deadline)"
    
    SUS-->>SUC: RequestCreatedResponse
    deactivate SUS
    SUC-->>C: 201 Created
    deactivate SUC
    
    alt Scenario A: Recruiter Confirms
        R->>SUC: POST /api/status-update/{id}/confirm
        activate SUC
        SUC->>SUS: confirmStatusUpdate(requestId, confirmedBy=RECRUITER)
        activate SUS
        
        SUS->>DB: Update request status
        Note over DB: status = CONFIRMED<br/>confirmedBy = RECRUITER<br/>confirmedAt = now()
        
        SUS->>EVS: updateEmploymentStatus(jobApplyId, TERMINATED)
        activate EVS
        EVS->>DB: Update EmploymentVerification
        Note over DB: terminationType = RESIGNATION<br/>terminationDate = now()<br/>isActive = false
        EVS-->>SUS: Updated
        deactivate EVS
        
        SUS->>DB: Record status history
        
        SUS->>NP: publishEvent("STATUS_UPDATE_CONFIRMED")
        NP->>C: Email: "Status update approved"
        NP->>R: Email: "Status update confirmed"
        
        SUS-->>SUC: Success
        deactivate SUS
        SUC-->>R: 200 OK
        deactivate SUC
        
    else Scenario B: Recruiter Disputes
        R->>SUC: POST /api/status-update/{id}/dispute
        Note over R: Claims candidate still working<br/>provides counter-evidence
        activate SUC
        SUC->>SUS: disputeStatusUpdate(requestId, disputeReason)
        activate SUS
        
        SUS->>DB: Update request status
        Note over DB: status = DISPUTED
        
        SUS->>SDS: createDispute(requestId)
        activate SDS
        SDS->>DB: Create StatusDispute
        Note over DB: status = OPEN<br/>escalatedToAdmin = true<br/>candidateEvidenceCount = 1<br/>recruiterEvidenceCount = 1<br/>priorityScore = calculatePriority()
        SDS-->>SUS: Dispute created
        deactivate SDS
        
        SUS->>NP: publishEvent("STATUS_DISPUTE_CREATED")
        NP->>A: Email: "New dispute requires admin review"
        NP->>C: Email: "Dispute escalated to admin"
        
        SUS-->>SUC: DisputeCreatedResponse
        deactivate SUS
        SUC-->>R: 201 Created
        deactivate SUC
        
        Note over A: Admin reviews dispute
        A->>SUC: POST /api/admin/dispute/{id}/resolve
        activate SUC
        SUC->>SDS: resolveDispute(disputeId, decision, notes)
        activate SDS
        
        SDS->>DB: Update dispute
        Note over DB: status = RESOLVED_CANDIDATE_FAVOR<br/>resolvedByAdminId = adminId<br/>finalStatusDecision = TERMINATED
        
        SDS->>SUS: applyResolution(requestId, TERMINATED)
        activate SUS
        SUS->>EVS: updateEmploymentStatus(jobApplyId, TERMINATED)
        SUS->>DB: Record status history
        SUS-->>SDS: Applied
        deactivate SUS
        
        SDS->>NP: publishEvent("DISPUTE_RESOLVED")
        NP->>C: Email: "Dispute resolved in your favor"
        NP->>R: Email: "Dispute resolved"
        
        SDS-->>SUC: Success
        deactivate SDS
        SUC-->>A: 200 OK
        deactivate SUC
        
    else Scenario C: No Response (Auto-Approve)
        Note over Scheduler: After 7 days, scheduled job runs
        Scheduler->>SUS: processExpiredVerifications()
        activate SUS
        
        SUS->>DB: Find expired requests (deadline < now())
        DB-->>SUS: Expired requests
        
        loop For each expired request
            SUS->>DB: Update request status
            Note over DB: status = AUTO_APPROVED<br/>confirmedBy = SYSTEM
            
            SUS->>EVS: updateEmploymentStatus(jobApplyId, requestedStatus)
            
            SUS->>DB: Record status history
            Note over DB: Marked as auto-approved
            
            SUS->>NP: publishEvent("STATUS_AUTO_APPROVED")
            NP->>C: Email: "Status update auto-approved"
            NP->>R: Email: "Missed verification deadline"
        end
        
        SUS-->>Scheduler: Processed
        deactivate SUS
    end
```

---

## 4. Candidate Recommendation & Matching Flow

```mermaid
sequenceDiagram
    actor R as Recruiter
    participant CRC as CandidateRecommendationController
    participant CRS as CandidateRecommendationService
    participant QSS as QualificationScoringService
    participant CWS as CandidateWeaviateService
    participant JPS as JobPostingService
    participant DB as Database
    participant Weaviate as Weaviate Vector DB
    
    R->>CRC: GET /api/recommendations/{jobPostingId}
    activate CRC
    
    CRC->>CRS: getRecommendedCandidates(jobPostingId, limit=50)
    activate CRS
    
    CRS->>JPS: getJobPosting(jobPostingId)
    activate JPS
    JPS->>DB: Fetch job posting details
    DB-->>JPS: Job with requirements
    Note over JPS: Required skills: Java, Spring Boot<br/>Experience: 3+ years<br/>Location: Hanoi<br/>Salary: 1500-2000 USD
    JPS-->>CRS: JobPostingDetails
    deactivate JPS
    
    CRS->>CWS: searchSimilarCandidates(jobRequirements)
    activate CWS
    
    CWS->>Weaviate: Vector similarity search
    Note over Weaviate: Query by skill embeddings<br/>+ experience vector<br/>+ location filter
    Weaviate-->>CWS: Top 100 candidate IDs
    
    CWS-->>CRS: CandidateList
    deactivate CWS
    
    CRS->>DB: Load candidate profiles (batch)
    DB-->>CRS: Full candidate data
    
    loop For each candidate
        CRS->>QSS: calculateQualificationScore(candidate, job)
        activate QSS
        
        QSS->>QSS: Score skills match (40%)
        Note over QSS: Required skills: 100%<br/>Bonus skills: +20%<br/>Total skills score: 8.5/10
        
        QSS->>QSS: Score experience (30%)
        Note over QSS: Years: 4 (meets 3+ req)<br/>Relevant positions: +10%<br/>Total exp score: 9.0/10
        
        QSS->>QSS: Score education (20%)
        Note over QSS: Degree match: Bachelor CS<br/>GPA: 3.5/4.0<br/>Total edu score: 8.0/10
        
        QSS->>QSS: Score location & preferences (10%)
        Note over QSS: Same city: +5%<br/>Salary expectation match: +4%<br/>Total pref score: 9.0/10
        
        QSS->>QSS: Calculate weighted total
        Note over QSS: Final Score = <br/>(8.5×0.4) + (9.0×0.3) + <br/>(8.0×0.2) + (9.0×0.1) = 8.6/10
        
        QSS-->>CRS: QualificationScore: 8.6
        deactivate QSS
    end
    
    CRS->>CRS: Sort by score descending
    CRS->>CRS: Filter by minimum threshold (>= 6.0)
    CRS->>CRS: Limit to top 50
    
    CRS->>DB: Check existing applications
    Note over DB: Exclude candidates who already applied
    
    CRS-->>CRC: RecommendationResponse[]
    Note over CRC: [<br/>  {candidateId: 123, score: 8.6, matchRate: 86%},<br/>  {candidateId: 456, score: 8.2, matchRate: 82%},<br/>  ...<br/>]
    deactivate CRS
    
    CRC-->>R: 200 OK + Candidate list
    deactivate CRC
    
    R->>R: Review candidates
    R->>CRC: POST /api/recommendations/invite
    Note over R: Send job invitation to top candidates
```

---

## 5. Employment Contract Lifecycle Flow

```mermaid
sequenceDiagram
    actor R as Recruiter
    actor C as Candidate
    participant ECC as EmploymentContractController
    participant ECS as EmploymentContractService
    participant FSS as FirebaseStorageService
    participant JAS as JobApplyService
    participant NP as NotificationProducer
    participant DB as Database
    
    Note over R,C: Phase 1: Contract Creation
    R->>ECC: POST /api/employment-contract/create
    Note over R: jobApplyId, startDate, salary,<br/>position, terms
    activate ECC
    
    ECC->>ECS: createContract(contractDto)
    activate ECS
    
    ECS->>DB: Get JobApply
    DB-->>ECS: JobApply (status=APPROVED)
    
    ECS->>DB: Create EmploymentContract
    Note over DB: status = DRAFT<br/>startDate = specified date<br/>probationMonths = 2<br/>salary = agreed amount
    DB-->>ECS: Contract created
    
    ECS->>FSS: generateContractPDF(contractData)
    activate FSS
    FSS->>FSS: Render PDF from template
    FSS->>FSS: Upload to Firebase Storage
    FSS-->>ECS: Contract PDF URL
    deactivate FSS
    
    ECS->>DB: Update contract with PDF URL
    
    ECS-->>ECC: ContractResponse
    deactivate ECS
    ECC-->>R: 201 Created
    deactivate ECC
    
    Note over R,C: Phase 2: Send to Candidate
    R->>ECC: POST /api/employment-contract/{id}/send
    activate ECC
    
    ECC->>ECS: sendToCandidate(contractId)
    activate ECS
    
    ECS->>DB: Update contract status
    Note over DB: status = SENT_TO_CANDIDATE<br/>sentAt = now()
    
    ECS->>NP: publishEvent("CONTRACT_SENT")
    NP->>C: Email: "Contract awaiting your signature"
    NP->>C: Attach: Contract PDF + signing link
    
    ECS-->>ECC: Success
    deactivate ECS
    ECC-->>R: 200 OK
    deactivate ECC
    
    Note over R,C: Phase 3: Candidate Review & Sign
    C->>ECC: GET /api/employment-contract/{id}
    ECC-->>C: Contract details + PDF
    
    C->>C: Review contract terms
    
    alt Candidate Accepts
        C->>ECC: POST /api/employment-contract/{id}/sign
        Note over C: Digital signature + consent
        activate ECC
        
        ECC->>ECS: candidateSign(contractId, signature)
        activate ECS
        
        ECS->>DB: Update contract
        Note over DB: status = CANDIDATE_SIGNED<br/>candidateSignedAt = now()<br/>candidateSignature = signature
        
        ECS->>NP: publishEvent("CONTRACT_SIGNED_BY_CANDIDATE")
        NP->>R: Email: "Candidate signed contract"
        
        ECS-->>ECC: Success
        deactivate ECS
        ECC-->>C: 200 OK
        deactivate ECC
        
        Note over R,C: Phase 4: Company Counter-Sign
        R->>ECC: POST /api/employment-contract/{id}/company-sign
        activate ECC
        
        ECC->>ECS: companySign(contractId, signature)
        activate ECS
        
        ECS->>DB: Update contract
        Note over DB: status = COMPANY_SIGNED<br/>companySignedAt = now()<br/>companySignature = signature
        
        ECS->>DB: Activate contract
        Note over DB: status = ACTIVE<br/>activatedAt = now()
        
        ECS->>JAS: updateStatus(jobApplyId, WORKING)
        activate JAS
        JAS->>DB: Update JobApply status
        JAS->>DB: Record status history
        JAS-->>ECS: Success
        deactivate JAS
        
        ECS->>NP: publishEvent("CONTRACT_ACTIVATED")
        NP->>C: Email: "Contract fully executed - Welcome aboard!"
        NP->>R: Email: "Contract fully executed"
        
        ECS-->>ECC: Success
        deactivate ECS
        ECC-->>R: 200 OK
        deactivate ECC
        
    else Candidate Declines
        C->>ECC: POST /api/employment-contract/{id}/decline
        Note over C: Reason: salary expectations<br/>not met
        activate ECC
        
        ECC->>ECS: candidateDecline(contractId, reason)
        activate ECS
        
        ECS->>DB: Update contract
        Note over DB: status = DECLINED_BY_CANDIDATE<br/>declineReason = reason<br/>declinedAt = now()
        
        ECS->>JAS: updateStatus(jobApplyId, REJECTED)
        
        ECS->>NP: publishEvent("CONTRACT_DECLINED")
        NP->>R: Email: "Candidate declined contract"
        
        ECS-->>ECC: Success
        deactivate ECS
        ECC-->>C: 200 OK
        deactivate ECC
    end
    
    Note over R,C: Phase 5: Termination (Future)
    
    opt Contract Termination
        R->>ECC: POST /api/employment-contract/{id}/terminate
        Note over R: Termination type, reason, notice period
        activate ECC
        
        ECC->>ECS: terminateContract(contractId, terminationDto)
        activate ECS
        
        ECS->>DB: Update contract
        Note over DB: status = TERMINATED<br/>terminationType = RESIGNATION/FIRED<br/>terminationDate = now()<br/>endedAt = now()
        
        ECS->>JAS: updateStatus(jobApplyId, TERMINATED)
        
        ECS->>NP: publishEvent("CONTRACT_TERMINATED")
        NP->>C: Email: "Employment terminated"
        NP->>R: Email: "Contract termination processed"
        
        ECS-->>ECC: Success
        deactivate ECS
        ECC-->>R: 200 OK
        deactivate ECC
    end
```

---

## 6. Company Review Submission & Moderation Flow

```mermaid
sequenceDiagram
    actor C as Candidate
    actor A as Admin
    participant CRC as CompanyReviewController
    participant CRS as CompanyReviewService
    participant RES as ReviewEligibilityService
    participant EVS as EmploymentVerificationService
    participant ISS as InterviewScheduleService
    participant NP as NotificationProducer
    participant DB as Database
    
    Note over C: Phase 1: Check Eligibility
    C->>CRC: GET /api/company-review/eligibility/{jobApplyId}
    activate CRC
    
    CRC->>RES: checkEligibility(jobApplyId)
    activate RES
    
    RES->>DB: Get JobApply
    DB-->>RES: JobApply (status=WORKING)
    
    RES->>ISS: getInterviewStatus(jobApplyId)
    activate ISS
    ISS->>DB: Query InterviewSchedule
    DB-->>ISS: Interview (status=COMPLETED)
    ISS-->>RES: Interview completed
    deactivate ISS
    
    RES->>EVS: getEmploymentDuration(jobApplyId)
    activate EVS
    EVS->>DB: Get EmploymentVerification
    DB-->>EVS: Employment (daysEmployed=45, isActive=true)
    EVS-->>RES: 45 days employed
    deactivate EVS
    
    RES->>DB: Get existing reviews for this jobApply
    DB-->>RES: [ApplicationReview, InterviewReview]
    
    RES->>RES: Determine qualification
    Note over RES: Status: WORKING<br/>Interview: COMPLETED<br/>Days employed: 45 (>= 30)<br/><br/>Qualification: HIRED<br/>Can review application: Already done<br/>Can review interview: Already done<br/>Can review work experience: YES
    
    RES-->>CRC: EligibilityResponse
    Note over CRC: {<br/>  qualification: "HIRED",<br/>  canReviewApplication: false,<br/>  canReviewInterview: false,<br/>  canReviewWorkExperience: true<br/>}
    deactivate RES
    
    CRC-->>C: 200 OK
    deactivate CRC
    
    Note over C: Phase 2: Submit Review
    C->>CRC: POST /api/company-review
    Note over C: reviewType: WORK_EXPERIENCE<br/>ratings: {overall: 4.5, workCulture: 5, ...}<br/>reviewText, pros, cons
    activate CRC
    
    CRC->>CRS: createReview(reviewDto)
    activate CRS
    
    CRS->>RES: validateEligibility(jobApplyId, WORK_EXPERIENCE)
    activate RES
    RES->>RES: Re-check eligibility
    RES-->>CRS: Eligible
    deactivate RES
    
    CRS->>DB: Check duplicate review
    DB-->>CRS: No existing WORK_EXPERIENCE review
    
    CRS->>DB: Create CompanyReview
    Note over DB: status = PENDING_MODERATION<br/>reviewType = WORK_EXPERIENCE<br/>overallRating = 4.5<br/>workCultureRating = 5.0<br/>reviewText = "Great company..."<br/>createdAt = now()
    DB-->>CRS: Review created
    
    CRS->>NP: publishEvent("REVIEW_SUBMITTED")
    NP->>A: Email: "New review pending moderation"
    
    CRS-->>CRC: ReviewResponse
    Note over CRC: Review submitted successfully.<br/>Under admin review.
    deactivate CRS
    
    CRC-->>C: 201 Created
    deactivate CRC
    
    Note over A: Phase 3: Admin Moderation
    A->>CRC: GET /api/admin/reviews/pending
    CRC-->>A: List of pending reviews
    
    A->>A: Review content for policy violations
    
    alt Review Approved
        A->>CRC: POST /api/admin/reviews/{id}/approve
        activate CRC
        
        CRC->>CRS: approveReview(reviewId, adminId)
        activate CRS
        
        CRS->>DB: Update review status
        Note over DB: status = APPROVED<br/>moderatedAt = now()<br/>moderatedByAdminId = adminId
        
        CRS->>DB: Update company statistics
        Note over DB: Recalculate recruiter avg rating<br/>Update review count by type<br/>Update rating breakdown
        
        CRS->>NP: publishEvent("REVIEW_APPROVED")
        NP->>C: Email: "Your review has been published"
        
        CRS-->>CRC: Success
        deactivate CRS
        CRC-->>A: 200 OK
        deactivate CRC
        
    else Review Rejected
        A->>CRC: POST /api/admin/reviews/{id}/reject
        Note over A: Reason: Inappropriate language
        activate CRC
        
        CRC->>CRS: rejectReview(reviewId, reason, adminId)
        activate CRS
        
        CRS->>DB: Update review status
        Note over DB: status = REJECTED<br/>moderationNotes = reason<br/>moderatedAt = now()
        
        CRS->>NP: publishEvent("REVIEW_REJECTED")
        NP->>C: Email: "Review not approved: [reason]"
        
        CRS-->>CRC: Success
        deactivate CRS
        CRC-->>A: 200 OK
        deactivate CRC
        
    else Review Flagged (by users)
        Note over C: Another user flags review as inappropriate
        C->>CRC: POST /api/company-review/{id}/flag
        activate CRC
        
        CRC->>CRS: flagReview(reviewId, reason)
        activate CRS
        
        CRS->>DB: Update review
        Note over DB: status = FLAGGED<br/>Add flag count
        
        CRS->>NP: publishEvent("REVIEW_FLAGGED")
        NP->>A: Email: "Review flagged for moderation"
        
        CRS-->>CRC: Success
        deactivate CRS
        CRC-->>C: 200 OK
        deactivate CRC
    end
    
    Note over C: Phase 4: Public View
    C->>CRC: GET /api/company-review/company/{recruiterId}
    Note over C: View all approved reviews for company
    activate CRC
    
    CRC->>CRS: getCompanyReviews(recruiterId, filters)
    activate CRS
    
    CRS->>DB: Query approved reviews
    Note over DB: WHERE recruiterId = X<br/>AND status = APPROVED<br/>ORDER BY createdAt DESC
    DB-->>CRS: Review list
    
    CRS->>DB: Get rating statistics
    DB-->>CRS: Stats (avgRating, reviewCount by type)
    
    CRS-->>CRC: ReviewsWithStats
    deactivate CRS
    
    CRC-->>C: 200 OK
    Note over C: Display reviews + ratings breakdown
    deactivate CRC
```

---

## 7. Payment Processing Flow (Recruiter & Candidate)

```mermaid
sequenceDiagram
    actor U as User (Recruiter/Candidate)
    participant PC as PaymentController
    participant PS as PaymentService
    participant IS as InvoiceService
    participant PKS as PackageService
    participant ES as EntitlementService
    participant PG as Payment Gateway (VNPay/Stripe)
    participant NP as NotificationProducer
    participant DB as Database
    
    Note over U: Phase 1: Browse Packages
    U->>PC: GET /api/packages
    activate PC
    
    PC->>PKS: getAvailablePackages(userType)
    activate PKS
    PKS->>DB: Query active packages
    DB-->>PKS: Package list
    Note over DB: [<br/>  {id: 1, name: "Basic", price: 99$, features: [10 posts]},<br/>  {id: 2, name: "Pro", price: 299$, features: [50 posts]},<br/>  {id: 3, name: "Enterprise", price: 999$}<br/>]
    PKS-->>PC: PackageResponse[]
    deactivate PKS
    
    PC-->>U: 200 OK
    deactivate PC
    
    Note over U: Phase 2: Initiate Payment
    U->>PC: POST /api/payment/create
    Note over U: {<br/>  packageId: 2,<br/>  paymentMethod: "VNPAY"<br/>}
    activate PC
    
    PC->>PS: createPayment(packageId, paymentMethod)
    activate PS
    
    PS->>PKS: getPackage(packageId)
    activate PKS
    PKS->>DB: Get package details
    DB-->>PKS: Package (price=299$, features)
    PKS-->>PS: PackageDetails
    deactivate PKS
    
    PS->>IS: createInvoice(userId, packageId, amount)
    activate IS
    IS->>DB: Create Invoice
    Note over DB: status = PENDING<br/>amount = 299$<br/>invoiceNumber = auto-generated<br/>createdAt = now()
    DB-->>IS: Invoice created (invoiceId: 12345)
    IS-->>PS: InvoiceResponse
    deactivate IS
    
    PS->>DB: Create Payment record
    Note over DB: status = PENDING<br/>invoiceId = 12345<br/>amount = 299$<br/>paymentMethod = VNPAY<br/>transactionId = generated
    DB-->>PS: Payment created
    
    PS->>PG: initializePayment(paymentData)
    activate PG
    Note over PG: Create payment session<br/>Generate payment URL
    PG-->>PS: PaymentURL + sessionToken
    deactivate PG
    
    PS-->>PC: PaymentInitResponse
    Note over PC: {<br/>  paymentUrl: "https://vnpay.com/...",<br/>  transactionId: "TXN123",<br/>  expiresIn: 900 (15 min)<br/>}
    deactivate PS
    
    PC-->>U: 200 OK + Payment URL
    deactivate PC
    
    Note over U: Phase 3: User Redirected to Payment Gateway
    U->>PG: Navigate to payment URL
    activate PG
    
    PG-->>U: Payment form (card details, bank selection)
    U->>PG: Submit payment
    
    PG->>PG: Process payment
    Note over PG: Validate card/account<br/>Authorize transaction<br/>Capture funds
    
    alt Payment Successful
        PG-->>U: Payment successful page
        Note over U: Redirect back to CareerMate
        
        PG->>PC: POST /api/payment/callback (webhook)
        Note over PG: {<br/>  transactionId: "TXN123",<br/>  status: "SUCCESS",<br/>  signature: "hash",<br/>  amount: 299$<br/>}
        activate PC
        
        PC->>PS: handlePaymentCallback(callbackData)
        activate PS
        
        PS->>PS: validateSignature(callbackData)
        Note over PS: Verify webhook authenticity<br/>Prevent tampering
        
        PS->>DB: Update Payment status
        Note over DB: status = SUCCESS<br/>paidAt = now()<br/>gatewayTransactionId = PG_TXN_456
        
        PS->>IS: updateInvoiceStatus(invoiceId, PAID)
        activate IS
        IS->>DB: Update Invoice
        Note over DB: status = PAID<br/>paidAt = now()
        IS-->>PS: Success
        deactivate IS
        
        PS->>ES: activateEntitlement(userId, packageId)
        activate ES
        ES->>DB: Create/Update Entitlement
        Note over DB: userId = user123<br/>packageId = 2<br/>startDate = now()<br/>expiryDate = now() + 30 days<br/>postingCredits = 50<br/>isActive = true
        ES-->>PS: Entitlement activated
        deactivate ES
        
        PS->>NP: publishEvent("PAYMENT_SUCCESS")
        NP->>U: Email: "Payment confirmed - Package activated"
        NP->>U: SMS: "50 posting credits added"
        
        PS-->>PC: Success
        deactivate PS
        
        PC-->>PG: 200 OK
        deactivate PC
        
    else Payment Failed
        PG-->>U: Payment failed page
        Note over U: Insufficient funds / Card declined
        
        PG->>PC: POST /api/payment/callback
        Note over PG: {<br/>  transactionId: "TXN123",<br/>  status: "FAILED",<br/>  errorCode: "INSUFFICIENT_FUNDS"<br/>}
        activate PC
        
        PC->>PS: handlePaymentCallback(callbackData)
        activate PS
        
        PS->>DB: Update Payment status
        Note over DB: status = FAILED<br/>failureReason = errorCode
        
        PS->>IS: updateInvoiceStatus(invoiceId, FAILED)
        
        PS->>NP: publishEvent("PAYMENT_FAILED")
        NP->>U: Email: "Payment failed - Please try again"
        
        PS-->>PC: Failure acknowledged
        deactivate PS
        
        PC-->>PG: 200 OK
        deactivate PC
    end
    
    deactivate PG
    
    Note over U: Phase 4: View Entitlement
    U->>PC: GET /api/entitlement/current
    activate PC
    
    PC->>ES: getCurrentEntitlement(userId)
    activate ES
    ES->>DB: Query active entitlement
    DB-->>ES: Entitlement data
    ES-->>PC: EntitlementResponse
    Note over PC: {<br/>  packageName: "Pro",<br/>  creditsRemaining: 50,<br/>  expiryDate: "2025-01-12",<br/>  features: [...]<br/>}
    deactivate ES
    
    PC-->>U: 200 OK
    deactivate PC
```

---

## 8. Email Delivery, File Storage & Blog Management Flow

### 8.1 Email Notification System (Generic Flow)

```mermaid
sequenceDiagram
    participant Service as Any Service Layer
    participant NP as NotificationProducer
    participant Kafka as Kafka Broker
    participant NC as NotificationConsumer
    participant ES as EmailService
    participant SMTP as SMTP Server
    participant DB as Database
    
    Note over Service: Trigger: Job applied, Interview scheduled,<br/>Payment success, Review approved, etc.
    
    Service->>NP: publishEvent(eventType, recipientData, templateData)
    activate NP
    Note over NP: Event types:<br/>- JOB_APPLICATION_SUBMITTED<br/>- INTERVIEW_SCHEDULED<br/>- PAYMENT_SUCCESS<br/>- CONTRACT_SENT<br/>- REVIEW_APPROVED<br/>- STATUS_UPDATE_REQUIRED<br/>... (21+ event types)
    
    NP->>Kafka: Send to "notifications" topic
    activate Kafka
    Kafka-->>NP: Acknowledged
    deactivate Kafka
    NP-->>Service: Event published
    deactivate NP
    
    Note over Kafka,NC: Async processing - decoupled from source
    
    Kafka->>NC: Consume notification event
    activate NC
    
    NC->>NC: Determine channels
    Note over NC: EMAIL ✓<br/>SMS (optional)<br/>IN_APP ✓<br/>PUSH (mobile)
    
    par Email Notification
        NC->>ES: sendEmail(recipient, eventType, data)
        activate ES
        
        ES->>DB: Load template by eventType
        DB-->>ES: Email template
        Note over ES: Templates:<br/>interview_scheduled.html<br/>payment_success.html<br/>contract_sent.html<br/>review_approved.html
        
        ES->>ES: Populate template variables
        Note over ES: Dynamic data:<br/>{{userName}}<br/>{{eventDetails}}<br/>{{actionLink}}
        
        ES->>SMTP: Send email
        activate SMTP
        
        alt Email Success
            SMTP-->>ES: 250 OK
            ES->>DB: Log: status=SENT
        else Email Failure
            SMTP-->>ES: Error
            ES->>DB: Log: status=FAILED, retry++
            ES->>ES: Schedule retry
        end
        deactivate SMTP
        
        ES-->>NC: Email processed
        deactivate ES
        
    and In-App Notification
        NC->>DB: Create NotificationRecord
        Note over DB: userId, eventType,<br/>message, read=false
    end
    
    NC-->>Kafka: Acknowledge
    deactivate NC
```

### 8.2 File Storage Flow (Firebase)

```mermaid
sequenceDiagram
    actor U as User
    participant FC as FileController
    participant FS as FileService
    participant FSS as FirebaseStorageService
    participant Firebase as Firebase Storage
    participant DB as Database
    
    U->>FC: POST /api/files/upload
    Note over U: Multipart file upload<br/>(resume PDF, image, document)
    activate FC
    
    FC->>FC: Validate file
    Note over FC: Check file size (< 10MB)<br/>Check file type (PDF, JPG, PNG, DOCX)<br/>Check content type
    
    FC->>FS: uploadFile(file, metadata)
    activate FS
    
    FS->>FS: Generate unique filename
    Note over FS: UUID + original extension<br/>"abc123-resume.pdf"
    
    FS->>FSS: uploadToFirebase(file, path)
    activate FSS
    
    FSS->>FSS: Initialize Firebase Admin SDK
    Note over FSS: Use service account credentials
    
    FSS->>Firebase: Upload file to bucket
    Note over Firebase: Path: /uploads/resumes/abc123-resume.pdf<br/>Metadata: {contentType, size, uploadedBy}
    activate Firebase
    
    Firebase->>Firebase: Store file
    Firebase-->>FSS: File uploaded successfully
    deactivate Firebase
    
    FSS->>Firebase: Generate signed URL (public access)
    activate Firebase
    Firebase-->>FSS: Public download URL
    Note over Firebase: https://storage.googleapis.com/<br/>careermate/uploads/resumes/abc123-resume.pdf
    deactivate Firebase
    
    FSS-->>FS: FileUploadResponse (URL, metadata)
    deactivate FSS
    
    FS->>DB: Save file metadata
    Note over DB: FileRecord:<br/>fileName, fileUrl, fileType<br/>uploadedBy, uploadedAt<br/>fileSize
    DB-->>FS: Metadata saved
    
    FS-->>FC: FileResponse
    Note over FC: {<br/>  fileId: "123",<br/>  fileName: "resume.pdf",<br/>  fileUrl: "https://...",<br/>  uploadedAt: "2025-12-13T..."<br/>}
    deactivate FS
    
    FC-->>U: 200 OK
    deactivate FC
    
    opt File Download
        U->>FC: GET /api/files/{fileId}
        activate FC
        FC->>FS: getFile(fileId)
        activate FS
        FS->>DB: Query file metadata
        DB-->>FS: FileRecord with URL
        FS-->>FC: FileResponse
        deactivate FS
        FC-->>U: Redirect to Firebase URL
        deactivate FC
        U->>Firebase: Download file directly
        Firebase-->>U: File stream
    end
    
    opt File Delete
        U->>FC: DELETE /api/files/{fileId}
        activate FC
        FC->>FS: deleteFile(fileId)
        activate FS
        FS->>DB: Get file metadata
        DB-->>FS: FileRecord
        FS->>FSS: deleteFromFirebase(filePath)
        activate FSS
        FSS->>Firebase: Delete file
        Firebase-->>FSS: Deleted
        FSS-->>FS: Success
        deactivate FSS
        FS->>DB: Delete file record
        FS-->>FC: Deleted
        deactivate FS
        FC-->>U: 204 No Content
        deactivate FC
    end
```

### 8.3 Blog Management Flow

```mermaid
sequenceDiagram
    actor A as Admin/Editor
    actor U as User (Reader)
    participant BC as BlogController
    participant BS as BlogService
    participant BCS as BlogCommentService
    participant BRS as BlogRatingService
    participant ACMC as AdminCommentModerationController
    participant DB as Database
    
    Note over A: Phase 1: Create Blog Post
    A->>BC: POST /api/blog
    Note over A: {<br/>  title: "Top 10 Interview Tips",<br/>  content: "...",<br/>  tags: ["interview", "tips"],<br/>  category: "CAREER_ADVICE"<br/>}
    activate BC
    
    BC->>BS: createBlog(blogDto)
    activate BS
    
    BS->>DB: Create Blog entity
    Note over DB: status = DRAFT<br/>title, content, tags<br/>authorId, createdAt
    DB-->>BS: Blog created (blogId: 456)
    
    BS-->>BC: BlogResponse
    deactivate BS
    BC-->>A: 201 Created
    deactivate BC
    
    A->>BC: PATCH /api/blog/{id}/publish
    activate BC
    BC->>BS: publishBlog(blogId)
    activate BS
    BS->>DB: Update blog status
    Note over DB: status = PUBLISHED<br/>publishedAt = now()
    BS-->>BC: Success
    deactivate BS
    BC-->>A: 200 OK
    deactivate BC
    
    Note over U: Phase 2: Read Blog
    U->>BC: GET /api/blog/{id}
    activate BC
    
    BC->>BS: getBlog(blogId)
    activate BS
    BS->>DB: Query blog
    DB-->>BS: Blog data
    BS->>DB: Increment view count
    BS-->>BC: BlogResponse
    deactivate BS
    
    BC-->>U: 200 OK
    Note over U: Display blog content
    deactivate BC
    
    Note over U: Phase 3: Rate Blog
    U->>BC: POST /api/blog/{id}/rating
    Note over U: {rating: 5, comment: "Very helpful!"}
    activate BC
    
    BC->>BRS: rateBlog(blogId, userId, rating)
    activate BRS
    
    BRS->>DB: Check existing rating
    DB-->>BRS: No previous rating
    
    BRS->>DB: Create BlogRating
    Note over DB: blogId, userId<br/>rating = 5<br/>createdAt = now()
    
    BRS->>DB: Update blog avg rating
    Note over DB: Calculate new average:<br/>avgRating = (sum of all ratings) / count
    
    BRS-->>BC: RatingResponse
    deactivate BRS
    BC-->>U: 201 Created
    deactivate BC
    
    Note over U: Phase 4: Comment on Blog
    U->>BC: POST /api/blog/{id}/comments
    Note over U: {content: "Great article!"}
    activate BC
    
    BC->>BCS: addComment(blogId, userId, content)
    activate BCS
    
    BCS->>DB: Create BlogComment
    Note over DB: status = PENDING_MODERATION<br/>blogId, userId<br/>content, createdAt
    DB-->>BCS: Comment created
    
    BCS-->>BC: CommentResponse
    Note over BC: "Comment submitted.<br/>Awaiting moderation."
    deactivate BCS
    BC-->>U: 201 Created
    deactivate BC
    
    Note over A: Phase 5: Moderate Comments
    A->>ACMC: GET /api/admin/comments/pending
    activate ACMC
    ACMC->>BCS: getPendingComments()
    activate BCS
    BCS->>DB: Query pending comments
    DB-->>BCS: Comment list
    BCS-->>ACMC: Comments
    deactivate BCS
    ACMC-->>A: 200 OK
    deactivate ACMC
    
    alt Approve Comment
        A->>ACMC: POST /api/admin/comments/{id}/approve
        activate ACMC
        ACMC->>BCS: approveComment(commentId, adminId)
        activate BCS
        BCS->>DB: Update comment
        Note over DB: status = APPROVED<br/>moderatedAt = now()<br/>moderatedBy = adminId
        BCS-->>ACMC: Success
        deactivate BCS
        ACMC-->>A: 200 OK
        deactivate ACMC
        
    else Reject Comment
        A->>ACMC: POST /api/admin/comments/{id}/reject
        Note over A: Reason: Spam content
        activate ACMC
        ACMC->>BCS: rejectComment(commentId, reason)
        activate BCS
        BCS->>DB: Update comment
        Note over DB: status = REJECTED<br/>rejectionReason = reason
        BCS-->>ACMC: Success
        deactivate BCS
        ACMC-->>A: 200 OK
        deactivate ACMC
    end
    
    Note over U: Phase 6: View Approved Comments
    U->>BC: GET /api/blog/{id}/comments
    activate BC
    BC->>BCS: getApprovedComments(blogId)
    activate BCS
    BCS->>DB: Query approved comments
    Note over DB: WHERE blogId = X<br/>AND status = APPROVED<br/>ORDER BY createdAt DESC
    DB-->>BCS: Comment list
    BCS-->>BC: Comments
    deactivate BCS
    BC-->>U: 200 OK
    Note over U: Display comments below blog
    deactivate BC
```

---

## Diagram Usage Notes

**Viewing Mermaid Diagrams:**
1. Copy the code blocks to [Mermaid Live Editor](https://mermaid.live)
2. View in VS Code with Mermaid extension
3. Render in markdown viewers that support Mermaid

**Diagram Conventions:**
- **Actors** (hexagon): Human users (Candidate, Recruiter, Admin)
- **Participants** (rectangle): System components (Controllers, Services, Database)
- **Solid arrows** (→): Synchronous calls
- **Dashed arrows** (-->>): Responses
- **Activate bars**: Show execution time
- **Notes**: Provide context and data examples
- **Alt/Opt blocks**: Show conditional flows

**Integration Points:**
- 🟢 **Kafka**: Async event-driven notifications
- 🔵 **Firebase**: Cloud file storage
- 🟣 **Payment Gateways**: VNPay, Stripe
- 🟡 **SMTP**: Email delivery
- 🔴 **Weaviate**: Vector similarity search

---

**Last Updated**: December 13, 2025  
**Format**: Mermaid Sequence Diagrams  
**Status**: Production-ready documentation
