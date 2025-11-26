# Recruiter Entitlement Controller API Documentation

## Overview
Controller manages entitlement checking functionalities for recruiters. It verifies whether a recruiter has access to various premium features based on their current package subscription (BASIC, PROFESSIONAL, or ENTERPRISE).

**Base URL:** `/api/recruiter-entitlement`

**Tag:** Recruiter - Entitlement

---

## API Endpoints

### 1. Check AI Matching Entitlement

#### Endpoint
```
GET /api/recruiter-entitlement/ai-matching-checker
```

#### Purpose
Check if the recruiter has permission to use the AI Matching feature (AI-powered candidate matching and recommendation) based on their current package subscription.

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
GET /api/recruiter-entitlement/ai-matching-checker
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
  - `true` = Recruiter can use AI Matching feature
  - `false` = Feature not available in recruiter's current package

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not a Recruiter:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current recruiter from JWT token via `coachUtil.getCurrentRecruiter()`
2. Check if recruiter is on BASIC package:
   - If recruiterInvoice is null or not active → BASIC package
   - Otherwise → Use package from active invoice
3. Query entitlement table for package + `AI_MATCHING` entitlement code
4. If entitlement exists and is enabled → Return `true`
5. Otherwise → Return `false`

#### Notes
- Required role: `ROLE_RECRUITER`
- **Must call this API before calling AI Matching API**
- BASIC package users may have limited or no access to this feature
- AI Matching feature typically helps recruiters find best-fit candidates using AI algorithms
- Entitlement is checked at the package level (simple enable/disable)

---

### 2. Check Job Posting Entitlement

#### Endpoint
```
GET /api/recruiter-entitlement/job-posting-checker
```

#### Purpose
Check if the recruiter can post more jobs this month based on their package limits. Different packages have different monthly job posting quotas.

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
GET /api/recruiter-entitlement/job-posting-checker
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK) - Can Post:**
```json
{
  "code": 200,
  "message": "success",
  "result": true
}
```

**Success Response (200 OK) - Cannot Post:**
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
  - `true` = Recruiter can post more jobs this month
  - `false` = Recruiter has reached monthly job posting limit

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not a Recruiter:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current recruiter from JWT token
2. Get current month and year
3. Count job postings created by recruiter **in current month**
4. Determine current package (BASIC if no active invoice)
5. Query entitlement for `JOB_POSTING` with package
6. Check if entitlement is enabled
7. **If limitValue = 0 → Unlimited job postings** → Return `true`
8. **If limitValue > 0 → Compare with monthly count:**
   - If postedCountThisMonth < limitValue → Return `true`
   - Otherwise → Return `false`

#### Notes
- Required role: `ROLE_RECRUITER`
- **Must call this API before showing "Post Job" button or form**
- **Package Limits (typical):**
  - BASIC: Maximum 5 job postings per month
  - PROFESSIONAL: Maximum 20 job postings per month
  - ENTERPRISE: Unlimited (limitValue = 0)
