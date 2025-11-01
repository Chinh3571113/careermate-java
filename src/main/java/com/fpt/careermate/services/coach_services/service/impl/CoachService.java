package com.fpt.careermate.services.coach_services.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
import com.fpt.careermate.services.coach_services.service.dto.response.CourseListResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.CourseResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.QuestionResponse;

import java.util.List;

public interface CoachService {
    CourseResponse generateCourse(CourseCreationRequest request);
    String generateLesson(int lessonId) throws JsonProcessingException;
    List<CourseListResponse> getMyCourses();
    void markLesson(int lessonId, boolean marked);
    CourseResponse getCourseById(int courseId);
    List<QuestionResponse> generateQuestionList(int lessonId);
}
