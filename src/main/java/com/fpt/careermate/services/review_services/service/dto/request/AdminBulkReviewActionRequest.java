package com.fpt.careermate.services.review_services.service.dto.request;

import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request for bulk actions on reviews (hide, show, remove)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBulkReviewActionRequest {
    @NotEmpty(message = "Review IDs are required")
    private List<Integer> reviewIds;

    @NotNull(message = "Action is required")
    private ReviewStatus newStatus;

    private String reason; // Optional reason for the action
}
