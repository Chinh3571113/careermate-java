package com.fpt.careermate.services.coach_services.service;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.util.CoachUtil;
import com.fpt.careermate.services.coach_services.domain.Course;
import com.fpt.careermate.services.coach_services.repository.CourseRepo;
import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
import com.fpt.careermate.services.coach_services.service.dto.response.*;
import com.fpt.careermate.services.coach_services.service.impl.CoachService;
import com.fpt.careermate.services.coach_services.service.mapper.CourseMapper;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import io.weaviate.client.v1.graphql.query.builder.GetBuilder;
import io.weaviate.client.v1.graphql.query.fields.Field;
import io.weaviate.client.v1.graphql.query.fields.Fields;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CourseImp implements CoachService {

    WeaviateClient client;
    CourseMapper courseMapper;
    CourseRepo courseRepo;
    CoachUtil coachUtil;

    @Override
    // Hàm gợi ý khóa học dựa trên vai trò (role) của người dùng
    public List<RecommendedCourseResponse> recommendCourse(String role) {
        String collectionName = "Course";
        String[] target_vector = {"title_vector"};

        // Tạo bộ lọc tìm kiếm gần theo văn bản (nearText)
        // "concepts" là mảng các từ khóa hoặc cụm từ dùng để tìm kiếm ngữ nghĩa
        // "certainty" là ngưỡng độ tin cậy tối thiểu của kết quả (0.7f = 70%)
        NearTextArgument nearText = NearTextArgument.builder()
                // vì SDK được sinh máy móc từ định nghĩa GraphQL, nên nó phản ánh y nguyên kiểu danh sách.
                .concepts(new String[]{role.toLowerCase().trim()})
                .certainty(0.71f)
                .targetVectors(target_vector) // Sử dụng trường vector tùy chỉnh
                .build();

        // Xác định các trường cần lấy từ đối tượng "Course" trong Weaviate
        // Bao gồm: "title" và "_additional.certainty" (độ tương tự)
        Fields fields = Fields.builder()
                .fields(new Field[]{
                        Field.builder().name("title").build(),
                        Field.builder().name("url").build(),
                        Field.builder().name("_additional").fields(new Field[]{
                                Field.builder().name("certainty").build()
                        }).build()
                })
                .build();

        // Tạo câu truy vấn GraphQL để lấy danh sách 5 khóa học liên quan nhất
        String query = GetBuilder.builder()
                .className(collectionName)
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
        List<Map<String, Object>> courseData = (List<Map<String, Object>>) get.get(collectionName);  // danh sách khóa học

        // Chuyển từng phần tử trong danh sách sang đối tượng phản hồi (DTO)
        List<RecommendedCourseResponse> recommendedCourseResponseList = new ArrayList<>();
        courseData.forEach(course -> {
            String title = (String) course.get("title");
            String url = (String) course.get("url");
            Map<String, Object> additional = (Map<String, Object>) course.get("_additional");
            Double similarityScore = (Double) additional.get("certainty");

            // Thêm vào danh sách kết quả trả về
            recommendedCourseResponseList.add(new RecommendedCourseResponse(title, url, similarityScore));
        });

        // Trả về danh sách khóa học gợi ý
        return recommendedCourseResponseList;
    }

    // Hàm thêm khóa học khi candidate chọn course được gợi ý
    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public void addCourse(CourseCreationRequest request) {
        Course course = courseMapper.toCourse(request);
        // Thiết lập ngày tạo khóa học và liên kết với candidate
        course.setCreatedAt(LocalDate.now());
        course.setCandidate(coachUtil.getCurrentCandidate());

        // Lưu course vào Postgres
        courseRepo.save(course);
    }

    // Hàm lấy danh sách khóa học của candidate hiện tại với trạng thái đã đánh dấu
    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public CoursePageResponse getMyCoursesWithMarkedStatus(int page, int size) {
        return getMyCourses(page, size, true);
    }

    // Hàm lấy danh sách khóa học của candidate hiện tại với trạng thái chưa đánh dấu
    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public CoursePageResponse getMyCoursesWithUnMarkedStatus(int page, int size) {
        return getMyCourses(page, size, false);
    }

    private CoursePageResponse getMyCourses(int page, int size, boolean marked) {
        List<CourseResponse> myCourses = new ArrayList<>();
        Integer candidateId = coachUtil.getCurrentCandidate().getCandidateId();

        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());
        Page<Course> courses = courseRepo.findByMarkedAndCandidate_CandidateId(marked, candidateId, pageable);

        // Chuyển từng Course sang CourseResponse và thêm vào danh sách
        courses.getContent().forEach(course -> {
            CourseResponse courseResponse = courseMapper.toCourseResponse(course);
            myCourses.add(courseResponse);
        });

        // Chuyển Page<Course> sang CoursePageResponse
        CoursePageResponse coursePageResponse = courseMapper.toCoursePageResponse(courses);
        coursePageResponse.setContent(myCourses);


        return coursePageResponse;
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public void markCourse(int courseId, boolean marked) {
        // Kiểm tra khóa học có tồn tại không
        Optional<Course> exsting = courseRepo.findById(courseId);
        if(exsting.isEmpty()) throw new AppException(ErrorCode.COURSE_NOT_FOUND);

        // Cập nhật trạng thái đánh dấu
        Course course = exsting.get();
        course.setMarked(marked);
        courseRepo.save(course);
    }
}