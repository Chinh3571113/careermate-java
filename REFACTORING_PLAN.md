# Architectural Refactoring Plan: Breaking Up job_services

## 🎯 Executive Summary

**Problem:** The `job_services` package is monolithic, containing 5 distinct domains that should be separated.

**Solution:** Extract 3 new service packages while keeping `job_services` focused on core job management.

**Timeline:** 5 days (1 week)

**Risk Level:** Medium (manageable with proper testing)

---

## 📊 Current vs. Proposed Architecture

### Current Structure (Monolithic ⚠️)

```
services/
└── job_services/  ⚠️ 5 DOMAINS MIXED TOGETHER
    ├── domain/
    │   ├── JobApply.java                    [Core Job]
    │   ├── JobPosting.java                  [Core Job]
    │   ├── JobFeedback.java                 [Core Job]
    │   ├── SavedJob.java                    [Core Job]
    │   ├── InterviewSchedule.java           [Interview Domain] ❌
    │   ├── InterviewRescheduleRequest.java  [Interview Domain] ❌
    │   ├── EmploymentVerification.java      [Employment Domain] ❌
    │   ├── StatusUpdateRequest.java         [Verification Domain] ❌
    │   ├── StatusDispute.java               [Dispute Domain] ❌
    │   └── EvidenceFile.java                [Support Domain] ❌
    │
    ├── repository/
    │   ├── JobApplyRepo
    │   ├── InterviewScheduleRepo            ❌ Should move
    │   ├── StatusUpdateRequestRepo          ❌ Should move
    │   └── ...
    │
    ├── service/
    │   ├── JobApplyImp                      ✅ Stays
    │   ├── StatusUpdateServiceImpl          ❌ Should move
    │   ├── DisputeResolutionServiceImpl     ❌ Should move
    │   └── impl/
    │       ├── InterviewScheduleService     ❌ Should move
    │       └── EmploymentContractService    ❌ Should move
    │
    └── web/rest/
        ├── JobApplyController               ✅ Stays
        ├── InterviewScheduleController      ❌ Should move
        └── StatusUpdateController           ❌ Should move
```

**Domain Count:** 5 domains crammed into 1 package

**Lines of Code:** ~15,000+ lines in one package

**Team Impact:** Merge conflicts, unclear ownership, navigation difficulty

---

### Proposed Structure (Domain-Driven ✅)

```
services/
│
├── job_services/              ✅ CORE JOB DOMAIN ONLY
│   ├── domain/
│   │   ├── JobApply.java
│   │   ├── JobPosting.java
│   │   ├── JobFeedback.java
│   │   ├── SavedJob.java
│   │   └── JobApplyStatusHistory.java
│   ├── repository/
│   │   ├── JobApplyRepo
│   │   ├── JobPostingRepo
│   │   ├── JobFeedbackRepo
│   │   └── SavedJobRepo
│   ├── service/
│   │   ├── JobApplyService (interface)
│   │   ├── JobPostingService (interface)
│   │   └── impl/
│   │       ├── JobApplyServiceImpl
│   │       └── JobPostingServiceImpl
│   └── web/rest/
│       ├── JobApplyController
│       └── JobPostingController
│
├── interview_services/        ✅ NEW - INTERVIEW DOMAIN
│   ├── domain/
│   │   ├── InterviewSchedule.java
│   │   └── InterviewRescheduleRequest.java
│   ├── repository/
│   │   ├── InterviewScheduleRepo
│   │   └── InterviewRescheduleRequestRepo
│   ├── service/
│   │   ├── InterviewScheduleService (interface)
│   │   └── impl/
│   │       └── InterviewScheduleServiceImpl (CREATE THIS)
│   ├── scheduler/
│   │   └── InterviewReminderScheduler
│   └── web/rest/
│       └── InterviewScheduleController
│
├── employment_services/       ✅ NEW - EMPLOYMENT DOMAIN
│   ├── domain/
│   │   └── EmploymentVerification.java
│   ├── repository/
│   │   └── EmploymentVerificationRepo
│   ├── service/
│   │   ├── EmploymentVerificationService (interface)
│   │   └── impl/
│   │       └── EmploymentVerificationServiceImpl (CREATE THIS)
│   ├── scheduler/
│   │   └── ContractExpirationScheduler
│   └── (no controller needed - internal service)
│
├── verification_services/     ✅ NEW - STATUS VERIFICATION DOMAIN
│   ├── domain/
│   │   ├── StatusUpdateRequest.java
│   │   ├── StatusDispute.java
│   │   └── EvidenceFile.java
│   ├── repository/
│   │   ├── StatusUpdateRequestRepo
│   │   ├── StatusDisputeRepo
│   │   └── EvidenceFileRepo
│   ├── service/
│   │   ├── StatusUpdateService (interface)
│   │   ├── DisputeResolutionService (interface)
│   │   └── impl/
│   │       ├── StatusUpdateServiceImpl
│   │       └── DisputeResolutionServiceImpl
│   ├── scheduler/
│   │   ├── StatusVerificationScheduler
│   │   └── DisputePriorityScheduler
│   └── web/rest/
│       └── StatusUpdateController
│
└── review_services/           ✅ ALREADY GOOD - REVIEW DOMAIN
    ├── domain/
    │   └── CompanyReview.java
    ├── repository/
    │   └── CompanyReviewRepo
    ├── service/
    │   ├── ReviewEligibilityService
    │   ├── CompanyReviewService (interface)
    │   └── impl/
    │       └── CompanyReviewServiceImpl (CREATE THIS)
    ├── constant/
    │   ├── ReviewType
    │   ├── CandidateQualification
    │   └── ReviewStatus
    └── web/rest/
        └── CompanyReviewController (CREATE THIS)
```

