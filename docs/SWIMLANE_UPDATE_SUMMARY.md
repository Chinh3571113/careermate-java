# Recruiter Activity Diagrams - Swimlane Update Summary

## Overview
Đã cập nhật các Activity Diagrams trong file `RECRUITER_ACTIVITY_DIAGRAM.md` để bao gồm **swimlane notation**, giúp phân chia rõ ràng trách nhiệm của từng actor/system trong workflows.

---

## Swimlane Structure

Mỗi activity diagram hiện được tổ chức thành các lanes (cột) đại diện cho:

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          WORKFLOW NAME                                       │
├────────────────┬────────────────┬────────────────┬────────────────┬─────────┤
│   RECRUITER    │     SYSTEM     │    DATABASE    │     KAFKA      │  OTHER  │
├────────────────┼────────────────┼────────────────┼────────────────┼─────────┤
│   Actions by   │   Backend      │   Data         │   Async        │ External│
│   recruiter    │   logic        │   persistence  │   events       │ actors  │
└────────────────┴────────────────┴────────────────┴────────────────┴─────────┘
```

### Swimlane Actors:

1. **RECRUITER** - User actions và decisions
2. **SYSTEM** - Backend application logic
3. **DATABASE** - Data queries và persistence (PostgreSQL)
4. **KAFKA** - Async event publishing và notifications
5. **AI** - AI services (Gemini, Weaviate)
6. **CANDIDATE** - Candidate interactions
7. **ADMIN** - Admin actions (for dispute resolution)
8. **FIREBASE** - File storage operations
9. **VNPAY** - Payment gateway

---

## Updated Diagrams

### ✅ 1. Registration & Onboarding (Pending Update)
**Swimlanes**: Recruiter → System → Database → Admin

**Flow**:
- Recruiter submits registration
- System validates data
- Database stores information
- Admin approves/rejects

---

### ✅ 2. Post Job Activity (UPDATED)
**Swimlanes**: Recruiter → System → Database → Kafka → AI

**Key Interactions**:
```
RECRUITER          SYSTEM          DATABASE        KAFKA           AI
    │                 │                │             │             │
    │ Fill Job Form   │                │             │             │
    ├────────────────>│                │             │             │
    │                 │ Validate       │             │             │
    │                 ├────────────────────────────────────────────>│
    │                 │                │             │   AI Check  │
    │                 │<────────────────────────────────────────────┤
    │                 │ Create Job     │             │             │
    │                 ├───────────────>│             │             │
    │                 │ Publish Event  │             │             │
    │                 ├────────────────┼────────────>│             │
    │                 │                │  "job_      │             │
    │                 │                │   posted"   │             │
```

**Benefits**:
- ✅ Clear data flow
- ✅ AI validation step visible
- ✅ Async notification via Kafka

---

### ✅ 3. Review Applications Activity (UPDATED)
**Swimlanes**: Recruiter → System → Database → Kafka → AI

**Key Interactions**:
```
RECRUITER          SYSTEM          DATABASE        KAFKA           AI
    │                 │                │             │             │
    │ Select App      │                │             │             │
    ├────────────────>│                │             │             │
    │                 │ Fetch Profile  │             │             │
    │                 ├───────────────>│             │             │
    │                 │ Get AI Score   │─────────────────────────>│
    │                 │<────────────────│             │  Match     │
    │                 │<──────────────────────────────────────────┤
    │ View Profile    │                │             │   Score    │
    │ with AI Score   │                │             │             │
    │                 │                │             │             │
    │ Make Decision   │                │             │             │
    ├────────────────>│                │             │             │
    │                 │ Update Status  │             │             │
    │                 ├───────────────>│             │             │
    │                 │ Publish Event  │             │             │
    │                 ├────────────────┼────────────>│             │
