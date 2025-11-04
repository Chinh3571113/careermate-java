package com.fpt.careermate.services.coach_services.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.coach_services.domain.*;
import com.fpt.careermate.services.coach_services.domain.Module;
import com.fpt.careermate.services.coach_services.repository.CourseRepo;
import com.fpt.careermate.services.coach_services.repository.LessonRepo;
import com.fpt.careermate.services.coach_services.repository.QuestionRepo;
import com.fpt.careermate.services.coach_services.service.dto.response.*;
import com.fpt.careermate.services.coach_services.service.impl.CoachService;
import com.fpt.careermate.services.coach_services.service.mapper.CoachMapper;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.UnifiedJedis;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CoachImp implements CoachService {

    WeaviateClient client;

    @Override
    // Hàm gợi ý khóa học dựa trên vai trò (role) của người dùng
    public List<RecommendedCourseResponse> recommendCourse(String role) {

        // Tạo bộ lọc tìm kiếm gần theo văn bản (nearText)
        // "concepts" là mảng các từ khóa hoặc cụm từ dùng để tìm kiếm ngữ nghĩa
        // "certainty" là ngưỡng độ tin cậy tối thiểu của kết quả (0.7f = 70%)
        NearTextArgument nearText = NearTextArgument.builder()
                // vì SDK được sinh máy móc từ định nghĩa GraphQL, nên nó phản ánh y nguyên kiểu danh sách.
                .concepts(new String[]{role})
                .certainty(0.7f)
                .build();

        // Xác định các trường cần lấy từ đối tượng "Course" trong Weaviate
        // Bao gồm: "title" và "_additional.certainty" (độ tương tự)
        Fields fields = Fields.builder()
                .fields(new Field[]{
                        Field.builder().name("title").build(),
                        Field.builder().name("_additional").fields(new Field[]{
                                Field.builder().name("certainty").build()
                        }).build()
                })
                .build();

        // Tạo câu truy vấn GraphQL để lấy danh sách 5 khóa học liên quan nhất
        String query = GetBuilder.builder()
                .className("Course")
                .fields(fields)                 // các trường cần lấy
                .withNearTextFilter(nearText)   // áp dụng bộ lọc nearText
                .limit(5)
                .build()
                .buildQuery();

        // Gửi truy vấn GraphQL đến Weaviate và nhận kết quả trả về
        // tự viết câu truy vấn GraphQL dạng chuỗi (query)
        Result<GraphQLResponse> result = client.graphQL().raw().withQuery(query).run();
        GraphQLResponse graphQLResponse = result.getResult();

        // Trích xuất dữ liệu từ phản hồi GraphQL (ở dạng Map lồng nhau)
        Map<String, Object> data = (Map<String, Object>) graphQLResponse.getData();   // {Get={Course=[{...}]}}
        Map<String, Object> get = (Map<String, Object>) data.get("Get");              // {Course=[{...}]}
        List<Map<String, Object>> courseData = (List<Map<String, Object>>) get.get("Course");  // danh sách khóa học

        // Chuyển từng phần tử trong danh sách sang đối tượng phản hồi (DTO)
        List<RecommendedCourseResponse> recommendedCourseResponseList = new ArrayList<>();
        courseData.forEach(course -> {
            String title = (String) course.get("title");
            Map<String, Object> additional = (Map<String, Object>) course.get("_additional");
            Double similarityScore = (Double) additional.get("certainty");

            // Thêm vào danh sách kết quả trả về
            recommendedCourseResponseList.add(new RecommendedCourseResponse(title, similarityScore));
        });

        // Trả về danh sách khóa học gợi ý
        return recommendedCourseResponseList;
    }

}