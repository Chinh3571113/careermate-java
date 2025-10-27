package com.fpt.careermate.services.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.careermate.services.dto.response.CourseListResponse;
import com.fpt.careermate.services.dto.response.CourseResponse;
import com.fpt.careermate.services.dto.response.QuestionResponse;

import java.util.List;

public interface CoachService {
    CourseResponse generateCourse(String topic);
    String generateLesson(int lessonId) throws JsonProcessingException;
    List<CourseListResponse> getMyCourses();
    void markLesson(int lessonId, boolean marked);
    CourseResponse getCourseById(int courseId);
    List<QuestionResponse> generateQuestionList(int lessonId);
}
