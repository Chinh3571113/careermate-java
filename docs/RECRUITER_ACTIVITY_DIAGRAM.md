# CareerMate - Recruiter Activity Diagrams

## Overview
Activity diagrams cho các hoạt động chính của Recruiter trong hệ thống CareerMate.

---

## 1. Recruiter Registration & Onboarding

```
                    START
                      │
                      ▼
        ┌─────────────────────────┐
        │  Choose Registration    │
        │  Method                 │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌───────────────┐   ┌───────────────┐
│ Google OAuth2 │   │ Email/Password│
└───────┬───────┘   └───────┬───────┘
        │                   │
        │                   ▼
        │           ┌───────────────┐
        │           │ Enter Credentials│
        │           └───────┬───────┘
        │                   │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  System Authenticates   │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Fill Company Profile   │
        │  • Company Name         │
        │  • Industry             │
        │  • Size                 │
        │  • Address              │
        │  • Tax ID               │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Upload Documents       │
        │  • Business License     │
        │  • Company Logo         │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Set Working Hours      │
        │  (Weekly Schedule)      │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Submit for Approval    │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Admin Reviews          │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌───────────────┐   ┌───────────────┐
│   Approved    │   │   Rejected    │
└───────┬───────┘   └───────┬───────┘
        │                   │
        │                   ▼
        │           ┌───────────────┐
        │           │ Receive Feedback│
        │           └───────┬───────┘
        │                   │
        │                   ▼
        │           ┌───────────────┐
        │           │ Fix & Resubmit│
        │           └───────┬───────┘
        │                   │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Account Activated      │
        │  Receive Welcome Email  │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Dashboard Access       │
        └─────────┬───────────────┘
                  │
                  ▼
                  END
```

---

