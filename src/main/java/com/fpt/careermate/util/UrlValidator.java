package com.fpt.careermate.util;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class UrlValidator {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean isWebsiteReachable(String websiteUrl) {
        if (websiteUrl == null || websiteUrl.isBlank()) return false;

        try {
            URI uri = URI.create(websiteUrl);
            if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))) return false;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(3))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            int code = response.statusCode();
            return (200 <= code && code < 400);

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isImageUrlValid(String logoUrl) {
        if (logoUrl == null || logoUrl.isBlank()) return false;

        try {
            URI uri = URI.create(logoUrl);
            if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))) return false;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .method("HEAD", HttpRequest.BodyPublishers.noBody()) // HEAD: get header, not include body
                    .timeout(Duration.ofSeconds(3))
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            int code = response.statusCode();
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            return (200 <= code && code < 400) && contentType.startsWith("image/");

        } catch (Exception e) {
            return false;
        }
    }

}
