package com.fpt.careermate.services.health_services.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component("kafka")
@RequiredArgsConstructor
@Slf4j
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    @Override
    public Health health() {
        try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterResult cluster = client.describeCluster();
            
            var nodes = cluster.nodes().get(3, TimeUnit.SECONDS);
            if (nodes == null || nodes.isEmpty()) {
                log.warn("Kafka health check: No brokers found");
                return Health.down()
                        .withDetail("message", "No Kafka brokers found")
                        .build();
            }
            
            String controllerId = cluster.controller().get(2, TimeUnit.SECONDS).idString();
            String clusterId = cluster.clusterId().get(2, TimeUnit.SECONDS);
            
            return Health.up()
                    .withDetail("brokers", nodes.size())
                    .withDetail("controller", controllerId)
                    .withDetail("clusterId", clusterId)
                    .withDetail("message", "Kafka cluster is healthy")
                    .build();
                    
        } catch (Exception e) {
            log.error("Kafka health check failed", e);
            return Health.down(e)
                    .withDetail("message", "Kafka cluster check failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