## 2. Post Job Activity

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              POST JOB WORKFLOW                                │
├────────────────┬────────────────┬────────────────┬────────────────┬──────────┤
│   RECRUITER    │     SYSTEM     │    DATABASE    │     KAFKA      │    AI    │
├────────────────┼────────────────┼────────────────┼────────────────┼──────────┤
│                │                │                │                │          │
│      START     │                │                │                │          │
│       │        │                │                │                │          │
│       ▼        │                │                │                │          │
│ Check          │                │                │                │          │
│ Entitlements   │────────────────>│                │                │          │
│                │ Get Usage      │                │                │          │
│                │                │                │                │          │
│                │<────────────────│ Fetch          │                │          │
│                │ Return Slots   │ Entitlements   │                │          │
│       │        │                │<───────────────│                │          │
│       ▼        │                │ Return Data    │                │          │
│  ┌─────────┐  │                │                │                │          │
│  │Has Slots│  │                │                │                │          │
│  └────┬────┘  │                │                │                │          │
│       │       │                │                │                │          │
│   ┌───┴───┐   │                │                │                │          │
│   │  Yes  │   │                │                │                │          │
│   └───┬───┘   │                │                │                │          │
│       │       │                │                │                │          │
│       ▼       │                │                │                │          │
│ Click "Post   │                │                │                │          │
│ New Job"      │                │                │                │          │
│       │       │                │                │                │          │
│       ▼       │                │                │                │          │
│ Fill Job Form │                │                │                │          │
│ • Title       │                │                │                │          │
│ • Description │                │                │                │          │
│ • Requirements│                │                │                │          │
│ • Salary      │                │                │                │          │
│ • Location    │                │                │                │          │
│       │       │                │                │                │          │
│       ▼       │                │                │                │          │
│ Add Skills    │────────────────>│                │                │          │
│               │ Fetch Skills   │                │                │          │
│               │                │                │                │          │
│               │<────────────────│ Query Skills   │                │          │
│               │ Return List    │<───────────────│                │          │
│       │       │                │ Skill Data     │                │          │
│       ▼       │                │                │                │          │
│ Select Skills │                │                │                │          │
│ & Deadline    │                │                │                │          │
│       │       │                │                │                │          │
│       ▼       │                │                │                │          │
│ Review Preview│                │                │                │          │
│       │       │                │                │                │          │
│   ┌───┴───┐   │                │                │                │          │
│   │Publish│   │                │                │                │          │
│   └───┬───┘   │                │                │                │          │
│       │       │                │                │                │          │
│       ▼       │                │                │                │          │
│ Submit Job    │────────────────>│                │                │          │
│               │ Validate Job   │────────────────────────────────>│          │
│               │                │                │                │ AI Check │
│               │                │                │                │ Quality  │
│               │                │                │                │ & Score  │
│               │<───────────────────────────────────────────────────│          │
│               │ Valid ✓        │                │                │          │
│               │                │                │                │          │
│               │                │                │                │          │
│               │<────────────────│ Create Job     │                │          │
│               │ Job Created    │────────────────>│                │          │
│               │                │ Save Job       │                │          │
│               │                │ Decrement Slot │                │          │
│               │                │<───────────────│                │          │
│               │                │                │                │          │
│               │                │                │                │          │
│               │                │────────────────────────────────>│          │
│               │                │ Publish Event  │                │          │
│               │                │ "job_posted"   │                │          │
│       ▼       │                │                │                │          │
│ Show Success  │                │                │                │          │
│ "Job Posted"  │                │                │                │          │
│       │       │                │                │   │            │          │
│       ▼       │                │                │   │            │          │
│      END      │                │                │   ▼            │          │
│               │                │                │ Notify         │          │
│               │                │                │ Matching       │          │
│               │                │                │ Candidates     │          │
└───────────────┴────────────────┴────────────────┴────────────────┴──────────┘
```

---

## 3. Review Applications Activity

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        REVIEW APPLICATIONS WORKFLOW                           │
├────────────────┬────────────────┬────────────────┬────────────────┬──────────┤
│   RECRUITER    │     SYSTEM     │    DATABASE    │     KAFKA      │    AI    │
├────────────────┼────────────────┼────────────────┼────────────────┼──────────┤
│                │                │                │                │          │
│      START     │                │                │                │          │
│       │        │                │                │                │          │
│       ▼        │                │                │                │          │
│ Access         │────────────────>│                │                │          │
│ Dashboard      │ Load Dashboard │                │                │          │
│                │                │                │                │          │
│                │<────────────────│ Query Jobs     │                │          │
│                │ Job List       │────────────────>│                │          │
│       │        │ with App Count │ Fetch Jobs     │                │          │
│       ▼        │                │<───────────────│                │          │
│ Select Job     │                │                │                │          │
│ Posting        │────────────────>│                │                │          │
│                │ Get Applications│               │                │          │
│                │                │                │                │          │
│                │<────────────────│ Query Apps     │                │          │
│                │ App List       │────────────────>│                │          │
│       │        │                │ Fetch Apps with│                │          │
│       ▼        │                │ AI Scores      │────────────────────────>│
│ Filter & Sort  │                │<───────────────│                │ Calculate│
│ • By Date      │                │                │                │ Match    │
│ • By AI Score  │                │                │<────────────────────────│
│ • By Status    │                │                │                │ Scores   │
│       │        │                │                │                │          │
│       ▼        │                │                │                │          │
│ Select         │────────────────>│                │                │          │
│ Application    │ Get Candidate  │                │                │          │
│                │ Profile        │                │                │          │
│                │<────────────────│ Fetch Profile  │                │          │
│                │ • Resume       │────────────────>│                │          │
│       │        │ • Experience   │ Get Candidate  │                │          │
│       ▼        │ • Skills       │<───────────────│                │          │
│ View Profile   │ • AI Score     │                │                │          │
│ & Resume       │                │                │                │          │
│       │        │                │                │                │          │
│       ▼        │                │                │                │          │
│ ┌──────────┐  │                │                │                │          │
│ │Download  │  │────────────────>│                │                │          │
│ │Resume?   │  │ Get Resume URL │                │                │          │
│ └────┬─────┘  │<────────────────│ Fetch URL      │                │          │
│      │        │ Resume File    │────────────────>│                │          │
│      ▼        │                │ Get File       │                │          │
│ Make Decision │                │<───────────────│                │          │
│      │        │                │                │                │          │
│  ┌───┴───┬────┬─────┐          │                │                │          │
│  │       │    │     │          │                │                │          │
│  ▼       ▼    ▼     ▼          │                │                │          │
│Shortlist│Interview│Reject│Pending│              │                │          │
│  │      │    │     │          │                │                │          │
│  └──────┴────┴─────┴──────┘  │                │                │          │
│       │        │                │                │                │          │
│       ▼        │                │                │                │          │
│ Add Notes     │────────────────>│                │                │          │
│ & Comments    │ Update Status  │                │                │          │
│               │                │                │                │          │
│               │<────────────────│ Save Status    │                │          │
│               │ Updated ✓      │────────────────>│                │          │
│               │                │ Update App     │                │          │
│               │                │<───────────────│                │          │
│               │                │                │                │          │
│               │                │                │                │          │
│               │                │────────────────────────────────>│          │
│               │                │ Publish Event  │                │          │
│               │                │ "app_status_   │                │          │
│               │                │  changed"      │                │          │
│       │       │                │                │   │            │          │
│       ▼       │                │                │   ▼            │          │
│ ┌──────────┐ │                │                │ Notify         │          │
│ │More Apps?│ │                │                │ Candidate      │          │
│ └────┬─────┘ │                │                │                │          │
│      │       │                │                │                │          │
│  ┌───┴───┐   │                │                │                │          │
│  │  Yes  │   │                │                │                │          │
│  └───┬───┘   │                │                │                │          │
│      │       │                │                │                │          │
│  (Loop back to Select Application)             │                │          │
│      │       │                │                │                │          │
│  ┌───┴───┐   │                │                │                │          │
│  │  No   │   │                │                │                │          │
│  └───┬───┘   │                │                │                │          │
│      │       │                │                │                │          │
│      ▼       │                │                │                │          │
│     END      │                │                │                │          │
└───────────────┴────────────────┴────────────────┴────────────────┴──────────┘
```

---

