# Recruiter Invoice Controller API Documentation

## Overview
Controller manages invoice/order functionalities for recruiters, including viewing active invoices, checking package status, and canceling subscriptions.

**Base URL:** `/api/recruiter-invoice`

**Tag:** Recruiter - Invoice

---

## API Endpoints

### 1. Cancel My Invoice

#### Endpoint
```
DELETE /api/recruiter-invoice
```

#### Purpose
Cancel the recruiter's current active package/subscription. This sets the invoice status to CANCELLED, marks it as inactive, and records the cancellation date.

#### Request

**Method:** `DELETE`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:** None

**Request Body:** None

**Example Request:**
```bash
DELETE /api/recruiter-invoice
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success"
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message

#### Error Response

**404 Not Found - Recruiter Invoice Not Found:**
```json
{
  "code": 404,
  "message": "RECRUITER_INVOICE_NOT_FOUND",
  "result": null
}
```

**400 Bad Request - Cannot Delete Invoice:**
```json
{
  "code": 400,
  "message": "CANNOT_DELETE_MY_RECRUITER_INVOICE",
  "result": null
}
```

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

1. Get current recruiter information from JWT token via `coachUtil.getCurrentRecruiter()`
2. Query database to find active invoice for the recruiter
   - If no active invoice found → Throw `RECRUITER_INVOICE_NOT_FOUND` exception
3. Verify invoice is currently active
   - If invoice is not active → Throw `CANNOT_DELETE_MY_RECRUITER_INVOICE` exception
4. Update invoice record:
   - Set status to `CANCELLED`
   - Set `cancelledAt` to current date
   - Set `isActive` to `false`
5. Save updated invoice to database
6. Return success response

#### Notes
- Required role: `ROLE_RECRUITER`
- Only active invoices can be cancelled
- Cancellation is immediate and takes effect immediately
- Recruiter will lose access to package benefits after cancellation
- Cancellation date is recorded for audit purposes
- Cannot cancel an invoice that is already inactive or cancelled
- No refund logic is handled by this endpoint
- After cancellation, recruiter reverts to BASIC package

---

### 2. Get My Active Invoice

#### Endpoint
```
GET /api/recruiter-invoice/my-invoice
```

#### Purpose
Retrieve the recruiter's current active invoice/subscription details including package information, start date, end date, and amount paid.

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
GET /api/recruiter-invoice/my-invoice
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "result": {
    "startDate": "2025-11-01",
    "endDate": "2025-12-01",
    "packageName": "PROFESSIONAL",
    "amount": 250000
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (MyRecruiterInvoiceResponse): Active invoice details

**MyRecruiterInvoiceResponse:**
- **startDate** (LocalDate): Package activation/start date in format YYYY-MM-DD
- **endDate** (LocalDate): Package expiration date in format YYYY-MM-DD
- **packageName** (String): Name of the active package (e.g., "BASIC", "PROFESSIONAL", "ENTERPRISE")
- **amount** (Long): Amount paid for the package in VND

#### Error Response

**404 Not Found - Recruiter Invoice Not Found:**
```json
{
  "code": 404,
  "message": "RECRUITER_INVOICE_NOT_FOUND",
  "result": null
}
```

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

1. Get current recruiter information from JWT token via `coachUtil.getCurrentRecruiter()`
2. Query database to find active invoice for the recruiter using recruiter ID
3. If no active invoice found → Throw `RECRUITER_INVOICE_NOT_FOUND` exception
4. Map invoice entity to MyRecruiterInvoiceResponse DTO using mapper
5. Return invoice details with package information

#### Notes
- Required role: `ROLE_RECRUITER`
- Only returns currently active invoice
- If recruiter has no active package (e.g., on BASIC plan or expired), returns 404 error
- Use this endpoint to display subscription information in recruiter dashboard
- Dates are in ISO format (YYYY-MM-DD)
- Amount is in VND (Vietnamese Dong)
- Can be used to check remaining days by calculating difference between current date and endDate

---

### 3. Check Active Package

#### Endpoint
```
GET /api/recruiter-invoice/active-package
```

#### Purpose
Check whether the recruiter has an active package/subscription. This endpoint should be called before attempting to purchase a new package to prevent duplicate active subscriptions.

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
GET /api/recruiter-invoice/active-package
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK) - Has Active Package:**
```json
{
  "code": 200,
  "message": "success",
  "result": true
}
```

**Success Response (200 OK) - No Active Package:**
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
  - `true` = Recruiter has an active package
  - `false` = Recruiter does not have an active package (can purchase new package)

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

1. Get current recruiter information from JWT token via `coachUtil.getCurrentRecruiter()`
2. Query database to check if recruiter has an active invoice (where `isActive = true`)
3. If active invoice exists → Return `true`
4. If no active invoice exists → Return `false`

#### Notes
- Required role: `ROLE_RECRUITER`
- **Important:** This endpoint should be called **before** calling `POST /api/recruiter-payment` to prevent payment errors
- Returns boolean value, not exception on no active package (unlike `/my-invoice` endpoint)
- Used for validation before allowing package purchase
- Lightweight check - does not return full invoice details
- Can be used to conditionally show "Upgrade Package" or "Current Package" UI

---

## Common Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | CANNOT_DELETE_MY_RECRUITER_INVOICE | Cannot cancel an invoice that is not active |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | User does not have access permission (not a RECRUITER) |
| 404 | RECRUITER_INVOICE_NOT_FOUND | No active invoice found for the recruiter |
| 500 | Internal Server Error | Server error |

## Security

- All endpoints require authentication and role `RECRUITER`
- Uses JWT Bearer token for authentication
- Security is implemented through `@PreAuthorize("hasRole('RECRUITER')")` annotation
- Recruiters can only access their own invoices (enforced by getCurrentRecruiter)

## Database Dependencies

- **RecruiterInvoice entity:** Stores invoice/order information
  - Fields: startDate, endDate, status, isActive, cancelledAt, amount
- **RecruiterPackage entity:** Package information linked to invoice
- **Recruiter entity:** Recruiter profile
- **Account entity:** User account for authentication

## Invoice Status Values

**StatusInvoice enum values:**
- **PAID:** Invoice has been paid and is active
- **CANCELLED:** Invoice has been cancelled by user
- **PENDING:** Invoice is awaiting payment (not used in current flow)
- **EXPIRED:** Invoice has expired (handled by scheduled tasks)

## Workflow Examples

### Example 1: Check and Purchase New Package
```bash
# Step 1: Check if recruiter has active package
GET /api/recruiter-invoice/active-package
Response: {"result": false}