**Domain Count:** 5 domains in 5 separate packages ✅

**Lines of Code per Package:** ~3,000 lines each (manageable)

**Team Impact:** Clear ownership, parallel development, no conflicts

---

## 📋 Migration Checklist

### Phase 1: Preparation (Day 1 Morning)

#### 1.1 Create Backup
```bash
git checkout -b backup-before-refactor
git push origin backup-before-refactor
```

#### 1.2 Document Current State
```bash
# Run all tests and save output
./mvnw test > test-results-before.txt

# List all API endpoints
curl http://localhost:8080/swagger-ui.html > api-endpoints-before.html

# Take snapshot of package structure
tree src/main/java/com/fpt/careermate/services > structure-before.txt
```

#### 1.3 Create New Package Directories
```bash
cd src/main/java/com/fpt/careermate/services/

# Create interview_services structure
mkdir -p interview_services/{domain,repository,service/impl,scheduler,web/rest,dto}

# Create employment_services structure
mkdir -p employment_services/{domain,repository,service/impl,scheduler,dto}

# Create verification_services structure
mkdir -p verification_services/{domain,repository,service/impl,scheduler,web/rest,dto}
```

---

### Phase 2: Move Interview Services (Day 1 Afternoon)

#### 2.1 Move Domain Entities
```bash
cd job_services/domain/
git mv InterviewSchedule.java ../../interview_services/domain/
git mv InterviewRescheduleRequest.java ../../interview_services/domain/
```

**Update package declarations:**
```java
// In InterviewSchedule.java
// OLD:
package com.fpt.careermate.services.job_services.domain;

// NEW:
package com.fpt.careermate.services.interview_services.domain;
```

**Update imports in other files:**
```bash
# Use IDE's "Find and Replace in Files"
# Find: com.fpt.careermate.services.job_services.domain.InterviewSchedule
# Replace: com.fpt.careermate.services.interview_services.domain.InterviewSchedule
```

#### 2.2 Move Repositories
```bash
cd job_services/repository/
git mv InterviewScheduleRepo.java ../../interview_services/repository/
git mv InterviewRescheduleRequestRepo.java ../../interview_services/repository/
```

#### 2.3 Move Services
```bash
cd job_services/service/impl/
git mv InterviewScheduleService.java ../../../interview_services/service/

# CREATE implementation (doesn't exist yet)
# Create: interview_services/service/impl/InterviewScheduleServiceImpl.java
```

#### 2.4 Move Controllers
```bash
cd job_services/web/rest/
git mv InterviewScheduleController.java ../../../interview_services/web/rest/
```

#### 2.5 Move Schedulers
```bash
cd job_services/service/scheduler/
git mv InterviewReminderScheduler.java ../../../interview_services/scheduler/
```

#### 2.6 Test Interview Services
```bash
# Compile only interview_services package
./mvnw compile -pl interview_services

# Run tests
./mvnw test -Dtest=Interview*
```

---

### Phase 3: Move Employment Services (Day 2 Morning)

