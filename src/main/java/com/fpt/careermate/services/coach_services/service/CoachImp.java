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
    String REDIS_URL = "redis://localhost:6379";

    CourseRepo courseRepo;
    CandidateRepo candidateRepo;
    LessonRepo lessonRepo;
    QuestionRepo questionRepo;
    AuthenticationImp authenticationImp;
    CoachMapper coachMapper;
    WeaviateClient client;
    ChatClient chatClient;
    ObjectMapper objectMapper;
    UnifiedJedis jedis = new UnifiedJedis(REDIS_URL); // Sử dụng Jedis làm client để kết nối Redis

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public CourseResponse generateCourse(String title) throws JsonProcessingException {
        // Kiểm tra nếu khóa học đã tồn tại trong postgres
        Candidate candidate = getCurrentCandidate();
        Optional<Course> exstingCourse =
                courseRepo.findByTitleAndCandidate_CandidateId(title, candidate.getCandidateId());
        if (exstingCourse.isPresent()) {

            log.info("Course already exists in Postgres for title: {}", title);
            return coachMapper.toCourseResponse(exstingCourse.get());
        }

        // Kiểm tra nếu khóa học chung đã có trong Redis cache
        boolean existsedRedisCourse = jedis.exists(title);
        if (existsedRedisCourse) {
            // Lưu khóa học vào postgres từ Redis
            Course savedPostgres = saveCourseFromJson(title, jedis.hget(title, "content"));
            log.info("Course found in Redis cache for title: {}", title);

            return coachMapper.toCourseResponse(savedPostgres);
        }

        // Nếu chưa tồn tại khóa học chung, tạo khóa học mới bằng cách gọi mô hình ngôn ngữ lớn (LLM)
        // Thiết lập vai trò, phong cách, quy tắc
        SystemMessage systemMessage = new SystemMessage("""
                You are an expert course generation assistant.
                """
        );

        // Đưa yêu cầu cụ thể
        UserMessage userMessage = new UserMessage(String.format("""
                Generate a comprehensive learning course for: "%s".
                The course should cover essential skills, tools, and knowledge areas needed to master the topic.
                Structure the course in the following JSON format:
                {{
                    "modules": [
                      {{
                        "position": <module position number, starting from 1>,
                        "title": "<module topic area, e.g., 'Asynchronous Programming'>",
                        "lessons": [
                          {{
                            "position": <lesson position number within the module, starting from 1>,
                            "title": "<lesson name>"
                          }},
                        ]
                      }}
                    ]
                }}
                
                Guide:
                - The course should be structured into 4-6 modules.
                - Each module should contain 3-5 lessons.
                - Do not change the course title.
                """, title)
        );

        // tạo prompt từ các message
        // "đóng gói” lời dặn + câu hỏi → thành 1 gói hoàn chỉnh
        Prompt prompt = new Prompt(systemMessage, userMessage);

        // gửi prompt đến model
        // chat client: Là đối tượng đại diện cho client kết nối đến mô hình ngôn ngữ. Mục đích:
        ChatResponse response = chatClient
                .prompt(prompt)
                .call() // Bước thực thi
                .chatResponse(); // Lấy về đối tượng phản hồi dạng chat

        // lấy content và token usage
        String content = response.getResult().getOutput().getText();
        long totalTokens = response.getMetadata().getUsage().getTotalTokens();
        long completionTokens = response.getMetadata().getUsage().getCompletionTokens();
        long promptTokens = response.getMetadata().getUsage().getPromptTokens();
        log.info("Generated course with {} totalTokens", totalTokens);
        log.info("Generated course with {} completionTokens", completionTokens);
        log.info("Generated course with {} promptTokens", promptTokens);

        // Loại bỏ các ký tự không cần thiết để có được chuỗi JSON hợp lệ
        String jsonString = content.trim();
        jsonString = jsonString.replace("```json", "");
        jsonString = jsonString.replace("```", "").trim();

        // Save to Redis cache
        Map<String, String> courseMap = new HashMap<>();
        courseMap.put("content", jsonString);
        jedis.hset(title, courseMap);
//        jedis.expire(title, 86400); // set expiration time to 24 hours


        // Lưu khóa học vào database
        Course savedPostgres = saveCourseFromJson(title, jsonString);

        // Map to response
        return coachMapper.toCourseResponse(savedPostgres);
    }

    private Course saveCourseFromJson(String title, String jsonString) throws JsonProcessingException {
        // Chuyển chuỗi JSON thành đối tượng Course
        // ObjectMapper là cây cầu giữa JSON và Java object.
        JsonNode root = objectMapper.readTree(jsonString);
        // readTree chuyển chuỗi JSON thành JsonNode (cây JSON)
        // Tạo đối tượng Course từ JsonNode
        Course course = new Course();
        course.setTitle(title);
        course.setCreatedAt(LocalDate.now());
        // Gán candidate hiện tại cho course
        course.setCandidate(getCurrentCandidate());

        // Thêm modules và lessons vào course
        for (JsonNode moduleNode : root.get("modules")) {
            Module module = new Module();
            module.setTitle(moduleNode.get("title").asText());
            module.setPosition(moduleNode.get("position").asInt());
            // tạo liên kết ngược để Hibernate biết rằng module này nằm trong course nào
            module.setCourse(course);

            // Thêm lessons vào module
            for (JsonNode lessonNode : moduleNode.get("lessons")) {
                Lesson lesson = new Lesson();
                lesson.setTitle(lessonNode.get("title").asText());
                lesson.setPosition(lessonNode.get("position").asInt());
                // tạo liên kết ngược để Hibernate biết rằng lesson này nằm trong module nào
                lesson.setModule(module);
                // Thêm lesson vào module
                module.getLessons().add(lesson);
            }
            // Thêm module vào course
            course.getModules().add(module);
        }
        // Save to database
        Course saved = courseRepo.save(course);
        return saved;
    }

    // Generate lesson content for course
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public LessonContentResponse generateLesson(int lessonId) throws JsonProcessingException {
        // Kiểm tra nếu lesson không tồn tại trong postgres vì có thể truyền lessonId không hợp lệ
        Optional<Lesson> exstingLesson = lessonRepo.findById(lessonId);
        if (exstingLesson.isEmpty()) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }
        Lesson lesson = exstingLesson.get();

        // Check if lesson content already exists in Postgres
        if (lesson.getCoreContent() != null && !lesson.getCoreContent().isEmpty()) {
            log.info("Lesson content already exists in Postgres for lesson ID: {}", lessonId);
            return coachMapper.toLessonContentResponse(lesson);
        }

        // Kiểm tra nếu lesson content chung đã có trong Redis cache
        boolean existsedRedisLesson = jedis.exists(lesson.getTitle());
        if (existsedRedisLesson) {
            // Lưu nội dung lesson vào postgres từ Redis
            String jsonString = jedis.hget(lesson.getTitle(), "content");
            log.info("Lesson content found in Redis cache for lesson title: {}", lesson.getTitle());
            Lesson savedPostgres = saveLessonContentFromJson(lesson, jsonString);
            return coachMapper.toLessonContentResponse(savedPostgres);
        }

        // Nếu chưa tồn tại nội dung của lesson, tạo nội dung lesson mới bằng cách gọi mô hình ngôn ngữ lớn (LLM)
        // Thiết lập vai trò, phong cách, quy tắc
        SystemMessage systemMessage = new SystemMessage("""
                You are an expert lesson content generation assistant.
                """
        );

        // Đưa yêu cầu cụ thể
        UserMessage userMessage = new UserMessage(String.format("""
                Generate a comprehensive learning description for the lesson: "%s".
                Structure the course in the following JSON format:
                {{
                     "lesson_overview": "<brief overview of the lesson>",
                     "core_content": "<detailed lesson content with 150 words>",
                     "exercise": "<a exercise related to the lesson>" (type is String),
                     "conclusion" "<summary of the lesson>"
                }}
                """, lesson.getTitle())
        );

        // tạo prompt từ các message
        // "đóng gói” lời dặn + câu hỏi → thành 1 gói hoàn chỉnh
        Prompt prompt = new Prompt(systemMessage, userMessage);

        // gửi prompt đến model
        // chat client: Là đối tượng đại diện cho client kết nối đến mô hình ngôn ngữ. Mục đích:
        ChatResponse response = chatClient
                .prompt(prompt)
                .call() // Bước thực thi
                .chatResponse(); // Lấy về đối tượng phản hồi dạng chat

        // lấy content và token usage
        String content = response.getResult().getOutput().getText();
        long totalTokens = response.getMetadata().getUsage().getTotalTokens();
        long completionTokens = response.getMetadata().getUsage().getCompletionTokens();
        long promptTokens = response.getMetadata().getUsage().getPromptTokens();
        log.info("Generated lesson content with {} totalTokens", totalTokens);
        log.info("Generated lesson content with {} completionTokens", completionTokens);
        log.info("Generated lesson content with {} promptTokens", promptTokens);

        // Loại bỏ các ký tự không cần thiết để có được chuỗi JSON hợp lệ
        String jsonString = content.trim();
        jsonString = jsonString.replace("```json", "");
        jsonString = jsonString.replace("```", "").trim();

        // Save to Redis cache
        Map<String, String> lessonContentMap = new HashMap<>();
        lessonContentMap.put("content", jsonString);
        jedis.hset(lesson.getTitle(), lessonContentMap);