# Step 2: If false, proceed to create payment
POST /api/recruiter-payment?packageName=PROFESSIONAL

# Step 3: After successful payment, verify active invoice
GET /api/recruiter-invoice/my-invoice
Response: {"result": {"packageName": "PROFESSIONAL", ...}}
```

### Example 2: View and Cancel Current Subscription
```bash
# Step 1: View current subscription
GET /api/recruiter-invoice/my-invoice
Response: {"result": {"packageName": "PROFESSIONAL", "endDate": "2025-12-31", ...}}

# Step 2: Cancel subscription
DELETE /api/recruiter-invoice
Response: {"message": "success"}

# Step 3: Verify cancellation
GET /api/recruiter-invoice/active-package
Response: {"result": false}
```

### Example 3: Prevent Duplicate Purchase
```bash
# Step 1: Recruiter tries to purchase while having active package
GET /api/recruiter-invoice/active-package
Response: {"result": true}

# Step 2: Frontend prevents payment button or shows message
# "You already have an active package. Please cancel it before purchasing a new one."
```

## Business Rules

1. **One Active Package Rule:** Recruiter can only have ONE active package at a time
2. **Cancellation:** Cancelling a package takes effect immediately, no grace period
3. **No Refunds:** Cancellation does not trigger refund (handled separately if needed)
4. **BASIC Package:** BASIC package users have no invoice record (invoice is null)
5. **Package Upgrade:** To upgrade, recruiter must cancel current package first, then purchase new one
6. **Expiration Handling:** Expired packages are handled by scheduled background tasks (not covered in this controller)
7. **Revert to BASIC:** After cancellation, recruiter automatically reverts to BASIC (free) package

## Related Services

- `RecruiterInvoiceImp`: Business logic service for invoice management
- `RecruiterInvoiceMapper`: Mapper to convert entities to DTOs
- `CoachUtil`: Utility to get current user information
- `RecruiterInvoiceRepo`: Repository for RecruiterInvoice entity

## Frontend Integration Notes

### Display Package Status
```javascript
// Check if recruiter has active package
const hasPackage = await checkActivePackage();
if (hasPackage) {
  // Show "Manage Subscription" button
  // Fetch and display package details
  const invoice = await getMyInvoice();
  displayPackageInfo(invoice);
} else {
  // Show "Upgrade to Professional" button
  displayPackageOptions();
}
```

### Handle Package Cancellation
```javascript
async function cancelSubscription() {
  const confirmed = confirm("Are you sure you want to cancel your subscription? You will lose access to premium features.");
  if (confirmed) {
    await cancelMyInvoice();
    showMessage("Subscription cancelled successfully. You have been downgraded to BASIC package.");
    // Refresh page or update UI
    window.location.reload();
  }
}
```

### Prevent Duplicate Purchase
```javascript
async function purchasePackage(packageName) {
  // Check first
  const hasActive = await checkActivePackage();
  if (hasActive) {
    showError("You already have an active package. Please cancel it first.");
    return;
  }
  
  // Proceed with payment
  const paymentUrl = await createPaymentUrl(packageName);
  window.location.href = paymentUrl;
}
```

### Display Subscription Details in Dashboard
```javascript
async function loadRecruiterDashboard() {
  try {
    const invoice = await fetch('/api/recruiter-invoice/my-invoice', {
      headers: { 'Authorization': `Bearer ${token}` }
    }).then(r => r.json());
    
    if (invoice.code === 200) {
      // Calculate days remaining
      const endDate = new Date(invoice.result.endDate);
      const today = new Date();
      const daysRemaining = Math.ceil((endDate - today) / (1000 * 60 * 60 * 24));
      
      document.getElementById('subscription-info').innerHTML = `
        <div class="subscription-card">
          <h3>Current Subscription</h3>
          <p class="package-name">${invoice.result.packageName}</p>
          <p class="package-price">${invoice.result.amount.toLocaleString()} VND</p>
          <p class="date-info">
            Valid from ${invoice.result.startDate} to ${invoice.result.endDate}
          </p>
          <p class="days-remaining ${daysRemaining < 7 ? 'warning' : ''}">
            ${daysRemaining} days remaining
          </p>
          <button onclick="cancelSubscription()" class="btn-cancel">
            Cancel Subscription
          </button>
        </div>
      `;
      
      // Show renewal reminder if less than 7 days
      if (daysRemaining < 7) {
        showRenewalReminder(invoice.result.packageName, daysRemaining);
      }
    }
  } catch (error) {
    // No active subscription
    document.getElementById('subscription-info').innerHTML = `
      <div class="no-subscription">
        <p>You are currently on the BASIC (free) package</p>
        <button onclick="showPackageOptions()" class="btn-upgrade">
          Upgrade to Premium
        </button>
      </div>
    `;
  }
}

