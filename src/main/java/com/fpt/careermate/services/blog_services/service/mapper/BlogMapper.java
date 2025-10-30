package com.fpt.careermate.services.blog_services.service.mapper;

import com.fpt.careermate.services.blog_services.domain.Blog;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogCreationRequest;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogUpdateRequest;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface BlogMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "admin", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tags", source = "tags", qualifiedByName = "listToString")
    Blog toBlog(BlogCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "admin", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tags", source = "tags", qualifiedByName = "listToString")
    void updateBlog(@MappingTarget Blog blog, BlogUpdateRequest request);

    @Mapping(target = "admin.adminId", source = "admin.adminId")
    @Mapping(target = "admin.name", source = "admin.name")
    @Mapping(target = "admin.phone", source = "admin.phone")
    @Mapping(target = "admin.email", source = "admin.account.email")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "stringToList")
    BlogResponse toBlogResponse(Blog blog);

    @Named("listToString")
    default String listToString(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return String.join(",", tags);
    }

    @Named("stringToList")
    default List<String> stringToList(String tags) {
        if (tags == null || tags.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
