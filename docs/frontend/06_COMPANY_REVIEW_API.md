# Company Review API Documentation

**Module**: Company Review System  
**Base URL**: `/api`  
**Version**: 3.0  
**Last Updated**: November 25, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [Staged Review System](#staged-review-system)
3. [Data Models](#data-models)
4. [API Endpoints](#api-endpoints)
5. [Review Eligibility Rules](#review-eligibility-rules)
6. [Rating Calculations](#rating-calculations)
7. [Business Rules](#business-rules)
8. [Error Handling](#error-handling)

---

## Overview

The **Company Review System** allows verified former employees to submit reviews about their experience at a company. Reviews are only allowed from candidates who were employed for at least 30 days and verified their employment status.

### Why Staged Reviews?

Traditional problem: Users write one review immediately after leaving, which may be emotional or biased.

**Our Solution**: Staged review system
- ✅ **Stage 1 (Day 30)**: Initial impressions during employment
- ✅ **Stage 2 (Post-termination)**: Reflection after leaving
- ✅ **Balanced perspective**: Combines immediate experience + retrospective insight
- ✅ **Prevents fake reviews**: Must have verified employment

---

## Staged Review System

### Review Timeline

```
┌────────────────────────────────────────────────────────────────┐
│                      REVIEW TIMELINE                            │
└────────────────────────────────────────────────────────────────┘

Day 0: Employment starts
       └── Cannot submit review yet

Day 30: Eligible for STAGE 1 review
       ├── Candidate verifies "still employed"
       ├── Review eligibility: ELIGIBLE
       └── Can submit Stage 1 review (optional)

Day X: Employment ends (resignation/firing/layoff)
       ├── Stage 1 review becomes read-only
       └── Eligible for STAGE 2 review

Day X+1: Can submit Stage 2 review
       ├── Reflects on entire employment experience
       └── Review marked as COMPLETE

Final Review:
       ├── If both stages submitted: Shows both perspectives
       └── If only Stage 1: Shows Stage 1 only
```

### Stage Comparison

| Aspect | Stage 1 (Day 30) | Stage 2 (Post-Termination) |
|--------|-----------------|---------------------------|
| **Timing** | While employed (Day 30-36) | After leaving |
| **Perspective** | Current employee experience | Retrospective reflection |
| **Optional** | Yes, can skip | Yes, can skip |
| **Can edit** | Yes, until termination | No, final |
| **Eligibility** | Employed 30+ days + verified | Must have Stage 1 OR employed 30+ days |

---

## Data Models

### CompanyReviewRequest
```typescript
interface CompanyReviewRequest {
  jobApplyId: number;
  stage: ReviewStage;                 // "STAGE_1" or "STAGE_2"
  
  // Rating categories (1-5 stars each)
  overallRating: number;              // 1-5 (required)
  workLifeBalance: number;            // 1-5 (required)
  compensation: number;               // 1-5 (required)
  management: number;                 // 1-5 (required)
  culture: number;                    // 1-5 (required)
  careerGrowth: number;               // 1-5 (required)
  
  // Written feedback
  title: string;                      // Short headline (required, max 100 chars)
  pros: string;                       // Positive aspects (required, min 50 chars)
  cons: string;                       // Negative aspects (required, min 50 chars)
  advice: string;                     // Advice to management (optional, min 50 chars)
  
  // Recommendation
  wouldRecommend: boolean;            // true/false (required)
  
  // Anonymity
  isAnonymous: boolean;               // true = hide name, false = show name
}

enum ReviewStage {
  STAGE_1 = "STAGE_1",                // Day 30 review (while employed)
  STAGE_2 = "STAGE_2"                 // Post-termination review
}
```

### CompanyReviewResponse
```typescript
interface CompanyReviewResponse {
  id: number;
  jobApplyId: number;
  
  // Company & job details
  companyId: number;
  companyName: string;
  jobTitle: string;
  
  // Reviewer info
  reviewerId: number;
  reviewerName?: string;              // Null if anonymous
  isAnonymous: boolean;
  employmentDuration: string;         // e.g., "3 months", "2 years"
  
  // Review stage
  stage: ReviewStage;
  reviewStatus: ReviewStatus;         // "STAGE_1_ONLY", "STAGE_2_ONLY", "COMPLETE"
  
  // Ratings (1-5 stars)
  overallRating: number;
  workLifeBalance: number;
  compensation: number;
  management: number;
  culture: number;
  careerGrowth: number;
  averageRating: number;              // Calculated average of all categories
  
  // Written feedback
  title: string;
  pros: string;
  cons: string;
  advice?: string;
  wouldRecommend: boolean;
  
  // Metadata
  createdAt: string;
  updatedAt: string;
  
  // Helpfulness votes
  helpfulCount: number;
  notHelpfulCount: number;
  
  // Verification
  isVerifiedEmployee: boolean;        // Always true (employment verified)
}

enum ReviewStatus {
  STAGE_1_ONLY = "STAGE_1_ONLY",      // Only Stage 1 submitted
  STAGE_2_ONLY = "STAGE_2_ONLY",      // Only Stage 2 submitted
  COMPLETE = "COMPLETE"               // Both stages submitted
}
```

### CompanyReviewStatistics
```typescript
interface CompanyReviewStatistics {
  companyId: number;
  companyName: string;
  
  totalReviews: number;
  
  // Overall ratings (1-5 scale)
  averageOverallRating: number;
  averageWorkLifeBalance: number;
  averageCompensation: number;
  averageManagement: number;
  averageCulture: number;
  averageCareerGrowth: number;
  
  // Recommendation stats
  recommendationPercentage: number;   // % of reviewers who would recommend
  
  // Review breakdown
  fiveStarCount: number;
  fourStarCount: number;
  threeStarCount: number;
  twoStarCount: number;
  oneStarCount: number;
  
  // Rating distribution (for charts)
  ratingDistribution: {
    stars: number;                    // 1-5
    count: number;
    percentage: number;
  }[];
}
```

### ReviewEligibilityResponse
```typescript
interface ReviewEligibilityResponse {
  jobApplyId: number;
  isEligible: boolean;
  reason: string;
  
  // Eligibility details
  daysEmployed: number;
  verified30Days: boolean;
  isActive: boolean;                  // Still employed?
  
  // Stage eligibility
  canSubmitStage1: boolean;
  canSubmitStage2: boolean;
  
  // Existing reviews
  hasStage1Review: boolean;
  hasStage2Review: boolean;
  
  // Next steps
  nextAction?: string;                // e.g., "Verify employment to submit Stage 1"
}
```

---

## API Endpoints

### 1. Check Review Eligibility

**Endpoint**: `GET /api/job-applies/{jobApplyId}/review-eligibility`  
**Auth**: `CANDIDATE` role required  
**Description**: Check if candidate is eligible to submit a review and which stage(s).

#### Response: `200 OK` - Eligible for Stage 1
```json
{
  "jobApplyId": 123,
  "isEligible": true,
  "reason": "Employed for 30+ days and verified at 30-day checkpoint",
  
  "daysEmployed": 35,
  "verified30Days": true,
  "isActive": true,
  
  "canSubmitStage1": true,
  "canSubmitStage2": false,
  
  "hasStage1Review": false,
  "hasStage2Review": false,
  
  "nextAction": "You can submit a Stage 1 review (while employed)"
}
```

#### Response: `200 OK` - Eligible for Stage 2
```json
{
  "jobApplyId": 123,
  "isEligible": true,
  "reason": "Employment ended after 30+ days",
  
  "daysEmployed": 120,
  "verified30Days": true,
  "isActive": false,
  
  "canSubmitStage1": false,
  "canSubmitStage2": true,
  
  "hasStage1Review": true,
  "hasStage2Review": false,
  
  "nextAction": "You can submit a Stage 2 review (post-termination reflection)"
}
```

#### Response: `200 OK` - Not Eligible
```json
{
  "jobApplyId": 123,
  "isEligible": false,
  "reason": "Employment duration less than 30 days",
  
  "daysEmployed": 25,
  "verified30Days": false,
  "isActive": false,
  
  "canSubmitStage1": false,
  "canSubmitStage2": false,
  
  "nextAction": "You must be employed for at least 30 days to submit a review"
}
```

---

### 2. Submit Company Review

**Endpoint**: `POST /api/company-reviews`  
**Auth**: `CANDIDATE` role required  
**Description**: Submit a Stage 1 or Stage 2 review.

#### Request: Stage 1 Review
```http
POST /api/company-reviews
Authorization: Bearer {candidate_token}
Content-Type: application/json

{
  "jobApplyId": 123,
  "stage": "STAGE_1",
  
  "overallRating": 4,
  "workLifeBalance": 5,
  "compensation": 3,
  "management": 4,
  "culture": 5,
  "careerGrowth": 3,
  
  "title": "Great work-life balance, competitive salary could be better",
  "pros": "Excellent work-life balance with flexible remote work policy. The team culture is collaborative and supportive. Management is transparent and communicative. Great benefits package including health insurance and learning budget.",
  "cons": "Salary is below market rate for the role. Limited opportunities for quick promotion. Some processes feel bureaucratic and slow. Office location is inconvenient.",
  "advice": "Consider conducting a market analysis for compensation packages. Streamline decision-making processes to reduce bureaucracy. Provide more mentorship programs for career growth.",
  
  "wouldRecommend": true,
  "isAnonymous": false
}
```

#### Response: `201 Created`
```json
{
  "id": 5001,
  "jobApplyId": 123,
  "companyName": "Tech Corp",
  "jobTitle": "Senior Software Engineer",
  
  "reviewerName": "Jane Smith",
  "isAnonymous": false,
  "employmentDuration": "35 days",
  
  "stage": "STAGE_1",
  "reviewStatus": "STAGE_1_ONLY",
  
  "overallRating": 4,
  "averageRating": 4.0,
  
  "title": "Great work-life balance, competitive salary could be better",
  "wouldRecommend": true,
  
  "createdAt": "2025-11-26T10:00:00",
  "isVerifiedEmployee": true,
  
  "message": "Stage 1 review submitted successfully. You can submit a Stage 2 review after your employment ends."
}
```

#### Request: Stage 2 Review
```http
POST /api/company-reviews
Authorization: Bearer {candidate_token}
Content-Type: application/json

{
  "jobApplyId": 123,
  "stage": "STAGE_2",
  
  "overallRating": 3,
  "workLifeBalance": 4,
  "compensation": 2,
  "management": 3,
  "culture": 4,
  "careerGrowth": 2,
  
  "title": "Good experience overall, but limited growth opportunities",
  "pros": "Looking back, the work-life balance was truly exceptional. The people were great to work with and I learned a lot about collaboration. The flexible work policy was a major plus.",
  "cons": "In retrospect, the lack of career growth was the main reason for leaving. Compensation never improved despite strong performance. No clear promotion path. Some decisions felt very slow.",
  "advice": "Invest more in employee development and career progression. Consider creating clearer promotion criteria and timelines. Conduct regular market salary reviews to remain competitive.",
  
  "wouldRecommend": true,
  "isAnonymous": false
}
```

#### Response: `201 Created`
```json
{
  "id": 5001,
  "reviewStatus": "COMPLETE",
  "message": "Stage 2 review submitted successfully. Your complete review is now published.",
  
  "stage1Rating": 4.0,
  "stage2Rating": 3.0,
  "overallRating": 3.5
}
```

---

### 3. Get Company Reviews

**Endpoint**: `GET /api/companies/{companyId}/reviews`  
**Auth**: Public (no auth required)  
**Description**: Get all reviews for a company with pagination.

#### Query Parameters
```
?page=0&size=10                       // Pagination
&sortBy=recent                        // Sort: recent, helpful, rating_high, rating_low
&stage=COMPLETE                       // Filter: STAGE_1_ONLY, STAGE_2_ONLY, COMPLETE
&minRating=3                          // Filter: minimum overall rating (1-5)
```

#### Response: `200 OK`
```json
{
  "companyId": 456,
  "companyName": "Tech Corp",
  "totalReviews": 127,
  
  "reviews": [
    {
      "id": 5001,
      "jobTitle": "Senior Software Engineer",
      "reviewerName": "Jane Smith",
      "isAnonymous": false,
      "employmentDuration": "4 months",
      
      "stage": "COMPLETE",
      "reviewStatus": "COMPLETE",
      
      "overallRating": 3.5,
      "averageRating": 3.5,
      
      "title": "Good experience overall, but limited growth opportunities",
      "pros": "Looking back, the work-life balance was truly exceptional...",
      "cons": "In retrospect, the lack of career growth was the main reason...",
      "wouldRecommend": true,
      
      "createdAt": "2025-11-26T10:00:00",
      "helpfulCount": 12,
      "notHelpfulCount": 2,
      
      "isVerifiedEmployee": true
    }
  ],
  
  "pagination": {
    "currentPage": 0,
    "totalPages": 13,
    "pageSize": 10
  }
}
```

---

### 4. Get Company Review Statistics

**Endpoint**: `GET /api/companies/{companyId}/review-statistics`  
**Auth**: Public (no auth required)  
**Description**: Get aggregated review statistics for a company.

#### Response: `200 OK`
```json
{
  "companyId": 456,
  "companyName": "Tech Corp",
  "totalReviews": 127,
  
  "averageOverallRating": 3.8,
  "averageWorkLifeBalance": 4.2,
  "averageCompensation": 3.4,
  "averageManagement": 3.6,
  "averageCulture": 4.0,
  "averageCareerGrowth": 3.2,
  
  "recommendationPercentage": 78.5,
  
  "fiveStarCount": 35,
  "fourStarCount": 52,
  "threeStarCount": 28,
  "twoStarCount": 9,
  "oneStarCount": 3,
  
  "ratingDistribution": [
    { "stars": 5, "count": 35, "percentage": 27.6 },
    { "stars": 4, "count": 52, "percentage": 40.9 },
    { "stars": 3, "count": 28, "percentage": 22.0 },
    { "stars": 2, "count": 9, "percentage": 7.1 },
    { "stars": 1, "count": 3, "percentage": 2.4 }
  ]
}
```

---

### 5. Get My Reviews (Candidate)

**Endpoint**: `GET /api/candidates/{candidateId}/reviews`  
**Auth**: `CANDIDATE` role required  
**Description**: Get all reviews submitted by the logged-in candidate.

#### Response: `200 OK`
```json
{
  "candidateId": 789,
  "totalReviews": 3,
  
  "reviews": [
    {
      "id": 5001,
      "companyName": "Tech Corp",
      "jobTitle": "Senior Software Engineer",
      "stage": "COMPLETE",
      "overallRating": 3.5,
      "createdAt": "2025-11-26T10:00:00",
      "isAnonymous": false
    },
    {
      "id": 4985,
      "companyName": "StartupXYZ",
      "jobTitle": "Full Stack Developer",
      "stage": "STAGE_1_ONLY",
      "overallRating": 4.5,
      "createdAt": "2025-09-15T14:30:00",
      "isAnonymous": true
    }
  ]
}
```

---

### 6. Update Stage 1 Review

**Endpoint**: `PUT /api/company-reviews/{reviewId}`  
**Auth**: `CANDIDATE` role required  
**Description**: Edit Stage 1 review (only while still employed).

#### Request
```http
PUT /api/company-reviews/5001
Authorization: Bearer {candidate_token}
Content-Type: application/json

{
  "overallRating": 5,
  "workLifeBalance": 5,
  "compensation": 4,
  "management": 5,
  "culture": 5,
  "careerGrowth": 4,
  
  "title": "Excellent workplace with great culture",
  "pros": "Updated: After more time here, I've grown to appreciate...",
  "cons": "Updated: My concerns about compensation were addressed...",
  "wouldRecommend": true
}
```

#### Response: `200 OK`
```json
{
  "id": 5001,
  "message": "Stage 1 review updated successfully",
  "updatedAt": "2025-11-27T09:00:00"
}
```

#### Business Logic
- ✅ Can only edit Stage 1 reviews
- ✅ Can only edit while still employed
- ❌ Cannot edit Stage 2 reviews (final)
- ❌ Cannot edit after termination

---

### 7. Delete Review

**Endpoint**: `DELETE /api/company-reviews/{reviewId}`  
**Auth**: `CANDIDATE` (if owner) or `ADMIN` role required  
**Description**: Delete a review (soft delete, not permanent).

#### Response: `200 OK`
```json
{
  "message": "Review deleted successfully",
  "reviewId": 5001
}
```

---

### 8. Mark Review as Helpful

**Endpoint**: `POST /api/company-reviews/{reviewId}/helpful`  
**Auth**: Any authenticated user  
**Description**: Mark a review as helpful (upvote).

#### Response: `200 OK`
```json
{
  "reviewId": 5001,
  "helpfulCount": 13,
  "message": "Review marked as helpful"
}
```

---

### 9. Mark Review as Not Helpful

**Endpoint**: `POST /api/company-reviews/{reviewId}/not-helpful`  
**Auth**: Any authenticated user  
**Description**: Mark a review as not helpful (downvote).

#### Response: `200 OK`
```json
{
  "reviewId": 5001,
  "notHelpfulCount": 3,
  "message": "Review marked as not helpful"
}
```

---

## Review Eligibility Rules

### Eligibility Matrix

| Scenario | Days Employed | Verified 30 Days | Is Active | Can Submit Stage 1 | Can Submit Stage 2 |
|---------|--------------|-----------------|-----------|-------------------|-------------------|
| Currently employed, < 30 days | 20 | ❌ | ✅ | ❌ | ❌ |
| Currently employed, 30+ days, not verified | 35 | ❌ | ✅ | ❌ | ❌ |
| Currently employed, 30+ days, verified | 35 | ✅ | ✅ | ✅ | ❌ |
| Left before 30 days | 25 | ❌ | ❌ | ❌ | ❌ |
| Left after 30+ days, verified | 120 | ✅ | ❌ | ❌ | ✅ |
| Left after 30+ days, not verified | 120 | ❌ | ❌ | ❌ | ❌ |

### Stage 1 Eligibility Rules

```typescript
function canSubmitStage1Review(verification: EmploymentVerification): boolean {
  return (
    verification.isActive &&              // Must be currently employed
    verification.verified30Days &&        // Must have verified at Day 30
    !hasExistingStage1Review()           // No duplicate Stage 1
  );
}
```

### Stage 2 Eligibility Rules

```typescript
function canSubmitStage2Review(verification: EmploymentVerification): boolean {
  return (
    !verification.isActive &&             // Must have left company
    verification.verified30Days &&        // Must have verified at Day 30
    verification.getDaysEmployed() >= 30 && // Worked at least 30 days
    !hasExistingStage2Review()           // No duplicate Stage 2
  );
}
```

---

## Rating Calculations

### Average Rating Calculation

```typescript
function calculateAverageRating(review: CompanyReview): number {
  const sum = 
    review.overallRating +
    review.workLifeBalance +
    review.compensation +
    review.management +
    review.culture +
    review.careerGrowth;
  
  return Math.round((sum / 6) * 10) / 10;  // Round to 1 decimal place
}
```

### Company Overall Rating

```typescript
function calculateCompanyOverallRating(companyId: number): number {
  const reviews = reviewRepo.findByCompanyId(companyId);
  
  const totalRating = reviews.reduce((sum, review) => {
    return sum + review.averageRating;
  }, 0);
  
  return Math.round((totalRating / reviews.length) * 10) / 10;
}
```

### Recommendation Percentage

```typescript
function calculateRecommendationPercentage(companyId: number): number {
  const reviews = reviewRepo.findByCompanyId(companyId);
  const recommendCount = reviews.filter(r => r.wouldRecommend).length;
  
  return Math.round((recommendCount / reviews.length) * 100 * 10) / 10;
}
```

---

## Business Rules

### Submission Rules

| Rule | Validation |
|------|------------|
| **Minimum employment duration** | 30+ days |
| **Must verify employment** | verified30Days = true |
| **One Stage 1 per job** | Cannot submit duplicate |
| **One Stage 2 per job** | Cannot submit duplicate |
| **Stage 1 only while employed** | isActive = true |
| **Stage 2 only after leaving** | isActive = false |

### Content Rules

| Rule | Validation |
|------|------------|
| **Title length** | 10-100 characters |
| **Pros minimum** | 50 characters |
| **Cons minimum** | 50 characters |
| **Advice minimum** | 50 characters (if provided) |
| **Rating range** | 1-5 stars (integer) |

### Edit Rules

| Rule | Validation |
|------|------------|
| **Can edit Stage 1** | Only while employed |
| **Cannot edit Stage 2** | Final and immutable |
| **Cannot edit after termination** | Stage 1 becomes read-only |
| **Can delete own reviews** | Soft delete within 30 days |

### Anonymity Rules

| Rule | Validation |
|------|------------|
| **Can be anonymous** | Name hidden from public |
| **Employment verified badge** | Always shown (cannot hide) |
| **Admin can see identity** | For moderation |

---

## Error Handling

### Common Error Responses

#### 400 Bad Request - Not Eligible
```json
{
  "error": "REVIEW_NOT_ELIGIBLE",
  "message": "You are not eligible to submit a review for this job application",
  "reason": "Employment duration less than 30 days",
  "daysEmployed": 25,
  "requirementDays": 30
}
```

#### 400 Bad Request - Already Submitted Stage
```json
{
  "error": "REVIEW_ALREADY_EXISTS",
  "message": "You have already submitted a Stage 1 review for this job",
  "existingReviewId": 5001,
  "stage": "STAGE_1"
}
```

#### 400 Bad Request - Cannot Edit Stage 2
```json
{
  "error": "CANNOT_EDIT_STAGE_2_REVIEW",
  "message": "Stage 2 reviews cannot be edited after submission",
  "reviewId": 5001
}
```

#### 400 Bad Request - Content Too Short
```json
{
  "error": "CONTENT_TOO_SHORT",
  "message": "Pros section must be at least 50 characters",
  "field": "pros",
  "currentLength": 32,
  "requiredLength": 50
}
```

#### 403 Forbidden - Not Review Owner
```json
{
  "error": "UNAUTHORIZED_REVIEW_EDIT",
  "message": "You can only edit your own reviews",
  "reviewId": 5001
}
```

---

## Frontend Integration Checklist

### Review Eligibility Check Page
- [ ] Call eligibility API before showing review form
- [ ] Display eligibility status (ELIGIBLE / NOT_ELIGIBLE / PENDING)
- [ ] Show reason for ineligibility
- [ ] Display countdown: "Verify employment to unlock reviews"

### Stage 1 Review Form (Day 30+)
- [ ] 6 rating categories (star selection, 1-5)
- [ ] Title input (10-100 chars, character counter)
- [ ] Pros textarea (min 50 chars, character counter)
- [ ] Cons textarea (min 50 chars, character counter)
- [ ] Advice textarea (optional, min 50 chars)
- [ ] Would recommend? (Yes/No toggle)
- [ ] Anonymous? (checkbox)
- [ ] "Submit Stage 1 Review" button
- [ ] Note: "You can edit this until your employment ends"

### Stage 2 Review Form (Post-Termination)
- [ ] Same as Stage 1 form
- [ ] Display Stage 1 review (read-only) for reference
- [ ] "Submit Stage 2 Review" button
- [ ] Warning: "Stage 2 reviews cannot be edited"

### Company Review List Page
- [ ] Display all reviews with pagination
- [ ] Sort dropdown (Recent, Helpful, Rating High/Low)
- [ ] Filter by stage (All, Stage 1 Only, Complete)
- [ ] Filter by rating (All, 5-star, 4-star, etc.)
- [ ] Show reviewer name (or "Anonymous")
- [ ] Show employment duration
- [ ] Show "Verified Employee" badge
- [ ] Helpful/Not Helpful buttons
- [ ] Expand/collapse full review

### Company Statistics Dashboard
- [ ] Overall rating (large number + stars)
- [ ] Recommendation percentage (with icon)
- [ ] Rating breakdown by category (bar chart)
- [ ] Rating distribution (histogram: 5-star, 4-star, etc.)
- [ ] Total review count

### My Reviews Page (Candidate)
- [ ] List all submitted reviews
- [ ] Show stage status (Stage 1 Only, Complete)
- [ ] Edit button (only for Stage 1 while employed)
- [ ] Delete button (within 30 days)
- [ ] View full review link

---

**End of Company Review API Documentation**