function showRenewalReminder(packageName, daysRemaining) {
  const banner = document.getElementById('renewal-banner');
  banner.innerHTML = `
    <div class="alert alert-warning">
      <strong>⚠️ Subscription Expiring Soon!</strong>
      <p>Your ${packageName} package expires in ${daysRemaining} days.</p>
      <a href="/renew" class="btn-renew">Renew Now</a>
    </div>
  `;
  banner.style.display = 'block';
}
```

### Complete Subscription Management Component
```javascript
class SubscriptionManager {
  constructor() {
    this.apiBase = '/api/recruiter-invoice';
    this.token = localStorage.getItem('authToken');
  }
  
  async getHeaders() {
    return {
      'Authorization': `Bearer ${this.token}`,
      'Content-Type': 'application/json'
    };
  }
  
  async checkActive() {
    const response = await fetch(`${this.apiBase}/active-package`, {
      headers: await this.getHeaders()
    });
    const data = await response.json();
    return data.result;
  }
  
  async getInvoice() {
    const response = await fetch(`${this.apiBase}/my-invoice`, {
      headers: await this.getHeaders()
    });
    if (!response.ok) throw new Error('No active invoice');
    return await response.json();
  }
  
  async cancel() {
    const response = await fetch(this.apiBase, {
      method: 'DELETE',
      headers: await this.getHeaders()
    });
    if (!response.ok) throw new Error('Failed to cancel');
    return await response.json();
  }
  
  async renderUI(containerId) {
    const container = document.getElementById(containerId);
    
    try {
      const hasActive = await this.checkActive();
      
      if (hasActive) {
        const invoiceData = await this.getInvoice();
        const invoice = invoiceData.result;
        
        container.innerHTML = this.renderActiveSubscription(invoice);
      } else {
        container.innerHTML = this.renderNoSubscription();
      }
    } catch (error) {
      console.error('Error loading subscription:', error);
      container.innerHTML = this.renderError();
    }
  }
  
