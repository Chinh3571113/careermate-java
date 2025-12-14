# Sequence Diagrams Required for CareerMate Project Documentation

## Overview
This document lists all sequence diagrams needed to comprehensively document the CareerMate job application platform. The diagrams are organized by priority (Critical, High, Medium) and service domain.

---

## 🔴 CRITICAL PRIORITY - Core Business Workflows

### 1. Job Application Submission Flow
**Domain**: Job Services  
**Actors**: Candidate, System, Recruiter  
**Purpose**: Document the complete workflow from candidate applying to a job through initial status assignment

**Key Steps**:
- Candidate submits application via `JobApplyController.applyForJob()`
- System validates candidate eligibility
- System checks resume completeness
- System creates JobApply record with SUBMITTED status
- System triggers notification to recruiter
- System updates candidate's application history

**Services Involved**:
- JobApplyService
- ResumeService
- NotificationService
- CandidateProfileService

**Transitions to**: Interview Scheduling Flow or Application Review Flow

---

### 2. Interview Scheduling & Management Flow
**Domain**: Job Services + Interview Services  
**Actors**: Recruiter, Candidate, System  
**Purpose**: Document interview lifecycle from scheduling to completion

**Key Steps**:
- Recruiter creates interview via `InterviewScheduleController.scheduleInterview()`
- System validates time slot availability via `InterviewCalendarService`
- System sends notification to candidate
- Candidate confirms/reschedules interview
- System sends 24h and 2h reminder notifications
- Interview conducted
- Recruiter updates interview outcome (PASS/FAIL/NEEDS_SECOND_ROUND)
- System updates JobApply status based on outcome

**Services Involved**:
- InterviewScheduleService
- InterviewCalendarService
- NotificationService
- JobApplyService

**State Diagram Reference**: INTERVIEW_STATE_DIAGRAM.drawio

---

### 3. Bilateral Status Verification Flow (Status Update Request)
**Domain**: Job Services  
**Actors**: Candidate, Recruiter, Admin, System  
**Purpose**: Document the dispute resolution mechanism when candidate and recruiter disagree on status

**Key Steps**:
- Candidate initiates status change request via `StatusUpdateController.requestStatusUpdate()`
- Candidate uploads evidence files
- System calculates evidence trust score
- System sends verification request to recruiter (7-day deadline)
- **Scenario A**: Recruiter confirms → Status updated, notifications sent
- **Scenario B**: Recruiter disputes → Escalates to admin
- **Scenario C**: No response after 7 days → Auto-approved
- Admin resolves dispute (if escalated)
- System updates employment verification records
- System records status history

**Services Involved**:
- StatusUpdateService
- EvidenceFileService
- StatusDisputeService
- EmploymentVerificationService
- NotificationService

**Critical Features**:
- Evidence file trust scoring (3-10 scale)
- 7-day auto-approval mechanism
- Priority calculation for admin queue
- Status history recording

---

### 4. Candidate Recommendation & Matching Flow
**Domain**: Recommendation Services  
**Actors**: System, Recruiter, Candidate  
**Purpose**: Document AI-powered candidate-job matching algorithm

**Key Steps**:
- Recruiter creates job posting with requirements
- System indexes job via `CandidateWeaviateService`
- System calculates qualification scores via `QualificationScoringService`
- System retrieves top-N matching candidates
- System ranks by skills, experience, education
- System filters by location, salary expectations
- Recruiter reviews recommended candidates
- Recruiter sends job invitations

**Services Involved**:
- CandidateRecommendationService
- QualificationScoringService
- CandidateWeaviateService
- JobPostingService

**Algorithm Details**:
- Vector similarity search
- Multi-factor scoring (skills 40%, experience 30%, education 20%, location 10%)
- Real-time index updates

---

### 5. Employment Contract Lifecycle Flow
**Domain**: Job Services  
**Actors**: Recruiter, Candidate, System  
**Purpose**: Document contract creation, signing, and termination

**Key Steps**:
- Recruiter creates contract via `EmploymentContractController.createContract()`
- System generates contract PDF with terms
- System sends contract to candidate for signature
- Candidate reviews and signs (digital signature)
- System notifies recruiter of signature
- Recruiter counter-signs
- Contract becomes active
- **Termination Path**: Either party initiates termination
- System updates JobApply status to TERMINATED
- System records termination details in EmploymentVerification

