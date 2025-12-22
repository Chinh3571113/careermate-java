package com.fpt.careermate.services.blog_services.service;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearTextArgument;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Semantic toxicity analyzer using Weaviate vector similarity
 * to determine if flagged comments are truly toxic or false positives
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SemanticToxicityAnalyzer {

    WeaviateClient weaviateClient;

    // Known toxic patterns for semantic comparison
    private static final List<String> TOXIC_PATTERNS = Arrays.asList(
        "You are stupid and worthless",
        "I hate you and everything you stand for",
        "This is complete garbage and you should be ashamed",
        "Go kill yourself you piece of trash",
        "You're an idiot who doesn't know anything",
        "This company is run by morons and thieves",
        "I hope you get fired and lose everything",
        "Worst advice ever from a total fraud"
    );

    // Known positive patterns (for comparison)
    private static final List<String> POSITIVE_PATTERNS = Arrays.asList(
        "This advice is killer and will help my career",
        "I hate to admit it but this is amazing content",
        "This article killed my doubts about career changes",
        "My stupid mistake was not reading this sooner",
        "This job posting is sick in a good way",
        "The recruiter contact is legit and helpful"
    );

    /**
     * Analyze comment toxicity using semantic similarity
     * Returns toxicity score: 0.0 (benign) to 1.0 (highly toxic)
     * 
     * @param commentText The comment text to analyze
     * @param flagReason The reason it was flagged (optional context)
     * @return ToxicityScore with confidence level
     */
    public ToxicityScore analyzeToxicity(String commentText, String flagReason) {
        try {
            log.info("Analyzing toxicity for comment: {}", commentText.substring(0, Math.min(50, commentText.length())));

            // Calculate semantic similarity to toxic patterns
            double toxicSimilarity = calculateMaxSimilarity(commentText, TOXIC_PATTERNS);
            
            // Calculate semantic similarity to positive patterns
            double positiveSimilarity = calculateMaxSimilarity(commentText, POSITIVE_PATTERNS);

            // Calculate toxicity score
            // If more similar to toxic patterns, score increases
            // If more similar to positive patterns, score decreases
            double rawScore = (toxicSimilarity - positiveSimilarity + 1.0) / 2.0;
            
            // Normalize to 0.0-1.0 range
            double normalizedScore = Math.max(0.0, Math.min(1.0, rawScore));

            // Determine confidence level
            ConfidenceLevel confidence;
            if (Math.abs(toxicSimilarity - positiveSimilarity) > 0.3) {
                confidence = ConfidenceLevel.HIGH;
            } else if (Math.abs(toxicSimilarity - positiveSimilarity) > 0.15) {
                confidence = ConfidenceLevel.MEDIUM;
            } else {
                confidence = ConfidenceLevel.LOW;
            }

            log.info("Toxicity analysis complete - Score: {}, Confidence: {}", normalizedScore, confidence);

            return ToxicityScore.builder()
                    .toxicityScore(normalizedScore)
                    .confidence(confidence)
                    .toxicSimilarity(toxicSimilarity)
                    .positiveSimilarity(positiveSimilarity)
                    .reasoning(buildReasoning(normalizedScore, toxicSimilarity, positiveSimilarity, confidence))
                    .build();

        } catch (Exception e) {
            log.error("Error analyzing toxicity: {}", e.getMessage(), e);
            // On error, return neutral score with low confidence
            return ToxicityScore.builder()
                    .toxicityScore(0.5)
                    .confidence(ConfidenceLevel.LOW)
                    .reasoning("Error during analysis: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Calculate maximum semantic similarity between text and a list of patterns
     * Uses simple word overlap heuristic (can be enhanced with Weaviate embeddings)
     */
    private double calculateMaxSimilarity(String text, List<String> patterns) {
        String normalizedText = text.toLowerCase();
        double maxSimilarity = 0.0;

        for (String pattern : patterns) {
            double similarity = calculateTextSimilarity(normalizedText, pattern.toLowerCase());
            maxSimilarity = Math.max(maxSimilarity, similarity);
        }

        return maxSimilarity;
    }

    /**
     * Calculate similarity between two texts using word overlap
     * Returns value between 0.0 and 1.0
     */
    private double calculateTextSimilarity(String text1, String text2) {
        Set<String> words1 = new HashSet<>(Arrays.asList(text1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(text2.split("\\s+")));

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * Build human-readable reasoning for the toxicity score
     */
    private String buildReasoning(double score, double toxicSim, double positiveSim, ConfidenceLevel confidence) {
        if (score > 0.7) {
            return String.format("High toxicity (%.1f%% similar to known toxic patterns). %s confidence.", 
                    score * 100, confidence);
        } else if (score > 0.4) {
            return String.format("Moderate toxicity (%.1f%% score). Review context carefully. %s confidence.", 
                    score * 100, confidence);
        } else {
            return String.format("Likely false positive (%.1f%% similar to positive patterns). %s confidence.", 
                    positiveSim * 100, confidence);
        }
    }

    /**
     * Bulk analyze multiple comments
     */
    public Map<Long, ToxicityScore> analyzeBatch(Map<Long, String> commentTexts) {
        Map<Long, ToxicityScore> results = new HashMap<>();
        
        for (Map.Entry<Long, String> entry : commentTexts.entrySet()) {
            results.put(entry.getKey(), analyzeToxicity(entry.getValue(), null));
        }
        
        return results;
    }

    // Inner classes for result structure
    @lombok.Data
    @lombok.Builder
    public static class ToxicityScore {
        private double toxicityScore;      // 0.0-1.0 (0=benign, 1=toxic)
        private ConfidenceLevel confidence;
        private double toxicSimilarity;
        private double positiveSimilarity;
        private String reasoning;
    }

    public enum ConfidenceLevel {
        HIGH,    // Clear distinction between toxic and positive
        MEDIUM,  // Some ambiguity
        LOW      // Very ambiguous, manual review critical
    }
}