## 4. Schedule Interview Activity

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                        SCHEDULE INTERVIEW WORKFLOW                               │
├────────────────┬────────────────┬────────────────┬───────────────┬──────────────┤
│   RECRUITER    │     SYSTEM     │    DATABASE    │     KAFKA     │  CANDIDATE   │
├────────────────┼────────────────┼────────────────┼───────────────┼──────────────┤
│                │                │                │               │              │
│      START     │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Select         │────────────────>│                │               │              │
│ Shortlisted    │ Get Candidate  │                │               │              │
│ Candidate      │                │                │               │              │
│                │<────────────────│ Fetch Data     │               │              │
│                │ Candidate Info │────────────────>│               │              │
│       │        │                │ Get Candidate  │               │              │
│       ▼        │                │<───────────────│               │              │
│ Click "Schedule│               │                │               │              │
│ Interview"     │────────────────>│                │               │              │
│                │ Check Calendar │                │               │              │
│                │                │                │               │              │
│                │<────────────────│ Query Calendar │               │              │
│                │ • Working Hours│────────────────>│               │              │
│       │        │ • Existing     │ Fetch:         │               │              │
│       ▼        │   Interviews   │ • WorkingHours │               │              │
│ View Calendar  │ • Time Off     │ • Interviews   │               │              │
│ Available Slots│ • Free Slots   │ • TimeOff      │               │              │
│                │                │<───────────────│               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Select:        │                │                │               │              │
│ • Type (Phone/ │                │                │               │              │
│   Video/       │                │                │               │              │
│   In-person)   │                │                │               │              │
│ • Date & Time  │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Submit Time    │────────────────>│                │               │              │
│                │ Validate Slot  │                │               │              │
│                │ • No conflicts │                │               │              │
│                │ • Work hours   │                │               │              │
│                │<────────────────│ Check Conflicts│               │              │
│                │ Valid ✓        │────────────────>│               │              │
│       │        │                │ Validate       │               │              │
│       ▼        │                │<───────────────│               │              │
│ Set Duration   │                │                │               │              │
│ (30/45/60 min) │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Add Details:   │                │                │               │              │
│ • Meeting Link │                │                │               │              │
│ • Agenda       │                │                │               │              │
│ • Location     │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Add            │────────────────>│                │               │              │
│ Interviewers   │ Get Recruiters │                │               │              │
│                │<────────────────│ Query Team     │               │              │
│                │ Team List      │────────────────>│               │              │
│       │        │                │ Fetch Recruiters│              │              │
│       ▼        │                │<───────────────│               │              │
│ Request AI     │────────────────>│                │               │              │
│ Questions      │ Generate Qs    │────────────────>│               │              │
│                │                │ Get Job & Skills│              │              │
│                │                │<───────────────│               │              │
│                │<────────────────│ AI Generated   │               │              │
│       │        │ Questions      │                │               │              │
│       ▼        │ Based on:      │                │               │              │
│ Review/Edit    │ • Job Reqs     │                │               │              │
│ Questions      │ • Skills       │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Add Notes      │                │                │               │              │
│ (Optional)     │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Confirm        │────────────────>│                │               │              │
│ Schedule       │ Create Interview│               │               │              │
│                │                │                │               │              │
│                │<────────────────│ Save Interview │               │              │
│                │ Interview ID   │────────────────>│               │              │
│                │                │ Create Schedule│               │              │
│                │                │<───────────────│               │              │
│                │                │                │               │              │
│                │                │────────────────────────────────>│              │
│                │                │ Publish Event  │               │              │
│                │                │ "interview_    │               │              │
│                │                │  scheduled"    │               │              │
│       │        │                │                │   │           │              │
│       ▼        │                │                │   ▼           │              │
│ Confirmation   │                │                │ Notify        │              │
│ "Interview     │                │                │ Candidate     │              │
│  Scheduled"    │                │                │               │              │
│                │                │                │               │<─────────────┤
│                │                │                │               │ Send:        │
│                │                │                │               │ • Email      │
│                │                │                │               │ • Push       │
│                │                │                │               │ • Calendar   │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Wait for       │                │                │               │              │
│ Confirmation   │                │                │               │              │
│       │        │                │                │               │              │
│  ┌────┴────┐   │                │                │               │              │
│  │Confirmed│   │                │                │               │              │
│  │   by    │   │                │                │               │              │
│  │Candidate│   │                │                │               │              │
│  └────┬────┘   │                │                │               │              │
│       │        │                │                │<──────────────────────────────┤
│       │        │                │                │ Candidate     │              │
│       │        │                │                │ Confirms      │              │
│       │        │                │<───────────────┤               │              │
│       │        │                │ Update Status  │               │              │
│       │        │<────────────────│ CONFIRMED      │               │              │
│       │        │ Updated ✓      │────────────────>│               │              │
│       │        │                │ Save Confirm   │               │              │
│       │        │                │<───────────────│               │              │
│       │        │                │                │               │              │
│       │        │                │────────────────────────────────>│              │
│       │        │                │ Publish Event  │               │              │
│       │        │                │ "interview_    │               │              │
│       │        │                │  confirmed"    │               │              │
│<──────────────────────────────────────────────────┤               │              │
│ Notification   │                │                │ Notify        │              │
│ "Interview     │                │                │ Recruiter     │              │
│  Confirmed"    │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Add to         │                │                │               │              │
│ My Calendar    │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│      END       │                │                │               │              │
└────────────────┴────────────────┴────────────────┴───────────────┴──────────────┘
                      │
                      ▼
        ┌─────────────────────────┐
        │  Select Shortlisted     │
        │  Candidate              │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Click "Schedule        │
        │  Interview"             │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Check Calendar         │
        │  Availability           │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  System Shows:          │
        │  • Working Hours        │
        │  • Existing Interviews  │
        │  • Time Off Periods     │
        │  • Available Slots      │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Select Interview Type  │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┬─────────┐
        │                   │         │
        ▼                   ▼         ▼
   ┌────────┐        ┌────────┐ ┌────────┐
   │ Phone  │        │ Video  │ │In-Person│
   └────┬───┘        └────┬───┘ └────┬───┘
        │                 │         │
        └─────────┬───────┴─────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Select Date & Time     │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  System Validates:      │
        │  • Not conflicting      │
        │  • Within working hours │
        │  • Not on time off      │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
   ┌────────┐        ┌────────┐
   │ Valid  │        │Invalid │
   └────┬───┘        └────┬───┘
        │                 │
        │                 ▼
        │          ┌──────────────┐
        │          │ Show Error   │
        │          │ Message      │
        │          └──────┬───────┘
        │                 │
        │                 ▼
        │          ┌──────────────┐
        │          │ Select       │
        │          │ Different    │
        │          │ Time         │
        │          └──────┬───────┘
        │                 │
        └─────────┬───────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Set Duration           │
        │  (30/45/60 minutes)     │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Add Interview Details  │
        │  • Meeting Link (Video) │
        │  • Location (In-person) │
        │  • Phone Number (Phone) │
        │  • Agenda               │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Add Interviewers       │
        │  (Multiple recruiters)  │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  AI Suggests Interview  │
        │  Questions based on:    │
        │  • Job Requirements     │
        │  • Candidate Skills     │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Review/Edit Questions  │
        │  (Optional)             │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Add Special Notes      │
        │  (Optional)             │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Review Interview       │
        │  Summary                │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Confirm Schedule       │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Save to Database       │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Publish Event to Kafka │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌───────────────┐   ┌───────────────┐