  renderActiveSubscription(invoice) {
    const endDate = new Date(invoice.endDate);
    const today = new Date();
    const daysLeft = Math.ceil((endDate - today) / (1000 * 60 * 60 * 24));
    
    return `
      <div class="subscription-active">
        <div class="badge badge-success">Active</div>
        <h2>${invoice.packageName}</h2>
        <div class="price">${invoice.amount.toLocaleString()} VND</div>
        <div class="dates">
          <p>Started: ${invoice.startDate}</p>
          <p>Expires: ${invoice.endDate}</p>
        </div>
        <div class="days-left ${daysLeft < 7 ? 'warning' : ''}">
          <strong>${daysLeft}</strong> days remaining
        </div>
        <div class="actions">
          <button onclick="subscriptionManager.handleCancel()" 
                  class="btn btn-danger">
            Cancel Subscription
          </button>
        </div>
      </div>
    `;
  }
  
  renderNoSubscription() {
    return `
      <div class="subscription-none">
        <h2>BASIC Package (Free)</h2>
        <p>You are currently on the free tier with limited features.</p>
        <button onclick="location.href='/packages'" class="btn btn-primary">
          View Premium Packages
        </button>
      </div>
    `;
  }
  
  renderError() {
    return `
      <div class="alert alert-error">
        <p>Unable to load subscription information. Please try again.</p>
        <button onclick="location.reload()" class="btn">Retry</button>
      </div>
    `;
  }
  
  async handleCancel() {
    if (!confirm('Are you sure you want to cancel your subscription? This action cannot be undone.')) {
      return;
    }
    
    try {
      await this.cancel();
      alert('Subscription cancelled successfully. You have been downgraded to BASIC package.');
      location.reload();
    } catch (error) {
      alert('Failed to cancel subscription. Please try again.');
    }
  }
}

// Initialize
const subscriptionManager = new SubscriptionManager();
document.addEventListener('DOMContentLoaded', () => {
  subscriptionManager.renderUI('subscription-container');
});
```

## Important Notes

- **Invoice vs Package:** Invoice represents a purchase/subscription record, Package represents the product being purchased
- **Active Flag:** The `isActive` flag is the primary indicator of subscription status
- **Date Tracking:** System tracks startDate, endDate, and cancelledAt for audit trail
- **Automatic Expiration:** Invoices are automatically marked as expired by background scheduled tasks when endDate passes
- **No Partial Refunds:** Current implementation does not handle prorated refunds on cancellation
- **Immediate Effect:** Cancellation takes effect immediately, recruiter loses access to premium features right away
- **Revert to BASIC:** After cancellation or expiration, recruiter automatically uses BASIC (free) package
- **Monthly Quotas Reset:** After downgrade to BASIC, monthly quotas (like job postings) are adjusted to BASIC limits

## Comparison: Recruiter vs Candidate Invoice

| Aspect | Recruiter Invoice | Candidate Invoice |
|--------|------------------|-------------------|
| Base URL | `/api/recruiter-invoice` | `/api/candidate-invoice` |
| Default Package | BASIC | FREE |
| Package Tiers | BASIC/PROFESSIONAL/ENTERPRISE | FREE/PLUS/PREMIUM |
| Typical Price Range | 100,000 - 500,000 VND | 50,000 - 300,000 VND |
| Main Features | Job Postings, AI Matching | Job Applications, CV Builder |
| Error Code (Not Found) | RECRUITER_INVOICE_NOT_FOUND | CANDIDATE_INVOICE_NOT_FOUND |
| Error Code (Cannot Delete) | CANNOT_DELETE_MY_RECRUITER_INVOICE | CANNOT_DELETE_ORDER |

## API Response Time Expectations

- **GET /active-package**: < 100ms (lightweight boolean check)
- **GET /my-invoice**: < 200ms (single query with mapping)
- **DELETE /**: < 300ms (update operation with transaction)

## Rate Limiting Recommendations

While not implemented in the current controller, consider these limits for production:
- **GET requests**: 100 requests per minute per user
- **DELETE requests**: 10 requests per minute per user (prevent abuse)

## Monitoring and Logging

The controller uses `@Slf4j` for logging. Key events logged:
- Invoice cancellation attempts
- Active package checks
- Invoice retrieval operations
- Error scenarios (not found, unauthorized access)

Monitor these metrics:
- Number of cancellations per day
- Average subscription duration
- Most common cancellation timing (days before expiry)
- Reactivation rate (users who cancel then repurchase)

