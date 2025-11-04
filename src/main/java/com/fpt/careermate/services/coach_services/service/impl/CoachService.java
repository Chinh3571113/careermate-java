package com.fpt.careermate.services.coach_services.service.impl;


import com.fpt.careermate.services.coach_services.service.dto.response.*;

import java.util.List;

public interface CoachService {
    List<RecommendedCourseResponse> recommendCourse(String role);
}
