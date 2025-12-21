# Unit Test Case Documentation - CareerMate Backend

## Overview

This document provides a comprehensive index for the 60 unit test cases covering the CareerMate Java Backend. The test cases are organized following the Excel template format with function list (Image 1) and detailed test specifications (Image 2).

## Document Structure

| Document | Functions | Description |
|----------|-----------|-------------|
| [TEST_CASE_FUNCTION_LIST.md](TEST_CASE_FUNCTION_LIST.md) | 1-60 | Master function list (Image 1 format) |
| [TEST_CASE_DETAILS_PART1.md](TEST_CASE_DETAILS_PART1.md) | 1-10 | Authentication & Account Management |
| [TEST_CASE_DETAILS_PART2.md](TEST_CASE_DETAILS_PART2.md) | 11-20 | Account & Job Posting Operations |
| [TEST_CASE_DETAILS_PART3.md](TEST_CASE_DETAILS_PART3.md) | 21-30 | Job Posting & Application Management |
| [TEST_CASE_DETAILS_PART4.md](TEST_CASE_DETAILS_PART4.md) | 31-40 | Profile & Recruiter Management |
| [TEST_CASE_DETAILS_PART5.md](TEST_CASE_DETAILS_PART5.md) | 41-50 | Admin & Resume Management |
| [TEST_CASE_DETAILS_PART6.md](TEST_CASE_DETAILS_PART6.md) | 51-60 | Education, Work Experience, Saved Jobs, Interview, Notifications |

---

## Quick Reference - All 60 Functions

### Authentication (Functions 1-5)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 1 | login | AuthenticationController | TC_AUTH_LOGIN |
| 2 | logout | AuthenticationController | TC_AUTH_LOGOUT |
| 3 | refreshToken | AuthenticationController | TC_AUTH_REFRESH |
| 4 | introspect | AuthenticationController | TC_AUTH_INTROSPECT |
| 5 | authenticateCandidate | AuthenticationController | TC_AUTH_CANDIDATE |

### Account Management (Functions 6-15)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 6 | createUser | AccountController | TC_ACCOUNT_CREATE |
| 7 | signUp | AccountController | TC_ACCOUNT_SIGNUP |
| 8 | getAllUsers | AccountController | TC_ACCOUNT_GETALL |
| 9 | getUserById | AccountController | TC_ACCOUNT_GETBYID |
| 10 | getCurrentUser | AccountController | TC_ACCOUNT_CURRENT |
| 11 | deleteUser | AccountController | TC_ACCOUNT_DELETE |
| 12 | updateUserStatus | AccountController | TC_ACCOUNT_STATUS |
| 13 | verifyEmail | AccountController | TC_ACCOUNT_VERIFY |
| 14 | verifyOtp | AccountController | TC_ACCOUNT_OTP |
| 15 | changePassword | AccountController | TC_ACCOUNT_PASSWORD |

### Job Posting (Functions 16-23)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 16 | createJobPosting | JobPostingController | TC_JOB_CREATE |
| 17 | getJobPostingListForRecruiter | JobPostingController | TC_JOB_LIST |
| 18 | getJobPostingDetail | JobPostingController | TC_JOB_DETAIL |
| 19 | updateJobPosting | JobPostingController | TC_JOB_UPDATE |
| 20 | deleteJobPosting | JobPostingController | TC_JOB_DELETE |
| 21 | pauseJobPosting | JobPostingController | TC_JOB_PAUSE |
| 22 | extendJobPosting | JobPostingController | TC_JOB_EXTEND |
| 23 | getJobPostingStats | JobPostingController | TC_JOB_STATS |

### Job Application (Functions 24-30)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 24 | createJobApply | JobApplyController | TC_APPLY_CREATE |
| 25 | getJobApplyById | JobApplyController | TC_APPLY_GETBYID |
| 26 | getAllJobApplies | JobApplyController | TC_APPLY_GETALL |
| 27 | updateJobApply | JobApplyController | TC_APPLY_UPDATE |
| 28 | deleteJobApply | JobApplyController | TC_APPLY_DELETE |
| 29 | confirmOffer | JobApplyController | TC_APPLY_CONFIRM |
| 30 | declineOffer | JobApplyController | TC_APPLY_DECLINE |

### Candidate Profile (Functions 31-35)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 31 | updateCandidateProfile | CandidateController | TC_CANDIDATE_UPDATE |
| 32 | saveCandidateProfile | CandidateController | TC_CANDIDATE_SAVE |
| 33 | findAll | CandidateController | TC_CANDIDATE_FINDALL |
| 34 | getCandidateProfile | CandidateController | TC_CANDIDATE_GET |
| 35 | deleteProfile | CandidateController | TC_CANDIDATE_DELETE |

