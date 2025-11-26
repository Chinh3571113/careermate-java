-- ============================================
-- Company Review Feature - Database Migration
-- ============================================
-- Version: 1.0
-- Date: November 25, 2025
-- Description: Adds company review functionality with staged review types
--              and enhanced job application tracking

-- ============================================
-- STEP 1: Alter job_apply table - Add new tracking fields
-- ============================================

ALTER TABLE job_apply
ADD COLUMN interview_scheduled_at TIMESTAMP NULL,
ADD COLUMN interviewed_at TIMESTAMP NULL,
ADD COLUMN hired_at TIMESTAMP NULL,
ADD COLUMN left_at TIMESTAMP NULL,
ADD COLUMN last_contact_at TIMESTAMP NULL,
ADD COLUMN status_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add index on status for faster filtering
CREATE INDEX idx_job_apply_status ON job_apply(status);

-- Add index on candidate + status for eligibility checks
CREATE INDEX idx_job_apply_candidate_status ON job_apply(candidate_id, status);

COMMENT ON COLUMN job_apply.interview_scheduled_at IS 'When interview was scheduled';
COMMENT ON COLUMN job_apply.interviewed_at IS 'When interview actually occurred';
COMMENT ON COLUMN job_apply.hired_at IS 'When candidate was hired/employed';
COMMENT ON COLUMN job_apply.left_at IS 'When employment ended';
COMMENT ON COLUMN job_apply.last_contact_at IS 'Last communication from company';
COMMENT ON COLUMN job_apply.status_changed_at IS 'Last status update timestamp';

-- ============================================
-- STEP 2: Update StatusJobApply enum values
-- ============================================
-- Note: In PostgreSQL, you must use ALTER TYPE to add enum values
-- If using MySQL, the enum is defined in the @Enumerated annotation

-- PostgreSQL:
ALTER TYPE status_job_apply ADD VALUE IF NOT EXISTS 'INTERVIEW_SCHEDULED';
ALTER TYPE status_job_apply ADD VALUE IF NOT EXISTS 'INTERVIEWED';
ALTER TYPE status_job_apply ADD VALUE IF NOT EXISTS 'ACCEPTED';
ALTER TYPE status_job_apply ADD VALUE IF NOT EXISTS 'NO_RESPONSE';
ALTER TYPE status_job_apply ADD VALUE IF NOT EXISTS 'WITHDRAWN';

-- MySQL: The enum is managed by JPA @Enumerated, no SQL needed

-- ============================================
-- STEP 3: Create job_apply_status_history table
-- ============================================

