package com.fpt.careermate.services.coach_services.service.impl;


import com.fpt.careermate.services.coach_services.service.dto.response.RecommendedRoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.ResumeRoadmapPageResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.RoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.TopicDetailResponse;

import java.util.List;

public interface RoadmapService {
    RoadmapResponse getRoadmap(String roadmapName);
    TopicDetailResponse getTopicDetail(int topicId);
    TopicDetailResponse getSubtopicDetail(int subtopicId);
    List<RecommendedRoadmapResponse> recommendRoadmap(String role);
    void highlightedResume(int resumeId);
    RoadmapResponse getCandidateRoadmap(int resumeId, String roadmapName);
    ResumeRoadmapPageResponse getMyRoadmapListOfAResume(Integer resumeId, int page, int size, String sortBy);
    void toggleSubtopicProgressStatus(int resumeId, int subtopicId);
}
