# Roadmap Controller API Documentation

## Overview
Controller manages roadmap functionalities including importing roadmaps from CSV files, retrieving roadmap details, and recommending roadmaps based on candidate's career goals.

**Base URL:** `/api/roadmap`

**Tag:** Roadmap

---

## API Endpoints

### 1. Add Single Roadmap (Deprecated)

#### Endpoint
```
POST /api/roadmap
```

#### Purpose
Import a single roadmap from a CSV file. **Note: This API is deprecated and should not be used.**

#### Request

**Method:** `POST`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/x-www-form-urlencoded
```

**Request Parameters:**
- **nameRoadmap** (String, required): Name of the roadmap to be imported
- **fileName** (String, required): Path to the CSV file containing roadmap data

**Request Body:** None

**Example Request:**
```bash
POST /api/roadmap?nameRoadmap=Frontend%20Developer&fileName=frontend_developer.csv
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

**404 Not Found - File Not Found:**
```json
{
  "code": 404,
  "message": "FILE_NOT_FOUND",
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

**403 Forbidden - User is not an Admin:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

**500 Internal Server Error - IO Exception:**
```json
{
  "code": 500,
  "message": "IO_EXCEPTION",
  "result": null
}
```

#### Main Logic

1. Read CSV file from the specified path
2. Parse CSV records with columns: topic, subtopic, tags, resources, description
3. Create Roadmap entity with the given name (converted to uppercase)
4. Process each CSV record:
   - If topic exists and subtopic is empty → Create Topic entity
   - If both topic and subtopic exist → Create Subtopic entity and link to corresponding Topic
5. Save Roadmap with all Topics and Subtopics to PostgreSQL database
6. Add Roadmap to Weaviate vector database for semantic search

#### Notes
- Required role: `ROLE_ADMIN`
- **This API is deprecated - use `/import-all` endpoint instead**
- CSV file must have specific format with columns: topic, subtopic, tags, resources, description
- Roadmap name is automatically converted to uppercase
- Data is stored in both PostgreSQL and Weaviate databases

---

### 2. Import All Roadmaps

#### Endpoint
```
POST /api/roadmap/import-all
```

#### Purpose
Automatically import all roadmaps from the `roadmap_data` directory. The roadmap name is extracted from the file name (e.g., `updated_frontend_developer.csv` → `FRONTEND DEVELOPER`).

#### Request

**Method:** `POST`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Request Body:** None

**Example Request:**
```bash
POST /api/roadmap/import-all
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "Successfully imported all roadmaps"
}
```

**Response Fields:**
- **code** (int): HTTP status code
- **message** (String): Result message

#### Error Response

**404 Not Found - Directory Not Found:**
```json
{
  "code": 404,
  "message": "FILE_NOT_FOUND",
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

**403 Forbidden - User is not an Admin:**
```json
{
  "code": 403,
  "message": "Access Denied",
  "result": null
}
```

#### Main Logic

1. Access the roadmap data directory: `../django/agent_core/data/craw/roadmap_data`
2. Scan for all CSV files in the directory
3. For each CSV file:
   - Extract roadmap name from filename (remove "updated_" prefix, replace underscores with spaces, convert to uppercase)
   - Check if roadmap already exists in database
   - If not exists, import the roadmap using the same logic as single import
   - If exists, skip and continue to next file
4. Log success/error for each file processed
5. Continue processing remaining files even if one fails

#### Notes
- Required role: `ROLE_ADMIN`
- Expected directory path: `../django/agent_core/data/craw/roadmap_data`
- Only processes `.csv` files
- Skips roadmaps that already exist in the database
- Processes all files in batch with error handling per file
- Filename format: `updated_<roadmap_name>.csv` or `<roadmap_name>.csv`

---

### 3. Get Roadmap by Name

#### Endpoint
```
GET /api/roadmap
```

#### Purpose
Retrieve detailed information about a specific roadmap including all topics and subtopics.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:**
- **roadmapName** (String, required): Name of the roadmap to retrieve

**Request Body:** None

**Example Request:**
```bash
GET /api/roadmap?roadmapName=Frontend%20Developer
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "result": {
    "name": "FRONTEND DEVELOPER",
    "topics": [
      {
        "id": 1,
        "name": "HTML & CSS",
        "tags": "web, frontend, basics",
        "subtopics": [
          {
            "id": 10,
            "name": "HTML Basics",
            "tags": "html, beginner"
          },
          {
            "id": 11,
            "name": "CSS Flexbox",
            "tags": "css, layout"
          }
        ]
      },
      {
        "id": 2,
        "name": "JavaScript",
        "tags": "programming, frontend",
        "subtopics": [
          {
            "id": 20,
            "name": "ES6 Features",
            "tags": "javascript, modern"
          }
        ]
      }
    ]
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (RoadmapResponse): Roadmap data

**RoadmapResponse:**
- **name** (String): Roadmap name (uppercase)
- **topics** (List<TopicResponse>): List of topics in the roadmap

**TopicResponse:**
- **id** (int): Topic ID
- **name** (String): Topic name
- **tags** (String): Comma-separated tags
- **subtopics** (List<SubtopicResponse>): List of subtopics under this topic

**SubtopicResponse:**
- **id** (int): Subtopic ID
- **name** (String): Subtopic name
- **tags** (String): Comma-separated tags

#### Error Response

**404 Not Found - Roadmap Not Found:**
```json
{
  "code": 404,
  "message": "ROADMAP_NOT_FOUND",
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

1. Convert roadmap name to uppercase and trim whitespace
2. Query PostgreSQL database to find roadmap by name
3. If roadmap not found → Throw `ROADMAP_NOT_FOUND` exception
4. Convert Roadmap entity to RoadmapResponse DTO using mapper
5. Return roadmap with all topics and subtopics in hierarchical structure

#### Notes
- Required role: `ROLE_CANDIDATE`
- Roadmap name is case-insensitive (converted to uppercase internally)
- Returns complete hierarchy: Roadmap → Topics → Subtopics
- Does not include resource URLs in this endpoint (use topic/subtopic detail endpoints)

---

### 4. Get Topic Detail by ID

#### Endpoint
```
GET /api/roadmap/topic/{topicId}
```

#### Purpose
Retrieve detailed information about a specific topic including its description and learning resources.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters:**
- **topicId** (int, required): ID of the topic to retrieve

**Request Body:** None

**Example Request:**
```bash
GET /api/roadmap/topic/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "result": {
    "name": "HTML & CSS",
    "description": "Learn the fundamentals of HTML and CSS to build web pages",
    "resourceResponses": [
      {
        "url": "https://developer.mozilla.org/en-US/docs/Web/HTML"
      },
      {
        "url": "https://www.w3schools.com/html/"
      },
      {
        "url": "https://css-tricks.com/"
      }
    ]
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (TopicDetailResponse): Topic detail data

**TopicDetailResponse:**
- **name** (String): Topic name
- **description** (String): Detailed description of the topic
- **resourceResponses** (List<ResourceResponse>): List of learning resources

**ResourceResponse:**
- **url** (String): URL to learning resource

#### Error Response

**404 Not Found - Topic Not Found:**
```json
{
  "code": 404,
  "message": "TOPIC_NOT_FOUND",
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

1. Query PostgreSQL database to find topic by ID
2. If topic not found → Throw `TOPIC_NOT_FOUND` exception
3. Parse resource URLs from comma-separated string stored in database
4. Create ResourceResponse object for each URL (trimmed)
5. Map topic entity to TopicDetailResponse DTO
6. Set resource list in response
7. Return complete topic details with resources

#### Notes
- Required role: `ROLE_CANDIDATE`
- Resources are stored as comma-separated URLs in the database
- Each URL is trimmed of whitespace before being returned
- Use this endpoint to get learning materials for a specific topic

---

### 5. Get Subtopic Detail by ID

#### Endpoint
```
GET /api/roadmap/subtopic/{subtopicId}
```

#### Purpose
Retrieve detailed information about a specific subtopic including its description and learning resources.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Path Parameters:**
- **subtopicId** (int, required): ID of the subtopic to retrieve

**Request Body:** None

**Example Request:**
```bash
GET /api/roadmap/subtopic/10
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Response

**Success Response (200 OK):**
```json
{
  "code": 200,
  "message": "success",
  "result": {
    "name": "HTML Basics",
    "description": "Understanding HTML elements, attributes, and document structure",
    "resourceResponses": [
      {
        "url": "https://www.w3schools.com/html/html_intro.asp"
      },
      {
        "url": "https://developer.mozilla.org/en-US/docs/Learn/HTML/Introduction_to_HTML"
      }
    ]
  }
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (TopicDetailResponse): Subtopic detail data

**TopicDetailResponse:**
- **name** (String): Subtopic name
- **description** (String): Detailed description of the subtopic
- **resourceResponses** (List<ResourceResponse>): List of learning resources

**ResourceResponse:**
- **url** (String): URL to learning resource

#### Error Response

**404 Not Found - Subtopic Not Found:**
```json
{
  "code": 404,
  "message": "SUBTOPIC_NOT_FOUND",
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

1. Query PostgreSQL database to find subtopic by ID
2. If subtopic not found → Throw `SUBTOPIC_NOT_FOUND` exception
3. Parse resource URLs from comma-separated string stored in database
4. Create ResourceResponse object for each URL (trimmed)
5. Map subtopic entity to TopicDetailResponse DTO
6. Set resource list in response
7. Return complete subtopic details with resources

#### Notes
- Required role: `ROLE_CANDIDATE`
- Resources are stored as comma-separated URLs in the database
- Each URL is trimmed of whitespace before being returned
- Subtopics are more specific topics under main topics
- Same response structure as Topic Detail endpoint

---

### 6. Recommend Roadmap

#### Endpoint
```
GET /api/roadmap/recommendation
```

#### Purpose
Recommend roadmaps based on the candidate's career goal/role using semantic search with Weaviate vector database.

#### Request

**Method:** `GET`

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Query Parameters:**
- **role** (String, required): Candidate's desired career role or job position

**Request Body:** None

**Example Request:**
```bash
GET /api/roadmap/recommendation?role=Frontend%20Developer
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
      "title": "FRONTEND DEVELOPER",
      "similarityScore": 0.95
    },
    {
      "title": "REACT DEVELOPER",
      "similarityScore": 0.87
    },
    {
      "title": "WEB DEVELOPER",
      "similarityScore": 0.82
    }
  ]
}
```

**Response Fields:**

**ApiResponse wrapper:**
- **code** (int): HTTP status code
- **message** (String): Result message
- **result** (List<RecommendedRoadmapResponse>): List of recommended roadmaps

**RecommendedRoadmapResponse:**
- **title** (String): Roadmap name
- **similarityScore** (double): Similarity/certainty score (0.0 to 1.0, where 1.0 is perfect match)

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

**500 Internal Server Error - Weaviate Connection Error:**
```json
{
  "code": 500,
  "message": "Internal Server Error",
  "result": null
}
```

#### Main Logic

1. Convert role input to uppercase and trim whitespace
2. Create NearText semantic search query for Weaviate:
   - Use role as search concept
   - Set certainty threshold at 0.71 (71% minimum similarity)
   - Target custom vector field: `name_vector`
3. Build GraphQL query to retrieve:
   - Roadmap name
   - Similarity certainty score
4. Execute query against Weaviate vector database
5. Parse GraphQL response to extract roadmap data
6. Map results to RecommendedRoadmapResponse DTOs
7. Return top 3 most similar roadmaps with their similarity scores

#### Notes
- Required role: `ROLE_CANDIDATE`
- Uses Weaviate vector database for semantic search (AI-powered)
- Returns maximum of 3 recommendations
- Minimum similarity threshold: 71% (0.71 certainty)
- Higher similarity score = better match for the role
- Recommendations are based on semantic meaning, not exact keyword matching
- Role input is case-insensitive

---

## Common Error Codes

| Code | Message | Description |
|------|---------|-------------|
| 400 | Bad Request | Invalid request parameters |
| 401 | Unauthorized | Missing or invalid token |
| 403 | Forbidden | User does not have access permission (not ADMIN or CANDIDATE) |
| 404 | Not Found | Resource not found (Roadmap/Topic/Subtopic/File) |
| 500 | Internal Server Error | Server error or IO exception |

## Security

- Admin endpoints (`/`, `/import-all`) require authentication and role `ADMIN`
- Candidate endpoints (all GET endpoints) require authentication and role `CANDIDATE`
- Uses JWT Bearer token for authentication
- Security is implemented through `@PreAuthorize` annotations

## Database Dependencies

- **Roadmap entity:** Stores roadmap information
- **Topic entity:** Stores main topics in roadmaps
- **Subtopic entity:** Stores subtopics under topics
- **PostgreSQL:** Relational database for structured data
- **Weaviate:** Vector database for semantic search and recommendations

## File Structure

### CSV File Format
CSV files must contain the following columns:
- **topic**: Main topic name (required for topic rows)
- **subtopic**: Subtopic name (empty for topic rows, filled for subtopic rows)
- **tags**: Comma-separated tags
- **resources**: Comma-separated URLs to learning resources
- **description**: Detailed description of the topic/subtopic

### Example CSV Structure
```csv
topic,subtopic,tags,resources,description
HTML & CSS,,web basics,https://url1.com,Learn HTML and CSS
HTML & CSS,HTML Basics,html,https://url2.com,HTML fundamentals
HTML & CSS,CSS Styling,css,https://url3.com,CSS basics
JavaScript,,programming,https://url4.com,JavaScript programming
```

## Related Services

- `RoadmapImp`: Business logic service
- `RoadmapMapper`: Mapper to convert entities to DTOs
- `WeaviateClient`: Client for vector database operations
- `RoadmapRepo`: Repository for Roadmap entity
- `TopicRepo`: Repository for Topic entity
- `SubtopicRepo`: Repository for Subtopic entity

