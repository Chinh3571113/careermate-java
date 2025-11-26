# CareerMate Backend - Java Spring Boot Application

## Overview
Enterprise-grade job matching and recruitment management platform built with Spring Boot 3.x, featuring interview scheduling, employment contract management, bilateral status verification, and company review systems.

## Technology Stack
- **Framework**: Spring Boot 3.x
- **Language**: Java 21
- **Build Tool**: Maven
- **Database**: MySQL
- **Messaging**: Apache Kafka (Notification System)
- **ORM**: JPA/Hibernate
- **Security**: Spring Security with JWT
- **API Documentation**: Swagger/OpenAPI 3.0
- **Mapping**: MapStruct

## Quick Start

```bash
# Clone repository
git clone https://github.com/anhlaptrinh/Sep490_CareerMate_Java.git
cd Sep490_CareerMate_Java

# Build project
./mvnw clean compile

# Run application
./mvnw spring-boot:run

# Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

## Core Features

### 🎯 Interview Management
Multi-round scheduling, automated reminders, reschedule workflows, no-show tracking

### 📝 Employment Contracts
Digital signatures, lifecycle tracking, probation management, auto-expiration

### ✅ Status Verification
Bilateral verification system with 7-day auto-approval, dispute escalation

### ⚖️ Dispute Resolution
Admin arbitration, evidence scoring, AI recommendations, priority alerts

### ⭐ Company Reviews
Staged eligibility, 3 review types, anonymous support, moderation system

### 🔔 Kafka Notifications
Async delivery, topic-based routing, priority levels, rich metadata

## API Endpoints

**Interview Scheduling** (`InterviewScheduleController`)
```
POST /api/job-applies/{id}/schedule-interview
POST /api/interviews/{id}/confirm
POST /api/interviews/{id}/reschedule
GET  /api/interviews/candidate/{id}/upcoming
```

**Employment Contracts** (`EmploymentContractController`)
```
POST /api/job-applies/{id}/employment-contract
POST /api/employment-contracts/{id}/send-for-signature
POST /api/employment-contracts/{id}/sign
GET  /api/employment-contracts/company/{id}
```

**Status Verification** (`StatusUpdateController`)
```
POST /api/job-applies/{id}/candidate-status-update
POST /api/status-updates/{id}/confirm
POST /api/status-updates/{id}/dispute
GET  /api/recruiters/{id}/status-updates/pending
```

**Dispute Resolution** (`DisputeResolutionController`) [Admin]
```
GET  /api/admin/disputes
POST /api/admin/disputes/{id}/resolve
GET  /api/admin/disputes/{id}/recommendation
```

**Company Reviews** (`CompanyReviewController`)
```
POST /api/v1/reviews
GET  /api/v1/reviews/company/{id}
GET  /api/v1/reviews/company/{id}/statistics
```

## Architecture

### Notification System
```
Kafka Topics:
├── admin-notifications (3 partitions)
├── recruiter-notifications (3 partitions)
└── candidate-notifications (3 partitions)

NotificationEvent Structure:
├── recipientId: String
├── title: String
├── message: String
├── category: String (CONTRACT, STATUS_UPDATE, INTERVIEW, DISPUTE, REVIEW)
├── priority: Integer (1=HIGH, 2=MEDIUM, 3=LOW)
└── metadata: Map<String, Object>
```

### Scheduled Jobs
- **Interview Reminders**: Every hour (24h), every 30 min (2h)
- **Status Verification**: Daily 1 AM (auto-approve), every 6 hours (reminders)
- **Contract Management**: Daily 2 AM (process expired)
- **Dispute Alerts**: Daily 10 AM (priority notifications)

## Database Schema

**Key Tables:**
- `interview_schedule` - Interview lifecycle tracking
- `employment_contract` - Contract management
- `status_update_request` - Bilateral verification
- `status_dispute` - Admin arbitration
- `company_review` - Company ratings & reviews

## Configuration

**application.yml:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/careermate
  kafka:
    bootstrap-servers: localhost:9092
```

## Build & Test

```bash
# Compile only
./mvnw clean compile

# Run tests
./mvnw test

# Skip tests
./mvnw clean install -DskipTests

# Check build status
./mvnw verify
```

## Troubleshooting

**Build Fails**: `./mvnw clean install -U`  
**Kafka Issues**: Check `netstat -an | grep 9092`  
**DB Migration**: `./mvnw flyway:info`

## Project Structure
```
src/main/
├── java/com/fpt/careermate/
│   ├── config/              # Security, Swagger, JWT
│   ├── services/
│   │   ├── job_services/    # Interview, Contract, Status
│   │   ├── review_services/ # Company reviews
│   │   └── kafka/           # Notifications
│   └── common/              # Exceptions, utilities
└── resources/
    ├── application.yml
    └── db/migration/        # Flyway SQL
```

## Authentication

All endpoints require JWT Bearer token:
```
Authorization: Bearer <token>
```

**Roles**: ADMIN, RECRUITER, CANDIDATE

## Contributing

1. Create feature branch: `feature/your-feature`
2. Make changes & test
3. Submit pull request

---

**Build Status**: ✅ SUCCESS (482 files)  
**Version**: 3.0  
**Updated**: November 25, 2025