#### 3.1 Move Domain
```bash
cd job_services/domain/
git mv EmploymentVerification.java ../../employment_services/domain/
```

#### 3.2 Move Repository
```bash
cd job_services/repository/
git mv EmploymentVerificationRepo.java ../../employment_services/repository/
```

#### 3.3 Move Service
```bash
cd job_services/service/impl/
git mv EmploymentContractService.java ../../../employment_services/service/

# Rename to EmploymentVerificationService (consistent naming)
cd ../../../employment_services/service/
git mv EmploymentContractService.java EmploymentVerificationService.java

# CREATE implementation
# Create: employment_services/service/impl/EmploymentVerificationServiceImpl.java
```

#### 3.4 Move Scheduler
```bash
cd job_services/service/scheduler/
git mv ContractExpirationScheduler.java ../../../employment_services/scheduler/
```

#### 3.5 Update References in JobApply
```java
// In JobApply.java
// OLD:
import com.fpt.careermate.services.job_services.domain.EmploymentVerification;

// NEW:
import com.fpt.careermate.services.employment_services.domain.EmploymentVerification;
```

#### 3.6 Test Employment Services
```bash
./mvnw compile -pl employment_services
./mvnw test -Dtest=Employment*
```

---

### Phase 4: Move Verification Services (Day 2 Afternoon)

#### 4.1 Move Domains
```bash
cd job_services/domain/
git mv StatusUpdateRequest.java ../../verification_services/domain/
git mv StatusDispute.java ../../verification_services/domain/
git mv EvidenceFile.java ../../verification_services/domain/
```

#### 4.2 Move Repositories
```bash
cd job_services/repository/
git mv StatusUpdateRequestRepo.java ../../verification_services/repository/
git mv StatusDisputeRepo.java ../../verification_services/repository/
git mv EvidenceFileRepo.java ../../verification_services/repository/
```

#### 4.3 Move Services
```bash
cd job_services/service/
git mv StatusUpdateServiceImpl.java ../../verification_services/service/impl/
git mv DisputeResolutionServiceImpl.java ../../verification_services/service/impl/

cd impl/
git mv StatusUpdateService.java ../../../verification_services/service/
git mv DisputeResolutionService.java ../../../verification_services/service/
```

#### 4.4 Move Controller
```bash
cd job_services/web/rest/
git mv StatusUpdateController.java ../../../verification_services/web/rest/
```

#### 4.5 Move Schedulers
```bash
cd job_services/service/scheduler/
git mv StatusVerificationScheduler.java ../../../verification_services/scheduler/
git mv DisputePriorityScheduler.java ../../../verification_services/scheduler/
```

#### 4.6 Test Verification Services
```bash
./mvnw compile -pl verification_services
./mvnw test -Dtest=StatusUpdate*,Dispute*
```

---

### Phase 5: Update Cross-Package References (Day 3)

#### 5.1 Update JobApply Entity

**JobApply currently references:**
- `InterviewSchedule` → Update to `interview_services` package
- `EmploymentVerification` → Update to `employment_services` package

```java
// In JobApply.java

// OLD imports:
import com.fpt.careermate.services.job_services.domain.InterviewSchedule;
import com.fpt.careermate.services.job_services.domain.EmploymentVerification;

// NEW imports:
import com.fpt.careermate.services.interview_services.domain.InterviewSchedule;
import com.fpt.careermate.services.employment_services.domain.EmploymentVerification;
```

#### 5.2 Update Service Dependencies

**Example: ReviewEligibilityService needs JobApply**

```java
// In ReviewEligibilityService.java

// OLD:
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;

// NEW (no change - JobApply stays in job_services):
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
```

#### 5.3 Update Spring Configurations

**If any configuration classes reference services:**

```java
// In SecurityConfig.java or similar

// OLD:
import com.fpt.careermate.services.job_services.service.StatusUpdateServiceImpl;

// NEW:
import com.fpt.careermate.services.verification_services.service.impl.StatusUpdateServiceImpl;
```

#### 5.4 Global Import Update

**Use IDE's "Find and Replace in Files":**

