# CareerMate - Data Flow Diagrams

## 1. Job Application Flow

```
┌──────────┐
│Candidate │
└────┬─────┘
     │
     │ 1. Browse Jobs (GET /api/jobs)
     ▼
┌────────────────┐         ┌──────────────┐
│  Job Service   │────────►│  PostgreSQL  │
└────────────────┘         └──────────────┘
     │                           ▲
     │ 2. View Job Details       │
     │    (GET /api/jobs/{id})   │
     ▼                           │
┌────────────────┐               │
│ Recommendation │               │
│    Service     │───────────────┘
│  (AI Powered)  │         Fetch job data
└────────────────┘
     │
     │ 3. Get AI Recommendations
     │    (GET /api/recommendations/jobs)
     ▼
┌────────────────┐         ┌──────────────┐
│   Weaviate     │◄────────│ Gemini AI    │
│  Vector DB     │         │              │
└────────────────┘         └──────────────┘
     │
     │ Semantic matching results
     ▼
┌──────────┐
│Candidate │
└────┬─────┘
     │
     │ 4. Apply for Job (POST /api/job-applies)
     ▼
┌────────────────┐         ┌──────────────┐
│  Job Service   │────────►│  PostgreSQL  │
└────┬───────────┘         └──────────────┘
     │                     Create JobApply record
     │
     │ 5. Trigger Notification Event
     ▼
┌────────────────┐         ┌──────────────┐
│     Kafka      │────────►│Notification  │
│    Producer    │         │   Consumer   │
└────────────────┘         └──────┬───────┘
                                  │
                                  │ 6. Send Notifications
                                  ▼
                          ┌───────────────────┐
                          │  Multi-Channel    │
                          │  • FCM Push       │
                          │  • Email (SMTP)   │
                          │  • SSE Stream     │
                          └───────────────────┘
                                  │
                                  ▼
                          ┌──────────────┐
                          │  Recruiter   │
                          └──────────────┘
```

---

## 2. Interview Scheduling Flow

```
┌──────────┐
│Recruiter │
└────┬─────┘
     │
     │ 1. Review Applications (GET /api/job-applies/job/{jobId})
     ▼
┌────────────────┐         ┌──────────────┐
│  Job Service   │◄───────►│  PostgreSQL  │
└────────────────┘         └──────────────┘
     │
     │ 2. Check Calendar Availability
     │    (GET /api/calendar/availability)
     ▼
┌────────────────┐         ┌──────────────┐
│   Calendar     │◄───────►│  PostgreSQL  │
│    Service     │         │              │
└────────────────┘         │ • Working    │
     │                     │   Hours      │
     │                     │ • Time Off   │
     ▼                     │ • Existing   │
Check conflicts           │   Interviews │
     │                     └──────────────┘
     │ 3. Schedule Interview
     │    (POST /api/interviews/schedule)
     ▼
┌────────────────┐         ┌──────────────┐
│  Interview     │────────►│  PostgreSQL  │
│    Service     │         └──────────────┘
└────┬───────────┘         Create InterviewSchedule
     │
     │ 4. Publish Event
     ▼
┌────────────────┐
│  Kafka Topic   │
│  • recruiter   │
│  • candidate   │
└────┬───────────┘
     │
     │ 5. Consume Event
     ▼
┌────────────────┐
│ Notification   │
│    Service     │
└────┬───────────┘
     │
     │ 6. Send Multi-channel Notifications
     ├─────────────────┬─────────────────┐
     ▼                 ▼                 ▼
┌─────────┐      ┌─────────┐      ┌─────────┐
│   FCM   │      │  SMTP   │      │   SSE   │
└─────────┘      └─────────┘      └─────────┘
     │                 │                 │
     └─────────────────┴─────────────────┘
                       │
                       ▼
                 ┌──────────┐
                 │Candidate │
                 └────┬─────┘
                      │
                      │ 7. Confirm/Reschedule
                      │    (POST /api/interviews/{id}/confirm)
                      ▼
                 ┌────────────────┐
                 │  Interview     │
                 │    Service     │
                 └────────────────┘
```

---

## 3. Employment Contract Flow

