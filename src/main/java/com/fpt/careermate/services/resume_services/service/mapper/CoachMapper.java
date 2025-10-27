package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Course;
import com.fpt.careermate.domain.Lesson;
import com.fpt.careermate.domain.Module;
import com.fpt.careermate.domain.Question;
import com.fpt.careermate.services.dto.response.CourseResponse;
import com.fpt.careermate.services.dto.response.LessonResponse;
import com.fpt.careermate.services.dto.response.ModuleResponse;
import com.fpt.careermate.services.dto.response.QuestionResponse;
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
