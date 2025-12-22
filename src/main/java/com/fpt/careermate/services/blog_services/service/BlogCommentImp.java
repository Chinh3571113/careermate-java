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
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    SemanticToxicityAnalyzer semanticToxicityAnalyzer;

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
            comment.setIsHidden(true); // Auto-hide flagged comments
            comment.setFlagReason(moderation.flagReason);
            comment.setFlaggedAt(java.time.LocalDateTime.now());
            comment.setReviewedByAdmin(false);
            log.warn("Comment auto-flagged and hidden for moderation. User: {}, Reason: {}",
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
            comment.setIsHidden(true); // Auto-hide flagged comments
            comment.setFlagReason(moderation.flagReason);
            comment.setFlaggedAt(java.time.LocalDateTime.now());
            comment.setReviewedByAdmin(false);
            log.warn("Updated comment auto-flagged and hidden for moderation. User: {}, Reason: {}",
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
    public Page<BlogCommentResponse> getAllCommentsForAdmin(Pageable pageable, Long blogId, String userEmail, 
                                                            String content, String startDateStr, String endDateStr) {
        log.info("Admin getting all comments - page: {}, blogId: {}, userEmail: {}, content: {}, startDate: {}, endDate: {}",
                pageable.getPageNumber(), blogId, userEmail, content, startDateStr, endDateStr);

        Specification<BlogComment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by blogId if provided
            if (blogId != null) {
                predicates.add(cb.equal(root.get("blog").get("id"), blogId));
            }

            // Filter by userEmail if provided (case-insensitive partial match)
            if (userEmail != null && !userEmail.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("userEmail")), "%" + userEmail.toLowerCase() + "%"));
            }

            // Filter by content if provided (case-insensitive partial match)
            if (content != null && !content.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("content")), "%" + content.toLowerCase() + "%"));
            }

            // Filter by start date
            if (startDateStr != null && !startDateStr.isEmpty()) {
                LocalDate startDate = LocalDate.parse(startDateStr);
                LocalDateTime startDateTime = startDate.atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }

            // Filter by end date
            if (endDateStr != null && !endDateStr.isEmpty()) {
                LocalDate endDate = LocalDate.parse(endDateStr);
                LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<BlogComment> comments = blogCommentRepo.findAll(spec, pageable);
        return comments.map(blogCommentMapper::toBlogCommentResponse);
    }

    // Added overload: keep compatibility with older test expectations (pageable, blogId, userEmail)
    public Page<BlogCommentResponse> getAllCommentsForAdmin(Pageable pageable, Long blogId, String userEmail) {
        return getAllCommentsForAdmin(pageable, blogId, userEmail, null, null, null);
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
     * Search/filter flagged comments with optional filters including date range and content
     */
    public Page<BlogCommentResponse> searchFlaggedComments(String userEmail, Long blogId, String content, 
                                                           String startDateStr, String endDateStr, Pageable pageable) {
        log.info("Admin searching flagged comments - userEmail: {}, blogId: {}, content: {}, startDate: {}, endDate: {}", 
                userEmail, blogId, content, startDateStr, endDateStr);

        Specification<BlogComment> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Must be flagged
            predicates.add(cb.equal(root.get("isFlagged"), true));
            
            // User email filter
            if (userEmail != null && !userEmail.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("user").get("email")), 
                    "%" + userEmail.toLowerCase() + "%"));
            }
            
            // Blog ID filter
            if (blogId != null) {
                predicates.add(cb.equal(root.get("blog").get("id"), blogId));
            }
            
            // Content filter (case-insensitive partial match)
            if (content != null && !content.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("content")), 
                    "%" + content.toLowerCase() + "%"));
            }
            
            // Date range filter (expects YYYY-MM-DD format)
            if (startDateStr != null && !startDateStr.isEmpty()) {
                try {
                    LocalDate startDate = LocalDate.parse(startDateStr);
                    LocalDateTime startDateTime = startDate.atStartOfDay();
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
                } catch (Exception e) {
                    log.warn("Invalid start date: {}", startDateStr);
                }
            }
            
            if (endDateStr != null && !endDateStr.isEmpty()) {
                try {
                    LocalDate endDate = LocalDate.parse(endDateStr);
                    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
                } catch (Exception e) {
                    log.warn("Invalid end date: {}", endDateStr);
                }
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<BlogComment> flaggedComments = blogCommentRepo.findAll(spec, pageable);
        return flaggedComments.map(this::toBlogCommentResponseWithModerationInfo);
    }

    // Added overload to support tests: (userEmail, blogId, pageable)
    public Page<BlogCommentResponse> searchFlaggedComments(String userEmail, Long blogId, Pageable pageable) {
        return searchFlaggedComments(userEmail, blogId, null, null, null, pageable);
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

        // Update blog's comment count
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

    /**
     * Semantic Toxicity Analysis Methods
     */
    
    @Transactional
    public Map<String, Object> analyzeCommentToxicity(Long commentId) {
        log.info("Analyzing toxicity for comment ID: {}", commentId);

        BlogComment comment = blogCommentRepo.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_EXISTED));

        SemanticToxicityAnalyzer.ToxicityScore score = semanticToxicityAnalyzer.analyzeToxicity(
                comment.getContent(), comment.getFlagReason());

        // Update comment with toxicity score
        comment.setToxicityScore(score.getToxicityScore());
        comment.setToxicityConfidence(score.getConfidence().name());
        comment.setAnalyzedAt(LocalDateTime.now());
        blogCommentRepo.save(comment);

        return Map.of(
                "commentId", commentId,
                "toxicityScore", score.getToxicityScore(),
                "confidence", score.getConfidence(),
                "reasoning", score.getReasoning(),
                "toxicSimilarity", score.getToxicSimilarity(),
                "positiveSimilarity", score.getPositiveSimilarity()
        );
    }

    @Transactional
    public Map<String, Object> batchAnalyzeToxicity(List<Long> commentIds) {
        log.info("Batch analyzing toxicity for {} comments", commentIds.size());

        List<BlogComment> comments = blogCommentRepo.findAllById(commentIds);
        
        Map<Long, String> commentTexts = new java.util.HashMap<>();
        for (BlogComment comment : comments) {
            commentTexts.put(comment.getId(), comment.getContent());
        }

        Map<Long, SemanticToxicityAnalyzer.ToxicityScore> scores = 
                semanticToxicityAnalyzer.analyzeBatch(commentTexts);

        // Update all comments with scores
        List<Map<String, Object>> results = new ArrayList<>();
        for (BlogComment comment : comments) {
            SemanticToxicityAnalyzer.ToxicityScore score = scores.get(comment.getId());
            if (score != null) {
                comment.setToxicityScore(score.getToxicityScore());
                comment.setToxicityConfidence(score.getConfidence().name());
                comment.setAnalyzedAt(LocalDateTime.now());
                
                results.add(Map.of(
                        "commentId", comment.getId(),
                        "toxicityScore", score.getToxicityScore(),
                        "confidence", score.getConfidence(),
                        "reasoning", score.getReasoning()
                ));
            }
        }
        
        blogCommentRepo.saveAll(comments);

        return Map.of(
                "totalAnalyzed", results.size(),
                "results", results
        );
    }

    @Transactional
    public Map<String, Object> bulkActionComments(String action, List<Long> commentIds, 
                                                   Double minToxicity, String confidence) {
        log.info("Performing bulk action: {} on {} comments", action, commentIds.size());

        List<BlogComment> comments = blogCommentRepo.findAllById(commentIds);
        
        // Filter by toxicity score if provided
        List<BlogComment> filteredComments = comments;
        if (minToxicity != null || confidence != null) {
            filteredComments = comments.stream()
                    .filter(c -> {
                        boolean matchToxicity = minToxicity == null || 
                                (c.getToxicityScore() != null && c.getToxicityScore() >= minToxicity);
                        boolean matchConfidence = confidence == null || 
                                (c.getToxicityConfidence() != null && c.getToxicityConfidence().equals(confidence));
                        return matchToxicity && matchConfidence;
                    })
                    .toList();
        }

        int actioned = 0;
        switch (action.toLowerCase()) {
            case "hide":
                for (BlogComment comment : filteredComments) {
                    comment.setIsHidden(true);
                    actioned++;
                }
                break;
            case "show":
                for (BlogComment comment : filteredComments) {
                    comment.setIsHidden(false);
                    actioned++;
                }
                break;
            case "delete":
                for (BlogComment comment : filteredComments) {
                    blogCommentRepo.delete(comment);
                    updateBlogCommentCount(comment.getBlog());
                    actioned++;
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + action);
        }

        if (!action.equalsIgnoreCase("delete")) {
            blogCommentRepo.saveAll(filteredComments);
        }

        return Map.of(
                "action", action,
                "totalComments", commentIds.size(),
                "filtered", filteredComments.size(),
                "actioned", actioned
        );
    }
}
