-- =====================================================
-- Database Migration Script for Interview Scheduling 
-- and Employment Contract Management System
-- Version: 1.0
-- Date: 2024
-- =====================================================

-- =====================================================
-- SECTION 1: Interview Scheduling Tables
-- =====================================================

-- Create interview_schedule table
CREATE TABLE IF NOT EXISTS interview_schedule (
    interview_schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_apply_id INT NOT NULL UNIQUE,
    interview_round INT DEFAULT 1,
    scheduled_date DATETIME NOT NULL,
    duration_minutes INT DEFAULT 60,
    interview_type VARCHAR(30) NOT NULL,
    location VARCHAR(500),
    interviewer_name VARCHAR(200),
    interviewer_email VARCHAR(200),
    interviewer_phone VARCHAR(20),
    preparation_notes VARCHAR(2000),
    meeting_link VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    candidate_confirmed BOOLEAN DEFAULT FALSE,
    candidate_confirmed_at DATETIME,
    reminder_sent_24h BOOLEAN DEFAULT FALSE,
    reminder_sent_2h BOOLEAN DEFAULT FALSE,
    interview_completed_at DATETIME,
    interviewer_notes VARCHAR(2000),
    outcome VARCHAR(30),
    created_by_recruiter_id INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_interview_job_apply FOREIGN KEY (job_apply_id) 
        REFERENCES job_apply(job_apply_id) ON DELETE CASCADE,
    CONSTRAINT fk_interview_recruiter FOREIGN KEY (created_by_recruiter_id) 
        REFERENCES recruiter(recruiter_id) ON DELETE SET NULL,
    
    INDEX idx_interview_schedule_date (scheduled_date),
    INDEX idx_interview_schedule_status (status),
    INDEX idx_interview_job_apply (job_apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create interview_reschedule_request table
CREATE TABLE IF NOT EXISTS interview_reschedule_request (
    reschedule_request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_schedule_id BIGINT NOT NULL,
    original_date DATETIME NOT NULL,
    new_requested_date DATETIME NOT NULL,
    reason VARCHAR(1000),
    requested_by VARCHAR(20) NOT NULL,
    requires_consent BOOLEAN NOT NULL DEFAULT TRUE,
    consent_given BOOLEAN DEFAULT FALSE,
    consent_given_at DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_CONSENT',
    response_notes VARCHAR(1000),
    expires_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_reschedule_interview FOREIGN KEY (interview_schedule_id) 
        REFERENCES interview_schedule(interview_schedule_id) ON DELETE CASCADE,
    
    INDEX idx_reschedule_status (status),
    INDEX idx_reschedule_interview (interview_schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- SECTION 2: Add Comments for Documentation
-- =====================================================

-- Interview Schedule table comments
ALTER TABLE interview_schedule COMMENT = 'Stores interview scheduling information for job applications';
ALTER TABLE interview_schedule MODIFY COLUMN interview_schedule_id BIGINT AUTO_INCREMENT COMMENT 'Primary key';
ALTER TABLE interview_schedule MODIFY COLUMN job_apply_id INT NOT NULL COMMENT 'Foreign key to job_apply table (one-to-one)';
ALTER TABLE interview_schedule MODIFY COLUMN interview_round INT DEFAULT 1 COMMENT 'Interview round number (1, 2, 3, etc.)';
ALTER TABLE interview_schedule MODIFY COLUMN scheduled_date DATETIME NOT NULL COMMENT 'Date and time of interview';
ALTER TABLE interview_schedule MODIFY COLUMN duration_minutes INT DEFAULT 60 COMMENT 'Duration of interview in minutes';
ALTER TABLE interview_schedule MODIFY COLUMN interview_type VARCHAR(30) NOT NULL COMMENT 'Type: IN_PERSON, VIDEO_CALL, PHONE, ONLINE_ASSESSMENT';
ALTER TABLE interview_schedule MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT 'Status: SCHEDULED, CONFIRMED, COMPLETED, CANCELLED, NO_SHOW, RESCHEDULED';
ALTER TABLE interview_schedule MODIFY COLUMN outcome VARCHAR(30) COMMENT 'Outcome: PASS, FAIL, PENDING, NEEDS_SECOND_ROUND';

-- Interview Reschedule Request table comments
ALTER TABLE interview_reschedule_request COMMENT = 'Stores reschedule requests for interviews';
ALTER TABLE interview_reschedule_request MODIFY COLUMN requested_by VARCHAR(20) NOT NULL COMMENT 'Who requested: RECRUITER or CANDIDATE';
ALTER TABLE interview_reschedule_request MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING_CONSENT' COMMENT 'Status: PENDING_CONSENT, ACCEPTED, REJECTED, EXPIRED';

-- =====================================================
-- SECTION 3: Sample Data (Optional - for testing)
-- =====================================================

-- Note: Uncomment below to insert sample data for testing
/*
-- Sample interview schedule
INSERT INTO interview_schedule (
    job_apply_id, interview_round, scheduled_date, duration_minutes, 
    interview_type, location, interviewer_name, interviewer_email,
    status, created_by_recruiter_id
) VALUES (
    1, 1, DATE_ADD(NOW(), INTERVAL 3 DAY), 60,
    'VIDEO_CALL', 'https://zoom.us/j/123456789', 
    'John Smith', 'john.smith@company.com',
    'SCHEDULED', 1
);
*/

-- =====================================================
-- SECTION 4: Verification Queries
-- =====================================================

-- Verify tables created successfully
SELECT 
    TABLE_NAME, 
    TABLE_ROWS,
    CREATE_TIME,
    TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME IN ('interview_schedule', 'interview_reschedule_request')
ORDER BY TABLE_NAME;

-- Verify indexes created
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    INDEX_TYPE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME IN ('interview_schedule', 'interview_reschedule_request')
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- Verify foreign keys created
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME IN ('interview_schedule', 'interview_reschedule_request')
AND REFERENCED_TABLE_NAME IS NOT NULL;

-- =====================================================
-- END OF MIGRATION SCRIPT
-- =====================================================
