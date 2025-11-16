package com.fpt.careermate.services.recommendation.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.recommendation.dto.RecommendationResponseDTO;
import com.fpt.careermate.services.recommendation.service.CandidateRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Candidate Recommendation", description = "AI-powered candidate recommendation system")
public class CandidateRecommendationController {

    CandidateRecommendationService recommendationService;

    @GetMapping("/recruiter/recommendations/job/{jobPostingId}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    @SecurityRequirement(name = "bearerToken")
    @Operation(
            summary = "Get recommended candidates for a job posting",
            description = "Uses AI to find and rank candidates whose skills match the job requirements"
    )
    public ApiResponse<RecommendationResponseDTO> getRecommendedCandidates(
            @Parameter(description = "Job posting ID") @PathVariable int jobPostingId,
            @Parameter(description = "Maximum number of candidates to return") @RequestParam(required = false) Integer maxCandidates,
            @Parameter(description = "Minimum match score (0.0 - 1.0)") @RequestParam(required = false) Double minMatchScore
    ) {
        log.info("🔍 Getting recommended candidates for job posting ID: {} (maxCandidates: {}, minScore: {})",
                jobPostingId, maxCandidates, minMatchScore);
        RecommendationResponseDTO response = recommendationService.getRecommendedCandidatesForJob(
                jobPostingId,
                maxCandidates,
                minMatchScore
        );
        log.info("📊 Found {} candidates for job posting {}",
                response.getTotalCandidatesFound(), jobPostingId);
        return ApiResponse.<RecommendationResponseDTO>builder()
                .result(response)
                .build();
    }

    @PostMapping({"/admin/recommendations/refresh-candidate/{candidateId}", "/admin/recommendations/sync-candidate/{candidateId}"})
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerToken")
    @Operation(
            summary = "Refresh candidate profile in Weaviate",
            description = "Updates a candidate's comprehensive profile data in Weaviate vector database for AI-powered recommendations. " +
                    "Includes skills, experience, education, certificates, projects, awards, and languages."
    )
    public ApiResponse<String> refreshCandidateProfile(
            @Parameter(description = "Candidate ID") @PathVariable int candidateId
    ) {
        log.info("🔄 Admin refreshing candidate {} profile in Weaviate", candidateId);
        try {
            recommendationService.syncCandidateToWeaviate(candidateId);
            log.info("✅ Successfully refreshed candidate {} profile", candidateId);
            return ApiResponse.<String>builder()
                    .result("Candidate profile refreshed successfully in recommendation system")
                    .build();
        } catch (Exception e) {
            log.error("❌ Failed to refresh candidate {}: {}", candidateId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping({"/admin/recommendations/refresh-all-candidates", "/admin/recommendations/sync-all-candidates"})
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerToken")
    @Operation(
            summary = "Refresh all candidate profiles in Weaviate",
            description = "Batch refreshes all candidate profiles with comprehensive data (skills, experience, education, etc.) " +
                    "in Weaviate for the AI recommendation system. Use this after schema recreation or for bulk updates."
    )
    public ApiResponse<String> refreshAllCandidateProfiles() {
        log.info("🔄 Admin refreshing all candidate profiles in Weaviate");
        recommendationService.syncAllCandidatesToWeaviate();
        return ApiResponse.<String>builder()
                .result("All candidate profiles refresh started")
                .build();
    }

    @DeleteMapping("/admin/recommendations/candidate/{candidateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete candidate from Weaviate",
            description = "Removes a candidate's profile from the Weaviate recommendation index"
    )
    public ApiResponse<String> deleteCandidateFromWeaviate(
            @Parameter(description = "Candidate ID") @PathVariable int candidateId
    ) {
        log.info("Admin deleting candidate {} from Weaviate", candidateId);
        recommendationService.deleteCandidateFromWeaviate(candidateId);
        return ApiResponse.<String>builder()
                .result("Candidate deleted from recommendation system")
                .build();
    }

    @GetMapping("/class")
    public ApiResponse<String> getCollection() {
        recommendationService.getCollection();
        return ApiResponse.<String>builder()
                .result("Get collection successfully")
                .build();
    }

    @PostMapping("/admin/recommendations/recreate-schema")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerToken")
    @Operation(
            summary = "Recreate Weaviate schema with comprehensive candidate profile structure",
            description = "Deletes and recreates the schema with all candidate qualification fields: " +
                    "skills (40%), work experience (25%), education (15%), certificates (10%), " +
                    "projects (5%), awards (3%), languages (2%). " +
                    "Uses text2vec-weaviate embeddings for semantic search. " +
                    "⚠️ After calling this, you MUST refresh all candidate profiles."
    )
    public ApiResponse<String> recreateSchema() {
        log.info("🔄 Admin recreating Weaviate schema with comprehensive profile structure...");
        try {
            recommendationService.recreateSchema();
            log.info("✅ Schema recreated successfully");
            return ApiResponse.<String>builder()
                    .result("Schema recreated successfully with comprehensive profile structure. " +
                            "Please refresh all candidate profiles now using /admin/recommendations/refresh-all-candidates")
                    .build();
        } catch (Exception e) {
            log.error("❌ Failed to recreate schema: {}", e.getMessage(), e);
            throw e;
        }
    }
}

