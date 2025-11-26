# Candidate Invoice Controller API Documentation

## Overview
Controller manages invoice/order functionalities for candidates, including viewing active invoices, checking package status, and canceling subscriptions.

**Base URL:** `/api/candidate-invoice`

**Tag:** Candidate - Invoice

---

## API Endpoints

### 1. Cancel My Invoice

#### Endpoint
```
DELETE /api/candidate-invoice
```

#### Purpose
Cancel the candidate's current active package/subscription. This sets the invoice status to CANCELLED, marks it as inactive, and records the cancellation date.

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
DELETE /api/candidate-invoice
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

**404 Not Found - Candidate Invoice Not Found:**
```json
{
  "code": 404,
  "message": "CANDIDATE_INVOICE_NOT_FOUND",
  "result": null
}
```

**400 Bad Request - Cannot Delete Order:**
```json
{
  "code": 400,
  "message": "CANNOT_DELETE_ORDER",
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

**403 Forbidden - User is not a Candidate:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current candidate information from JWT token via authentication service
2. Query database to find active invoice for the candidate
   - If no active invoice found → Throw `CANDIDATE_INVOICE_NOT_FOUND` exception
3. Verify invoice is currently active
   - If invoice is not active → Throw `CANNOT_DELETE_ORDER` exception
4. Update invoice record:
   - Set status to `CANCELLED`
   - Set `cancelledAt` to current date
   - Set `isActive` to `false`
5. Save updated invoice to database
6. Return success response

#### Notes
- Required role: `ROLE_CANDIDATE`
- Only active invoices can be cancelled
- Cancellation is immediate and takes effect immediately
- Candidate will lose access to package benefits after cancellation
- Cancellation date is recorded for audit purposes
- Cannot cancel an invoice that is already inactive or cancelled
- No refund logic is handled by this endpoint

---

### 2. Get My Active Invoice

#### Endpoint
```
GET /api/candidate-invoice/my-invoice
```

#### Purpose
Retrieve the candidate's current active invoice/subscription details including package information, start date, end date, and amount paid.

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
GET /api/candidate-invoice/my-invoice
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
    "endDate": "2025-11-30",
    "packageName": "PREMIUM",
    "amount": 150000
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (MyCandidateInvoiceResponse): Active invoice details

**MyCandidateInvoiceResponse:**
- **startDate** (LocalDate): Package activation/start date in format YYYY-MM-DD
- **endDate** (LocalDate): Package expiration date in format YYYY-MM-DD
- **packageName** (String): Name of the active package (e.g., "BASIC", "PREMIUM")
- **amount** (Long): Amount paid for the package in VND

#### Error Response

**404 Not Found - Candidate Invoice Not Found:**
```json
{
  "code": 404,
  "message": "CANDIDATE_INVOICE_NOT_FOUND",
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

**403 Forbidden - User is not a Candidate:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Get current candidate information from JWT token via `coachUtil.getCurrentCandidate()`
2. Query database to find active invoice for the candidate using candidate ID
3. If no active invoice found → Throw `CANDIDATE_INVOICE_NOT_FOUND` exception
4. Map invoice entity to MyCandidateInvoiceResponse DTO using mapper
5. Return invoice details with package information

#### Notes
- Required role: `ROLE_CANDIDATE`
- Only returns currently active invoice
- If candidate has no active package (e.g., on FREE plan or expired), returns 404 error
- Use this endpoint to display subscription information in user profile
- Dates are in ISO format (YYYY-MM-DD)
- Amount is in VND (Vietnamese Dong)
- Can be used to check remaining days by calculating difference between current date and endDate

---

### 3. Check Active Package

#### Endpoint
```
GET /api/candidate-invoice/active-package
```

#### Purpose
Check whether the candidate has an active package/subscription. This endpoint should be called before attempting to purchase a new package to prevent duplicate active subscriptions.

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
GET /api/candidate-invoice/active-package
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
  - `true` = Candidate has an active package
  - `false` = Candidate does not have an active package (can purchase new package)

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

1. Get current candidate information from JWT token via authentication service
2. Query database to check if candidate has an active invoice (where `isActive = true`)
3. If active invoice exists → Return `true`
4. If no active invoice exists → Return `false`

#### Notes
- Required role: `ROLE_CANDIDATE`
- **Important:** This endpoint should be called **before** calling `POST /api/candidate-payment` to prevent payment errors
- Returns boolean value, not exception on no active package (unlike `/my-invoice` endpoint)
- Used for validation before allowing package purchase
- Lightweight check - does not return full invoice details
- Can be used to conditionally show "Upgrade Package" or "Current Package" UI

---

## Common Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | CANNOT_DELETE_ORDER | Cannot cancel an invoice that is not active |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | User does not have access permission (not a CANDIDATE) |
| 404 | CANDIDATE_INVOICE_NOT_FOUND | No active invoice found for the candidate |
| 500 | Internal Server Error | Server error |

## Security

- All endpoints require authentication and role `CANDIDATE`
- Uses JWT Bearer token for authentication
- Security is implemented through `@PreAuthorize("hasRole('CANDIDATE')")` annotation
- Candidates can only access their own invoices (enforced by getCurrentCandidate)

## Database Dependencies

- **CandidateInvoice entity:** Stores invoice/order information
  - Fields: startDate, endDate, status, isActive, cancelledAt, amount
- **CandidatePackage entity:** Package information linked to invoice
- **Candidate entity:** Candidate profile
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
# Step 1: Check if user has active package
GET /api/candidate-invoice/active-package
Response: {"result": false}

# Step 2: If false, proceed to create payment
POST /api/candidate-payment?packageName=PREMIUM

# Step 3: After successful payment, verify active invoice
GET /api/candidate-invoice/my-invoice
Response: {"result": {"packageName": "PREMIUM", ...}}
```

### Example 2: View and Cancel Current Subscription
```bash
# Step 1: View current subscription
GET /api/candidate-invoice/my-invoice
Response: {"result": {"packageName": "PREMIUM", "endDate": "2025-12-31", ...}}

# Step 2: Cancel subscription
DELETE /api/candidate-invoice
Response: {"message": "success"}

# Step 3: Verify cancellation
GET /api/candidate-invoice/active-package
Response: {"result": false}
```

### Example 3: Prevent Duplicate Purchase
```bash
# Step 1: User tries to purchase while having active package
GET /api/candidate-invoice/active-package
Response: {"result": true}

# Step 2: Frontend prevents payment button or shows message
# "You already have an active package. Please cancel it before purchasing a new one."
```

## Business Rules

1. **One Active Package Rule:** Candidate can only have ONE active package at a time
2. **Cancellation:** Cancelling a package takes effect immediately, no grace period
3. **No Refunds:** Cancellation does not trigger refund (handled separately if needed)
4. **FREE Package:** FREE package users have no invoice record (invoice is null)
5. **Package Upgrade:** To upgrade, user must cancel current package first, then purchase new one
6. **Expiration Handling:** Expired packages are handled by scheduled background tasks (not covered in this controller)

## Related Services

- `CandidateInvoiceImp`: Business logic service for invoice management
- `CandidateInvoiceMapper`: Mapper to convert entities to DTOs
- `CoachUtil`: Utility to get current user information
- `AuthenticationImp`: Authentication service for user lookup
- `CandidateInvoiceRepo`: Repository for CandidateInvoice entity

## Frontend Integration Notes

### Display Package Status
```javascript
// Check if user has active package
const hasPackage = await checkActivePackage();
if (hasPackage) {
  // Show "Manage Subscription" button
  // Fetch and display package details
  const invoice = await getMyInvoice();
  displayPackageInfo(invoice);
} else {
  // Show "Upgrade to Premium" button
  displayPackageOptions();
}
```

### Handle Package Cancellation
```javascript
async function cancelSubscription() {
  const confirmed = confirm("Are you sure you want to cancel your subscription?");
  if (confirmed) {
    await cancelMyInvoice();
    showMessage("Subscription cancelled successfully");
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

## Important Notes

- **Invoice vs Package:** Invoice represents a purchase/subscription record, Package represents the product being purchased
- **Active Flag:** The `isActive` flag is the primary indicator of subscription status
- **Date Tracking:** System tracks startDate, endDate, and cancelledAt for audit trail
- **Automatic Expiration:** Invoices are automatically marked as expired by background scheduled tasks when endDate passes
- **No Partial Refunds:** Current implementation does not handle prorated refunds on cancellation
- **Immediate Effect:** Cancellation takes effect immediately, user loses access to premium features right away

