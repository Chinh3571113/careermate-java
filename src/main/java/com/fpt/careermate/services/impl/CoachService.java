package com.fpt.careermate.services.impl;


import com.fpt.careermate.services.dto.response.CourseResponse;

public interface CoachService {
    CourseResponse generateCourse(String topic);
}
