# CareerMate Entity Table - Updated & Complete

## Comparison: Old Entity List vs Current Project vs ERD

### ✅ LEGEND
- 🟢 **EXISTS** - Entity exists in project and ERD
- 🟡 **RENAMED** - Entity renamed or merged
- 🔴 **MISSING FROM ERD** - Entity exists in project but not in ERD
- ⚪ **REMOVED** - Entity no longer exists
- 🔵 **NEW** - New entity added to project

---

## Updated Entity Table (53 Entities Total)

| # | Entity Name (Table) | Status | Description | Old # |
|---|---------------------|--------|-------------|-------|
| **AUTHENTICATION & ACCESS CONTROL** |||||
| 1 | `account` | 🟢 EXISTS | Stores user account info: email, password, status | #1 |
| 2 | `account_role` | 🟢 EXISTS | Junction table: maps accounts to roles (M:N) | #2 |
| 3 | `role` | 🟢 EXISTS | System roles: ADMIN, RECRUITER, CANDIDATE | #3 |
| 4 | `role_permissions` | 🟢 EXISTS | Junction table: assigns permissions to roles (M:N) | #4 |
| 5 | `permission` | 🟢 EXISTS | System actions/access rights (CREATE_JOB, VIEW_RESUME, etc.) | #5 |
| 6 | `forgot_password` | 🔵 NEW | Stores password reset tokens and expiry | - |
| 7 | `invalid_token` | 🔵 NEW | Blacklisted JWT tokens for logout | - |
| **USER PROFILES** |||||
| 8 | `candidate` | 🟡 RENAMED | Candidate profile: DOB, phone, gender, job level, experience | #6 (was `candidateInfo`) |
| 9 | `recruiter` | 🟡 RENAMED | Recruiter/company profile: logo, website, rating, description | #7 (was `recruiterInfo`) |
| 10 | `admin` | 🟡 RENAMED | Admin user details for system management | #8 (was `adminInfo`) |
| 11 | `recruiter_profile_update_requests` | 🔵 NEW | Tracks recruiter profile update requests pending admin approval | - |
| **RESUME & CV** |||||
| 12 | `resume` | 🟢 EXISTS | Candidate resumes: summary, file URL, primary flag | #9 |
| 13 | `education` | 🟢 EXISTS | Education records: school, degree, major, GPA | #10 |
| 14 | `work_experience` | 🟢 EXISTS | Past work experiences: company, position, duration | #11 |
| 15 | `skill` | 🟢 EXISTS | Candidate skills with experience level | #12 |
| 16 | `foreign_language` | 🟢 EXISTS | Languages known with proficiency level | #13 |
| 17 | `highlight_project` | 🟢 EXISTS | Portfolio projects linked to resumes | #14 |
| 18 | `certificate` | 🟢 EXISTS | Certifications and credentials | #15 |
| 19 | `award` | 🟢 EXISTS | Awards and recognitions | #16 |
| **JOB POSTING & APPLICATION** |||||
| 20 | `job_posting` | 🟢 EXISTS | Job listings: title, location, salary, requirements | #17 |
| 21 | `job_description` | 🟢 EXISTS | Detailed job info: responsibilities, benefits | #18 |
| 22 | `jd_skill` | 🔵 NEW | Skills required/optional for job descriptions | - |
| 23 | `job_apply` | 🔵 NEW | Job applications: candidate, job, status, applied date | - |
| 24 | `saved_job` | 🔵 NEW | Bookmarked jobs by candidates | - |
| 25 | `job_feedback` | 🔵 NEW | Recruiter feedback to candidates on applications | - |
| **CANDIDATE PREFERENCES** |||||
| 26 | `industry_experiences` | 🟡 RENAMED | Industry preferences of candidates | #19 (was `industry_experience`) |
| 27 | `work_model` | 🟢 EXISTS | Working model preferences: onsite, remote, hybrid | #20 |
| 28 | `work_location_preferred` | 🟢 EXISTS | Preferred job locations | #21 |
| **INTERVIEW MANAGEMENT** |||||
| 29 | `interview_schedule` | 🔵 NEW | Scheduled interviews: date, time, location, status | - |
| 30 | `interview_session` | 🔵 NEW | AI interview practice sessions for candidates | - |
| 31 | `interview_question` | 🔵 NEW | Questions used in AI interview sessions | - |
| **EMPLOYMENT & VERIFICATION** |||||
| 32 | `employment_verification` | 🔵 NEW | Tracks employment status, duration, termination | - |
| **NOTIFICATION** |||||
| 33 | `notifications` | 🟢 EXISTS | Notifications sent to users (job alerts, updates) | #22 |
| 34 | `device_tokens` | 🔵 NEW | Mobile device tokens for push notifications | - |
| 35 | `notification_heartbeat` | 🔵 NEW | Health monitoring for notification service | - |
| **PAYMENT & SUBSCRIPTION** |||||
| 36 | `recruiter_package` | 🔵 NEW | Subscription packages for recruiters | #23 (split) |
| 37 | `candidate_package` | 🔵 NEW | Subscription packages for candidates | #23 (split) |
| 38 | `recruiter_invoice` | 🔵 NEW | Payment invoices for recruiters | - |
| 39 | `candidate_invoice` | 🔵 NEW | Payment invoices for candidates | - |
| 40 | `recruiter_entitlement` | 🔵 NEW | Active entitlements/credits for recruiters | - |
| 41 | `candidate_entitlement` | 🔵 NEW | Active entitlements/credits for candidates | - |
| 42 | `recruiter_entitlement_package` | 🔵 NEW | Junction: links entitlements to packages (recruiter) | - |
| 43 | `candidate_entitlement_package` | 🔵 NEW | Junction: links entitlements to packages (candidate) | - |
| **BLOG & CONTENT** |||||
| 44 | `blog` | 🟡 RENAMED | Blog posts/articles written by admins | #24 (was `article`) |
| 45 | `blog_comment` | 🟡 RENAMED | User comments on blog posts | #25 (was `comment`) |
| 46 | `blog_rating` | 🟡 RENAMED | Ratings on blog posts | #26 (was `rating`) |
| **COMPANY REVIEW** |||||
| 47 | `company_review` | 🔵 NEW | Candidate reviews of companies (work experience, interview) | - |
| **CAREER COACHING** |||||
| 48 | `roadmap` | 🔵 NEW | Career roadmaps for candidates | - |
| 49 | `topic` | 🔵 NEW | Topics within career roadmaps | - |
| 50 | `subtopic` | 🔵 NEW | Subtopics within topics | - |
| 51 | `course` | 🔵 NEW | Courses recommended for career development | - |
| **CALENDAR & SCHEDULING** |||||
| 52 | `recruiter_working_hours` | 🔵 NEW | Recruiter availability for scheduling | - |
| 53 | `recruiter_time_off` | 🔵 NEW | Recruiter time-off/unavailable periods | - |

