package com.fpt.careermate.services.blog_services.repository;

import com.fpt.careermate.services.blog_services.domain.BlogComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogCommentRepo extends JpaRepository<BlogComment, Long> {
        Page<BlogComment> findByBlog_IdAndIsHiddenFalse(Long blogId, Pageable pageable);

        Long countByBlogIdAndIsHiddenFalse(Long blogId);

        // Admin methods for comment management
        Page<BlogComment> findByBlogIdAndUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(Long blogId,
                        String userEmail,
                        Pageable pageable);

        Page<BlogComment> findByBlogIdOrderByCreatedAtDesc(Long blogId, Pageable pageable);

        Page<BlogComment> findByUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(String userEmail, Pageable pageable);

        Page<BlogComment> findAllByOrderByCreatedAtDesc(Pageable pageable);

        Long countByIsHiddenFalse();

        Long countByIsHiddenTrue();

        Long countByBlog_IdAndIsHiddenFalse(Long blogId);

        // Auto-flagging moderation methods
        Page<BlogComment> findByIsFlaggedTrueAndReviewedByAdminFalseOrderByFlaggedAtDesc(Pageable pageable);

        Page<BlogComment> findByIsFlaggedTrueOrderByFlaggedAtDesc(Pageable pageable);

        Long countByIsFlaggedTrueAndReviewedByAdminFalse();

        Long countByIsFlaggedTrue();

        // Search/filter flagged comments with optional filters (using JPQL for proper
        // sorting support)
        @Query("SELECT bc FROM blog_comment bc " +
                        "LEFT JOIN bc.user u " +
                        "WHERE bc.isFlagged = true AND " +
                        "(:userEmail IS NULL OR :userEmail = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :userEmail, '%'))) AND "
                        +
                        "(:blogId IS NULL OR bc.blog.id = :blogId)")
        Page<BlogComment> searchFlaggedComments(@Param("userEmail") String userEmail,
                        @Param("blogId") Long blogId,
                        Pageable pageable);
}
