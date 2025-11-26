package com.fpt.careermate.services.review_services.constant;

/**
 * Candidate qualification levels that determine what they can review
 */
public enum CandidateQualification {
    /**
     * Applied but no response after 7+ days
     * Can review: APPLICATION_EXPERIENCE only
     */
    APPLICANT,
    
    /**
     * Completed at least one interview
     * Can review: APPLICATION_EXPERIENCE, INTERVIEW_EXPERIENCE
     */
    INTERVIEWED,
    
    /**
     * Employed for 30+ days
     * Can review: All review types (APPLICATION, INTERVIEW, WORK_EXPERIENCE)
     */
    HIRED,
    
    /**
     * Rejected at any stage
     * Can review: Up to the stage they reached (application or interview)
     */
    REJECTED,
    
    /**
     * Not yet eligible to review (applied < 7 days, no status change)
     */
    NOT_ELIGIBLE
}
