package com.fpt.careermate.services.blog_services.service.impl;

import com.fpt.careermate.services.blog_services.service.dto.request.BlogCreationRequest;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogUpdateRequest;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BlogService {
    BlogResponse createBlog(BlogCreationRequest request, String email);

    BlogResponse updateBlog(Long blogId, BlogUpdateRequest request);

    void deleteBlog(Long blogId);

    BlogResponse getBlogById(Long blogId);

    Page<BlogResponse> getAllBlogs(Pageable pageable);

    Page<BlogResponse> getBlogsByStatus(String status, Pageable pageable);

    Page<BlogResponse> getBlogsByCategory(String category, Pageable pageable);

    Page<BlogResponse> getBlogsByAuthor(int authorId, Pageable pageable);

    Page<BlogResponse> searchBlogs(String keyword, String status, Pageable pageable);

    Page<BlogResponse> filterBlogs(String keyword, String status, String category, Pageable pageable);

    List<String> getAllCategories();

    BlogResponse publishBlog(Long blogId);

    BlogResponse unpublishBlog(Long blogId);

    BlogResponse archiveBlog(Long blogId);

    BlogResponse unarchiveBlog(Long blogId);

    void incrementViewCount(Long blogId);
    
    BlogResponse getBlogBySlug(String slug);
    
    void incrementViewCountBySlug(String slug);
    
    List<BlogResponse> getRelatedBlogs(Long blogId, int limit);
}
