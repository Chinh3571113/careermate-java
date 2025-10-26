package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Course;
import com.fpt.careermate.domain.Lesson;
import com.fpt.careermate.domain.Module;
import com.fpt.careermate.services.dto.response.CourseResponse;
import com.fpt.careermate.services.dto.response.LessonResponse;
import com.fpt.careermate.services.dto.response.ModuleResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CoachMapper {
    CourseResponse toCourseResponse(Course course);
    ModuleResponse toModuleResponse(Module module);
    LessonResponse toLessonResponse(Lesson lesson);
}
