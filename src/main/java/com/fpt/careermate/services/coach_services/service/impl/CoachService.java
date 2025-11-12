package com.fpt.careermate.services.coach_services.service.impl;


import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
import com.fpt.careermate.services.coach_services.service.dto.response.*;

import java.util.List;

public interface CoachService {
    List<RecommendedCourseResponse> recommendCourse(String role);
    void addCourse(CourseCreationRequest request);
    CoursePageResponse getMyCoursesWithMarkedStatus(int page, int size);
    CoursePageResponse getMyCoursesWithUnMarkedStatus(int page, int size);
    void markCourse(int courseId, boolean marked);
}
