package com.fpt.careermate.services.health_services.service;

import io.weaviate.client.WeaviateClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("weaviate")
@RequiredArgsConstructor
@Slf4j
public class WeaviateHealthIndicator implements HealthIndicator {

    private final WeaviateClient weaviateClient;

    @Override
    public Health health() {
        try {
            // Simple connectivity test - try to get schema
            var result = weaviateClient.schema().getter().run();
            
            if (result != null && result.hasErrors()) {
                log.warn("Weaviate health check returned errors: {}", result.getError());
                return Health.down()
                        .withDetail("message", "Weaviate returned errors")
                        .withDetail("error", result.getError().toString())
                        .build();
            }
            
            if (result != null && result.getResult() != null) {
                return Health.up()
                        .withDetail("message", "Weaviate is reachable")
                        .withDetail("schemaClasses", result.getResult().getClasses().size())
                        .build();
            } else {
                return Health.down()
                        .withDetail("message", "Weaviate returned no schema")
                        .build();
            }
        } catch (Exception e) {
            log.error("Weaviate health check failed", e);
            return Health.down(e)
                    .withDetail("message", "Weaviate check failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
