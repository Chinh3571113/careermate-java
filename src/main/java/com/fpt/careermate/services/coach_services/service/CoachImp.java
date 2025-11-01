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
import com.fpt.careermate.services.coach_services.service.impl.CoachService;
import com.fpt.careermate.services.coach_services.service.mapper.CoachMapper;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
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

}