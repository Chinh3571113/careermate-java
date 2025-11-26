package com.fpt.careermate.services.review_services.constant;

/**
 * Status of a company review
 */
public enum ReviewStatus {
    /**
     * Review is active and visible
     */
    ACTIVE,
    
    /**
     * Review flagged for moderation (spam, abuse, etc.)
     */
    FLAGGED,
    
    /**
     * Review removed by moderator for policy violation
     */
    REMOVED,
    
    /**
     * Review archived (e.g., company no longer exists)
     */
    ARCHIVED
}
