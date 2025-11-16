package com.fpt.careermate.services.health_services.service;

import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.kafka.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Monitors system health and sends critical alerts to admins
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthMonitoringScheduler {

    private final HealthService healthService;
    private final NotificationProducer notificationProducer;
    
    // Track last notification time to avoid spam (component -> last notification time)
    private final Map<String, Instant> lastNotificationTimes = new ConcurrentHashMap<>();
    private static final long NOTIFICATION_COOLDOWN_SECONDS = 300; // 5 minutes

    /**
     * Check critical components health every 2 minutes
     */
    @Scheduled(fixedRate = 120000) // 2 minutes
    public void monitorCriticalComponents() {
        try {
            if (healthService.isCriticalComponentDown()) {
                var healthStatus = healthService.getAggregatedHealth();
                
                // Check each critical component
                healthStatus.components().forEach((name, component) -> {
                    if ("DOWN".equals(component.status()) || "OUT_OF_SERVICE".equals(component.status())) {
                        // Check if we should send notification (respect cooldown)
                        if (shouldSendNotification(name)) {
                            sendCriticalHealthAlert(name, component.message(), component.details());
                            lastNotificationTimes.put(name, Instant.now());
                        }
                    } else {
                        // Component recovered, clear cooldown
                        lastNotificationTimes.remove(name);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error in health monitoring scheduler", e);
        }
    }

    /**
     * Check if we should send notification based on cooldown period
     */
    private boolean shouldSendNotification(String component) {
        Instant lastNotification = lastNotificationTimes.get(component);
        if (lastNotification == null) {
            return true; // Never notified before
        }
        
        long secondsSinceLastNotification = Instant.now().getEpochSecond() - lastNotification.getEpochSecond();
        return secondsSinceLastNotification >= NOTIFICATION_COOLDOWN_SECONDS;
    }

    /**
     * Send critical health alert to all admins
     */
    private void sendCriticalHealthAlert(String component, String message, Map<String, Object> details) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("component", component);
            metadata.put("message", message);
            metadata.put("timestamp", Instant.now().toString());
            metadata.put("severity", "CRITICAL");
            metadata.putAll(details);

            NotificationEvent event = NotificationEvent.builder()
                    .eventId(java.util.UUID.randomUUID().toString())
                    .eventType("SYSTEM_HEALTH_CRITICAL")
                    .timestamp(LocalDateTime.now())
                    .recipientId("admin-team") // Can be enhanced to get all admin IDs
                    .recipientEmail("admin-team") // Set proper email
                    .title(String.format("🚨 Critical: %s is DOWN", component))
                    .message(String.format(
                            "System component '%s' is experiencing issues. Status: DOWN\\n\\nDetails: %s\\n\\nPlease investigate immediately.",
                            component, message))
                    .metadata(metadata)
                    .priority(1) // HIGH priority
                    .build();

            notificationProducer.sendAdminNotification(event);
            log.warn("🚨 Critical health alert sent for component: {}", component);
            
        } catch (Exception e) {
            log.error("Failed to send critical health alert for component: {}", component, e);
        }
    }

    /**
     * Clear stale cooldown entries every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupCooldowns() {
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        lastNotificationTimes.entrySet().removeIf(entry -> entry.getValue().isBefore(oneHourAgo));
        log.debug("Cleaned up health monitoring cooldowns");
    }
}
