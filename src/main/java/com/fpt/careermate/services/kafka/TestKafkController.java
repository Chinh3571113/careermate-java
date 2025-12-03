package com.fpt.careermate.services.kafka;

import com.fpt.careermate.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Test - Kafka", description = "For development testing Kafka integration")
@RequestMapping("/test/kafka")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Component
public class TestKafkController {
    KafkaTemplate<String, String> kafkaTemplate;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<Void> testKafka() {
        kafkaTemplate.send("test-topic", "ping from cloud run");
        log.info("Sent test message!");

        return ApiResponse.<Void>builder()
                .code(200)
                .message("success")
                .build();
    }
}
