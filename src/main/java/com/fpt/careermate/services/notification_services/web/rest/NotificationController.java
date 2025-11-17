package com.fpt.careermate.services.notification_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.notification_services.service.NotificationService;
import com.fpt.careermate.services.notification_services.service.dto.response.NotificationResponse;
import com.fpt.careermate.services.notification_services.service.dto.response.NotificationStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "APIs for managing user notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class NotificationController {

        NotificationService notificationService;

        @GetMapping
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get My Notifications", description = "Retrieve all notifications for the authenticated user with pagination")
        public ApiResponse<Page<NotificationResponse>> getMyNotifications(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {

                log.info("REST request to get notifications | page: {}, size: {}", page, size);

                Sort sort = sortDir.equalsIgnoreCase("ASC")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();
                Pageable pageable = PageRequest.of(page, size, sort);

                return ApiResponse.<Page<NotificationResponse>>builder()
                                .result(notificationService.getMyNotifications(pageable))
                                .build();
        }

        @GetMapping("/unread")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get Unread Notifications", description = "Retrieve only unread notifications for the authenticated user")
        public ApiResponse<Page<NotificationResponse>> getUnreadNotifications(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                log.info("REST request to get unread notifications | page: {}, size: {}", page, size);

                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                return ApiResponse.<Page<NotificationResponse>>builder()
                                .result(notificationService.getUnreadNotifications(pageable))
                                .build();
        }

        @GetMapping("/unread-count")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get Unread Count", description = "Get the count of unread notifications for the authenticated user")
        public ApiResponse<Long> getUnreadCount() {
                log.debug("REST request to get unread notification count");

                return ApiResponse.<Long>builder()
                                .result(notificationService.getUnreadCount())
                                .build();
        }

        @PutMapping("/{notificationId}/read")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Mark Notification as Read", description = "Mark a specific notification as read")
        public ApiResponse<NotificationResponse> markAsRead(@PathVariable Long notificationId) {
                log.info("REST request to mark notification as read: {}", notificationId);

                return ApiResponse.<NotificationResponse>builder()
                                .result(notificationService.markAsRead(notificationId))
                                .build();
        }

        @PutMapping("/mark-all-read")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Mark All as Read", description = "Mark all notifications as read for the authenticated user")
        public ApiResponse<Void> markAllAsRead() {
                log.info("REST request to mark all notifications as read");

                notificationService.markAllAsRead();

                return ApiResponse.<Void>builder()
                                .message("All notifications marked as read")
                                .build();
        }

        @DeleteMapping("/{notificationId}")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Delete Notification", description = "Delete a specific notification")
        public ApiResponse<Void> deleteNotification(@PathVariable Long notificationId) {
                log.info("REST request to delete notification: {}", notificationId);

                notificationService.deleteNotification(notificationId);

                return ApiResponse.<Void>builder()
                                .message("Notification deleted successfully")
                                .build();
        }

        @GetMapping("/stats")
        @PreAuthorize("isAuthenticated()")
        @Operation(summary = "Get Notification Statistics", description = "Get notification statistics for the authenticated user")
        public ApiResponse<NotificationStatsResponse> getStats() {
                log.debug("REST request to get notification stats");

                return ApiResponse.<NotificationStatsResponse>builder()
                                .result(notificationService.getNotificationStats())
                                .build();
        }

        @PostMapping("/test/{userRole}/{recipientId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Send Test Notification", description = "Send a test notification (Admin only)")
        public ApiResponse<Void> sendTestNotification(
                        @PathVariable String userRole,
                        @PathVariable String recipientId) {

                log.info("REST request to send test notification | role: {}, recipient: {}", userRole, recipientId);

                notificationService.sendTestNotification(userRole, recipientId);

                return ApiResponse.<Void>builder()
                                .message("Test notification sent successfully")
                                .build();
        }

        @PostMapping("/broadcast/role")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Send Notification to Role", description = "Send notification to all users with specific role (Admin only)")
        public ApiResponse<Void> sendNotificationToRole(
                        @RequestParam String roleName,
                        @RequestParam String title,
                        @RequestParam String message,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false, defaultValue = "2") Integer priority) {

                log.info("REST request to send notification to role: {} | title: {}", roleName, title);

                notificationService.sendNotificationToRole(roleName, title, message, category, priority);

                return ApiResponse.<Void>builder()
                                .message("Notification sent to all users with role: " + roleName)
                                .build();
        }

        @PostMapping("/broadcast/all")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Send Broadcast Notification", description = "Send notification to all active users (Admin only)")
        public ApiResponse<Void> sendBroadcastNotification(
                        @RequestParam String title,
                        @RequestParam String message,
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false, defaultValue = "2") Integer priority) {

                log.info("REST request to send broadcast notification | title: {}", title);

                notificationService.sendBroadcastNotification(title, message, category, priority);

                return ApiResponse.<Void>builder()
                                .message("Broadcast notification sent to all active users")
                                .build();
        }
}
