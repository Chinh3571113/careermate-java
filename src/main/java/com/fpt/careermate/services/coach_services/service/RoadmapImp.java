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

