package com.fpt.careermate.services.health_services.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component("email")
@RequiredArgsConstructor
@Slf4j
public class EmailHealthIndicator implements HealthIndicator {

    private final JavaMailSender mailSender;

    @Override
    public Health health() {
        try {
            // Create a test message to verify mail sender is configured
            var message = mailSender.createMimeMessage();
            
            if (message != null) {
                // Mail sender is properly configured
                return Health.up()
                        .withDetail("message", "Mail sender is configured")
                        .withDetail("status", "ready")
                        .build();
            } else {
                return Health.down()
                        .withDetail("message", "Mail sender returned null message")
                        .build();
            }
        } catch (Exception e) {
            log.error("Email health check failed", e);
            return Health.down(e)
                    .withDetail("message", "Mail sender check failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
