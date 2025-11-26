# SavedJob Controller API Documentation

## Overview
Controller manages functionalities related to saving jobs (saved jobs) for candidates.

**Base URL:** `/api/saved-jobs`

**Tag:** Candidate - Saved Job Posting

---

## API Endpoints

### 1. Toggle Save/Unsave Job Posting

#### Endpoint
```
POST /api/saved-jobs/toggle/{jobId}
```

#### Purpose
Allows candidates to save or unsave a job. If the job is already saved, it will be unsaved; if not saved, it will be added to the saved jobs list.

#### Request

**Method:** `POST`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Path Parameters:**
- **jobId** (int, required): ID of the job posting to save/unsave

**Request Body:** None

**Example Request:**
```bash
POST /api/saved-jobs/toggle/123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "result": true
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (boolean): `true` = job has been saved, `false` = job has been unsaved

#### Error Response

**404 Not Found - Job Posting Not Found:**
```json
{
  "code": 404,
  "message": "JOB_POSTING_NOT_FOUND",
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
2. Check if job posting exists
   - If not exists → Throw `JOB_POSTING_NOT_FOUND` exception
3. Search if candidate has already saved this job
   - If already saved → Delete from saved jobs list and return `false`
   - If not saved → Create new SavedJob entity with current timestamp and return `true`

#### Notes
- Required role: `ROLE_CANDIDATE`
- This API uses toggle pattern, no need to know current state, just call to reverse the state
- Job save time is recorded automatically (`savedAt = LocalDateTime.now()`)

---

### 2. Get All Saved Job Postings

#### Endpoint
```
GET /api/saved-jobs
```

#### Purpose
Retrieve a list of all job postings that the candidate has saved, with pagination support.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:**
- **page** (int, optional, default: 0): Page number (starts from 0)
- **size** (int, optional, default: 5): Number of items per page

**Request Body:** None

**Example Request:**
```bash
GET /api/saved-jobs?page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "result": {
    "content": [
      {
        "savedJobId": 1,
        "title": "Senior Java Developer",
        "companyName": "FPT Software",
        "companyAddress": "Hà Nội",
        "salaryRange": "1000-2000 USD",
        "skills": "Java, Spring Boot, MySQL",
        "yearOfExperience": "3-5",
        "workModel": "Hybrid",
        "expirationDate": "2025-12-31",
        "savedAt": "2025-11-20T10:30:00",
        "jobId": 123
      }
    ],
    "number": 0,
    "size": 5,
    "totalElements": 15,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (PageSavedJobPostingResponse): Paginated data

**PageSavedJobPostingResponse:**
- **content** (List<SavedJobPostingResponse>): List of saved jobs
- **number** (int): Current page number (starts from 0)
- **size** (int): Number of items per page
- **totalElements** (long): Total number of items across all pages
- **totalPages** (int): Total number of pages
- **first** (boolean): Whether this is the first page
- **last** (boolean): Whether this is the last page

**SavedJobPostingResponse:**
- **savedJobId** (int): ID of the saved job record
- **title** (String): Job title
- **companyName** (String): Company name
- **companyAddress** (String): Company address
- **salaryRange** (String): Salary range
- **skills** (String): List of skills (comma-separated)
- **yearOfExperience** (String): Required years of experience
- **workModel** (String): Work model (Remote/Onsite/Hybrid)
- **expirationDate** (String): Job posting expiration date
- **savedAt** (String): Time when candidate saved this job
- **jobId** (int): Job posting ID

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

1. Get current candidate information from JWT token
2. Create Pageable with:
   - Page number and size from request parameters
   - Sort by `savedAt` descending (most recently saved jobs displayed first)
3. Query database to get candidate's saved jobs
4. Convert entity to DTO response:
   - Get all skill names from jobDescriptions
   - Remove duplicates, sort alphabetically
   - Concatenate into a comma-separated string
5. Return result with pagination information

#### Notes
- Required role: `ROLE_CANDIDATE`
- Default display is 5 items per page
- List is sorted by most recent save time
- Skills are automatically deduplicated and sorted alphabetically

---

### 3. Get Jobs For Candidate

#### Endpoint
```
GET /api/saved-jobs/jobs-for-candidate
```

#### Purpose
Retrieve a list of all active job postings for candidates to view, along with information about whether the candidate has saved that job. This API is used to display in the "Jobs" tab with search and filter capabilities.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN> (Optional)
```

**Query Parameters:**
- **keyword** (String, optional, default: null): Search keyword (not yet implemented in logic)
- **page** (int, optional, default: 0): Page number (starts from 0)
- **size** (int, optional, default: 10): Number of items per page
- **sortBy** (String, optional, default: createAt): Field to sort by
- **sortDir** (String, optional, default: desc): Sort direction (asc/desc)
- **candidateId** (int, optional, default: 0): Candidate ID to check saved status

**Request Body:** None

**Example Request:**
```bash
GET /api/saved-jobs/jobs-for-candidate?page=0&size=10&candidateId=5
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "result": {
    "content": [
      {
        "id": 123,
        "title": "Senior Java Developer",
        "description": "We are looking for...",
        "address": "Hà Nội",
        "expirationDate": "2025-12-31",
        "postTime": "2025-11-01",
        "skills": [
          {
            "id": 1,
            "name": "Java",
            "description": "Java programming language"
          },
          {
            "id": 2,
            "name": "Spring Boot",
            "description": "Spring Boot framework"
          }
        ],
        "yearsOfExperience": 3,
        "workModel": "Hybrid",
        "salaryRange": "1000-2000 USD",
        "reason": "Good match for your profile",
        "jobPackage": "Premium",
        "recruiterInfo": {
          "recruiterId": 10,
          "companyName": "FPT Software",
          "website": "https://fptsoftware.com",
          "logoUrl": "https://example.com/logo.png",
          "about": "Leading IT company in Vietnam"
        },
        "isSaved": true
      }
    ],
    "number": 0,
    "size": 10,
    "totalElements": 50,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (PageJobPostingForCandidateResponse): Paginated data

**PageJobPostingForCandidateResponse:**
- **content** (List<JobPostingForCandidateResponse>): List of job postings
- **number** (int): Current page number (starts from 0)
- **size** (int): Number of items per page
- **totalElements** (long): Total number of items across all pages
- **totalPages** (int): Total number of pages
- **first** (boolean): Whether this is the first page
- **last** (boolean): Whether this is the last page

**JobPostingForCandidateResponse:**
- **id** (int): Job posting ID
- **title** (String): Job title
- **description** (String): Detailed job description
- **address** (String): Work location address
- **expirationDate** (LocalDate): Expiration date
- **postTime** (LocalDate): Posting date
- **skills** (Set<JobPostingSkillResponse>): List of required skills
- **yearsOfExperience** (int): Required years of experience
- **workModel** (String): Work model
- **salaryRange** (String): Salary range
- **reason** (String): Reason for recommending this job
- **jobPackage** (String): Job posting package
- **recruiterInfo** (RecruiterCompanyInfo): Recruiting company information
- **isSaved** (boolean): Whether candidate has saved this job

**RecruiterCompanyInfo:**
- **recruiterId** (int): Recruiter ID
- **companyName** (String): Company name
- **website** (String): Company website
- **logoUrl** (String): Company logo URL
- **about** (String): About the company

#### Error Response

**400 Bad Request - Invalid parameters:**
```json
{
  "code": 400,
  "message": "Invalid request parameters",
  "result": null
}
```

#### Main Logic

1. Create Pageable with pagination information and sort by `createAt` descending (most recently posted jobs displayed first)
2. Query all job postings that have:
   - Status = ACTIVE
   - Recruiter verification status = APPROVED
3. For each job posting:
   - Convert to DTO response
   - Set recruiter/company information
   - Set skills list from job descriptions
   - Check if candidate has saved this job:
     - If `candidateId = 0` → set `isSaved = false` (user not logged in or not a candidate)
     - If `candidateId != 0` → query database to check and set `isSaved`
4. Return result with pagination information

#### Notes
- This API can be called without authentication (public)
- If `candidateId` is not provided or = 0, all jobs will have `isSaved = false`
- Parameters `keyword`, `sortBy`, `sortDir` are defined but not yet used in current logic
- Only displays job postings from approved recruiters
- Default sort by most recent posting date

---

## Common Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | User does not have access permission (not a CANDIDATE) |
| 404 | Not Found | Resource does not exist (Job Posting Not Found) |
| 500 | Internal Server Error | Server error |

## Security

- Endpoints `/toggle/{jobId}` and `/getSavedJobs` require authentication and role `CANDIDATE`
- Endpoint `/jobs-for-candidate` can be accessed publicly but saved status feature only works with valid candidateId
- Uses JWT Bearer token for authentication
- Security is implemented through `@PreAuthorize("hasRole('CANDIDATE')")` annotation

## Database Dependencies

- **SavedJob entity:** Stores relationship between candidate and job posting
- **JobPosting entity:** Job information
- **Candidate entity:** Candidate information
- **Recruiter entity:** Recruiter information
- **JobDescription entity:** Job details and required skills

## Related Services

- `SavedJobImp`: Business logic service
- `CoachUtil`: Utility to get current user information
- `SavedJobMapper`: Mapper to convert entity to DTO
- `JobPostingMapper`: Mapper to convert job posting entity to DTO
