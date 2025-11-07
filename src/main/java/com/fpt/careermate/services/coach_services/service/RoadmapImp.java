package com.fpt.careermate.services.coach_services.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.coach_services.domain.Roadmap;
import com.fpt.careermate.services.coach_services.domain.Subtopic;
import com.fpt.careermate.services.coach_services.domain.Topic;
import com.fpt.careermate.services.coach_services.repository.RoadmapRepo;
import com.fpt.careermate.services.coach_services.service.impl.RoadmapService;
import io.weaviate.client.WeaviateClient;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoadmapImp implements RoadmapService {

    WeaviateClient client;
    RoadmapRepo roadmapRepo;

    // Thêm roadmap vào Postgres
    @Transactional
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void addRoadmap(String nameRoadmap, String fileName) {
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
            roadmap.setName(nameRoadmap);

            // Duyệt từng dòng (record) trong file CSV
            records.forEach(record -> {
                // Lấy giá trị cột "topic" từ mỗi dòng
                String topic = record.get("topic");
                String subtopic = record.get("subtopic");
                String tags = record.get("tags");
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
        } catch (FileNotFoundException e) {
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        } catch (IOException e) {
            throw new AppException(ErrorCode.IO_EXCEPTION);
        }
    }
}