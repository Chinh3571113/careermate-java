package com.fpt.careermate.services.interview_services.web.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Test endpoint để kiểm tra Gemini AI connection
 * Xóa file này sau khi test xong
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestGeminiController {

    private final ChatClient chatClient;

    @Value("${spring.ai.openai.api-key:NOT_SET}")
    private String apiKey;

    @GetMapping("/gemini-status")
    public Map<String, Object> testGeminiConnection() {
        Map<String, Object> result = new HashMap<>();

        // Check if API key is set
        boolean isKeySet = apiKey != null && !apiKey.equals("default") && !apiKey.equals("NOT_SET");
        result.put("apiKeyConfigured", isKeySet);
        result.put("apiKeyPrefix", isKeySet ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "NOT_SET");

        // Try a simple API call
        try {
            String response = chatClient.prompt()
                    .user("Say 'Hello from Gemini!' in one line")
                    .call()
                    .content();

            result.put("connectionStatus", "SUCCESS");
            result.put("testResponse", response);
        } catch (Exception e) {
            result.put("connectionStatus", "FAILED");
            result.put("error", e.getMessage());
        }

        return result;
    }
}

