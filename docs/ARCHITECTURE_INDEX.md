# CareerMate System - Architecture Documentation Index

Tài liệu kiến trúc hệ thống CareerMate được tổ chức thành các phần sau:

---

## 📚 Danh Mục Tài Liệu

### 1. 🎯 [Context Diagram](CONTEXT_DIAGRAM.md)
**Mục đích**: Hiển thị tổng quan về hệ thống, các actors, và external systems

**Nội dung bao gồm**:
- System overview và boundaries
- External actors (Candidate, Recruiter, Admin)
- External systems (PostgreSQL, Kafka, Redis, Firebase, etc.)
- Technology stack
- Security architecture
- Key features summary

**Khi nào sử dụng**:
- Giới thiệu hệ thống cho stakeholders
- Hiểu tổng quan về kiến trúc
- Xác định integration points

---

### 2. 📦 [Container Diagram (PlantUML)](./container-diagram.puml)
**Mục đích**: Hiển thị các containers (services) bên trong hệ thống

**Nội dung bao gồm**:
- 13 main services (Authentication, Job, Interview, Contract, etc.)
- Database và message broker
- Inter-service communication
- External API integrations

**Khi nào sử dụng**:
- Thiết kế microservices architecture
- Hiểu service dependencies
- Planning deployment strategy

**Cách xem**: Sử dụng PlantUML viewer hoặc paste vào http://www.plantuml.com/plantuml/uml/

---

### 3. 🌐 [Context Diagram (PlantUML)](./context-diagram.puml)
**Mục đích**: C4 Level 1 diagram với format PlantUML

**Nội dung bao gồm**:
- System context với actors
- External systems relationships
- High-level data flows

**Khi nào sử dụng**:
- Presentations và documentation
- System overview meetings
- Architecture reviews

**Cách xem**: Sử dụng PlantUML viewer

---

### 4. 🔌 [API Endpoints Reference](API_ENDPOINTS_REFERENCE.md)
**Mục đích**: Comprehensive API documentation

**Nội dung bao gồm**:
- 13 API modules với 200+ endpoints
- Request/Response formats
- Authentication requirements
- Pagination, filtering, sorting
- Rate limiting
- File upload endpoints
- WebSocket/SSE endpoints

**Khi nào sử dụng**:
- Frontend development
- API integration
- Testing và QA
- Client SDK development

---

### 5. 📊 [Data Flow Diagrams](DATA_FLOW_DIAGRAMS.md)
**Mục đích**: Minh họa chi tiết luồng dữ liệu trong hệ thống

**Nội dung bao gồm**:
- 10 main data flows:
  1. Job Application Flow
  2. Interview Scheduling Flow
  3. Employment Contract Flow
  4. Payment Processing Flow
  5. Company Review Flow
  6. AI-Powered Recommendation Flow
  7. Real-time Notification Flow
  8. Authentication & Authorization Flow
  9. File Upload Flow
  10. Cache Strategy Flow

**Khi nào sử dụng**:
- Hiểu business processes
- Debugging và troubleshooting
- Performance optimization
- Integration testing

---

## 🏗️ Kiến Trúc Hệ Thống

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         Users Layer                          │
│  Candidate           Recruiter            Admin              │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway Layer                         │
│              Spring Boot REST API (Port 8080)                │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│Authentication│ │   Business   │ │    Admin     │
│   Services   │ │   Services   │ │   Services   │
│              │ │              │ │              │
│ • OAuth2     │ │ • Jobs       │ │ • Dashboard  │
│ • JWT        │ │ • Interviews │ │ • Moderation │
│ • RBAC       │ │ • Contracts  │ │ • Analytics  │
└──────────────┘ │ • Reviews    │ └──────────────┘
                 │ • Payments   │
                 │ • Profiles   │
                 │ • Resumes    │
                 │ • Coach      │
                 │ • AI/ML      │
                 └──────┬───────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│  PostgreSQL  │ │    Kafka     │ │    Redis     │
│   Database   │ │Message Broker│ │    Cache     │
└──────────────┘ └──────────────┘ └──────────────┘
        │               │               │
        └───────────────┴───────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                    External Services                         │
│  • Firebase Storage    • VNPay Payment   • Gmail SMTP        │
│  • Google OAuth2       • Gemini AI       • Weaviate DB       │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔑 Key Components

### 1. Authentication & Authorization
- **JWT-based authentication** (15-minute access tokens)
- **OAuth2 integration** with Google
- **Role-based access control** (Admin, Recruiter, Candidate)
- **Token refresh mechanism** (7-day refresh tokens)
- **Redis-based token blacklist**