- This is a **monthly quota-based** entitlement check
- Counter resets at the beginning of each month
- Applies to new job postings only (editing existing jobs doesn't count)

---

## Entitlement Codes Reference

**RecruiterEntitlementCode constants used in the system:**
- **AI_MATCHING**: AI-powered candidate matching and recommendation feature
- **JOB_POSTING**: Job posting creation functionality (with monthly quota)

## Package Codes Reference

**RecruiterPackageCode constants:**
- **BASIC**: Default free package for all new recruiters
- **PROFESSIONAL**: Mid-tier paid package with increased limits
- **ENTERPRISE**: Top-tier package with unlimited or maximum features

## Entitlement Patterns

### 1. Simple Enable/Disable Check
Used by: AI Matching

**Logic:**
- Check if entitlement exists for current package
- Check if entitlement is enabled
- Return true/false

### 2. Monthly Quota Check
Used by: Job Posting

**Logic:**
- Count usage in current month
- Check against monthly limit
- If limit = 0 → Unlimited
- If limit > 0 → Compare monthly count < limit
- Resets at beginning of each month

## Typical Package Configuration

| Feature | BASIC | PROFESSIONAL | ENTERPRISE |
|---------|-------|--------------|------------|
| AI Matching | ❌ Disabled | ✅ Enabled | ✅ Enabled |
| Job Posting | 5/month | 20/month | ♾️ Unlimited |
| Candidate Search | Limited | Advanced | Full Access |
| Analytics | Basic | Advanced | Premium |

## Common Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | User does not have access permission (not a RECRUITER) |
| 500 | Internal Server Error | Server error |

## Security

- All endpoints require authentication and role `RECRUITER`
- Uses JWT Bearer token for authentication
- Security is implemented through `@PreAuthorize("hasRole('RECRUITER')")` at service level
- Each check is scoped to the authenticated recruiter (cannot check other users)

## Database Dependencies

- **RecruiterEntitlementPackage entity**: Junction table linking packages to entitlements with limits
- **RecruiterPackage entity**: Package definitions (BASIC, PROFESSIONAL, ENTERPRISE)
- **RecruiterEntitlement entity**: Entitlement definitions (feature codes)
- **RecruiterInvoice entity**: Active subscription information
- **Recruiter entity**: Recruiter profile
- **JobPosting entity**: Job posting records for monthly counting

## Frontend Integration Examples

### Example 1: Check Before Using AI Matching
```javascript
async function useAIMatching() {
  // Check entitlement first
  const canUse = await fetch('/api/recruiter-entitlement/ai-matching-checker', {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json());
  
  if (!canUse.result) {
    showUpgradeModal('AI Matching is only available in PROFESSIONAL and ENTERPRISE packages');
    return;
  }
  
  // Proceed with feature
  const candidates = await getAIMatchedCandidates();
  displayCandidates(candidates);
}
```

### Example 2: Conditional Job Posting Button
```javascript
async function renderPostJobButton() {
  const canPost = await fetch('/api/recruiter-entitlement/job-posting-checker', {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json());
  
  const button = document.getElementById('postJobBtn');
  
  if (canPost.result) {
    // Show active post job button
    button.disabled = false;
    button.textContent = 'Post New Job';
    button.onclick = () => showPostJobForm();
  } else {
    // Show disabled button with upgrade prompt
    button.disabled = true;
    button.textContent = 'Monthly Limit Reached';
    
    // Show upgrade link
    document.getElementById('upgradePrompt').innerHTML = `
      <p>You've reached your monthly job posting limit.</p>
      <a href="/upgrade" class="btn-upgrade">Upgrade to post more jobs</a>
    `;
  }
}
```

### Example 3: Job Posting Limit Display
```javascript
async function displayJobPostingStatus() {
  const canPost = await fetch('/api/recruiter-entitlement/job-posting-checker', {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json());
  
  // Get current month's posting count
  const currentCount = await getCurrentMonthJobPostings();
  
  // Get package limit from backend or config
  const packageLimit = await getPackageLimit(); // e.g., 5, 20, or "Unlimited"
  
  document.getElementById('job-posting-status').innerHTML = `
    <div class="quota-info">
      <h4>Job Postings This Month</h4>
      <p class="count">${currentCount} / ${packageLimit}</p>
      ${canPost.result 
        ? '<button onclick="postNewJob()" class="btn-primary">Post New Job</button>'
        : '<button disabled class="btn-disabled">Limit Reached</button>'
      }
      ${!canPost.result && packageLimit !== 'Unlimited'
        ? '<a href="/upgrade" class="upgrade-link">Upgrade for more postings</a>'
        : ''
      }
    </div>
  `;
}
```

### Example 4: Batch Check All Entitlements
```javascript
async function checkRecruiterEntitlements() {
  const endpoints = [
    'ai-matching-checker',
    'job-posting-checker'
  ];
  
  const results = await Promise.all(
    endpoints.map(endpoint => 
      fetch(`/api/recruiter-entitlement/${endpoint}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      }).then(r => r.json())
    )
  );
  
  return {
    canUseAIMatching: results[0].result,
    canPostJob: results[1].result
  };
}