│ Notification  │   │ Calendar      │
│ to Candidate  │   │ Updated       │
│ • Email       │   │               │
│ • Push        │   │               │
│ • SMS         │   │               │
└───────┬───────┘   └───────┬───────┘
        │                   │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Wait for Candidate     │
        │  Confirmation           │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌───────────────┐   ┌───────────────┐
│  Confirmed    │   │  Reschedule   │
│               │   │  Request      │
└───────┬───────┘   └───────┬───────┘
        │                   │
        │                   ▼
        │           ┌───────────────┐
        │           │ Review Request│
        │           └───────┬───────┘
        │                   │
        │           ┌───────┴───────┐
        │           │               │
        │           ▼               ▼
        │    ┌──────────┐   ┌──────────┐
        │    │ Accept   │   │ Decline  │
        │    └────┬─────┘   └────┬─────┘
        │         │              │
        │         ▼              │
        │  (Repeat Scheduling)   │
        │                        │
        └────────┬───────────────┘
                 │
                 ▼
        ┌───────────────┐
        │ Interview     │
        │ Scheduled     │
        └───────┬───────┘
                │
                ▼
        ┌───────────────┐
        │ Add to        │
        │ My Calendar   │
        └───────┬───────┘
                │
                ▼
               END
```

---

## 5. Conduct Interview & Evaluation Activity

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                    CONDUCT INTERVIEW & EVALUATION WORKFLOW                       │
├────────────────┬────────────────┬────────────────┬───────────────┬──────────────┤
│   RECRUITER    │     SYSTEM     │    DATABASE    │     KAFKA     │  CANDIDATE   │
├────────────────┼────────────────┼────────────────┼───────────────┼──────────────┤
│                │                │                │               │              │
│      START     │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Receive        │<───────────────────────────────────────────────────────────────┤
│ Reminder       │                │                │ Scheduled     │              │
│ (1 hour before)│                │                │ Notification  │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Access         │────────────────>│                │               │              │
│ Interview from │ Get Interview  │                │               │              │
│ Calendar       │                │                │               │              │
│                │<────────────────│ Query Interview│               │              │
│                │ Interview Data:│────────────────>│               │              │
│       │        │ • Candidate    │ Fetch:         │               │              │
│       ▼        │ • Resume       │ • Interview    │               │              │
│ View Details:  │ • Questions    │ • Candidate    │               │              │
│ • Candidate    │ • Job Desc     │ • Questions    │               │              │
│ • Resume       │                │<───────────────│               │              │
│ • Questions    │                │                │               │              │
│ • Agenda       │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Start Interview│────────────────>│                │               │              │
│ (Join Meeting/ │ Start Session  │                │               │              │
│  Call)         │                │                │               │              │
│                │<────────────────│ Update Status  │               │              │
│                │ Session Started│────────────────>│               │              │
│       │        │                │ ONGOING        │               │              │
│       ▼        │                │<───────────────│               │              │
│ ┌──────────┐  │                │                │               │              │
│ │Candidate │  │                │                │               │              │
│ │No Show?  │  │                │                │               │              │
│ └────┬─────┘  │                │                │               │              │
│      │        │                │                │               │              │
│  ┌───┴───┐    │                │                │               │              │
│  │  No   │    │                │                │               │              │
│  └───┬───┘    │                │                │               │              │
│      │        │                │                │               │              │
│      ▼        │                │                │               │              │
│ Conduct        │                │                │               │              │
│ Interview      │                │                │               │              │
│ • Ask Questions│               │                │               │              │
│ • Take Notes   │                │                │               │              │
│ • Observe      │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Complete       │────────────────>│                │               │              │
│ Interview      │ End Session    │                │               │              │
│                │<────────────────│ Update Status  │               │              │
│                │ Completed      │────────────────>│               │              │
│       │        │                │ COMPLETED      │               │              │
│       ▼        │                │<───────────────│               │              │
│ Open Evaluation│────────────────>│                │               │              │
│ Form           │ Get Eval Form  │                │               │              │
│                │<────────────────│ Fetch Form     │               │              │
│                │ Form Template  │────────────────>│               │              │
│       │        │                │ Get Template   │               │              │
│       ▼        │                │<───────────────│               │              │
│ Rate Candidate:│                │                │               │              │
│ • Technical    │                │                │               │              │
│   Skills (1-5) │                │                │               │              │
│ • Communication│                │                │               │              │
│   (1-5)        │                │                │               │              │
│ • Problem      │                │                │               │              │
│   Solving (1-5)│                │                │               │              │
│ • Cultural Fit │                │                │               │              │
│   (1-5)        │                │                │               │              │
│ • Overall (1-5)│                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Add Detailed   │                │                │               │              │
│ Notes:         │                │                │               │              │
│ • Strengths    │                │                │               │              │
│ • Weaknesses   │                │                │               │              │
│ • Key Obs      │                │                │               │              │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│ Make Decision  │                │                │               │              │
│       │        │                │                │               │              │
│  ┌────┴────┬───┴───┐            │               │               │              │
│  │         │       │            │               │               │              │
│  ▼         ▼       ▼            │               │               │              │
│ Next    Additional Reject       │               │               │              │
│ Round   Interview               │               │               │              │
│  │         │       │            │               │               │              │
│  └─────────┴───────┘            │               │               │              │
│       │        │                │               │               │              │
│       ▼        │                │               │               │              │
│ Submit         │────────────────>│               │               │              │
│ Evaluation     │ Save Evaluation│               │               │              │
│                │                │               │               │              │
│                │<────────────────│ Save to DB    │               │              │
│                │ Saved ✓        │────────────────>│               │              │
│                │                │ Create         │               │              │
│                │                │ Evaluation     │               │              │
│                │                │<───────────────│               │              │
│                │                │                │               │              │
│                │                │────────────────────────────────>│              │
│                │                │ Publish Event  │               │              │
│                │                │ "interview_    │               │              │
│                │                │  evaluated"    │               │              │
│       │        │                │                │   │           │              │
│       ▼        │                │                │   ▼           │              │
│ Confirmation   │                │                │ Notify        │              │
│ "Evaluation    │                │                │ Candidate     │              │
│  Saved"        │                │                │               │<─────────────┤
│                │                │                │               │ Send Result  │
│                │                │                │               │ Notification │
│       │        │                │                │               │              │
│       ▼        │                │                │               │              │
│      END       │                │                │               │              │
└────────────────┴────────────────┴────────────────┴───────────────┴──────────────┘
        │  • Candidate Info       │
        │  • Resume               │
        │  • Job Description      │
        │  • Prepared Questions   │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Start Interview        │
        │  (Join Meeting/Call)    │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Conduct Interview      │
        │  • Ask Questions        │
        │  • Take Notes           │
        │  • Observe Responses    │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Candidate No Show?     │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
   ┌────────┐        ┌────────┐
   │  Yes   │        │   No   │
   └────┬───┘        └────┬───┘
        │                 │
        ▼                 ▼
┌───────────────┐  (Continue Interview)
│ Mark as       │
│ No Show       │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Send          │
│ Notification  │
└───────┬───────┘
        │
        └────────────────┐
                         │
                         ▼
                ┌───────────────────┐
                │ Complete Interview│
                └────────┬──────────┘
                         │
                         ▼
                ┌───────────────────┐
                │ Open Evaluation   │
                │ Form              │
                └────────┬──────────┘
                         │
                         ▼
                ┌───────────────────┐
                │ Rate Candidate    │
                │ • Technical Skills│
                │ • Communication   │
                │ • Problem Solving │
                │ • Cultural Fit    │
                │ • Overall Score   │
                │ (1-5 stars each)  │
                └────────┬──────────┘
                         │
                         ▼
                ┌───────────────────┐
                │ Add Detailed Notes│
                │ • Strengths       │
                │ • Weaknesses      │
                │ • Key Observations│
                └────────┬──────────┘
                         │
                         ▼
                ┌───────────────────┐
                │ Make Decision     │
                └────────┬──────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Proceed to   │ │ Additional   │ │  Reject      │
│ Next Round   │ │ Interview    │ │              │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       │                │                ▼
       │                │        ┌──────────────┐
       │                │        │ Add Rejection│
       │                │        │ Feedback     │
       │                │        └──────┬───────┘
       │                │               │
       │                ▼               │
       │        ┌──────────────┐        │
       │        │ Schedule     │        │
       │        │ Next Interview│       │
       │        └──────┬───────┘        │
       │               │                │
       └───────┬───────┴────────────────┘
               │
               ▼
      ┌───────────────────┐
      │ Update Job Apply  │
      │ Status            │
      └────────┬──────────┘
               │
               ▼
      ┌───────────────────┐
      │ Publish Event to  │
      │ Kafka             │
      └────────┬──────────┘
               │
               ▼
      ┌───────────────────┐
      │ Notification Sent │
      │ to Candidate      │
      └────────┬──────────┘
               │
               ▼
      ┌───────────────────┐
      │ Save Evaluation   │
      │ to Database       │
      └────────┬──────────┘
               │
               ▼
              END
```

