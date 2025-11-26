package com.fpt.careermate.services.review_services.constant;

/**
 * Types of reviews candidates can leave based on their experience stage with the company
 */
public enum ReviewType {
    /**
     * Review of application/communication experience
     * Eligibility: Applied 7+ days ago, no response OR rejected at application stage
     */
    APPLICATION_EXPERIENCE,
    
    /**
     * Review of interview process
     * Eligibility: Completed at least one interview (any outcome)
     */
    INTERVIEW_EXPERIENCE,
    
    /**
     * Review of actual work experience
     * Eligibility: Employed for 30+ days (current or former employee)
     */
    WORK_EXPERIENCE
}
