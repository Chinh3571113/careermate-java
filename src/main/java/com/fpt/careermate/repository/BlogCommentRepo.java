package com.fpt.careermate.repository;

import com.fpt.careermate.domain.BlogComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogCommentRepo extends JpaRepository<BlogComment, Long> {
    Page<BlogComment> findByBlog_IdAndIsDeletedFalse(Long blogId, Pageable pageable);

    Long countByBlogIdAndIsDeletedFalse(Long blogId);

    // Admin methods for comment management
    Page<BlogComment> findByBlogIdAndUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(Long blogId, String userEmail,
            Pageable pageable);

    Page<BlogComment> findByBlogIdOrderByCreatedAtDesc(Long blogId, Pageable pageable);

    Page<BlogComment> findByUserEmailContainingIgnoreCaseOrderByCreatedAtDesc(String userEmail, Pageable pageable);

    Page<BlogComment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Long countByIsDeletedFalse();

    Long countByIsDeletedTrue();
    Long countByBlog_IdAndIsDeletedFalse(Long blogId);
}

