# Admin Invoice Controller API Documentation

## Overview
Controller manages invoice/order management functionalities for administrators. Allows admins to view and filter all invoices (both recruiter and candidate invoices) with pagination support for monitoring subscription activities.

**Base URL:** `/admin/invoices`

**Tag:** Admin - Invoice

---

## API Endpoints

### 1. Get All Recruiter Invoices

#### Endpoint
```
GET /admin/invoices/recruiters
```

#### Purpose
Retrieve a paginated list of all recruiter invoices with optional filtering by status and active state. This endpoint allows administrators to monitor all recruiter subscriptions, payments, and cancellations.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:**
- **status** (String, optional): Filter by invoice status (PAID, CANCELLED, PENDING, EXPIRED)
- **isActive** (Boolean, optional): Filter by active state
  - `true` = Only active invoices
  - `false` = Only inactive invoices
  - `null` (not provided) = All invoices
- **page** (int, optional, default: 0): Page number (starts from 0)
- **size** (int, optional, default: 5): Number of items per page

**Request Body:** None

**Example Requests:**
```bash
# Get all recruiter invoices (first page)
GET /admin/invoices/recruiters?page=0&size=10

# Get only active recruiter invoices
GET /admin/invoices/recruiters?isActive=true&page=0&size=10

# Get only cancelled invoices
GET /admin/invoices/recruiters?status=CANCELLED&page=0&size=10

# Get active PAID invoices
GET /admin/invoices/recruiters?status=PAID&isActive=true&page=0&size=10

# Get expired invoices
GET /admin/invoices/recruiters?status=EXPIRED&isActive=false&page=0&size=20
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "Get recruiter invoices successfully",
  "result": {
    "content": [
      {
        "id": 1,
        "fullname": "John Doe",
        "packageName": "PROFESSIONAL",
        "amount": 250000,
        "status": "PAID",
        "startDate": "2025-11-01",
        "endDate": "2025-12-01",
        "cancelledAt": null,
        "isActive": true
      },
      {
        "id": 2,
        "fullname": "Jane Smith",
        "packageName": "ENTERPRISE",
        "amount": 500000,
        "status": "PAID",
        "startDate": "2025-10-15",
        "endDate": "2025-11-15",
        "cancelledAt": null,
        "isActive": true
      },
      {
        "id": 3,
        "fullname": "Bob Wilson",
        "packageName": "PROFESSIONAL",
        "amount": 250000,
        "status": "CANCELLED",
        "startDate": "2025-09-01",
        "endDate": "2025-10-01",
        "cancelledAt": "2025-09-15",
        "isActive": false
      }
    ],
    "number": 0,
    "size": 10,
    "totalElements": 150,
    "totalPages": 15,
    "first": true,
    "last": false
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (PageInvoiceListResponse): Paginated invoice data

**PageInvoiceListResponse:**
- **content** (List<InvoiceListResponse>): List of recruiter invoices
- **number** (int): Current page number (starts from 0)
- **size** (int): Number of items per page
- **totalElements** (long): Total number of invoices across all pages
- **totalPages** (int): Total number of pages
- **first** (boolean): Whether this is the first page
- **last** (boolean): Whether this is the last page

**InvoiceListResponse:**
- **id** (int): Invoice ID
- **fullname** (String): Recruiter's full name
- **packageName** (String): Package name (BASIC, PROFESSIONAL, ENTERPRISE)
- **amount** (Long): Payment amount in VND
- **status** (String): Invoice status (PAID, CANCELLED, PENDING, EXPIRED)
- **startDate** (LocalDate): Subscription start date in format YYYY-MM-DD
- **endDate** (LocalDate): Subscription end date in format YYYY-MM-DD
- **cancelledAt** (LocalDate): Cancellation date (null if not cancelled)
- **isActive** (boolean): Whether the invoice is currently active

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not an Admin:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Verify user has ADMIN role via JWT token
2. Parse query parameters (status, isActive, page, size)
3. Create Pageable object for pagination
4. Query database with filters:
   - If status provided: Filter by invoice status
   - If isActive provided: Filter by active state
   - If both provided: Apply both filters
   - If neither provided: Return all invoices
5. Convert entities to DTO responses using mapper
6. Return paginated results with metadata

#### Notes
- Required role: `ROLE_ADMIN`
- Default page size is 5 items
- Status values are case-sensitive: PAID, CANCELLED, PENDING, EXPIRED
- Includes recruiter full name for easy identification
- Shows cancellation date when applicable
- Use for admin dashboard and subscription monitoring

---

### 2. Get All Candidate Invoices

#### Endpoint
```
GET /admin/invoices/candidates
```

#### Purpose
Retrieve a paginated list of all candidate invoices with optional filtering by status and active state. This endpoint allows administrators to monitor all candidate subscriptions, payments, and cancellations.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:**
- **status** (String, optional): Filter by invoice status (PAID, CANCELLED, PENDING, EXPIRED)
- **isActive** (Boolean, optional): Filter by active state
  - `true` = Only active invoices
  - `false` = Only inactive invoices
  - `null` (not provided) = All invoices
- **page** (int, optional, default: 0): Page number (starts from 0)
- **size** (int, optional, default: 5): Number of items per page

**Request Body:** None

**Example Requests:**
```bash
# Get all candidate invoices (first page)
GET /admin/invoices/candidates?page=0&size=10

