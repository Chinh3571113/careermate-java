package com.fpt.careermate.services.coach_services.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.careermate.services.coach_services.service.dto.response.CourseListResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.CourseResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.QuestionResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.RecommendedCourseResponse;

import java.util.List;

public interface CoachService {
    CourseResponse generateCourse(String title) throws JsonProcessingException;
    String generateLesson(int lessonId) throws JsonProcessingException;
    List<CourseListResponse> getMyCourses();
    void markLesson(int lessonId, boolean marked);
    CourseResponse getCourseById(int courseId);
    List<QuestionResponse> generateQuestionList(int lessonId);
    List<RecommendedCourseResponse> recommendCourse(String role);
}