---

## 6. Send Employment Contract Activity

```
                    START
                      │
                      ▼
        ┌─────────────────────────┐
        │  Select Candidate for   │
        │  Job Offer              │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Click "Send Contract"  │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Choose Contract Type   │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┬─────────┐
        │                   │         │
        ▼                   ▼         ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Full-time    │  │ Part-time    │  │ Contract/    │
│              │  │              │  │ Freelance    │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │
       └─────────┬───────┴─────────────────┘
                 │
                 ▼
        ┌─────────────────────────┐
        │  Enter Contract Details │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  • Position Title       │
        │  • Department           │
        │  • Start Date           │
        │  • End Date (if any)    │
        │  • Probation Period     │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Enter Salary Details   │
        │  • Base Salary          │
        │  • Currency             │
        │  • Payment Frequency    │
        │  • Bonuses              │
        │  • Benefits             │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Set Working Conditions │
        │  • Working Hours        │
        │  • Work Location        │
        │  • Remote Options       │
        │  • Annual Leave         │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Add Job Responsibilities│
        │  (Auto-filled from JD)  │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Add Terms & Conditions │
        │  • Non-compete          │
        │  • Confidentiality      │
        │  • Termination Policy   │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Preview Contract       │
        │  (Generated PDF)        │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Edit Contract?         │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
   ┌────────┐        ┌────────┐
   │  Yes   │        │   No   │
   └────┬───┘        └────┬───┘
        │                 │
        │                 ▼
        │         ┌───────────────┐
        │         │ Add Digital   │
        │         │ Signature     │
        │         └───────┬───────┘
        │                 │
        │                 ▼
        │         ┌───────────────┐
        │         │ Set Signing   │
        │         │ Deadline      │
        │         │ (7-14 days)   │
        │         └───────┬───────┘
        │                 │
        └────┬────────────┘
             │
             ▼
    ┌───────────────────┐
    │ Confirm & Send    │
    └────────┬──────────┘
             │
             ▼
    ┌───────────────────┐
    │ Upload to Firebase│
    │ Storage           │
    └────────┬──────────┘
             │
             ▼
    ┌───────────────────┐
    │ Save to Database  │
    │ Status: SENT      │
    └────────┬──────────┘
             │
             ▼
    ┌───────────────────┐
    │ Publish Event to  │
    │ Kafka             │
    └────────┬──────────┘
             │
             ▼
    ┌───────────────────┐
    │ Notification Sent │
    │ to Candidate      │
    │ • Email with link │
    │ • Push notification│
    └────────┬──────────┘
             │
             ▼
    ┌───────────────────┐
    │ Update Job Apply  │
    │ Status: OFFERED   │
    └────────┬──────────┘
             │
             ▼
    ┌───────────────────┐
    │ Track Contract    │
    │ Status            │
    └────────┬──────────┘
             │
    ┌────────┴────────┬────────┬────────┐
    │                 │        │        │
    ▼                 ▼        ▼        ▼
┌────────┐  ┌────────────┐ ┌──────┐ ┌──────┐
│Pending │  │ Signed     │ │Expired│ │Declined│
└────┬───┘  └────┬───────┘ └──┬───┘ └──┬───┘
     │           │            │       │
     │           ▼            │       │
     │   ┌──────────────┐    │       │
     │   │ Start        │    │       │
     │   │ Verification │    │       │
     │   │ Process      │    │       │
     │   └──────┬───────┘    │       │
     │          │            │       │
     └──────────┴────────────┴───────┘
                │
                ▼
               END
```

