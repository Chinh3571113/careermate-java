package com.fpt.careermate.services.coach_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.coach_services.service.AsyncRoadmapImp;
import com.fpt.careermate.services.coach_services.service.RoadmapImp;
import com.fpt.careermate.services.coach_services.service.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roadmap")
@Tag(name = "Candidate - Roadmap", description = "Manage roadmap")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoadmapController {

    RoadmapImp roadmapImp;
    AsyncRoadmapImp asyncRoadmapImp;

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


    @PostMapping("/highlighted-resume")
    @Operation(description = """
            Recommend roadmap based on candidate's career goal
            input: resumeId
            output: none
            Need login as CANDIDATE to access this API
            """)
    public ApiResponse<Void> highlightedResume(
            @RequestParam int resumeId
    )
    {
        roadmapImp.highlightedResume(resumeId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/candidate-roadmap")
    @Operation(description = """
            Get candidate roadmap based on resumeId and roadmapName
            input: resumeId, roadmapName
            output: candidate roadmap with progress
            Need login as CANDIDATE to access this API
            """)
    public ApiResponse<RoadmapResponse> getCandidateRoadmap(
            @RequestParam int resumeId,
            @RequestParam String roadmapName
    )
    {
        return ApiResponse.<RoadmapResponse>builder()
                .result(roadmapImp.getCandidateRoadmap(resumeId, roadmapName))
                .code(200)
                .message("success")
                .build();
    }

    @GetMapping("/resume-roadmaps")
    @Operation(description = """
            Get paginated list of roadmaps with optional resume filter
            input: 
            - resumeId (optional): If provided, get roadmaps for specific resume. If not, get all roadmaps for all resumes of current candidate
            - page (default 0): Page number
            - size (default 10): Items per page
            - sortBy (default 'createdat_desc'): Sort option
            sortBy options: 'createdat_asc', 'createdat_desc', 'roadmapname_asc', 'roadmapname_desc', 'resumetitle_asc', 'resumetitle_desc'
            output: paginated list of resume roadmaps
            Need login as CANDIDATE to access this API
            """)
    public ApiResponse<ResumeRoadmapPageResponse> getMyRoadmapListOfAResume(
            @RequestParam(required = false) Integer resumeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdat_desc") String sortBy
    )
    {
        return ApiResponse.<ResumeRoadmapPageResponse>builder()
                .result(roadmapImp.getMyRoadmapListOfAResume(resumeId, page, size, sortBy))
                .code(200)
                .message("success")
                .build();
    }

    @PutMapping("/resume/{resumeId}/subtopic/{subtopicId}/toggle-status")
    @Operation(description = """
            Toggle subtopic progress status between COMPLETED and NOT_STARTED
            input: resumeId (path variable), subtopicId (path variable)
            behavior:
            - If current status is NOT_STARTED -> change to COMPLETED
            - If current status is COMPLETED -> change to NOT_STARTED
            - If current status is IN_PROGRESS -> change to COMPLETED
            output: no content (200)
            Need login as CANDIDATE to access this API
            Only the owner of the resume can toggle the status
            """)
    public ApiResponse<Void> toggleSubtopicProgressStatus(
            @PathVariable int resumeId,
            @PathVariable int subtopicId
    )
    {
        roadmapImp.toggleSubtopicProgressStatus(resumeId, subtopicId);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Subtopic progress status toggled successfully")
                .build();
    }

}


