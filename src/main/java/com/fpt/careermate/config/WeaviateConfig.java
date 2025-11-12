package com.fpt.careermate.config;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.WeaviateClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.weaviate.client.v1.auth.exception.AuthException;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class WeaviateConfig {

    @Value("${weaviate.url:http://localhost:8080}")
    private String weaviateUrl;

    @Value("${weaviate.api-key:}")
    private String apiKey;

    @Value("${google.api-key:}")
    private String googleKey;

    @Value("${huggingface.api-key:}")
    private String hfKey;

    @Value("${weaviate.vectorizer:text2vec-weaviate}")
    private String vectorizer;

    /**
     * Create Weaviate client with both local and cloud flexibility.
     */
    @Bean
    public WeaviateClient weaviateClient() throws AuthException {
        try {
            // Clean URL
            String cleanUrl = weaviateUrl.trim();

            // Determine scheme
            String scheme = (cleanUrl.startsWith("localhost") || cleanUrl.startsWith("127.0.0.1"))
                    ? "http"
                    : "https";

            // Remove existing prefix
            cleanUrl = cleanUrl.replaceFirst("^https?://", "");

            Map<String, String> headers = new HashMap<>();

            // Always add cluster header for cloud setups
            headers.put("X-Weaviate-Cluster-URL", scheme + "://" + cleanUrl);

            // Add optional API headers for hosted vectorizers or external integrations
            if (googleKey != null && !googleKey.isEmpty()) {
                headers.put("X-Goog-Studio-Api-Key", googleKey.trim());
            }
            if (hfKey != null && !hfKey.isEmpty()) {
                headers.put("X-HuggingFace-Api-Key", hfKey.trim());
            }

            // Authenticated connection
            if (apiKey != null && !apiKey.isEmpty()) {
                log.info("🔐 Using authenticated Weaviate connection");
                Config config = new Config(scheme, cleanUrl, headers);
                WeaviateClient client = WeaviateAuthClient.apiKey(config, apiKey.trim());
                log.info("✅ Weaviate client initialized (auth) at {}://{}", scheme, cleanUrl);
                return client;
            } else {
                // Non-authenticated connection (local dev)
                log.info("🔓 Using non-authenticated Weaviate connection");
                Config config = new Config(scheme, cleanUrl, headers);
                WeaviateClient client = new WeaviateClient(config);
                log.info("✅ Weaviate client initialized (no auth) at {}://{}", scheme, cleanUrl);
                return client;
            }

        } catch (Exception e) {
            log.error("❌ Failed to initialize Weaviate client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to connect to Weaviate", e);
        }
    }

    public String getVectorizer() {
        return vectorizer;
    }
}
