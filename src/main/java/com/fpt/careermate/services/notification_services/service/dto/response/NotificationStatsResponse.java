package com.fpt.careermate.services.notification_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationStatsResponse {
    long totalNotifications;
    long unreadCount;
    long highPriorityCount;
}
