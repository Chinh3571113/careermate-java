package com.fpt.careermate.services.review_services.service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for job application with review status for each review type.
 * Used to display grouped review cards showing which reviews are:
 * - submitted (completed)
 * - available (can write now)
 * - not_eligible (need to wait or not qualified)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationReviewStatusResponse {

    private Integer jobApplyId;
    private String jobTitle;
    private String companyName;
    private String companyLogo;
    private LocalDateTime appliedAt;
    private LocalDateTime interviewedAt;
    private LocalDateTime hiredAt;
    private Integer daysSinceApplication;
    private Integer daysEmployed;

    // Status for each review type
    private ReviewTypeStatus applicationReview;
    private ReviewTypeStatus interviewReview;
    private ReviewTypeStatus workReview;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewTypeStatus {
        /**
         * submitted - Review already written
         * available - Eligible to write now
         * not_eligible - Not yet qualified (e.g., not enough days)
         */
        private String status;

        /** Review ID if submitted */
        private Integer reviewId;

        /** Rating if submitted */
        private Integer rating;

        /** Reason why not eligible (e.g., "Need 5 more days", "Not interviewed yet") */
        private String reason;
    }
}
