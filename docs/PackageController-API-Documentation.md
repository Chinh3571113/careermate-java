# Package Controller API Documentation

## Overview
Controller manages package functionalities for both candidates and recruiters, providing package listings with their entitlements (benefits/features).

**Base URL:** `/api/package`

**Tag:** Package

---

## API Endpoints

### 1. Get Package List for Candidate

#### Endpoint
```
GET /api/package/candidate
```

#### Purpose
Retrieve a list of all available packages for candidates with their pricing, duration, and entitlements (benefits/features included in each package).

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
GET /api/package/candidate
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "result": [
    {
      "name": "Basic",
      "price": 50000,
      "durationDays": 30,
      "entitlements": [
        {
          "name": "Profile Views",
          "code": "PROFILE_VIEWS",
          "unit": "views",
          "hasLimit": true,
          "enabled": true,
          "limitValue": 100
        },
        {
          "name": "Job Applications",
          "code": "JOB_APPLICATIONS",
          "unit": "applications",
          "hasLimit": true,
          "enabled": true,
          "limitValue": 10
        },
        {
          "name": "AI Resume Review",
          "code": "AI_RESUME_REVIEW",
          "unit": "reviews",
          "hasLimit": true,
          "enabled": true,
          "limitValue": 3
        }
      ]
    },
    {
      "name": "Premium",
      "price": 150000,
      "durationDays": 90,
      "entitlements": [
        {
          "name": "Profile Views",
          "code": "PROFILE_VIEWS",
          "unit": "views",
          "hasLimit": false,
          "enabled": true,
          "limitValue": 0
        },
        {
          "name": "Job Applications",
          "code": "JOB_APPLICATIONS",
          "unit": "applications",
          "hasLimit": false,
          "enabled": true,
          "limitValue": 0
        },
        {
          "name": "AI Resume Review",
          "code": "AI_RESUME_REVIEW",
          "unit": "reviews",
          "hasLimit": false,
          "enabled": true,
          "limitValue": 0
        }
      ]
    }
  ]
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (List<PackageResponse>): List of candidate packages

**PackageResponse:**
- **name** (String): Package name
- **price** (Long): Package price in VND (Vietnamese Dong)
- **durationDays** (int): Package validity duration in days
- **entitlements** (List<EntitlementResponse>): List of benefits/features included

**EntitlementResponse:**
- **name** (String): Entitlement display name
- **code** (String): Entitlement unique code identifier
- **unit** (String): Unit of measurement (e.g., "views", "applications", "reviews")
- **hasLimit** (boolean): Whether this entitlement has a usage limit
- **enabled** (boolean): Whether this entitlement is currently active
- **limitValue** (int): Maximum usage limit (0 means unlimited when hasLimit is false)

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

1. Verify user has CANDIDATE role via JWT token
2. Query database to fetch all candidate packages with their entitlements using optimized join query
3. Map package entities to PackageResponse DTOs using mapper
4. Return complete list of packages with all entitlements

#### Notes
- Required role: `ROLE_CANDIDATE`
- Returns all available packages regardless of user's current subscription
- Price is in VND (Vietnamese Dong)
- Duration is in days
- Entitlements with `hasLimit = false` and `limitValue = 0` means unlimited usage
- Use this endpoint to display package options for candidates to purchase

---

### 2. Get Package List for Recruiter

#### Endpoint
```
GET /api/package/recruiter
```

#### Purpose
Retrieve a list of all available packages for recruiters with their pricing, duration, and entitlements (benefits/features included in each package).

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
GET /api/package/recruiter
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "result": [
    {
      "name": "Starter",
      "price": 100000,
      "durationDays": 30,
      "entitlements": [
        {
          "name": "Job Postings",
          "code": "JOB_POSTINGS",
          "unit": "postings",
          "hasLimit": true,
          "enabled": true,
          "limitValue": 5
        },
        {
          "name": "Candidate Search",
          "code": "CANDIDATE_SEARCH",
          "unit": "searches",
          "hasLimit": true,
          "enabled": true,
          "limitValue": 50
        },
        {
          "name": "Featured Job",
          "code": "FEATURED_JOB",
          "unit": "jobs",
          "hasLimit": true,
          "enabled": false,
          "limitValue": 0
        }
      ]
    },
    {
      "name": "Professional",
      "price": 250000,
      "durationDays": 90,
      "entitlements": [
        {
          "name": "Job Postings",
          "code": "JOB_POSTINGS",
          "unit": "postings",
          "hasLimit": true,
          "enabled": true,
          "limitValue": 20
        },
        {
          "name": "Candidate Search",
          "code": "CANDIDATE_SEARCH",
          "unit": "searches",
          "hasLimit": false,
          "enabled": true,
          "limitValue": 0
        },
        {
          "name": "Featured Job",
          "code": "FEATURED_JOB",
          "unit": "jobs",
          "hasLimit": true,
          "enabled": true,
          "limitValue": 3
        }
      ]
    }
  ]
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (List<PackageResponse>): List of recruiter packages