**Services Involved**:
- EmploymentContractService
- EmploymentVerificationService
- StatusUpdateService
- NotificationService
- FileStorageService (contract PDF)

**State Transitions**:
- DRAFT → SENT_TO_CANDIDATE → CANDIDATE_SIGNED → COMPANY_SIGNED → ACTIVE → TERMINATED

---

### 6. Company Review Submission & Moderation Flow
**Domain**: Review Services  
**Actors**: Candidate, Admin, System, Recruiter  
**Purpose**: Document review lifecycle with eligibility checks and moderation

**Key Steps**:
- Candidate checks eligibility via `CompanyReviewController.checkEligibility()`
- System validates review qualification:
  - APPLICATION_EXPERIENCE: Applied to job
  - INTERVIEW_EXPERIENCE: Completed interview
  - WORK_EXPERIENCE: Employed ≥30 days
- Candidate submits review with ratings
- System calculates overall rating
- System queues review for moderation (status: PENDING_MODERATION)
- Admin reviews via `AdminCommentModerationController`
- Admin approves/rejects/flags review
- If approved: Review published, company statistics updated
- System sends notification to candidate

**Services Involved**:
- CompanyReviewService
- ReviewEligibilityService
- EmploymentVerificationService
- NotificationService

**Review Types**:
1. Application Experience (any applicant)
2. Interview Experience (interviewed candidates only)
3. Work Experience (employed ≥30 days only)

---

## 🟡 HIGH PRIORITY - Core Feature Flows

### 7. User Registration & Authentication Flow
**Domain**: Authentication Services  
**Actors**: User (Candidate/Recruiter), System, Email Service  
**Purpose**: Document user onboarding and login process

**Key Steps**:
- User submits registration via `RegistrationController.register()`
- System validates email uniqueness
- System creates Account entity
- System generates verification token
- System sends verification email
- User clicks verification link
- System activates account
- User logs in via `AuthenticationController.login()`
- System generates JWT access + refresh tokens
- System creates active session

**Services Involved**:
- AccountService
- AuthenticationService
- EmailService
- TokenService

**OAuth2 Integration**:
- Google OAuth2 for recruiters via `OAuth2RecruiterController`

---

### 8. Resume Creation & Management Flow
**Domain**: Resume Services  
**Actors**: Candidate, System  
**Purpose**: Document comprehensive resume building workflow

**Key Steps**:
- Candidate creates resume via `ResumeController.createResume()`
- Candidate adds education via `EducationController.addEducation()`
- Candidate adds work experience via `WorkExpController.addWorkExperience()`
- Candidate adds skills via `SkillController.addSkill()`
- Candidate adds certifications via `CertificateController.addCertificate()`
- Candidate adds awards via `AwardController.addAward()`
- Candidate adds languages via `ForeignLanguageController.addLanguage()`
- Candidate adds projects via `HighLightProjectController.addProject()`
- System calculates resume completeness score
- System validates required fields
- Candidate sets primary resume
- System updates candidate profile

**Services Involved**:
- ResumeService
- EducationService
- WorkExperienceService
- SkillService
- CertificateService
- AwardService
- ForeignLanguageService
- HighLightProjectService

**Resume Types**:
- MASTER (primary comprehensive resume)
- TAILORED (job-specific versions)

---

### 9. Payment Processing Flow (Recruiter)
**Domain**: Payment Services  
**Actors**: Recruiter, System, Payment Gateway, Admin  
**Purpose**: Document subscription payment and entitlement activation

**Key Steps**:
- Recruiter selects package via `PackageController.getPackages()`
- Recruiter initiates payment via `RecruiterPaymentController.createPayment()`
- System creates invoice via `RecruiterInvoiceService`
- System redirects to payment gateway (VNPay/Stripe)
- Payment gateway processes payment
- Payment gateway sends webhook callback
- System validates payment signature
- System activates entitlement via `RecruiterEntitlementService`
- System grants posting credits
- System sends confirmation email

**Services Involved**:
- RecruiterPaymentService
- RecruiterInvoiceService
- RecruiterEntitlementService
- PackageService
- NotificationService

