package com.fpt.careermate.common.constant;

public enum StatusJobApply {
    SUBMITTED,           // Initial application submitted
    REVIEWING,           // Application under review by recruiter
    INTERVIEW_SCHEDULED, // Interview has been scheduled
    INTERVIEWED,         // Interview completed (any outcome)
    APPROVED,            // Approved for next stage (not final hire)
    ACCEPTED,            // Candidate hired/employed (legacy - use WORKING)
    WORKING,             // Candidate currently employed (v3.0)
    PROBATION_FAILED,    // Failed probation period (v3.0)
    TERMINATED,          // Employment ended for any reason (v3.0)
    REJECTED,            // Application rejected
    BANNED,              // Candidate banned from applying
    NO_RESPONSE,         // Company did not respond after 7+ days
    WITHDRAWN            // Candidate withdrew their application
}
