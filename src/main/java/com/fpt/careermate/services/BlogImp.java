package com.fpt.careermate.services;

import com.fpt.careermate.domain.Account;
import com.fpt.careermate.domain.Blog;
import com.fpt.careermate.repository.AccountRepo;
import com.fpt.careermate.repository.BlogRepo;
import com.fpt.careermate.services.dto.request.BlogCreationRequest;
import com.fpt.careermate.services.dto.request.BlogUpdateRequest;
import com.fpt.careermate.services.dto.response.BlogResponse;
import com.fpt.careermate.services.mapper.BlogMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogImp implements com.fpt.careermate.services.impl.BlogService {
    BlogRepo blogRepo;
    AccountRepo accountRepo;
    BlogMapper blogMapper;

    @Override
    @Transactional
    public BlogResponse createBlog(BlogCreationRequest request, String email) {
        log.info("Creating blog for author email: {}", email);

        Account author = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Blog blog = blogMapper.toBlog(request);
        blog.setAuthor(author);

        if (request.getStatus() != null) {
            try {
                blog.setStatus(Blog.BlogStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.BLOG_INVALID_STATUS);
            }
        }

        blog = blogRepo.save(blog);
        return blogMapper.toBlogResponse(blog);
    }

    @Override
    @Transactional
    public BlogResponse updateBlog(Long blogId, BlogUpdateRequest request) {
        log.info("Updating blog ID: {}", blogId);

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        blogMapper.updateBlog(blog, request);
        blog = blogRepo.save(blog);

        return blogMapper.toBlogResponse(blog);
    }

    @Override
    @Transactional
    public void deleteBlog(Long blogId) {
        log.info("Deleting blog ID: {}", blogId);

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        blogRepo.delete(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponse getBlogById(Long blogId) {
        log.info("Fetching blog ID: {}", blogId);

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        return blogMapper.toBlogResponse(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> getAllBlogs(Pageable pageable) {
        log.info("Fetching all blogs with pagination");
        return blogRepo.findAll(pageable)
                .map(blogMapper::toBlogResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> getBlogsByStatus(String status, Pageable pageable) {
        log.info("Fetching blogs by status: {}", status);

        Blog.BlogStatus blogStatus;
        try {
            blogStatus = Blog.BlogStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.BLOG_INVALID_STATUS);
        }

        return blogRepo.findByStatus(blogStatus, pageable)
                .map(blogMapper::toBlogResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> getBlogsByCategory(String category, Pageable pageable) {
        log.info("Fetching blogs by category: {}", category);
        return blogRepo.findByCategory(category, pageable)
                .map(blogMapper::toBlogResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> getBlogsByAuthor(int authorId, Pageable pageable) {
        log.info("Fetching blogs by author ID: {}", authorId);
        return blogRepo.findByAuthorId(authorId, pageable)
                .map(blogMapper::toBlogResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogResponse> searchBlogs(String keyword, String status, Pageable pageable) {
        log.info("Searching blogs with keyword: {}, status: {}", keyword, status);

        Blog.BlogStatus blogStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                blogStatus = Blog.BlogStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.BLOG_INVALID_STATUS);
            }
        }

        return blogRepo.searchBlogs(keyword, blogStatus, pageable)
                .map(blogMapper::toBlogResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        log.info("Fetching all blog categories");
        return blogRepo.findAllCategories();
    }

    @Override
    @Transactional
    public BlogResponse publishBlog(Long blogId) {
        log.info("Publishing blog ID: {}", blogId);

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        blog.setStatus(Blog.BlogStatus.PUBLISHED);
        blog.setPublishedAt(LocalDateTime.now());
        blog = blogRepo.save(blog);

        return blogMapper.toBlogResponse(blog);
    }

    @Override
    @Transactional
    public BlogResponse unpublishBlog(Long blogId) {
        log.info("Unpublishing blog ID: {}", blogId);

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        blog.setStatus(Blog.BlogStatus.DRAFT);
        blog.setPublishedAt(null); // Clear the published date
        blog = blogRepo.save(blog);

        return blogMapper.toBlogResponse(blog);
    }

    @Override
    @Transactional
    public BlogResponse archiveBlog(Long blogId) {
        log.info("Archiving blog ID: {}", blogId);

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        blog.setStatus(Blog.BlogStatus.ARCHIVED);
        blog = blogRepo.save(blog);

        return blogMapper.toBlogResponse(blog);
    }

    @Override
    @Transactional
    public BlogResponse unarchiveBlog(Long blogId) {
        log.info("Unarchiving blog ID: {}", blogId);

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        blog.setStatus(Blog.BlogStatus.PUBLISHED);
        // Restore published date if it was previously published
        if (blog.getPublishedAt() == null) {
            blog.setPublishedAt(LocalDateTime.now());
        }
        blog = blogRepo.save(blog);

        return blogMapper.toBlogResponse(blog);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long blogId) {
        log.info("Incrementing view count for blog ID: {}", blogId);

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_FOUND));

        blog.setViewCount(blog.getViewCount() + 1);
        blogRepo.save(blog);
    }
}
