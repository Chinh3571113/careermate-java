package com.fpt.careermate.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Firebase configuration class to initialize Firebase Admin SDK
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

                InputStream serviceAccount;
                if (credentialsPath != null) {
                    // Use environment variable path
                    serviceAccount = new FileInputStream(credentialsPath);
                    log.info("Using Firebase credentials from: {}", credentialsPath);
                } else {
                    // Fallback to classpath
                    ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                    if (!resource.exists()) {
                        log.error(
                                "❌ Firebase service account file not found! Please add firebase-service-account.json to src/main/resources/");
                        throw new RuntimeException(
                                "Firebase service account file not found: firebase-service-account.json");
                    }
                    serviceAccount = resource.getInputStream();
                    log.info("Using Firebase credentials from classpath");
                }

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId("careermate-97d8c")
                        .setStorageBucket("careermate-97d8c.firebasestorage.app")
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase initialized successfully for project: careermate-97d8c");

            } catch (Exception e) {
                log.error("❌ Firebase initialization failed: {}", e.getMessage());
                throw new RuntimeException("Firebase initialization failed", e);
            }
        } else {
            log.info("Firebase is already initialized");
        }
    }
}