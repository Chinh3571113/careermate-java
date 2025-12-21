package com.fpt.careermate.services.coach_services.service.impl;


import com.fpt.careermate.services.coach_services.service.dto.response.RecommendedRoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.RoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.TopicDetailResponse;

import java.util.List;

public interface AdminRoadmapService {
    void createRoadmapCollection();
    void deleteRoadmapCollection(String deletedCollectionName);
    void createRoadmapCollection2();
}