---

## 7. Manage Calendar & Working Hours Activity

```
                    START
                      │
                      ▼
        ┌─────────────────────────┐
        │  Access Calendar        │
        │  Management             │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌───────────────┐   ┌───────────────┐
│Set Working    │   │ Request       │
│Hours          │   │ Time Off      │
└───────┬───────┘   └───────┬───────┘
        │                   │
        │                   ▼
        │           ┌───────────────┐
        │           │ Select Dates  │
        │           │ • Start Date  │
        │           │ • End Date    │
        │           └───────┬───────┘
        │                   │
        │                   ▼
        │           ┌───────────────┐
        │           │ Select Type   │
        │           │ • Vacation    │
        │           │ • Sick Leave  │
        │           │ • Business    │
        │           │ • Other       │
        │           └───────┬───────┘
        │                   │
        │                   ▼
        │           ┌───────────────┐
        │           │ Add Reason    │
        │           │ (Optional)    │
        │           └───────┬───────┘
        │                   │
        │                   ▼
        │           ┌───────────────┐
        │           │ Check for     │
        │           │ Conflicts     │
        │           │ (Scheduled    │
        │           │  Interviews)  │
        │           └───────┬───────┘
        │                   │
        │           ┌───────┴───────┐
        │           │               │
        │           ▼               ▼
        │    ┌──────────┐   ┌──────────┐
        │    │Conflicts │   │No Conflicts│
        │    │Found     │   │          │
        │    └────┬─────┘   └────┬─────┘
        │         │              │
        │         ▼              │
        │  ┌──────────────┐     │
        │  │ Show Warning │     │
        │  │ & Conflicts  │     │
        │  └──────┬───────┘     │
        │         │              │
        │         ▼              │
        │  ┌──────────────┐     │
        │  │ Reschedule   │     │
        │  │ Interviews?  │     │
        │  └──────┬───────┘     │
        │         │              │
        │  ┌──────┴──────┐      │
        │  │             │      │
        │  ▼             ▼      │
        │ Yes           No      │
        │  │             │      │
        │  │             ▼      │
        │  │      ┌──────────┐  │
        │  │      │  Cancel  │  │
        │  │      │  Request │  │
        │  │      └──────────┘  │
        │  │                    │
        │  ▼                    │
        │ (Auto reschedule      │
        │  interviews)          │
        │  │                    │
        │  └────────┬───────────┘
        │           │
        │           ▼
        │   ┌──────────────┐
        │   │ Submit       │
        │   │ Time Off     │
        │   └──────┬───────┘
        │          │
        │          ▼
        │   ┌──────────────┐
        │   │ Save to DB   │
        │   └──────┬───────┘
        │          │
        │          ▼
        │   ┌──────────────┐
        │   │ Block        │
        │   │ Calendar     │
        │   │ for Period   │
        │   └──────┬───────┘
        │          │
        └──────────┴───────────────┐
                   │                
                   ▼                
        ┌─────────────────────────┐
        │ Working Hours Setup     │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │ For Each Day of Week    │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │ Select Day              │
        │ (Monday - Sunday)       │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │ Set Status              │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
┌───────────────┐   ┌───────────────┐
│ Working Day   │   │ Day Off       │
└───────┬───────┘   └───────┬───────┘
        │                   │
        ▼                   │
┌───────────────┐           │
│ Set Hours     │           │
│ • Start Time  │           │
│ • End Time    │           │
│ • Break Time  │           │
└───────┬───────┘           │
        │                   │
        └─────────┬─────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │ More Days to Configure? │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
   ┌────────┐        ┌────────┐
   │  Yes   │        │   No   │
   └────┬───┘        └────┬───┘
        │                 │
        └────► (Loop)     │
                          ▼
                ┌───────────────┐
                │ Save Schedule │
                └───────┬───────┘
                        │
                        ▼
                ┌───────────────┐
                │ Update        │
                │ Availability  │
                │ in System     │
                └───────┬───────┘
                        │
                        ▼
                ┌───────────────┐
                │ Confirmation  │
                │ Message       │
                └───────┬───────┘
                        │
                        ▼
                       END
```

