package com.fpt.careermate.services.notification_services.service;

import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.kafka.producer.NotificationProducer;
import com.fpt.careermate.services.notification_services.repository.NotificationRepo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Scheduled service for sending daily reminders and announcements to candidates
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationSchedulerService {

    NotificationProducer notificationProducer;
    NotificationRepo notificationRepo;

    private static final List<String> DAILY_TIPS = Arrays.asList(
            "💡 Tip: Update your profile regularly to attract more recruiters!",
            "🎯 Reminder: Complete your profile to increase your chances of getting hired.",
            "📝 Don't forget to check for new job opportunities today!",
            "⭐ Keep your skills section up-to-date with the latest technologies.",
            "🚀 Pro tip: Apply to jobs within 24 hours of posting for better chances!",
            "📊 Review your application status to follow up with recruiters.",
            "💼 Consider expanding your job search criteria for more opportunities.",
            "🌟 A complete profile gets 5x more views from recruiters!");

    /**
     * Send daily reminder to all active candidates
     * Runs every day at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *") // 9:00 AM every day
    public void sendDailyReminders() {
        log.info("📅 Starting daily reminder job at {}", LocalDateTime.now());

        try {
            // Get random tip for the day
            String dailyTip = DAILY_TIPS.get(new Random().nextInt(DAILY_TIPS.size()));

            // In production, fetch active candidate IDs from database
            // For now, send to a test candidate
            List<String> candidateIds = getActiveCandidateIds();

            for (String candidateId : candidateIds) {
                sendDailyReminderToCandidate(candidateId, dailyTip);
            }

            log.info("✅ Daily reminder job completed. Sent to {} candidates", candidateIds.size());

        } catch (Exception e) {
            log.error("❌ Error in daily reminder job", e);
        }
    }

    /**
     * Send daily reminder to a specific candidate
     */
    private void sendDailyReminderToCandidate(String candidateId, String tip) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("reminderType", "daily");
            metadata.put("timestamp", LocalDateTime.now().toString());
            metadata.put("tip", tip);

            String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

            NotificationEvent event = NotificationEvent.builder()
                    .eventType("DAILY_REMINDER")
                    .recipientId(candidateId)
                    .recipientEmail(candidateId + "@example.com") // Fetch real email from DB
                    .title("Daily Career Reminder")
                    .subject("Your Daily Career Tip - " + formattedDate)
                    .message(String.format(
                            "Good morning! 🌅\n\n" +
                                    "%s\n\n" +
                                    "Make today count in your job search journey!\n\n" +
                                    "Quick actions:\n" +
                                    "• Check new job postings\n" +
                                    "• Update your application status\n" +
                                    "• Review messages from recruiters\n\n" +
                                    "Best of luck!\n" +
                                    "CareerMate Team",
                            tip))
                    .category("DAILY_REMINDER")
                    .metadata(metadata)
                    .priority(3) // Low priority
                    .build();

            notificationProducer.sendNotification("candidate-notifications", event);
            log.debug("✅ Daily reminder sent to candidate: {}", candidateId);

        } catch (Exception e) {
            log.error("❌ Failed to send daily reminder to candidate: {}", candidateId, e);
        }
    }

    /**
     * Send weekly announcement to all candidates
     * Runs every Monday at 10:00 AM
     */
    @Scheduled(cron = "0 0 10 * * MON") // 10:00 AM every Monday
    public void sendWeeklyAnnouncement() {
        log.info("📢 Starting weekly announcement job at {}", LocalDateTime.now());

        try {
            List<String> candidateIds = getActiveCandidateIds();

            for (String candidateId : candidateIds) {
                sendWeeklyAnnouncementToCandidate(candidateId);
            }

            log.info("✅ Weekly announcement job completed. Sent to {} candidates", candidateIds.size());

        } catch (Exception e) {
            log.error("❌ Error in weekly announcement job", e);
        }
    }

    /**
     * Send weekly announcement to a specific candidate
     */
    private void sendWeeklyAnnouncementToCandidate(String candidateId) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("announcementType", "weekly");
            metadata.put("timestamp", LocalDateTime.now().toString());

            String weekNumber = LocalDateTime.now().format(DateTimeFormatter.ofPattern("w"));

            NotificationEvent event = NotificationEvent.builder()
                    .eventType("ANNOUNCEMENT")
                    .recipientId(candidateId)
                    .recipientEmail(candidateId + "@example.com") // Fetch real email from DB
                    .title("Weekly Career Update")
                    .subject("This Week's Job Market Insights - Week " + weekNumber)
                    .message(
                            "🎯 Weekly Career Update\n\n" +
                                    "Here's what's happening this week in the job market:\n\n" +
                                    "📈 Trending Skills:\n" +
                                    "• AI/Machine Learning\n" +
                                    "• Cloud Computing (AWS, Azure)\n" +
                                    "• Full-stack Development\n\n" +
                                    "🔥 Hot Industries:\n" +
                                    "• Technology & IT\n" +
                                    "• Healthcare\n" +
                                    "• Finance & Banking\n\n" +
                                    "💡 This Week's Focus:\n" +
                                    "Consider highlighting these skills in your applications for better visibility!\n\n"
                                    +
                                    "📊 Your Activity:\n" +
                                    "• Check your application response rates\n" +
                                    "• Update skills based on market trends\n" +
                                    "• Connect with new recruiters\n\n" +
                                    "Keep pushing forward!\n" +
                                    "CareerMate Team")
                    .category("ANNOUNCEMENT")
                    .metadata(metadata)
                    .priority(2) // Medium priority
                    .build();

            notificationProducer.sendNotification("candidate-notifications", event);
            log.debug("✅ Weekly announcement sent to candidate: {}", candidateId);

        } catch (Exception e) {
            log.error("❌ Failed to send weekly announcement to candidate: {}", candidateId, e);
        }
    }

    /**
     * Send application deadline reminder
     * Runs every day at 6:00 PM
     */
    @Scheduled(cron = "0 0 18 * * *") // 6:00 PM every day
    public void sendApplicationDeadlineReminders() {
        log.info("⏰ Starting application deadline reminder job at {}", LocalDateTime.now());

        try {
            // In production: Query database for candidates with pending applications
            // nearing deadline
            // For now, this is a placeholder

            List<String> candidatesWithDeadlines = getCandidatesWithUpcomingDeadlines();

            for (String candidateId : candidatesWithDeadlines) {
                sendDeadlineReminderToCandidate(candidateId);
            }

            log.info("✅ Deadline reminder job completed. Sent to {} candidates", candidatesWithDeadlines.size());

        } catch (Exception e) {
            log.error("❌ Error in deadline reminder job", e);
        }
    }

    /**
     * Send application deadline reminder to candidate
     */
    private void sendDeadlineReminderToCandidate(String candidateId) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("reminderType", "deadline");
            metadata.put("timestamp", LocalDateTime.now().toString());
            metadata.put("urgency", "high");

            NotificationEvent event = NotificationEvent.builder()
                    .eventType("APPLICATION_DEADLINE_REMINDER")
                    .recipientId(candidateId)
                    .recipientEmail(candidateId + "@example.com")
                    .title("⚠️ Application Deadline Approaching")
                    .subject("Don't Miss Out - Application Deadlines This Week")
                    .message(
                            "⚠️ Deadline Alert!\n\n" +
                                    "You have job applications with approaching deadlines:\n\n" +
                                    "📋 Jobs closing soon:\n" +
                                    "• Software Engineer at Tech Corp (2 days left)\n" +
                                    "• Product Manager at StartupXYZ (3 days left)\n\n" +
                                    "⏰ Action Required:\n" +
                                    "Review and complete your pending applications before the deadline.\n\n" +
                                    "💡 Quick Tips:\n" +
                                    "• Double-check your resume\n" +
                                    "• Customize your cover letter\n" +
                                    "• Prepare for potential interviews\n\n" +
                                    "Don't let great opportunities slip away!\n" +
                                    "CareerMate Team")
                    .category("DEADLINE_REMINDER")
                    .metadata(metadata)
                    .priority(1) // High priority
                    .build();

            notificationProducer.sendNotification("candidate-notifications", event);
            log.debug("✅ Deadline reminder sent to candidate: {}", candidateId);

        } catch (Exception e) {
            log.error("❌ Failed to send deadline reminder to candidate: {}", candidateId, e);
        }
    }

    /**
     * Clean up old read notifications
     * Runs every Sunday at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN") // 2:00 AM every Sunday
    public void cleanupOldNotifications() {
        log.info("🧹 Starting notification cleanup job at {}", LocalDateTime.now());

        try {
            // Delete notifications older than 30 days that have been read
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int deletedCount = notificationRepo.deleteOldReadNotifications(cutoffDate);

            log.info("✅ Cleanup job completed. Deleted {} old notifications", deletedCount);

        } catch (Exception e) {
            log.error("❌ Error in cleanup job", e);
        }
    }

    /**
     * Get active candidate IDs from database
     * In production, this should query the candidate table
     */
    private List<String> getActiveCandidateIds() {
        // TODO: Implement actual database query
        // return candidateRepo.findAllActiveIds();

        // For testing, return sample IDs
        return Arrays.asList("candidate1", "candidate2", "candidate3");
    }

    /**
     * Get candidates with upcoming deadlines
     * In production, this should query applications with deadlines in next 3 days
     */
    private List<String> getCandidatesWithUpcomingDeadlines() {
        // TODO: Implement actual database query
        // return applicationRepo.findCandidatesWithDeadlinesInNextDays(3);

        // For testing, return sample IDs
        return Arrays.asList("candidate1");
    }
}
