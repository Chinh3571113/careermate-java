package com.fpt.careermate.services.coach_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.coach_services.service.AdminRoadmapImp;
import com.fpt.careermate.services.coach_services.service.RoadmapImp;
import com.fpt.careermate.services.coach_services.service.dto.response.RoadmapResponse;
import com.fpt.careermate.services.coach_services.service.dto.response.TopicDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/roadmap")
@Tag(name = "Admin - Roadmap", description = "Manage roadmap")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminRoadmapController {

    AdminRoadmapImp adminRoadmapImp;

    @PostMapping("/collection-creation")
    @Operation(description = """
            Create roadmap collection for recommendation
            Creates a collection with name properties
            Uses sentence-transformers/all-MiniLM-L6-v2 model for vectorization
            Need login as ADMIN to access this API
            """)
    public ApiResponse<Void> createRoadmapCollection()
    {
        adminRoadmapImp.createRoadmapCollection();
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Roadmap collection created successfully")
                .build();
    }

    @PostMapping("/collection-2-creation")
    @Operation(description = """
            Create Weaviate collection for roadmap semantic search
            Creates a collection with name and skills properties
            Uses sentence-transformers/all-MiniLM-L6-v2 model for vectorization
            Need login as ADMIN to access this API
            """)
    public ApiResponse<Void> createRoadmapCollection2()
    {
        adminRoadmapImp.createRoadmapCollection2();
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Roadmap collection 2 created successfully")
                .build();
    }

    @DeleteMapping
    @Operation(description = """
            Delete Weaviate collection for roadmap semantic search
            Deletes the collection created for roadmap semantic search
            Need login as ADMIN to access this API
            """)
    public ApiResponse<Void> deleteRoadmapCollection(
            @RequestParam String deletedCollectionName
    )
    {
        adminRoadmapImp.deleteRoadmapCollection(deletedCollectionName);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Roadmap collection is deleted successfully")
                .build();
    }


}