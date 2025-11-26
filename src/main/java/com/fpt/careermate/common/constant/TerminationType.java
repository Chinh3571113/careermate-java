package com.fpt.careermate.common.constant;

/**
 * Enum representing the type of employment termination.
 * Used in bilateral verification system to classify why employment ended.
 * 
 * @since v3.0 - Bilateral Verification & Dispute Resolution
 */
public enum TerminationType {
    RESIGNATION,          // Candidate voluntarily resigned
    FIRED_PERFORMANCE,    // Terminated due to poor performance
    FIRED_MISCONDUCT,     // Terminated due to misconduct/violation
    CONTRACT_END,         // Fixed-term contract naturally expired
    MUTUAL_AGREEMENT,     // Both parties agreed to end employment
    PROBATION_FAILED,     // Failed probation evaluation
    COMPANY_CLOSURE,      // Company shut down/went out of business
    LAYOFF                // Position eliminated (economic reasons)
}