### 2. Core Business Services
| Service | Responsibility | Key Features |
|---------|---------------|--------------|
| **Job Service** | Job postings management | CRUD, search, filtering, saved jobs |
| **Interview Service** | Interview scheduling | Calendar, reminders, AI interviews |
| **Contract Service** | Employment contracts | E-signature, verification, disputes |
| **Review Service** | Company reviews | Ratings, moderation, statistics |
| **Payment Service** | Payment processing | VNPay, invoices, packages |
| **Profile Service** | User profiles | Candidate/Recruiter data |
| **Resume Service** | CV management | Upload, parse, download |
| **Coach Service** | Career guidance | Roadmaps, courses, topics |
| **Recommendation** | AI matching | Jobs/candidates matching |

### 3. Infrastructure Services
- **Notification Service**: Kafka-based multi-channel notifications
- **File Service**: Firebase Storage integration
- **Cache Service**: Redis for performance
- **Admin Service**: System management and analytics

---

## 🔄 Integration Patterns

### 1. Synchronous Communication
- **REST API**: HTTP/HTTPS with JSON
- **Response Time**: <500ms target
- **Use Cases**: CRUD operations, queries

### 2. Asynchronous Communication
- **Kafka**: Event-driven architecture
- **3 Topics**: admin, recruiter, candidate notifications
- **Partitioning**: 3 partitions per topic for scalability
- **Use Cases**: Notifications, background tasks

### 3. External Integrations
- **Google OAuth2**: Social login
- **VNPay**: Payment gateway (sandbox)
- **Firebase**: File storage, FCM push notifications
- **Gmail SMTP**: Email delivery
- **Gemini AI**: AI-powered features
- **Weaviate**: Semantic search and matching

---

## 📊 Data Architecture

### Database Schema (PostgreSQL)
```
Core Tables:
├── accounts (users)
├── candidates
├── recruiters
├── admins
├── roles & permissions
├── job_postings
├── job_applies
├── interview_schedules
├── employment_verifications
├── company_reviews
├── invoices & entitlements
├── notifications
├── resumes & related tables
├── roadmaps & courses
└── blogs & comments
```

### Caching Strategy (Redis)
- **Sessions**: User session data
- **Tokens**: JWT tokens and blacklist
- **Hot Data**: Frequently accessed data (TTL: 5-60 min)
- **Rate Limiting**: API throttling

### Message Queues (Kafka)
- **Topic Structure**: `{role}-notifications`
- **Partitions**: 3 per topic
- **Consumer Groups**: One per service type
- **Retention**: 7 days

---

## 🔐 Security Features

### Authentication
✅ JWT with signature verification  
✅ OAuth2 with Google  
✅ Password encryption (BCrypt)  
✅ Token refresh mechanism  
✅ Session management  

### Authorization
✅ Role-based access control (RBAC)  
✅ Permission-based endpoints  
✅ Resource ownership validation  
✅ Admin-only operations  

### Data Protection
✅ HTTPS/TLS encryption  
✅ SQL injection prevention (JPA)  
✅ XSS protection  
✅ CORS configuration  
✅ Input validation  

---

## 🚀 Performance Optimization

### Caching
- Redis for hot data
- Query result caching
- API response caching
- Static content CDN (future)

### Database
- Proper indexing
- Query optimization
- Connection pooling
- Pagination for large datasets

### Async Processing
- Kafka for heavy operations
- Background job processing
- Email queue management
- Batch processing for analytics

---

## 📈 Monitoring & Observability

### Logging
- Structured logging (JSON format)
- Log levels: ERROR, WARN, INFO, DEBUG
- Request/Response logging
- Audit trails for sensitive operations

### Metrics
- API response times
- Database query performance
- Kafka lag monitoring
- Cache hit ratios
- External API latencies

### Health Checks
- `/actuator/health` endpoint
- Database connectivity
- Kafka connectivity
- Redis availability

---

## 🛠️ Development Setup

### Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL 17
- Redis 7.2
- Kafka 7.5 (with Zookeeper)
- Docker & Docker Compose (recommended)

