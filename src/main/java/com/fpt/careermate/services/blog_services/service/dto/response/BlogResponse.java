package com.fpt.careermate.services.blog_services.service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlogResponse {
    Long id;
    String title;
    String content;
    String summary;
    String thumbnailUrl;
    String category;
    List<String> tags;
    String status;
    Integer viewCount;
    LocalDateTime publishedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    AdminInfo admin;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class AdminInfo {
        int adminId;
        String name;
        String phone;
        String email;
    }
}