//        jedis.expire(title, 86400); // set expiration time to 24 hours


        // Parse JSON and set lesson content
        Lesson savedPostgres = saveLessonContentFromJson(lesson, jsonString);

        return coachMapper.toLessonContentResponse(savedPostgres);
    }

    private Lesson saveLessonContentFromJson(Lesson lesson, String jsonString) throws JsonProcessingException {
        // Parse JSON and set lesson content
        JsonNode root = objectMapper.readTree(jsonString);
        lesson.setLessonOverview(root.get("lesson_overview").asText());
        lesson.setCoreContent(root.get("core_content").asText());
        lesson.setExercise(root.get("exercise").asText());
        lesson.setConclusion(root.get("conclusion").asText());
        return lessonRepo.save(lesson);
    }

    // Get my courses
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public List<CourseListResponse> getMyCourses() {
        Candidate candidate = getCurrentCandidate();
        Optional<List<Course>> exstingCourses =
                courseRepo.findByCandidate_CandidateId(candidate.getCandidateId());
        List<Course> courses = exstingCourses.get();

        // Map to response
        List<CourseListResponse> responses = new ArrayList<>();
        courses.forEach(exstingCourse -> {
            CourseListResponse response = new CourseListResponse();
            response.setTitle(exstingCourse.getTitle());
            response.setId(exstingCourse.getId());
            response.setModuleCount(exstingCourse.getModules().size());
            // Count total lessons
            int totalLessons = exstingCourse.getModules()
                    .stream()
                    .mapToInt(m -> m.getLessons().size())
                    .sum();
            response.setLessonCount(totalLessons);
            // Count completed lessons
            long completedLessons = exstingCourse.getModules()
                    .stream()
                    .flatMap(m -> m.getLessons().stream())
                    .filter(Lesson::isMarked)
                    .count();
            double completion = totalLessons == 0 ? 0 : (completedLessons * 100.0 / totalLessons);

            response.setCompletion(Math.round(completion) + "%");

            responses.add(response);
        });

        return responses;
    }

    // Mark lesson as completed
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public void markLesson(int lessonId, boolean marked) {
        // Check if lesson exists
        Optional<Lesson> exstingLesson = lessonRepo.findById(lessonId);
        if (exstingLesson.isEmpty()) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }

        Lesson lesson = exstingLesson.get();
        lesson.setMarked(marked);
        lessonRepo.save(lesson);
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public List<QuestionResponse> generateQuestionList(int lessonId) throws JsonProcessingException {
        // Check if lesson exists
        Optional<Lesson> exstingLesson = lessonRepo.findById(lessonId);
        if (exstingLesson.isEmpty()) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }
        Lesson lesson = exstingLesson.get();

        // Check if questions already exist for the lesson
        List<Question> existingQuestions = questionRepo.findByLesson_Id(lessonId);
        if (!existingQuestions.isEmpty()) {
            log.info("Questions already exist for lesson ID: {}", lessonId);
            return coachMapper.toQuestionResponseList(existingQuestions);
        }

        // KIểm tra nếu question list chung đã có trong Redis cache
        boolean existsedRedisQuestion = jedis.exists("question:" + lesson.getTitle());
        if (existsedRedisQuestion) {
            // Lưu question list vào postgres từ Redis
            String jsonString = jedis.hget("question:" + lesson.getTitle(), "content");
            log.info("Question list found in Redis cache for lesson title: {}", lesson.getTitle());
            // Parse JSON and save questions to Postgres
            List<Question> savedQuestions = saveQuestionFromJson(lesson, jsonString);
            return coachMapper.toQuestionResponseList(savedQuestions);
        }

        // Nếu chưa tồn tại question list chung, tạo question list mới bằng cách gọi mô hình ngôn ngữ lớn (LLM)
        // Thiết lập vai trò, phong cách, quy tắc
        SystemMessage systemMessage = new SystemMessage("""
                You are an expert question generation assistant.
                """
        );

        // Đưa yêu cầu cụ thể
        UserMessage userMessage = new UserMessage(String.format("""
                Generate 3-5 question for the lesson: "%s".
                Each question should include 4 option and 1 is correct option.:
                Structure the questions in the following JSON format:
                {
                  "questions": [
                    {{
                      "question": "<question text>",
                      "options": [
                        {"label": "A","option": "<option 1>"},
                        {"label": "B","option": "<option 2>"},
                        {"label": "C","option": "<option 3>"},
                        {"label": "D","option": "<option 4>"}
                      ],
                      "correct_option": "A/B/C/D",
                      "explanation": "<explanation for the correct answer, why it is correct and why others are wrong
                    }}
                  ]
                }
                """, lesson.getTitle())
        );

        // tạo prompt từ các message
        // "đóng gói” lời dặn + câu hỏi → thành 1 gói hoàn chỉnh
        Prompt prompt = new Prompt(systemMessage, userMessage);

        // gửi prompt đến model
        // chat client: Là đối tượng đại diện cho client kết nối đến mô hình ngôn ngữ. Mục đích:
        ChatResponse response = chatClient
                .prompt(prompt)
                .call() // Bước thực thi
                .chatResponse(); // Lấy về đối tượng phản hồi dạng chat

        // lấy content và token usage
        String content = response.getResult().getOutput().getText();
        long totalTokens = response.getMetadata().getUsage().getTotalTokens();
        long completionTokens = response.getMetadata().getUsage().getCompletionTokens();
        long promptTokens = response.getMetadata().getUsage().getPromptTokens();
        log.info("Generated course with {} totalTokens", totalTokens);
        log.info("Generated course with {} completionTokens", completionTokens);
        log.info("Generated course with {} promptTokens", promptTokens);

        // Loại bỏ các ký tự không cần thiết để có được chuỗi JSON hợp lệ
        String jsonString = content.trim();
        jsonString = jsonString.replace("```json", "");
        jsonString = jsonString.replace("```", "").trim();

        // Save to Redis cache
        Map<String, String> questionMap = new HashMap<>();
        questionMap.put("content", jsonString);
        jedis.hset("question:" + lesson.getTitle(), questionMap);
