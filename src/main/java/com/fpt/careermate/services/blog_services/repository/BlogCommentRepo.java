package com.fpt.careermate.services.blog_services.repository;

import com.fpt.careermate.services.blog_services.domain.BlogComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogCommentRepo extends JpaRepository<BlogComment, Long> {
    Page<BlogComment> findByBlog_IdAndIsHiddenFalse(Long blogId, Pageable pageable);

    Long countByBlogIdAndIsHiddenFalse(Long blogId);

    // Admin methods for comment management
    Page<BlogComment> findByBlogIdAndUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(Long blogId, String userEmail,
            Pageable pageable);

    Page<BlogComment> findByBlogIdOrderByCreatedAtDesc(Long blogId, Pageable pageable);

    Page<BlogComment> findByUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(String userEmail, Pageable pageable);

    Page<BlogComment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Long countByIsHiddenFalse();

    Long countByIsHiddenTrue();
    Long countByBlog_IdAndIsHiddenFalse(Long blogId);
}

