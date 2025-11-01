package com.fpt.careermate.services.coach_services.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.util.ApiClient;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.coach_services.domain.*;
import com.fpt.careermate.services.coach_services.domain.Module;
import com.fpt.careermate.services.coach_services.repository.CourseRepo;
import com.fpt.careermate.services.coach_services.repository.LessonRepo;
import com.fpt.careermate.services.coach_services.repository.QuestionRepo;
import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
import com.fpt.careermate.services.coach_services.service.dto.response.CourseListResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.CourseResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.QuestionResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.RecommendedCourseResponse;
import com.fpt.careermate.services.coach_services.service.impl.CoachService;
import com.fpt.careermate.services.coach_services.service.mapper.CoachMapper;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.management.monitor.StringMonitor;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CoachImp implements CoachService {
    String BASE_URL = "http://localhost:8000/api/v1/coach/";

    ApiClient apiClient;
    CourseRepo courseRepo;
    CandidateRepo candidateRepo;
    LessonRepo lessonRepo;
    QuestionRepo questionRepo;
    AuthenticationImp authenticationImp;
    CoachMapper coachMapper;
    WeaviateClient client;


    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public CourseResponse generateCourse(CourseCreationRequest request) {
        String url = BASE_URL + "generate-course/";
        Map<String, String> body = Map.of(
                "title", request.getTitle(),
                "description", request.getDescription()
        );

        // Call Django API
        Map<String, Object> data = apiClient.post(url, apiClient.getToken(), body);

        // Save to database
        Course course = new Course();
        course.setTitle((String) data.get("title"));
        course.setDescription((String) data.get("description"));
        course.setTags((String) data.get("tags"));
        course.setCreatedAt(LocalDate.now());

        // Populate modules and lessons
        List<Map<String, Object>> modulesData = (List<Map<String, Object>>) data.get("modules");
        for (Map<String, Object> moduleData : modulesData) {
            Module module = new Module();
            module.setTitle((String) moduleData.get("title"));
            module.setPosition((Integer) moduleData.get("position"));
            module.setCourse(course);

            List<Map<String, Object>> lessonsData = (List<Map<String, Object>>) moduleData.get("lessons");
            for (Map<String, Object> lessonData : lessonsData) {
                Lesson lesson = new Lesson();
                lesson.setTitle((String) lessonData.get("title"));
                lesson.setPosition((Integer) lessonData.get("position"));
                lesson.setModule(module);
                module.getLessons().add(lesson);
            }

            course.getModules().add(module);
        }

        // Set Candidate
        course.setCandidate(getCurrentCandidate());

        return coachMapper.toCourseResponse(courseRepo.save(course));
    }

    // Generate lesson for course
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public String generateLesson(int lessonId) throws JsonProcessingException {
        String url = BASE_URL + "generate-course/lesson";

        // Check if lesson exists
        Optional<Lesson> exstingLesson = lessonRepo.findById(lessonId);
        if (exstingLesson.isEmpty()) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }

        // Check if lesson content already exists
        Lesson lesson = exstingLesson.get();
        if(lesson.getContent() != null && !lesson.getContent().isEmpty()) {
            return  lesson.getContent();
        }

        // If lesson content is empty, call API to generate content
        Map<String, String> body = Map.of("lesson", lesson.getTitle());

        // Call Django API
        Map<String, Object> data = apiClient.post(url, apiClient.getToken(), body);

        // Save to database
        Object contentObj = data.get("content");
        if (contentObj instanceof String) {
            lesson.setContent((String) contentObj);
        } else {
            // fallback: convert object to JSON text
            String contentJson = new ObjectMapper().writeValueAsString(contentObj);
            lesson.setContent(contentJson);
        }

        return lessonRepo.save(lesson).getContent();
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
    public CourseResponse getCourseById(int courseId) {
        Candidate candidate = getCurrentCandidate();
        Optional<Course> exstingCourse =
                courseRepo.findByIdAndCandidate_CandidateId(courseId, candidate.getCandidateId());
        if (exstingCourse.isEmpty()) {
            throw new AppException(ErrorCode.COURSE_NOT_FOUND);
        }

        return coachMapper.toCourseResponse(exstingCourse.get());
    }

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public List<QuestionResponse> generateQuestionList(int lessonId) {
        String url = BASE_URL + "generate-course/lesson/question-list";

        // Check if lesson exists
        Optional<Lesson> exstingLesson = lessonRepo.findById(lessonId);
        if (exstingLesson.isEmpty()) {
            throw new AppException(ErrorCode.LESSON_NOT_FOUND);
        }
        Lesson lesson = exstingLesson.get();

        // Check if question already exists
        if(!lesson.getQuestions().isEmpty() || lesson.getQuestions().size() > 0) {
            return coachMapper.toQuestionResponseList(lesson.getQuestions());
        }

        Map<String, String> body = Map.of("lesson", lesson.getTitle());

        // Call Django API
        Map<String, Object> data = apiClient.post(url, apiClient.getToken(), body);
        List<Map<String, Object>> questions = (List<Map<String, Object>>) data.get("questions");

        questions.forEach(question -> {
            Question q = new Question();
            q.setTitle((String)  question.get("question"));
            q.setLesson(lesson);
            q.setExplanation((String) question.get("explanation"));
            lesson.getQuestions().add(q);

            List<Map<String, Object>> options = (List<Map<String, Object>>) question.get("options");
            options.forEach(option -> {
                Option o = new Option();
                o.setContent((String) option.get("option"));
                o.setLabel((String) option.get("label"));
                o.setQuestion(q);
                q.getOptions().add(o);

                // Set correct option
                if (o.getLabel().equals(question.get("correct_option"))) {
                    q.setCorrectOption(o);
                }
            });

            // Save each question
            questionRepo.save(q);
        });

        // Return saved questions of a lesson
        Optional<Lesson> savedLesson = lessonRepo.findById(lessonId);
        return coachMapper.toQuestionResponseList(savedLesson.get().getQuestions());
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
                .concepts(new String[]{ role })
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