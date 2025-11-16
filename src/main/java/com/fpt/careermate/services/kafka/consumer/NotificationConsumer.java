package com.fpt.careermate.services.kafka.consumer;

import com.fpt.careermate.config.KafkaConfig;
import com.fpt.careermate.services.health_services.domain.NotificationHeartbeat;
import com.fpt.careermate.services.health_services.repository.NotificationHeartbeatRepo;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.notification_services.domain.Notification;
import com.fpt.careermate.services.notification_services.repository.NotificationRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Kafka consumer service for processing notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationHeartbeatRepo heartbeatRepo;
    private final NotificationRepo notificationRepo;

    /**
     * Consumer for admin notifications
     */
    @KafkaListener(topics = KafkaConfig.ADMIN_NOTIFICATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeAdminNotification(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("📨 Received ADMIN notification | Topic: {} | Partition: {} | Offset: {} | EventId: {}",
                    KafkaConfig.ADMIN_NOTIFICATION_TOPIC, partition, offset, event.getEventId());

            // Process the notification
            processNotification(event);

            // Manually commit offset after successful processing
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed ADMIN notification: {}", event.getEventId());

            // Update heartbeat
            updateHeartbeat(true, null);

        } catch (Exception e) {
            log.error("❌ Error processing ADMIN notification: {} | Error: {}",
                    event.getEventId(), e.getMessage(), e);
            // Update heartbeat with error
            updateHeartbeat(false, e.getMessage());
            // Don't acknowledge - message will be reprocessed
        }
    }

    /**
     * Consumer for recruiter notifications
     */
    @KafkaListener(topics = KafkaConfig.RECRUITER_NOTIFICATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeRecruiterNotification(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("📨 Received RECRUITER notification | Topic: {} | Partition: {} | Offset: {} | EventId: {}",
                    KafkaConfig.RECRUITER_NOTIFICATION_TOPIC, partition, offset, event.getEventId());

            // Process the notification
            processNotification(event);

            // Manually commit offset after successful processing
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed RECRUITER notification: {}", event.getEventId());

            // Update heartbeat
            updateHeartbeat(true, null);

        } catch (Exception e) {
            log.error("❌ Error processing RECRUITER notification: {} | Error: {}",
                    event.getEventId(), e.getMessage(), e);
            // Update heartbeat with error
            updateHeartbeat(false, e.getMessage());
            // Don't acknowledge - message will be reprocessed
        }
    }

    /**
     * Consumer for candidate notifications
     */
    @KafkaListener(topics = KafkaConfig.CANDIDATE_NOTIFICATION_TOPIC, groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeCandidateNotification(
            @Payload NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("📨 Received CANDIDATE notification | Topic: {} | Partition: {} | Offset: {} | EventId: {}",
                    KafkaConfig.CANDIDATE_NOTIFICATION_TOPIC, partition, offset, event.getEventId());

            // Process the notification
            processNotification(event);

            // Manually commit offset after successful processing
            acknowledgment.acknowledge();
            log.info("✅ Successfully processed CANDIDATE notification: {}", event.getEventId());

            // Update heartbeat
            updateHeartbeat(true, null);

        } catch (Exception e) {
            log.error("❌ Error processing CANDIDATE notification: {} | Error: {}",
                    event.getEventId(), e.getMessage(), e);
            // Update heartbeat with error
            updateHeartbeat(false, e.getMessage());
            // Don't acknowledge - message will be reprocessed
        }
    }

    /**
     * Process notification event
     * Save to database for REST API retrieval by mobile clients
     */
    private void processNotification(NotificationEvent event) {
        log.info("Processing notification: {}", event);

        try {
            // Check if notification already exists (idempotency)
            if (notificationRepo.existsByEventId(event.getEventId())) {
                log.warn("⚠️ Notification with eventId {} already exists, skipping", event.getEventId());
                return;
            }

            // Save notification to database
            Notification notification = Notification.builder()
                    .eventId(event.getEventId())
                    .eventType(event.getEventType())
                    .recipientId(event.getRecipientId())
                    .recipientEmail(event.getRecipientEmail())
                    .title(event.getTitle())
                    .subject(event.getSubject())
                    .message(event.getMessage())
                    .category(event.getCategory())
                    .metadata(event.getMetadata())
                    .priority(event.getPriority())
                    .isRead(false)
                    .createdAt(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                    .build();

            notificationRepo.save(notification);
            log.info("✅ Notification saved to database | eventId: {} | recipient: {}",
                    event.getEventId(), event.getRecipientId());

            // Optional: Send additional notifications (email, push, etc.)
            sendAdditionalNotifications(event);

        } catch (Exception e) {
            log.error("❌ Failed to save notification to database | eventId: {}", event.getEventId(), e);
            throw e; // Re-throw to trigger Kafka retry
        }
    }

    /**
     * Send additional notifications (email, push, SMS, etc.)
     * This can be extended based on requirements
     */
    private void sendAdditionalNotifications(NotificationEvent event) {
        // Log different event types for monitoring
        switch (event.getEventType()) {
            case "JOB_POSTING_APPROVED":
                log.info("📋 Job posting approved notification processed for: {}", event.getRecipientId());
                break;
            case "JOB_POSTING_REJECTED":
                log.info("❌ Job posting rejected notification processed for: {}", event.getRecipientId());
                break;
            case "APPLICATION_RECEIVED":
                log.info("📬 Application received notification processed for: {}", event.getRecipientId());
                break;
            case "APPLICATION_STATUS_CHANGED":
                log.info("🔄 Application status changed notification processed for: {}", event.getRecipientId());
                break;
            case "PROFILE_UPDATE_REQUEST":
                log.info("👤 Profile update request notification processed for admin");
                break;
            case "PROFILE_UPDATE_APPROVED":
                log.info("✅ Profile update approved notification processed for: {}", event.getRecipientId());
                break;
            case "PROFILE_UPDATE_REJECTED":
                log.info("❌ Profile update rejected notification processed for: {}", event.getRecipientId());
                break;
            case "DAILY_REMINDER":
                log.info("⏰ Daily reminder notification processed for: {}", event.getRecipientId());
                break;
            case "ANNOUNCEMENT":
                log.info("📢 Announcement notification processed for: {}", event.getRecipientId());
                break;
            default:
                log.info("📧 Generic notification processed for: {}", event.getRecipientId());
        }

        // TODO: Integrate with email service, push notification service, etc.
        // Example: emailService.sendEmail(event.getRecipientEmail(),
        // event.getSubject(), event.getMessage());
        // Example: pushNotificationService.send(event.getRecipientId(),
        // event.getTitle(), event.getMessage());
    }

    /**
     * Update heartbeat for health monitoring
     */
    private void updateHeartbeat(boolean success, String errorMessage) {
        try {
            NotificationHeartbeat heartbeat = heartbeatRepo.findByName("notification-worker")
                    .orElse(NotificationHeartbeat.builder()
                            .name("notification-worker")
                            .lastProcessedAt(Instant.now())
                            .messageCount(0L)
                            .errorCount(0L)
                            .build());

            heartbeat.setLastProcessedAt(Instant.now());
            heartbeat.setMessageCount(heartbeat.getMessageCount() + 1);

            if (!success) {
                heartbeat.setErrorCount(heartbeat.getErrorCount() + 1);
                heartbeat.setLastErrorMessage(errorMessage);
            }

            heartbeatRepo.save(heartbeat);
        } catch (Exception e) {
            log.error("Failed to update heartbeat", e);
        }
    }
}
