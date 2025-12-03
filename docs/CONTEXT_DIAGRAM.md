# CareerMate System - Context Diagram

## System Overview
**CareerMate** là một nền tảng tuyển dụng và phát triển nghề nghiệp toàn diện, kết nối ứng viên với nhà tuyển dụng, cung cấp các tính năng quản lý tuyển dụng, phỏng vấn, hợp đồng lao động, đánh giá công ty và hướng dẫn nghề nghiệp.

---

## Context Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          EXTERNAL ACTORS & SYSTEMS                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐       ┌──────────────┐       ┌──────────────┐           │
│  │   Candidate  │       │  Recruiter   │       │    Admin     │           │
│  │  (Ứng viên)  │       │(Nhà tuyển dụng)│     │  (Quản trị)  │           │
│  └──────┬───────┘       └──────┬───────┘       └──────┬───────┘           │
│         │                      │                       │                   │
│         │  Apply jobs          │  Post jobs            │  Moderate         │
│         │  View interviews     │  Schedule interviews  │  Resolve disputes │
│         │  Sign contracts      │  Manage applicants    │  Approve content  │
│         │  Review companies    │  Review candidates    │  View analytics   │
│         │  Learn roadmaps      │  Purchase packages    │  Manage system    │
│         │                      │                       │                   │
│         └──────────────────────┼───────────────────────┘                   │
│                                │                                           │
└────────────────────────────────┼───────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                             │
│                        ┌─────────────────────────┐                         │
│                        │                         │                         │
│                        │   CareerMate System     │                         │
│                        │   (Spring Boot 3.x)     │                         │
│                        │                         │                         │
│                        │  • Authentication       │                         │
│                        │  • Job Management       │                         │
│                        │  • Interview Scheduling │                         │
│                        │  • Contract Management  │                         │
│                        │  • Review System        │                         │
│                        │  • Payment Processing   │                         │
│                        │  • Notification System  │                         │
│                        │  • Career Coaching      │                         │
│                        │  • AI Recommendations   │                         │
│                        │                         │                         │
│                        └───────────┬─────────────┘                         │
│                                    │                                       │
└────────────────────────────────────┼───────────────────────────────────────┘
                                     │
        ┌────────────────────────────┼────────────────────────────┐
        │                            │                            │
        ▼                            ▼                            ▼
┌───────────────┐          ┌──────────────────┐        ┌──────────────────┐
│   PostgreSQL  │          │  Apache Kafka    │        │  External APIs   │
│   Database    │          │  Message Broker  │        │                  │
│               │          │                  │        │  • Google OAuth2 │
│ • Users       │          │ • Notifications  │        │  • VNPay Payment │
│ • Jobs        │          │ • Events         │        │  • Gmail SMTP    │
│ • Interviews  │          │ • Async Tasks    │        │  • Firebase      │
│ • Contracts   │          │                  │        │  • Gemini AI     │
│ • Reviews     │          │ 3 Topics:        │        │  • Weaviate      │
│ • Payments    │          │ - admin          │        │  • HuggingFace   │
│               │          │ - recruiter      │        └──────────────────┘
└───────────────┘          │ - candidate      │
                           └──────────────────┘
        │                            │                            │
        ▼                            ▼                            ▼
┌───────────────┐          ┌──────────────────┐        ┌──────────────────┐
│     Redis     │          │    Zookeeper     │        │  Firebase        │
│     Cache     │          │  (Kafka Manager) │        │  Storage         │
│               │          │                  │        │                  │
│ • Sessions    │          │ • Coordination   │        │ • File uploads   │
│ • Tokens      │          │ • Configuration  │        │ • Documents      │
│ • Cache data  │          │                  │        │ • Images         │
└───────────────┘          └──────────────────┘        │ • Resumes        │
                                                        └──────────────────┘
```

---

## System Boundaries

### Internal System (CareerMate Backend)
**Technology**: Spring Boot 3.x, Java 21, Maven
**Base URL**: `http://localhost:8080`
**API Documentation**: Swagger/OpenAPI 3.0 at `/swagger-ui.html`

