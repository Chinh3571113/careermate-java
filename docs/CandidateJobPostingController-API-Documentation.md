# Candidate Job Posting Controller API Documentation

## Overview
Controller manages public job posting functionalities, allowing users to browse and search approved job postings, view company information, and access job details. This is a public API that doesn't require authentication for most endpoints.

**Base URL:** `/api/job-postings`

**Tag:** Job Postings

---

## API Endpoints

### 1. Get All Approved Job Postings

#### Endpoint
```
GET /api/job-postings
```

#### Purpose
Retrieve all approved and active job postings that haven't expired. Supports keyword search (searches in title, description, and address) with pagination and sorting capabilities.

#### Request

**Method:** `GET`

**Headers:** None required (public endpoint)

**Query Parameters:**
- **keyword** (String, optional): Search term to filter job postings by title, description, or address
- **page** (int, optional, default: 0): Page number (starts from 0)
- **size** (int, optional, default: 10): Number of items per page
- **sortBy** (String, optional, default: createAt): Field to sort by (e.g., createAt, expirationDate, title)
- **sortDir** (String, optional, default: desc): Sort direction - "asc" for ascending or "desc" for descending

**Request Body:** None

**Example Requests:**
```bash
# Basic request with default pagination
GET /api/job-postings?page=0&size=10

# Search for developer jobs
GET /api/job-postings?keyword=developer&page=0&size=20

# Search with custom sorting
GET /api/job-postings?keyword=java&sortBy=expirationDate&sortDir=asc

# Get all jobs sorted by newest first
GET /api/job-postings?sortBy=createAt&sortDir=desc
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "Job postings retrieved successfully",
  "result": {
    "content": [
      {
        "id": 123,
        "title": "Senior Java Developer",
        "description": "We are looking for an experienced Java developer...",
        "address": "Hanoi, Vietnam",
        "expirationDate": "2025-12-31",
        "postTime": "2025-11-01",
        "skills": [
          {
            "id": 1,
            "name": "Java",
            "mustToHave": true
          },
          {
            "id": 2,
            "name": "Spring Boot",
            "mustToHave": true
          },
          {
            "id": 3,
            "name": "Docker",
            "mustToHave": false
          }
        ],
        "yearsOfExperience": 3,
        "workModel": "Hybrid",
        "salaryRange": "1000-2000 USD",
        "reason": "Matches your profile",
        "jobPackage": "Premium",
        "recruiterInfo": {
          "recruiterId": 10,
          "companyName": "FPT Software",
          "website": "https://fptsoftware.com",
          "logoUrl": "https://example.com/logo.png",
          "about": "Leading IT company in Vietnam"
        },
        "isSaved": false
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
- **result** (PageResponse<JobPostingForCandidateResponse>): Paginated job postings data

**PageResponse:**
- **content** (List<JobPostingForCandidateResponse>): List of job postings
- **number** (int): Current page number (starts from 0)
- **size** (int): Number of items per page
- **totalElements** (long): Total number of job postings across all pages
- **totalPages** (int): Total number of pages
- **first** (boolean): Whether this is the first page
- **last** (boolean): Whether this is the last page

**JobPostingForCandidateResponse:**
- **id** (int): Job posting ID
- **title** (String): Job title
- **description** (String): Detailed job description
- **address** (String): Work location
- **expirationDate** (LocalDate): Job posting expiration date
- **postTime** (LocalDate): Date when job was posted
- **skills** (Set<JobPostingSkillResponse>): Required skills with must-have flag
- **yearsOfExperience** (int): Required years of experience
- **workModel** (String): Work arrangement (Remote/Onsite/Hybrid)
- **salaryRange** (String): Salary range
- **reason** (String): Why this job is recommended
- **jobPackage** (String): Posting package type (Basic/Premium)
- **recruiterInfo** (RecruiterCompanyInfo): Company information
- **isSaved** (boolean): Whether job is saved by user (always false for unauthenticated requests)

**JobPostingSkillResponse:**
- **id** (int): Skill ID
- **name** (String): Skill name
- **mustToHave** (boolean): Whether this is a required (must-have) skill or optional (nice-to-have)

**RecruiterCompanyInfo:**
- **recruiterId** (int): Recruiter/company ID
- **companyName** (String): Company name
- **website** (String): Company website URL
- **logoUrl** (String): Company logo URL
- **about** (String): Company description

#### Error Response

**400 Bad Request - Invalid Parameters:**
```json
{
  "code": 400,
  "message": "Invalid request parameters",
  "result": null
}
```

#### Main Logic

1. Parse query parameters with default values
2. Create Sort object based on sortBy and sortDir parameters
3. Create Pageable object with page, size, and sort
4. Call service to retrieve approved job postings:
   - Filter by status = ACTIVE
   - Filter by recruiter verification status = APPROVED
   - Filter by expiration date (not expired)
   - If keyword provided: Search in title, description, and address (case-insensitive)
5. Convert entities to DTO responses
6. Return paginated results with metadata

#### Notes
- **Public endpoint** - No authentication required
- Only shows approved and active job postings
- Expired job postings are excluded automatically
- Keyword search is case-insensitive and searches across multiple fields
- Default sorting is by creation date (newest first)
- **isSaved** field is always false for unauthenticated users

---

### 2. Get Job Posting Detail

#### Endpoint
```
GET /api/job-postings/{id}
```

#### Purpose
Retrieve detailed information about a specific approved job posting. Only active and non-expired job postings can be viewed.

#### Request

**Method:** `GET`

**Headers:** None required (public endpoint)

**Path Parameters:**
- **id** (int, required): Job posting ID

**Query Parameters:** None

**Request Body:** None

**Example Request:**
```bash
GET /api/job-postings/123
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "Job posting detail retrieved successfully",
  "result": {
    "id": 123,
    "title": "Senior Java Developer",
    "description": "We are looking for an experienced Java developer with strong backend skills. Responsibilities include designing and implementing scalable microservices, code reviews, and mentoring junior developers.",
    "address": "Hanoi, Vietnam - Hoa Lac Hi-Tech Park",
    "expirationDate": "2025-12-31",
    "postTime": "2025-11-01",
    "skills": [
      {
        "id": 1,
        "name": "Java",
        "mustToHave": true
      },
      {
        "id": 2,
        "name": "Spring Boot",
        "mustToHave": true
      },
      {
        "id": 3,
        "name": "MySQL",
        "mustToHave": true
      },
      {
        "id": 4,
        "name": "Docker",
        "mustToHave": false
      },
      {
        "id": 5,
        "name": "Kubernetes",
        "mustToHave": false
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
      "logoUrl": "https://example.com/fpt-logo.png",
      "about": "FPT Software is a leading IT company in Vietnam, specializing in software development, digital transformation, and IT services."
    },
    "isSaved": false
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (JobPostingForCandidateResponse): Job posting details

*See "Get All Approved Job Postings" for detailed field descriptions*

#### Error Response

**404 Not Found - Job Posting Not Found:**
```json
{
  "code": 404,
  "message": "JOB_POSTING_NOT_FOUND",
  "result": null
}
```

**410 Gone - Job Posting Expired:**
```json
{
  "code": 410,
  "message": "JOB_POSTING_EXPIRED",
  "result": null
}
```

#### Main Logic

1. Query database to find job posting by ID with status = ACTIVE
2. If not found → Throw `JOB_POSTING_NOT_FOUND` exception
3. Check if job posting has expired (expirationDate < current date)
4. If expired → Throw `JOB_POSTING_EXPIRED` exception
5. Convert entity to DTO response:
   - Map all job posting fields
   - Extract and map skills from job descriptions with must-have flag
   - Map recruiter/company information
6. Return complete job posting details

#### Notes
- **Public endpoint** - No authentication required
- Only shows approved and active job postings
- Expired jobs return 410 error even if they exist in database
- Skills are separated into must-have (required) and nice-to-have (optional)
- Full job description and company information included

---

### 3. Get Job Postings of a Specific Company

#### Endpoint
```
GET /api/job-postings/company/list/{recruiterId}
```

#### Purpose
Retrieve all job postings from a specific company/recruiter. Supports pagination and optional keyword search. Optionally checks if jobs are saved by a specific candidate.

#### Request

**Method:** `GET`

**Headers:** None required (public endpoint)

**Path Parameters:**
- **recruiterId** (int, required): ID of the recruiter/company

**Query Parameters:**
- **page** (int, required): Page number (starts from 0)
- **size** (int, required): Number of items per page
- **keyword** (String, optional): Search term to filter job postings within this company
- **candidateId** (int, optional, default: 0): Candidate ID to check if jobs are saved (0 = not checking)

**Request Body:** None

**Example Requests:**
```bash
# Get all jobs from company ID 10
GET /api/job-postings/company/list/10?page=0&size=10

# Search for Java jobs in company
GET /api/job-postings/company/list/10?page=0&size=10&keyword=java

# Check saved status for candidate ID 5
GET /api/job-postings/company/list/10?page=0&size=10&candidateId=5
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "List job posting of Company detail retrieved successfully",
  "result": {
    "content": [
      {
        "id": 123,
        "title": "Senior Java Developer",
        "description": "We are looking for...",
        "address": "Hanoi, Vietnam",
        "expirationDate": "2025-12-31",
        "postTime": "2025-11-01",
        "skills": [
          {
            "id": 1,
            "name": "Java",
            "mustToHave": true
          }
        ],
        "yearsOfExperience": 3,
        "workModel": "Hybrid",
        "salaryRange": "1000-2000 USD",
        "reason": null,
        "jobPackage": "Premium",
        "recruiterInfo": {
          "recruiterId": 10,
          "companyName": "FPT Software",
          "website": "https://fptsoftware.com",
          "logoUrl": "https://example.com/logo.png",
          "about": "Leading IT company"
        },
        "isSaved": true
      }
    ],
    "number": 0,
    "size": 10,
    "totalElements": 25,
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
- **result** (PageJobPostingForRecruiterResponse): Paginated job postings from company

**PageJobPostingForRecruiterResponse:** (Same structure as PageResponse<JobPostingForCandidateResponse>)
- **content** (List<JobPostingForCandidateResponse>): List of job postings
- **number** (int): Current page number
- **size** (int): Items per page
- **totalElements** (long): Total jobs from this company
- **totalPages** (int): Total pages
- **first** (boolean): Is first page
- **last** (boolean): Is last page

#### Error Response

**404 Not Found - Recruiter Not Found:**
```json
{
  "code": 404,
  "message": "RECRUITER_NOT_FOUND",
  "result": null
}
```

#### Main Logic

1. Query job postings filtered by recruiter ID
2. Filter by status = ACTIVE
3. If keyword provided: Filter by title or description containing keyword
4. Apply pagination
5. For each job posting:
   - Convert to DTO
   - If candidateId > 0: Check if candidate has saved this job
   - Set isSaved flag accordingly
6. Return paginated results

#### Notes
- **Public endpoint** - No authentication required
- Shows only active job postings from the specified company
- **candidateId = 0**: All jobs show isSaved = false
- **candidateId > 0**: Checks saved status for that specific candidate
- Useful for company profile pages showing all their job openings

---

### 4. Get Company Detail

#### Endpoint
```
GET /api/job-postings/company/{recruiterId}
```

#### Purpose
Retrieve detailed information about a specific company/recruiter including company profile, about section, website, and logo.

#### Request

**Method:** `GET`

**Headers:** None required (public endpoint)

**Path Parameters:**
- **recruiterId** (int, required): ID of the recruiter/company

**Query Parameters:** None

**Request Body:** None

**Example Request:**
```bash
GET /api/job-postings/company/10
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "Company detail retrieved successfully",
  "result": {
    "recruiterId": 10,
    "companyName": "FPT Software",
    "website": "https://fptsoftware.com",
    "logoUrl": "https://example.com/fpt-logo.png",
    "about": "FPT Software is a leading IT company in Vietnam with over 20,000 employees. We specialize in software development, digital transformation, cloud services, and IT consulting. Our clients include Fortune 500 companies across various industries."
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (RecruiterCompanyInfo): Company information

**RecruiterCompanyInfo:**
- **recruiterId** (int): Recruiter/company ID
- **companyName** (String): Official company name
- **website** (String): Company website URL
- **logoUrl** (String): URL to company logo image
- **about** (String): Detailed description about the company

#### Error Response

**404 Not Found - Recruiter Not Found:**
```json
{
  "code": 404,
  "message": "RECRUITER_NOT_FOUND",
  "result": null
}
```

#### Main Logic

1. Query database to find recruiter by ID
2. If not found → Throw `RECRUITER_NOT_FOUND` exception
3. Map recruiter entity to RecruiterCompanyInfo DTO
4. Return company details

#### Notes
- **Public endpoint** - No authentication required
- Returns company information regardless of verification status
- Use this endpoint for company profile pages
- Does not include job postings (use endpoint #3 for that)

---

### 5. Get List of Companies

#### Endpoint
```
GET /api/job-postings/company
```

#### Purpose
Retrieve a paginated list of all approved companies/recruiters with their active job counts. Supports filtering by company address.

#### Request

**Method:** `GET`

**Headers:** None required (public endpoint)

**Query Parameters:**
- **page** (int, required): Page number (starts from 0)
- **size** (int, required): Number of items per page
- **companyAddress** (String, optional): Filter companies by address (partial match, case-insensitive)

**Request Body:** None

**Example Requests:**
```bash
# Get all approved companies
GET /api/job-postings/company?page=0&size=20

# Filter companies in Hanoi
GET /api/job-postings/company?page=0&size=20&companyAddress=Hanoi

# Filter companies in Ho Chi Minh City
GET /api/job-postings/company?page=0&size=20&companyAddress=Ho%20Chi%20Minh
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "Companies retrieved successfully",
  "result": {
    "content": [
      {
        "id": 10,
        "companyName": "FPT Software",
        "companyAddress": "Hanoi, Vietnam",
        "logoUrl": "https://example.com/fpt-logo.png",
        "jobCount": 25
      },
      {
        "id": 11,
        "companyName": "Viettel Solutions",
        "companyAddress": "Hanoi, Vietnam",
        "logoUrl": "https://example.com/viettel-logo.png",
        "jobCount": 15
      },
      {
        "id": 12,
        "companyName": "VNG Corporation",
        "companyAddress": "Ho Chi Minh City, Vietnam",
        "logoUrl": "https://example.com/vng-logo.png",
        "jobCount": 30
      }
    ],
    "number": 0,
    "size": 20,
    "totalElements": 50,
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
- **result** (PageRecruiterResponse): Paginated list of companies

**PageRecruiterResponse:**
- **content** (List<RecruiterResponse>): List of companies
- **number** (int): Current page number
- **size** (int): Items per page
- **totalElements** (long): Total number of approved companies
- **totalPages** (int): Total pages
- **first** (boolean): Is first page
- **last** (boolean): Is last page

**RecruiterResponse:**
- **id** (int): Recruiter/company ID
- **companyName** (String): Company name
- **companyAddress** (String): Company location/address
- **logoUrl** (String): Company logo URL
- **jobCount** (long): Number of active job postings from this company

#### Error Response

**400 Bad Request - Invalid Parameters:**
```json
{
  "code": 400,
  "message": "Invalid request parameters",
  "result": null
}
```

#### Main Logic

1. Create Pageable with sorting by company name (ascending)
2. Query approved recruiters (verification status = APPROVED):
   - If companyAddress provided: Filter by address (case-insensitive, partial match)
   - If companyAddress is null/empty: Get all approved recruiters
3. For each recruiter:
   - Convert to RecruiterResponse DTO
   - Count active job postings for this recruiter
   - Set jobCount field
4. Return paginated results with metadata

#### Notes
- **Public endpoint** - No authentication required
- Only shows approved/verified companies
- **jobCount** shows number of currently active job postings
- Sorted alphabetically by company name
- Address filter is case-insensitive and matches partial strings
- Use this for "Browse Companies" or "Company Directory" pages

---

### 6. Get Company Addresses for Autocomplete

#### Endpoint
```
GET /api/job-postings/addresses
```

#### Purpose
Get a list of distinct company addresses for autocomplete functionality. Returns addresses from approved recruiters that match the keyword, useful for search filters and location dropdowns.

#### Request

**Method:** `GET`

**Headers:** None required (public endpoint)

**Query Parameters:**
- **keyword** (String, optional): Search term to filter addresses (case-insensitive, partial match)
- **limit** (int, optional, default: 10): Maximum number of results to return

**Request Body:** None

**Example Requests:**
```bash
# Get all addresses (up to 10)
GET /api/job-postings/addresses?limit=10

# Search for addresses containing "hanoi"
GET /api/job-postings/addresses?keyword=hanoi&limit=5

# Get more results
GET /api/job-postings/addresses?limit=20

# Search for Ho Chi Minh City
GET /api/job-postings/addresses?keyword=ho%20chi%20minh&limit=10
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "Addresses retrieved successfully",
  "result": [
    "Hanoi, Vietnam",
    "Hanoi - Hoa Lac Hi-Tech Park",
    "Ho Chi Minh City, Vietnam",
    "Ho Chi Minh City - District 1",
    "Da Nang, Vietnam",
    "Hai Phong, Vietnam",
    "Can Tho, Vietnam"
  ]
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (List<String>): List of distinct company addresses

#### Error Response

**400 Bad Request - Invalid Parameters:**
```json
{
  "code": 400,
  "message": "Invalid request parameters",
  "result": null
}
```

#### Main Logic

1. If keyword is null or empty: Set searchKeyword to empty string (returns all)
2. Create Pageable with limit (page 0, size = limit)
3. Query distinct company addresses from approved recruiters:
   - Filter by verification status = APPROVED
   - Filter by address containing keyword (case-insensitive)
   - Get distinct values only
   - Limit results to specified number
4. Return list of address strings

#### Notes
- **Public endpoint** - No authentication required
- Returns distinct addresses only (no duplicates)
- Only includes addresses from approved recruiters
- Case-insensitive search
- Partial match on keyword
- **Use cases:**
  - Autocomplete input fields for address search
  - Dropdown filters for job location
  - Location suggestion in search forms
- Default limit of 10 keeps response lightweight
- If keyword is empty, returns most common addresses (up to limit)

---

## Common Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | Bad Request | Invalid request parameters |
| 404 | JOB_POSTING_NOT_FOUND | Job posting does not exist or not approved |
| 404 | RECRUITER_NOT_FOUND | Company/recruiter does not exist |
| 410 | JOB_POSTING_EXPIRED | Job posting has expired |
| 500 | Internal Server Error | Server error |

## Security

- **All endpoints are public** - No authentication required
- Only approved and active content is displayed
- Expired job postings are filtered out automatically
- Only approved recruiters (verification status = APPROVED) are shown
- Candidate-specific features (saved jobs) require optional candidateId parameter

## Database Dependencies

- **JobPosting entity:** Job posting information with status and expiration date
- **Recruiter entity:** Company/recruiter information with verification status
- **JobDescription entity:** Job details including required skills
- **JdSkill entity:** Skill information with must-have flag
- **SavedJob entity:** Saved jobs by candidates (for isSaved flag)

## Status Constants

**StatusJobPosting:**
- **ACTIVE:** Published and visible to public
- **INACTIVE:** Unpublished or draft
- **EXPIRED:** Past expiration date

**StatusRecruiter:**
- **APPROVED:** Verified and allowed to post jobs
- **PENDING:** Awaiting verification
- **REJECTED:** Not approved

## Search and Filter Features

### Keyword Search (Endpoint #1)
- Searches across: job title, description, and address
- Case-insensitive
- Partial match
- Example: "java" matches "Java Developer", "javascript developer", "Java Backend"

### Address Filter (Endpoint #5)
- Filters by company location
- Case-insensitive
- Partial match
- Example: "hanoi" matches "Hanoi, Vietnam", "Hanoi - Hoa Lac"

### Autocomplete Addresses (Endpoint #6)
- Returns distinct addresses only
- Optimized for autocomplete UIs
- Limited results for performance

## Sorting Options

Available sort fields:
- **createAt** (default): Job posting creation date
- **expirationDate**: Job posting expiration date
- **title**: Job title
- **yearsOfExperience**: Required experience
- **salaryRange**: Salary (string sort)

Sort directions:
- **desc** (default): Descending (newest/highest first)
- **asc**: Ascending (oldest/lowest first)

## Pagination Best Practices

1. **Default page size:** 10 items
2. **Maximum recommended:** 50 items per page for performance
3. **Page numbers:** Start from 0 (zero-based)
4. **Metadata:** Use totalPages and totalElements to build pagination UI
5. **First/Last flags:** Use to disable prev/next buttons

## Frontend Integration Examples

### Example 1: Job Listing Page with Search
```javascript
async function loadJobPostings(keyword = '', page = 0, size = 10) {
  const params = new URLSearchParams({
    page: page,
    size: size,
    sortBy: 'createAt',
    sortDir: 'desc'
  });
  
  if (keyword) {
    params.append('keyword', keyword);
  }
  
  const response = await fetch(`/api/job-postings?${params}`);
  const data = await response.json();
  
  displayJobPostings(data.result.content);
  renderPagination(data.result.number, data.result.totalPages);
}

// Search functionality
document.getElementById('searchBtn').addEventListener('click', () => {
  const keyword = document.getElementById('searchInput').value;
  loadJobPostings(keyword, 0, 10);
});
```

### Example 2: Job Detail Page
```javascript
async function loadJobDetail(jobId) {
  try {
    const response = await fetch(`/api/job-postings/${jobId}`);
    const data = await response.json();
    
    if (data.code === 200) {
      displayJobDetail(data.result);
    } else {
      showError('Job posting not found or has expired');
    }
  } catch (error) {
    console.error('Error loading job detail:', error);
  }
}

function displayJobDetail(job) {
  // Display job information
  document.getElementById('jobTitle').textContent = job.title;
  document.getElementById('companyName').textContent = job.recruiterInfo.companyName;
  document.getElementById('salary').textContent = job.salaryRange;
  document.getElementById('location').textContent = job.address;
  
  // Display must-have skills
  const mustHaveSkills = job.skills.filter(s => s.mustToHave);
  displaySkills('mustHave', mustHaveSkills);
  
  // Display nice-to-have skills
  const niceToHaveSkills = job.skills.filter(s => !s.mustToHave);
  displaySkills('niceToHave', niceToHaveSkills);
}
```

### Example 3: Address Autocomplete
```javascript
let debounceTimer;

document.getElementById('addressInput').addEventListener('input', (e) => {
  clearTimeout(debounceTimer);
  
  debounceTimer = setTimeout(async () => {
    const keyword = e.target.value;
    
    if (keyword.length >= 2) {
      const response = await fetch(
        `/api/job-postings/addresses?keyword=${encodeURIComponent(keyword)}&limit=5`
      );
      const data = await response.json();
      
      showAutocompleteResults(data.result);
    }
  }, 300); // Debounce for 300ms
});

function showAutocompleteResults(addresses) {
  const dropdown = document.getElementById('addressDropdown');
  dropdown.innerHTML = addresses
    .map(addr => `<div class="option" onclick="selectAddress('${addr}')">${addr}</div>`)
    .join('');
  dropdown.style.display = 'block';
}
```

### Example 4: Company Directory
```javascript
async function loadCompanies(page = 0, address = '') {
  const params = new URLSearchParams({
    page: page,
    size: 20
  });
  
  if (address) {
    params.append('companyAddress', address);
  }
  
  const response = await fetch(`/api/job-postings/company?${params}`);
  const data = await response.json();
  
  displayCompanies(data.result.content);
}

function displayCompanies(companies) {
  const container = document.getElementById('companiesContainer');
  
  container.innerHTML = companies.map(company => `
    <div class="company-card" onclick="viewCompany(${company.id})">
      <img src="${company.logoUrl}" alt="${company.companyName}">
      <h3>${company.companyName}</h3>
      <p>${company.companyAddress}</p>
      <span class="job-count">${company.jobCount} jobs</span>
    </div>
  `).join('');
}

function viewCompany(recruiterId) {
  window.location.href = `/company/${recruiterId}`;
}
```

### Example 5: Company Profile Page
```javascript
async function loadCompanyProfile(recruiterId) {
  // Load company details
  const detailResponse = await fetch(`/api/job-postings/company/${recruiterId}`);
  const companyData = await detailResponse.json();
  displayCompanyInfo(companyData.result);
  
  // Load company's job postings
  const jobsResponse = await fetch(
    `/api/job-postings/company/list/${recruiterId}?page=0&size=10`
  );
  const jobsData = await jobsResponse.json();
  displayCompanyJobs(jobsData.result.content);
}
```

## Important Notes

- All endpoints return only **approved** and **active** content
- Job postings are automatically filtered by expiration date
- **isSaved** flag requires candidateId parameter (authenticated requests)
- Keyword search is flexible and searches multiple fields
- Address filtering uses partial, case-insensitive matching
- Default sort is by creation date (newest first)
- Public API means no rate limiting by user, but consider implementing on frontend
- For production, consider adding CDN/caching for company logos
- Skills are categorized as must-have (required) vs nice-to-have (optional)

