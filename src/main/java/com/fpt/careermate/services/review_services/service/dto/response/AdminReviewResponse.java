package com.fpt.careermate.services.review_services.service.dto.response;

import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detailed review response for admin/recruiter management
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminReviewResponse {
    private Integer id;
    private Integer candidateId;
    private String candidateName;
    private String candidateEmail;
    private Integer recruiterId;
    private String companyName;
    private Integer jobPostingId;
    private String jobTitle;
    private Integer jobApplyId;
    
    private ReviewType reviewType;
    private ReviewStatus status;
    
    private String reviewText;
    private Integer overallRating;
    
    // Category ratings
    private Integer communicationRating;
    private Integer responsivenessRating;
    private Integer interviewProcessRating;
    private Integer workCultureRating;
    private Integer managementRating;
    private Integer benefitsRating;
    private Integer workLifeBalanceRating;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Boolean isAnonymous;
    private Boolean isVerified;
    
    private Integer flagCount;
    private String removalReason;
    private Double sentimentScore;
}
