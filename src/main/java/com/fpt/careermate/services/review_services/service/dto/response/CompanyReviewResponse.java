package com.fpt.careermate.services.review_services.service.dto.response;

import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyReviewResponse {
    
    Integer id;
    Integer candidateId;
    String candidateName;  // Null if anonymous
    Integer recruiterId;
    String companyName;
    Integer jobApplyId;
    Integer jobPostingId;
    String jobTitle;
    
    ReviewType reviewType;
    ReviewStatus status;
    
    String reviewText;
    Integer overallRating;
    
    // Aspect-specific ratings
    Integer communicationRating;
    Integer responsivenessRating;
    Integer interviewProcessRating;
    Integer workCultureRating;
    Integer managementRating;
    Integer benefitsRating;
    Integer workLifeBalanceRating;
    
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    Boolean isAnonymous;
    Boolean isVerified;
    
    Integer flagCount;
    Double sentimentScore;
}
