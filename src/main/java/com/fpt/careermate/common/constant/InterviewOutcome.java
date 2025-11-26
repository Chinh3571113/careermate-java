package com.fpt.careermate.common.constant;

/**
 * Interview outcome after completion
 */
public enum InterviewOutcome {
    /**
     * Candidate passed the interview
     */
    PASS,
    
    /**
     * Candidate failed the interview
     */
    FAIL,
    
    /**
     * Decision pending, still evaluating
     */
    PENDING,
    
    /**
     * Needs another round of interview
     */
    NEEDS_SECOND_ROUND
}