```
┌──────────┐
│Recruiter │
└────┬─────┘
     │
     │ 1. Create Contract (POST /api/employment-contracts/create)
     ▼
┌────────────────┐         ┌──────────────┐
│   Contract     │────────►│  PostgreSQL  │
│    Service     │         └──────────────┘
└────┬───────────┘         Create EmploymentVerification
     │
     │ 2. Generate Contract Document
     ▼
┌────────────────┐         ┌──────────────┐
│   Template     │────────►│   Firebase   │
│    Engine      │         │   Storage    │
└────────────────┘         └──────────────┘
     │                     Upload contract PDF
     │
     │ 3. Send to Candidate (POST /api/employment-contracts/{id}/send)
     ▼
┌────────────────┐
│     Kafka      │
└────┬───────────┘
     │
     │ 4. Notification
     ▼
┌────────────────┐         ┌──────────────┐
│ Notification   │────────►│  Candidate   │
│    Service     │         └──────┬───────┘
└────────────────┘                │
                                  │
                                  │ 5. Review & Sign
                                  │    (POST /api/employment-contracts/{id}/sign)
                                  ▼
                          ┌────────────────┐
                          │   Contract     │
                          │    Service     │
                          └────┬───────────┘
                               │
                               │ 6. Update Status
                               ▼
                          ┌──────────────┐
                          │  PostgreSQL  │
                          │              │
                          │ Status:      │
                          │ SIGNED       │
                          └──────────────┘
                               │
                               │ 7. Notify Both Parties
                               ▼
                          ┌────────────────┐
                          │     Kafka      │
                          └────┬───────────┘
                               │
                    ┌──────────┴──────────┐
                    ▼                     ▼
              ┌──────────┐          ┌──────────┐
              │Candidate │          │Recruiter │
              └──────────┘          └──────────┘
```

---

## 4. Payment Processing Flow

```
┌──────────┐
│  User    │
│(Candidate│
│/Recruiter)│
└────┬─────┘
     │
     │ 1. View Packages (GET /api/packages)
     ▼
┌────────────────┐         ┌──────────────┐
│   Package      │◄───────►│  PostgreSQL  │
│   Service      │         └──────────────┘
└────────────────┘
     │
     │ 2. Select Package & Create Payment
     │    (POST /api/candidate-payment/create)
     ▼
┌────────────────┐         ┌──────────────┐
│   Payment      │────────►│  PostgreSQL  │
│   Service      │         └──────────────┘
└────┬───────────┘         Create Invoice (PENDING)
     │
     │ 3. Generate Payment URL
     ▼
┌────────────────┐
│    VNPay       │
│   Gateway      │
└────┬───────────┘
     │
     │ 4. Redirect to Payment Page
     ▼
┌────────────────┐
│   User         │
│  (Browser)     │
└────┬───────────┘
     │
     │ 5. Complete Payment on VNPay
     ▼
┌────────────────┐
│    VNPay       │
└────┬───────────┘
     │
     │ 6. Payment Callback (GET /api/candidate-payment/return)
     ▼
┌────────────────┐         ┌──────────────┐
│   Payment      │────────►│  PostgreSQL  │
│   Service      │         └──────────────┘
└────┬───────────┘         Update Invoice (PAID)
     │                     Create Entitlement
     │
     │ 7. Activate Package Features
     ▼
┌────────────────┐
│  Entitlement   │
│    Service     │
└────┬───────────┘
     │
     │ 8. Send Confirmation
     ▼
┌────────────────┐         ┌──────────────┐
│     Kafka      │────────►│ Notification │
└────────────────┘         │   Service    │
                           └──────┬───────┘
                                  │
                                  │ 9. Email Receipt & SMS
                                  ▼
                           ┌──────────────┐
                           │     User     │
                           └──────────────┘
```

---

## 5. Company Review Flow

