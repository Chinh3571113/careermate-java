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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        Storage tempStorage = null;
        try {
            // Đọc service account từ classpath
            Resource resource = resourceLoader.getResource("classpath:careermate-bucket.json");

            if (resource.exists()) {
                try (InputStream serviceAccountStream = resource.getInputStream()) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountStream);

                    tempStorage = StorageOptions.newBuilder()
                            .setCredentials(credentials)
                            .build()
                            .getService();
                }
            } else {
                log.error("✗ careermate-bucket.json not found in classpath");
                log.info("Please ensure the file exists at: src/main/resources/careermate-bucket.json");
            }
        } catch (Exception e) {
            log.error("✗ Failed to initialize Google Cloud Storage: {}", e.getMessage());
            log.warn("Roadmap seeder will not run. Please check:");
            log.warn("1. File 'google-cloud-account.json' exists in src/main/resources/");
            log.warn("2. Service account has Storage Object Viewer role");
            log.warn("3. Service account has access to bucket");
        }
        this.storage = tempStorage;
        this.roadmapRepo = roadmapRepo;
        this.weaviateClient = weaviateClient;
    }

    @Override
    public void run(String... args) throws Exception {
        // Kiểm tra xem Storage có được khởi tạo thành công không
        if (storage == null) {
            log.error("Cannot run RoadmapSeeder: Google Cloud Storage is not initialized");
            return;
        }

        try {
            seedAllRoadmapsFromGcs(bucketName, prefix);
        } catch (StorageException e) {
            log.error("GCS Storage error [{}]: {}", e.getCode(), e.getMessage());
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
        List<String> csvFiles = new ArrayList<>();

        try {
            Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(prefix)).iterateAll();

            for (Blob blob : blobs) {
                String fileName = blob.getName();
                if (fileName.toLowerCase().endsWith(".csv") && !blob.isDirectory()) {
                    csvFiles.add(fileName);
                }
            }
        } catch (StorageException e) {
            log.error("Error listing files from bucket '{}': [{}] {}",
                bucketName, e.getCode(), e.getMessage());
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
        List<Map<String, String>> records = new ArrayList<>();

        BlobId blobId = BlobId.of(bucketName, objectName);
        Blob blob = storage.get(blobId);

        if (blob == null) {
            log.error("The object {} was not found in bucket {}", objectName, bucketName);
        }

        // Đọc tệp CSV từ GCS bằng InputStream
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Channels.newInputStream(blob.reader()), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord csvRecord : csvParser) {
                Map<String, String> record = new HashMap<>();
                csvRecord.toMap().forEach((key, value) -> record.put(key, value));
                records.add(record);
            }
        } catch (IOException e) {
            log.error("Error reading CSV from GCS: {}", e.getMessage());
        }

        return records;
    }

    // ============= SEEDER METHODS =============

    /**
     * Seed tất cả roadmap từ Google Cloud Storage vào Postgres và Weaviate
     */
    private void seedAllRoadmapsFromGcs(String bucketName, String prefix) {
        // Lấy danh sách tất cả file CSV từ GCS
        List<String> csvFiles = listCsvFiles(bucketName, prefix);

        if (csvFiles.isEmpty()) {
            log.warn("No CSV files found in GCS bucket: {}/{}", bucketName, prefix);
            return;
        }

        // Duyệt qua từng file CSV
        for (String csvFile : csvFiles) {
            try {
                // Trích xuất tên roadmap từ tên file
                String roadmapName = extractRoadmapName(csvFile);

                boolean existsInPostgres = roadmapRepo.findByName(roadmapName).isPresent();
                boolean existsInWeaviate = checkRoadmapExistsInWeaviate(roadmapName);

                // Case 1: Tồn tại ở cả 2 nơi -> Skip
                if (existsInPostgres && existsInWeaviate) {
                    continue;
                }

                // Case 2: Có trong Weaviate nhưng không có trong Postgres -> Thêm vào Postgres
                if (existsInWeaviate && !existsInPostgres) {
                    seedRoadmapToPostgresOnly(roadmapName, bucketName, csvFile);
                    continue;
                }

                // Case 3: Có trong Postgres nhưng không có trong Weaviate -> Thêm vào Weaviate
                if (existsInPostgres && !existsInWeaviate) {
                    addRoadmapToWeaviate(roadmapName);
                    continue;
                }

                // Case 4: Không có ở cả 2 nơi -> Thêm vào cả 2
                seedRoadmapFromGcs(roadmapName, bucketName, csvFile);
            } catch (Exception e) {
                log.error("✗ Error processing file: {}", csvFile, e);
                // Tiếp tục xử lý các file khác
            }
        }
    }

    /**
     * Seed roadmap chỉ vào Postgres (không thêm vào Weaviate)
     * Dùng khi roadmap đã tồn tại trong Weaviate
     */
    @Transactional
    public void seedRoadmapToPostgresOnly(String roadmapName, String bucketName, String objectName) throws IOException {
        List<Topic> topics = new ArrayList<>();

        // Đọc dữ liệu CSV từ GCS
        List<Map<String, String>> records = readCsvFromGcs(bucketName, objectName);

        if (records.isEmpty()) {
            log.warn("No records found in CSV file: {}", objectName);
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

        // Lưu vào Postgres
        roadmapRepo.save(roadmap);
    }

    /**
     * Seed một roadmap cụ thể từ Google Cloud Storage vào cả Postgres và Weaviate
     */
    @Transactional
    public void seedRoadmapFromGcs(String roadmapName, String bucketName, String objectName) throws IOException {
        List<Topic> topics = new ArrayList<>();

        // Đọc dữ liệu CSV từ GCS
        List<Map<String, String>> records = readCsvFromGcs(bucketName, objectName);

        if (records.isEmpty()) {
            log.warn("No records found in CSV file: {}", objectName);
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

        // Lưu vào Postgres
        roadmapRepo.save(roadmap);

        // Thêm vào Weaviate
        addRoadmapToWeaviate(roadmapName);
    }

    /**
     * Thêm roadmap vào Weaviate với kiểm tra trùng lặp
     */
    private void addRoadmapToWeaviate(String name) {
        String weaviateClassName = "Roadmap";

        // Kiểm tra xem roadmap đã tồn tại trong Weaviate chưa
        if (checkRoadmapExistsInWeaviate(name)) {
            log.warn("Roadmap '{}' already exists in Weaviate, skipping...", name);
            return;
        }

        // Tạo roadmap map để thêm vào weaviate
        Map<String, Object> roadmapMap = new HashMap<>();
        roadmapMap.put("name", name.toUpperCase().trim());

        Result<WeaviateObject> result = weaviateClient.data().creator()
                .withClassName(weaviateClassName)
                .withProperties(roadmapMap)
                .run();

        if (result.hasErrors()) {
            log.error("Error adding roadmap to Weaviate: {}", result.getError().getMessages());
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

            Result<GraphQLResponse> result = weaviateClient.graphQL().raw().withQuery(query).run();

            if (result.hasErrors()) {
                log.warn("Error checking roadmap existence in Weaviate: {}", result.getError().getMessages());
                return false;
            }

            GraphQLResponse response = result.getResult();
            Map<String, Object> data = (Map<String, Object>) response.getData();
            Map<String, Object> get = (Map<String, Object>) data.get("Get");
            List<Map<String, Object>> roadmaps = (List<Map<String, Object>>) get.get(weaviateClassName);

            return roadmaps != null && !roadmaps.isEmpty();
        } catch (Exception e) {
            log.error("Exception while checking roadmap existence in Weaviate: {}", e.getMessage());
            return false;
        }
    }
}
