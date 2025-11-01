package com.fpt.careermate.config;

import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.auth.exception.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.weaviate.client.Config;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class WeaviateConfig {

    @Value("${weaviate.url}")
    private String url;

    @Value("${weaviate.api-key}")
    private String apiKey;

    @Value("${google.api-key}")
    private String studio_key;

    @Bean
    public WeaviateClient weaviateClient() throws AuthException {
        String clusterUrl = "https://oei76mp3ttcpw5prggx3fq.c0.asia-southeast1.gcp.weaviate.cloud";

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Weaviate-Cluster-URL", clusterUrl);
        headers.put("X-Goog-Studio-Api-Key", studio_key);

        Config config = new Config("https", url, headers);
        WeaviateClient client = WeaviateAuthClient.apiKey(config, apiKey);
        return client;

    }

}
