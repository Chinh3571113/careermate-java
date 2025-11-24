package com.fpt.careermate.services.health_services.service;

import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("firebase")
@RequiredArgsConstructor
@Slf4j
public class FirebaseHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Check if Firebase Storage bucket is accessible
            var bucket = StorageClient.getInstance().bucket();

            if (bucket != null && bucket.exists()) {
                return Health.up()
                        .withDetail("message", "Firebase Storage is accessible")
                        .withDetail("bucket", bucket.getName())
                        .build();
            } else {
                return Health.down()
                        .withDetail("message", "Firebase Storage bucket not found")
                        .build();
            }
        } catch (Exception e) {
            log.error("Firebase health check failed", e);
            return Health.down(e)
                    .withDetail("message", "Firebase check failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
