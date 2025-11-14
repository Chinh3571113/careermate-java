package com.fpt.careermate.services.coach_services.service.impl;


import com.fpt.careermate.services.coach_services.service.dto.response.RecommendedRoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.RoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.TopicDetailResponse;

import java.util.List;

public interface RoadmapService {
    void addRoadmap(String nameRoadmap, String fileName);
    RoadmapResponse getRoadmap(String roadmapName);
    TopicDetailResponse getTopicDetail(int topicId);
    TopicDetailResponse getSubtopicDetail(int subtopicId);
    List<RecommendedRoadmapResponse> recommendRoadmap(String role);
}
