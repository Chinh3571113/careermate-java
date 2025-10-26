package com.fpt.careermate.services;

import com.fpt.careermate.domain.*;
import com.fpt.careermate.domain.Module;
import com.fpt.careermate.repository.CandidateRepo;
import com.fpt.careermate.repository.CourseRepo;
import com.fpt.careermate.services.dto.response.CourseResponse;
import com.fpt.careermate.services.impl.CoachService;
import com.fpt.careermate.services.mapper.CoachMapper;
import com.fpt.careermate.util.ApiClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CoachImp implements CoachService {
    String BASE_URL = "http://localhost:8000/api/v1/coach/";

    ApiClient apiClient;
    CourseRepo courseRepo;
    CandidateRepo candidateRepo;
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

    private Candidate getCurrentCandidate() {
        Account account = authenticationImp.findByEmail();
        Optional<Candidate> exsting = candidateRepo.findByAccount_Id(account.getId());
        return exsting.get();
    }

}