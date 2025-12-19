package com.fpt.careermate.services.review_services.service;

import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.services.job_services.domain.EmploymentVerification;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.repository.EmploymentVerificationRepo;
import com.fpt.careermate.services.review_services.constant.CandidateQualification;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Service to determine candidate eligibility for leaving company reviews
 * Based on their journey stage with the company (application → interview → employment)
 * 
 * ELIGIBILITY RULES:
 * - APPLICATION_EXPERIENCE: Applied 7+ days ago (any non-withdrawn status)
 * - INTERVIEW_EXPERIENCE: Has interviewedAt timestamp set
 * - WORK_EXPERIENCE: Has been employed (WORKING/TERMINATED/ACCEPTED) for 30+ days
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewEligibilityService {
    
    private static final int MIN_DAYS_FOR_APPLICATION_REVIEW = 7;
    private static final int MIN_DAYS_FOR_WORK_REVIEW = 30;
    
    private final EmploymentVerificationRepo employmentVerificationRepo;
    
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
        
        // HIRED: Must have been employed for 30+ days
        // This is the ONLY way to qualify for WORK_EXPERIENCE reviews
        if (isEmploymentStatus(status)) {
            Integer daysEmployed = getDaysEmployed(jobApply);
            if (daysEmployed != null && daysEmployed >= MIN_DAYS_FOR_WORK_REVIEW) {
                log.debug("Candidate qualifies as HIRED with {} days employed", daysEmployed);
                return CandidateQualification.HIRED;
            }
            // Employed but < 30 days - can still review APPLICATION and possibly INTERVIEW
            // Fall through to check interview status
        }
        
        // INTERVIEWED: Has completed an interview (has interviewedAt timestamp)
        if (jobApply.getInterviewedAt() != null) {
            log.debug("Candidate qualifies as INTERVIEWED");
            return CandidateQualification.INTERVIEWED;
        }
        
        // REJECTED: Can review their application experience
        if (status == StatusJobApply.REJECTED || status == StatusJobApply.BANNED) {
            log.debug("Candidate qualifies as REJECTED");
            return CandidateQualification.REJECTED;
        }
        
        // APPLICANT: Applied 7+ days ago (any active status)
        Integer daysSinceApplication = jobApply.getDaysSinceApplication();
        if (daysSinceApplication != null && daysSinceApplication >= MIN_DAYS_FOR_APPLICATION_REVIEW) {
            if (status != StatusJobApply.WITHDRAWN) {
                log.debug("Candidate qualifies as APPLICANT with {} days since application", daysSinceApplication);
                return CandidateQualification.APPLICANT;
            }
        }
        
        // NOT_ELIGIBLE: Less than 7 days or withdrawn
        log.debug("Candidate NOT_ELIGIBLE - status: {}, daysSince: {}", status, daysSinceApplication);
        return CandidateQualification.NOT_ELIGIBLE;
    }
    
    /**
     * Get days employed from EmploymentVerification, calculating dynamically from start date
     */
    private Integer getDaysEmployed(JobApply jobApply) {
        return employmentVerificationRepo.findByJobApplyId(jobApply.getId())
                .map(EmploymentVerification::calculateDaysEmployed)
                .orElse(null);
    }
    
    /**
     * Check if status indicates active or past employment
     */
    private boolean isEmploymentStatus(StatusJobApply status) {
        return status == StatusJobApply.WORKING 
            || status == StatusJobApply.TERMINATED
            || status == StatusJobApply.ACCEPTED;
    }
    
    /**
     * Get all review types this candidate can submit based on their application state
     * This is MORE GRANULAR than just qualification - it checks specific requirements
     * 
     * @param jobApply The job application to evaluate
     * @return Set of ReviewType they can submit
     */
    public Set<ReviewType> getAllowedReviewTypes(JobApply jobApply) {
        Set<ReviewType> allowedTypes = new HashSet<>();
        
        if (jobApply == null) {
            return allowedTypes;
        }
        
        Integer daysSinceApplication = jobApply.getDaysSinceApplication();
        Integer daysEmployed = getDaysEmployed(jobApply);
        StatusJobApply status = jobApply.getStatus();
        
        // APPLICATION_EXPERIENCE: 7+ days since application, not withdrawn
        if (daysSinceApplication != null && daysSinceApplication >= MIN_DAYS_FOR_APPLICATION_REVIEW
            && status != StatusJobApply.WITHDRAWN) {
            allowedTypes.add(ReviewType.APPLICATION_EXPERIENCE);
        }
        
        // INTERVIEW_EXPERIENCE: Must have been interviewed
        if (jobApply.getInterviewedAt() != null) {
            allowedTypes.add(ReviewType.INTERVIEW_EXPERIENCE);
        }
        
        // WORK_EXPERIENCE: Must have been employed for 30+ days
        // This is STRICT - no exceptions
        if (isEmploymentStatus(status) && daysEmployed != null && daysEmployed >= MIN_DAYS_FOR_WORK_REVIEW) {
            allowedTypes.add(ReviewType.WORK_EXPERIENCE);
        }
        
        log.debug("Allowed review types for jobApply {}: {} (days since app: {}, days employed: {})", 
            jobApply.getId(), allowedTypes, daysSinceApplication, daysEmployed);
        
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
        if (jobApply == null) {
            return "Unable to determine eligibility.";
        }
        
        Set<ReviewType> allowedTypes = getAllowedReviewTypes(jobApply);
        Integer daysEmployed = getDaysEmployed(jobApply);
        Integer daysSinceApp = jobApply.getDaysSinceApplication();
        StatusJobApply status = jobApply.getStatus();
        
        // Check if employed but not 30 days yet
        if (isEmploymentStatus(status)) {
            if (daysEmployed == null || daysEmployed < MIN_DAYS_FOR_WORK_REVIEW) {
                int daysRemaining = MIN_DAYS_FOR_WORK_REVIEW - (daysEmployed != null ? daysEmployed : 0);
                StringBuilder msg = new StringBuilder();
                msg.append(String.format("You've been employed for %d days. ", daysEmployed != null ? daysEmployed : 0));
                msg.append(String.format("Work experience review available in %d day(s). ", daysRemaining));
                if (!allowedTypes.isEmpty()) {
                    msg.append("You can currently review: " + allowedTypes + ".");
                }
                return msg.toString();
            } else {
                return String.format(
                    "You've been employed for %d days. You can review the application process, interview experience, and work culture.",
                    daysEmployed
                );
            }
        }
        
        // Has interviewed
        if (jobApply.getInterviewedAt() != null) {
            return "You've completed an interview. You can review the application and interview process.";
        }
        
        // Check if they can review at all
        if (daysSinceApp != null && daysSinceApp >= MIN_DAYS_FOR_APPLICATION_REVIEW 
            && status != StatusJobApply.WITHDRAWN) {
            return String.format(
                "You applied %d days ago. You can review the company's communication and responsiveness.",
                daysSinceApp
            );
        }
        
        // Not eligible yet
        int daysRemaining = MIN_DAYS_FOR_APPLICATION_REVIEW - (daysSinceApp != null ? daysSinceApp : 0);
        if (daysRemaining > 0) {
            return String.format(
                "You can leave a review in %d day(s) if you don't receive a response.",
                daysRemaining
            );
        } else {
            return "You're not yet eligible to review this company.";
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
