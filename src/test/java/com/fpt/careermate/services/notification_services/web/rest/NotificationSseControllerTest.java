package com.fpt.careermate.services.notification_services.web.rest;

import com.fpt.careermate.config.CustomJwtDecoder;
import com.fpt.careermate.services.notification_services.service.NotificationSseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationSseController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("NotificationSseController Tests")
class NotificationSseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationSseService sseService;

    @MockBean
    private CustomJwtDecoder jwtDecoder;

    @Nested
    @DisplayName("GET /api/notifications/stream")
    class StreamNotificationsTests {

        @Test
        @DisplayName("Should establish SSE connection with valid token")
        void shouldEstablishSseConnectionWithValidToken() throws Exception {
            String token = "valid-jwt-token";
            String userId = "user123";
            
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "HS256");
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", userId);
            
            Jwt jwt = new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
            
            SseEmitter emitter = new SseEmitter(30000L);
            
            when(jwtDecoder.decode(token)).thenReturn(jwt);
            when(sseService.createConnection(userId)).thenReturn(emitter);

            mockMvc.perform(get("/api/notifications/stream")
                            .param("token", token)
                            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                    .andExpect(status().isOk());

            verify(jwtDecoder).decode(token);
            verify(sseService).createConnection(userId);
        }

        @Test
        @DisplayName("Should return unauthorized when no token provided")
        void shouldReturnUnauthorizedWhenNoTokenProvided() throws Exception {
            mockMvc.perform(get("/api/notifications/stream")
                            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                    .andExpect(status().isUnauthorized());

            verify(sseService, never()).createConnection(any());
        }

        @Test
        @DisplayName("Should return unauthorized when token is invalid")
        void shouldReturnUnauthorizedWhenTokenIsInvalid() throws Exception {
            String invalidToken = "invalid-token";
            
            when(jwtDecoder.decode(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

            mockMvc.perform(get("/api/notifications/stream")
                            .param("token", invalidToken)
                            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                    .andExpect(status().isUnauthorized());

            verify(sseService, never()).createConnection(any());
        }

        @Test
        @DisplayName("Should return unauthorized when token is empty")
        void shouldReturnUnauthorizedWhenTokenIsEmpty() throws Exception {
            mockMvc.perform(get("/api/notifications/stream")
                            .param("token", "")
                            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                    .andExpect(status().isUnauthorized());

            verify(sseService, never()).createConnection(any());
        }
    }
}