```
┌──────────┐
│Candidate │
└────┬─────┘
     │
     │ 1. Check Eligibility
     │    - Must have completed employment with company
     │    - Verified employment record
     ▼
┌────────────────┐         ┌──────────────┐
│   Review       │◄───────►│  PostgreSQL  │
│   Service      │         └──────────────┘
└────┬───────────┘         Verify EmploymentVerification
     │
     │ Eligible ✓
     │
     │ 2. Submit Review (POST /api/v1/reviews)
     ▼
┌────────────────┐         ┌──────────────┐
│   Review       │────────►│  PostgreSQL  │
│   Service      │         └──────────────┘
└────┬───────────┘         Create CompanyReview
     │                     Status: PENDING
     │
     │ 3. Auto-moderation Check
     ▼
┌────────────────┐
│  Content       │
│  Filter        │
│  (Gemini AI)   │
└────┬───────────┘
     │
     ├────► Approved ────┐
     │                   │
     └────► Flagged ─────┤
                         ▼
                    ┌────────────────┐
                    │  Admin Queue   │
                    └────┬───────────┘
                         │
                         │ Admin Review
                         ▼
                    ┌────────────────┐
                    │     Admin      │
                    └────┬───────────┘
                         │
                         │ Approve/Reject
                         ▼
┌────────────────┐         ┌──────────────┐
│   Review       │────────►│  PostgreSQL  │
│   Service      │         └──────────────┘
└────┬───────────┘         Update Status: APPROVED
     │
     │ 4. Update Statistics
     ▼
┌────────────────┐         ┌──────────────┐
│  Statistics    │────────►│    Redis     │
│   Calculator   │         └──────────────┘
└────────────────┘         Cache aggregated stats
     │
     │ 5. Notify Company
     ▼
┌────────────────┐         ┌──────────────┐
│     Kafka      │────────►│  Recruiter   │
└────────────────┘         └──────────────┘
```

---

## 6. AI-Powered Job Recommendation Flow

```
┌──────────┐
│Candidate │
└────┬─────┘
     │
     │ 1. Request Recommendations
     │    (GET /api/recommendations/jobs)
     ▼
┌────────────────┐         ┌──────────────┐
│Recommendation  │────────►│  PostgreSQL  │
│   Service      │         └──────────────┘
└────┬───────────┘         Fetch candidate profile
     │                     • Skills
     │                     • Experience
     │                     • Preferences
     │
     │ 2. Generate Embedding
     ▼
┌────────────────┐
│   Gemini AI    │
│  (Text2Vec)    │
└────┬───────────┘
     │
     │ Vector representation
     │ of candidate profile
     ▼
┌────────────────┐         ┌──────────────┐
│   Weaviate     │◄───────►│  PostgreSQL  │
│  Vector DB     │         └──────────────┘
└────┬───────────┘         Job postings data
     │
     │ 3. Semantic Search
     │    Cosine similarity matching
     ▼
Ranked job matches
     │
     │ 4. Apply Business Rules
     ▼
┌────────────────┐
│  Filter &      │
│  Rank Engine   │
└────┬───────────┘
     │
     │ • Location match
     │ • Salary range
     │ • Work model preference
     │ • Industry experience
     │ • Skill requirements
     ▼
Top N recommendations
     │
     │ 5. Cache Results
     ▼
┌────────────────┐
│     Redis      │
└────────────────┘
     │
     │ TTL: 1 hour
     ▼
┌────────────────┐
│   Response     │
│   to User      │
└────────────────┘
```

---

## 7. Real-time Notification Flow

```
┌──────────────────────────────────────────────────┐
│              EVENT SOURCES                       │
├──────────────────────────────────────────────────┤
│  • Job Application                               │
│  • Interview Schedule                            │
│  • Contract Signing                              │
│  • Payment Success                               │
│  • Status Update                                 │
│  • Review Posted                                 │
└────┬─────────────────────────────────────────────┘
     │
     │ Emit Event
     ▼
┌────────────────┐
│     Kafka      │
│   Producer     │
└────┬───────────┘
     │
     │ Publish to Topic
     ├────────────────────┬────────────────────┐
     ▼                    ▼                    ▼
┌────────────┐    ┌────────────┐    ┌────────────┐
│  admin-    │    │ recruiter- │    │ candidate- │
│notifications│   │notifications│   │notifications│
│ (3 parts)  │    │ (3 parts)  │    │ (3 parts)  │
└────┬───────┘    └────┬───────┘    └────┬───────┘
     │                 │                 │
     └─────────────────┴─────────────────┘
                       │
                       │ Consume from Partitions
                       ▼
              ┌────────────────┐
              │  Notification  │
              │    Consumer    │
              │   (3 instances)│
              └────┬───────────┘
                   │
                   │ Process Event
                   ▼
              ┌────────────────┐         ┌──────────────┐
              │ Notification   │────────►│  PostgreSQL  │
              │   Service      │         └──────────────┘
              └────┬───────────┘         Save notification
                   │
                   │ Determine Channels
                   │
      ┌────────────┼────────────┬─────────────┐
      │            │            │             │
      ▼            ▼            ▼             ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│   FCM    │ │  Email   │ │   SSE    │ │  Redis   │
│  Push    │ │  (SMTP)  │ │ Stream   │ │  Cache   │
└────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘
     │            │            │             │
     │            │            │             │
     └────────────┴────────────┴─────────────┘
                               │
                               ▼
                         ┌──────────┐
                         │   User   │
                         │ Receives │
                         │  in      │
                         │ Real-time│
                         └──────────┘
```

