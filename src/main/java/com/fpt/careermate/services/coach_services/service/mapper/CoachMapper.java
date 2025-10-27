package com.fpt.careermate.services.coach_services.service.mapper;

import com.fpt.careermate.services.coach_services.domain.Course;
import com.fpt.careermate.services.coach_services.domain.Lesson;
import com.fpt.careermate.services.coach_services.domain.Module;
import com.fpt.careermate.services.coach_services.domain.Question;
import com.fpt.careermate.services.coach_services.service.dto.response.CourseResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.LessonResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.ModuleResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.QuestionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CoachMapper {
    CourseResponse toCourseResponse(Course course);
    ModuleResponse toModuleResponse(Module module);
    @Mapping(source = "marked", target = "marked")
    LessonResponse toLessonResponse(Lesson lesson);

    QuestionResponse toQuestionResponse(Question question);
    List<QuestionResponse> toQuestionResponseList(List<Question> questions);
}
