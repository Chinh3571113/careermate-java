package com.fpt.careermate.services.coach_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.coach_services.service.CourseImp;
import com.fpt.careermate.services.coach_services.service.RoadmapImp;
import com.fpt.careermate.services.coach_services.service.dto.request.CourseCreationRequest;
import com.fpt.careermate.services.coach_services.service.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/roadmap")
@Tag(name = "Roadmap", description = "Manage roadmap")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoadmapController {

    RoadmapImp roadmapImp;

    @PostMapping()
    @Operation(description = """
            Do not use this API
            Need login as ADMIN to access
            """)
    public ApiResponse<Void> addRoadmap(
            @RequestParam String nameRoadmap,
            @RequestParam String fileName)
    {
        roadmapImp.addRoadmap(nameRoadmap, fileName);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping()
    @Operation(description = """
            Get roadmap by name
            input: roadmapName
            output: RoadmapResponse have topics and subtopics
            Need login as CANDIDATE to access this API
            """)
    public ApiResponse<RoadmapResponse> getRoadmap(@RequestParam String roadmapName)
    {
        return ApiResponse.<RoadmapResponse>builder()
                .result(roadmapImp.getRoadmap(roadmapName))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/topic/{topicId}")
    @Operation(description = """
            Get topic by id
            input: topicId
            output: name, description and resources of topic
            Need login as CANDIDATE to access this API
            """)
    public ApiResponse<TopicDetailResponse> getTopicDetailById(@PathVariable int topicId)
    {
        return ApiResponse.<TopicDetailResponse>builder()
                .result(roadmapImp.getTopicDetail(topicId))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/subtopic/{subtopicId}")
    @Operation(description = """
            Get subtopic by id
            input: subtopicId
            output: name, description and resources of subtopicId
            Need login as CANDIDATE to access this API
            """)
    public ApiResponse<TopicDetailResponse> getSubtopicDetailById(@PathVariable int subtopicId)
    {
        return ApiResponse.<TopicDetailResponse>builder()
                .result(roadmapImp.getSubtopicDetail(subtopicId))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/recommendation")
    @Operation(description = """
            Recommend roadmap based on candidate's career goal
            input: role
            output: list of recommended roadmaps
            Need login as CANDIDATE to access this API
            """)
    public ApiResponse<List<RecommendedRoadmapResponse>> recommendRoadmap(@RequestParam String role)
    {
        return ApiResponse.<List<RecommendedRoadmapResponse>>builder()
                .result(roadmapImp.recommendRoadmap(role))
                .code(200)
                .message("success")
                .build();
    }
}