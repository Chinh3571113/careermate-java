package com.fpt.careermate.config;

import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.WeaviateClient;
import lombok.extern.slf4j.Slf4j;
import io.weaviate.client.v1.auth.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class WeaviateConfig {

    @Value("${weaviate.url:http://localhost:8080}")
    private String weaviateUrl;

    @Value("${weaviate.api-key:}")
    private String apiKey;

    @Value("${weaviate.vectorizer:text2vec-weaviate}")
    private String vectorizer;

    @Value("${google.api-key}")
    private String studio_key;

    @Value("${huggingface.api-key}")
    private String hf_key;

    @Bean
    public WeaviateClient weaviateClient() {
        try {
            // Trim any whitespace from URL
            String cleanUrl = weaviateUrl.trim();

            // Determine scheme based on URL
            String scheme = cleanUrl.startsWith("localhost") || cleanUrl.startsWith("127.0.0.1")
                ? "http"
                : "https";

            String clusterUrl = "https://oei76mp3ttcpw5prggx3fq.c0.asia-southeast1.gcp.weaviate.cloud";

            // Remove any protocol prefix if present
            cleanUrl = cleanUrl.replaceFirst("^https?://", "");

            Config config;
            Map<String, String> headers = new HashMap<>();


        headers.put("X-Weaviate-Cluster-URL", clusterUrl);
        headers.put("X-Goog-Studio-Api-Key", studio_key);
        headers.put("X-HuggingFace-Api-Key", hf_key);

            if (apiKey != null && !apiKey.isEmpty()) {
                String cleanApiKey = apiKey.trim();
                headers.put("Authorization", "Bearer " + cleanApiKey);
                log.info("🔐 Using authenticated Weaviate connection");
            } else {
                log.info("🔓 Using non-authenticated Weaviate connection");
            }

            // Add cluster URL header for Weaviate Cloud hosted vectorization
            // This is required for text2vec-weaviate vectorizer
            headers.put("X-Weaviate-Cluster-Url", scheme + "://" + cleanUrl);
            log.info("🌐 Adding cluster URL header for hosted vectorization: {}://{}", scheme, cleanUrl);

            config = new Config(scheme, cleanUrl, headers);

            WeaviateClient client = new WeaviateClient(config);
            log.info("✅ Weaviate client initialized successfully at {}://{}", scheme, cleanUrl);
            return client;
        } catch (Exception e) {
            log.error("❌ Failed to initialize Weaviate client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to connect to Weaviate", e);
        }
    }

    public String getVectorizer() {
        return vectorizer;
    }
}
