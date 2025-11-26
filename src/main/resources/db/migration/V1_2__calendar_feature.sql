-- V1.2 Calendar Feature Migration
-- Creates tables for recruiter working hours configuration and time-off management
-- Supports interview scheduling with real-world business rules

-- =====================================================
-- Table: recruiter_working_hours
-- Purpose: Store recruiter's weekly schedule configuration
-- =====================================================
CREATE TABLE recruiter_working_hours (
    working_hours_id INT AUTO_INCREMENT PRIMARY KEY,
    recruiter_id INT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    is_working_day BOOLEAN NOT NULL DEFAULT TRUE,
    start_time TIME NULL,
    end_time TIME NULL,
    lunch_break_start TIME NULL,
    lunch_break_end TIME NULL,
    buffer_minutes_between_interviews INT NOT NULL DEFAULT 15,
    max_interviews_per_day INT NOT NULL DEFAULT 8,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_working_hours_recruiter 
        FOREIGN KEY (recruiter_id) 
        REFERENCES recruiter(recruiter_id) 
        ON DELETE CASCADE,
    
    -- Unique constraint: One configuration per recruiter per day
    CONSTRAINT uk_recruiter_day 
        UNIQUE KEY (recruiter_id, day_of_week),
    
    -- Check constraint: Valid day of week
    CONSTRAINT chk_day_of_week 
        CHECK (day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    
    -- Check constraint: Buffer time range
    CONSTRAINT chk_buffer_minutes 
        CHECK (buffer_minutes_between_interviews >= 0 AND buffer_minutes_between_interviews <= 60),
    
    -- Check constraint: Max interviews range
    CONSTRAINT chk_max_interviews 
        CHECK (max_interviews_per_day >= 1 AND max_interviews_per_day <= 20)
);

-- Index for faster queries by recruiter
CREATE INDEX idx_working_hours_recruiter ON recruiter_working_hours(recruiter_id);

-- Index for queries filtering by working days
CREATE INDEX idx_working_hours_is_working ON recruiter_working_hours(recruiter_id, is_working_day);

-- =====================================================
-- Table: recruiter_time_off
-- Purpose: Track recruiter unavailability periods
-- =====================================================
CREATE TABLE recruiter_time_off (
    time_off_id INT AUTO_INCREMENT PRIMARY KEY,
    recruiter_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    time_off_type VARCHAR(30) NOT NULL,
    reason VARCHAR(500) NULL,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE,
    approved_by_admin_id INT NULL,
    approved_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_time_off_recruiter 
        FOREIGN KEY (recruiter_id) 
        REFERENCES recruiter(recruiter_id) 
        ON DELETE CASCADE,
    
    -- Check constraint: Valid date range
    CONSTRAINT chk_time_off_dates 
        CHECK (end_date >= start_date),
    
    -- Check constraint: Valid time-off type
    CONSTRAINT chk_time_off_type 
        CHECK (time_off_type IN ('VACATION', 'SICK_LEAVE', 'PERSONAL_DAY', 'PUBLIC_HOLIDAY', 'COMPANY_EVENT', 'TRAINING', 'OTHER'))
);

-- Index for faster queries by recruiter
CREATE INDEX idx_time_off_recruiter ON recruiter_time_off(recruiter_id);

-- Index for date range queries (most common operation)
CREATE INDEX idx_time_off_dates ON recruiter_time_off(recruiter_id, start_date, end_date);

-- Index for approval queries
CREATE INDEX idx_time_off_approval ON recruiter_time_off(is_approved);

-- Index for pending approvals (admin queries)
CREATE INDEX idx_time_off_pending ON recruiter_time_off(is_approved, created_at);

-- =====================================================
-- Sample Data (Optional - for development/testing)
-- =====================================================
-- Uncomment below to insert sample working hours for testing

-- INSERT INTO recruiter_working_hours (recruiter_id, day_of_week, is_working_day, start_time, end_time, lunch_break_start, lunch_break_end, buffer_minutes_between_interviews, max_interviews_per_day)
-- VALUES 
--     -- Monday to Thursday: 9 AM - 5 PM with 1-hour lunch
--     (1, 'MONDAY', TRUE, '09:00:00', '17:00:00', '12:00:00', '13:00:00', 15, 8),
--     (1, 'TUESDAY', TRUE, '09:00:00', '17:00:00', '12:00:00', '13:00:00', 15, 8),
--     (1, 'WEDNESDAY', TRUE, '09:00:00', '17:00:00', '12:00:00', '13:00:00', 15, 8),
--     (1, 'THURSDAY', TRUE, '09:00:00', '17:00:00', '12:00:00', '13:00:00', 15, 8),
--     -- Friday: Shorter day (9 AM - 3 PM)
--     (1, 'FRIDAY', TRUE, '09:00:00', '15:00:00', '12:00:00', '13:00:00', 15, 5),
--     -- Weekend: Off
--     (1, 'SATURDAY', FALSE, NULL, NULL, NULL, NULL, 15, 0),
--     (1, 'SUNDAY', FALSE, NULL, NULL, NULL, NULL, 15, 0);

-- Sample time-off record (Christmas holidays)
-- INSERT INTO recruiter_time_off (recruiter_id, start_date, end_date, time_off_type, reason, is_approved, approved_by_admin_id, approved_at)
-- VALUES (1, '2024-12-23', '2024-12-27', 'VACATION', 'Year-end holidays', TRUE, 100, '2024-12-01 10:00:00');
