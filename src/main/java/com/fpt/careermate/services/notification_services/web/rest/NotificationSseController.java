package com.fpt.careermate.services.notification_services.web.rest;

import com.fpt.careermate.config.CustomJwtDecoder;
import com.fpt.careermate.services.notification_services.service.NotificationSseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * Controller for Server-Sent Events (SSE) real-time notification streaming.
 * Provides endpoints for clients to establish long-lived HTTP connections
 * and receive real-time notifications as they occur.
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
@Tag(name = "Notification SSE", description = "Real-time notification streaming via Server-Sent Events")
@RequiredArgsConstructor
@Slf4j
public class NotificationSseController {

    private final NotificationSseService sseService;
    private final CustomJwtDecoder jwtDecoder;

    /**
     * Establish an SSE connection for real-time notifications.
     * 
     * IMPORTANT: Native EventSource cannot send Authorization headers!
     * You MUST pass the JWT token as a query parameter.
     * 
     * This endpoint creates a long-lived HTTP connection that streams notifications
     * to the client in real-time.
     * 
     * How to use from frontend:
     * 1. Get token: const token = localStorage.getItem('token');
     * 2. Create connection: new EventSource('/api/notifications/stream?token=' + token);
     * 3. Listen for 'notification' and 'unread-count' events
     * 
     * Events sent by server:
     * - connected: Initial event confirming connection established
     * - notification: New notification received (NotificationResponse object)
     * - unread-count: Updated unread notification count
     * - keepalive: Periodic ping to keep connection alive
     * 
     * Connection management:
     * - Timeout: 30 minutes of inactivity
     * - Auto-reconnect: Browser handles reconnection automatically
     * - Multiple tabs: Each tab gets its own connection
     * - Authentication: JWT token required as query parameter
     * 
     * @param token JWT Bearer token (query parameter) - REQUIRED because EventSource cannot send headers
     * @param response HttpServletResponse for error handling
     * @return SseEmitter that streams notifications to the client
     * @throws IOException if response writing fails
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream real-time notifications", description = """
            Establish a Server-Sent Events (SSE) connection to receive real-time notifications.

            **IMPORTANT**: Pass JWT token as query parameter: ?token=YOUR_JWT_TOKEN

            Native EventSource API cannot send custom headers, so token must be in query parameter.

            **Events**:
            - connected: Connection established successfully
            - notification: New notification received (JSON object)
            - unread-count: Updated unread count (JSON: { count: number })
            - keepalive: Keep connection alive (sent periodically)

            **Usage Example**:
            1. Get token from localStorage: const jwtToken = localStorage.getItem('token');
            2. Create EventSource: new EventSource('/api/notifications/stream?token=' + jwtToken);
            3. Listen for 'notification' and 'unread-count' events

            **Connection Details**:
            - Timeout: 30 minutes
            - Auto-reconnect: Yes (handled by browser)
            - Multiple tabs: Supported (each gets own connection)
            """)
    public SseEmitter streamNotifications(
            @RequestParam(value = "token", required = false) String token,
            HttpServletResponse response) throws IOException {

        // Try to get userId from SecurityContext first (if authenticated via header)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;

        // If authenticated via Spring Security (Authorization header)
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser")) {
            userId = authentication.getName();
            log.info("🔌 SSE connection via Authorization header | userId: {}", userId);
        }
        // If token provided as query parameter (EventSource workaround)
        else if (token != null && !token.isEmpty()) {
            try {
                // Validate and decode JWT token
                var jwt = jwtDecoder.decode(token);
                userId = jwt.getSubject(); // Get userId from token
                log.info("🔌 SSE connection via query parameter | userId: {}", userId);
            } catch (Exception e) {
                log.error("❌ Invalid token in SSE connection: {}", e.getMessage());
                // Send error response for SSE - don't throw exception
                if (!response.isCommitted()) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write("{\"error\":\"Invalid or expired token\"}");
                }
                return null;
            }
        }
        // No authentication provided
        else {
            log.error("❌ Unauthenticated SSE connection attempt - no token provided");
            // Send error response for SSE - don't throw exception
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\":\"Authentication required\"}");
            }
            return null;
        }

        return sseService.createConnection(userId);
    }
}
