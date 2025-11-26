package com.fpt.careermate.services.coach_services.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.coach_services.domain.Roadmap;
import com.fpt.careermate.services.coach_services.domain.Subtopic;
import com.fpt.careermate.services.coach_services.domain.Topic;
import com.fpt.careermate.services.coach_services.repository.RoadmapRepo;
import com.fpt.careermate.services.coach_services.repository.SubtopicRepo;
import com.fpt.careermate.services.coach_services.repository.TopicRepo;
import com.fpt.careermate.services.coach_services.service.dto.response.*;
import com.fpt.careermate.services.coach_services.service.impl.RoadmapService;
import com.fpt.careermate.services.coach_services.service.mapper.RoadmapMapper;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.data.model.WeaviateObject;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoadmapImp implements RoadmapService {

    WeaviateClient client;
    RoadmapRepo roadmapRepo;
    TopicRepo topicRepo;
    SubtopicRepo subtopicRepo;
    RoadmapMapper roadmapMapper;

    // Thêm toàn bộ roadmap từ thư mục roadmap_data vào Postgres
    @Transactional
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void addAllRoadmaps() {
        String directoryPath = "../django/agent_core/data/craw/roadmap_data";
        File directory = new File(directoryPath);

        // Kiểm tra thư mục tồn tại
        if (!directory.exists() || !directory.isDirectory()) {
            log.error("Directory not found: {}", directoryPath);
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }

        // Lấy danh sách tất cả file CSV trong thư mục
        File[] csvFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));

        if (csvFiles == null || csvFiles.length == 0) {
            log.warn("No CSV files found in directory: {}", directoryPath);
            return;
        }

        log.info("Found {} CSV files to process", csvFiles.length);

        // Duyệt qua từng file CSV
        for (File csvFile : csvFiles) {
            try {
                // Trích xuất tên roadmap từ tên file
                // Ví dụ: "updated_frontend_developer.csv" -> "FRONTEND DEVELOPER"
                String fileName = csvFile.getName();
                String roadmapName = extractRoadmapName(fileName);

                log.info("Processing file: {} -> Roadmap name: {}", fileName, roadmapName);

                // Kiểm tra xem roadmap đã tồn tại chưa
                if (roadmapRepo.findByName(roadmapName).isPresent()) {
                    log.warn("Roadmap '{}' already exists, skipping...", roadmapName);
                    continue;
                }

                // Thêm roadmap từ file
                addRoadmapFromFile(roadmapName, csvFile.getAbsolutePath());
                log.info("Successfully added roadmap: {}", roadmapName);

            } catch (Exception e) {
                log.error("Error processing file: {}", csvFile.getName(), e);
                // Tiếp tục xử lý các file khác
            }
        }

        log.info("Finished processing all roadmap files");
    }

    // Trích xuất tên roadmap từ tên file
    private String extractRoadmapName(String fileName) {
        // Bỏ phần mở rộng .csv
        String nameWithoutExtension = fileName.replaceAll("\\.csv$", "");

        // Bỏ prefix "updated_" nếu có
        String cleanName = nameWithoutExtension.replaceFirst("^updated_", "");

        // Thay thế dấu gạch dưới bằng khoảng trắng và chuyển thành chữ hoa
        return cleanName.replace("_", " ").toUpperCase().trim();
    }

    // Thêm roadmap từ file path cụ thể
    protected void addRoadmapFromFile(String roadmapName, String filePath) {
        List<Topic> topics = new ArrayList<>();

        try {
            // Mở file CSV
            FileReader reader = new FileReader(filePath);

            // Parse file CSV
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            // Tạo đối tượng Roadmap
            Roadmap roadmap = new Roadmap();
            roadmap.setName(roadmapName);

            // Duyệt từng dòng trong file CSV
            records.forEach(csvRecord -> {
                String topic = csvRecord.get("topic");
                String subtopic = csvRecord.get("subtopic");
                String tags = csvRecord.get("tags") != null ? csvRecord.get("tags") : "";
                String resources = csvRecord.get("resources");
                String description = csvRecord.get("description");

                // Nếu là Topic (có topic nhưng không có subtopic)
                if (!topic.isEmpty() && subtopic.isEmpty()) {
                    Topic topicObj = new Topic(topic, tags, resources, description, roadmap);
                    topics.add(topicObj);
                }
                // Nếu là Subtopic (có cả topic và subtopic)
                else if (!topic.isEmpty() && !subtopic.isEmpty()) {
                    Subtopic subtopicObj = new Subtopic(subtopic, tags, resources, description);
                    // Tìm Topic tương ứng và thêm Subtopic vào
                    topics.forEach(t -> {
                        if (t.getName().equals(topic)) {
                            subtopicObj.setTopic(t);
                            t.getSubtopics().add(subtopicObj);
                        }
                    });
                }
            });

            // Thêm topics vào roadmap
            roadmap.setTopics(topics);

            // Lưu vào Postgres
            roadmapRepo.save(roadmap);

            // Thêm vào Weaviate
            addRoadmapToWeaviate(roadmapName);

        } catch (FileNotFoundException e) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        } catch (IOException e) {
            throw new AppException(ErrorCode.IO_EXCEPTION);
        }
    }

    // Thêm roadmap vào Postgres
    @Transactional
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void addRoadmap(String roadmapName, String fileName) {
        String filePath = "../django/agent_core/data/craw/"+fileName;
        List<Topic> topics = new ArrayList<>();
        // Lấy thư mục làm việc hiện tại (thư mục mà chương trình đang được chạy)
//        String dir = System.getProperty("user.dir");
//        log.info("Current working directory: {}", dir);
        try {
            // Mở file CSV theo đường dẫn đã truyền vào (filePath)
            FileReader reader = new FileReader(filePath);

            // Dùng Apache Commons CSV để parse file CSV:
            // - withFirstRecordAsHeader() giúp bỏ qua hàng tiêu đề (header)
            // - parse(reader) đọc toàn bộ file thành Iterable<CSVRecord>
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            //Tạo đối tượng Roadmap từ các giá trị lấy được
            Roadmap roadmap = new Roadmap();
            roadmap.setName(roadmapName.toUpperCase().trim());

            // Duyệt từng dòng (record) trong file CSV
            records.forEach(record -> {
                // Lấy giá trị cột "topic" từ mỗi dòng
                String topic = record.get("topic");
                String subtopic = record.get("subtopic");
                String tags = record.get("tags") != null ? record.get("tags") : "";
                String resources = record.get("resources");
                String description = record.get("description");

                // Kiểm tra nếu topic có và subtopic rỗng thì là Topic thì thêm vào bảng Topic
                if (!topic.isEmpty() && subtopic.isEmpty()) {
                    // Tạo đối tượng Topic từ các giá trị lấy được và thêm vào danh sách topics tạm thời
                    Topic topicObj = new Topic(topic, tags, resources, description, roadmap);
                    topics.add(topicObj);
                }
                // Kiểm tra nếu topic có và subtopic có thì là Subtopic
                else if (!topic.isEmpty() && !subtopic.isEmpty()) {
                    Subtopic subtopicObj = new Subtopic(subtopic, tags, resources, description);
                    // Tìm Topic tương ứng trong danh sách topics tạm thời
                    topics.forEach(t -> {
                                if (t.getName().equals(topic)) {
                                    // Thêm Subtopic vào Topic tìm được
                                    subtopicObj.setTopic(t);
                                    t.getSubtopics().add(subtopicObj);
                                }
                            }
                    );
                }
            });

            // Thêm topic vào Roadmap
            roadmap.setTopics(topics);
            // Thêm Roadmap vào Postgres
            roadmapRepo.save(roadmap);
            // Thêm Roadmap vào Weaviate
            addRoadmapToWeaviate(roadmapName);
        } catch (FileNotFoundException e) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        } catch (IOException e) {
            throw new AppException(ErrorCode.IO_EXCEPTION);
        }
    }

    // Lấy roadmap detail từ Postgres
    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public RoadmapResponse getRoadmap(String roadmapName) {
        Roadmap roadmap = roadmapRepo.findByName(roadmapName.toUpperCase().trim())
                .orElseThrow(() -> new AppException(ErrorCode.ROADMAP_NOT_FOUND));

        return roadmapMapper.toRoadmapResponse(roadmap);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public TopicDetailResponse getTopicDetail(int topicId) {
        // Kiểm tra topic tồn tại
        Topic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
        String[] urls = topic.getResources().split(",");
        List<ResourceResponse> resourceResponses = new ArrayList<>();

        TopicDetailResponse topicDetailResponse = roadmapMapper.topicDetailResponse(topic);

        // Map urls to ResourceResponse
        for (String url : urls) {
            ResourceResponse resourceResponse = new ResourceResponse(url.trim());
            resourceResponses.add(resourceResponse);
        }

        topicDetailResponse.setResourceResponses(resourceResponses);
        return topicDetailResponse;
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public TopicDetailResponse getSubtopicDetail(int subtopicId) {
        // Kiểm tra subtopic tồn tại
        Subtopic subtopic = subtopicRepo.findById(subtopicId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBTOPIC_NOT_FOUND));
        String[] urls = subtopic.getResources().split(",");
        List<ResourceResponse> resourceResponses = new ArrayList<>();

        TopicDetailResponse topicDetailResponse = roadmapMapper.toSubtopicDetailResponse(subtopic);

        // Map urls to ResourceResponse
        for (String url : urls) {
            ResourceResponse resourceResponse = new ResourceResponse(url.trim());
            resourceResponses.add(resourceResponse);
        }

        topicDetailResponse.setResourceResponses(resourceResponses);
        return topicDetailResponse;
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public List<RecommendedRoadmapResponse> recommendRoadmap(String role) {
        String collectionName = "Roadmap";

        // Tạo bộ lọc tìm kiếm gần theo văn bản (nearText)
        // "concepts" là mảng các từ khóa hoặc cụm từ dùng để tìm kiếm ngữ nghĩa
        // "certainty" là ngưỡng độ tin cậy tối thiểu của kết quả (0.7f = 70%)
        NearTextArgument nearText = NearTextArgument.builder()
                // vì SDK được sinh máy móc từ định nghĩa GraphQL, nên nó phản ánh y nguyên kiểu danh sách.
                .concepts(new String[]{role.toUpperCase().trim()})
                .certainty(0.71f)
                .build();

        // Xác định các trường cần lấy từ đối tượng "Roadmap" trong Weaviate
        // Bao gồm: "name" và "_additional.certainty" (độ tương tự)
        Fields fields = Fields.builder()
                .fields(new Field[]{
                        Field.builder().name("name").build(),
                        Field.builder().name("_additional").fields(new Field[]{
                                Field.builder().name("certainty").build()
                        }).build()
                })
                .build();

        // Tạo câu truy vấn GraphQL để lấy danh sách 3 roadmap liên quan nhất
        String query = GetBuilder.builder()
                .className(collectionName)
                .fields(fields)                 // các trường cần lấy
                .withNearTextFilter(nearText)   // áp dụng bộ lọc nearText
                .limit(3)
                .build()
                .buildQuery();

        // Gửi truy vấn GraphQL đến Weaviate và nhận kết quả trả về
        // tự viết câu truy vấn GraphQL dạng chuỗi (query)
        Result<GraphQLResponse> result = client.graphQL().raw().withQuery(query).run();
        GraphQLResponse graphQLResponse = result.getResult();

        // Trích xuất dữ liệu từ phản hồi GraphQL (ở dạng Map lồng nhau)
        Map<String, Object> data = (Map<String, Object>) graphQLResponse.getData();   // {Get={Roadmap=[{...}]}}
        Map<String, Object> get = (Map<String, Object>) data.get("Get");              // {Roadmap=[{...}]}
        List<Map<String, Object>> RoadmapData = (List<Map<String, Object>>) get.get(collectionName);

        // Chuyển từng phần tử trong danh sách sang đối tượng phản hồi (DTO)
        List<RecommendedRoadmapResponse> recommendedRoadmapResponseList = new ArrayList<>();
        RoadmapData.forEach(roadmap -> {
            String name = (String) roadmap.get("name");
            Map<String, Object> additional = (Map<String, Object>) roadmap.get("_additional");
            Double similarityScore = (Double) additional.get("certainty");

            // Thêm vào danh sách kết quả trả về
            recommendedRoadmapResponseList.add(new RecommendedRoadmapResponse(name, similarityScore));
        });

        // Trả về danh sách roadmap gợi ý
        return recommendedRoadmapResponseList;
    }

    private void addRoadmapToWeaviate(String name) {
        String weaviateClassName = "Roadmap";

        // Tạo roadmap map để thêm vào weaviate
        Map<String,Object> roadmapMap = new HashMap<>();
        roadmapMap.put("name", name.toUpperCase().trim());

        Result<WeaviateObject> result = client.data().creator()
                .withClassName(weaviateClassName)
                .withProperties(roadmapMap)
                .run();
    }

    // Reset Roadmap collection
    @PreAuthorize("hasRole('ADMIN')")
    public void resetRoadmapCollection() {
        String collectionName = "Roadmap";

        try {
            // Delete existing collection
            Result<Boolean> deleteResult = client.schema().classDeleter()
                    .withClassName(collectionName)
                    .run();

            if (deleteResult.hasErrors()) {
                log.warn("Could not delete existing collection: {}", deleteResult.getError().getMessages());
            }
            deleteRoadmapInPostgres();

            Thread.sleep(2000);

            // Create new collection
            createRoadmapCollection(collectionName);
            createRoadmapProperty(collectionName);
        } catch (Exception e) {
            log.error("Error resetting Roadmap collection: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private void createRoadmapCollection(String collectionName) {
        Map<String, Object> moduleConfig = new HashMap<>();
        Map<String, Object> text2vecHuggingface = new HashMap<>();
        text2vecHuggingface.put("vectorizeClassName", false);
        text2vecHuggingface.put("model", "sentence-transformers/all-MiniLM-L6-v2");
        moduleConfig.put("text2vec-huggingface", text2vecHuggingface);

        Map<String, Object> namePropertyConfig = new HashMap<>();
        Map<String, Object> nameText2vec = new HashMap<>();
        nameText2vec.put("skip", false);
        nameText2vec.put("vectorizePropertyName", false);
        namePropertyConfig.put("text2vec-huggingface", nameText2vec);

        WeaviateClass weaviateClass =
                io.weaviate.client.v1.schema.model.WeaviateClass.builder()
                .className(collectionName)
                .description("A collection of roadmap with title.")
                .vectorizer("text2vec-huggingface")
                .moduleConfig(moduleConfig)
                .build();

        Result<Boolean> result = client.schema().classCreator()
                .withClass(weaviateClass)
                .run();

        if (result.hasErrors()) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    private void createRoadmapProperty(String collectionName) {
        Property nameProperty = Property.builder()
                .name("name")
                .dataType(Collections.singletonList("text"))
                .description("The name of the roadmap")
                .moduleConfig(new HashMap<String, Object>() {{
                    put("text2vec-huggingface", new HashMap<String, Object>() {{
                        put("skip", false);
                        put("vectorizePropertyName", false);
                    }});
                }})
                .build();

        Result<Boolean> result = client.schema().propertyCreator()
                .withClassName(collectionName)
                .withProperty(nameProperty)
                .run();

        if (result.hasErrors()) {
            throw new AppException(ErrorCode.CANNOT_CREATE_ROADMAP_PROPERTY);
        }
    }

    private void deleteRoadmapInPostgres(){
        roadmapRepo.deleteAll();
    }
}

