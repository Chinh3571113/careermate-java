package com.fpt.careermate.repository;

import com.fpt.careermate.domain.BlogComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogCommentRepo extends JpaRepository<BlogComment, Long> {
    Page<BlogComment> findByBlog_IdAndIsDeletedFalse(Long blogId, Pageable pageable);

    Long countByBlog_IdAndIsDeletedFalse(Long blogId);
}

