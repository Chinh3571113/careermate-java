package com.fpt.careermate.services.blog_services.service;

import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.blog_services.domain.Blog;
import com.fpt.careermate.services.blog_services.domain.BlogComment;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.blog_services.repository.BlogCommentRepo;
import com.fpt.careermate.services.blog_services.repository.BlogRepo;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogCommentRequest;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogCommentResponse;
import com.fpt.careermate.services.blog_services.service.mapper.BlogCommentMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
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

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogCommentImp {
    BlogCommentRepo blogCommentRepo;
    BlogRepo blogRepo;
    AccountRepo accountRepo;
    BlogCommentMapper blogCommentMapper;
    ContentModerationService contentModerationService;

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

        // Auto-flag inappropriate content
        ContentModerationService.ModerationResult moderation = contentModerationService
                .analyzeContent(request.getContent());

        if (moderation.shouldFlag) {
            comment.setIsFlagged(true);
            comment.setFlagReason(moderation.flagReason);
            comment.setFlaggedAt(java.time.LocalDateTime.now());
            comment.setReviewedByAdmin(false);
            log.warn("Comment auto-flagged for moderation. User: {}, Reason: {}",
                    email, moderation.flagReason);
        }

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

        Page<BlogComment> comments = blogCommentRepo.findByBlog_IdAndIsHiddenFalse(blogId, pageable);

        return comments.map(blogCommentMapper::toBlogCommentResponse);
    }

    public Page<BlogCommentResponse> getCommentsByBlogId(Long blogId, Pageable pageable) {
        log.info("Getting comments for blog ID: {} with pageable", blogId);

        if (!blogRepo.existsById(blogId)) {
            throw new AppException(ErrorCode.BLOG_NOT_EXISTED);
        }

        Page<BlogComment> comments = blogCommentRepo.findByBlog_IdAndIsHiddenFalse(blogId, pageable);

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

        // Re-check content for inappropriate content on update
        ContentModerationService.ModerationResult moderation = contentModerationService
                .analyzeContent(request.getContent());

        if (moderation.shouldFlag) {
            comment.setIsFlagged(true);
            comment.setFlagReason(moderation.flagReason);
            comment.setFlaggedAt(java.time.LocalDateTime.now());
            comment.setReviewedByAdmin(false);
            log.warn("Updated comment auto-flagged for moderation. User: {}, Reason: {}",
                    email, moderation.flagReason);
        } else if (comment.getIsFlagged() && comment.getReviewedByAdmin()) {
            // If previously flagged but now clean, keep admin review status
            // Admin can decide whether to unflag
        }

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
        Long commentCount = blogCommentRepo.countByBlog_IdAndIsHiddenFalse(blog.getId());
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
        log.info("Admin permanently deleting comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        Blog blog = comment.getBlog();
        blogCommentRepo.delete(comment);

        // Update blog's comment count
        updateBlogCommentCount(blog);

        log.info("Comment permanently deleted by admin: {}", commentId);
    }

    @Transactional
    public BlogCommentResponse hideComment(Long commentId) {
        log.info("Admin hiding comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        comment.setIsHidden(true);
        blogCommentRepo.save(comment);

        // Update blog's comment count
        updateBlogCommentCount(comment.getBlog());

        return blogCommentMapper.toBlogCommentResponse(comment);
    }

    @Transactional
    public BlogCommentResponse showComment(Long commentId) {
        log.info("Admin showing comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        comment.setIsHidden(false);
        blogCommentRepo.save(comment);

        // Update blog's comment count
        updateBlogCommentCount(comment.getBlog());

        return blogCommentMapper.toBlogCommentResponse(comment);
    }

    public Object getCommentStatistics() {
        log.info("Admin getting comment statistics");

        Long totalComments = blogCommentRepo.count();
        Long visibleComments = blogCommentRepo.countByIsHiddenFalse();
        Long hiddenComments = blogCommentRepo.countByIsHiddenTrue();
        Long flaggedComments = blogCommentRepo.countByIsFlaggedTrue();
        Long pendingReviewComments = blogCommentRepo.countByIsFlaggedTrueAndReviewedByAdminFalse();

        return new Object() {
            public final Long total = totalComments;
            public final Long visible = visibleComments;
            public final Long hidden = hiddenComments;
            public final Long flagged = flaggedComments;
            public final Long pendingReview = pendingReviewComments;
        };
    }

    // ==================== AUTO-FLAGGING MODERATION METHODS ====================

    /**
     * Get all flagged comments pending admin review
     * Primary method for admin moderation dashboard
     */
    public Page<BlogCommentResponse> getFlaggedCommentsPendingReview(Pageable pageable) {
        log.info("Admin getting flagged comments pending review - page: {}", pageable.getPageNumber());

        Page<BlogComment> flaggedComments = blogCommentRepo
                .findByIsFlaggedTrueAndReviewedByAdminFalseOrderByFlaggedAtDesc(pageable);

        return flaggedComments.map(this::toBlogCommentResponseWithModerationInfo);
    }

    /**
     * Search/filter flagged comments with optional filters
     */
    public Page<BlogCommentResponse> searchFlaggedComments(String userEmail, Long blogId, Pageable pageable) {
        log.info("Admin searching flagged comments - userEmail: {}, blogId: {}", userEmail, blogId);

        Page<BlogComment> flaggedComments = blogCommentRepo
                .searchFlaggedComments(userEmail, blogId, pageable);

        return flaggedComments.map(this::toBlogCommentResponseWithModerationInfo);
    }

    /**
     * Get all flagged comments (including reviewed ones)
     */
    public Page<BlogCommentResponse> getAllFlaggedComments(Pageable pageable) {
        log.info("Admin getting all flagged comments - page: {}", pageable.getPageNumber());

        Page<BlogComment> flaggedComments = blogCommentRepo
                .findByIsFlaggedTrueOrderByFlaggedAtDesc(pageable);

        return flaggedComments.map(this::toBlogCommentResponseWithModerationInfo);
    }

    /**
     * Approve flagged comment (mark as reviewed, unflag, and show)
     */
    @Transactional
    public BlogCommentResponse approveFlaggedComment(Long commentId) {
        log.info("Admin approving flagged comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        comment.setIsFlagged(false);
        comment.setReviewedByAdmin(true);
        comment.setIsHidden(false);
        comment.setFlagReason(null);
        comment.setFlaggedAt(null);

        blogCommentRepo.save(comment);

        // Update blog comment count if it was hidden
        updateBlogCommentCount(comment.getBlog());

        log.info("Comment approved and unflagged by admin: {}", commentId);
        return blogCommentMapper.toBlogCommentResponse(comment);
    }

    /**
     * Reject flagged comment (mark as reviewed and hide)
     */
    @Transactional
    public BlogCommentResponse rejectFlaggedComment(Long commentId) {
        log.info("Admin rejecting flagged comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        comment.setReviewedByAdmin(true);
        comment.setIsHidden(true);

        blogCommentRepo.save(comment);

        // Update blog comment count
        updateBlogCommentCount(comment.getBlog());

        log.info("Comment rejected and hidden by admin: {}", commentId);
        return blogCommentMapper.toBlogCommentResponse(comment);
    }

    /**
     * Unflag comment without hiding (manual unflag)
     */
    @Transactional
    public BlogCommentResponse unflagComment(Long commentId) {
        log.info("Admin manually unflagging comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        comment.setIsFlagged(false);
        comment.setReviewedByAdmin(true);
        comment.setFlagReason(null);
        comment.setFlaggedAt(null);

        blogCommentRepo.save(comment);

        log.info("Comment unflagged by admin: {}", commentId);
        return blogCommentMapper.toBlogCommentResponse(comment);
    }

    /**
     * Get moderation statistics for dashboard
     */
    public Object getModerationStatistics() {
        log.info("Admin getting moderation statistics");

        Long totalFlagged = blogCommentRepo.countByIsFlaggedTrue();
        Long pendingReview = blogCommentRepo.countByIsFlaggedTrueAndReviewedByAdminFalse();
        Map<String, Object> moderationRules = contentModerationService.getModerationStats();

        return new Object() {
            public final Long totalFlaggedComments = totalFlagged;
            public final Long pendingReviewComments = pendingReview;
            public final Long reviewedComments = totalFlagged - pendingReview;
            public final Map<String, Object> automationRules = moderationRules;
        };
    }

    /**
     * Helper method to add moderation info to response
     */
    private BlogCommentResponse toBlogCommentResponseWithModerationInfo(BlogComment comment) {
        BlogCommentResponse response = blogCommentMapper.toBlogCommentResponse(comment);

        // Calculate severity if flagged
        if (comment.getIsFlagged() && comment.getFlagReason() != null) {
            ContentModerationService.ModerationResult result = new ContentModerationService.ModerationResult(true,
                    comment.getFlagReason());
            int severity = contentModerationService.calculateSeverityScore(result);
            String priority = contentModerationService.getPriorityLevel(severity);

            // Add moderation metadata to response (you may want to create a specific DTO
            // for this)
            log.debug("Comment {} severity: {}, priority: {}", comment.getId(), severity, priority);
        }

        return response;
    }
}