---

## Summary of Changes

### Renamed Entities (6)
| Old Name | New Name | Reason |
|----------|----------|--------|
| `candidateInfo` | `candidate` | Simplified naming |
| `recruiterInfo` | `recruiter` | Simplified naming |
| `adminInfo` | `admin` | Simplified naming |
| `article` | `blog` | More descriptive |
| `comment` | `blog_comment` | Clarifies relationship |
| `rating` | `blog_rating` | Clarifies relationship |
| `industry_experience` | `industry_experiences` | Pluralized |
| `package` | Split to `recruiter_package` + `candidate_package` | Separated by user type |

### New Entities Added (27)
1. **Authentication**: `forgot_password`, `invalid_token`
2. **Profile**: `recruiter_profile_update_requests`
3. **Job**: `jd_skill`, `job_apply`, `saved_job`, `job_feedback`
4. **Interview**: `interview_schedule`, `interview_session`, `interview_question`
5. **Employment**: `employment_verification`
6. **Notification**: `device_tokens`, `notification_heartbeat`
7. **Payment**: `recruiter_invoice`, `candidate_invoice`, `recruiter_entitlement`, `candidate_entitlement`, `recruiter_entitlement_package`, `candidate_entitlement_package`
8. **Review**: `company_review`
9. **Coaching**: `roadmap`, `topic`, `subtopic`, `course`
10. **Calendar**: `recruiter_working_hours`, `recruiter_time_off`

### Removed/Not Implemented (0)
- All original entities still exist (some renamed)

---

## ERD Completeness Check

