package com.fpt.careermate.services.review_services.service.dto.response;

import com.fpt.careermate.services.review_services.constant.ReviewType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Public-facing review DTO.
 * Does not expose reviewer identifiers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicCompanyReviewResponse {

    Integer id;

    // Company / job context
    Integer recruiterId;
    String companyName;
    Integer jobPostingId;
    String jobTitle;

    // Review
    ReviewType reviewType;
    String reviewText;
    Integer overallRating;

    // Aspect ratings
    Integer communicationRating;
    Integer responsivenessRating;
    Integer interviewProcessRating;
    Integer workCultureRating;
    Integer managementRating;
    Integer benefitsRating;
    Integer workLifeBalanceRating;

    // Public identity
    Boolean isAnonymous;
    String candidateName; // null if anonymous

    // Timestamps
    LocalDateTime createdAt;
}
