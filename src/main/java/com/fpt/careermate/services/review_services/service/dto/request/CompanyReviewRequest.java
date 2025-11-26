package com.fpt.careermate.services.review_services.service.dto.request;

import com.fpt.careermate.services.review_services.constant.ReviewType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompanyReviewRequest {
    
    @NotNull(message = "Job application ID is required")
    Integer jobApplyId;
    
    @NotNull(message = "Review type is required")
    ReviewType reviewType;
    
    @NotBlank(message = "Review text is required")
    @Size(min = 20, max = 2000, message = "Review text must be between 20 and 2000 characters")
    String reviewText;
    
    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    Integer overallRating;
    
    // Aspect-specific ratings (optional)
    @Min(value = 1, message = "Communication rating must be at least 1")
    @Max(value = 5, message = "Communication rating must be at most 5")
    Integer communicationRating;
    
    @Min(value = 1, message = "Responsiveness rating must be at least 1")
    @Max(value = 5, message = "Responsiveness rating must be at most 5")
    Integer responsivenessRating;
    
    @Min(value = 1, message = "Interview process rating must be at least 1")
    @Max(value = 5, message = "Interview process rating must be at most 5")
    Integer interviewProcessRating;
    
    @Min(value = 1, message = "Work culture rating must be at least 1")
    @Max(value = 5, message = "Work culture rating must be at most 5")
    Integer workCultureRating;
    
    @Min(value = 1, message = "Management rating must be at least 1")
    @Max(value = 5, message = "Management rating must be at most 5")
    Integer managementRating;
    
    @Min(value = 1, message = "Benefits rating must be at least 1")
    @Max(value = 5, message = "Benefits rating must be at most 5")
    Integer benefitsRating;
    
    @Min(value = 1, message = "Work-life balance rating must be at least 1")
    @Max(value = 5, message = "Work-life balance rating must be at most 5")
    Integer workLifeBalanceRating;
    
    @NotNull(message = "Anonymous flag is required")
    Boolean isAnonymous;
}