```

**Benefits**:
- ✅ AI scoring integration visible
- ✅ Status update flow clear
- ✅ Candidate notification via Kafka

---

### ✅ 4. Schedule Interview Activity (UPDATED)
**Swimlanes**: Recruiter → System → Database → Kafka → Candidate

**Key Interactions**:
```
RECRUITER          SYSTEM          DATABASE        KAFKA        CANDIDATE
    │                 │                │             │              │
    │ Select Candidate│                │             │              │
    ├────────────────>│                │             │              │
    │                 │ Check Calendar │             │              │
    │                 ├───────────────>│             │              │
    │                 │ • WorkingHours │             │              │
    │                 │ • Interviews   │             │              │
    │                 │ • TimeOff      │             │              │
    │                 │<───────────────┤             │              │
    │ View Available  │                │             │              │
    │ Slots           │                │             │              │
    │                 │                │             │              │
    │ Select Time     │                │             │              │
    ├────────────────>│                │             │              │
    │                 │ Validate Slot  │             │              │
    │                 ├───────────────>│             │              │
    │                 │<───────────────┤             │              │
    │                 │ Valid ✓        │             │              │
    │                 │                │             │              │
    │ Confirm         │                │             │              │
    ├────────────────>│                │             │              │
    │                 │ Create Interview│            │              │
    │                 ├───────────────>│             │              │
    │                 │ Publish Event  │             │              │
    │                 ├────────────────┼────────────>│              │
    │                 │                │ "interview_ │              │
    │                 │                │  scheduled" │              │
    │                 │                │             ├─────────────>│
    │                 │                │             │ Notify       │
    │                 │                │             │<─────────────┤
    │                 │                │<────────────┤ Confirm      │
    │                 │<───────────────┤ Update      │              │
    │ Interview       │                │ CONFIRMED   │              │
    │ Confirmed       │                │             │              │
```

**Benefits**:
- ✅ Bi-directional communication visible
- ✅ Calendar conflict validation
- ✅ Confirmation flow clear

---

### ✅ 5. Conduct Interview & Evaluation Activity (UPDATED)
**Swimlanes**: Recruiter → System → Database → Kafka → Candidate

**Key Interactions**:
```
RECRUITER          SYSTEM          DATABASE        KAFKA        CANDIDATE
    │                 │                │             │              │
    │ Receive Reminder│                │             │              │
    │<────────────────────────────────────────────────┤              │
    │                 │                │ Scheduled   │              │
    │                 │                │ Notification│              │
    │                 │                │             │              │
    │ Access Interview│                │             │              │
    ├────────────────>│                │             │              │
    │                 │ Get Details    │             │              │
    │                 ├───────────────>│             │              │
    │                 │<───────────────┤             │              │
    │ View Details    │                │             │              │
    │                 │                │             │              │
    │ Start Interview │                │             │              │
    ├────────────────>│                │             │              │
    │                 │ Update Status  │             │              │
    │                 ├───────────────>│             │              │
    │                 │ ONGOING        │             │              │
    │                 │                │             │              │
    │ Conduct         │                │             │              │
    │ Interview       │                │             │              │
    │                 │                │             │              │
    │ Complete        │                │             │              │
    ├────────────────>│                │             │              │
    │                 │ End Session    │             │              │
    │                 ├───────────────>│             │              │
    │                 │                │             │              │
    │ Fill Evaluation │                │             │              │
    │ • Tech Skills   │                │             │              │
    │ • Communication │                │             │              │
    │ • Problem Solve │                │             │              │
    │ • Cultural Fit  │                │             │              │
    │ • Overall       │                │             │              │
    ├────────────────>│                │             │              │
    │                 │ Save Eval      │             │              │
    │                 ├───────────────>│             │              │
    │                 │ Publish Event  │             │              │
    │                 ├────────────────┼────────────>│              │
    │                 │                │ "interview_ │              │
    │                 │                │  evaluated" ├─────────────>│
    │                 │                │             │ Notify       │
