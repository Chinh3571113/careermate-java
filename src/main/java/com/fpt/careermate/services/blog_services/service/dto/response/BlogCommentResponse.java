package com.fpt.careermate.services.blog_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlogCommentResponse {
    Long id;
    Long blogId;
    Long userId;
    String userName;
    String content;
    Boolean isHidden; // true = hidden by admin, false = visible
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

