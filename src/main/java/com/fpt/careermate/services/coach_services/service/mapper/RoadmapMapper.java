package com.fpt.careermate.services.coach_services.service.mapper;

import com.fpt.careermate.services.coach_services.domain.Roadmap;
import com.fpt.careermate.services.coach_services.domain.Subtopic;
import com.fpt.careermate.services.coach_services.domain.Topic;
import com.fpt.careermate.services.coach_services.service.dto.response.RoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.SubtopicResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.TopicDetailResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.TopicResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoadmapMapper {
    RoadmapResponse toRoadmapResponse(Roadmap roadmap);
    TopicResponse toTopicResponse(Topic topic);
    SubtopicResponse toSubtopicResponse(Subtopic subtopic);

    TopicDetailResponse topicDetailResponse(Topic topic);
    TopicDetailResponse toSubtopicDetailResponse(Subtopic subtopic);
}