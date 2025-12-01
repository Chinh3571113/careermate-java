package com.fpt.careermate.common.seeder;

import com.fpt.careermate.services.coach_services.domain.Roadmap;
import com.fpt.careermate.services.coach_services.domain.Subtopic;
import com.fpt.careermate.services.coach_services.domain.Topic;
import com.fpt.careermate.services.coach_services.repository.RoadmapRepo;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class RoadmapSeeder implements CommandLineRunner {

    private final Storage storage;
    private final RoadmapRepo roadmapRepo;
    private final WeaviateClient weaviateClient;

    @Value("${app.seeder.roadmap.bucket-name}")
    private String bucketName;

    @Value("${app.seeder.roadmap.prefix}")
    private String prefix;

    @Autowired
    public RoadmapSeeder(RoadmapRepo roadmapRepo,
                         WeaviateClient weaviateClient,
                         ResourceLoader resourceLoader) {
        log.info("=== RoadmapSeeder Constructor Started ===");

        // Log database environment variables
        log.info("→ Checking database environment variables:");
        log.info("  - DB_HOST: {}", System.getenv("DB_HOST") != null ? "SET" : "NOT SET");
        log.info("  - DB_PORT: {}", System.getenv("DB_PORT") != null ? "SET" : "NOT SET");
        log.info("  - DB_NAME: {}", System.getenv("DB_NAME") != null ? "SET" : "NOT SET");
        log.info("  - DB_USER_LOCAL: {}", System.getenv("DB_USER_LOCAL") != null ? "SET" : "NOT SET");
        log.info("  - DB_PASSWORD_LOCAL: {}", System.getenv("DB_PASSWORD_LOCAL") != null ? "SET" : "NOT SET");

        Storage tempStorage = null;
        try {
            GoogleCredentials credentials = null;

            // Priority 1: Try to get credentials from environment variable (for Cloud Run)
            String credentialsJson = System.getenv("GOOGLE_CLOUD_CREDENTIALS_JSON");

            if (credentialsJson != null && !credentialsJson.isEmpty()) {
                log.info("→ Using Google Cloud credentials from environment variable");
                log.info("→ Credentials JSON length: {} characters", credentialsJson.length());
                try (InputStream credentialStream = new ByteArrayInputStream(
                        credentialsJson.getBytes(StandardCharsets.UTF_8))) {
                    credentials = GoogleCredentials.fromStream(credentialStream);
                    log.info("✓ Successfully loaded credentials from environment variable");
                }
            }
            // Priority 2: Try to get credentials from Application Default Credentials (for Cloud Run)
            else {
                try {
                    log.info("→ Attempting to use Application Default Credentials");
                    credentials = GoogleCredentials.getApplicationDefault();
                    log.info("✓ Successfully loaded Application Default Credentials");
                } catch (IOException e) {
                    log.info("→ Application Default Credentials not available: {}", e.getMessage());
                    log.info("→ Trying local resource file");
                    // Priority 3: Fall back to local resource file
                    Resource resource = resourceLoader.getResource("classpath:careermate-bucket.json");

                    if (resource.exists()) {
                        log.info("→ Using Google Cloud credentials from local resource file");
                        try (InputStream serviceAccountStream = resource.getInputStream()) {
                            credentials = GoogleCredentials.fromStream(serviceAccountStream);
                            log.info("✓ Successfully loaded credentials from local file");
                        }
                    } else {
                        log.error("✗ careermate-bucket.json not found in classpath");
                        log.info("Please ensure the file exists at: src/main/resources/careermate-bucket.json");
                    }
                }
            }

            if (credentials != null) {
                log.info("→ Building Storage with credentials...");
                tempStorage = StorageOptions.newBuilder()
                        .setCredentials(credentials)
                        .build()
                        .getService();
                log.info("✓ Google Cloud Storage initialized successfully");
            } else {
                log.error("✗ No credentials available - Storage is NULL");
            }
        } catch (Exception e) {
            log.error("✗ Failed to initialize Google Cloud Storage: {}", e.getMessage(), e);
            log.warn("Roadmap seeder will not run. Please check:");
            log.warn("1. Environment variable GOOGLE_CLOUD_CREDENTIALS_JSON is set (for Cloud Run)");
            log.warn("2. OR Application Default Credentials are available");
            log.warn("3. OR File 'careermate-bucket.json' exists in src/main/resources/ (for local)");
            log.warn("4. Service account has Storage Object Viewer role");
            log.warn("5. Service account has access to bucket");
        }

        log.info("→ Assigning storage field...");
        this.storage = tempStorage;

        log.info("→ Checking injected dependencies...");
        log.info("  - RoadmapRepo injected: {}", (roadmapRepo != null ? "YES" : "NULL"));
        log.info("  - WeaviateClient injected: {}", (weaviateClient != null ? "YES" : "NULL"));

        try {
            log.info("→ Assigning roadmapRepo field...");
            this.roadmapRepo = roadmapRepo;
            log.info("✓ RoadmapRepo assigned successfully");
        } catch (Exception e) {
            log.error("✗ Failed to assign roadmapRepo: {}", e.getMessage(), e);
            throw e;
        }

        try {
            log.info("→ Assigning weaviateClient field...");
            this.weaviateClient = weaviateClient;
            log.info("✓ WeaviateClient assigned successfully");
        } catch (Exception e) {
            log.error("✗ Failed to assign weaviateClient: {}", e.getMessage(), e);
            throw e;
        }

        log.info("=== RoadmapSeeder initialized - Storage: {}, RoadmapRepo: {}, WeaviateClient: {} ===",
            (storage != null ? "OK" : "NULL"),
            (roadmapRepo != null ? "OK" : "NULL"),
            (weaviateClient != null ? "OK" : "NULL"));
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== RoadmapSeeder.run() started ===");
        log.info("Bucket name: {}", bucketName);
        log.info("Prefix: {}", prefix);

        // Kiểm tra xem Storage có được khởi tạo thành công không
        if (storage == null) {
            log.error("Cannot run RoadmapSeeder: Google Cloud Storage is not initialized");
            return;
        }

        try {
            log.info("Starting to seed roadmaps from GCS bucket: {}/{}", bucketName, prefix);
            seedAllRoadmapsFromGcs(bucketName, prefix);
            log.info("=== RoadmapSeeder.run() completed successfully ===");
        } catch (StorageException e) {
            log.error("GCS Storage error [{}]: {}", e.getCode(), e.getMessage(), e);
            log.info("Troubleshooting:");
            log.info("→ Verify service account has 'Storage Object Viewer' role");
            log.info("→ Verify bucket '{}' exists and is accessible", bucketName);
            log.info("→ Check if service account is enabled in GCP Console");
        } catch (Exception e) {
            log.error("Error running RoadmapSeeder: {}", e.getMessage(), e);
        }
    }

    // Lấy danh sách tất cả file CSV từ bucket
    private List<String> listCsvFiles(String bucketName, String prefix) {
        log.info("    → Listing files from bucket '{}' with prefix '{}'", bucketName, prefix);
        List<String> csvFiles = new ArrayList<>();

        try {
            Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();

            int totalBlobs = 0;
            for (Blob blob : blobs) {
                totalBlobs++;
                String fileName = blob.getName();
                log.debug("      → Found blob: {} (isDirectory: {})", fileName, blob.isDirectory());

                if (fileName.toLowerCase().endsWith(".csv") && !blob.isDirectory()) {
                    csvFiles.add(fileName);
                    log.info("      ✓ Added CSV file: {}", fileName);
                }
            }
            log.info("    → Total blobs: {}, CSV files: {}", totalBlobs, csvFiles.size());
        } catch (StorageException e) {
            log.error("    ✗ Error listing files from bucket '{}': [{}] {}",
                bucketName, e.getCode(), e.getMessage(), e);
            throw e;
        }

        return csvFiles;
    }

    // Trích xuất tên roadmap từ tên file
    private String extractRoadmapName(String fileName) {
        // Lấy tên file từ đường dẫn đầy đủ (nếu có prefix)
        String name = fileName.contains("/") ? fileName.substring(fileName.lastIndexOf("/") + 1) : fileName;

        // Bỏ phần mở rộng .csv
        String nameWithoutExtension = name.replaceAll("\\.csv$", "");

        // Bỏ prefix "updated_" nếu có
        String cleanName = nameWithoutExtension.replaceFirst("^updated_", "");

        // Thay thế dấu gạch dưới bằng khoảng trắng và chuyển thành chữ hoa
        return cleanName.replace("_", " ").toUpperCase().trim();
    }

    // Đọc CSV từ GCS và trả về dữ liệu có cấu trúc
    private List<Map<String, String>> readCsvFromGcs(String bucketName, String objectName) throws IOException {
        log.info("      → Reading CSV file: {}/{}", bucketName, objectName);
        List<Map<String, String>> records = new ArrayList<>();

        BlobId blobId = BlobId.of(bucketName, objectName);
        Blob blob = storage.get(blobId);

        if (blob == null) {
            log.error("      ✗ The object {} was not found in bucket {}", objectName, bucketName);
            throw new IOException("Object not found: " + objectName);
        }

        log.info("      → Blob found, size: {} bytes", blob.getSize());

        // Đọc tệp CSV từ GCS bằng InputStream
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Channels.newInputStream(blob.reader()), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                Map<String, String> record = new HashMap<>();
                csvRecord.toMap().forEach((key, value) -> record.put(key, value));
                records.add(record);
            }
            log.info("      ✓ Successfully read {} records from CSV", records.size());
        } catch (IOException e) {
            log.error("      ✗ Error reading CSV from GCS: {}", e.getMessage(), e);
            throw e;
        }

        return records;
    }

    // ============= SEEDER METHODS =============

    /**
     * Seed tất cả roadmap từ Google Cloud Storage vào Postgres và Weaviate
     */
    private void seedAllRoadmapsFromGcs(String bucketName, String prefix) {
        log.info("→ Listing CSV files from bucket: {}/{}", bucketName, prefix);

        // Lấy danh sách tất cả file CSV từ GCS
        List<String> csvFiles = listCsvFiles(bucketName, prefix);
        log.info("→ Found {} CSV files", csvFiles.size());

        if (csvFiles.isEmpty()) {
            log.warn("No CSV files found in GCS bucket: {}/{}", bucketName, prefix);
            return;
        }

        int processed = 0, skipped = 0, errors = 0;

        // Duyệt qua từng file CSV
        for (String csvFile : csvFiles) {
            try {
                // Trích xuất tên roadmap từ tên file
                String roadmapName = extractRoadmapName(csvFile);
                log.info("→ Processing file: {} -> Roadmap: {}", csvFile, roadmapName);

                boolean existsInPostgres = roadmapRepo.findByName(roadmapName).isPresent();
                boolean existsInWeaviate = checkRoadmapExistsInWeaviate(roadmapName);

                log.info("  Roadmap '{}' - Postgres: {}, Weaviate: {}",
                    roadmapName, existsInPostgres, existsInWeaviate);

                // Case 1: Tồn tại ở cả 2 nơi -> Skip
                if (existsInPostgres && existsInWeaviate) {
                    log.info("  ✓ Roadmap '{}' exists in both systems, skipping", roadmapName);
                    skipped++;
                    continue;
                }

                // Case 2: Có trong Weaviate nhưng không có trong Postgres -> Thêm vào Postgres
                if (existsInWeaviate && !existsInPostgres) {
                    log.info("  → Adding roadmap '{}' to Postgres only", roadmapName);
                    seedRoadmapToPostgresOnly(roadmapName, bucketName, csvFile);
                    log.info("  ✓ Successfully added roadmap '{}' to Postgres", roadmapName);
                    processed++;
                    continue;
                }

                // Case 3: Có trong Postgres nhưng không có trong Weaviate -> Thêm vào Weaviate
                if (existsInPostgres && !existsInWeaviate) {
                    log.info("  → Adding roadmap '{}' to Weaviate only", roadmapName);
                    addRoadmapToWeaviate(roadmapName);
                    log.info("  ✓ Successfully added roadmap '{}' to Weaviate", roadmapName);
                    processed++;
                    continue;
                }

                // Case 4: Không có ở cả 2 nơi -> Thêm vào cả 2
                log.info("  → Adding roadmap '{}' to both systems", roadmapName);
                seedRoadmapFromGcs(roadmapName, bucketName, csvFile);
                log.info("  ✓ Successfully added roadmap '{}' to both systems", roadmapName);
                processed++;

            } catch (Exception e) {
                log.error("✗ Error processing file '{}': {}", csvFile, e.getMessage(), e);
                errors++;
            }
        }

        log.info("=== Seeding Summary: Processed={}, Skipped={}, Errors={} ===",
            processed, skipped, errors);
    }

    /**
     * Seed roadmap chỉ vào Postgres (không thêm vào Weaviate)
     * Dùng khi roadmap đã tồn tại trong Weaviate
     */
    @Transactional
    public void seedRoadmapToPostgresOnly(String roadmapName, String bucketName, String objectName) throws IOException {
        log.info("    → Reading CSV from GCS: {}/{}", bucketName, objectName);
        List<Topic> topics = new ArrayList<>();

        // Đọc dữ liệu CSV từ GCS
        List<Map<String, String>> records = readCsvFromGcs(bucketName, objectName);
        log.info("    → Read {} records from CSV", records.size());

        if (records.isEmpty()) {
            log.warn("    ✗ No records found in CSV file: {}", objectName);
            return;
        }

        // Tạo đối tượng Roadmap
        Roadmap roadmap = new Roadmap();
        roadmap.setName(roadmapName.toUpperCase().trim());

        // Duyệt từng dòng trong CSV
        for (Map<String, String> record : records) {
            String topic = record.getOrDefault("topic", "").trim();
            String subtopic = record.getOrDefault("subtopic", "").trim();
            String tags = record.getOrDefault("tags", "").trim();
            String resources = record.getOrDefault("resources", "").trim();
            String description = record.getOrDefault("description", "").trim();

            // Nếu là Topic (có topic nhưng không có subtopic)
            if (!topic.isEmpty() && subtopic.isEmpty()) {
                Topic topicObj = new Topic(topic, tags, resources, description, roadmap);
                topics.add(topicObj);
            }
            // Nếu là Subtopic (có cả topic và subtopic)
            else if (!topic.isEmpty() && !subtopic.isEmpty()) {
                Subtopic subtopicObj = new Subtopic(subtopic, tags, resources, description);
                // Tìm Topic tương ứng và thêm Subtopic vào
                for (Topic t : topics) {
                    if (t.getName().equals(topic)) {
                        subtopicObj.setTopic(t);
                        t.getSubtopics().add(subtopicObj);
                        break;
                    }
                }
            }
        }

        // Thêm topics vào roadmap
        roadmap.setTopics(topics);
        log.info("    → Saving roadmap '{}' with {} topics to Postgres", roadmapName, topics.size());

        // Lưu vào Postgres
        try {
            Roadmap saved = roadmapRepo.save(roadmap);
            log.info("    ✓ Successfully saved roadmap '{}' with ID: {}", roadmapName, saved.getId());
        } catch (Exception e) {
            log.error("    ✗ Failed to save roadmap '{}' to Postgres: {}", roadmapName, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Seed một roadmap cụ thể từ Google Cloud Storage vào cả Postgres và Weaviate
     */
    @Transactional
    public void seedRoadmapFromGcs(String roadmapName, String bucketName, String objectName) throws IOException {
        log.info("    → Reading CSV from GCS: {}/{}", bucketName, objectName);
        List<Topic> topics = new ArrayList<>();

        // Đọc dữ liệu CSV từ GCS
        List<Map<String, String>> records = readCsvFromGcs(bucketName, objectName);
        log.info("    → Read {} records from CSV", records.size());

        if (records.isEmpty()) {
            log.warn("    ✗ No records found in CSV file: {}", objectName);
            return;
        }

        // Tạo đối tượng Roadmap
        Roadmap roadmap = new Roadmap();
        roadmap.setName(roadmapName.toUpperCase().trim());

        // Duyệt từng dòng trong CSV
        for (Map<String, String> record : records) {
            String topic = record.getOrDefault("topic", "").trim();
            String subtopic = record.getOrDefault("subtopic", "").trim();
            String tags = record.getOrDefault("tags", "").trim();
            String resources = record.getOrDefault("resources", "").trim();
            String description = record.getOrDefault("description", "").trim();

            // Nếu là Topic (có topic nhưng không có subtopic)
            if (!topic.isEmpty() && subtopic.isEmpty()) {
                Topic topicObj = new Topic(topic, tags, resources, description, roadmap);
                topics.add(topicObj);
            }
            // Nếu là Subtopic (có cả topic và subtopic)
            else if (!topic.isEmpty() && !subtopic.isEmpty()) {
                Subtopic subtopicObj = new Subtopic(subtopic, tags, resources, description);
                // Tìm Topic tương ứng và thêm Subtopic vào
                for (Topic t : topics) {
                    if (t.getName().equals(topic)) {
                        subtopicObj.setTopic(t);
                        t.getSubtopics().add(subtopicObj);
                        break;
                    }
                }
            }
        }

        // Thêm topics vào roadmap
        roadmap.setTopics(topics);
        log.info("    → Saving roadmap '{}' with {} topics to Postgres", roadmapName, topics.size());

        // Lưu vào Postgres
        try {
            Roadmap saved = roadmapRepo.save(roadmap);
            log.info("    ✓ Successfully saved roadmap '{}' with ID: {} to Postgres", roadmapName, saved.getId());
        } catch (Exception e) {
            log.error("    ✗ Failed to save roadmap '{}' to Postgres: {}", roadmapName, e.getMessage(), e);
            throw e;
        }

        // Thêm vào Weaviate
        log.info("    → Adding roadmap '{}' to Weaviate", roadmapName);
        try {
            addRoadmapToWeaviate(roadmapName);
            log.info("    ✓ Successfully added roadmap '{}' to Weaviate", roadmapName);
        } catch (Exception e) {
            log.error("    ✗ Failed to add roadmap '{}' to Weaviate: {}", roadmapName, e.getMessage(), e);
        }
    }

    /**
     * Thêm roadmap vào Weaviate với kiểm tra trùng lặp
     */
    private void addRoadmapToWeaviate(String name) {
        String weaviateClassName = "Roadmap";
        log.info("      → Checking if roadmap '{}' exists in Weaviate", name);

        // Kiểm tra xem roadmap đã tồn tại trong Weaviate chưa
        if (checkRoadmapExistsInWeaviate(name)) {
            log.warn("      ⚠ Roadmap '{}' already exists in Weaviate, skipping...", name);
            return;
        }

        // Tạo roadmap map để thêm vào weaviate
        Map<String, Object> roadmapMap = new HashMap<>();
        roadmapMap.put("name", name.toUpperCase().trim());

        log.info("      → Creating roadmap '{}' in Weaviate", name);
        Result<WeaviateObject> result = weaviateClient.data().creator()
                .withClassName(weaviateClassName)
                .withProperties(roadmapMap)
                .run();

        if (result.hasErrors()) {
            log.error("      ✗ Error adding roadmap '{}' to Weaviate: {}", name, result.getError().getMessages());
            throw new RuntimeException("Failed to add roadmap to Weaviate: " + result.getError().getMessages());
        } else {
            log.info("      ✓ Successfully created roadmap '{}' in Weaviate", name);
        }
    }

    /**
     * Kiểm tra roadmap có tồn tại trong Weaviate không
     */
    private boolean checkRoadmapExistsInWeaviate(String name) {
        String weaviateClassName = "Roadmap";
        String normalizedName = name.toUpperCase().trim();

        try {
            // Tạo câu truy vấn GraphQL để tìm roadmap với tên cụ thể
            String query = String.format(
                    "{ Get { %s(where: {path: [\"name\"], operator: Equal, valueText: \"%s\"}) { name } } }",
                    weaviateClassName, normalizedName
            );

            log.debug("        → Executing Weaviate query: {}", query);
            Result<GraphQLResponse> result = weaviateClient.graphQL().raw().withQuery(query).run();

            if (result.hasErrors()) {
                log.warn("        ⚠ Error checking roadmap '{}' existence in Weaviate: {}",
                    normalizedName, result.getError().getMessages());
                return false;
            }

            GraphQLResponse response = result.getResult();
            Map<String, Object> data = (Map<String, Object>) response.getData();
            Map<String, Object> get = (Map<String, Object>) data.get("Get");
            List<Map<String, Object>> roadmaps = (List<Map<String, Object>>) get.get(weaviateClassName);

            boolean exists = roadmaps != null && !roadmaps.isEmpty();
            log.debug("        → Roadmap '{}' exists in Weaviate: {}", normalizedName, exists);
            return exists;
        } catch (Exception e) {
            log.error("        ✗ Exception while checking roadmap '{}' existence in Weaviate: {}",
                normalizedName, e.getMessage(), e);
            return false;
        }
    }
}