| Find | Replace |
|------|---------|
| `job_services.domain.InterviewSchedule` | `interview_services.domain.InterviewSchedule` |
| `job_services.domain.InterviewRescheduleRequest` | `interview_services.domain.InterviewRescheduleRequest` |
| `job_services.domain.EmploymentVerification` | `employment_services.domain.EmploymentVerification` |
| `job_services.domain.StatusUpdateRequest` | `verification_services.domain.StatusUpdateRequest` |
| `job_services.domain.StatusDispute` | `verification_services.domain.StatusDispute` |
| `job_services.domain.EvidenceFile` | `verification_services.domain.EvidenceFile` |
| `job_services.repository.InterviewScheduleRepo` | `interview_services.repository.InterviewScheduleRepo` |
| `job_services.service.impl.StatusUpdateService` | `verification_services.service.StatusUpdateService` |
| `job_services.service.StatusUpdateServiceImpl` | `verification_services.service.impl.StatusUpdateServiceImpl` |

**Total Estimated Updates:** ~100-150 import statements

---

### Phase 6: Test Everything (Day 4)

#### 6.1 Compilation Test
```bash
# Clean and compile entire project
./mvnw clean compile

# Should have ZERO errors
```

#### 6.2 Unit Tests
```bash
# Run all tests
./mvnw test

# Compare with baseline
diff test-results-before.txt test-results-after.txt

# All tests should still pass
```

#### 6.3 Integration Tests
```bash
# Start application
./mvnw spring-boot:run

# Test API endpoints
curl http://localhost:8080/api/interviews/schedule
curl http://localhost:8080/api/status-updates/pending
curl http://localhost:8080/api/reviews/eligibility/123

# Verify responses match expected
```

#### 6.4 Database Migration Test
```bash
# Reset database
./mvnw flyway:clean
./mvnw flyway:migrate

# Should apply all migrations successfully
```

---

### Phase 7: Create Missing Implementations (Day 5)

#### 7.1 Create InterviewScheduleServiceImpl
```java
package com.fpt.careermate.services.interview_services.service.impl;

@Service
@RequiredArgsConstructor
public class InterviewScheduleServiceImpl implements InterviewScheduleService {
    
    private final InterviewScheduleRepo interviewRepo;
    private final JobApplyRepo jobApplyRepo;
    private final NotificationService notificationService;
    
    @Override
    @Transactional
    public InterviewScheduleResponse scheduleInterview(
        Integer jobApplyId, 
        InterviewScheduleRequest request
    ) {
        // Implementation here (~800 lines total for all methods)
    }
    
    // ... 13 more methods
}
```

#### 7.2 Create EmploymentVerificationServiceImpl
```java
package com.fpt.careermate.services.employment_services.service.impl;

@Service
@RequiredArgsConstructor
public class EmploymentVerificationServiceImpl implements EmploymentVerificationService {
    
    private final EmploymentVerificationRepo verificationRepo;
    private final JobApplyRepo jobApplyRepo;
    
    @Override
    @Transactional
    public EmploymentVerificationResponse createVerification(
        Integer jobApplyId,
        EmploymentVerificationRequest request
    ) {
        // Implementation here (~500 lines total)
    }
    
    // ... more methods
}
```

#### 7.3 Create CompanyReviewServiceImpl
```java
package com.fpt.careermate.services.review_services.service.impl;

@Service
@RequiredArgsConstructor
public class CompanyReviewServiceImpl implements CompanyReviewService {
    
    private final CompanyReviewRepo reviewRepo;
    private final ReviewEligibilityService eligibilityService;
    private final JobApplyRepo jobApplyRepo;
    
    @Override
    @Transactional
    public CompanyReviewResponse submitReview(
        CompanyReviewRequest request,
        Integer candidateId
    ) {
        // Implementation here (~600 lines total)
    }
    
    // ... more methods
}
```

---

## 🎯 Success Criteria

### Refactoring Complete When:

✅ **Code Structure**
- [ ] 4 service packages exist (job, interview, employment, verification)
- [ ] Each package has clear single responsibility
- [ ] No circular dependencies between packages

✅ **Compilation**
- [ ] `./mvnw clean compile` succeeds with 0 errors
- [ ] No import errors
- [ ] No missing class errors

✅ **Tests**
- [ ] All existing tests pass
- [ ] No test failures introduced by refactoring
- [ ] Test coverage maintained or improved

✅ **Runtime**
- [ ] Application starts successfully
- [ ] All API endpoints accessible
- [ ] Database migrations work
- [ ] No bean definition errors

✅ **Service Implementations**
- [ ] `InterviewScheduleServiceImpl` created and working
- [ ] `EmploymentVerificationServiceImpl` created and working
- [ ] `CompanyReviewServiceImpl` created and working
- [ ] All 9 TODO placeholders replaced

