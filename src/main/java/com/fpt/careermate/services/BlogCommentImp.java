package com.fpt.careermate.services;

import com.fpt.careermate.domain.Account;
import com.fpt.careermate.domain.Blog;
import com.fpt.careermate.domain.BlogComment;
import com.fpt.careermate.repository.AccountRepo;
import com.fpt.careermate.repository.BlogCommentRepo;
import com.fpt.careermate.repository.BlogRepo;
import com.fpt.careermate.services.dto.request.BlogCommentRequest;
import com.fpt.careermate.services.dto.response.BlogCommentResponse;
import com.fpt.careermate.services.mapper.BlogCommentMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogCommentImp {
    BlogCommentRepo blogCommentRepo;
    BlogRepo blogRepo;
    AccountRepo accountRepo;
    BlogCommentMapper blogCommentMapper;

    @Transactional
    public BlogCommentResponse createComment(Long blogId, BlogCommentRequest request) {
        log.info("Creating comment for blog ID: {}", blogId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Account user = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_EXISTED));

        if (blog.getStatus() != Blog.BlogStatus.PUBLISHED) {
            throw new AppException(ErrorCode.BLOG_NOT_PUBLISHED);
        }

        BlogComment comment = blogCommentMapper.toBlogComment(request);
        comment.setBlog(blog);
        comment.setUser(user);

        comment = blogCommentRepo.save(comment);

        // Update blog's comment count
        updateBlogCommentCount(blog);

        return blogCommentMapper.toBlogCommentResponse(comment);
    }

    public Page<BlogCommentResponse> getCommentsByBlog(Long blogId, int page, int size, String sortBy, String sortDir) {
        log.info("Getting comments for blog ID: {} (page: {}, size: {})", blogId, page, size);

        if (!blogRepo.existsById(blogId)) {
            throw new AppException(ErrorCode.BLOG_NOT_EXISTED);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BlogComment> comments = blogCommentRepo.findByBlog_IdAndIsDeletedFalse(blogId, pageable);

        return comments.map(blogCommentMapper::toBlogCommentResponse);
    }

    public Page<BlogCommentResponse> getCommentsByBlogId(Long blogId, Pageable pageable) {
        log.info("Getting comments for blog ID: {} with pageable", blogId);

        if (!blogRepo.existsById(blogId)) {
            throw new AppException(ErrorCode.BLOG_NOT_EXISTED);
        }

        Page<BlogComment> comments = blogCommentRepo.findByBlog_IdAndIsDeletedFalse(blogId, pageable);

        return comments.map(blogCommentMapper::toBlogCommentResponse);
    }

    @Transactional
    public BlogCommentResponse updateComment(Long commentId, BlogCommentRequest request) {
        log.info("Updating comment ID: {}", commentId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        if (!comment.getUser().getEmail().equals(email)) {
            throw new AppException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.setContent(request.getContent());

        comment = blogCommentRepo.save(comment);

        return blogCommentMapper.toBlogCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        log.info("Deleting comment ID: {}", commentId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        Account user = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Allow deletion if user is the comment author or an admin
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!comment.getUser().getEmail().equals(email) && !isAdmin) {
            throw new AppException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        blogCommentRepo.delete(comment);

        // Update blog's comment count
        updateBlogCommentCount(comment.getBlog());
    }

    public BlogCommentResponse getCommentById(Long commentId) {
        log.info("Getting comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        return blogCommentMapper.toBlogCommentResponse(comment);
    }

    private void updateBlogCommentCount(Blog blog) {
        Long commentCount = blogCommentRepo.countByBlog_IdAndIsDeletedFalse(blog.getId());
        blog.setCommentCount(commentCount.intValue());
        blogRepo.save(blog);

        log.info("Updated blog comment count: {}", commentCount);
    }

    // Admin management methods
    public Page<BlogCommentResponse> getAllCommentsForAdmin(Pageable pageable, Long blogId, String userEmail) {
        log.info("Admin getting all comments - page: {}, blogId: {}, userEmail: {}",
                pageable.getPageNumber(), blogId, userEmail);

        Page<BlogComment> comments;
        if (blogId != null && userEmail != null) {
            comments = blogCommentRepo.findByBlogIdAndUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(
                    blogId, userEmail, pageable);
        } else if (blogId != null) {
            comments = blogCommentRepo.findByBlogIdOrderByCreatedAtDesc(blogId, pageable);
        } else if (userEmail != null) {
            comments = blogCommentRepo.findByUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(userEmail, pageable);
        } else {
            comments = blogCommentRepo.findAllByOrderByCreatedAtDesc(pageable);
        }

        return comments.map(blogCommentMapper::toBlogCommentResponse);
    }

    @Transactional
    public void deleteCommentAsAdmin(Long commentId) {
        log.info("Admin deleting comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        comment.setIsDeleted(true);
        blogCommentRepo.save(comment);

        // Update blog's comment count
        updateBlogCommentCount(comment.getBlog());

        log.info("Comment deleted by admin: {}", commentId);
    }

    @Transactional
    public BlogCommentResponse hideComment(Long commentId) {
        log.info("Admin hiding comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        comment.setIsDeleted(true); // Using isDeleted as hidden flag
        blogCommentRepo.save(comment);

        return blogCommentMapper.toBlogCommentResponse(comment);
    }

    @Transactional
    public BlogCommentResponse showComment(Long commentId) {
        log.info("Admin showing comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        comment.setIsDeleted(false);
        blogCommentRepo.save(comment);

        return blogCommentMapper.toBlogCommentResponse(comment);
    }

    public Object getCommentStatistics() {
        log.info("Admin getting comment statistics");

        Long totalComments = blogCommentRepo.count();
        Long visibleComments = blogCommentRepo.countByIsDeletedFalse();
        Long hiddenComments = blogCommentRepo.countByIsDeletedTrue();

        return new Object() {
            public final Long total = totalComments;
            public final Long visible = visibleComments;
            public final Long hidden = hiddenComments;
        };
    }
}