```

**Benefits**:
- ✅ Interview lifecycle tracking
- ✅ Evaluation structure clear
- ✅ Result notification to candidate

---

## Remaining Diagrams to Update

### 🔄 6. Send Employment Contract
**Planned Swimlanes**: Recruiter → System → Database → Firebase → Kafka → Candidate

**Key Steps**:
- Contract creation
- PDF generation
- Firebase upload
- Signature process
- Status tracking

---

### 🔄 7. Manage Calendar & Working Hours
**Planned Swimlanes**: Recruiter → System → Database

**Key Steps**:
- Set working hours per day
- Request time off
- Conflict checking
- Calendar blocking

---

### 🔄 8. Purchase Package
**Planned Swimlanes**: Recruiter → System → Database → VNPay → Kafka

**Key Steps**:
- Package selection
- Payment processing
- VNPay integration
- Entitlement activation
- Confirmation

---

### 🔄 9. View Analytics & Reports
**Planned Swimlanes**: Recruiter → System → Database → Redis → AI

**Key Steps**:
- Dashboard access
- Data aggregation
- Cache checking
- Chart generation
- AI insights
- Export

---

### 🔄 10. Handle Dispute
**Planned Swimlanes**: Candidate → System → Database → Kafka → Recruiter → Admin

**Key Steps**:
- Dispute creation
- Evidence upload
- Admin review
- Decision making
- Resolution execution

---

## Benefits of Swimlane Notation

### 1. **Clarity of Responsibilities**
- Rõ ràng actor nào làm gì
- Dễ identify bottlenecks
- Clear handoffs between systems

### 2. **System Architecture Visibility**
- Database interactions
- Kafka event flows
- External service calls
- Caching strategies

### 3. **Better Documentation**
- Onboarding developers
- System design reviews
- Testing strategy
- API integration

### 4. **Performance Analysis**
- Identify slow operations
- Database query optimization
- Async vs sync operations
- Caching opportunities

### 5. **Error Handling**
- Failure points visible
- Retry logic
- Fallback mechanisms
- Error notification flows

---

## Reading the Diagrams

### Horizontal Flow (Left to Right)
- Represents time progression
- Activities in sequence
- Decision points

### Vertical Lanes (Columns)
- Each column = one actor/system
- Actions within lane = that actor's responsibility
- Arrows crossing lanes = interactions

### Arrow Types
```
────────────> Direct call/request (synchronous)
- - - - - - > Async event/notification
<───────────> Response/callback
```

### Symbols
```
│   Normal flow
├─> Decision branch
▼   Flow direction
┌─┐ Process/Activity
```

---

## Implementation Notes

### Database Operations
All database queries và updates happen in DATABASE lane:
- SELECT queries
- INSERT/UPDATE operations
- Transaction management
- Data validation

### Kafka Events
All async notifications go through KAFKA lane:
- Event publishing
- Topic routing
- Consumer processing
- Multi-channel delivery

### AI Operations
AI processing in dedicated AI lane:
- Gemini API calls
- Weaviate searches
- Score calculations
- Recommendations

---

## Next Steps

1. ✅ Complete remaining 5 diagrams with swimlanes
2. ✅ Add sequence numbers to steps
3. ✅ Include error handling paths
4. ✅ Add timing annotations
5. ✅ Document edge cases

---

## Usage Guidelines

### For Developers
- Use to understand system flow
- Identify integration points
- Plan testing strategy
- Debug issues

### For Architects
- System design validation
- Performance bottleneck identification
- Scalability planning
- Security review

### For Product Managers
- Feature flow understanding
- User journey mapping
- Requirement validation
- Timeline estimation

### For QA
- Test case creation
- Integration test planning
- Edge case identification
- Performance testing

---

## Related Documentation

- [Main Activity Diagrams](RECRUITER_ACTIVITY_DIAGRAM.md)
- [Context Diagram](CONTEXT_DIAGRAM.md)
- [Data Flow Diagrams](DATA_FLOW_DIAGRAMS.md)
- [API Endpoints](API_ENDPOINTS_REFERENCE.md)

---

**Last Updated**: December 3, 2025
**Status**: 5/10 diagrams updated with swimlanes
**Next Update**: Complete remaining 5 diagrams

