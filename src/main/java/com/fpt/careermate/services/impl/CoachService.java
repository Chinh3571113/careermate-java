package com.fpt.careermate.services.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fpt.careermate.services.dto.response.CourseListResponse;
import com.fpt.careermate.services.dto.response.CourseResponse;

import java.util.List;

public interface CoachService {
    CourseResponse generateCourse(String topic);
    String generateLesson(int lessonId) throws JsonProcessingException;
    List<CourseListResponse> getMyCourses();
}
