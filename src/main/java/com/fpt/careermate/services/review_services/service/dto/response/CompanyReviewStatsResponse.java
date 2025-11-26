package com.fpt.careermate.services.review_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyReviewStatsResponse {
    
    Integer recruiterId;
    String companyName;
    
    // Overall statistics
    Long totalReviews;
    Double averageOverallRating;
    
    // Review counts by type
    Long applicationReviews;
    Long interviewReviews;
    Long workExperienceReviews;
    
    // Average ratings by aspect
    Double avgCommunication;
    Double avgResponsiveness;
    Double avgInterviewProcess;
    Double avgWorkCulture;
    Double avgManagement;
    Double avgBenefits;
    Double avgWorkLifeBalance;
    
    // Rating distribution (1-5 stars)
    Map<Integer, Long> ratingDistribution;
    
    // Sentiment analysis
    Double avgSentimentScore;
    
    // Verification
    Long verifiedReviews;
    Long anonymousReviews;
}