---

## 8. Authentication & Authorization Flow

```
┌──────────┐
│   User   │
└────┬─────┘
     │
     │ Option A: Standard Login
     │ (POST /api/auth/login)
     │
     ▼
┌────────────────┐         ┌──────────────┐
│Authentication  │◄───────►│  PostgreSQL  │
│   Service      │         └──────────────┘
└────┬───────────┘         Verify credentials
     │
     │ Option B: OAuth2 Login
     │ (GET /api/oauth2/google)
     │
     ▼
┌────────────────┐         ┌──────────────┐
│   Google       │────────►│ OAuth2       │
│   OAuth2       │         │  Callback    │
└────────────────┘         └──────┬───────┘
                                  │
                                  │ Exchange code
                                  ▼
                          ┌────────────────┐
                          │Authentication  │
                          │   Service      │
                          └────┬───────────┘
                               │
                               │ Generate JWT
                               ▼
                          ┌────────────────┐
                          │   JWT Token    │
                          │   Generator    │
                          └────┬───────────┘
                               │
                               │ • Access Token (15 min)
                               │ • Refresh Token (7 days)
                               ▼
                          ┌────────────────┐
                          │     Redis      │
                          └────────────────┘
                               │ Cache token
                               │
                               ▼
                          ┌────────────────┐
                          │   Response     │
                          │   to User      │
                          └────────────────┘
                               │
                               │
┌──────────┐                   │
│   User   │◄──────────────────┘
└────┬─────┘
     │
     │ Subsequent Requests
     │ (Authorization: Bearer <token>)
     ▼
┌────────────────┐         ┌──────────────┐
│   Security     │◄───────►│    Redis     │
│   Filter       │         └──────────────┘
└────┬───────────┘         Verify token
     │
     │ Token valid?
     ├────► Yes ────┐
     │              │
     └────► No ─────┤
                    ▼
            ┌────────────────┐
            │  Check Redis   │
            │  Blacklist     │
            └────┬───────────┘
                 │
                 │ Not blacklisted?
                 ├────► Yes ────┐
                 │              │
                 └────► No ─────┤
                                ▼
                        ┌────────────────┐
                        │  Decode JWT    │
                        └────┬───────────┘
                             │
                             │ Extract claims
                             ▼
                        ┌────────────────┐
                        │  Load User     │
                        │  & Permissions │
                        └────┬───────────┘
                             │
                             │ Check authorization
                             ▼
                        ┌────────────────┐
                        │  Access        │
                        │  Granted       │
                        └────────────────┘
```

---

## 9. File Upload Flow

```
┌──────────┐
│   User   │
└────┬─────┘
     │
     │ 1. Upload File (POST /api/resumes/{id}/upload)
     │    Content-Type: multipart/form-data
     ▼
┌────────────────┐
│   REST API     │
└────┬───────────┘
     │
     │ 2. Validate File
     │    • Type (PDF, DOCX, JPG, PNG)
     │    • Size (Max 10MB)
     │    • Virus scan
     ▼
┌────────────────┐
│  File Service  │
└────┬───────────┘
     │
     │ 3. Generate unique filename
     │    UUID + original extension
     ▼
┌────────────────┐         ┌──────────────┐
│   Firebase     │◄────────│  Firebase    │
│   SDK          │         │   Config     │
└────┬───────────┘         └──────────────┘
     │
     │ 4. Upload to Cloud Storage
     │    gs://careermate-97d8c.firebasestorage.app/
     ▼
┌────────────────┐
│   Firebase     │
│   Storage      │
└────┬───────────┘
     │
     │ 5. Get Download URL
     ▼
Public URL
     │
     │ 6. Save metadata
     ▼
┌────────────────┐         ┌──────────────┐
│  File Service  │────────►│  PostgreSQL  │
└────────────────┘         └──────────────┘
     │                     • filename
     │                     • url
     │                     • size
     │                     • type
     │                     • uploaded_at
     │
     │ 7. Optional: AI Processing
     ▼
┌────────────────┐
│   Gemini AI    │
│  (CV Parser)   │
└────┬───────────┘
     │
     │ Extract: skills, experience, education
     ▼
┌────────────────┐         ┌──────────────┐
│  Resume        │────────►│  PostgreSQL  │
│  Service       │         └──────────────┘
└────────────────┘         Update candidate profile
     │
     │ 8. Response with URL
     ▼
┌────────────────┐
│     User       │
└────────────────┘
```

