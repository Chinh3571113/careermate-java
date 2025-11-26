package com.fpt.careermate.common.constant;

/**
 * Interview status for InterviewSchedule
 */
public enum InterviewStatus {
    /**
     * Interview has been scheduled
     */
    SCHEDULED,
    
    /**
     * Candidate has confirmed attendance
     */
    CONFIRMED,
    
    /**
     * Interview has been completed
     */
    COMPLETED,
    
    /**
     * Interview was cancelled
     */
    CANCELLED,
    
    /**
     * Candidate did not show up
     */
    NO_SHOW,
    
    /**
     * Interview rescheduled to new time
     */
    RESCHEDULED
}