### Quick Start
```bash
# 1. Start infrastructure with Docker Compose
cd careermate_docker
docker-compose up -d

# 2. Configure environment variables
cp .env.example .env
# Edit .env with your credentials

# 3. Build the project
./mvnw clean install

# 4. Run the application
./mvnw spring-boot:run

# 5. Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

### Docker Services
- **PostgreSQL**: `localhost:5439`
- **Redis**: `localhost:6379`
- **Redis Insight**: `localhost:5540`
- **Kafka**: `localhost:9092`
- **Kafka UI**: `localhost:8090`
- **Zookeeper**: `localhost:2181`

---

## 📋 API Documentation

### Swagger UI
- **URL**: http://localhost:8080/swagger-ui.html
- **Features**: 
  - Interactive API testing
  - Request/Response schemas
  - Authentication testing
  - Example values

### OpenAPI Specification
- **URL**: http://localhost:8080/v3/api-docs
- **Format**: JSON
- **Version**: OpenAPI 3.0

---

## 🎯 Use Cases

### For Candidates
1. ✅ Register và login (OAuth2 hoặc username/password)
2. ✅ Tạo và quản lý CV
3. ✅ Tìm kiếm và ứng tuyển công việc
4. ✅ Nhận AI-powered job recommendations
5. ✅ Lập lịch và tham gia phỏng vấn
6. ✅ Ký hợp đồng lao động điện tử
7. ✅ Đánh giá công ty sau khi làm việc
8. ✅ Học các lộ trình nghề nghiệp
9. ✅ Nhận notifications real-time
10. ✅ Mua gói dịch vụ premium

### For Recruiters
1. ✅ Register công ty và profile
2. ✅ Đăng tin tuyển dụng
3. ✅ Quản lý ứng viên
4. ✅ Lập lịch phỏng vấn với calendar sync
5. ✅ Nhận AI-powered candidate recommendations
6. ✅ Gửi hợp đồng lao động
7. ✅ Đánh giá ứng viên
8. ✅ Xem analytics và báo cáo
9. ✅ Quản lý lịch làm việc
10. ✅ Mua gói dịch vụ

### For Admins
1. ✅ Dashboard tổng quan hệ thống
2. ✅ Quản lý users (candidates, recruiters)
3. ✅ Kiểm duyệt nội dung (jobs, reviews, comments)
4. ✅ Giải quyết tranh chấp
5. ✅ Xem analytics và thống kê
6. ✅ Quản lý thanh toán và invoices
7. ✅ Cấu hình hệ thống

---

## 🎨 UI/UX Integration

### Frontend Technologies (Expected)
- React/Next.js hoặc Vue/Nuxt
- TypeScript
- Tailwind CSS
- Axios/Fetch for API calls
- WebSocket/SSE for real-time updates

### API Integration Pattern
```javascript
// Example: Job application
const applyForJob = async (jobId, candidateId) => {
  try {
    const response = await axios.post('/api/job-applies', {
      jobId,
      candidateId,
      resumeId: selectedResumeId
    }, {
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    });
    
    // Success
    showNotification('Applied successfully!');
    
  } catch (error) {
    // Error handling
    showError(error.response.data.message);
  }
};
```

---

## 🔮 Future Enhancements

### Planned Features
- [ ] Video interview integration (WebRTC)
- [ ] Advanced AI chatbot for career advice
- [ ] Mobile apps (React Native)
- [ ] Blockchain-based certificate verification
- [ ] Multi-language support (i18n)
- [ ] Advanced analytics dashboard
- [ ] Social media integrations (LinkedIn, Facebook)
- [ ] SMS notifications (Twilio)
- [ ] GraphQL API option
- [ ] Microservices split for scalability

### Technical Improvements
- [ ] Kubernetes deployment
- [ ] Service mesh (Istio)
- [ ] Distributed tracing (Jaeger)
- [ ] Centralized logging (ELK Stack)
- [ ] API Gateway (Kong/Nginx)
- [ ] CDN for static assets
- [ ] Load balancer (HAProxy/Nginx)
- [ ] Database replication
- [ ] Kafka cluster expansion

---

## 📞 Support & Contact

### Documentation
- [Context Diagram](CONTEXT_DIAGRAM.md)
- [API Endpoints](API_ENDPOINTS_REFERENCE.md)
- [Data Flows](DATA_FLOW_DIAGRAMS.md)
- [Recruiter Activity Diagrams](RECRUITER_ACTIVITY_DIAGRAM.md)
- [Implementation Guides](./docs/)

### Repository
- GitHub: [CareerMate Backend](https://github.com/anhlaptrinh/Sep490_CareerMate_Java)

### Issue Tracking
- Report bugs via GitHub Issues
- Feature requests welcome
- Pull requests accepted

---

## 📝 License

[Your License Here]

---

## 👥 Contributors

- Development Team
- Architecture Team
- QA Team
- DevOps Team

---

## 🙏 Acknowledgments

- Spring Boot team
- Apache Kafka community
- PostgreSQL community
- Firebase team
- Google AI team
- Open source contributors

---

**Last Updated**: December 2, 2025  
**Version**: 1.0.0  
**Status**: Active Development

