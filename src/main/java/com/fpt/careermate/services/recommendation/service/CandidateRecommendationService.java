package com.fpt.careermate.services.recommendation.service;

import com.fpt.careermate.services.recommendation.dto.CandidateRecommendationDTO;
import com.fpt.careermate.services.recommendation.dto.RecommendationResponseDTO;

import java.util.List;

public interface CandidateRecommendationService {
    
    /**
     * Get recommended candidates for a specific job posting
     * @param jobPostingId The ID of the job posting
     * @param maxCandidates Maximum number of candidates to return (default: 10)
     * @param minMatchScore Minimum match score threshold (0.0 to 1.0, default: 0.5)
     * @return List of recommended candidates with match scores
     */
    RecommendationResponseDTO getRecommendedCandidatesForJob(
            int jobPostingId, 
            Integer maxCandidates, 
            Double minMatchScore
    );
    
    /**
     * Sync candidate profile to Weaviate vector database
     * @param candidateId The candidate ID to sync
     */
    void syncCandidateToWeaviate(int candidateId);
    
    /**
     * Sync all candidates to Weaviate
     */
    void syncAllCandidatesToWeaviate();
    
    /**
     * Delete candidate from Weaviate
     * @param candidateId The candidate ID to delete
     */
    void deleteCandidateFromWeaviate(int candidateId);

    void getCollection();

    /**
     * Recreate Weaviate schema with proper vectorization settings
     * This will delete the existing schema and create a new one
     * After calling this, you must re-sync all candidates
     */
    void recreateSchema();
}

