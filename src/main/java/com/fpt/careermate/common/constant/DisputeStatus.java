package com.fpt.careermate.common.constant;

/**
 * Status of a dispute in the resolution workflow.
 * 
 * @since v3.0 - Bilateral Verification & Dispute Resolution
 */
public enum DisputeStatus {
    OPEN,             // Dispute created, awaiting admin review
    UNDER_REVIEW,     // Admin is reviewing evidence
    RESOLVED,         // Admin made final decision
    WITHDRAWN         // One party withdrew their claim
}
