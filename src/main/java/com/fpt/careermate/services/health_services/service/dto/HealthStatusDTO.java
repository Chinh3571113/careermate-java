package com.fpt.careermate.services.health_services.service.dto;

import java.time.Instant;
import java.util.Map;

public record HealthStatusDTO(
        String overallStatus,
        Map<String, ComponentHealth> components,
        Instant generatedAt
) {}
