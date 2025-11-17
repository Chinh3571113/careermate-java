package com.fpt.careermate.services.notification_services.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.kafka.producer.NotificationProducer;
import com.fpt.careermate.services.notification_services.domain.Notification;
import com.fpt.careermate.services.notification_services.repository.NotificationRepo;
import com.fpt.careermate.services.notification_services.service.dto.response.NotificationResponse;
import com.fpt.careermate.services.notification_services.service.dto.response.NotificationStatsResponse;
import com.fpt.careermate.services.notification_services.service.mapper.NotificationMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationImp implements NotificationService {

    NotificationRepo notificationRepo;
    NotificationMapper notificationMapper;
    NotificationProducer notificationProducer;
    AccountRepo accountRepo;

    /**
     * Get current authenticated user ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName(); // Returns user email or ID
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        String userId = getCurrentUserId();
        log.info("Fetching notifications for user: {}", userId);

        Page<Notification> notifications = notificationRepo.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(Pageable pageable) {
        String userId = getCurrentUserId();
        log.info("Fetching unread notifications for user: {}", userId);

        Page<Notification> notifications = notificationRepo.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId,
                pageable);
        return notifications.map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        String userId = getCurrentUserId();
        long count = notificationRepo.countByRecipientIdAndIsReadFalse(userId);
        log.debug("Unread count for user {}: {}", userId, count);
        return count;
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        String userId = getCurrentUserId();
        log.info("Marking notification {} as read for user: {}", notificationId, userId);

        Notification notification = notificationRepo.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepo.save(notification);
            log.info("✅ Notification {} marked as read", notificationId);
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        String userId = getCurrentUserId();
        log.info("Marking all notifications as read for user: {}", userId);

        int updatedCount = notificationRepo.markAllAsRead(userId, LocalDateTime.now());
        log.info("✅ Marked {} notifications as read for user: {}", updatedCount, userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        String userId = getCurrentUserId();
        log.info("Deleting notification {} for user: {}", notificationId, userId);

        Notification notification = notificationRepo.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notificationRepo.delete(notification);
        log.info("✅ Notification {} deleted", notificationId);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatsResponse getNotificationStats() {
        String userId = getCurrentUserId();
        log.debug("Fetching notification stats for user: {}", userId);

        long total = notificationRepo.countByRecipientIdAndIsReadFalse(userId);
        long unread = notificationRepo.countByRecipientIdAndIsReadFalse(userId);

        return NotificationStatsResponse.builder()
                .totalNotifications(total)
                .unreadCount(unread)
                .highPriorityCount(0) // Can be enhanced later
                .build();
    }

    @Override
    @Transactional
    public void sendTestNotification(String userRole, String recipientId) {
        log.info("Sending test notification for role: {} to recipient: {}", userRole, recipientId);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("testType", "manual");
        metadata.put("timestamp", LocalDateTime.now().toString());

        NotificationEvent event = NotificationEvent.builder()
                .eventType("TEST_" + userRole.toUpperCase() + "_NOTIFICATION")
                .recipientId(recipientId)
                .recipientEmail(recipientId + "@test.com")
                .title("Test Notification")
                .subject("Test Notification for " + userRole)
                .message(
                        "This is a test notification to verify the Kafka and notification system is working correctly.")
                .category("TEST")
                .metadata(metadata)
                .priority(2)
                .build();

        // Send to appropriate topic based on role
        switch (userRole.toUpperCase()) {
            case "ADMIN":
                notificationProducer.sendAdminNotification(event);
                break;
            case "RECRUITER":
                notificationProducer.sendRecruiterNotification(event);
                break;
            case "CANDIDATE":
                notificationProducer.sendNotification("candidate-notifications", event);
                break;
            default:
                throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        log.info("✅ Test notification sent successfully");
    }

    @Override
    @Transactional
    public void sendNotificationToRole(String roleName, String title, String message, String category, Integer priority) {
        log.info("📢 Sending notification to all users with role: {}", roleName);

        try {
            // Get all accounts with the specified role
            List<Account> accounts = accountRepo.searchAccounts(
                    List.of(roleName),
                    List.of("ACTIVE"),
                    null,
                    Pageable.unpaged()
            ).getContent();

            log.info("Found {} active users with role: {}", accounts.size(), roleName);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("broadcastType", "role");
            metadata.put("targetRole", roleName);
            metadata.put("timestamp", LocalDateTime.now().toString());

            // Send notification to each user
            for (Account account : accounts) {
                NotificationEvent event = NotificationEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("BROADCAST_NOTIFICATION")
                        .recipientId(account.getEmail())
                        .recipientEmail(account.getEmail())
                        .title(title)
                        .subject(title)
                        .message(message)
                        .category(category != null ? category : "ANNOUNCEMENT")
                        .metadata(metadata)
                        .priority(priority != null ? priority : 2)
                        .timestamp(LocalDateTime.now())
                        .build();

                // Route to appropriate topic based on role
                if (roleName.equalsIgnoreCase("ADMIN")) {
                    notificationProducer.sendAdminNotification(event);
                } else if (roleName.equalsIgnoreCase("RECRUITER")) {
                    notificationProducer.sendRecruiterNotification(event);
                } else if (roleName.equalsIgnoreCase("CANDIDATE")) {
                    notificationProducer.sendNotification("candidate-notifications", event);
                }
            }

            log.info("✅ Successfully sent {} notifications to role: {}", accounts.size(), roleName);
        } catch (Exception e) {
            log.error("❌ Failed to send notification to role: {}", roleName, e);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Override
    @Transactional
    public void sendBroadcastNotification(String title, String message, String category, Integer priority) {
        log.info("📢 Sending broadcast notification to all active users");

        try {
            // Get all active accounts
            List<Account> accounts = accountRepo.searchAccounts(
                    null,
                    List.of("ACTIVE"),
                    null,
                    Pageable.unpaged()
            ).getContent();

            log.info("Found {} active users for broadcast", accounts.size());

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("broadcastType", "all");
            metadata.put("timestamp", LocalDateTime.now().toString());

            // Send notification to each user
            for (Account account : accounts) {
                NotificationEvent event = NotificationEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("BROADCAST_NOTIFICATION")
                        .recipientId(account.getEmail())
                        .recipientEmail(account.getEmail())
                        .title(title)
                        .subject(title)
                        .message(message)
                        .category(category != null ? category : "ANNOUNCEMENT")
                        .metadata(metadata)
                        .priority(priority != null ? priority : 2)
                        .timestamp(LocalDateTime.now())
                        .build();

                // Route to appropriate topic based on user's primary role
                if (account.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
                    notificationProducer.sendAdminNotification(event);
                } else if (account.getRoles().stream().anyMatch(role -> role.getName().equals("RECRUITER"))) {
                    notificationProducer.sendRecruiterNotification(event);
                } else if (account.getRoles().stream().anyMatch(role -> role.getName().equals("CANDIDATE"))) {
                    notificationProducer.sendNotification("candidate-notifications", event);
                }
            }

            log.info("✅ Successfully sent {} broadcast notifications", accounts.size());
        } catch (Exception e) {
            log.error("❌ Failed to send broadcast notification", e);
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }
}
