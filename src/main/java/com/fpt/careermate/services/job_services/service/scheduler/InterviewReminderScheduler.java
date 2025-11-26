package com.fpt.careermate.services.job_services.service.scheduler;

import com.fpt.careermate.services.job_services.service.InterviewScheduleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Scheduled service for interview reminder notifications.
 * Sends 24-hour and 2-hour reminders to candidates.
 * 
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class InterviewReminderScheduler {

    InterviewScheduleService interviewScheduleService;

    /**
     * Send 24-hour interview reminders.
     * Runs every hour to check for interviews 24 hours away.
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void send24HourReminders() {
        log.info("Starting 24-hour interview reminder job at {}", LocalDateTime.now());

        try {
            Integer remindersSent = interviewScheduleService.send24HourReminders();
            
            if (remindersSent > 0) {
                log.info("Sent {} 24-hour interview reminders", remindersSent);
            } else {
                log.debug("No 24-hour reminders needed at this time");
            }
            
        } catch (Exception e) {
            log.error("Error sending 24-hour interview reminders", e);
        }
    }

    /**
     * Send 2-hour interview reminders.
     * Runs every 30 minutes to check for interviews 2 hours away.
     */
    @Scheduled(cron = "0 */30 * * * *") // Every 30 minutes
    public void send2HourReminders() {
        log.info("Starting 2-hour interview reminder job at {}", LocalDateTime.now());

        try {
            Integer remindersSent = interviewScheduleService.send2HourReminders();
            
            if (remindersSent > 0) {
                log.info("Sent {} 2-hour interview reminders", remindersSent);
            } else {
                log.debug("No 2-hour reminders needed at this time");
            }
            
        } catch (Exception e) {
            log.error("Error sending 2-hour interview reminders", e);
        }
    }
}
