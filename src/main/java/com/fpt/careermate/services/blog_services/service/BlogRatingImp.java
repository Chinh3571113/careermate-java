package com.fpt.careermate.services.blog_services.service;

import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.blog_services.domain.Blog;
import com.fpt.careermate.services.blog_services.domain.BlogRating;
import com.fpt.careermate.services.account_services.repository.AccountRepo;
import com.fpt.careermate.services.blog_services.repository.BlogRatingRepo;
import com.fpt.careermate.services.blog_services.repository.BlogRepo;
import com.fpt.careermate.services.blog_services.service.dto.request.BlogRatingRequest;
import com.fpt.careermate.services.blog_services.service.dto.response.BlogRatingResponse;
import com.fpt.careermate.services.blog_services.service.mapper.BlogRatingMapper;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Admin management methods
    public Page<BlogRating> getAllRatingsForAdmin(int page, int size, String sortBy, String sortDirection,
                                                  String startDateStr, String endDateStr, String userEmail,
                                                  Long blogId, Integer ratingValue) {
        log.info("Getting all ratings for admin - Page: {}, Size: {}, Sort: {} {}, StartDate: {}, EndDate: {}, UserEmail: {}, BlogId: {}, Rating: {}", 
                page, size, sortBy, sortDirection, startDateStr, endDateStr, userEmail, blogId, ratingValue);

        Specification<BlogRating> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by userEmail if provided (case-insensitive partial match)
            if (userEmail != null && !userEmail.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("userEmail")), "%" + userEmail.toLowerCase() + "%"));
            }

            // Filter by blogId if provided
            if (blogId != null) {
                predicates.add(cb.equal(root.get("blog").get("id"), blogId));
            }

            // Filter by rating value if provided
            if (ratingValue != null) {
                predicates.add(cb.equal(root.get("rating"), ratingValue));
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

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        return blogRatingRepo.findAll(spec, pageable);
    }

    public BlogRating getRatingById(Long id) {
        log.info("Getting rating by ID for admin: {}", id);
        return blogRatingRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found with ID: " + id));
    }

    public void deleteRatingAsAdmin(Long id) {
        log.info("Admin deleting rating with ID: {}", id);

        BlogRating rating = blogRatingRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rating not found with ID: " + id));

        Blog blog = rating.getBlog();
        blogRatingRepo.delete(rating);

        // Update blog stats after deletion
        updateBlogRatingStats(blog);

        log.info("Rating deleted successfully by admin");
    }

    public Map<String, Object> getRatingStatistics() {
        log.info("Getting rating statistics for admin");

        Map<String, Object> stats = new HashMap<>();

        long totalRatings = blogRatingRepo.count();
        stats.put("totalRatings", totalRatings);

        // Get rating distribution
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, blogRatingRepo.countByRating(i));
        }
        stats.put("ratingDistribution", ratingDistribution);

        // Calculate average rating across all blogs
        Double overallAverageRating = blogRatingRepo.findOverallAverageRating();
        stats.put("overallAverageRating", overallAverageRating != null ? overallAverageRating : 0.0);

        // Get top rated blogs
        List<Object[]> topRatedBlogs = blogRatingRepo.findTopRatedBlogs(PageRequest.of(0, 5));
        stats.put("topRatedBlogs", topRatedBlogs);

        log.info("Rating statistics calculated: {} total ratings", totalRatings);
        return stats;
    }

    public Map<String, Object> getBlogRatingSummary(Long blogId) {
        log.info("Getting rating summary for blog ID: {}", blogId);

        Map<String, Object> summary = new HashMap<>();

        Blog blog = blogRepo.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found with ID: " + blogId));

        Long totalRatings = blogRatingRepo.countByBlog_Id(blogId);
        Double averageRating = blogRatingRepo.findAverageRatingByBlog_Id(blogId);

        summary.put("blogId", blogId);
        summary.put("blogTitle", blog.getTitle());
        summary.put("totalRatings", totalRatings);
        summary.put("averageRating", averageRating != null ? averageRating : 0.0);

        // Get rating distribution for this blog
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, blogRatingRepo.countByBlogIdAndRating(blogId, i));
        }
        summary.put("ratingDistribution", ratingDistribution);

        // Get recent ratings for this blog
        List<BlogRating> recentRatings = blogRatingRepo.findByBlogIdOrderByCreatedAtDesc(blogId, PageRequest.of(0, 5));
        summary.put("recentRatings", recentRatings);

        log.info("Blog rating summary calculated for blog: {}", blogId);
        return summary;
    }
}