### Main Components:
1. **Authentication Services** - OAuth2, JWT, Role-based access
2. **Job Services** - Job postings, applications, saved jobs
3. **Interview Services** - Scheduling, sessions, AI-powered interviews
4. **Employment Services** - Contracts, verifications, status tracking
5. **Review Services** - Company reviews, ratings, feedback
6. **Order Services** - Packages, invoices, entitlements
7. **Payment Services** - VNPay integration, transactions
8. **Notification Services** - Kafka-based async notifications, SSE
9. **Profile Services** - Candidate/Recruiter profiles, skills
10. **Resume Services** - CV management, work experience, education
11. **Blog Services** - Content management, comments, ratings
12. **Coach Services** - Career roadmaps, topics, courses
13. **Admin Services** - Dashboard, moderation, analytics
14. **Recommendation** - AI-powered job/candidate matching

---

## External Actors

### 1. Candidate (Ứng viên)
**Role**: Job seeker
**Capabilities**:
- Tìm kiếm và ứng tuyển công việc
- Quản lý hồ sơ và CV
- Tham gia phỏng vấn
- Ký hợp đồng lao động
- Đánh giá công ty
- Học các lộ trình nghề nghiệp
- Mua gói dịch vụ premium
- Nhận thông báo real-time

### 2. Recruiter (Nhà tuyển dụng)
**Role**: Employer/HR Manager
**Capabilities**:
- Đăng tin tuyển dụng
- Lập lịch phỏng vấn
- Quản lý ứng viên
- Gửi hợp đồng lao động
- Đánh giá ứng viên
- Quản lý lịch làm việc
- Mua gói dịch vụ
- Xem phân tích và báo cáo

### 3. Admin (Quản trị viên)
**Role**: System administrator
**Capabilities**:
- Kiểm duyệt nội dung
- Giải quyết tranh chấp
- Quản lý người dùng
- Xem dashboard và analytics
- Cấu hình hệ thống
- Quản lý thanh toán

---

## External Systems

### 1. PostgreSQL Database
**Purpose**: Primary data store
**Contains**:
- User accounts (candidates, recruiters, admins)
- Job postings and applications
- Interview schedules and sessions
- Employment contracts and verifications
- Company reviews and ratings
- Payment invoices and entitlements
- Notifications and device tokens
- Roadmaps and learning content

### 2. Apache Kafka (Message Broker)
**Purpose**: Asynchronous event processing and notifications
**Configuration**:
- Bootstrap Servers: `localhost:9092`
- 3 Main Topics:
  - `admin-notifications` (3 partitions)
  - `recruiter-notifications` (3 partitions)
  - `candidate-notifications` (3 partitions)
**Use Cases**:
- Real-time notifications
- Event-driven workflows
- Async task processing
- System integration events

### 3. Redis Cache
**Purpose**: In-memory caching and session management
**Port**: `6379`
**Use Cases**:
- Session storage
- JWT token blacklist
- API rate limiting
- Temporary data cache

### 4. Zookeeper
**Purpose**: Kafka coordination and management
**Port**: `2181`
**Function**: Manages Kafka cluster state and configuration

### 5. Google OAuth2
**Purpose**: Third-party authentication
**Integration**: OAuth2 Resource Server
**Scopes**: profile, email
**Use Case**: Social login for candidates and recruiters

### 6. VNPay Payment Gateway
**Purpose**: Payment processing
**Endpoints**:
- Payment URL: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`
- API URL: `https://sandbox.vnpayment.vn/merchant_webapi/api/transaction`
**Use Case**: Package purchases for candidates and recruiters

### 7. Gmail SMTP
**Purpose**: Email notifications
**Host**: `smtp.gmail.com:587`
**Use Cases**:
- Welcome emails
- Interview reminders
- Contract notifications
- Password reset
- System alerts

### 8. Firebase
**Purpose**: File storage and push notifications
**Services**:
- Firebase Storage (bucket: `careermate-97d8c.firebasestorage.app`)
- Firebase Cloud Messaging (FCM)
**Use Cases**:
- Resume/document uploads
- Profile images
- Company logos
- Push notifications to mobile devices

### 9. Google Gemini AI
**Purpose**: AI-powered features
**Model**: `gemini-2.0-flash`
**Base URL**: `https://generativelanguage.googleapis.com`
**Use Cases**:
- Interview question generation
- CV analysis and recommendations
- Job-candidate matching
- Career advice chatbot

### 10. Weaviate Vector Database
**Purpose**: Semantic search and AI recommendations
**Vectorizer**: `text2vec-weaviate`
**Use Cases**:
- Job search with semantic understanding
- Candidate-job similarity matching
- Skill-based recommendations
- Content similarity analysis

