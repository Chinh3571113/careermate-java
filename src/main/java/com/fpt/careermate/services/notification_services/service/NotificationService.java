package com.fpt.careermate.services.notification_services.service;

import com.fpt.careermate.services.notification_services.service.dto.response.NotificationResponse;
import com.fpt.careermate.services.notification_services.service.dto.response.NotificationStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Get all notifications for the authenticated user
     */
    Page<NotificationResponse> getMyNotifications(Pageable pageable);

    /**
     * Get unread notifications for the authenticated user
     */
    Page<NotificationResponse> getUnreadNotifications(Pageable pageable);

    /**
     * Get unread notification count
     */
    long getUnreadCount();

    /**
     * Mark a notification as read
     */
    NotificationResponse markAsRead(Long notificationId);

    /**
     * Mark all notifications as read
     */
    void markAllAsRead();

    /**
     * Delete a notification
     */
    void deleteNotification(Long notificationId);

    /**
     * Get notification statistics
     */
    NotificationStatsResponse getNotificationStats();

    /**
     * Send test notification (for testing purposes)
     */
    void sendTestNotification(String userRole, String recipientId);

    /**
     * Send notification to all users with a specific role
     */
    void sendNotificationToRole(String roleName, String title, String message, String category, Integer priority);

    /**
     * Send notification to all active users (broadcast)
     */
    void sendBroadcastNotification(String title, String message, String category, Integer priority);
}
