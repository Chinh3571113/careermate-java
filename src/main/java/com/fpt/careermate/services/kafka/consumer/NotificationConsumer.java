package com.fpt.careermate.services.kafka.consumer;

import com.fpt.careermate.config.KafkaConfig;
import com.fpt.careermate.services.health_services.domain.NotificationHeartbeat;
import com.fpt.careermate.services.health_services.repository.NotificationHeartbeatRepo;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Kafka consumer service for processing notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationHeartbeatRepo heartbeatRepo;

    /**
     * Consumer for admin notifications
     */
    @KafkaListener(
        topics = KafkaConfig.ADMIN_NOTIFICATION_TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
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
    @KafkaListener(
        topics = KafkaConfig.RECRUITER_NOTIFICATION_TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
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
    @KafkaListener(
        topics = KafkaConfig.CANDIDATE_NOTIFICATION_TOPIC,
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
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
     * This is where you would integrate with your notification service
     */
    private void processNotification(NotificationEvent event) {
        log.info("Processing notification: {}", event);

        // Example: emailService.sendEmail(event.getRecipientEmail(), event.getSubject(), event.getMessage());
        // Example: pushNotificationService.send(event.getRecipientId(), event.getMessage());

        // Simulate processing
        switch (event.getEventType()) {
            case "JOB_POSTING_APPROVED":
                log.info("📋 Job posting approved notification sent to: {}", event.getRecipientId());
                break;
            case "JOB_POSTING_REJECTED":
                log.info("❌ Job posting rejected notification sent to: {}", event.getRecipientId());
                break;
            case "APPLICATION_RECEIVED":
                log.info("📬 Application received notification sent to: {}", event.getRecipientId());
                break;
            case "APPLICATION_STATUS_CHANGED":
                log.info("🔄 Application status changed notification sent to: {}", event.getRecipientId());
                break;
            default:
                log.info("📧 Generic notification sent to: {}", event.getRecipientId());
        }
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