### 11. HuggingFace
**Purpose**: ML model hosting and inference
**Use Cases**:
- Text embeddings generation
- NLP tasks
- Supporting Weaviate vectorization

---

## Data Flow Patterns

### 1. Synchronous (Request-Response)
```
User → REST API → Service Layer → Repository → PostgreSQL
                    ↓
                  Cache (Redis)
                    ↓
                  Response
```

### 2. Asynchronous (Event-Driven)
```
Service → Kafka Producer → Kafka Topic → Kafka Consumer → Notification Service
                                              ↓
                                        FCM/Email/SSE
```

### 3. AI-Enhanced Flow
```
User Request → Service → Gemini AI / Weaviate → Process Result → Response
                  ↓
            Cache frequently used results
```

---

## Security Architecture

### Authentication Methods
1. **JWT-based**: Custom username/password authentication
2. **OAuth2**: Google social login
3. **Role-based Access Control (RBAC)**: Admin, Recruiter, Candidate

### Security Features
- JWT token validation (900s validity, 7-day refresh)
- Password encryption
- Token blacklisting via Redis
- OAuth2 Resource Server
- CORS configuration
- Request validation

---

## API Architecture

### REST API Structure
- **Base Path**: `/api/`
- **Versioning**: Some endpoints use `/v1/` prefix
- **Documentation**: Swagger UI at `/swagger-ui.html`
- **API Docs JSON**: `/v3/api-docs`

### Main API Modules

#### Authentication (`/api/auth/*`)
- Registration
- Login
- OAuth2 callback
- Token refresh
- Password reset

#### Jobs (`/api/jobs/*`, `/api/job-applies/*`)
- Job posting CRUD
- Job application
- Saved jobs
- Job search and filtering

#### Interviews (`/api/interviews/*`, `/api/interview-schedule/*`)
- Schedule creation
- Interview confirmation
- Reschedule/cancel
- AI interview sessions
- Calendar integration

#### Contracts (`/api/employment-contracts/*`, `/api/employment-verification/*`)
- Contract generation
- Digital signature
- Status verification
- Dispute resolution

#### Reviews (`/api/v1/reviews/*`)
- Company reviews
- Rating statistics
- Review moderation

#### Payments (`/api/candidate-payment/*`, `/api/recruiter-payment/*`)
- VNPay integration
- Package purchase
- Invoice management

#### Notifications (`/api/notifications/*`)
- SSE streaming
- Device token management
- Notification history

#### Profiles (`/api/candidates/*`, `/api/recruiters/*`)
- Profile management
- Skill updates
- Work history

#### Resumes (`/api/resumes/*`)
- CV upload/download
- Education history
- Work experience
- Skills and certificates

#### Roadmaps (`/api/roadmaps/*`, `/api/coach/*`)
- Career guidance
- Learning paths
- Course recommendations

#### Admin (`/api/admin/*`)
- Dashboard analytics
- Content moderation
- User management
- Dispute resolution

---

## Deployment Architecture

### Container Services (Docker Compose)
```yaml
Services:
  - postgres:17-alpine (Port: 5439)
  - redis:7.2-alpine (Port: 6379)
  - redisinsight (Port: 5540)
  - zookeeper:7.5.0 (Port: 2181)
  - kafka:7.5.0 (Ports: 9092, 9093)
  - kafka-ui (Port: 8090)
```

### Application
- **Runtime**: Java 21
- **Framework**: Spring Boot 3.5.6
- **Build**: Maven
- **Port**: 8080
- **Time Zone**: Asia/Ho_Chi_Minh

---

## Key Features Summary

### 🎯 Core Recruitment Features
- **Job Posting & Search**: Full-text search, filters, saved jobs
- **Application Management**: Track status, history, feedback
- **Interview Scheduling**: Multi-round, calendar sync, reminders
- **AI Interviews**: Automated question generation, evaluation

### 📝 Contract & Verification
- **Digital Contracts**: E-signature, lifecycle tracking
- **Employment Verification**: Bilateral status confirmation
- **Dispute Resolution**: Admin arbitration, evidence management

### ⭐ Review & Rating System
- **Company Reviews**: Anonymous, moderated, verified
- **Rating Statistics**: Aggregated scores, trends
- **Review Types**: Work environment, salary, management

