# Candidate Entitlement Controller API Documentation

## Overview
Controller manages entitlement checking functionalities for candidates. It verifies whether a candidate has access to various premium features based on their current package subscription (FREE, PLUS, or PREMIUM).

**Base URL:** `/api/candidate-entitlement`

**Tag:** Candidate - Entitlement

---

## API Endpoints

### 1. Check Roadmap Recommendation Entitlement

#### Endpoint
```
GET /api/candidate-entitlement/roadmap-recommendation-checker
```

#### Purpose
Check if the candidate has permission to use the AI Roadmap Recommendation feature based on their current package subscription.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:** None

**Request Body:** None

**Example Request:**
```bash
GET /api/candidate-entitlement/roadmap-recommendation-checker
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK) - Has Access:**
```json
{
  "code": 200,
  "message": "success",
  "result": true
}
```

**Success Response (200 OK) - No Access:**
```json
{
  "code": 200,
  "message": "success",
  "result": false
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (Boolean): 
  - `true` = Candidate can use AI Roadmap Recommendation feature
  - `false` = Feature not available in candidate's current package

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not a Candidate:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current candidate from JWT token
2. Check if candidate is on FREE package:
   - If candidateInvoice is null or not active → FREE package
   - Otherwise → Use package from active invoice
3. Query entitlement table for package + `AI_ROADMAP` entitlement code
4. If entitlement exists and is enabled → Return `true`
5. Otherwise → Return `false`

#### Notes
- Required role: `ROLE_CANDIDATE`
- **Must call this API before calling Roadmap Recommendation API**
- FREE package users may have limited or no access to this feature
- Entitlement is checked at the package level, not usage count

---

### 2. Check Job Recommendation Entitlement

#### Endpoint
```
GET /api/candidate-entitlement/job-recommendation-checker
```

#### Purpose
Check if the candidate has permission to use the AI Job Recommendation feature based on their current package subscription.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:** None

**Request Body:** None

**Example Request:**
```bash
GET /api/candidate-entitlement/job-recommendation-checker
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK) - Has Access:**
```json
{
  "code": 200,
  "message": "success",
  "result": true
}
```

**Success Response (200 OK) - No Access:**
```json
{
  "code": 200,
  "message": "success",
  "result": false
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (Boolean): 
  - `true` = Candidate can use AI Job Recommendation feature
  - `false` = Feature not available in candidate's current package

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not a Candidate:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current candidate from JWT token
2. Check if candidate is on FREE package
3. Query entitlement table for package + `JOB_RECOMMENDATION` entitlement code
4. If entitlement exists and is enabled → Return `true`
5. Otherwise → Return `false`

#### Notes
- Required role: `ROLE_CANDIDATE`
- **Must call this API before calling Job Recommendation API**
- Determines access to AI-powered job matching features

---

### 3. Check AI Analyzer Entitlement

#### Endpoint
```
GET /api/candidate-entitlement/ai-analyzer-checker
```

#### Purpose
Check if the candidate has permission to use the AI Analyzer feature (e.g., CV analysis, skill gap analysis) based on their current package subscription.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:** None

**Request Body:** None

**Example Request:**
```bash
GET /api/candidate-entitlement/ai-analyzer-checker
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK) - Has Access:**
```json
{
  "code": 200,
  "message": "success",
  "result": true
}
```

**Success Response (200 OK) - No Access:**
```json
{
  "code": 200,
  "message": "success",
  "result": false
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (Boolean): 
  - `true` = Candidate can use AI Analyzer feature
  - `false` = Feature not available in candidate's current package

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not a Candidate:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current candidate from JWT token
2. Check if candidate is on FREE package
3. Query entitlement table for package + `AI_ANALYZER` entitlement code
4. If entitlement exists and is enabled → Return `true`
5. Otherwise → Return `false`

#### Notes
- Required role: `ROLE_CANDIDATE`
- **Must call this API before calling AI Analyzer API**
- AI Analyzer typically includes CV analysis and skill recommendations

---

### 4. Check CV Builder Entitlement

#### Endpoint
```
GET /api/candidate-entitlement/cv-builder-checker
```

#### Purpose
Check if the candidate can create a new CV based on their current package limits. Different packages have different CV creation limits.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:** None

**Request Body:** None

**Example Request:**
```bash
GET /api/candidate-entitlement/cv-builder-checker
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK) - Can Create:**
```json
{
  "code": 200,
  "message": "success",
  "result": true
}
```

**Success Response (200 OK) - Cannot Create:**
```json
{
  "code": 200,
  "message": "success",
  "result": false
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (Boolean): 
  - `true` = Candidate can create a new CV
  - `false` = Candidate has reached CV creation limit for their package

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not a Candidate:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current candidate from JWT token
2. Count existing CVs/resumes created by candidate
3. Determine current package (FREE if no active invoice)
4. Query entitlement for `CV_BUILDER` with package
5. Check if entitlement is enabled
6. **If limitValue = 0 → Unlimited CVs** → Return `true`
7. **If limitValue > 0 → Compare with current count:**
   - If currentCvCount < limitValue → Return `true`
   - Otherwise → Return `false`

#### Notes
- Required role: `ROLE_CANDIDATE`
- **Must call this API before showing "Create New CV" button**
- **Package Limits (typical):**
  - FREE: Maximum 1 CV
  - PLUS: Maximum 3 CVs
  - PREMIUM: Unlimited (limitValue = 0)
- This is a **count-based** entitlement check

---

### 5. Check Apply Job Entitlement

#### Endpoint
```
GET /api/candidate-entitlement/apply-job-checker
```

#### Purpose
Check if the candidate can apply for more jobs this month based on their package limits. Different packages have different monthly job application quotas.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:** None

**Request Body:** None

**Example Request:**
```bash
GET /api/candidate-entitlement/apply-job-checker
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK) - Can Apply:**
```json
{
  "code": 200,
  "message": "success",
  "result": true
}
```

**Success Response (200 OK) - Cannot Apply:**
```json
{
  "code": 200,
  "message": "success",
  "result": false
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (Boolean): 
  - `true` = Candidate can apply for more jobs this month
  - `false` = Candidate has reached monthly application limit

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not a Candidate:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current candidate from JWT token
2. Get current month and year
3. Count job applications submitted by candidate **in current month**
4. Determine current package (FREE if no active invoice)
5. Query entitlement for `APPLY_JOB` with package
6. Check if entitlement is enabled
7. **If limitValue = 0 → Unlimited applications** → Return `true`
8. **If limitValue > 0 → Compare with monthly count:**
   - If appliedCountThisMonth < limitValue → Return `true`
   - Otherwise → Return `false`

#### Notes
- Required role: `ROLE_CANDIDATE`
- **Must call this API before showing "Apply" button**
- **Package Limits (typical):**
  - FREE: Maximum 5 job applications per month
  - PLUS: Maximum 20 job applications per month
  - PREMIUM: Unlimited (limitValue = 0)
- This is a **monthly quota-based** entitlement check
- Counter resets at the beginning of each month

---

### 6. Check Recruiter Info Entitlement

#### Endpoint
```
GET /api/candidate-entitlement/recruiter-info-checker
```

#### Purpose
Check if the candidate has permission to view detailed recruiter/company information based on their current package subscription.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:** None

**Request Body:** None

**Example Request:**
```bash
GET /api/candidate-entitlement/recruiter-info-checker
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK) - Has Access:**
```json
{
  "code": 200,
  "message": "success",
  "result": true
}
```

**Success Response (200 OK) - No Access:**
```json
{
  "code": 200,
  "message": "success",
  "result": false
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (Boolean): 
  - `true` = Candidate can view detailed recruiter information
  - `false` = Feature not available in candidate's current package

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not a Candidate:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current candidate from JWT token
2. Check if candidate is on FREE package
3. Query entitlement table for package + `RECRUITER_INFO` entitlement code
4. If entitlement exists and is enabled → Return `true`
5. Otherwise → Return `false`

#### Notes
- Required role: `ROLE_CANDIDATE`
- **Must call this API before displaying detailed recruiter info**
- May control access to company profiles, contact information, or company reviews

---

### 7. Check CV Download Entitlement

#### Endpoint
```
GET /api/candidate-entitlement/cv-download-checker
```

#### Purpose
Check if the candidate has permission to download their CV in various formats (PDF, DOCX, etc.) based on their current package subscription.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:** None

**Request Body:** None

**Example Request:**
```bash
GET /api/candidate-entitlement/cv-download-checker
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK) - Has Access:**
```json
{
  "code": 200,
  "message": "success",
  "result": true
}
```

**Success Response (200 OK) - No Access:**
```json
{
  "code": 200,
  "message": "success",
  "result": false
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (Boolean): 
  - `true` = Candidate can download their CV
  - `false` = Download feature not available in candidate's current package

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not a Candidate:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current candidate from JWT token
2. Check if candidate is on FREE package
3. Query entitlement table for package + `CV_DOWNLOAD` entitlement code
4. If entitlement exists and is enabled → Return `true`
5. Otherwise → Return `false`

#### Notes
- Required role: `ROLE_CANDIDATE`
- **Must call this API before showing "Download CV" button**
- May control access to different export formats (PDF, DOCX, etc.)
- FREE package users may need to upgrade to download CVs

---

## Entitlement Codes Reference

**EntitlementCode constants used in the system:**
- **AI_ROADMAP**: AI-powered roadmap recommendation feature
- **JOB_RECOMMENDATION**: AI-powered job matching and recommendation
- **AI_ANALYZER**: CV analysis and skill gap analysis
- **CV_BUILDER**: CV creation functionality (with count limit)
- **APPLY_JOB**: Job application feature (with monthly quota)
- **RECRUITER_INFO**: Access to detailed recruiter/company information
- **CV_DOWNLOAD**: CV export and download functionality

## Package Codes Reference

**PackageCode constants:**
- **FREE**: Default free package for all new users
- **PLUS**: Mid-tier paid package with increased limits
- **PREMIUM**: Top-tier package with unlimited or maximum features

## Common Entitlement Patterns

### 1. Simple Enable/Disable Check
Used by: Roadmap Recommendation, Job Recommendation, AI Analyzer, Recruiter Info, CV Download

**Logic:**
- Check if entitlement exists for current package
- Check if entitlement is enabled
- Return true/false

### 2. Count-Based Limit Check
Used by: CV Builder

**Logic:**
- Count existing resources (CVs)
- Check against package limit
- If limit = 0 → Unlimited
- If limit > 0 → Compare count < limit

### 3. Monthly Quota Check
Used by: Apply Job

**Logic:**
- Count usage in current month
- Check against monthly limit
- If limit = 0 → Unlimited
- If limit > 0 → Compare monthly count < limit
- Resets at beginning of each month

## Typical Package Configuration

| Feature | FREE | PLUS | PREMIUM |
|---------|------|------|---------|
| AI Roadmap | ❌ Disabled | ✅ Enabled | ✅ Enabled |
| Job Recommendation | ❌ Disabled | ✅ Enabled | ✅ Enabled |
| AI Analyzer | ❌ Disabled | ✅ Enabled | ✅ Enabled |
| CV Builder | 1 CV | 3 CVs | ♾️ Unlimited |
| Apply Job | 5/month | 20/month | ♾️ Unlimited |
| Recruiter Info | ❌ Disabled | ✅ Enabled | ✅ Enabled |
| CV Download | ❌ Disabled | ✅ Enabled | ✅ Enabled |

## Common Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | User does not have access permission (not a CANDIDATE) |
| 500 | Internal Server Error | Server error |

## Security

- All endpoints require authentication and role `CANDIDATE`
- Uses JWT Bearer token for authentication
- Security is implemented through `@PreAuthorize("hasRole('CANDIDATE')")` at service level
- Each check is scoped to the authenticated candidate (cannot check other users)

## Database Dependencies

- **CandidateEntitlementPackage entity**: Junction table linking packages to entitlements with limits
- **CandidatePackage entity**: Package definitions (FREE, PLUS, PREMIUM)
- **CandidateEntitlement entity**: Entitlement definitions (feature codes)
- **CandidateInvoice entity**: Active subscription information
- **Candidate entity**: User profile with CV count
- **JobApply entity**: Job application records for monthly counting

## Frontend Integration Examples

### Example 1: Check Before Using Feature
```javascript
async function useAIRoadmapRecommendation() {
  // Check entitlement first
  const canUse = await fetch('/api/candidate-entitlement/roadmap-recommendation-checker', {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json());
  
  if (!canUse.result) {
    showUpgradeModal('AI Roadmap Recommendation is only available in PLUS and PREMIUM packages');
    return;
  }
  
  // Proceed with feature
  const roadmaps = await getRoadmapRecommendations();
  displayRoadmaps(roadmaps);
}
```

### Example 2: Conditional UI Rendering
```javascript
async function renderJobApplicationButton(jobId) {
  const canApply = await fetch('/api/candidate-entitlement/apply-job-checker', {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json());
  
  if (canApply.result) {
    // Show active apply button
    return `<button onclick="applyJob(${jobId})">Apply Now</button>`;
  } else {
    // Show disabled button with upgrade prompt
    return `
      <button disabled>Monthly Limit Reached</button>
      <a href="/upgrade">Upgrade to apply more jobs</a>
    `;
  }
}
```

### Example 3: CV Builder Limit Display
```javascript
async function displayCVBuilderStatus() {
  const canCreate = await fetch('/api/candidate-entitlement/cv-builder-checker', {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json());
  
  const currentCount = await getCurrentCVCount();
  const packageLimit = await getPackageLimit(); // e.g., 1, 3, or "Unlimited"
  
  document.getElementById('cv-status').innerHTML = `
    <p>CVs Created: ${currentCount} / ${packageLimit}</p>
    ${canCreate.result 
      ? '<button onclick="createNewCV()">Create New CV</button>'
      : '<button disabled>Limit Reached - Upgrade to create more</button>'
    }
  `;
}
```

### Example 4: Batch Check All Entitlements
```javascript
async function checkAllEntitlements() {
  const endpoints = [
    'roadmap-recommendation-checker',
    'job-recommendation-checker',
    'ai-analyzer-checker',
    'cv-builder-checker',
    'apply-job-checker',
    'recruiter-info-checker',
    'cv-download-checker'
  ];
  
  const results = await Promise.all(
    endpoints.map(endpoint => 
      fetch(`/api/candidate-entitlement/${endpoint}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      }).then(r => r.json())
    )
  );
  
  return {
    canUseAIRoadmap: results[0].result,
    canUseJobRec: results[1].result,
    canUseAIAnalyzer: results[2].result,
    canCreateCV: results[3].result,
    canApplyJob: results[4].result,
    canViewRecruiterInfo: results[5].result,
    canDownloadCV: results[6].result
  };
}
```

## Best Practices

1. **Check Before Action**: Always call the appropriate checker endpoint before allowing users to access premium features
2. **Cache Results**: Consider caching entitlement check results for a short period (e.g., 5 minutes) to reduce API calls
3. **Clear UI**: Show users why they can't access a feature and provide clear upgrade path
4. **Proactive Checking**: Check entitlements when rendering UI, not just when user clicks
5. **Graceful Degradation**: If check fails, default to restricted access (fail-safe)
6. **Monthly Reset Communication**: For quota-based features, show users when their quota resets
7. **Real-time Updates**: After package upgrade/purchase, re-check all entitlements to update UI

## Important Notes

- All checker endpoints return boolean values, never throw exceptions for access denial
- FREE package is the default for users without active invoices
- Entitlements are checked in real-time based on current package status
- Monthly quotas (like Apply Job) reset automatically at the start of each month
- Count-based limits (like CV Builder) are lifetime counts within the package
- Package upgrade/downgrade immediately affects entitlement checks
- Cancelled subscriptions revert user to FREE package