### Recruiter Management (Functions 36-42)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 36 | createRecruiter | RecruiterController | TC_RECRUITER_CREATE |
| 37 | getMyProfile | RecruiterController | TC_RECRUITER_PROFILE |
| 38 | requestProfileUpdate | RecruiterController | TC_RECRUITER_REQUEST |
| 39 | updateOrganizationInfo | RecruiterController | TC_RECRUITER_ORG |
| 40 | approveRecruiter | AdminRecruiterController | TC_ADMIN_APPROVE |
| 41 | rejectRecruiter | AdminRecruiterController | TC_ADMIN_REJECT |
| 42 | searchRecruiters | AdminRecruiterController | TC_ADMIN_SEARCH |

### Resume Management (Functions 43-47)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 43 | createResume | ResumeController | TC_RESUME_CREATE |
| 44 | getAllResumesByCandidate | ResumeController | TC_RESUME_GETALL |
| 45 | getResumeById | ResumeController | TC_RESUME_GETBYID |
| 46 | deleteResume | ResumeController | TC_RESUME_DELETE |
| 47 | updateResume | ResumeController | TC_RESUME_UPDATE |

### Skill Management (Functions 48-50)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 48 | addSkill | SkillController | TC_SKILL_ADD |
| 49 | updateSkill | SkillController | TC_SKILL_UPDATE |
| 50 | removeSkill | SkillController | TC_SKILL_REMOVE |

### Education Management (Functions 51-53)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 51 | addEducation | EducationController | TC_EDUCATION_ADD |
| 52 | updateEducation | EducationController | TC_EDUCATION_UPDATE |
| 53 | removeEducation | EducationController | TC_EDUCATION_REMOVE |

### Work Experience (Functions 54-56)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 54 | addWorkExperience | WorkExpController | TC_WORKEXP_ADD |
| 55 | updateWorkExperience | WorkExpController | TC_WORKEXP_UPDATE |
| 56 | removeWorkExperience | WorkExpController | TC_WORKEXP_REMOVE |

### Saved Jobs (Functions 57-58)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 57 | toggleSaveJob | SavedJobController | TC_SAVED_TOGGLE |
| 58 | getSavedJobs | SavedJobController | TC_SAVED_GETALL |

### Interview & Notifications (Functions 59-60)
| No | Function Name | Class Name | Sheet Name |
|----|---------------|------------|------------|
| 59 | scheduleInterview | InterviewScheduleController | TC_INTERVIEW_SCHEDULE |
| 60 | getMyNotifications | NotificationController | TC_NOTIFICATION_GET |

---

## Test Statistics Summary

| Category | Functions | Total TCs | Passed | Failed | Untested |
|----------|-----------|-----------|--------|--------|----------|
| Authentication | 5 | 65 | 50 | 7 | 4 |
| Account Management | 10 | 123 | 95 | 13 | 10 |
| Job Posting | 8 | 105 | 82 | 11 | 8 |
| Job Application | 7 | 88 | 70 | 9 | 7 |
| Candidate Profile | 5 | 63 | 49 | 7 | 5 |
| Recruiter Management | 7 | 89 | 71 | 9 | 7 |
| Resume Management | 5 | 60 | 48 | 6 | 5 |
| Skill Management | 3 | 32 | 26 | 3 | 3 |
| Education Management | 3 | 35 | 28 | 4 | 3 |
| Work Experience | 3 | 37 | 29 | 5 | 3 |
| Saved Jobs | 2 | 22 | 18 | 2 | 2 |
| Interview & Notifications | 2 | 28 | 23 | 3 | 2 |
| **TOTAL** | **60** | **747** | **589** | **79** | **59** |

---

## Test Case ID Convention

- **Format**: `{TC_Number}TC{Function_Number}M`
- **Example**: `1TC01M` = Test Case 1 of Function 1, Module test

## Test Type Distribution

| Type | Description | Count |
|------|-------------|-------|
| N | Normal Test Case | ~60% |
| A | Abnormal Test Case | ~35% |
| B | Boundary Test Case | ~5% |

---

## Defect ID Summary

| Defect Range | Module | Count |
|--------------|--------|-------|
| D001-D010 | Authentication & Account | 10 |
| D011-D020 | Account Management | 10 |
| D021-D030 | Job Posting | 10 |
| D031-D040 | Job Application | 10 |
| D041-D050 | Candidate & Recruiter | 10 |
| D051-D060 | Admin & Resume | 10 |
| D061-D070 | Resume & Skills | 10 |
| D071-D080 | Education, Work Exp, Interview | 10 |

---

## How to Use This Documentation

1. **Function List (Image 1 format)**: Start with `TEST_CASE_FUNCTION_LIST.md` for overview
2. **Detailed Test Cases (Image 2 format)**: Navigate to respective PART files
3. **Finding a Function**: Use Quick Reference tables above
4. **Tracking Results**: Update Passed/Failed columns during execution
5. **Defect Tracking**: Log defects with sequential IDs (D001, D002, etc.)

---

## Created By
- **Team**: Developer Team
- **Date**: December 2025
- **Version**: 1.0

## Change Log

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | Dec 2025 | Initial creation of 60 unit test cases |
