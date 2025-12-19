package com.fpt.careermate.services.review_services.service.dto.request;

import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminBulkReviewStatusRequest {

    @NotEmpty
    List<Integer> reviewIds;

    @NotNull
    ReviewStatus status;

    String reason;
}
