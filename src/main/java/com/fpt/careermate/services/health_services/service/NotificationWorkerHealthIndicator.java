package com.fpt.careermate.services.health_services.service;

import com.fpt.careermate.services.health_services.domain.NotificationHeartbeat;
import com.fpt.careermate.services.health_services.repository.NotificationHeartbeatRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component("notificationWorker")
@RequiredArgsConstructor
@Slf4j
public class NotificationWorkerHealthIndicator implements HealthIndicator {

    private final NotificationHeartbeatRepo heartbeatRepo;
    private static final long STALE_THRESHOLD_SECONDS = 300; // 5 minutes

    @Override
    public Health health() {
        try {
            var heartbeat = heartbeatRepo.findByName("notification-worker");
            
            if (heartbeat.isEmpty()) {
                log.warn("Notification worker health check: No heartbeat record found");
                return Health.unknown()
                        .withDetail("message", "No heartbeat record found - worker may not have started")
                        .build();
            }
            
            NotificationHeartbeat beat = heartbeat.get();
            Instant last = beat.getLastProcessedAt();
            long secondsSinceLastProcess = Duration.between(last, Instant.now()).getSeconds();
            
            if (secondsSinceLastProcess > STALE_THRESHOLD_SECONDS) {
                log.warn("Notification worker is stale. Last processed: {} seconds ago", secondsSinceLastProcess);
                return Health.down()
                        .withDetail("lastProcessedSeconds", secondsSinceLastProcess)
                        .withDetail("lastProcessedAt", last.toString())
                        .withDetail("messageCount", beat.getMessageCount())
                        .withDetail("errorCount", beat.getErrorCount())
                        .withDetail("message", String.format("Worker stale for %d seconds (threshold: %d)", 
                                secondsSinceLastProcess, STALE_THRESHOLD_SECONDS))
                        .build();
            }
            
            return Health.up()
                    .withDetail("lastProcessedSeconds", secondsSinceLastProcess)
                    .withDetail("lastProcessedAt", last.toString())
                    .withDetail("messageCount", beat.getMessageCount())
                    .withDetail("errorCount", beat.getErrorCount())
                    .withDetail("message", "Worker is active")
                    .build();
                    
        } catch (Exception e) {
            log.error("Notification worker health check failed", e);
            return Health.down(e)
                    .withDetail("message", "Failed to check worker heartbeat")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
