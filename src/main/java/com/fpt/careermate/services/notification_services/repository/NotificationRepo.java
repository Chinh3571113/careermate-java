package com.fpt.careermate.services.notification_services.repository;

import com.fpt.careermate.services.notification_services.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific recipient with pagination
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId, Pageable pageable);

    /**
     * Find unread notifications for a specific recipient
     */
    Page<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(String recipientId, Pageable pageable);

    /**
     * Count unread notifications for a recipient
     */
    long countByRecipientIdAndIsReadFalse(String recipientId);

    /**
     * Find notification by ID and recipient ID (for authorization)
     */
    Optional<Notification> findByIdAndRecipientId(Long id, String recipientId);

    /**
     * Check if notification exists by event ID
     */
    boolean existsByEventId(String eventId);

    /**
     * Find notification by event ID
     */
    Optional<Notification> findByEventId(String eventId);

    /**
     * Mark all unread notifications as read for a recipient
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.recipientId = :recipientId AND n.isRead = false")
    int markAllAsRead(@Param("recipientId") String recipientId, @Param("readAt") LocalDateTime readAt);

    /**
     * Delete old read notifications (cleanup)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get notification statistics for a recipient
     */
    @Query("SELECT COUNT(n) as total, " +
            "SUM(CASE WHEN n.isRead = false THEN 1 ELSE 0 END) as unread, " +
            "SUM(CASE WHEN n.priority = 1 THEN 1 ELSE 0 END) as highPriority " +
            "FROM Notification n WHERE n.recipientId = :recipientId")
    Object getNotificationStats(@Param("recipientId") String recipientId);
}
