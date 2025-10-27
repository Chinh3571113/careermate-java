package com.fpt.careermate.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApiClient {

    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper;

    public Map<String,Object> post(String url, String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isEmpty()) headers.setBearerAuth(token);

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            // Log status + headers
            log.info("External API status: {}, headers: {}", response.getStatusCode(), response.getHeaders());
            String respBody = response.getBody();

            // fallback: if null, try a direct execute to read raw stream (rare cases)
            if (respBody == null) {
                log.warn("Response body null from exchange; trying direct execute to read stream.");
                respBody = restTemplate.execute(url, HttpMethod.POST, clientHttpRequest -> {
                    clientHttpRequest.getHeaders().putAll(headers);
                    byte[] bytes = jsonBody.getBytes(StandardCharsets.UTF_8);
                    clientHttpRequest.getBody().write(bytes);
                }, clientHttpResponse -> {
                    InputStream in = clientHttpResponse.getBody();
                    return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                });
                log.info("Raw body from execute(): {}", respBody);
            }

            if (respBody == null || respBody.isBlank()) {
                throw new AppException(ErrorCode.RESPONSE_BODY_EMPTY);
            }

            Map<String, Object> res = objectMapper.readValue(respBody, new TypeReference<>() {});
            return res;

        } catch (AppException ae) {
            throw ae;
        } catch (Exception e) {
            log.error("External API error", e);
            // optional: retry once after short sleep
            throw new AppException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    public String getToken(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        if (auth instanceof JwtAuthenticationToken jwt) return jwt.getToken().getTokenValue();
        return null;
    }
}
