package com.fpt.careermate.services.blog_services.repository;

import com.fpt.careermate.services.blog_services.domain.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepo extends JpaRepository<Blog, Long> {
    Page<Blog> findByStatus(Blog.BlogStatus status, Pageable pageable);

    Page<Blog> findByCategory(String category, Pageable pageable);

    Page<Blog> findByAdmin_AdminId(int adminId, Pageable pageable);

    @Query("SELECT DISTINCT b.category FROM blog b WHERE b.category IS NOT NULL")
    List<String> findAllCategories();

    @Query("SELECT b FROM blog b WHERE " +
           "(:keyword IS NULL OR " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR b.status = :status)")
    Page<Blog> searchBlogs(@Param("keyword") String keyword,
                           @Param("status") Blog.BlogStatus status,
                           Pageable pageable);
}

