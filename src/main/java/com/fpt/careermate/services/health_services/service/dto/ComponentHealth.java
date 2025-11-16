package com.fpt.careermate.services.health_services.service.dto;

import java.time.Instant;
import java.util.Map;

public record ComponentHealth(
        String name,
        String status,
        String message,
        Instant checkedAt,
        Map<String, Object> details
) {}
