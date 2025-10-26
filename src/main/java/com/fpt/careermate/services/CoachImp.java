package com.fpt.careermate.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.careermate.domain.*;
import com.fpt.careermate.domain.Module;
import com.fpt.careermate.repository.CandidateRepo;
import com.fpt.careermate.repository.CourseRepo;
import com.fpt.careermate.repository.LessonRepo;
import com.fpt.careermate.services.dto.response.CourseListResponse;
import com.fpt.careermate.services.dto.response.CourseResponse;
import com.fpt.careermate.services.impl.CoachService;
import com.fpt.careermate.services.mapper.CoachMapper;
import com.fpt.careermate.util.ApiClient;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;


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
    AuthenticationImp authenticationImp;
    CoachMapper coachMapper;

    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public CourseResponse generateCourse(String topic) {
        String url = BASE_URL + "generate-course/";
        Map<String, String> body = Map.of("topic", topic);

        // Call Django API
        Map<String, Object> data = apiClient.post(url, apiClient.getToken(), body);

        // Save to database
        Course course = new Course();
        course.setTitle((String) data.get("title"));

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

    private Candidate getCurrentCandidate() {
        Account account = authenticationImp.findByEmail();
        Optional<Candidate> exsting = candidateRepo.findByAccount_Id(account.getId());
        return exsting.get();
    }

}