**Payment States**:
- PENDING → PROCESSING → SUCCESS → COMPLETED
- PENDING → PROCESSING → FAILED

---

### 10. Payment Processing Flow (Candidate)
**Domain**: Payment Services  
**Actors**: Candidate, System, Payment Gateway  
**Purpose**: Document candidate premium feature purchases

**Key Steps**:
- Similar to Recruiter Payment Flow but via `CandidatePaymentController`
- Grants access to premium features (coach services, advanced analytics)
- Activates via `CandidateEntitlementService`

**Services Involved**:
- CandidatePaymentService
- CandidateInvoiceService
- CandidateEntitlementService

---

### 11. Job Posting Creation & Management Flow
**Domain**: Job Services  
**Actors**: Recruiter, System, Admin  
**Purpose**: Document job posting lifecycle

**Key Steps**:
- Recruiter creates posting via `JobPostingController.createJobPosting()`
- System validates entitlement credits
- System extracts skills via `JdSkillService`
- System publishes job
- System indexes for search/recommendations
- Candidates search and apply
- Admin moderates flagged postings via `AdminJobPostingController`
- Recruiter closes/archives posting

**Services Involved**:
- JobPostingService
- JdSkillService
- RecruiterEntitlementService
- CandidateRecommendationService

**Job Posting States**:
- DRAFT → PUBLISHED → ACTIVE → CLOSED → ARCHIVED

---

### 12. Notification Delivery Flow
**Domain**: Notification Services  
**Actors**: System, Kafka, User (Candidate/Recruiter/Admin)  
**Purpose**: Document event-driven notification system

**Key Steps**:
- Service publishes event to Kafka via `NotificationProducer`
- Kafka broker receives event
- `NotificationConsumer` processes event
- System determines notification channels (Email, SMS, Push, In-App)
- System sends via appropriate channel
- System creates in-app notification record
- User receives notification
- User reads notification via `NotificationController.getNotifications()`
- System marks as read
- **SSE Connection**: Real-time delivery via `NotificationSseController`

**Services Involved**:
- NotificationService
- NotificationProducer (Kafka)
- NotificationConsumer (Kafka)
- EmailService
- DeviceTokenService (for push notifications)

**Notification Types** (21 total):
- Interview reminders (24h, 2h before)
- Status update requests/approvals
- Contract signing notifications
- Payment confirmations
- Review moderation results

---

### 13. Admin Dashboard & Analytics Flow
**Domain**: Admin Services  
**Actors**: Admin, System  
**Purpose**: Document administrative overview and reporting

**Key Steps**:
- Admin accesses dashboard via `AdminDashboardController.getDashboardStats()`
- System aggregates real-time statistics:
  - Total users (candidates, recruiters)
  - Active job postings
  - Pending moderation items
  - Revenue metrics
  - Dispute queue
- Admin reviews pending disputes via `StatusDisputeController`
- Admin moderates reviews via `AdminCommentModerationController`
- Admin manages recruiter verification requests via `AdminRecruiterUpdateRequestController`
- Admin generates reports

**Services Involved**:
- AdminDashboardService
- StatusDisputeService
- CompanyReviewService
- RecruiterService

---

## 🟢 MEDIUM PRIORITY - Supporting Feature Flows

### 14. Email Notification Sending Flow
**Domain**: Email Services  
**Actors**: System, Email Provider (SMTP)  
**Purpose**: Document email delivery mechanism

**Key Steps**:
- Service triggers email via `EmailService.sendEmail()`
- System loads email template
- System populates template variables
- System connects to SMTP server
- System sends email
- System logs delivery status
- Handles bounce/failure scenarios

**Services Involved**:
- EmailService

---

### 15. File Upload & Storage Flow
**Domain**: Storage + File Services  
**Actors**: User, System, Firebase Storage  
**Purpose**: Document file handling (resumes, evidence, contracts)

**Key Steps**:
- User uploads file via `FileController.uploadFile()`
- System validates file type and size
- System generates unique filename
- System uploads to Firebase Storage via `FirebaseStorageService`
- Firebase returns public URL
- System saves file metadata
- System returns URL to client

**Services Involved**:
- FileService
- FirebaseStorageService

**Supported File Types**:
- PDF, DOCX (resumes, contracts)
- JPG, PNG (evidence files, profile pictures)

---