# Get only active candidate invoices
GET /admin/invoices/candidates?isActive=true&page=0&size=10

# Get only cancelled invoices
GET /admin/invoices/candidates?status=CANCELLED&page=0&size=10

# Get active PAID invoices
GET /admin/invoices/candidates?status=PAID&isActive=true&page=0&size=10

# Get all invoices with larger page size
GET /admin/invoices/candidates?page=0&size=50
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "Get candidate invoices successfully",
  "result": {
    "content": [
      {
        "id": 101,
        "fullname": "Alice Johnson",
        "packageName": "PREMIUM",
        "amount": 150000,
        "status": "PAID",
        "startDate": "2025-11-20",
        "endDate": "2026-02-20",
        "cancelledAt": null,
        "isActive": true
      },
      {
        "id": 102,
        "fullname": "Charlie Brown",
        "packageName": "PLUS",
        "amount": 100000,
        "status": "PAID",
        "startDate": "2025-11-01",
        "endDate": "2025-12-01",
        "cancelledAt": null,
        "isActive": true
      },
      {
        "id": 103,
        "fullname": "Diana Prince",
        "packageName": "PLUS",
        "amount": 100000,
        "status": "CANCELLED",
        "startDate": "2025-10-01",
        "endDate": "2025-11-01",
        "cancelledAt": "2025-10-15",
        "isActive": false
      }
    ],
    "number": 0,
    "size": 10,
    "totalElements": 320,
    "totalPages": 32,
    "first": true,
    "last": false
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (PageInvoiceListResponse): Paginated invoice data

**PageInvoiceListResponse:**
- **content** (List<InvoiceListResponse>): List of candidate invoices
- **number** (int): Current page number (starts from 0)
- **size** (int): Number of items per page
- **totalElements** (long): Total number of invoices across all pages
- **totalPages** (int): Total number of pages
- **first** (boolean): Whether this is the first page
- **last** (boolean): Whether this is the last page

**InvoiceListResponse:**
- **id** (int): Invoice ID
- **fullname** (String): Candidate's full name
- **packageName** (String): Package name (FREE, PLUS, PREMIUM)
- **amount** (Long): Payment amount in VND
- **status** (String): Invoice status (PAID, CANCELLED, PENDING, EXPIRED)
- **startDate** (LocalDate): Subscription start date in format YYYY-MM-DD
- **endDate** (LocalDate): Subscription end date in format YYYY-MM-DD
- **cancelledAt** (LocalDate): Cancellation date (null if not cancelled)
- **isActive** (boolean): Whether the invoice is currently active

#### Error Response

**401 Unauthorized - Missing or Invalid Token:**
```json
{
  "code": 401,
  "message": "Unauthorized",
  "result": null
}
```

**403 Forbidden - User is not an Admin:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Verify user has ADMIN role via JWT token
2. Parse query parameters (status, isActive, page, size)
3. Create Pageable object for pagination
4. Query database with filters:
   - If status provided: Filter by invoice status
   - If isActive provided: Filter by active state
   - If both provided: Apply both filters
   - If neither provided: Return all invoices
5. Convert entities to DTO responses using mapper
6. Return paginated results with metadata

#### Notes
- Required role: `ROLE_ADMIN`
- Default page size is 5 items
- Status values are case-sensitive: PAID, CANCELLED, PENDING, EXPIRED
- Includes candidate full name for easy identification
- Shows cancellation date when applicable
- Use for admin dashboard and subscription monitoring

---

## Filter Combinations

### Common Filter Scenarios

#### 1. View All Active Subscriptions
```bash
GET /admin/invoices/recruiters?isActive=true
GET /admin/invoices/candidates?isActive=true
```
**Use case:** Monitor currently active paying users

#### 2. View All Cancelled Subscriptions
```bash
GET /admin/invoices/recruiters?status=CANCELLED
GET /admin/invoices/candidates?status=CANCELLED
```
**Use case:** Analyze churn and cancellation patterns

#### 3. View Expired Subscriptions
```bash
GET /admin/invoices/recruiters?status=EXPIRED&isActive=false
GET /admin/invoices/candidates?status=EXPIRED&isActive=false
```
**Use case:** Identify users who need to renew

#### 4. View Active Paid Invoices Only
```bash
GET /admin/invoices/recruiters?status=PAID&isActive=true
GET /admin/invoices/candidates?status=PAID&isActive=true
```
**Use case:** Current revenue-generating subscriptions

#### 5. View All Invoices (No Filter)
```bash
GET /admin/invoices/recruiters
GET /admin/invoices/candidates
```
**Use case:** Complete invoice history

## Invoice Status Values

**StatusInvoice enum constants:**
- **PAID:** Invoice has been successfully paid and subscription is/was active
- **CANCELLED:** User cancelled the subscription before expiration
- **PENDING:** Payment is pending (not commonly used in current flow)
- **EXPIRED:** Subscription period has ended naturally

## Status vs Active State Matrix

| Status | isActive | Meaning |
|--------|----------|---------|
| PAID | true | Currently active paid subscription |
| PAID | false | Past subscription that has expired |
| CANCELLED | false | User cancelled their subscription |
| EXPIRED | false | Subscription expired naturally |
| PENDING | false | Payment not yet completed |

## Common Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | User does not have ADMIN role |
| 500 | Internal Server Error | Server error |

## Security

- Both endpoints require authentication and role `ADMIN`
- Uses JWT Bearer token for authentication
- Security is implemented through `@PreAuthorize("hasRole('ADMIN')")` annotation
- Only administrators can access these endpoints
- No user-specific filtering - admins see all invoices

## Database Dependencies

- **RecruiterInvoice entity:** Stores recruiter subscription invoices
- **CandidateInvoice entity:** Stores candidate subscription invoices
- **RecruiterPackage entity:** Package information for recruiters
- **CandidatePackage entity:** Package information for candidates
- **Recruiter entity:** Recruiter profile with full name
- **Candidate entity:** Candidate profile with full name

## Frontend Integration Examples

### Example 1: Admin Dashboard - Invoice Statistics
```javascript
async function loadInvoiceStatistics() {
  const [recruiterActive, candidateActive, recruiterCancelled, candidateCancelled] = 
    await Promise.all([
      fetch('/admin/invoices/recruiters?isActive=true&size=1000', {
        headers: { 'Authorization': `Bearer ${adminToken}` }
      }).then(r => r.json()),
      fetch('/admin/invoices/candidates?isActive=true&size=1000', {
        headers: { 'Authorization': `Bearer ${adminToken}` }
      }).then(r => r.json()),
      fetch('/admin/invoices/recruiters?status=CANCELLED&size=1000', {
        headers: { 'Authorization': `Bearer ${adminToken}` }
      }).then(r => r.json()),
      fetch('/admin/invoices/candidates?status=CANCELLED&size=1000', {
        headers: { 'Authorization': `Bearer ${adminToken}` }
      }).then(r => r.json())
    ]);
  
  // Display statistics
  document.getElementById('stats').innerHTML = `
    <div class="stats-grid">
      <div class="stat-card">
        <h3>Active Recruiter Subscriptions</h3>
        <p class="stat-value">${recruiterActive.result.totalElements}</p>
      </div>
      <div class="stat-card">
        <h3>Active Candidate Subscriptions</h3>
        <p class="stat-value">${candidateActive.result.totalElements}</p>
      </div>
      <div class="stat-card">
        <h3>Cancelled Recruiters</h3>
        <p class="stat-value">${recruiterCancelled.result.totalElements}</p>
      </div>
      <div class="stat-card">
        <h3>Cancelled Candidates</h3>
        <p class="stat-value">${candidateCancelled.result.totalElements}</p>
      </div>
    </div>
  `;
}
```

### Example 2: Invoice List with Filters
```javascript
class AdminInvoiceManager {
  constructor(userType) {
    this.userType = userType; // 'recruiters' or 'candidates'
    this.baseUrl = `/admin/invoices/${userType}`;
    this.currentPage = 0;
    this.pageSize = 10;
    this.filters = {};
  }
  
  async loadInvoices() {
    const params = new URLSearchParams({
      page: this.currentPage,
      size: this.pageSize
    });
    
    if (this.filters.status) {
      params.append('status', this.filters.status);
    }
    
    if (this.filters.isActive !== undefined) {
      params.append('isActive', this.filters.isActive);
    }
    
    const response = await fetch(`${this.baseUrl}?${params}`, {
      headers: { 'Authorization': `Bearer ${adminToken}` }
    });
    
    const data = await response.json();
    this.renderInvoices(data.result);
    this.renderPagination(data.result);
  }
  
  renderInvoices(result) {
    const tbody = document.getElementById('invoice-table-body');
    
    tbody.innerHTML = result.content.map(invoice => `
      <tr class="${invoice.isActive ? 'active' : 'inactive'}">
        <td>${invoice.id}</td>
        <td>${invoice.fullname}</td>
        <td><span class="badge badge-package">${invoice.packageName}</span></td>
        <td>${invoice.amount.toLocaleString()} VND</td>
        <td><span class="badge badge-${invoice.status.toLowerCase()}">${invoice.status}</span></td>
        <td>${invoice.startDate}</td>
        <td>${invoice.endDate}</td>
        <td>${invoice.cancelledAt || '-'}</td>
        <td>
          <span class="badge ${invoice.isActive ? 'badge-success' : 'badge-secondary'}">
            ${invoice.isActive ? 'Active' : 'Inactive'}
          </span>
        </td>
      </tr>
    `).join('');
  }
  
  renderPagination(result) {
    const pagination = document.getElementById('pagination');
    const pages = [];
    
    for (let i = 0; i < result.totalPages; i++) {
      pages.push(`
        <button 
          class="page-btn ${i === result.number ? 'active' : ''}"
          onclick="invoiceManager.goToPage(${i})"
        >
          ${i + 1}
        </button>
      `);
    }
    
    pagination.innerHTML = `
      <button 
        onclick="invoiceManager.goToPage(${result.number - 1})"
        ${result.first ? 'disabled' : ''}
      >
        Previous
      </button>
      ${pages.join('')}
      <button 
        onclick="invoiceManager.goToPage(${result.number + 1})"
        ${result.last ? 'disabled' : ''}
      >
        Next
      </button>
    `;
  }
  
  goToPage(page) {
    this.currentPage = page;
    this.loadInvoices();
  }
  
  applyFilters(status, isActive) {
    this.filters = { status, isActive };
    this.currentPage = 0; // Reset to first page
    this.loadInvoices();
  }
  
  clearFilters() {
    this.filters = {};
    this.currentPage = 0;
    this.loadInvoices();
  }
}

// Initialize
const recruiterInvoiceManager = new AdminInvoiceManager('recruiters');
const candidateInvoiceManager = new AdminInvoiceManager('candidates');
```

### Example 3: Filter Controls
```html
<div class="filter-section">
  <h3>Filters</h3>
  
  <div class="filter-group">
    <label>Status:</label>
    <select id="status-filter">
      <option value="">All</option>
      <option value="PAID">Paid</option>
      <option value="CANCELLED">Cancelled</option>
      <option value="EXPIRED">Expired</option>
      <option value="PENDING">Pending</option>
    </select>
  </div>
  
  <div class="filter-group">
    <label>Active State:</label>
    <select id="active-filter">
      <option value="">All</option>
      <option value="true">Active Only</option>
      <option value="false">Inactive Only</option>
    </select>
  </div>
  
  <button onclick="applyFilters()">Apply Filters</button>
  <button onclick="clearFilters()">Clear Filters</button>
</div>

<script>
function applyFilters() {
  const status = document.getElementById('status-filter').value;
  const isActive = document.getElementById('active-filter').value;
  
  // Convert to proper types
  const statusParam = status || undefined;
  const isActiveParam = isActive ? isActive === 'true' : undefined;
  
  invoiceManager.applyFilters(statusParam, isActiveParam);
}

function clearFilters() {
  document.getElementById('status-filter').value = '';
  document.getElementById('active-filter').value = '';
  invoiceManager.clearFilters();
}
</script>
```

### Example 4: Revenue Analytics
```javascript
async function calculateRevenue() {
  // Get all active paid invoices
  const [recruiterPaid, candidatePaid] = await Promise.all([
    fetch('/admin/invoices/recruiters?status=PAID&isActive=true&size=10000', {
      headers: { 'Authorization': `Bearer ${adminToken}` }
    }).then(r => r.json()),
    fetch('/admin/invoices/candidates?status=PAID&isActive=true&size=10000', {
      headers: { 'Authorization': `Bearer ${adminToken}` }
    }).then(r => r.json())
  ]);
  
  // Calculate monthly recurring revenue (MRR)
  const recruiterMRR = recruiterPaid.result.content.reduce((sum, inv) => {
    const duration = (new Date(inv.endDate) - new Date(inv.startDate)) / (1000 * 60 * 60 * 24);
    const monthlyAmount = (inv.amount / duration) * 30;
    return sum + monthlyAmount;
  }, 0);
  
  const candidateMRR = candidatePaid.result.content.reduce((sum, inv) => {
    const duration = (new Date(inv.endDate) - new Date(inv.startDate)) / (1000 * 60 * 60 * 24);
    const monthlyAmount = (inv.amount / duration) * 30;
    return sum + monthlyAmount;
  }, 0);
  
  const totalMRR = recruiterMRR + candidateMRR;
  
  document.getElementById('revenue-stats').innerHTML = `
    <div class="revenue-dashboard">
      <h2>Monthly Recurring Revenue (MRR)</h2>
      <div class="revenue-breakdown">
        <div class="revenue-card">
          <h3>Recruiter MRR</h3>
          <p class="amount">${recruiterMRR.toLocaleString()} VND</p>
          <p class="count">${recruiterPaid.result.totalElements} active subscriptions</p>
        </div>
        <div class="revenue-card">
          <h3>Candidate MRR</h3>
          <p class="amount">${candidateMRR.toLocaleString()} VND</p>
          <p class="count">${candidatePaid.result.totalElements} active subscriptions</p>
        </div>
        <div class="revenue-card total">
          <h3>Total MRR</h3>
          <p class="amount">${totalMRR.toLocaleString()} VND</p>
        </div>
      </div>
    </div>
  `;
}
```

### Example 5: Export Invoice Data
```javascript
async function exportInvoicesToCSV(userType) {
  // Fetch all invoices (use large page size)
  const response = await fetch(`/admin/invoices/${userType}?page=0&size=10000`, {
    headers: { 'Authorization': `Bearer ${adminToken}` }
  });
  
  const data = await response.json();
  const invoices = data.result.content;
  
  // Convert to CSV
  const csvHeader = 'ID,Full Name,Package,Amount (VND),Status,Start Date,End Date,Cancelled At,Is Active\n';
  const csvRows = invoices.map(inv => 
    `${inv.id},"${inv.fullname}",${inv.packageName},${inv.amount},${inv.status},${inv.startDate},${inv.endDate},${inv.cancelledAt || ''},${inv.isActive}`
  ).join('\n');
  
  const csv = csvHeader + csvRows;
  
  // Download CSV file
  const blob = new Blob([csv], { type: 'text/csv' });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${userType}_invoices_${new Date().toISOString().split('T')[0]}.csv`;
  a.click();
  window.URL.revokeObjectURL(url);
}
```

## Use Cases

### 1. Subscription Monitoring
Monitor all active subscriptions to understand current user base and revenue.

### 2. Churn Analysis
Track cancelled subscriptions to analyze churn patterns and identify reasons for cancellation.

### 3. Revenue Reporting
Calculate total revenue, MRR (Monthly Recurring Revenue), and revenue breakdown by package type.

### 4. Renewal Reminders
Identify subscriptions expiring soon to send renewal reminders.

### 5. Customer Support
Look up specific user subscriptions to assist with billing inquiries.

### 6. Financial Auditing
Export complete invoice history for financial audits and reporting.

## Best Practices

1. **Pagination:** Use appropriate page sizes (5-50) to balance performance and usability
2. **Filtering:** Apply filters to reduce data volume and improve query performance
3. **Caching:** Consider caching dashboard statistics for better performance
4. **Export Limits:** When exporting data, be mindful of memory limitations with large datasets
5. **Real-time Updates:** Refresh invoice lists periodically to show latest data
6. **Status Badges:** Use visual indicators (colors, badges) for quick status identification
7. **Date Formatting:** Display dates in user-friendly format while maintaining ISO format in data

## Performance Considerations

- Default page size is 5 to ensure fast response times
- For analytics/reports, use larger page sizes (up to 10,000)
- Filtering reduces query time significantly
- Consider implementing caching for frequently accessed statistics
- Use proper database indexes on status and isActive columns

## Important Notes

- **Admin Only:** These endpoints are strictly for administrators
- **No Modification:** Current implementation only supports viewing, not modifying invoices
- **Complete History:** Returns all invoices including expired and cancelled ones
- **Default Sorting:** Results are typically sorted by creation date or ID
- **Full Name Included:** Makes it easy to identify users without additional queries
- **Cancellation Tracking:** cancelledAt field helps track when users cancelled
- **Active vs Status:** isActive flag shows current state, status shows lifecycle state

## Related Admin Endpoints

While not in this controller, admins may also need:
- `GET /admin/users/recruiters` - Manage recruiter accounts
- `GET /admin/users/candidates` - Manage candidate accounts
- `GET /admin/packages` - View and manage package offerings
- `POST /admin/packages` - Create new package tiers
- `GET /admin/analytics/revenue` - Revenue analytics dashboard

