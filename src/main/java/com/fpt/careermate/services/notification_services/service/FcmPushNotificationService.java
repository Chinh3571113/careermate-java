package com.fpt.careermate.services.notification_services.service;

import com.fpt.careermate.services.notification_services.domain.DeviceToken;
import com.fpt.careermate.services.notification_services.repository.DeviceTokenRepo;
import com.fpt.careermate.services.notification_services.service.dto.response.NotificationResponse;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for sending push notifications via Firebase Cloud Messaging (FCM).
 * Handles Firebase initialization, message sending, and device token
 * management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FcmPushNotificationService {

    private final DeviceTokenRepo deviceTokenRepo;

    /**
     * Initialize Firebase Admin SDK on application startup.
     * Uses service account credentials from environment variable or classpath.
     */
    @PostConstruct
    public void initializeFirebase() {
        try {
            // Check if Firebase is already initialized
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("🔥 Firebase already initialized");
                return;
            }

            InputStream serviceAccount;

            // Try to get credentials from environment variable first (for Cloud Run)
            String credentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON");

            if (credentialsJson != null && !credentialsJson.isEmpty()) {
                // Environment variable contains JSON string (Cloud Run with Secret Manager)
                serviceAccount = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
                log.info("Using Firebase credentials from FIREBASE_CREDENTIALS_JSON environment variable");
            } else {
                // Fallback to classpath
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                if (!resource.exists()) {
                    log.error("Firebase service account file not found! Please add firebase-service-account.json to src/main/resources/");
                }
                serviceAccount = resource.getInputStream();
                log.info("Using Firebase credentials from classpath");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

            log.info("Firebase Cloud Messaging initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }

    /**
     * Send a push notification to a specific user.
     * Sends to all active device tokens registered for that user.
     *
     * @param userId       The user ID (email)
     * @param notification The notification to send
     * @return Number of devices successfully sent to
     */
    public int sendNotificationToUser(String userId, NotificationResponse notification) {
        List<DeviceToken> tokens = deviceTokenRepo.findByUserIdAndIsActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.debug("⚠️ No active device tokens for user: {}", userId);
            return 0;
        }

        log.info("📱 Sending push notification | userId: {} | devices: {} | eventType: {}",
                userId, tokens.size(), notification.getEventType());

        // Prepare notification data
        Map<String, String> data = new HashMap<>();
        data.put("notificationId", String.valueOf(notification.getId()));
        data.put("eventType", notification.getEventType());
        data.put("category", notification.getCategory() != null ? notification.getCategory() : "");
        data.put("priority", String.valueOf(notification.getPriority()));
        data.put("timestamp", notification.getCreatedAt().toString());

        // Send to each device and count successes
        int successCount = 0;
        for (DeviceToken deviceToken : tokens) {
            if (sendToDevice(deviceToken, notification, data)) {
                successCount++;
            }
        }

        return successCount;
    }

    /**
     * Send push notification to a single device token.
     *
     * @param deviceToken  The device token entity
     * @param notification The notification content
     * @param data         Additional data payload
     * @return true if sent successfully, false otherwise
     */
    private boolean sendToDevice(DeviceToken deviceToken, NotificationResponse notification, Map<String, String> data) {
        try {
            // Build notification payload
            Notification fcmNotification = Notification.builder()
                    .setTitle(notification.getTitle())
                    .setBody(notification.getMessage())
                    .build();

            // Build Android-specific config
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(getAndroidPriority(notification.getPriority()))
                    .setNotification(AndroidNotification.builder()
                            .setSound("default")
                            .setColor("#667eea") // CareerMate brand color
                            .setChannelId("careermate_notifications")
                            .build())
                    .build();

            // Build iOS-specific config
            ApnsConfig apnsConfig = ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setSound("default")
                            .setBadge(notification.getPriority())
                            .build())
                    .build();

            // Build the complete message
            Message message = Message.builder()
                    .setToken(deviceToken.getToken())
                    .setNotification(fcmNotification)
                    .putAllData(data)
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig)
                    .build();

            // Send the message
            String response = FirebaseMessaging.getInstance().send(message);

            log.info("✅ Push notification sent | userId: {} | deviceType: {} | messageId: {}",
                    deviceToken.getUserId(), deviceToken.getDeviceType(), response);

            // Update last used timestamp
            deviceToken.setLastUsedAt(LocalDateTime.now());
            deviceTokenRepo.save(deviceToken);

            return true;

        } catch (FirebaseMessagingException e) {
            handleFirebaseError(deviceToken, e);
            return false;
        } catch (Exception e) {
            log.error("❌ Unexpected error sending push notification | userId: {} | error: {}",
                    deviceToken.getUserId(), e.getMessage());
            return false;
        }
    }

    /**
     * Handle Firebase Messaging errors and mark invalid tokens as inactive.
     *
     * @param deviceToken The device token that failed
     * @param exception   The Firebase exception
     */
    private void handleFirebaseError(DeviceToken deviceToken, FirebaseMessagingException exception) {
        String errorCode = exception.getErrorCode() != null ? exception.getErrorCode().toString() : "UNKNOWN";

        log.error("❌ FCM error | userId: {} | errorCode: {} | message: {}",
                deviceToken.getUserId(), errorCode, exception.getMessage());

        // Mark token as inactive if it's invalid or unregistered
        if ("INVALID_ARGUMENT".equals(errorCode) ||
                "UNREGISTERED".equals(errorCode) ||
                "SENDER_ID_MISMATCH".equals(errorCode)) {

            deviceToken.setIsActive(false);
            deviceTokenRepo.save(deviceToken);

            log.warn("⚠️ Device token marked inactive | userId: {} | deviceType: {} | reason: {}",
                    deviceToken.getUserId(), deviceToken.getDeviceType(), errorCode);
        }
    }

    /**
     * Convert notification priority to Android priority.
     *
     * @param priority 1=HIGH, 2=MEDIUM, 3=LOW
     * @return Android priority
     */
    private AndroidConfig.Priority getAndroidPriority(Integer priority) {
        if (priority == null)
            return AndroidConfig.Priority.NORMAL;
        return priority <= 2 ? AndroidConfig.Priority.HIGH : AndroidConfig.Priority.NORMAL;
    }

    /**
     * Send a simple push notification with title and body.
     *
     * @param userId The user ID (email)
     * @param title  Notification title
     * @param body   Notification body
     */
    public void sendSimplePush(String userId, String title, String body) {
        List<DeviceToken> tokens = deviceTokenRepo.findByUserIdAndIsActiveTrue(userId);

        if (tokens.isEmpty()) {
            log.debug("⚠️ No active device tokens for user: {}", userId);
            return;
        }

        log.info("📱 Sending simple push | userId: {} | devices: {} | title: {}",
                userId, tokens.size(), title);

        tokens.forEach(deviceToken -> {
            try {
                Message message = Message.builder()
                        .setToken(deviceToken.getToken())
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .build();

                FirebaseMessaging.getInstance().send(message);
                log.info("✅ Simple push sent | userId: {}", userId);

            } catch (FirebaseMessagingException e) {
                handleFirebaseError(deviceToken, e);
            }
        });
    }

    /**
     * Send push notification to multiple users.
     *
     * @param userIds List of user IDs
     * @param title   Notification title
     * @param body    Notification body
     */
    public void sendBroadcast(List<String> userIds, String title, String body) {
        userIds.forEach(userId -> sendSimplePush(userId, title, body));
        log.info("📢 Broadcast push sent to {} users", userIds.size());
    }

    /**
     * Clean up inactive device tokens for all users.
     * Call this periodically to remove stale tokens.
     */
    public void cleanupInactiveTokens() {
        List<DeviceToken> inactiveTokens = deviceTokenRepo.findAll().stream()
                .filter(token -> !token.getIsActive())
                .toList();

        if (!inactiveTokens.isEmpty()) {
            deviceTokenRepo.deleteAll(inactiveTokens);
            log.info("🧹 Cleaned up {} inactive device tokens", inactiveTokens.size());
        }
    }
}
