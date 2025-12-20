# Review Management System Implementation Summary

## Overview
Implemented a comprehensive review management system with role-based permissions for Admin and Recruiter roles.

## Backend Implementation

### 1. Enhanced Review Status Enum
**File**: `ReviewStatus.java`
- Added `HIDDEN` status for admin-hidden reviews (excluded from calculations)
- Statuses: `ACTIVE`, `FLAGGED`, `HIDDEN`, `REMOVED`, `ARCHIVED`

### 2. Admin Review Management

#### Controllers
**AdminReviewController** (`/api/v1/admin/reviews`)
- `POST /search` - Advanced search with dynamic filtering
- `PUT /{reviewId}/status` - Update single review status
- `POST /bulk-action` - Bulk hide/show/remove reviews
- `GET /stats` - Review statistics dashboard

**Key Features**:
- ✅ Search by company name, candidate name, review type
- ✅ Filter by date range (start/end dates) for bombing detection
- ✅ Filter by rating range (min/max)
- ✅ Filter flagged reviews only
- ✅ Text search in review content
- ✅ Pagination and sorting
- ✅ Bulk actions with reason tracking

#### DTOs
**AdminReviewFilterRequest**:
```java
- companyName, candidateName
- reviewType, status
- startDate, endDate (for period filtering)
- minRating, maxRating
- flaggedOnly, minFlagCount
- searchText
- page, size, sortBy, sortDirection
```

**AdminBulkReviewActionRequest**:
```java
- List<Integer> reviewIds
- ReviewStatus newStatus
- String reason (optional)
```

**AdminReviewResponse**:
- Complete review details including candidate info, company info
- Category ratings
- Moderation fields (flagCount, removalReason, sentimentScore)

#### Services
**AdminReviewServiceImpl**:
- Dynamic JPA Specification-based filtering
- Compound search across multiple fields
- Statistics aggregation (total, by type, by time period)
- Bulk update with atomic transactions

### 3. Recruiter Review Viewing (Read-Only)

#### Controller
**RecruiterReviewController** (`/api/v1/recruiter/reviews`)
- `GET /my-company` - View company reviews (read-only)
- `GET /stats` - Company review statistics

**Key Features**:
- ✅ Only shows ACTIVE reviews (hides HIDDEN/REMOVED)
- ✅ Respects anonymous flag (hides candidate details)
- ✅ NO moderation powers (read-only)
- ✅ Filter by type, date range, rating
- ✅ Statistics limited to ACTIVE reviews only

#### Services
**RecruiterReviewServiceImpl**:
- Filters by recruiter ID automatically
- Only returns ACTIVE status reviews
- Sanitizes candidate info for anonymous reviews
- Statistics exclude hidden/removed reviews

### 4. Repository Enhancements

**CompanyReviewRepo**:
- Extended `JpaSpecificationExecutor` for dynamic queries
- New methods:
  ```java
  - countByStatus(ReviewStatus)
  - countByReviewType(ReviewType)
  - countByCreatedAtAfter(LocalDateTime)
  - countByRecruiterIdAndStatusAndCreatedAtAfter(...)
  - getAverageRatingByRecruiterAndStatus(...)
  ```

## Features Summary

### Admin Capabilities
1. **Dynamic Search & Filtering**
   - Search by company, candidate, review type
   - Date range filtering (critical for bombing detection)
   - Rating filters
   - Flag count filtering
   - Full-text search in review content

2. **Moderation Actions**
   - Hide reviews (excluded from public view & calculations)
   - Show reviews (make visible again)
   - Remove reviews with reason tracking
   - Bulk actions for efficiency

3. **Statistics Dashboard**
   - Total reviews by status
   - Reviews by type (APPLICATION, INTERVIEW, WORK)
   - Time-based metrics (24h, 7d, 30d)
   - Flagged review counts

4. **Spam/Bombing Detection**
   - Filter by date period
   - Sort by created date
   - Minimum flag count filter
   - Bulk hide capability

### Recruiter Capabilities
1. **View-Only Access**
   - Can view all ACTIVE reviews for their company
   - Cannot hide/show/remove reviews
   - Respects candidate anonymity

2. **Filtering**
   - By review type
   - By date range
   - By rating

3. **Statistics**
   - Total active reviews
   - Average ratings
   - Reviews by type
   - Time-period breakdowns

4. **Privacy Protection**
   - Anonymous reviews show "Anonymous" instead of names
   - No access to moderation fields
   - No flag counts visible

## Permission Model

```
┌─────────────────────────────────────────┐
│          ADMIN                          │
├─────────────────────────────────────────┤
│ ✅ View ALL reviews (all statuses)     │
│ ✅ Search with advanced filters        │
│ ✅ Filter by date for bombing detect   │
│ ✅ Hide/Show reviews                   │
│ ✅ Bulk actions                        │
│ ✅ See flagCount, reasons              │
│ ✅ Affects calculations                │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│        RECRUITER                        │
├─────────────────────────────────────────┤
│ ✅ View ACTIVE reviews only            │
│ ✅ Basic filtering (type, date, rating)│
│ ✅ View statistics                     │
│ ❌ Cannot hide/show/remove             │
│ ❌ No flagCount visibility             │
│ ❌ No moderation powers                │
│ 🔒 Respects anonymity                  │
└─────────────────────────────────────────┘
```

