package com.fpt.careermate.services.notification_services.web.rest;

import com.fpt.careermate.services.notification_services.service.NotificationSseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller for Server-Sent Events (SSE) real-time notification streaming.
 * Uses standard Spring Security authentication via Authorization header.
 * Frontend should use fetch() with headers for SSE instead of native EventSource.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Tag(name = "Notification SSE", description = "Real-time notification streaming via Server-Sent Events")
@RequiredArgsConstructor
@Slf4j
public class NotificationSseController {

    private final NotificationSseService sseService;

    /**
     * Establish an SSE connection for real-time notifications.
     * Requires standard JWT authentication via Authorization header.
     * 
     * Frontend should use fetch() with streaming instead of native EventSource
     * to properly send Authorization headers.
     * 
     * @param authentication Spring Security authentication (injected)
     * @return SseEmitter that streams notifications to the client
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream real-time notifications", description = """
            Establish a Server-Sent Events (SSE) connection to receive real-time notifications.
            
            Requires Authorization header with Bearer token.
            
            **Events**:
            - connected: Connection established successfully
            - notification: New notification received (JSON object)
            - unread-count: Updated unread count (JSON: { count: number })
            - keepalive: Keep connection alive (sent periodically)
            
            **Connection Details**:
            - Timeout: 30 minutes
            - Auto-reconnect: Handle in frontend
            """)
    public SseEmitter streamNotifications(Authentication authentication) {
        String userId = authentication.getName();
        log.info("🔌 SSE connection established | userId: {}", userId);
        return sseService.createConnection(userId);
    }
}