---

## ⚠️ Risk Mitigation

### Potential Risks & Solutions

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Breaking existing functionality** | HIGH | Run tests after each migration step |
| **Import path errors** | MEDIUM | Use IDE's automated refactoring tools |
| **Circular dependencies** | MEDIUM | Keep shared code in `job_services` |
| **Database migration issues** | HIGH | Test migrations on fresh DB |
| **Spring bean not found** | MEDIUM | Verify `@Service` annotations present |
| **API contract changes** | HIGH | Keep endpoint paths unchanged |

### Rollback Plan

**If refactoring fails:**
1. Restore from backup branch: `git checkout backup-before-refactor`
2. Cherry-pick successful changes: `git cherry-pick <commit-hash>`
3. Address issues one domain at a time
4. Don't deploy until all tests pass

---

## 📊 Estimated Effort

| Phase | Task | Time | Complexity |
|-------|------|------|------------|
| **1** | Preparation | 2 hours | Low |
| **2** | Move Interview Services | 4 hours | Medium |
| **3** | Move Employment Services | 3 hours | Medium |
| **4** | Move Verification Services | 4 hours | Medium |
| **5** | Update Cross-References | 6 hours | High |
| **6** | Test Everything | 4 hours | Medium |
| **7** | Create Implementations | 8 hours | High |
| | **TOTAL** | **31 hours** (~5 days) | |

**Breakdown:**
- **Refactoring Only:** 23 hours (3 days)
- **Service Implementations:** 8 hours (1 day)
- **Testing & Bug Fixes:** Buffer 1 day

---

## 🎓 Key Principles

### Domain-Driven Design

**Each package represents a bounded context:**
- `job_services` → Job posting and application management
- `interview_services` → Interview scheduling and tracking
- `employment_services` → Employment verification and tracking
- `verification_services` → Status updates and dispute resolution
- `review_services` → Company review and eligibility

### Dependency Rules

**Allowed Dependencies:**
- ✅ All services can depend on `job_services` (core domain)
- ✅ Services can use common utilities
- ✅ Controllers can call services in same package
- ✅ Services can publish events to other domains

**Forbidden Dependencies:**
- ❌ No circular dependencies between service packages
- ❌ `job_services` should NOT depend on specialized services
- ❌ Direct database access from controllers

### Package Organization

**Standard structure for each service package:**
```
<service_name>_services/
├── domain/          ← Entities (JPA models)
├── repository/      ← Data access (Spring Data repos)
├── service/         ← Business logic interfaces
│   └── impl/        ← Business logic implementations
├── scheduler/       ← Scheduled jobs (if any)
├── web/rest/        ← REST controllers (if public API)
├── dto/             ← Data transfer objects
│   ├── request/     ← API request DTOs
│   └── response/    ← API response DTOs
└── constant/        ← Enums and constants
```

---

## 📝 Post-Refactoring Tasks

### 1. Update Documentation
- [ ] Update README with new package structure
- [ ] Update architecture diagrams
- [ ] Document service dependencies
- [ ] Update API documentation

### 2. Team Communication
- [ ] Announce package structure changes
- [ ] Update development guidelines
- [ ] Share import path mapping document
- [ ] Conduct code walkthrough

### 3. CI/CD Updates
- [ ] Update build scripts if package-specific builds exist
- [ ] Update code coverage configuration
- [ ] Update static analysis rules
- [ ] Update deployment scripts

### 4. Monitoring
- [ ] Add service-specific metrics
- [ ] Update log aggregation queries
- [ ] Add domain-specific dashboards
- [ ] Set up alerts for each service

---

## ✅ Final Checklist Before Merge

**Before creating Pull Request:**
- [ ] All compilation errors fixed
- [ ] All tests passing (100% pass rate)
- [ ] No TODO comments in production code
- [ ] API endpoints tested with Postman
- [ ] Database migrations verified on clean DB
- [ ] Code review checklist completed
- [ ] Documentation updated
- [ ] Changelog updated with breaking changes
- [ ] Team notified of refactoring completion

**PR Description Should Include:**
- Summary of changes (4 new packages created)
- Migration steps performed
- Test results (before/after comparison)
- Breaking changes (if any)
- Rollback instructions

---

**Next Action:** Start with Phase 1 (Preparation) tomorrow morning. Allocate full week for refactoring.
