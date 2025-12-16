# CareerMate AI Components - Complete Algorithm & Calculation Reference

## Executive Summary

This document consolidates all AI-powered recommendation algorithms across the CareerMate platform, providing formulas, calculations, and implementation details for each component.

**Platform Overview:**
- **3 AI Components** across Java and Python backends
- **2 Machine Learning Systems** (Java candidate rec, Python job rec)
- **1 Rule-Based System** (Python role rec)
- **2 Vector Databases** (Weaviate v3 Java, Weaviate v4 Python)
- **2 Embedding Models** (Sentence-transformers 384-dim, Gemini 768-dim)

---

## Table of Contents

1. [Java Candidate Recommendation](#1-java-candidate-recommendation)
2. [Python Job Recommendation](#2-python-job-recommendation)
3. [Python Role/Position Recommendation](#3-python-roleposition-recommendation)
4. [Comparative Analysis](#4-comparative-analysis)
5. [Quick Reference](#5-quick-reference)

---

## 1. Java Candidate Recommendation

### 1.1 Purpose
Recommend candidates to recruiters based on job posting requirements.

### 1.2 Technology Stack
- **Framework:** Spring Boot 3.x
- **Vector DB:** Weaviate v3
- **Embedding:** Sentence-transformers (384 dimensions)
- **Algorithm:** Hybrid (Semantic Search + Skill Matching)

### 1.3 Core Formula

```java
Final Score = (Skill Match Score × 0.5) + (Semantic Similarity × 0.4) × Experience Factor

Where:
- Skill Match Score ∈ [0, 1]
- Semantic Similarity ∈ [0, 1]
- Experience Factor ∈ [0.8, 1.2]
```

### 1.4 Component Calculations

#### A. Skill Match Score

**Formula:**
```java
Skill Match Score = (Direct Matches + Synonym Matches + Hierarchy Bonuses) / Required Skills

Components:
1. Direct Match: Required skill exactly in candidate skills (+1.0)
2. Synonym Match: Synonym of required skill found (+1.0)
3. Hierarchy Bonus: Related parent/child skill (+0.3)
```

**Example Calculation:**
```java
Job Requirements: ["Java", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes"]
Candidate Skills: ["Java", "Spring Framework", "PostgreSQL", "Docker"]

Step 1: Direct Matches
- Java: ✓ (Direct) = +1.0
- PostgreSQL: ✓ (Direct) = +1.0
- Docker: ✓ (Direct) = +1.0
Total Direct: 3.0

Step 2: Synonym Matches
- "Spring Framework" is synonym of "Spring Boot": ✓ = +1.0
Total Synonyms: 1.0

Step 3: Hierarchy Bonuses
- "Docker" and "Kubernetes" are related (containerization): +0.3
Total Hierarchy: 0.3

Step 4: Calculate Score
Total Points = 3.0 + 1.0 + 0.3 = 4.3
Required Skills = 5
Skill Match Score = 4.3 / 5 = 0.86 (86%)
```

**Synonym Mapping (80+ mappings):**
```java
synonymMap = {
    "javascript": ["js", "ecmascript", "es6", "es2015"],
    "typescript": ["ts"],
    "python": ["python3", "py"],
    "java": ["java8", "java11", "java17", "jdk"],
    "spring": ["spring boot", "spring framework", "springboot"],
    "react": ["reactjs", "react.js"],
    "angular": ["angularjs", "angular.js"],
    "vue": ["vuejs", "vue.js"],
    "postgresql": ["postgres", "pg", "psql"],
    "mongodb": ["mongo"],
    "docker": ["containerization"],
    "kubernetes": ["k8s", "kube"],
    // ... 70+ more mappings
}
```

**Hierarchy Relationships:**
```java
hierarchyMap = {
    "Backend Development": {
        children: ["Java", "Spring", "Node.js", "Python", "Django", "Flask"],
        bonus: 0.3
    },
    "Frontend Development": {
        children: ["React", "Vue", "Angular", "JavaScript", "TypeScript"],
        bonus: 0.3
    },
    "Containerization": {
        children: ["Docker", "Kubernetes", "Podman"],
        bonus: 0.3
    },
    "Cloud Platforms": {
        children: ["AWS", "Azure", "GCP"],
        bonus: 0.2
    }
}
```

#### B. Semantic Similarity Score

**Method:** Weaviate Vector Search with Cosine Similarity

```java
// Step 1: Generate embeddings
Job Embedding = sentence_transformers.encode(jobDescription)  // 384-dim vector
Candidate Embedding = sentence_transformers.encode(candidateProfile)  // 384-dim vector

// Step 2: Store in Weaviate
weaviate.data.create(
    class_name="Candidate",
    properties={...},
    vector=Candidate Embedding
)

// Step 3: Search by job embedding
results = weaviate.query.get("Candidate")
    .with_near_vector(Job Embedding)
    .with_limit(100)
    .with_additional(["distance", "certainty"])
    .do()

// Step 4: Extract semantic similarity
Semantic Similarity = results[i].certainty  // Weaviate's certainty ∈ [0, 1]
```

**Weaviate Certainty Calculation:**
```java
distance = 1 - cosine_similarity(job_vector, candidate_vector)
certainty = 1 - (distance / 2)

// Example:
cosine_similarity = 0.85
distance = 1 - 0.85 = 0.15
certainty = 1 - (0.15 / 2) = 0.925 (92.5%)
```

#### C. Experience Factor

**Formula:**
```java
Experience Factor = calculateExperienceFactor(candidateYears, requiredYears)

if (candidateYears >= requiredYears + 2):
    return 1.20  // +20% bonus (significantly overqualified)
elif (candidateYears >= requiredYears):
    return 1.10  // +10% bonus (meets/exceeds requirement)
elif (candidateYears >= requiredYears - 1):
    return 1.00  // No adjustment (close enough)
elif (candidateYears >= requiredYears - 2):
    return 0.95  // -5% penalty (slightly underqualified)
elif (candidateYears >= requiredYears - 3):
    return 0.92  // -8% penalty (moderately underqualified)
else:
    return 0.80  // -20% penalty (significantly underqualified)
```

**Example:**
```java
Job requires: 5 years
Candidate has: 7 years
Experience Factor = 1.20 (overqualified bonus)

Job requires: 5 years
Candidate has: 3 years
Experience Factor = 0.92 (moderately underqualified)
```

### 1.5 Complete Example Calculation

**Scenario:**
```java
Job Posting:
- Title: "Senior Backend Developer"
- Required Skills: ["Java", "Spring Boot", "PostgreSQL", "Docker", "Microservices"]
- Required Experience: 5 years
- Description: "Looking for experienced backend developer..."

Candidate Profile:
- Skills: ["Java", "Spring Framework", "PostgreSQL", "Docker", "AWS"]
- Experience: 6 years
- Bio: "Backend developer specializing in microservices..."
```

**Step 1: Skill Match Score**
```java
Direct Matches:
- Java: ✓ (+1.0)
- PostgreSQL: ✓ (+1.0)
- Docker: ✓ (+1.0)

Synonym Matches:
- Spring Framework ≈ Spring Boot: ✓ (+1.0)

Hierarchy Bonuses:
- AWS related to Cloud/Backend: (+0.3)

Missing:
- Microservices: ✗ (but mentioned in bio, might help semantic)

Total Points = 3.0 + 1.0 + 0.3 = 4.3
Required Skills = 5
Skill Match Score = 4.3 / 5 = 0.86
```

**Step 2: Semantic Similarity**
```java
Job Vector = encode("Looking for experienced backend developer...")
Candidate Vector = encode("Backend developer specializing in microservices...")

Cosine Similarity = 0.82
Weaviate Certainty = 1 - ((1 - 0.82) / 2) = 0.91
Semantic Similarity = 0.91
```

**Step 3: Experience Factor**
```java
Candidate: 6 years
Required: 5 years
Difference: +1 year (exceeds requirement)
Experience Factor = 1.10
```

**Step 4: Final Score**
```java
Final Score = (0.86 × 0.5) + (0.91 × 0.4) × 1.10
            = (0.43 + 0.364) × 1.10
            = 0.794 × 1.10
            = 0.873

Final Score = 87.3% match
```

### 1.6 Response Format

```json
{
    "candidateId": "CAND-12345",
    "matchScore": 87.3,
    "skillMatchScore": 86.0,
    "semanticScore": 91.0,
    "experienceFactor": 1.10,
    "matchedSkills": ["Java", "Spring Framework", "PostgreSQL", "Docker"],
    "missingSkills": ["Microservices"],
    "yearsExperience": 6,
    "requiredExperience": 5,
    "recommendation": "Strong match - Highly recommended"
}
```

---

## 2. Python Job Recommendation

### 2.1 Purpose
Recommend job postings to candidates based on their profile and preferences.

### 2.2 Technology Stack
- **Framework:** Django REST Framework
- **Vector DB:** Weaviate v4
- **Embedding:** Google Gemini (768 dimensions)
- **Algorithm:** Hybrid (Content-Based 80% + Collaborative Filtering 20%)

### 2.3 Core Formula

```python
Final Score = (Content-Based Score × 0.8) + (Collaborative Filtering Score × 0.2)

Where:
- Content-Based Score ∈ [0, 1] (skill + semantic matching)
- CF Score ∈ [0, 1] (similar user preferences)
```

### 2.4 Component Calculations

#### A. Content-Based Score

**Formula:**
```python
Content-Based Score = (Skill Overlap × 0.6) + (Semantic Similarity × 0.4)

Skill Overlap = |User Skills ∩ Job Skills| / |Job Skills|
Semantic Similarity = cosine_similarity(user_vector, job_vector)
```

**Example:**
```python
User Skills: ["Python", "Django", "React", "PostgreSQL"]
Job Skills: ["Python", "Django", "PostgreSQL", "Docker", "AWS"]

Step 1: Skill Overlap
Intersection = {"Python", "Django", "PostgreSQL"} = 3 skills
Job Total = 5 skills
Skill Overlap = 3 / 5 = 0.60

Step 2: Semantic Similarity
User Vector = gemini.embed("5 years Python Django developer...")  # 768-dim
Job Vector = gemini.embed("Senior Backend Python Developer needed...")  # 768-dim

Cosine Similarity = cosine(User Vector, Job Vector) = 0.85
Semantic Similarity = 0.85

Step 3: Content-Based Score
Content Score = (0.60 × 0.6) + (0.85 × 0.4)
              = 0.36 + 0.34
              = 0.70 (70%)
```

#### B. Collaborative Filtering Score

**Method:** User-Based Collaborative Filtering with Weighted Feedback

```python
# Step 1: Find similar users
def find_similar_users(target_user, all_users, top_k=10):
    similarities = []
    
    for user in all_users:
        if user.id == target_user.id:
            continue
        
        # Calculate user similarity based on:
        # 1. Skill overlap
        # 2. Job interaction patterns
        # 3. Preference alignment
        
        skill_sim = jaccard_similarity(target_user.skills, user.skills)
        interaction_sim = interaction_overlap(target_user, user)
        
        total_sim = (skill_sim × 0.7) + (interaction_sim × 0.3)
        similarities.append((user, total_sim))
    
    # Return top K similar users
    return sorted(similarities, key=lambda x: x[1], reverse=True)[:top_k]

# Step 2: Calculate CF score for job
def calculate_cf_score(target_user, job, similar_users):
    weighted_sum = 0
    similarity_sum = 0
    
    for user, similarity in similar_users:
        # Get user's feedback on this job
        feedback = get_feedback(user, job)
        
        if feedback:
            # Feedback weights:
            # Applied: 1.0
            # Saved: 0.8
            # Viewed: 0.5
            # Liked: 0.9
            # Disliked: -0.5
            
            feedback_value = {
                'applied': 1.0,
                'saved': 0.8,
                'liked': 0.9,
                'viewed': 0.5,
                'disliked': -0.5
            }[feedback.action]
            
            weighted_sum += similarity × feedback_value
            similarity_sum += similarity
    
    if similarity_sum == 0:
        return 0.0
    
    # Normalize to [0, 1]
    raw_score = weighted_sum / similarity_sum
    normalized = (raw_score + 1) / 2  # Convert [-1, 1] to [0, 1]
    
    return normalized
```

**Example:**
```python
Target User: User A (skills: ["Python", "Django"])
Job: Backend Python Developer

Similar Users Found:
1. User B (similarity: 0.85)
   - Skills: ["Python", "Django", "Flask"]
   - Feedback on job: Applied (1.0)

2. User C (similarity: 0.72)
   - Skills: ["Python", "FastAPI"]
   - Feedback on job: Saved (0.8)

3. User D (similarity: 0.68)
   - Skills: ["Python", "Django", "React"]
   - Feedback on job: Viewed (0.5)

Calculation:
weighted_sum = (0.85 × 1.0) + (0.72 × 0.8) + (0.68 × 0.5)
             = 0.85 + 0.576 + 0.34
             = 1.766

similarity_sum = 0.85 + 0.72 + 0.68 = 2.25

raw_score = 1.766 / 2.25 = 0.785

normalized = (0.785 + 1) / 2 = 0.893

CF Score = 0.893 (89.3%)
```

#### C. Final Score Calculation

```python
Content-Based Score = 0.70 (from example above)
CF Score = 0.893 (from example above)

Final Score = (0.70 × 0.8) + (0.893 × 0.2)
            = 0.56 + 0.179
            = 0.739

Final Score = 73.9% match
```

### 2.5 Gemini Embedding Process

```python
import google.generativeai as genai

# Step 1: Configure Gemini
genai.configure(api_key=GEMINI_API_KEY)

# Step 2: Prepare text
def prepare_text(job):
    text = f"""
    Title: {job.title}
    Skills: {', '.join(job.skills)}
    Description: {job.description}
    Location: {job.location}
    Salary: {job.salary_range}
    """
    return text

# Step 3: Generate embedding
def generate_embedding(text):
    response = genai.embed_content(
        model="models/embedding-001",
        content=text,
        task_type="retrieval_document"
    )
    return response['embedding']  # 768-dimensional vector

# Step 4: Store in Weaviate v4
def store_in_weaviate(job, embedding):
    collection = client.collections.get("JobPosting")
    
    collection.data.insert(
        properties={
            "title": job.title,
            "skills": job.skills,
            "description": job.description,
            "salary": job.salary
        },
        vector=embedding
    )
```

### 2.6 Complete Example

**Scenario:**
```python
Candidate Profile:
- User ID: USER-789
- Skills: ["Python", "Django", "React", "PostgreSQL"]
- Experience: 4 years
- Preferences: Remote, Backend-focused
- Past Interactions:
  * Applied to 3 Django jobs
  * Saved 2 React jobs
  * Liked 5 backend roles

Job Posting:
- Title: "Senior Full Stack Developer (Python/React)"
- Skills: ["Python", "Django", "React", "PostgreSQL", "AWS"]
- Experience: 3-5 years
- Location: Remote
- Salary: $120K-$150K
```

**Step 1: Content-Based Score**
```python
# Skill Overlap
User Skills: ["Python", "Django", "React", "PostgreSQL"]
Job Skills: ["Python", "Django", "React", "PostgreSQL", "AWS"]

Intersection = 4 skills
Job Total = 5 skills
Skill Overlap = 4 / 5 = 0.80

# Semantic Similarity
User Vector = gemini.embed("4 years Python Django React developer...")
Job Vector = gemini.embed("Senior Full Stack Developer Python React...")

Cosine Similarity = 0.88

# Content Score
Content Score = (0.80 × 0.6) + (0.88 × 0.4)
              = 0.48 + 0.352
              = 0.832
```

**Step 2: Collaborative Filtering**
```python
# Find similar users
Similar Users:
1. User X (sim: 0.82): Applied to this job → +1.0
2. User Y (sim: 0.75): Saved this job → +0.8
3. User Z (sim: 0.70): Viewed this job → +0.5

# Calculate CF Score
weighted_sum = (0.82 × 1.0) + (0.75 × 0.8) + (0.70 × 0.5)
             = 0.82 + 0.60 + 0.35 = 1.77

similarity_sum = 0.82 + 0.75 + 0.70 = 2.27

raw_score = 1.77 / 2.27 = 0.780
normalized = (0.780 + 1) / 2 = 0.890

CF Score = 0.890
```

**Step 3: Final Score**
```python
Final Score = (0.832 × 0.8) + (0.890 × 0.2)
            = 0.6656 + 0.178
            = 0.844

Final Score = 84.4% match
```

**Step 4: Apply Filters**
```python
# Experience check
Job requires: 3-5 years
Candidate has: 4 years
✓ Within range

# Location preference
Job: Remote
Candidate prefers: Remote
✓ Match

# Salary expectation
Job offers: $120K-$150K
Candidate expects: $110K+
✓ Match

All filters passed → Include in recommendations
```

### 2.7 Response Format

```json
{
    "jobId": "JOB-54321",
    "title": "Senior Full Stack Developer (Python/React)",
    "matchScore": 84.4,
    "contentScore": 83.2,
    "cfScore": 89.0,
    "matchedSkills": ["Python", "Django", "React", "PostgreSQL"],
    "missingSkills": ["AWS"],
    "matchReason": "Strong skill match and highly rated by similar users",
    "similarUsersApplied": 3,
    "recommendationStrength": "Highly Recommended"
}
```

---

## 3. Python Role/Position Recommendation

### 3.1 Purpose
Recommend career roles/positions to candidates based on their skills and experience.

### 3.2 Technology Stack
- **Framework:** Django REST Framework
- **Algorithm:** Rule-Based Pattern Matching
- **NLP:** Regex-based extraction
- **ML:** None (pure rule-based)

### 3.3 Core Formula

```python
Final Score = Skill Match Score (no ML, no embeddings)

Confidence = (language_matches × 1.5 + tech_matches) / (total_languages + total_technologies) × 2
```

### 3.4 Detailed Calculation

#### A. Skill Match Score

**Formula:**
```python
def calculate_skill_match(user_skills, role_requirements):
    # Step 1: Normalize skills
    user_normalized = [normalize_skill(s) for s in user_skills]
    
    # Step 2: Count language matches
    language_matches = 0
    for lang in role_requirements['languages']:
        if normalize_skill(lang) in user_normalized:
            language_matches += 1
    
    # Step 3: Count technology matches
    tech_matches = 0
    for tech in role_requirements['technologies']:
        if normalize_skill(tech) in user_normalized:
            tech_matches += 1
    
    # Step 4: Calculate weighted score
    total_requirements = len(role_requirements['languages']) + len(role_requirements['technologies'])
    
    if total_requirements == 0:
        return 0.0
    
    # Languages weighted 1.5x
    weighted_score = (language_matches × 1.5 + tech_matches) / total_requirements × 2
    
    return min(weighted_score, 1.0)  # Cap at 1.0
```

**Normalization:**
```python
def normalize_skill(skill):
    # Convert to lowercase
    normalized = skill.lower().strip()
    
    # Map variants to canonical form
    skill_map = {
        'js': 'javascript',
        'ts': 'typescript',
        'py': 'python',
        'python3': 'python',
        'reactjs': 'react',
        'react.js': 'react',
        'nodejs': 'node.js',
        'postgres': 'postgresql',
        'psql': 'postgresql',
        'k8s': 'kubernetes',
        # ... 140+ mappings
    }
    
    return skill_map.get(normalized, normalized)
```

#### B. Experience Level Mapping

**Formula:**
```python
def get_experience_level(years):
    if 0 <= years < 2:
        return "Junior"
    elif 2 <= years < 5:
        return "Mid-Level"
    elif 5 <= years < 10:
        return "Senior"
    else:  # 10+ years
        return "Lead"

# Position naming
position = f"{experience_level} {role_name}"
```

**Note:** Experience does NOT affect confidence score (limitation!)

### 3.5 Role Templates

**12 Predefined Roles:**

```python
ROLE_PATTERNS = {
    'Backend Developer': {
        'languages': ['Python', 'Java', 'C#', 'Go', 'Ruby', 'PHP', 'Rust'],  # 7 languages
        'technologies': [  # 15 technologies
            'Django', 'Flask', 'FastAPI', 'Spring', 'Node.js', 'Express',
            '.NET', 'SQL', 'PostgreSQL', 'MySQL', 'MongoDB', 'Redis',
            'Docker', 'REST API', 'GraphQL'
        ]
    },
    
    'Frontend Developer': {
        'languages': ['JavaScript', 'TypeScript', 'HTML', 'CSS'],  # 4 languages
        'technologies': [  # 12 technologies
            'React', 'Vue', 'Angular', 'Svelte', 'Next.js', 'Nuxt.js',
            'Tailwind', 'Bootstrap', 'Webpack', 'Vite', 'Sass', 'Redux'
        ]
    },
    
    'Data Scientist': {
        'languages': ['Python', 'R', 'SQL', 'Julia'],  # 4 languages
        'technologies': [  # 12 technologies
            'pandas', 'numpy', 'scikit-learn', 'TensorFlow', 'PyTorch',
            'Jupyter', 'matplotlib', 'seaborn', 'Statsmodels', 'Keras',
            'XGBoost', 'LightGBM'
        ]
    },
    
    # ... 9 more roles
}
```

### 3.6 Complete Example Calculation

**Scenario:**
```python
User Input:
- Skills: ["Python", "Django", "FastAPI", "PostgreSQL", "Docker"]
- Experience: 3 years

Role: Backend Developer
```

**Step 1: Normalize Skills**
```python
User Skills (normalized): ["python", "django", "fastapi", "postgresql", "docker"]
```

**Step 2: Count Matches**
```python
Backend Developer Requirements:
- Languages: ['Python', 'Java', 'C#', 'Go', 'Ruby', 'PHP', 'Rust']  # 7 total
- Technologies: ['Django', 'Flask', 'FastAPI', 'Spring', 'Node.js', 'Express',
                 '.NET', 'SQL', 'PostgreSQL', 'MySQL', 'MongoDB', 'Redis',
                 'Docker', 'REST API', 'GraphQL']  # 15 total

Language Matches:
- Python: ✓ (1 match)

Technology Matches:
- Django: ✓
- FastAPI: ✓
- PostgreSQL: ✓
- Docker: ✓
(4 matches)

Total matches: 1 language + 4 technologies = 5
Total requirements: 7 languages + 15 technologies = 22
```

**Step 3: Calculate Score**
```python
language_matches = 1
tech_matches = 4
total_requirements = 22

weighted_score = (1 × 1.5 + 4) / 22 × 2
               = (1.5 + 4) / 22 × 2
               = 5.5 / 22 × 2
               = 0.25 × 2
               = 0.50

Confidence = min(0.50, 1.0) = 0.50 (50%)
```

**Step 4: Determine Experience Level**
```python
experience_years = 3
→ 2 <= 3 < 5
→ "Mid-Level"

Position = "Mid-Level Backend Developer"
```

**Step 5: Identify Skills**
```python
Matching Skills: ["Python", "Django", "FastAPI", "PostgreSQL", "Docker"]

Missing Skills (suggested):
- Languages: ["Java", "C#", "Go"]
- Technologies: ["Redis", "GraphQL", "REST API"]
```

### 3.7 Comparison: All Roles

**For same candidate:**
```python
Skills: ["Python", "Django", "FastAPI", "PostgreSQL", "Docker"]
Experience: 3 years

Results:
1. Backend Developer: 50% confidence
   - 1 language + 4 tech matches
   - Position: "Mid-Level Backend Developer"

2. Full Stack Developer: 35% confidence
   - 1 language + 2 tech matches (Python, Django, PostgreSQL)
   - Missing: Frontend skills (React, JavaScript)
   - Position: "Mid-Level Full Stack Developer"

3. DevOps Engineer: 30% confidence
   - 1 language + 1 tech match (Python, Docker)
   - Missing: Kubernetes, Terraform, CI/CD tools
   - Position: "Mid-Level DevOps Engineer"

4. Data Scientist: 15% confidence
   - 1 language match (Python)
   - Missing: pandas, numpy, ML libraries
   - Position: "Mid-Level Data Scientist"
```

### 3.8 NLP Extraction Example

**Free-form Text Input:**
```python
text = """
I'm a backend developer with 5 years of experience. I specialize in 
Python, Django, and FastAPI. I've worked extensively with PostgreSQL 
and have experience deploying applications using Docker and AWS.
"""

# Step 1: Extract Skills
extracted_skills = extract_skills(text)
→ ["Python", "Django", "FastAPI", "PostgreSQL", "Docker", "AWS"]

# Step 2: Extract Experience
extracted_experience = extract_experience(text)
→ 5.0 years (from "5 years of experience")

# Step 3: Recommend Roles
recommendations = recommend_roles(extracted_skills, extracted_experience)
```

**Experience Extraction Patterns:**
```python
patterns = [
    r'(\d+)\s*(?:\+)?\s*years?\s+(?:of\s+)?experience',  # "5 years of experience"
    r'experience[:\s]+(\d+)\s*years?',                    # "experience: 5 years"
    r'(\d+)\s*years?\s+(?:in|with|using)',                # "5 years with Python"
    r'worked\s+for\s+(\d+)\s*years?',                     # "worked for 5 years"
    # ... 9 patterns total
]

# Fallback: Infer from seniority keywords
seniority_map = {
    'junior': 1.0,
    'mid-level': 3.5,
    'senior': 7.0,
    'lead': 9.0,
    'principal': 12.0,
    'staff': 10.0
}
```

### 3.9 Response Format

```json
{
    "success": true,
    "input_type": "structured",
    "recommendations": [
        {
            "role": "Backend Developer",
            "position": "Mid-Level Backend Developer",
            "confidence": 0.50,
            "experience_level": "Mid-Level",
            "matching_skills": ["Python", "Django", "FastAPI", "PostgreSQL", "Docker"],
            "suggested_skills": ["Java", "Redis", "GraphQL", "Kubernetes"],
            "description": "Mid-Level backend developer focusing on server-side logic, databases, and APIs"
        }
    ],
    "total_skills": 5,
    "skill_insights": {
        "primary_focus": "backend",
        "is_full_stack": false,
        "has_data_skills": false,
        "has_devops_skills": true
    }
}
```

---

## 4. Comparative Analysis

### 4.1 Algorithm Comparison

| Feature | Java Candidate Rec | Python Job Rec | Python Role Rec |
|---------|-------------------|----------------|-----------------|
| **Purpose** | Candidates → Jobs | Jobs → Candidate | Roles → Candidate |
| **Algorithm Type** | Hybrid ML | Hybrid ML | Rule-Based |
| **Vector DB** | Weaviate v3 | Weaviate v4 | None |
| **Embeddings** | Sentence-transformers (384) | Gemini (768) | None |
| **Skill Matching** | Synonym + Hierarchy | Overlap only | Normalize only |
| **Experience Weight** | ✅ Yes (0.8-1.2) | ❌ No | ❌ No |
| **Learning** | ❌ No | ✅ Yes (CF) | ❌ No |
| **Speed** | ~200-500ms | ~500-1500ms | <50ms |
| **API Costs** | None (local) | Gemini API | None |

### 4.2 Formula Comparison

**Java Candidate:**
```
Score = (Skill Match × 0.5 + Semantic × 0.4) × Experience Factor
      = (0.86 × 0.5 + 0.91 × 0.4) × 1.10
      = 87.3%
```

**Python Job:**
```
Score = Content-Based × 0.8 + CF × 0.2
      = (Skill × 0.6 + Semantic × 0.4) × 0.8 + CF × 0.2
      = (0.80 × 0.6 + 0.88 × 0.4) × 0.8 + 0.89 × 0.2
      = 84.4%
```

**Python Role:**
```
Score = (Language × 1.5 + Tech) / Total × 2
      = (1 × 1.5 + 4) / 22 × 2
      = 50%
```

### 4.3 Scoring Weights

**Java Candidate Recommendation:**
- Skill Matching: **50%**
- Semantic Similarity: **40%**
- Experience Factor: **Multiplier (0.8-1.2)**

**Python Job Recommendation:**
- Content-Based (Skill + Semantic): **80%**
  - Within Content: Skill 60%, Semantic 40%
- Collaborative Filtering: **20%**

**Python Role Recommendation:**
- Skill Matching: **100%**
  - Languages: **1.5x weight**
  - Technologies: **1.0x weight**

### 4.4 Synonym/Normalization Coverage

| System | Coverage | Type |
|--------|----------|------|
| Java Candidate | 80+ mappings | Synonyms + Hierarchy |
| Python Job | None (relies on Gemini) | Gemini understands variants |
| Python Role | 140+ mappings | Normalization only |

### 4.5 Performance Benchmarks

| System | Avg Response | API Calls | Cost/Request |
|--------|--------------|-----------|--------------|
| Java Candidate | 200-500ms | 1 (Weaviate) | ~$0.001 |
| Python Job | 500-1500ms | 2 (Gemini + Weaviate) | ~$0.02 |
| Python Role | <50ms | 0 (local only) | $0 |

---

## 5. Quick Reference

### 5.1 Formula Cheat Sheet

**Java Candidate Recommendation:**
```
Final = (Skill × 0.5 + Semantic × 0.4) × Experience

Where:
Skill = (Direct + Synonyms + Hierarchy) / Required
Semantic = Weaviate Certainty
Experience = 0.8 to 1.2 based on years
```

**Python Job Recommendation:**
```
Final = Content × 0.8 + CF × 0.2

Where:
Content = (Skill Overlap × 0.6) + (Semantic × 0.4)
CF = Weighted similar user feedback
```

**Python Role Recommendation:**
```
Final = (Languages × 1.5 + Tech) / Total × 2

Where:
Languages = Count of matching programming languages
Tech = Count of matching technologies/frameworks
```

### 5.2 Experience Factor Table

**Java System:**
| Years Difference | Factor | Effect |
|-----------------|--------|--------|
| +2 or more | 1.20 | +20% bonus |
| 0 to +2 | 1.10 | +10% bonus |
| -1 | 1.00 | No change |
| -2 | 0.95 | -5% penalty |
| -3 | 0.92 | -8% penalty |
| -4 or less | 0.80 | -20% penalty |

**Python Systems:** No experience weighting (limitation!)

### 5.3 Skill Match Examples

**Scenario: User has ["Python", "Django"]**

| System | Job Requires | Calculation | Score |
|--------|-------------|-------------|-------|
| Java Candidate | ["Python", "Django", "PostgreSQL"] | (2 + 0 + 0) / 3 = 0.67 | 67% |
| Python Job | ["Python", "Django", "PostgreSQL", "Docker"] | 2 / 4 = 0.50 | 50% (before semantic) |
| Python Role | Backend (7 langs + 15 techs) | (1×1.5 + 1) / 22 × 2 = 0.23 | 23% |

### 5.4 When to Use Each System

**Use Java Candidate Recommendation when:**
- Recruiter needs candidates for specific job posting
- Need synonym-aware skill matching
- Experience level is critical factor
- Want fast local processing

**Use Python Job Recommendation when:**
- Candidate browsing job board
- Need personalized recommendations based on behavior
- Collaborative filtering adds value
- Semantic understanding important (Gemini)

**Use Python Role Recommendation when:**
- Career exploration ("What roles am I qualified for?")
- Resume optimization
- Skill gap analysis
- Need instant response (<50ms)
- No API costs acceptable

---

## 6. Implementation Pseudocode

### 6.1 Java Candidate Recommendation

```java
public List<CandidateMatch> recommendCandidates(JobPosting job) {
    // Step 1: Vector search
    List<Candidate> semanticMatches = weaviateClient
        .query()
        .get("Candidate")
        .withNearText(job.getDescription())
        .withLimit(100)
        .execute();
    
    // Step 2: Score each candidate
    List<CandidateMatch> scoredCandidates = new ArrayList<>();
    
    for (Candidate candidate : semanticMatches) {
        // Calculate skill match
        double skillScore = skillMatcher.calculateMatch(
            job.getRequiredSkills(),
            candidate.getSkills()
        );
        
        // Get semantic score from Weaviate
        double semanticScore = candidate.getCertainty();
        
        // Calculate experience factor
        double expFactor = calculateExperienceFactor(
            candidate.getExperienceYears(),
            job.getRequiredYears()
        );
        
        // Final score
        double finalScore = (skillScore * 0.5 + semanticScore * 0.4) * expFactor;
        
        scoredCandidates.add(new CandidateMatch(candidate, finalScore));
    }
    
    // Step 3: Sort and return top N
    return scoredCandidates.stream()
        .sorted(Comparator.comparingDouble(CandidateMatch::getScore).reversed())
        .limit(20)
        .collect(Collectors.toList());
}
```

### 6.2 Python Job Recommendation

```python
def recommend_jobs(user_id, top_n=20):
    # Step 1: Get user profile
    user = User.objects.get(id=user_id)
    user_vector = gemini.embed(user.profile_text)
    
    # Step 2: Vector search for content-based
    jobs = weaviate_client.query.get("JobPosting") \
        .with_near_vector(user_vector) \
        .with_limit(100) \
        .with_additional(["distance"]) \
        .do()
    
    # Step 3: Calculate scores
    scored_jobs = []
    
    for job in jobs:
        # Content-based score
        skill_overlap = calculate_skill_overlap(user.skills, job.skills)
        semantic_sim = 1 - job._additional['distance']
        content_score = (skill_overlap * 0.6) + (semantic_sim * 0.4)
        
        # Collaborative filtering score
        cf_score = calculate_cf_score(user, job)
        
        # Final score
        final_score = (content_score * 0.8) + (cf_score * 0.2)
        
        scored_jobs.append({
            'job': job,
            'score': final_score,
            'content': content_score,
            'cf': cf_score
        })
    
    # Step 4: Sort and filter
    scored_jobs.sort(key=lambda x: x['score'], reverse=True)
    
    return scored_jobs[:top_n]
```

### 6.3 Python Role Recommendation

```python
def recommend_roles(skills, experience_years):
    # Step 1: Normalize skills
    normalized_skills = [normalize_skill(s) for s in skills]
    
    # Step 2: Score each role
    role_scores = []
    
    for role_name, requirements in ROLE_PATTERNS.items():
        # Count matches
        lang_matches = sum(
            1 for lang in requirements['languages']
            if normalize_skill(lang) in normalized_skills
        )
        
        tech_matches = sum(
            1 for tech in requirements['technologies']
            if normalize_skill(tech) in normalized_skills
        )
        
        # Calculate score
        total_req = len(requirements['languages']) + len(requirements['technologies'])
        score = (lang_matches * 1.5 + tech_matches) / total_req * 2
        score = min(score, 1.0)
        
        # Determine experience level
        exp_level = get_experience_level(experience_years)
        position = f"{exp_level} {role_name}"
        
        # Find matching/missing skills
        matching = [s for s in skills if s in requirements['languages'] + requirements['technologies']]
        missing = [s for s in requirements['languages'] + requirements['technologies'] if s not in matching]
        
        role_scores.append({
            'role': role_name,
            'position': position,
            'confidence': score,
            'experience_level': exp_level,
            'matching_skills': matching[:5],
            'suggested_skills': missing[:5]
        })
    
    # Step 3: Sort and return
    role_scores.sort(key=lambda x: x['confidence'], reverse=True)
    return role_scores
```

---

## 7. Improvement Roadmap

### 7.1 Critical Issues

**Java Candidate Recommendation:**
- ⚠️ No feedback learning mechanism
- ⚠️ Synonym map needs expansion (200+ skills)
- ⚠️ Experience factor could be more granular

**Python Job Recommendation:**
- ⚠️ CF cold start problem (new users/jobs)
- ⚠️ Gemini API cost ($0.02 per request)
- ⚠️ No skill synonym handling

**Python Role Recommendation:**
- ⚠️⚠️ No experience weighting in score
- ⚠️⚠️ Flawed scoring formula (arbitrary ×2)
- ⚠️⚠️ Only 140 skills (needs 500+)
- ⚠️ Missing major roles (Game Dev, Blockchain, SRE)

### 7.2 Priority Improvements

**High Priority:**
1. **Python Role:** Fix scoring formula
2. **Python Role:** Add experience weighting
3. **All Systems:** Unified skill database (500+ skills)
4. **Python Job:** Implement caching for Gemini embeddings
5. **Java Candidate:** Add feedback loop

**Medium Priority:**
6. Add 5+ missing roles (Python Role)
7. Implement CF cold start handling (Python Job)
8. Add soft skills recognition (all systems)
9. Calibrate confidence scores (all systems)

**Low Priority:**
10. Add salary insights
11. Add location-based adjustments
12. Implement A/B testing framework

---

**Document Version:** 1.0  
**Last Updated:** December 10, 2024  
**Coverage:** 3 AI Components (Java Candidate, Python Job, Python Role)  
**Total Formulas:** 12+ mathematical calculations  
**Code Examples:** 15+ implementation examples
