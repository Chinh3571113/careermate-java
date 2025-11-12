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
        String[] target_vector = {"name_vector"};

        // Tạo bộ lọc tìm kiếm gần theo văn bản (nearText)
        // "concepts" là mảng các từ khóa hoặc cụm từ dùng để tìm kiếm ngữ nghĩa
        // "certainty" là ngưỡng độ tin cậy tối thiểu của kết quả (0.7f = 70%)
        NearTextArgument nearText = NearTextArgument.builder()
                // vì SDK được sinh máy móc từ định nghĩa GraphQL, nên nó phản ánh y nguyên kiểu danh sách.
                .concepts(new String[]{role.toUpperCase().trim()})
                .certainty(0.71f)
                .targetVectors(target_vector) // Sử dụng trường vector tùy chỉnh
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

}