---

## 8. Purchase Package Activity

```
                    START
                      │
                      ▼
        ┌─────────────────────────┐
        │  View Current           │
        │  Entitlements           │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Check Usage:           │
        │  • Job Postings Used    │
        │  • Featured Jobs Used   │
        │  • Candidate Views Left │
        │  • Expiry Date          │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Need More Features?    │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
   ┌────────┐        ┌────────┐
   │  Yes   │        │   No   │
   └────┬───┘        └────┬───┘
        │                 │
        │                 ▼
        │                END
        │
        ▼
┌───────────────────┐
│ Browse Packages   │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ View Package Tiers│
│ • Basic           │
│ • Professional    │
│ • Enterprise      │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Compare Features  │
│ • Job Slots       │
│ • Featured Posts  │
│ • Candidate Views │
│ • Support Level   │
│ • Duration        │
│ • Price           │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Select Package    │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Review Summary    │
│ • Package Details │
│ • Total Cost      │
│ • Tax             │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Proceed to Payment│
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Create Invoice    │
│ Status: PENDING   │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Generate VNPay    │
│ Payment URL       │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Redirect to VNPay │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Enter Payment Info│
│ on VNPay          │
│ • Card Number     │
│ • Expiry          │
│ • CVV             │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ VNPay Processes   │
│ Payment           │
└────────┬──────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌────────┐
│Success │ │ Failed │
└────┬───┘ └────┬───┘
     │          │
     │          ▼
     │   ┌──────────────┐
     │   │ Show Error   │
     │   │ Message      │
     │   └──────┬───────┘
     │          │
     │          ▼
     │   ┌──────────────┐
     │   │ Retry?       │
     │   └──────┬───────┘
     │          │
     │   ┌──────┴──────┐
     │   │             │
     │   ▼             ▼
     │  Yes           No
     │   │             │
     │   └────► (Back to Payment)
     │                 │
     │                 ▼
     │           ┌──────────┐
     │           │  END     │
     │           └──────────┘
     │
     ▼
┌───────────────────┐
│ Return to App     │
│ (Callback URL)    │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Verify Payment    │
│ with VNPay        │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Update Invoice    │
│ Status: PAID      │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Create Entitlement│
│ with Features     │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Set Expiry Date   │
│ (Package Duration)│
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Publish Event to  │
│ Kafka             │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Send Confirmation │
│ • Email with      │
│   Invoice PDF     │
│ • Push Notification│
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Update Dashboard  │
│ with New Features │
└────────┬──────────┘
         │
         ▼
┌───────────────────┐
│ Show Success      │
│ Message           │
└────────┬──────────┘
         │
         ▼
        END
```

---

## 9. View Analytics & Reports Activity

```
                    START
                      │
                      ▼
        ┌─────────────────────────┐
        │  Access Dashboard       │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  View Overview Stats    │
        │  • Total Jobs Posted    │
        │  • Total Applications   │
        │  • Active Interviews    │
        │  • Hired Candidates     │
        │  • Package Usage        │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Select Report Type     │
        └─────────┬───────────────┘
                  │
        ┌─────────┼─────────┬─────────┐
        │         │         │         │
        ▼         ▼         ▼         ▼
┌──────────┐┌──────────┐┌──────────┐┌──────────┐
│Job       ││Application││Interview ││Hiring    │
│Analytics ││Analytics  ││Analytics ││Analytics │
└────┬─────┘└────┬─────┘└────┬─────┘└────┬─────┘
     │           │           │           │
     │           │           │           │
     ▼           ▼           ▼           ▼
┌──────────┐┌──────────┐┌──────────┐┌──────────┐
│• Views   ││• Count   ││• Scheduled││• Hired   │
│• Applies ││• Sources ││• Completed││• Time to │
│• Conversion││• Status ││• No-shows ││  Hire    │
│• Top Jobs││• Quality ││• Ratings  ││• Cost    │
└────┬─────┘└────┬─────┘└────┬─────┘└────┬─────┘
     │           │           │           │
     └───────────┴───────────┴───────────┘
                 │
                 ▼
        ┌─────────────────────────┐
        │  Select Time Period     │
        │  • Last 7 days          │
        │  • Last 30 days         │
        │  • Last 3 months        │
        │  • Custom Range         │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Apply Filters          │
        │  • By Job               │
        │  • By Department        │
        │  • By Location          │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  System Generates Report│
        │  from Database          │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Display Charts & Graphs│
        │  • Line Charts          │
        │  • Bar Charts           │
        │  • Pie Charts           │
        │  • Tables               │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  View Insights          │
        │  • Trends               │
        │  • Benchmarks           │
        │  • AI Recommendations   │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Export Options?        │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
        ▼                   ▼
   ┌────────┐        ┌────────┐
   │  Yes   │        │   No   │
   └────┬───┘        └────┬───┘
        │                 │
        ▼                 ▼
┌───────────────┐        END
│ Select Format │
│ • PDF         │
│ • Excel       │
│ • CSV         │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Generate File │
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Download      │
└───────┬───────┘
        │
        ▼
       END
```