CREATE TABLE job_apply_status_history (
    id SERIAL PRIMARY KEY,
    job_apply_id INT NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by_user_id INT,
    change_reason VARCHAR(500),
    
    CONSTRAINT fk_status_history_job_apply 
        FOREIGN KEY (job_apply_id) 
        REFERENCES job_apply(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_status_history_job_apply ON job_apply_status_history(job_apply_id);
CREATE INDEX idx_status_history_changed_at ON job_apply_status_history(changed_at DESC);

COMMENT ON TABLE job_apply_status_history IS 'Audit trail for all job application status changes';

-- ============================================
-- STEP 4: Create company_review table
-- ============================================

CREATE TABLE company_review (
    id SERIAL PRIMARY KEY,
    candidate_id INT NOT NULL,
    recruiter_id INT NOT NULL,
    job_apply_id INT NOT NULL,
    job_posting_id INT NOT NULL,
    
    -- Review metadata
    review_type VARCHAR(50) NOT NULL,  -- APPLICATION_EXPERIENCE, INTERVIEW_EXPERIENCE, WORK_EXPERIENCE
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, FLAGGED, REMOVED, ARCHIVED
    
    -- Review content
    review_text TEXT NOT NULL,
    overall_rating INT NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    
    -- Aspect-specific ratings (nullable)
    communication_rating INT CHECK (communication_rating BETWEEN 1 AND 5),
    responsiveness_rating INT CHECK (responsiveness_rating BETWEEN 1 AND 5),
    interview_process_rating INT CHECK (interview_process_rating BETWEEN 1 AND 5),
    work_culture_rating INT CHECK (work_culture_rating BETWEEN 1 AND 5),
    management_rating INT CHECK (management_rating BETWEEN 1 AND 5),
    benefits_rating INT CHECK (benefits_rating BETWEEN 1 AND 5),
    work_life_balance_rating INT CHECK (work_life_balance_rating BETWEEN 1 AND 5),
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Privacy & verification
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    is_verified BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- Moderation
    flag_count INT DEFAULT 0,
    removal_reason VARCHAR(500),
    
    -- Weaviate integration
    weaviate_id VARCHAR(255) UNIQUE,
    sentiment_score DECIMAL(3, 2),  -- -1.00 to 1.00
    duplicate_check_hash VARCHAR(255),
    
    -- Foreign keys
    CONSTRAINT fk_review_candidate 
        FOREIGN KEY (candidate_id) 
        REFERENCES candidate(candidate_id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_review_recruiter 
        FOREIGN KEY (recruiter_id) 
        REFERENCES recruiter(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_review_job_apply 
        FOREIGN KEY (job_apply_id) 
        REFERENCES job_apply(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_review_job_posting 
        FOREIGN KEY (job_posting_id) 
        REFERENCES job_posting(id) 
        ON DELETE CASCADE,
    
    -- One review per type per application
    CONSTRAINT uk_one_review_per_type_per_application 
        UNIQUE (job_apply_id, review_type)
);

-- Indexes for performance
CREATE INDEX idx_review_recruiter_status ON company_review(recruiter_id, status);
CREATE INDEX idx_review_candidate ON company_review(candidate_id);
CREATE INDEX idx_review_type ON company_review(review_type);
CREATE INDEX idx_review_created_at ON company_review(created_at DESC);
CREATE INDEX idx_review_rating ON company_review(overall_rating);
CREATE INDEX idx_review_flagged ON company_review(status, flag_count) WHERE status = 'FLAGGED';
CREATE INDEX idx_review_duplicate_hash ON company_review(duplicate_check_hash);

COMMENT ON TABLE company_review IS 'Candidate reviews of companies based on their experience stage';
COMMENT ON COLUMN company_review.review_type IS 'Stage-based review: APPLICATION, INTERVIEW, or WORK_EXPERIENCE';
COMMENT ON COLUMN company_review.is_verified IS 'True if review linked to actual JobApply record';
COMMENT ON COLUMN company_review.weaviate_id IS 'UUID for semantic search and validation';
COMMENT ON COLUMN company_review.sentiment_score IS 'AI-calculated sentiment from review text';

-- ============================================
-- STEP 5: Data Migration - Set initial timestamps
-- ============================================

-- Set status_changed_at to create_at for existing records
UPDATE job_apply
SET status_changed_at = create_at
WHERE status_changed_at IS NULL;

-- Set last_contact_at for non-submitted statuses
UPDATE job_apply
SET last_contact_at = create_at
WHERE status != 'SUBMITTED' AND last_contact_at IS NULL;

-- ============================================
-- STEP 6: Create helpful views (optional)
-- ============================================

-- View: Company review statistics
CREATE OR REPLACE VIEW company_review_stats AS
SELECT 
    r.recruiter_id,
    COUNT(*) AS total_reviews,
    AVG(r.overall_rating) AS avg_overall_rating,
    COUNT(CASE WHEN r.review_type = 'APPLICATION_EXPERIENCE' THEN 1 END) AS application_reviews,
    COUNT(CASE WHEN r.review_type = 'INTERVIEW_EXPERIENCE' THEN 1 END) AS interview_reviews,
    COUNT(CASE WHEN r.review_type = 'WORK_EXPERIENCE' THEN 1 END) AS work_experience_reviews,
    AVG(r.communication_rating) AS avg_communication,
    AVG(r.responsiveness_rating) AS avg_responsiveness,
    AVG(r.interview_process_rating) AS avg_interview_process,
    AVG(r.work_culture_rating) AS avg_work_culture,
    AVG(r.management_rating) AS avg_management,
    AVG(r.benefits_rating) AS avg_benefits,
    AVG(r.work_life_balance_rating) AS avg_work_life_balance
FROM company_review r
WHERE r.status = 'ACTIVE'
GROUP BY r.recruiter_id;

COMMENT ON VIEW company_review_stats IS 'Aggregated review statistics per company';

-- View: Candidate eligibility for reviews
CREATE OR REPLACE VIEW candidate_review_eligibility AS
SELECT 
    ja.id AS job_apply_id,
    ja.candidate_id,
    ja.job_id,
    rec.id AS recruiter_id,
    ja.status,
    ja.create_at,
    ja.interviewed_at,
    ja.hired_at,
    EXTRACT(DAY FROM (CURRENT_TIMESTAMP - ja.create_at)) AS days_since_application,
    CASE 
        WHEN ja.hired_at IS NOT NULL THEN EXTRACT(DAY FROM (COALESCE(ja.left_at, CURRENT_TIMESTAMP) - ja.hired_at))
        ELSE NULL
    END AS days_employed,
    
    -- Eligibility flags
    (EXTRACT(DAY FROM (CURRENT_TIMESTAMP - ja.create_at)) >= 7 
        AND (ja.status IN ('SUBMITTED', 'NO_RESPONSE'))) AS can_review_application,
    (ja.interviewed_at IS NOT NULL) AS can_review_interview,
    (ja.status = 'ACCEPTED' 
        AND EXTRACT(DAY FROM (COALESCE(ja.left_at, CURRENT_TIMESTAMP) - ja.hired_at)) >= 30) AS can_review_work_experience
FROM job_apply ja
JOIN job_posting jp ON ja.job_id = jp.id
JOIN recruiter rec ON jp.recruiter_id = rec.id;

COMMENT ON VIEW candidate_review_eligibility IS 'Shows which review types each candidate is eligible to submit';

-- ============================================
-- STEP 7: Create trigger for automatic timestamp updates
-- ============================================

-- PostgreSQL trigger function
CREATE OR REPLACE FUNCTION update_company_review_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_company_review_updated_at
BEFORE UPDATE ON company_review
FOR EACH ROW
EXECUTE FUNCTION update_company_review_timestamp();

-- ============================================
-- ROLLBACK SCRIPT (if needed)
-- ============================================

/*
-- To rollback this migration:

DROP TRIGGER IF EXISTS tr_company_review_updated_at ON company_review;
DROP FUNCTION IF EXISTS update_company_review_timestamp();
DROP VIEW IF EXISTS candidate_review_eligibility;
DROP VIEW IF EXISTS company_review_stats;
DROP TABLE IF EXISTS company_review CASCADE;
DROP TABLE IF EXISTS job_apply_status_history CASCADE;

ALTER TABLE job_apply
DROP COLUMN IF EXISTS interview_scheduled_at,
DROP COLUMN IF EXISTS interviewed_at,
DROP COLUMN IF EXISTS hired_at,
DROP COLUMN IF EXISTS left_at,
DROP COLUMN IF EXISTS last_contact_at,
DROP COLUMN IF EXISTS status_changed_at;

-- Note: Cannot easily remove enum values in PostgreSQL
-- You would need to recreate the enum type entirely
*/

-- ============================================
-- Verification Queries
-- ============================================

-- Check new columns exist
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'job_apply' 
AND column_name IN ('interviewed_at', 'hired_at', 'status_changed_at');

-- Check new tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_name IN ('company_review', 'job_apply_status_history');

-- Count existing job applications by status
SELECT status, COUNT(*) 
FROM job_apply 
GROUP BY status 
ORDER BY COUNT(*) DESC;

-- ============================================
-- END OF MIGRATION SCRIPT
-- ============================================
