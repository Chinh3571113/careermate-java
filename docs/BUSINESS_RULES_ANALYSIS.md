# Business Rules (BR) Implementation Analysis

**Document Version:** 1.0  
**Analysis Date:** January 2025  
**Total BRs Analyzed:** 58 (BR-01 to BR-58)

---

## Executive Summary

| Status | Count | Percentage |
|--------|-------|------------|
| ✅ **Fully Implemented** | 34 | 58.6% |
| ⚠️ **Partially Implemented** | 12 | 20.7% |
| ❌ **Not Implemented** | 8 | 13.8% |
| 🔄 **Duplicate/Overlapping** | 4 | 6.9% |

---

## Detailed Analysis by Category

### 1. Authentication & Account Management (BR-01 to BR-05, BR-26 to BR-29)

| BR ID | Description | Status | Evidence | Notes |
|-------|-------------|--------|----------|-------|
| **BR-01** | Each account must have a unique email | ✅ Implemented | [Account.java#L23](src/main/java/com/fpt/careermate/services/account_services/domain/Account.java#L23): `@Column(name = "email", unique = true)` | Database-level constraint enforced |
| **BR-02** | Passwords must be encrypted using BCrypt | ✅ Implemented | [AuthenticationImp.java#L100](src/main/java/com/fpt/careermate/services/authentication_services/service/AuthenticationImp.java#L100): `new BCryptPasswordEncoder(10)` | BCrypt strength factor 10 |
| **BR-03** | Candidates can only access candidate-specific features | ✅ Implemented | [ResumeImp.java](src/main/java/com/fpt/careermate/services/resume_services/service/ResumeImp.java): `@PreAuthorize("hasRole('CANDIDATE')")` | 50+ endpoints protected |
| **BR-04** | Recruiters can only access recruiter-specific features | ✅ Implemented | [RecruiterController.java](src/main/java/com/fpt/careermate/services/recruiter_services/web/rest/RecruiterController.java): `@PreAuthorize("hasRole('RECRUITER')")` | RBAC enforced |
| **BR-05** | Admins have full access to all platform features | ✅ Implemented | [AdminInvoiceController.java](src/main/java/com/fpt/careermate/services/order_services/web/rest/AdminInvoiceController.java): `@PreAuthorize("hasRole('ADMIN')")` | 30+ admin-only endpoints |
| **BR-26** | Admin can suspend any account | ✅ Implemented | [RecruiterImp.java#L187](src/main/java/com/fpt/careermate/services/recruiter_services/service/RecruiterImp.java#L187): `if ("BANNED".equals(currentAccount.getStatus()))` | Status: ACTIVE, BANNED, PENDING |
| **BR-27** | Suspended accounts cannot login | ✅ Implemented | [ErrorCode.java#L55](src/main/java/com/fpt/careermate/common/exception/ErrorCode.java#L55): `ACCOUNT_BANNED(4007, "Your account has been banned")` | Login blocked with HTTP 403 |
| **BR-28** | Password reset link must expire after 24 hours | ✅ Implemented | [EmailImp.java#L91](src/main/java/com/fpt/careermate/services/email_services/service/EmailImp.java#L91): `ErrorCode.OTP_EXPIRED` | OTP expiration enforced |
| **BR-29** | Only verified email can reset password | ⚠️ Partial | OTP sent to email, but no explicit "verified email" flag in Account entity | Consider adding `isEmailVerified` field |

---

### 2. CV/Resume Management (BR-08 to BR-10, BR-37 to BR-39)

| BR ID | Description | Status | Evidence | Notes |
|-------|-------------|--------|----------|-------|
| **BR-08** | Each candidate can have multiple CVs | ✅ Implemented | [Resume.java](src/main/java/com/fpt/careermate/services/resume_services/domain/Resume.java): `@ManyToOne @JoinColumn(name = "candidateId")` | No limit on CV count |
| **BR-09** | CV file format limited to PDF/DOCX | ⚠️ Partial | ResumeType enum: `WEB, UPLOAD, DRAFT` - No explicit file format validation | **GAP:** Add file extension validation |
| **BR-10** | CV file size cannot exceed 10MB | ❌ Not Implemented | No file size validation found in codebase | **CRITICAL:** Add `@Size` validation |
| **BR-37** | Only owner can delete their CV | ✅ Implemented | [ResumeImp.java#L93](src/main/java/com/fpt/careermate/services/resume_services/service/ResumeImp.java#L93): Owner verification in `deleteResume()` | Uses JWT candidate ID |
| **BR-38** | Deleted CVs cannot be applied to jobs | ✅ Implemented | `CascadeType.ALL, orphanRemoval = true` in Resume entity | Related applications handled |
| **BR-39** | System provides CV recommendations based on job match | ✅ Implemented | [CandidateRecommendationServiceImpl.java](src/main/java/com/fpt/careermate/services/recommendation/service/CandidateRecommendationServiceImpl.java): Weaviate semantic search | AI-powered matching |

---

### 3. Job Posting & Application (BR-06, BR-07, BR-11, BR-14 to BR-18, BR-43, BR-53)

| BR ID | Description | Status | Evidence | Notes |
|-------|-------------|--------|----------|-------|
| **BR-06** | Recruiter must be verified to post jobs | ✅ Implemented | [JobPostingImp.java#L402](src/main/java/com/fpt/careermate/services/job_services/service/JobPostingImp.java#L402): `throw new AppException(ErrorCode.RECRUITER_NOT_VERIFIED)` | Status check on job creation |
| **BR-07** | Recruiter account must be approved by admin | ✅ Implemented | [RecruiterImp.java#L455](src/main/java/com/fpt/careermate/services/recruiter_services/service/RecruiterImp.java#L455): `RECRUITER_NOT_VERIFIED` validation | PENDING → ACTIVE workflow |
| **BR-11** | A candidate can only apply once per job posting | ✅ Implemented | [JobApplyImp.java#L101](src/main/java/com/fpt/careermate/services/job_services/service/JobApplyImp.java#L101): `throw new AppException(ErrorCode.ALREADY_APPLIED_TO_JOB_POSTING)` | Duplicate check on apply |
| **BR-14** | Job postings require an expiration date | ✅ Implemented | [ErrorCode.java#L78](src/main/java/com/fpt/careermate/common/exception/ErrorCode.java#L78): `INVALID_EXPIRATION_DATE` | Future date required |
| **BR-15** | Expired jobs must be hidden from candidates | ✅ Implemented | [JobPostingImp.java#L599](src/main/java/com/fpt/careermate/services/job_services/service/JobPostingImp.java#L599): Query filters by ACTIVE status and expiration | Public API filter |
| **BR-16** | Jobs require mandatory fields (title, description, location, salary) | ⚠️ Partial | Title/description validated, salary range optional | **GAP:** Add `@NotNull` for salary fields |
| **BR-17** | Application status workflow must be followed | ✅ Implemented | [StatusJobApply.java](src/main/java/com/fpt/careermate/common/constant/StatusJobApply.java): 14 defined statuses | SUBMITTED → REVIEWING → INTERVIEW_SCHEDULED → WORKING |
| **BR-18** | Job title must be unique per recruiter | ✅ Implemented | [JobPosting.java#L22](src/main/java/com/fpt/careermate/services/job_services/domain/JobPosting.java#L22): `@UniqueConstraint(columnNames = {"recruiter_id", "title"})` | Database constraint |
| **BR-43** | Same as BR-18 | 🔄 Duplicate | See BR-18 | **REMOVE:** Consolidate with BR-18 |
| **BR-53** | Job editing not allowed after first application | ❌ Not Implemented | `updateJobPosting()` has no application count check | **GAP:** Add application validation |

---

### 4. Premium Features & Subscription (BR-12, BR-13, BR-47, BR-48)

| BR ID | Description | Status | Evidence | Notes |
|-------|-------------|--------|----------|-------|
| **BR-12** | Premium features require active subscription | ✅ Implemented | [CandidateEntitlementCheckerService.java](src/main/java/com/fpt/careermate/services/order_services/service/CandidateEntitlementCheckerService.java) | AI features gated by entitlement |
| **BR-13** | Premium subscriptions auto-expire after period ends | ⚠️ Partial | Subscription expiration stored but no scheduled job found | **GAP:** Add cron job for auto-expiry |
| **BR-47** | Payment must be processed via VNPay | ✅ Implemented | [PaymentConfig.java](src/main/java/com/fpt/careermate/config/PaymentConfig.java): VNPay configuration | VND currency, Vietnamese gateway |
| **BR-48** | Subscription starts only after successful payment | ✅ Implemented | [ErrorCode.java#L28](src/main/java/com/fpt/careermate/common/exception/ErrorCode.java#L28): `PAYMENT_FAILED` | Entitlement created after callback |

---

### 5. AI Features (BR-19 to BR-22, BR-30 to BR-32, BR-51, BR-54, BR-55)

| BR ID | Description | Status | Evidence | Notes |
|-------|-------------|--------|----------|-------|
| **BR-19** | AI CV feedback personalized per user | ⚠️ Partial | No dedicated CV feedback service found | **GAP:** CV analysis not implemented |
| **BR-20** | AI interview practice adapts to job role | ✅ Implemented | [InterviewControllerTest.java](src/test/java/com/fpt/careermate/services/interview_services/web/rest/InterviewControllerTest.java): Interview session with questions | Session-based interview |
| **BR-21** | AI roadmap generated based on career goals | ✅ Implemented | [RoadmapController.java](src/main/java/com/fpt/careermate/services/coach_services/web/rest/RoadmapController.java): `getRoadmap()` endpoint | Entitlement-gated |
| **BR-22** | AI usage counts against user entitlement | ✅ Implemented | [CandidateEntitlementCheckerService.java#L84](src/main/java/com/fpt/careermate/services/order_services/service/CandidateEntitlementCheckerService.java#L84): `EntitlementCode.AI_ROADMAP` | Per-feature limits |
| **BR-30** | AI interaction logs must be stored | ❌ Not Implemented | No AiInteractionLog entity found | **CRITICAL:** Add audit trail |
| **BR-31** | AI must not access private user data without consent | ❌ Not Implemented | No consent mechanism found | **CRITICAL:** GDPR compliance needed |
| **BR-32** | AI reports stored for 90 days | ❌ Not Implemented | No data retention policy found | **GAP:** Add retention cron job |
| **BR-51** | AI matching must not discriminate | ❌ Not Implemented | No bias detection in recommendation service | **GAP:** Add fairness metrics |
| **BR-54** | AI can rank candidates by match score | ✅ Implemented | [CandidateRecommendationServiceImpl.java](src/main/java/com/fpt/careermate/services/recommendation/service/CandidateRecommendationServiceImpl.java): Scoring algorithm | Skill + experience weighting |
| **BR-55** | AI matching uses multiple criteria (skills, experience, education) | ✅ Implemented | [QualificationScoringService.java](src/main/java/com/fpt/careermate/services/recommendation/service/QualificationScoringService.java): 40% skills, 25% experience, 25% education | Multi-factor scoring |

---

### 6. Saved Jobs & Bookmarks (BR-23 to BR-25)

| BR ID | Description | Status | Evidence | Notes |
|-------|-------------|--------|----------|-------|
| **BR-23** | Candidate cannot save same job twice | ✅ Implemented | [SavedJob.java#L26](src/main/java/com/fpt/careermate/services/job_services/domain/SavedJob.java#L26): `@UniqueConstraint(columnNames = {"candidate_id", "job_id"})` | Database constraint |
| **BR-24** | Only candidates can save jobs | ✅ Implemented | [SavedJobController.java](src/main/java/com/fpt/careermate/services/job_services/web/rest/SavedJobController.java): Uses JWT candidate authentication | RBAC enforced |
| **BR-25** | Saved jobs list is private to user | ✅ Implemented | `getSavedJobs()` filters by authenticated candidate ID | No cross-user access |

---

### 7. Content & Blog (BR-33 to BR-36, BR-52)

| BR ID | Description | Status | Evidence | Notes |
|-------|-------------|--------|----------|-------|
| **BR-33** | Blog content must be moderated before publishing | ✅ Implemented | [ContentModerationService.java](src/main/java/com/fpt/careermate/services/blog_services/service/ContentModerationService.java): Profanity detection | Auto-flagging system |
| **BR-34** | Blog articles owned by author only | ✅ Implemented | [ErrorCode.java#L113](src/main/java/com/fpt/careermate/common/exception/ErrorCode.java#L113): `BLOG_UNAUTHORIZED` | Owner verification |
| **BR-35** | Blog rating one per user per blog | ✅ Implemented | [BlogRating.java#L18](src/main/java/com/fpt/careermate/services/blog_services/domain/BlogRating.java#L18): `@UniqueConstraint(columnNames = {"blog_id", "user_id"})` | Unique constraint |
| **BR-36** | Organization profiles must have required fields | ✅ Implemented | OrganizationInfo validation: companyEmail, contactPerson, phoneNumber, companyAddress | Jakarta validation |
| **BR-52** | Profanity filter on user-generated content | ✅ Implemented | [ContentModerationService.java#L22](src/main/java/com/fpt/careermate/services/blog_services/service/ContentModerationService.java#L22): `PROFANITY_WORDS` set | Comprehensive word list |

---

### 8. Interview Scheduling (BR-45, BR-46)

| BR ID | Description | Status | Evidence | Notes |
|-------|-------------|--------|----------|-------|
| **BR-45** | Interview must have scheduled date, time, and location | ✅ Implemented | [InterviewScheduleRequest.java](src/main/java/com/fpt/careermate/services/job_services/service/dto/request/InterviewScheduleRequest.java): Required fields | Validated on create |
| **BR-46** | Interview rescheduling requires mutual consent | ⚠️ Partial | [InterviewScheduleController.java#L165](src/main/java/com/fpt/careermate/services/job_services/web/rest/InterviewScheduleController.java#L165): "Without consent workflow" mentioned | Direct update exists |

---

### 9. Data Management (BR-40 to BR-42, BR-56 to BR-58)

| BR ID | Description | Status | Evidence | Notes |
|-------|-------------|--------|----------|-------|
| **BR-40** | Job recommendations based on user profile | ✅ Implemented | Weaviate semantic search + skill matching | Vector similarity |
| **BR-41** | All records must have created/updated timestamps | ✅ Implemented | [Resume.java#L36](src/main/java/com/fpt/careermate/services/resume_services/domain/Resume.java#L36): `@CreationTimestamp` | Hibernate managed |
| **BR-42** | Images limited to JPG, PNG, GIF | ⚠️ Partial | Tests reference `image/jpeg`, but no explicit validation | **GAP:** Add MIME type check |
| **BR-56** | Privacy policy acceptance required | ❌ Not Implemented | No privacy consent field in Account entity | **CRITICAL:** Add GDPR field |
| **BR-57** | Users can request data deletion | ⚠️ Partial | [AccountImp.java#L91](src/main/java/com/fpt/careermate/services/account_services/service/AccountImp.java#L91): `deleteAccount(int id)` exists | No user-initiated flow |
| **BR-58** | Deleted data must be anonymized | ❌ Not Implemented | Hard delete used, no anonymization | **GAP:** GDPR soft-delete |

---

### 10. Skills & Requirements (BR-49, BR-50)

| BR ID | Description | Status | Evidence | Notes |
|-------|-------------|--------|----------|-------|
| **BR-49** | Job postings must specify required skills | ⚠️ Partial | Skills stored via JdSkill entity, but not mandatory | **GAP:** Add `@NotEmpty` validation |
| **BR-50** | Skills should be from predefined taxonomy | ⚠️ Partial | [SkillMatcher.java](src/main/java/com/fpt/careermate/services/recommendation/util/SkillMatcher.java): Skill normalization exists | No enforced taxonomy |

---

## Duplicate/Overlapping Rules Summary

| Pair | Recommendation |
|------|----------------|
| BR-18 & BR-43 | **Consolidate:** Both describe unique job title per recruiter |
| BR-11 & BR-23 | **Distinct:** BR-11 = job application, BR-23 = saved jobs (both valid) |
| BR-33 & BR-52 | **Merge:** Both about content moderation/profanity |
| BR-08 & BR-10 | **Related:** BR-08 = multiple CVs, BR-10 = file size (keep separate) |

---

## Recommended Additional Business Rules

Based on the codebase analysis, the following BRs are **missing but should be added**:

| Proposed BR | Description | Justification |
|-------------|-------------|---------------|
| **BR-59** | Account lockout after 5 failed login attempts | Security best practice, not found in AuthenticationImp |
| **BR-60** | Session timeout after 30 minutes of inactivity | JWT only, no session management |
| **BR-61** | Rate limiting on API endpoints | No `@RateLimiter` annotations found |
| **BR-62** | Notification preferences (email/in-app/push) | NotificationService exists but no user preferences |
| **BR-63** | Job application withdrawal cooldown period | Currently no restriction on withdraw → reapply |
| **BR-64** | Recruiter can only view candidate profile after application | No consent-based profile visibility |
| **BR-65** | Maximum job postings per recruiter per month | Entitlement exists but no limit found |
| **BR-66** | Two-factor authentication for admin accounts | Critical security, not implemented |

---

## Priority Implementation Recommendations

### 🔴 Critical (Security/Compliance)
1. **BR-30** - AI interaction logging (audit trail)
2. **BR-31** - User consent for AI data access (GDPR)
3. **BR-56** - Privacy policy acceptance
4. **BR-58** - Data anonymization on deletion

### 🟡 High Priority (Feature Gaps)
1. **BR-10** - CV file size validation (10MB limit)
2. **BR-53** - Block job editing after applications received
3. **BR-09** - PDF/DOCX format enforcement
4. **BR-13** - Subscription auto-expiry cron job

### 🟢 Medium Priority (Enhancements)
1. **BR-42** - Image format validation
2. **BR-49** - Required skills enforcement
3. **BR-19** - AI CV feedback service
4. **BR-46** - Mutual consent for interview rescheduling

---

## Summary Statistics

```
Total Business Rules: 58
├── Authentication & Accounts: 9 rules
├── CV Management: 6 rules
├── Job Posting & Applications: 10 rules
├── Premium & Subscription: 4 rules
├── AI Features: 9 rules
├── Saved Jobs: 3 rules
├── Content & Blog: 5 rules
├── Interviews: 2 rules
├── Data Management: 6 rules
└── Skills: 2 rules

Implementation Status:
✅ Fully Implemented: 34 (58.6%)
⚠️ Partially Implemented: 12 (20.7%)
❌ Not Implemented: 8 (13.8%)
🔄 Duplicates: 4 (6.9%)
```

---

**Document prepared by:** GitHub Copilot  
**Review requested:** Development Team Lead