## API Endpoints

### Admin Endpoints (Protected: `@PreAuthorize("hasRole('ADMIN')")`)
```
POST   /api/v1/admin/reviews/search
PUT    /api/v1/admin/reviews/{reviewId}/status?newStatus=HIDDEN&reason=spam
POST   /api/v1/admin/reviews/bulk-action
GET    /api/v1/admin/reviews/stats
```

### Recruiter Endpoints (Protected: `@PreAuthorize("hasRole('RECRUITER')")`)
```
GET    /api/v1/recruiter/reviews/my-company?page=0&size=20&reviewType=WORK&startDate=2024-01-01
GET    /api/v1/recruiter/reviews/stats
```

## Security Features

1. **Role-Based Access Control**
   - Admin: Full moderation powers
   - Recruiter: Read-only for own company

2. **Data Sanitization**
   - Anonymous reviews hide candidate details
   - Recruiters don't see moderation metadata

3. **Audit Trail**
   - `updatedAt` timestamp on status changes
   - `removalReason` field for accountability
   - Action reasons in bulk operations

## Use Cases Covered

### 1. Review Bombing Detection
**Scenario**: Company receives 100 negative reviews in 1 day
```
Admin filters:
- startDate: 2024-12-17
- endDate: 2024-12-18
- companyName: "Target Company"
- minRating: 1, maxRating: 2
→ Shows suspicious reviews
→ Bulk hide with reason: "Suspected review bombing"
```

### 2. Spam Detection
**Scenario**: Same candidate submits duplicate reviews
```
Admin filters:
- candidateName: "John Doe"
- searchText: "exact repeated phrase"
→ Identifies duplicates
→ Individual hide with reason: "Duplicate content"
```

### 3. Recruiter Transparency
**Scenario**: Recruiter wants to see feedback
```
Recruiter accesses:
- /my-company?reviewType=INTERVIEW
- Sees only ACTIVE reviews
- Anonymous reviews protected
- Cannot manipulate reviews
```

### 4. Statistical Analysis
**Scenario**: Admin monitors review health
```
GET /admin/reviews/stats
→ Returns:
  - Total: 5000
  - Active: 4500
  - Hidden: 300 (6%)
  - Flagged: 50
  - Last 24h: 45
```

## Database Impact

### New Status Added
- Review calculations now exclude `HIDDEN` status
- Existing queries updated to check status
- Default status remains `ACTIVE`

### Performance Considerations
- JPA Specifications for dynamic queries
- Indexed fields: status, createdAt, recruiterId
- Pagination prevents large result sets
- Bulk operations use batch updates

## Next Steps (Frontend)

To complete the implementation, create:

1. **Admin Dashboard** (`/admin/reviews`)
   - Search form with all filters
   - Review cards/table
   - Bulk selection checkboxes
   - Hide/Show/Remove buttons
   - Date range picker for bombing detection

2. **Recruiter Dashboard** (`/recruiter/reviews`)
   - View-only review list
   - Basic filters (type, date, rating)
   - Statistics cards
   - No moderation UI

3. **Components Needed**:
   - `AdminReviewFilters.tsx`
   - `ReviewTable.tsx`
   - `BulkActionBar.tsx`
   - `ReviewStatsCards.tsx`
   - `RecruiterReviewList.tsx`

## Testing Checklist

- [ ] Admin can search reviews with all filters
- [ ] Admin can hide single review
- [ ] Admin can bulk hide reviews
- [ ] Hidden reviews excluded from calculations
- [ ] Recruiter can only see ACTIVE reviews
- [ ] Recruiter cannot access moderation endpoints
- [ ] Anonymous reviews hide candidate info for recruiters
- [ ] Date range filter works for bombing detection
- [ ] Bulk actions update all selected reviews
- [ ] Statistics accurately reflect current state

## Files Created/Modified

### Created:
1. `AdminReviewController.java`
2. `RecruiterReviewController.java`
3. `AdminReviewService.java`
4. `AdminReviewServiceImpl.java`
5. `RecruiterReviewService.java`
6. `RecruiterReviewServiceImpl.java`
7. `AdminReviewFilterRequest.java`
8. `AdminBulkReviewActionRequest.java`
9. `AdminReviewResponse.java`

### Modified:
1. `ReviewStatus.java` (added HIDDEN status)
2. `CompanyReviewRepo.java` (added JpaSpecificationExecutor, new methods)
3. `CompanyReviewServiceImpl.java` (fixed validation logic)

## Compilation Status
✅ Backend compiles successfully
✅ All services implemented
✅ All controllers secured with role-based access
✅ Repository methods added
✅ DTOs created

## Key Achievements
- ✅ Admin has full moderation control
- ✅ Recruiter has read-only access
- ✅ Dynamic search supports bombing detection
- ✅ Bulk actions for efficiency
- ✅ Privacy protection for anonymous reviews
- ✅ Audit trail with reasons
- ✅ Statistics dashboard ready
- ✅ Role-based security enforced