### 💰 Payment & Packages
- **VNPay Integration**: Secure payment gateway
- **Package Management**: Candidate & recruiter tiers
- **Invoice Tracking**: Payment history, refunds
- **Entitlements**: Feature access control

### 🔔 Real-time Notifications
- **Kafka-based**: Scalable, partitioned topics
- **Multi-channel**: FCM push, email, SSE
- **Priority Levels**: Critical, high, normal
- **Device Management**: Token registration, targeting

### 🤖 AI & ML Features
- **Job Recommendations**: Semantic matching via Weaviate
- **Interview AI**: Gemini-powered question generation
- **CV Analysis**: Automated skill extraction
- **Career Guidance**: Personalized roadmaps

### 📚 Career Development
- **Learning Roadmaps**: Structured career paths
- **Courses**: Curated learning resources
- **Topics & Subtopics**: Organized knowledge base
- **Progress Tracking**: Learning milestones

### 📊 Analytics & Reporting
- **Admin Dashboard**: System metrics, user stats
- **Recruiter Analytics**: Application insights, hiring funnel
- **Candidate Insights**: Application tracking, success rate

---

## Technology Stack Summary

### Backend Framework
- **Spring Boot 3.5.6** (Java 21)
- Spring Security (OAuth2, JWT)
- Spring Data JPA (Hibernate)
- Spring Kafka
- Spring Mail
- Spring AI (OpenAI integration)

### Database & Cache
- **PostgreSQL** (Primary database)
- **Redis** (Cache & sessions)
- **Weaviate** (Vector database)

### Message Broker
- **Apache Kafka** (Event streaming)
- **Zookeeper** (Kafka coordination)

### External Services
- **Google OAuth2** (Authentication)
- **VNPay** (Payment gateway)
- **Gmail SMTP** (Email)
- **Firebase** (Storage & FCM)
- **Google Gemini AI** (AI features)
- **HuggingFace** (ML models)

### Development Tools
- **Maven** (Build tool)
- **MapStruct** (Object mapping)
- **Lombok** (Boilerplate reduction)
- **Swagger/OpenAPI** (API documentation)
- **Docker Compose** (Local development)

---

## Environment Variables

### Database
- `DB_HOST`, `DB_PORT`, `DB_NAME`
- `DB_USER_LOCAL`, `DB_PASSWORD_LOCAL`

### Kafka
- `KAFKA_BOOTSTRAP_SERVERS`
- `KAFKA_SECURITY_PROTOCOL`
- `KAFKA_SASL_USERNAME`, `KAFKA_SASL_PASSWORD`

### External APIs
- `GOOGLE_API_KEY` (Gemini AI)
- `WEAVIATE_URL`, `WEAVIATE_API_KEY`
- `HUGGINGFACE_API_KEY`
- `EMAIL_NAME`, `EMAIL_PASSWORD`

### Payment
- `vnp_TmnCode`, `vnp_HashSecret`
- `SUCCESS_RETURN_URL`

### Storage
- `BUCKET_NAME`, `BUCKET_PREFIX`

### Swagger
- `SWAGGER_SERVER_URL`

---

## Conclusion

CareerMate là một hệ thống tuyển dụng hiện đại, tích hợp đầy đủ các tính năng từ đăng tin, ứng tuyển, phỏng vấn, đến ký hợp đồng và đánh giá. Hệ thống sử dụng kiến trúc microservices với Spring Boot, tích hợp AI/ML cho recommendations, Kafka cho xử lý bất đồng bộ, và nhiều dịch vụ bên ngoài để tạo trải nghiệm người dùng hoàn chỉnh.

**Điểm mạnh**:
- ✅ Kiến trúc modular, dễ mở rộng
- ✅ Real-time notifications qua Kafka
- ✅ AI-powered matching và recommendations
- ✅ Tích hợp đầy đủ payment gateway
- ✅ Hỗ trợ OAuth2 và JWT
- ✅ Event-driven architecture
- ✅ Comprehensive API documentation

**Use Cases chính**:
1. Candidate tìm việc và phát triển sự nghiệp
2. Recruiter đăng tin và quản lý tuyển dụng
3. Admin quản trị và kiểm duyệt hệ thống
4. Tất cả users nhận notifications real-time
5. AI hỗ trợ matching và recommendations

