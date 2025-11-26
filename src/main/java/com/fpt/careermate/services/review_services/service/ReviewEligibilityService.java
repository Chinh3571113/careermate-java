package com.fpt.careermate.services.review_services.service;

import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.review_services.constant.CandidateQualification;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Service to determine candidate eligibility for leaving company reviews
 * Based on their journey stage with the company (application → interview → employment)
 */
@Service
@Slf4j
public class ReviewEligibilityService {
    
    /**
     * Determine candidate's qualification level based on their job application
     * 
     * @param jobApply The job application to evaluate
     * @return CandidateQualification level
     */
    public CandidateQualification determineQualification(JobApply jobApply) {
        if (jobApply == null) {
            return CandidateQualification.NOT_ELIGIBLE;
        }
        
        StatusJobApply status = jobApply.getStatus();
        
        // Check if hired (30+ days employed)
        if (status == StatusJobApply.ACCEPTED && jobApply.getDaysEmployed() != null && jobApply.getDaysEmployed() >= 30) {
            return CandidateQualification.HIRED;
        }
        
        // Check if interviewed (completed interview)
        if (jobApply.getInterviewedAt() != null) {
            return CandidateQualification.INTERVIEWED;
        }
        
        // Check if rejected at any stage
        if (status == StatusJobApply.REJECTED || status == StatusJobApply.BANNED) {
            return CandidateQualification.REJECTED;
        }
        
        // Check if applicant (7+ days, no response)
        if (jobApply.getDaysSinceApplication() != null && jobApply.getDaysSinceApplication() >= 7) {
            if (status == StatusJobApply.SUBMITTED || status == StatusJobApply.NO_RESPONSE) {
                return CandidateQualification.APPLICANT;
            }
        }
        
        // Not yet eligible
        return CandidateQualification.NOT_ELIGIBLE;
    }
    
    /**
     * Get all review types this candidate can submit based on their qualification
     * 
     * @param jobApply The job application to evaluate
     * @return Set of ReviewType they can submit
     */
    public Set<ReviewType> getAllowedReviewTypes(JobApply jobApply) {
        Set<ReviewType> allowedTypes = new HashSet<>();
        CandidateQualification qualification = determineQualification(jobApply);
        
        switch (qualification) {
            case HIRED:
                // Can review all aspects: application, interview, and work experience
                allowedTypes.add(ReviewType.APPLICATION_EXPERIENCE);
                allowedTypes.add(ReviewType.INTERVIEW_EXPERIENCE);
                allowedTypes.add(ReviewType.WORK_EXPERIENCE);
                break;
                
            case INTERVIEWED:
                // Can review application and interview process
                allowedTypes.add(ReviewType.APPLICATION_EXPERIENCE);
                allowedTypes.add(ReviewType.INTERVIEW_EXPERIENCE);
                break;
                
            case REJECTED:
                // Can review up to stage they reached
                allowedTypes.add(ReviewType.APPLICATION_EXPERIENCE);
                if (jobApply.getInterviewedAt() != null) {
                    allowedTypes.add(ReviewType.INTERVIEW_EXPERIENCE);
                }
                break;
                
            case APPLICANT:
                // Can only review application/communication experience
                allowedTypes.add(ReviewType.APPLICATION_EXPERIENCE);
                break;
                
            case NOT_ELIGIBLE:
                // No review types allowed
                break;
        }
        
        return allowedTypes;
    }
    
    /**
     * Check if candidate can submit a specific review type
     * 
     * @param jobApply The job application to evaluate
     * @param reviewType The review type they want to submit
     * @return true if allowed, false otherwise
     */
    public boolean canSubmitReviewType(JobApply jobApply, ReviewType reviewType) {
        return getAllowedReviewTypes(jobApply).contains(reviewType);
    }
    
    /**
     * Get detailed eligibility explanation for UI display
     * 
     * @param jobApply The job application to evaluate
     * @return Human-readable eligibility message
     */
    public String getEligibilityMessage(JobApply jobApply) {
        CandidateQualification qualification = determineQualification(jobApply);
        
        switch (qualification) {
            case HIRED:
                return String.format(
                    "You've been employed for %d days. You can review the application process, interview experience, and work culture.",
                    jobApply.getDaysEmployed()
                );
                
            case INTERVIEWED:
                return "You've completed an interview. You can review the application and interview process.";
                
            case REJECTED:
                if (jobApply.getInterviewedAt() != null) {
                    return "You can review the application and interview process based on your experience.";
                } else {
                    return "You can review the application and communication experience.";
                }
                
            case APPLICANT:
                return String.format(
                    "You applied %d days ago. You can review the company's communication and responsiveness.",
                    jobApply.getDaysSinceApplication()
                );
                
            case NOT_ELIGIBLE:
                int daysRemaining = 7 - (jobApply.getDaysSinceApplication() != null ? jobApply.getDaysSinceApplication() : 0);
                if (daysRemaining > 0) {
                    return String.format(
                        "You can leave a review in %d day(s) if you don't receive a response.",
                        daysRemaining
                    );
                } else {
                    return "You're not yet eligible to review this company.";
                }
                
            default:
                return "Unable to determine eligibility.";
        }
    }
    
    /**
     * Validate review content is appropriate for the review type
     * 
     * @param reviewType The type of review
     * @param reviewText The review content
     * @return true if content seems appropriate, false if suspicious
     */
    public boolean isReviewContentAppropriate(ReviewType reviewType, String reviewText) {
        if (reviewText == null || reviewText.trim().length() < 20) {
            log.warn("Review content too short: {} characters", reviewText != null ? reviewText.length() : 0);
            return false;
        }
        
        String lowerText = reviewText.toLowerCase();
        
        switch (reviewType) {
            case APPLICATION_EXPERIENCE:
                // Should mention: application, response, communication, email, contact, etc.
                boolean hasAppKeywords = lowerText.contains("appli") || lowerText.contains("response") 
                    || lowerText.contains("communication") || lowerText.contains("email")
                    || lowerText.contains("contact") || lowerText.contains("recruiter");
                
                // Should NOT mention work culture/colleagues if only applied
                boolean hasWorkKeywords = lowerText.contains("coworker") || lowerText.contains("colleague") 
                    || lowerText.contains("manager") || lowerText.contains("team member");
                
                return hasAppKeywords && !hasWorkKeywords;
                
            case INTERVIEW_EXPERIENCE:
                // Should mention: interview, interviewer, questions, process, etc.
                boolean hasInterviewKeywords = lowerText.contains("interview") || lowerText.contains("question")
                    || lowerText.contains("process") || lowerText.contains("round");
                return hasInterviewKeywords;
                
            case WORK_EXPERIENCE:
                // Should mention: work, culture, management, team, benefits, etc.
                boolean hasWorkExpKeywords = lowerText.contains("work") || lowerText.contains("culture")
                    || lowerText.contains("management") || lowerText.contains("team")
                    || lowerText.contains("benefit") || lowerText.contains("salary")
                    || lowerText.contains("environment");
                return hasWorkExpKeywords;
                
            default:
                return true;
        }
    }
    
    /**
     * Get minimum days required for eligibility
     */
    public int getMinimumDaysForApplicationReview() {
        return 7;
    }
    
    /**
     * Get minimum employment days for work experience review
     */
    public int getMinimumDaysForWorkReview() {
        return 30;
    }
}
