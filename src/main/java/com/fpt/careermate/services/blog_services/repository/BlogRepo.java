package com.fpt.careermate.services.blog_services.repository;

import com.fpt.careermate.services.blog_services.domain.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepo extends JpaRepository<Blog, Long> {
    Page<Blog> findByStatus(Blog.BlogStatus status, Pageable pageable);

    Page<Blog> findByCategory(String category, Pageable pageable);

    Page<Blog> findByAdmin_AdminId(int adminId, Pageable pageable);
    
    Optional<Blog> findBySlug(String slug);
    
    boolean existsBySlug(String slug);

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
                           
    @Query("SELECT b FROM blog b WHERE b.id <> :blogId AND " +
           "(b.category = :category OR " +
           "(:tags IS NOT NULL AND b.tags IS NOT NULL AND " +
           "(LOWER(b.tags) LIKE LOWER(CONCAT('%', :tags, '%')))) " +
           ") AND b.status = 'PUBLISHED' ORDER BY b.publishedAt DESC")
    List<Blog> findRelatedBlogs(@Param("blogId") Long blogId,
                                @Param("category") String category,
                                @Param("tags") String tags,
                                Pageable pageable);
}

