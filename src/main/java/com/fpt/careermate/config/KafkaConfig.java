package com.fpt.careermate.config;

import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for creating topics and setting up basic Kafka beans.
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    // Cloud Kafka security settings (read directly from env vars)
    @Value("${KAFKA_SECURITY_PROTOCOL:}")
    private String securityProtocol;

    @Value("${KAFKA_SASL_MECHANISM:}")
    private String saslMechanism;

    @Value("${KAFKA_SASL_USERNAME:}")
    private String saslUsername;

    @Value("${KAFKA_SASL_PASSWORD:}")
    private String saslPassword;

    // Topic names as constants
    public static final String ADMIN_NOTIFICATION_TOPIC = "admin-notifications";
    public static final String RECRUITER_NOTIFICATION_TOPIC = "recruiter-notifications";
    public static final String CANDIDATE_NOTIFICATION_TOPIC = "candidate-notifications";

    /**
     * Common security properties for cloud Kafka (Confluent, Upstash, etc.)
     */
    private void addSecurityProperties(Map<String, Object> props) {
        if (securityProtocol != null && !securityProtocol.isEmpty()) {
            props.put("security.protocol", securityProtocol);
        }
        if (saslMechanism != null && !saslMechanism.isEmpty()) {
            props.put("sasl.mechanism", saslMechanism);
        }
        if (saslUsername != null && !saslUsername.isEmpty() && saslPassword != null && !saslPassword.isEmpty()) {
            String jaasConfig = String.format(
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
                saslUsername, saslPassword
            );
            props.put("sasl.jaas.config", jaasConfig);
        }
    }

    /**
     * Create admin notification topic
     */
    @Bean
    public NewTopic adminNotificationTopic() {
        return TopicBuilder
                .name(ADMIN_NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Create recruiter notification topic
     */
    @Bean
    public NewTopic recruiterNotificationTopic() {
        return TopicBuilder
                .name(RECRUITER_NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Create candidate notification topic
     */
    @Bean
    public NewTopic candidateNotificationTopic() {
        return TopicBuilder
                .name(CANDIDATE_NOTIFICATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Producer configuration
     */
    @Bean
    public ProducerFactory<String, NotificationEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Add cloud security properties if configured
        addSecurityProperties(configProps);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Kafka template for sending messages
     */
    @Bean
    public KafkaTemplate<String, NotificationEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * String-based producer factory for simple string messages (e.g., testing)
     */
    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Add cloud security properties if configured
        addSecurityProperties(configProps);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * String-based Kafka template for sending simple string messages
     */
    @Bean
    public KafkaTemplate<String, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(stringProducerFactory());
    }

    /**
     * Consumer configuration
     */
    @Bean
    public ConsumerFactory<String, NotificationEvent> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationEvent.class.getName());
        
        // Add cloud security properties if configured
        addSecurityProperties(configProps);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    /**
     * Kafka listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NotificationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConcurrency(3);
        return factory;
    }
}

