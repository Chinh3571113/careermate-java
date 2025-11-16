package com.fpt.careermate.services.blog_services.service.mapper;

import com.fpt.careermate.services.blog_services.domain.BlogComment;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogCommentRequest;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogCommentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BlogCommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "blog", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isHidden", ignore = true)
    BlogComment toBlogComment(BlogCommentRequest request);

    @Mapping(target = "blogId", source = "blog.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.username")
    BlogCommentResponse toBlogCommentResponse(BlogComment comment);
}