**PackageResponse:**
- **name** (String): Package name
- **price** (Long): Package price in VND (Vietnamese Dong)
- **durationDays** (int): Package validity duration in days
- **entitlements** (List<EntitlementResponse>): List of benefits/features included

**EntitlementResponse:**
- **name** (String): Entitlement display name
- **code** (String): Entitlement unique code identifier
- **unit** (String): Unit of measurement (e.g., "postings", "searches", "jobs")
- **hasLimit** (boolean): Whether this entitlement has a usage limit
- **enabled** (boolean): Whether this entitlement is currently active/available in the package
- **limitValue** (int): Maximum usage limit (0 means unlimited when hasLimit is false, or not available when enabled is false)

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

1. Verify user has RECRUITER role via JWT token
2. Query database to fetch all recruiter packages with their entitlements using optimized join query
3. Map package entities to PackageResponse DTOs using mapper
4. Return complete list of packages with all entitlements

#### Notes
- Required role: `ROLE_RECRUITER`
- Returns all available packages regardless of user's current subscription
- Price is in VND (Vietnamese Dong)
- Duration is in days
- Entitlements with `hasLimit = false` and `limitValue = 0` means unlimited usage
- Entitlements with `enabled = false` are not available in that package tier
- Use this endpoint to display package options for recruiters to purchase

---

## Common Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | User does not have access permission (not CANDIDATE or RECRUITER) |
| 500 | Internal Server Error | Server error |

## Security

- Endpoint `/candidate` requires authentication and role `CANDIDATE`
- Endpoint `/recruiter` requires authentication and role `RECRUITER`
- Uses JWT Bearer token for authentication
- Security is implemented through `@PreAuthorize` annotations

## Database Dependencies

- **CandidatePackage entity:** Stores package information for candidates
- **RecruiterPackage entity:** Stores package information for recruiters
- **Entitlement entity:** Stores benefits/features associated with packages
- Repositories use optimized queries with JOIN FETCH to load entitlements in single query

## Package Structure

### Candidate Packages
Candidate packages typically include entitlements such as:
- Profile visibility/views
- Job application limits
- Resume review services
- Interview preparation access
- Career coaching sessions
- Roadmap access

### Recruiter Packages
Recruiter packages typically include entitlements such as:
- Job posting limits
- Candidate search/database access
- Featured/highlighted job postings
- Applicant tracking features
- Advanced analytics
- Priority support

## Pricing Information

**Price Constraints (from PackageCreationRequest):**
- **Minimum price:** 50,000 VND
- **Maximum price:** 300,000 VND
- **Minimum duration:** 7 days
- **Maximum duration:** 365 days

Note: These constraints are used when creating/updating packages (admin functionality not exposed in this controller).

## Related Services

- `PackageImp`: Business logic service
- `PackageMapper`: Mapper to convert entities to DTOs
- `CandidatePackageRepo`: Repository for CandidatePackage entity
- `RecruiterPackageRepo`: Repository for RecruiterPackage entity

## Usage Example

### For Candidates:
1. Candidate logs in and receives JWT token
2. Call `GET /api/package/candidate` to view available packages
3. Choose a package based on price, duration, and entitlements
4. Proceed to purchase (order/payment flow - separate controller)

### For Recruiters:
1. Recruiter logs in and receives JWT token
2. Call `GET /api/package/recruiter` to view available packages
3. Compare packages based on job posting limits and features
4. Select appropriate package for business needs
5. Proceed to purchase (order/payment flow - separate controller)