---

## 10. Handle Dispute Activity

```
                    START
                      │
                      ▼
        ┌─────────────────────────┐
        │  Receive Dispute        │
        │  Notification           │
        │  (From Candidate)       │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  View Dispute Details   │
        │  • Candidate Claim      │
        │  • Contract Details     │
        │  • Employment Status    │
        │  • Evidence Submitted   │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Review Dispute Reason  │
        │  • Status Mismatch      │
        │  • Contract Terms       │
        │  • Payment Issue        │
        │  • Termination          │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Access Dispute         │
        │  Resolution Panel       │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Choose Response        │
        └─────────┬───────────────┘
                  │
        ┌─────────┼─────────┬─────────┐
        │         │         │         │
        ▼         ▼         ▼         ▼
┌──────────┐┌──────────┐┌──────────┐┌──────────┐
│  Accept  ││Partially ││ Reject   ││  Need    │
│  Claim   ││ Accept   ││          ││  More    │
│          ││          ││          ││  Info    │
└────┬─────┘└────┬─────┘└────┬─────┘└────┬─────┘
     │           │           │           │
     │           │           │           ▼
     │           │           │    ┌──────────────┐
     │           │           │    │ Request      │
     │           │           │    │ Additional   │
     │           │           │    │ Evidence     │
     │           │           │    └──────┬───────┘
     │           │           │           │
     │           │           │           ▼
     │           │           │    ┌──────────────┐
     │           │           │    │ Wait for     │
     │           │           │    │ Response     │
     │           │           │    └──────┬───────┘
     │           │           │           │
     │           │           │           │
     └───────────┴───────────┴───────────┘
                 │
                 ▼
        ┌─────────────────────────┐
        │  Upload Evidence        │
        │  • Documents            │
        │  • Emails               │
        │  • Screenshots          │
        │  • Communication Logs   │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Write Response         │
        │  • Explanation          │
        │  • Company Position     │
        │  • Proposed Resolution  │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Propose Solution       │
        │  • Correct Status       │
        │  • Compensation         │
        │  • Contract Amendment   │
        │  • Mediation            │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Submit Response        │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  System Notifies        │
        │  • Candidate            │
        │  • Admin (for review)   │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Admin Reviews          │
        │  Both Parties' Evidence │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Admin Makes Decision   │
        └─────────┬───────────────┘
                  │
        ┌─────────┴─────────┬─────────┐
        │                   │         │
        ▼                   ▼         ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ In Favor of  │  │ In Favor of  │  │  Requires    │
│ Recruiter    │  │ Candidate    │  │  Mediation   │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │
       │                 │                 ▼
       │                 │          ┌──────────────┐
       │                 │          │ Schedule     │
       │                 │          │ Mediation    │
       │                 │          │ Session      │
       │                 │          └──────┬───────┘
       │                 │                 │
       └─────────┬───────┴─────────────────┘
                 │
                 ▼
        ┌─────────────────────────┐
        │  Receive Notification   │
        │  of Decision            │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Execute Resolution     │
        │  • Update Status        │
        │  • Process Payment      │
        │  • Amend Contract       │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Update Records         │
        │  in Database            │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Dispute Resolved       │
        │  Status: CLOSED         │
        └─────────┬───────────────┘
                  │
                  ▼
        ┌─────────────────────────┐
        │  Add to Audit Log       │
        └─────────┬───────────────┘
                  │
                  ▼
                 END
```

---

## Summary

These activity diagrams cover the main workflows for Recruiter role in CareerMate:

1. **Registration & Onboarding** - Complete company profile setup and verification
2. **Post Job** - Create and publish job postings with AI assistance
3. **Review Applications** - Filter and evaluate candidate applications
4. **Schedule Interview** - Coordinate interview times with calendar management
5. **Conduct Interview** - Execute interviews and provide evaluations
6. **Send Contract** - Generate and send employment contracts
7. **Manage Calendar** - Set working hours and time off
8. **Purchase Package** - Upgrade features through payment gateway
9. **View Analytics** - Monitor recruitment metrics and performance
10. **Handle Dispute** - Resolve employment verification disputes

## Key Features Highlighted

✅ **AI-Powered Assistance**: Job validation, candidate matching, interview questions  
✅ **Calendar Integration**: Conflict detection, availability management  
✅ **Multi-Channel Notifications**: Email, Push, SMS via Kafka  
✅ **Payment Integration**: VNPay gateway for package purchases  
✅ **Dispute Resolution**: Structured evidence-based process  
✅ **Analytics Dashboard**: Real-time metrics and insights  
✅ **Document Management**: Firebase storage for contracts and files  
✅ **Status Tracking**: Complete audit trail for all activities  

---

**Related Documentation**:
- [Context Diagram](CONTEXT_DIAGRAM.md)
- [API Endpoints](API_ENDPOINTS_REFERENCE.md)
- [Data Flow Diagrams](DATA_FLOW_DIAGRAMS.md)
- [Architecture Index](ARCHITECTURE_INDEX.md)

