package com.fpt.careermate.services;

import com.fpt.careermate.domain.Account;
import com.fpt.careermate.domain.Blog;
import com.fpt.careermate.domain.BlogRating;
import com.fpt.careermate.repository.AccountRepo;
import com.fpt.careermate.repository.BlogRatingRepo;
import com.fpt.careermate.repository.BlogRepo;
import com.fpt.careermate.services.dto.request.BlogRatingRequest;
import com.fpt.careermate.services.dto.response.BlogRatingResponse;
import com.fpt.careermate.services.mapper.BlogRatingMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BlogRatingImp {
    BlogRatingRepo blogRatingRepo;
    BlogRepo blogRepo;
    AccountRepo accountRepo;
    BlogRatingMapper blogRatingMapper;

    @Transactional
    public BlogRatingResponse rateBlog(Long blogId, BlogRatingRequest request) {
        log.info("Rating blog ID: {} with rating: {}", blogId, request.getRating());

        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Account user = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check if blog exists and is published
        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new AppException(ErrorCode.BLOG_NOT_EXISTED));

        if (blog.getStatus() != Blog.BlogStatus.PUBLISHED) {
            throw new AppException(ErrorCode.BLOG_NOT_PUBLISHED);
        }

        // Check if user has already rated this blog
        Optional<BlogRating> existingRating = blogRatingRepo.findByBlog_IdAndUser_Id(blogId, Long.valueOf(user.getId()));

        BlogRating rating;
        if (existingRating.isPresent()) {
            // Update existing rating
            rating = existingRating.get();
            rating.setRating(request.getRating());
            log.info("Updating existing rating for user ID: {}", user.getId());
        } else {
            // Create new rating
            rating = blogRatingMapper.toBlogRating(request);
            rating.setBlog(blog);
            rating.setUser(user);
            log.info("Creating new rating for user ID: {}", user.getId());
        }

        BlogRating savedRating = blogRatingRepo.save(rating);

        // Update blog's average rating and rating count
        updateBlogRatingStats(blog);

        log.info("Rating saved successfully");
        return blogRatingMapper.toBlogRatingResponse(savedRating);
    }

    @Transactional(readOnly = true)
    public BlogRatingResponse getUserRating(Long blogId) {
        log.info("Getting user rating for blog ID: {}", blogId);

        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Account user = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check if blog exists
        if (!blogRepo.existsById(blogId)) {
            throw new AppException(ErrorCode.BLOG_NOT_EXISTED);
        }

        BlogRating rating = blogRatingRepo.findByBlog_IdAndUser_Id(blogId, Long.valueOf(user.getId()))
                .orElseThrow(() -> new AppException(ErrorCode.RATING_NOT_EXISTED));

        return blogRatingMapper.toBlogRatingResponse(rating);
    }

    @Transactional
    public void deleteRating(Long blogId) {
        log.info("Deleting rating for blog ID: {}", blogId);

        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Account user = accountRepo.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        BlogRating rating = blogRatingRepo.findByBlog_IdAndUser_Id(blogId, Long.valueOf(user.getId()))
                .orElseThrow(() -> new AppException(ErrorCode.RATING_NOT_EXISTED));

        Blog blog = rating.getBlog();
        blogRatingRepo.delete(rating);

        // Update blog's average rating and rating count
        updateBlogRatingStats(blog);

        log.info("Rating deleted successfully");
    }

    private void updateBlogRatingStats(Blog blog) {
        Double averageRating = blogRatingRepo.findAverageRatingByBlog_Id(blog.getId());
        Long ratingCount = blogRatingRepo.countByBlog_Id(blog.getId());

        blog.setAverageRating(averageRating != null ? averageRating : 0.0);
        blog.setRatingCount(ratingCount.intValue());
        blogRepo.save(blog);

        log.info("Updated blog rating stats - Average: {}, Count: {}", averageRating, ratingCount);
    }
}
