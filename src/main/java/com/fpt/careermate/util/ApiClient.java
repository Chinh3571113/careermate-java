package com.fpt.careermate.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApiClient {

    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper objectMapper;

    public Map<String, Object> post(String url, String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isEmpty()) {
            headers.setBearerAuth(token);
        }

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            String respBody = response.getBody();
            if (respBody == null) {
                throw new AppException(ErrorCode.EXTERNAL_API_ERROR);
            }

            // deserialize into a map if needed
             Map<String, Object> res = objectMapper.readValue(respBody, new TypeReference<Map<String, Object>>() {});
            return res;

        } catch (Exception e) {
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
