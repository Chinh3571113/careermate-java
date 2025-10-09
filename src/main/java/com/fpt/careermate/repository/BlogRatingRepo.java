package com.fpt.careermate.repository;

import com.fpt.careermate.domain.BlogRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogRatingRepo extends JpaRepository<BlogRating, Long> {
    Optional<BlogRating> findByBlog_IdAndUser_Id(Long blogId, Long userId);

    @Query("SELECT AVG(r.rating) FROM blog_rating r WHERE r.blog.id = :blogId")
    Double findAverageRatingByBlog_Id(@Param("blogId") Long blogId);

    Long countByBlog_Id(Long blogId);
}