// Usage in dashboard
async function loadRecruiterDashboard() {
  const entitlements = await checkRecruiterEntitlements();
  
  // Show/hide AI Matching feature
  document.getElementById('ai-matching-section').style.display = 
    entitlements.canUseAIMatching ? 'block' : 'none';
  
  // Enable/disable post job button
  document.getElementById('post-job-btn').disabled = !entitlements.canPostJob;
  
  // Show appropriate upgrade prompts
  if (!entitlements.canUseAIMatching) {
    showFeatureLockedBanner('AI Matching', 'PROFESSIONAL');
  }
  
  if (!entitlements.canPostJob) {
    showQuotaExceededBanner('Job Postings', getResetDate());
  }
}

function getResetDate() {
  const now = new Date();
  const nextMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1);
  return nextMonth.toLocaleDateString();
}
```

### Example 5: Progressive Feature Disclosure
```javascript
// Show features based on entitlements
async function renderRecruiterFeatures() {
  const entitlements = await checkRecruiterEntitlements();
  
  const features = [
    {
      id: 'job-posting',
      title: 'Post Jobs',
      available: entitlements.canPostJob,
      action: 'postJob()',
      upgradeMessage: 'Upgrade to post more jobs per month'
    },
    {
      id: 'ai-matching',
      title: 'AI Candidate Matching',
      available: entitlements.canUseAIMatching,
      action: 'openAIMatching()',
      upgradeMessage: 'Upgrade to use AI-powered candidate matching'
    }
  ];
  
  const container = document.getElementById('features-container');
  
  container.innerHTML = features.map(feature => `
    <div class="feature-card ${feature.available ? '' : 'locked'}">
      <h3>${feature.title}</h3>
      ${feature.available 
        ? `<button onclick="${feature.action}" class="btn-primary">Use Feature</button>`
        : `
          <div class="locked-overlay">
            <span class="lock-icon">🔒</span>
            <p>${feature.upgradeMessage}</p>
            <a href="/upgrade" class="btn-upgrade">Upgrade Now</a>
          </div>
        `
      }
    </div>
  `).join('');
}
```

### Example 6: Real-time Quota Display
```javascript
// Display remaining job postings
async function displayRemainingQuota() {
  const canPost = await fetch('/api/recruiter-entitlement/job-posting-checker', {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json());
  
  // Fetch additional info from backend
  const quotaInfo = await fetch('/api/recruiter/quota-info', {
    headers: { 'Authorization': `Bearer ${token}` }
  }).then(r => r.json());
  
  const { usedThisMonth, totalLimit } = quotaInfo.result;
  const remaining = totalLimit === 0 ? 'Unlimited' : totalLimit - usedThisMonth;
  
  // Update UI
  document.getElementById('quota-display').innerHTML = `
    <div class="quota-meter">
      <div class="quota-bar" style="width: ${totalLimit === 0 ? 100 : (usedThisMonth / totalLimit) * 100}%"></div>
    </div>
    <p class="quota-text">
      ${usedThisMonth} used ${totalLimit === 0 ? '(Unlimited)' : `/ ${totalLimit} this month`}
    </p>
    <p class="quota-remaining">
      ${remaining === 'Unlimited' 
        ? 'Unlimited postings available' 
        : `${remaining} postings remaining this month`
      }
    </p>
    ${!canPost.result && totalLimit !== 0
      ? '<p class="quota-reset">Resets on: ' + getResetDate() + '</p>'
      : ''
    }
  `;
}
```

## Best Practices

1. **Check Before Action**: Always call the appropriate checker endpoint before allowing recruiters to access premium features
2. **Cache Results**: Consider caching entitlement check results for a short period (e.g., 5 minutes) to reduce API calls
3. **Clear UI**: Show recruiters why they can't access a feature and provide clear upgrade path
4. **Proactive Checking**: Check entitlements when rendering UI, not just when user clicks
5. **Graceful Degradation**: If check fails, default to restricted access (fail-safe)
6. **Monthly Reset Communication**: Show recruiters when their quota resets for monthly limits
7. **Real-time Updates**: After package upgrade/purchase, re-check all entitlements to update UI
8. **Progressive Disclosure**: Show locked features with upgrade prompts rather than hiding them completely

## Workflow Examples

### Scenario 1: Recruiter on BASIC Package Tries to Use AI Matching
```
1. Recruiter clicks "Find Candidates with AI" button
2. Frontend calls GET /api/recruiter-entitlement/ai-matching-checker
3. Backend returns { "result": false } (BASIC package doesn't have AI Matching)
4. Frontend shows upgrade modal:
   "AI Matching is available in PROFESSIONAL and ENTERPRISE packages"
5. Recruiter clicks "Upgrade" and proceeds to payment
6. After successful payment, entitlement check returns true
7. Recruiter can now access AI Matching feature
```

### Scenario 2: Recruiter Reaches Monthly Job Posting Limit
```
1. Recruiter (BASIC package, 5 jobs/month limit) has posted 5 jobs this month
2. Recruiter clicks "Post New Job"
3. Frontend calls GET /api/recruiter-entitlement/job-posting-checker
4. Backend checks: 5 jobs posted this month, limit is 5
5. Backend returns { "result": false }
6. Frontend disables "Post Job" button and shows message:
   "You've reached your monthly limit of 5 job postings"
   "Resets on: December 1, 2025"
   "Upgrade to PROFESSIONAL for 20 jobs/month"
7. On December 1st, counter resets and recruiter can post 5 more jobs
```

### Scenario 3: Enterprise Package with Unlimited Features
```
1. Recruiter on ENTERPRISE package wants to post 50th job this month
2. Frontend calls GET /api/recruiter-entitlement/job-posting-checker
3. Backend checks: 49 jobs posted, but limitValue = 0 (unlimited)
4. Backend returns { "result": true }
5. Frontend allows recruiter to post job without restriction
6. No upgrade prompts or warnings shown
```

## Important Notes

- All checker endpoints return boolean values, never throw exceptions for access denial
- BASIC package is the default for recruiters without active invoices
- Entitlements are checked in real-time based on current package status
- **Monthly quotas** (like Job Posting) reset automatically at the start of each month
- Package upgrade/downgrade immediately affects entitlement checks
- Cancelled subscriptions revert recruiter to BASIC package
- The system tracks month and year to ensure accurate monthly quota counting
- Job posting edits don't count toward the monthly limit, only new postings
- Always check entitlements on the backend as well to prevent unauthorized access

## Comparison: Recruiter vs Candidate Entitlements

| Aspect | Recruiter Entitlements | Candidate Entitlements |
|--------|----------------------|----------------------|
| Default Package | BASIC | FREE |
| AI Feature | AI Matching | AI Roadmap, AI Analyzer |
| Main Quota | Job Postings (5/20/unlimited) | Job Applications (5/20/unlimited) |
| Count-based | Job Postings (monthly) | CV Builder (lifetime) |
| Typical Tiers | BASIC/PROFESSIONAL/ENTERPRISE | FREE/PLUS/PREMIUM |

## Related Endpoints

While not in this controller, recruiters may also need:
- `POST /api/recruiter-payment` - Create payment for package upgrade
- `GET /api/recruiter-invoice/my-invoice` - View current subscription
- `GET /api/package/recruiter` - View available packages
- `DELETE /api/recruiter-invoice` - Cancel subscription

## API Usage Flow

```
Recruiter Dashboard Load
    ↓
Check All Entitlements (batch)
    ↓
Render UI based on results
    ↓
[User clicks "Use Feature"]
    ↓
Check specific entitlement
    ↓
If true → Allow access
If false → Show upgrade prompt
```

## Testing Recommendations

### Test Cases:

1. **BASIC Package Tests:**
   - AI Matching should return false
   - Job Posting should return true (if < 5 this month)
   - Job Posting should return false (if = 5 this month)

2. **PROFESSIONAL Package Tests:**
   - AI Matching should return true
   - Job Posting should return true (if < 20 this month)
   - Job Posting should return false (if = 20 this month)

3. **ENTERPRISE Package Tests:**
   - AI Matching should return true
   - Job Posting should always return true (unlimited)

4. **Monthly Reset Test:**
   - Post 5 jobs in November (BASIC package)
   - Job Posting checker returns false
   - Wait until December 1st
   - Job Posting checker returns true again

5. **Package Upgrade Test:**
   - Start with BASIC package
   - AI Matching returns false
   - Upgrade to PROFESSIONAL
   - AI Matching returns true immediately

