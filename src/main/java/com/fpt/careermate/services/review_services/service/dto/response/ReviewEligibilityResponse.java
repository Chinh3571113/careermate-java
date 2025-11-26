package com.fpt.careermate.services.review_services.service.dto.response;

import com.fpt.careermate.services.review_services.constant.CandidateQualification;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewEligibilityResponse {
    
    Integer jobApplyId;
    Integer candidateId;
    Integer recruiterId;
    String companyName;
    
    CandidateQualification qualification;
    Set<ReviewType> allowedReviewTypes;
    String message;
    
    // Track which review types have already been submitted
    Map<ReviewType, Boolean> alreadyReviewed;
    
    // Additional details
    Integer daysSinceApplication;
    Integer daysEmployed;
    Boolean canReview;
}
