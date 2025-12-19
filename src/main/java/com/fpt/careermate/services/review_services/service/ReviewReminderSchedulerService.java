package com.fpt.careermate.services.review_services.service;

import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.kafka.producer.NotificationProducer;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import com.fpt.careermate.services.review_services.domain.CompanyReview;
import com.fpt.careermate.services.review_services.repository.CompanyReviewRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scheduled service to send review eligibility reminders to candidates
 * 
 * Triggers:
 * - 7 days after application: Remind to review application experience
 * - After interview: Remind to review interview experience  
 * - 30 days of employment: Remind to review work experience
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewReminderSchedulerService {

    JobApplyRepo jobApplyRepo;
    CompanyReviewRepo companyReviewRepo;
    NotificationProducer notificationProducer;
    ReviewEligibilityService reviewEligibilityService;
    
    private static final int APPLICATION_REVIEW_DAYS = 7;
    private static final int WORK_REVIEW_DAYS = 30;

    /**
     * Check for candidates who became eligible for APPLICATION_EXPERIENCE review
     * Runs daily at 10:00 AM
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void sendApplicationReviewReminders() {
        log.info("🔔 Starting application review reminder job at {}", LocalDateTime.now());
        
        try {
            // Find applications from exactly 7 days ago (to avoid duplicate notifications)
            LocalDateTime targetDate = LocalDateTime.now().minusDays(APPLICATION_REVIEW_DAYS);
            LocalDateTime startOfDay = targetDate.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            List<JobApply> eligibleApplications = jobApplyRepo.findByCreateAtBetween(startOfDay, endOfDay);
            
            int sentCount = 0;
            for (JobApply jobApply : eligibleApplications) {
                // Skip withdrawn applications
                if (jobApply.getStatus() == StatusJobApply.WITHDRAWN) {
                    continue;
                }
                
                // Check if already reviewed
                if (hasExistingReview(jobApply, ReviewType.APPLICATION_EXPERIENCE)) {
                    continue;
                }
                
                sendReviewReminder(jobApply, ReviewType.APPLICATION_EXPERIENCE,
                    "Share Your Application Experience",
                    "You applied to %s at %s 7 days ago. Help others by sharing your application experience!",
                    "/candidate/my-reviews");
                sentCount++;
            }
            
            log.info("✅ Application review reminders sent to {} candidates", sentCount);
            
        } catch (Exception e) {
            log.error("❌ Error in application review reminder job", e);
        }
    }

    /**
     * Check for candidates who completed interviews recently
     * Runs daily at 2:00 PM
     */
    @Scheduled(cron = "0 0 14 * * *")
    public void sendInterviewReviewReminders() {
        log.info("🔔 Starting interview review reminder job at {}", LocalDateTime.now());
        
        try {
            // Find interviews completed in the last 24 hours
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            
            List<JobApply> recentInterviews = jobApplyRepo.findByInterviewedAtAfter(yesterday);
            
            int sentCount = 0;
            for (JobApply jobApply : recentInterviews) {
                // Check if already reviewed
                if (hasExistingReview(jobApply, ReviewType.INTERVIEW_EXPERIENCE)) {
                    continue;
                }
                
                sendReviewReminder(jobApply, ReviewType.INTERVIEW_EXPERIENCE,
                    "How Was Your Interview?",
                    "You recently interviewed at %s for the %s position. Share your experience to help other candidates!",
                    "/candidate/my-reviews");
                sentCount++;
            }
            
            log.info("✅ Interview review reminders sent to {} candidates", sentCount);
            
        } catch (Exception e) {
            log.error("❌ Error in interview review reminder job", e);
        }
    }

    /**
     * Check for candidates who reached 30 days of employment
     * Runs daily at 11:00 AM
     */
    @Scheduled(cron = "0 0 11 * * *")
    public void sendWorkExperienceReviewReminders() {
        log.info("🔔 Starting work experience review reminder job at {}", LocalDateTime.now());
        
        try {
            // Find candidates hired exactly 30 days ago
            LocalDateTime targetDate = LocalDateTime.now().minusDays(WORK_REVIEW_DAYS);
            LocalDateTime startOfDay = targetDate.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            List<JobApply> eligibleEmployees = jobApplyRepo.findByHiredAtBetween(startOfDay, endOfDay);
            
            int sentCount = 0;
            for (JobApply jobApply : eligibleEmployees) {
                // Only for WORKING or ACCEPTED status (not TERMINATED)
                if (jobApply.getStatus() != StatusJobApply.WORKING && 
                    jobApply.getStatus() != StatusJobApply.ACCEPTED) {
                    continue;
                }
                
                // Check if already reviewed
                if (hasExistingReview(jobApply, ReviewType.WORK_EXPERIENCE)) {
                    continue;
                }
                
                sendReviewReminder(jobApply, ReviewType.WORK_EXPERIENCE,
                    "Share Your Work Experience",
                    "Congratulations on 30 days at %s as %s! Your work experience review can help others make informed career decisions.",
                    "/candidate/my-reviews");
                sentCount++;
            }
            
            log.info("✅ Work experience review reminders sent to {} candidates", sentCount);
            
        } catch (Exception e) {
            log.error("❌ Error in work experience review reminder job", e);
        }
    }

    /**
     * Check if candidate has already submitted a review of this type
     */
    private boolean hasExistingReview(JobApply jobApply, ReviewType reviewType) {
        List<CompanyReview> existing = companyReviewRepo.findByCandidateCandidateIdAndJobApplyId(
            jobApply.getCandidate().getCandidateId(), 
            jobApply.getId()
        );
        return existing.stream().anyMatch(r -> r.getReviewType() == reviewType);
    }

    /**
     * Send review reminder notification to candidate
     */
    private void sendReviewReminder(JobApply jobApply, ReviewType reviewType, 
                                   String title, String messageTemplate, String redirectUrl) {
        try {
            Candidate candidate = jobApply.getCandidate();
            String companyName = jobApply.getJobPosting().getRecruiter().getCompanyName();
            String jobTitle = jobApply.getJobPosting().getTitle();
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("jobApplyId", jobApply.getId());
            metadata.put("reviewType", reviewType.name());
            metadata.put("companyName", companyName);
            metadata.put("jobTitle", jobTitle);
            metadata.put("redirectUrl", redirectUrl);
            
            String message = String.format(messageTemplate, companyName, jobTitle);
            
            NotificationEvent event = NotificationEvent.builder()
                .eventType("REVIEW_REMINDER")
                .recipientId(String.valueOf(candidate.getCandidateId()))
                .recipientEmail(candidate.getAccount().getEmail())
                .title(title)
                .subject(title + " - " + companyName)
                .message(message + "\n\nClick here to write your review.")
                .category("REVIEW_REMINDER")
                .metadata(metadata)
                .priority(2) // Medium priority
                .build();
            
            notificationProducer.sendNotification("candidate-notifications", event);
            
            log.debug("✅ Review reminder sent - Type: {}, Candidate: {}, Company: {}", 
                reviewType, candidate.getCandidateId(), companyName);
                
        } catch (Exception e) {
            log.error("❌ Failed to send review reminder for job apply: {}", 
                jobApply.getId(), e);
        }
    }

    /**
     * Manual trigger to check and send pending review reminders for a specific candidate
     * Used by the frontend when candidate visits My Reviews page
     */
    public int checkAndSendRemindersForCandidate(Integer candidateId) {
        log.info("Checking pending review reminders for candidate: {}", candidateId);
        
        List<JobApply> applications = jobApplyRepo.findByCandidateId(candidateId);
        int sentCount = 0;
        
        for (JobApply jobApply : applications) {
            Set<ReviewType> allowedTypes = reviewEligibilityService.getAllowedReviewTypes(jobApply);
            
            for (ReviewType type : allowedTypes) {
                if (!hasExistingReview(jobApply, type)) {
                    // Could send notification here, but for now just count
                    sentCount++;
                }
            }
        }
        
        return sentCount;
    }
}