---

## 10. Cache Strategy Flow

```
┌──────────┐
│   User   │
└────┬─────┘
     │
     │ 1. Request Data (GET /api/jobs)
     ▼
┌────────────────┐
│   REST API     │
└────┬───────────┘
     │
     │ 2. Check Cache
     ▼
┌────────────────┐
│     Redis      │
└────┬───────────┘
     │
     ├────► Cache Hit ────┐
     │                    │
     └────► Cache Miss ───┤
                          │
                          ▼ No cache
                    ┌────────────────┐
                    │   Service      │
                    └────┬───────────┘
                         │
                         │ 3. Query Database
                         ▼
                    ┌────────────────┐
                    │  PostgreSQL    │
                    └────┬───────────┘
                         │
                         │ 4. Fetch Data
                         ▼
                    ┌────────────────┐
                    │   Service      │
                    └────┬───────────┘
                         │
                         │ 5. Store in Cache
                         │    TTL: depends on data type
                         │    • Job listings: 5 min
                         │    • User profiles: 15 min
                         │    • Recommendations: 1 hour
                         ▼
                    ┌────────────────┐
                    │     Redis      │
                    └────┬───────────┘
                         │
                         └────────────┐
                                      │
                                      ▼ Cache available
                                ┌────────────────┐
                                │   Response     │
                                │   to User      │
                                └────────────────┘

Cache Invalidation Strategy:

┌────────────────┐
│  Write/Update  │
│  Operation     │
└────┬───────────┘
     │
     │ 1. Update Database
     ▼
┌────────────────┐         ┌──────────────┐
│   Service      │────────►│  PostgreSQL  │
└────┬───────────┘         └──────────────┘
     │
     │ 2. Invalidate Cache
     ▼
┌────────────────┐
│     Redis      │
│   DEL key      │
└────────────────┘
     │
     │ Next read will refresh cache
     ▼
```

---

## Key Data Flow Patterns

### 1. **Synchronous Request-Response**
- User → API → Service → Database → Response
- Used for: CRUD operations, queries
- Latency: <500ms

### 2. **Asynchronous Event-Driven**
- Service → Kafka → Consumer → Action
- Used for: Notifications, background tasks
- Eventual consistency

### 3. **Cache-Aside Pattern**
- Check cache → Miss → Database → Update cache
- Used for: Frequently accessed data
- TTL-based expiration

### 4. **AI-Enhanced Processing**
- User input → Service → AI API → Process → Response
- Used for: Recommendations, content moderation
- Latency: 1-3s

### 5. **File Upload Pattern**
- Client → API → Cloud Storage → Metadata DB
- Used for: Documents, images, resumes
- Async processing for large files

---

## Performance Optimization

### Database Queries
- **Indexing**: On frequently queried fields
- **Pagination**: Limit result sets
- **Eager/Lazy Loading**: Optimize JPA queries

### Caching Strategy
- **Redis**: Hot data, sessions, tokens
- **Application Cache**: Static configuration
- **CDN**: Static assets (future)

### Async Processing
- **Kafka**: Decouple heavy operations
- **Background Jobs**: Email sending, report generation
- **Batch Processing**: Nightly statistics updates

---

## Monitoring Data Flows

### Metrics to Track
- API response times
- Kafka lag
- Cache hit ratio
- Database query performance
- External API latencies
- File upload success rate

### Logging Strategy
- Request/Response logs
- Error tracking
- Event processing logs
- Audit trails for sensitive operations

