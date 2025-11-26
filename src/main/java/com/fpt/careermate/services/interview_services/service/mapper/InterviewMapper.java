package com.fpt.careermate.services.interview_services.service.mapper;

import com.fpt.careermate.services.interview_services.domain.InterviewQuestion;
import com.fpt.careermate.services.interview_services.domain.InterviewSession;
import com.fpt.careermate.services.interview_services.service.dto.response.InterviewQuestionResponse;
import com.fpt.careermate.services.interview_services.service.dto.response.InterviewSessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InterviewMapper {

    @Mapping(source = "candidate.candidateId", target = "candidateId")
    InterviewSessionResponse toSessionResponse(InterviewSession session);

    InterviewQuestionResponse toQuestionResponse(InterviewQuestion question);

    List<InterviewQuestionResponse> toQuestionResponseList(List<InterviewQuestion> questions);
}