### 16. Blog Content Management Flow
**Domain**: Blog Services  
**Actors**: Admin/Editor, User, System  
**Purpose**: Document blog post creation and engagement

**Key Steps**:
- Editor creates blog via `BlogController.createBlog()`
- System publishes blog
- Users read blog
- Users rate blog via `BlogRatingController.rateBlog()`
- Users comment via `BlogCommentController.addComment()`
- Admin moderates comments via `AdminCommentController`
- System calculates engagement metrics

**Services Involved**:
- BlogService
- BlogRatingService
- BlogCommentService

---

### 17. Career Coach & Roadmap Flow
**Domain**: Coach Services  
**Actors**: Candidate, AI System  
**Purpose**: Document AI-powered career guidance

**Key Steps**:
- Candidate requests coaching via `CoachController.getCoachAdvice()`
- System analyzes candidate profile
- System generates personalized advice
- System creates career roadmap via `RoadmapController.generateRoadmap()`
- System recommends skills to learn
- System tracks progress

**Services Involved**:
- CoachService
- RoadmapService
- CandidateProfileService

---

### 18. Interview AI Analysis Flow (Gemini Integration)
**Domain**: Interview Services  
**Actors**: Candidate, Recruiter, AI System  
**Purpose**: Document AI-powered interview evaluation

**Key Steps**:
- System records interview session
- System sends transcript to Gemini AI via `TestGeminiController`
- AI analyzes candidate responses
- AI generates feedback report
- AI scores communication skills
- System presents report to recruiter

**Services Involved**:
- InterviewService
- GeminiAIService

---

### 19. Job Feedback Collection Flow
**Domain**: Job Services  
**Actors**: Recruiter, Candidate, System  
**Purpose**: Document recruiter feedback to candidates

**Key Steps**:
- Recruiter provides feedback via `JobFeedbackController.submitFeedback()`
- System associates feedback with JobApply
- System sends notification to candidate
- Candidate views feedback
- System tracks feedback quality metrics

**Services Involved**:
- JobFeedbackService
- NotificationService

---

### 20. Saved Jobs Management Flow
**Domain**: Job Services  
**Actors**: Candidate, System  
**Purpose**: Document job bookmarking feature

**Key Steps**:
- Candidate saves job via `SavedJobController.saveJob()`
- System creates bookmark record
- System sends reminders for saved jobs
- Candidate applies from saved jobs
- System removes after application

**Services Involved**:
- SavedJobService
- NotificationService

---

### 21. Recruiter Profile Management Flow
**Domain**: Recruiter Services  
**Actors**: Recruiter, Admin, System  
**Purpose**: Document recruiter onboarding and verification

**Key Steps**:
- Recruiter registers account
- Recruiter completes company profile via `RecruiterController.updateProfile()`
- Recruiter uploads company documents
- System creates verification request
- Admin reviews via `AdminRecruiterController`
- Admin approves/rejects verification
- System activates recruiter account
- Recruiter gains posting privileges

**Services Involved**:
- RecruiterService
- AdminRecruiterService
- FileStorageService

---

### 22. Candidate Profile Management Flow
**Domain**: Profile Services  
**Actors**: Candidate, System  
**Purpose**: Document candidate profile building

**Key Steps**:
- Candidate updates profile via `CandidateController.updateProfile()`
- Candidate uploads profile picture
- Candidate sets job preferences
- Candidate configures privacy settings
- System calculates profile completeness
- System updates recommendation index

**Services Involved**:
- CandidateProfileService
- FileStorageService
- CandidateRecommendationService

---

### 23. Role & Permission Management Flow
**Domain**: Authentication Services  
**Actors**: Admin, System  
**Purpose**: Document RBAC (Role-Based Access Control)

**Key Steps**:
- Admin creates role via `RoleController.createRole()`
- Admin assigns permissions via `PermissionController.assignPermission()`
- System validates permission hierarchy
- System grants access based on roles
- User actions checked against permissions

**Services Involved**:
- RoleService
- PermissionService
- AuthenticationService

---

### 24. Health Check & Monitoring Flow
**Domain**: Health Services  
**Actors**: System, Admin, Monitoring Tool  
**Purpose**: Document system health monitoring