### ✅ Entities IN ERD (Matching Project)
| ERD Entity | Project Entity | Match |
|------------|----------------|-------|
| candidate_entitlement | ✅ | Yes |
| candidate_entitlement_package | ✅ | Yes |
| candidate_package | ✅ | Yes |
| work_location_preferred | ✅ | Yes |
| job_description | ✅ | Yes |
| candidate_invoice | ✅ | Yes |
| notification | ✅ | Yes |
| certificate | ✅ | Yes |
| jd_skill | ✅ | Yes |
| job_posting | ✅ | Yes |
| job_feedback | ✅ | Yes |
| saved_job | ✅ | Yes |
| skill | ✅ | Yes |
| recruiter_entitlement_package | ✅ | Yes |
| recruiter_package | ✅ | Yes |
| job_apply | ✅ | Yes |
| candidateInfo (candidate) | ✅ | Yes |
| work_experience | ✅ | Yes |
| recruiter_entitlement | ✅ | Yes |
| recruiter_invoice | ✅ | Yes |
| resume | ✅ | Yes |
| foreign_language | ✅ | Yes |
| interview_schedule | ✅ | Yes |
| recruiter | ✅ | Yes |
| recruiter_update_request | ✅ | Yes |
| award | ✅ | Yes |
| highlight_project | ✅ | Yes |
| interviewer | ❌ | NOT an entity (just fields in interview_schedule) |
| account_role | ✅ | Yes |
| adminInfo (admin) | ✅ | Yes |
| interview_session | ✅ | Yes |
| subtopic | ✅ | Yes |
| topic | ✅ | Yes |
| roadmap | ✅ | Yes |
| role | ✅ | Yes |
| role_permissions | ✅ | Yes |
| interview_question | ✅ | Yes |
| article (blog) | ✅ | Yes |
| account | ✅ | Yes |
| permission | ✅ | Yes |
| Rating (blog_rating) | ✅ | Yes |
| Comment (blog_comment) | ✅ | Yes |
| education | ✅ | Yes |

### ❌ MISSING FROM ERD (Exist in Project)
| Entity | Description | Priority |
|--------|-------------|----------|
| `forgot_password` | Password reset tokens | Low |
| `invalid_token` | Blacklisted JWT tokens | Low |
| `device_tokens` | Mobile push notification tokens | Medium |
| `notification_heartbeat` | Health monitoring | Low |
| `company_review` | Candidate reviews of companies | **HIGH** |
| `employment_verification` | Employment status tracking | **HIGH** |
| `recruiter_working_hours` | Recruiter availability | Medium |
| `recruiter_time_off` | Recruiter unavailable periods | Medium |
| `course` | Career development courses | Medium |
| `work_model` | Candidate work preferences | Medium |
| `industry_experiences` | Candidate industry preferences | Medium |

### ⚠️ ERD Issues Found

1. **`interviewer`** shown as separate table in ERD but actually just **fields** in `interview_schedule`:
   - `interviewerName`
   - `interviewerEmail`
   - `interviewerPhone`
   - `interviewerNotes`

2. **Missing critical tables** in ERD:
   - `company_review` - Company review system
   - `employment_verification` - Employment tracking
   - `work_model` - Candidate preferences
   - `industry_experiences` - Candidate industry experience

3. **Naming inconsistencies** in ERD:
   - ERD shows `candidateInfo` → Project uses `candidate`
   - ERD shows `adminInfo` → Project uses `admin`
   - ERD shows `article` → Project uses `blog`
   - ERD shows `Rating` → Project uses `blog_rating`
   - ERD shows `Comment` → Project uses `blog_comment`

---

## Relationship Summary

### One-to-Many Relationships
```
account (1) ←→ (1) candidate
account (1) ←→ (1) recruiter
account (1) ←→ (1) admin
account (1) ←→ (M) forgot_password
candidate (1) ←→ (M) resume
candidate (1) ←→ (M) job_apply
candidate (1) ←→ (M) saved_job
candidate (1) ←→ (M) industry_experiences
candidate (1) ←→ (M) work_model
resume (1) ←→ (M) education
resume (1) ←→ (M) work_experience
resume (1) ←→ (M) skill
resume (1) ←→ (M) certificate
resume (1) ←→ (M) award
resume (1) ←→ (M) foreign_language
resume (1) ←→ (M) highlight_project
recruiter (1) ←→ (M) job_posting
job_posting (1) ←→ (1) job_description
job_posting (1) ←→ (M) job_apply
job_description (1) ←→ (M) jd_skill
job_apply (1) ←→ (M) interview_schedule
roadmap (1) ←→ (M) topic
topic (1) ←→ (M) subtopic
blog (1) ←→ (M) blog_comment
blog (1) ←→ (M) blog_rating
```

### Many-to-Many Relationships
```
account (M) ←→ (M) role [via account_role]
role (M) ←→ (M) permission [via role_permissions]
recruiter_entitlement (M) ←→ (M) recruiter_package [via recruiter_entitlement_package]
candidate_entitlement (M) ←→ (M) candidate_package [via candidate_entitlement_package]
```

