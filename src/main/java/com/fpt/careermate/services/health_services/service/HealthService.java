package com.fpt.careermate.services.health_services.service;

import com.fpt.careermate.services.health_services.service.dto.ComponentHealth;
import com.fpt.careermate.services.health_services.service.dto.HealthStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthService {

    private final Map<String, HealthIndicator> healthIndicators;

    public HealthStatusDTO getAggregatedHealth() {
        Map<String, ComponentHealth> components = new LinkedHashMap<>();
        boolean systemDown = false;

        // Only check critical components for system status
        String[] criticalComponents = { "db", "kafka" };

        for (Map.Entry<String, HealthIndicator> entry : healthIndicators.entrySet()) {
            String name = entry.getKey();

            // Skip notification worker completely - it's not critical
            if (name.equals("notificationWorker")) {
                continue;
            }

            Health health = entry.getValue().health();
            String status = health.getStatus().getCode();

            // Check if this is a critical component
            boolean isCritical = false;
            for (String criticalName : criticalComponents) {
                if (name.equals(criticalName)) {
                    isCritical = true;
                    break;
                }
            }

            // Only critical components affect overall status
            if (isCritical && Status.DOWN.getCode().equals(status)) {
                systemDown = true;
                log.warn("Critical component {} is DOWN!", name);
            }

            Map<String, Object> details = health.getDetails();
            String message = details != null ? details.getOrDefault("message", "").toString() : "";

            components.put(name, new ComponentHealth(
                    name,
                    status,
                    message,
                    Instant.now(),
                    details != null ? details : Map.of()));
        }

        String overall = systemDown ? Status.DOWN.getCode() : Status.UP.getCode();
        log.info("Health check: {} (DB and Kafka only)", overall);
        return new HealthStatusDTO(overall, components, Instant.now());
    }

    public boolean isCriticalComponentDown() {
        // Check critical components: kafka, db, weaviate
        String[] criticalComponents = { "kafka", "db", "weaviate" };

        for (String componentName : criticalComponents) {
            if (healthIndicators.containsKey(componentName)) {
                Health health = healthIndicators.get(componentName).health();
                String status = health.getStatus().getCode();
                if (Status.DOWN.getCode().equals(status) || Status.OUT_OF_SERVICE.getCode().equals(status)) {
                    log.warn("Critical component {} is down!", componentName);
                    return true;
                }
            }
        }

        return false;
    }
}
