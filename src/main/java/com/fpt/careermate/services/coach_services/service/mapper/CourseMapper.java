package com.fpt.careermate.services.coach_services.service.mapper;

import com.fpt.careermate.services.coach_services.domain.Course;
import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
import com.fpt.careermate.services.coach_services.service.dto.response.CoursePageResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.CourseResponse;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    Course toCourse(CourseCreationRequest request);
    CourseResponse toCourseResponse(Course course);
    CoursePageResponse toCoursePageResponse(Page<Course> courses);
}