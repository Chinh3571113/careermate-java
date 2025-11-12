package com.fpt.careermate.services.kafka.producer;

import com.fpt.careermate.config.KafkaConfig;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer service for sending notifications
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    /**
     * Send notification event to admin notification topic
     */
    public void sendAdminNotification(NotificationEvent event) {
        sendNotification(KafkaConfig.ADMIN_NOTIFICATION_TOPIC, event);
    }

    /**
     * Send notification event to recruiter notification topic
     */
    public void sendRecruiterNotification(NotificationEvent event) {
        sendNotification(KafkaConfig.RECRUITER_NOTIFICATION_TOPIC, event);
    }

    /**
     * Generic method to send notification to any topic
     */
    public void sendNotification(String topic, NotificationEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }

        try {
            log.info("📤 Sending notification to topic: {} with eventId: {}", topic, event.getEventId());

            CompletableFuture<SendResult<String, NotificationEvent>> future =
                kafkaTemplate.send(topic, event.getEventId(), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Message sent successfully to topic: {} | partition: {} | offset: {}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("❌ Failed to send message to topic: {} | error: {}", topic, ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("❌ Error sending notification to topic: {}", topic, e);
            throw new RuntimeException("Failed to send Kafka message", e);
        }
    }

    /**
     * Send notification with simple message
     */
    public void sendSimpleNotification(String topic, String recipientId, String subject, String message) {
        NotificationEvent event = NotificationEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .recipientId(recipientId)
            .subject(subject)
            .message(message)
            .timestamp(LocalDateTime.now())
            .priority(2) // Medium priority
            .build();

        sendNotification(topic, event);
    }
}

