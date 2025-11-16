package com.fpt.careermate.services.notification_services.service.mapper;

import com.fpt.careermate.services.notification_services.domain.Notification;
import com.fpt.careermate.services.notification_services.service.dto.response.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