//        jedis.expire(title, 86400); // set expiration time to 24 hours

        // Parse JSON and save questions to Postgres
        List<Question> savedQuestions = saveQuestionFromJson(lesson, jsonString);

        return coachMapper.toQuestionResponseList(savedQuestions);
    }

    private List<Question> saveQuestionFromJson(Lesson lesson, String jsonString) throws JsonProcessingException {
        // Parse JSON and save questions to Postgres
        List<Question> savedQuestions = new ArrayList<>();
        JsonNode root = objectMapper.readTree(jsonString);
        for (JsonNode questionNode : root.get("questions")) {
            Question question = new Question();
            question.setTitle(questionNode.get("question").asText());
            question.setExplanation(questionNode.get("explanation").asText());
            question.setLesson(lesson);

            // Parse options
            List<Option> options = new ArrayList<>();
            for (JsonNode optionNode : questionNode.get("options")) {
                Option option = new Option();
                option.setLabel(optionNode.get("label").asText());
                option.setContent(optionNode.get("option").asText());
                option.setQuestion(question);

                // Save correct option
                if (option.getLabel().equals(questionNode.get("correct_option").asText())) {
                    question.setCorrectOption(option);
                }

                options.add(option);
            }
            question.setOptions(options);

            // Save question to Postgres
            Question savedQuestion = questionRepo.save(question);
            savedQuestions.add(savedQuestion);
        }

        return savedQuestions;
    }

    private Candidate getCurrentCandidate() {
        Account account = authenticationImp.findByEmail();
        Optional<Candidate> exsting = candidateRepo.findByAccount_Id(account.getId());
        return exsting.get();
    }

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

    @PreDestroy
    public void shutdown() {
        log.info("Closing Redis connection...");
        jedis.close(); // chỉ đóng Redis khi app tắt
    }

}