---

## Recommended ERD Updates

1. **Add missing tables:**
   - `company_review`
   - `employment_verification`
   - `work_model`
   - `industry_experiences`
   - `course`
   - `device_tokens`
   - `recruiter_working_hours`
   - `recruiter_time_off`

2. **Remove `interviewer` table** - it's not a separate entity

3. **Rename for consistency:**
   - `candidateInfo` → `candidate`
   - `adminInfo` → `admin`
   - `article` → `blog`
   - `Rating` → `blog_rating`
   - `Comment` → `blog_comment`

4. **Add technical tables (optional):**
   - `forgot_password`
   - `invalid_token`
   - `notification_heartbeat`

---

**Total Entities: 53**
**In ERD: ~42**
**Missing from ERD: ~11**
**ERD Accuracy: ~79%**

---

## ERD Picture - Entity List (For Copy/Paste)

| # | Entity Name | Table Name | Description |
|---|-------------|------------|-------------|
| 1 | candidate_entitlement | candidate_entitlement | Active entitlements/credits for candidates |
| 2 | candidate_entitlement_package | candidate_entitlement_package | Junction: links entitlements to packages (candidate) |
| 3 | candidate_package | candidate_package | Subscription packages for candidates |
| 4 | work_location_preferred | work_location_preferred | Preferred job locations |
| 5 | job_description | job_description | Detailed job info: responsibilities, benefits |
| 6 | jd_skill | jd_skill | Skills required/optional for job descriptions |
| 7 | job_posting | job_posting | Job listings: title, location, salary, requirements |
| 8 | recruiter_entitlement_package | recruiter_entitlement_package | Junction: links entitlements to packages (recruiter) |
| 9 | recruiter_package | recruiter_package | Subscription packages for recruiters |
| 10 | recruiter_entitlement | recruiter_entitlement | Active entitlements/credits for recruiters |
| 11 | recruiter_invoice | recruiter_invoice | Payment invoices for recruiters |
| 12 | interview_schedule | interview_schedule | Scheduled interviews: date, time, location, status |
| 13 | recruiter | recruiter | Recruiter/company profile: logo, website, rating, description |
| 14 | interviewer | interviewer | ⚠️ NOT an entity - only fields in interview_schedule |
| 15 | account_role | account_role | Junction table: maps accounts to roles (M:N) |
| 16 | role | role | System roles: ADMIN, RECRUITER, CANDIDATE |
| 17 | role_permissions | role_permissions | Junction table: assigns permissions to roles (M:N) |
| 18 | permission | permission | System actions/access rights (CREATE_JOB, VIEW_RESUME, etc.) |
| 19 | account | account | Stores user account info: email, password, status |
| 20 | adminInfo | admin | Admin user details for system management |
| 21 | interview_question | interview_question | Questions used in AI interview sessions |
| 22 | interview_session | interview_session | AI interview practice sessions for candidates |
| 23 | saved_job | saved_job | Bookmarked jobs by candidates |
| 24 | job_apply | job_apply | Job applications: candidate, job, status, applied date |
| 25 | candidateInfo | candidate | Candidate profile: DOB, phone, gender, job level, experience |
| 26 | notification | notification | Notifications sent to users (job alerts, updates) |
| 27 | certificate | certificate | Certifications and credentials |
| 28 | skill | skill | Candidate skills with experience level |
| 29 | resume | resume | Candidate resumes: summary, file URL, primary flag |
| 30 | work_experience | work_experience | Past work experiences: company, position, duration |
| 31 | foreign_language | foreign_language | Languages known with proficiency level |
| 32 | highlight_project | highlight_project | Portfolio projects linked to resumes |
| 33 | award | award | Awards and recognitions |
| 34 | education | education | Education records: school, degree, major, GPA |
| 35 | recruiter_update_request | recruiter_profile_update_requests | Tracks recruiter profile update requests pending admin approval |
| 36 | candidate_invoice | candidate_invoice | Payment invoices for candidates |
| 37 | job_feedback | job_feedback | Recruiter feedback to candidates on applications |
| 38 | subtopic | subtopic | Subtopics within topics |
| 39 | topic | topic | Topics within career roadmaps |
| 40 | roadmap | roadmap | Career roadmaps for candidates |
| 41 | article | blog | Blog posts/articles written by admins |
| 42 | Rating | blog_rating | Ratings on blog posts |
| 43 | Comment | blog_comment | User comments on blog posts |

---

*Last Updated: December 13, 2025*