**Key Steps**:
- Monitoring tool polls `AdminHealthController.getHealthStatus()`
- System checks database connectivity
- System checks Redis cache
- System checks Kafka connection
- System checks Firebase connection
- System returns health report
- Admin alerted on failures

**Services Involved**:
- HealthCheckService

---

### 25. Employment Verification Flow
**Domain**: Job Services  
**Actors**: Recruiter, System  
**Purpose**: Document work history verification

**Key Steps**:
- Recruiter records employment start via `EmploymentVerificationController.createVerification()`
- System tracks employment duration (days_employed)
- System checks probation period status
- System enables review eligibility after 30 days
- System sends review prompt to candidate
- Recruiter records termination when employment ends
- System updates review eligibility

**Services Involved**:
- EmploymentVerificationService
- ReviewEligibilityService
- NotificationService

---

## 📊 Summary Statistics

**Total Sequence Diagrams**: 25

**By Priority**:
- 🔴 Critical: 6 diagrams
- 🟡 High: 7 diagrams  
- 🟢 Medium: 12 diagrams

**By Service Domain**:
- Job Services: 7 diagrams
- Authentication Services: 3 diagrams
- Payment Services: 2 diagrams
- Resume Services: 1 diagram
- Review Services: 1 diagram
- Recommendation Services: 1 diagram
- Notification Services: 1 diagram
- Admin Services: 1 diagram
- Interview Services: 2 diagrams
- Storage Services: 1 diagram
- Blog Services: 1 diagram
- Coach Services: 1 diagram
- Profile Services: 2 diagrams
- Health Services: 1 diagram

**Integration Points**:
- Kafka Event Streaming: 2 diagrams
- Firebase Storage: 2 diagrams
- AI/ML Services: 2 diagrams
- Payment Gateways: 2 diagrams
- Email/SMS: Multiple diagrams

---

## 🔄 Cross-Cutting Concerns

### A. Security & Authorization
- JWT token validation (included in each authenticated flow)
- Role-based access control checks
- Data encryption for sensitive fields

### B. Error Handling
- Validation errors
- Business logic errors
- External service failures
- Retry mechanisms

### C. Logging & Audit Trail
- Status history recording
- Payment transaction logs
- Review moderation logs
- User action tracking

### D. Performance Optimization
- Caching strategies (Redis)
- Database indexing
- Async processing (Kafka)
- Pagination for large datasets

---

## 🎯 Recommended Implementation Order

1. **Phase 1** (Critical Business Flows):
   - Job Application Submission Flow (#1)
   - Interview Scheduling & Management Flow (#2)
   - User Registration & Authentication Flow (#7)

2. **Phase 2** (Core Features):
   - Bilateral Status Verification Flow (#3)
   - Resume Creation & Management Flow (#8)
   - Company Review Submission & Moderation Flow (#6)

3. **Phase 3** (Revenue & Engagement):
   - Payment Processing Flows (#9, #10)
   - Candidate Recommendation & Matching Flow (#4)
   - Job Posting Creation & Management Flow (#11)

4. **Phase 4** (Supporting Systems):
   - Notification Delivery Flow (#12)
   - Employment Contract Lifecycle Flow (#5)
   - Admin Dashboard & Analytics Flow (#13)

5. **Phase 5** (Remaining Features):
   - All Medium Priority flows (#14-#25)

---

## 📝 Notes

- All diagrams should include error handling paths
- Include retry logic for external service calls
- Document timeout configurations
- Show database transaction boundaries
- Indicate async vs synchronous operations
- Mark Kafka event publications
- Highlight notification trigger points

---

## 🔗 Related Documentation

- State Diagrams: [JOB_APPLICATION_STATE_DIAGRAM.drawio](JOB_APPLICATION_STATE_DIAGRAM.drawio), [INTERVIEW_STATE_DIAGRAM.drawio](INTERVIEW_STATE_DIAGRAM.drawio)
- API Documentation: `docs/` folder
- Implementation Gap Analysis: [IMPLEMENTATION_GAP_ANALYSIS.md](docs/IMPLEMENTATION_GAP_ANALYSIS.md)
- Database Migrations: `database/migrations/`

---

**Last Updated**: January 2025  
**Status**: Ready for diagram creation  
**Next Action**: Begin creating sequence diagrams starting with Phase 1 critical flows
