package com.fpt.careermate.services.review_services.service.impl;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import com.fpt.careermate.services.review_services.domain.CompanyReview;
import com.fpt.careermate.services.review_services.repository.CompanyReviewRepo;
import com.fpt.careermate.services.review_services.service.RecruiterReviewService;
import com.fpt.careermate.services.review_services.service.dto.response.AdminReviewResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruiterReviewServiceImpl implements RecruiterReviewService {
    
    private final CompanyReviewRepo reviewRepo;
    private final RecruiterRepo recruiterRepo;
    
    @Override
    @Transactional(readOnly = true)
    public Page<AdminReviewResponse> getRecruiterCompanyReviews(
            Integer recruiterId,
            int page,
            int size,
            String reviewTypeStr,
            String startDateStr,
            String endDateStr,
            Integer rating,
            Integer maxRating,
            String searchText) {
        
        // Verify recruiter exists
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));
        
        log.info("📅 Date Filter Debug - startDate: {}, endDate: {}", startDateStr, endDateStr);
        
        Specification<CompanyReview> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by recruiter (company)
            predicates.add(cb.equal(root.get("recruiter").get("id"), recruiterId));
            
            // Only show ACTIVE reviews to recruiters (hide HIDDEN, REMOVED, etc.)
            predicates.add(cb.equal(root.get("status"), ReviewStatus.ACTIVE));
            
            // Review type filter
            if (reviewTypeStr != null && !reviewTypeStr.isEmpty()) {
                try {
                    ReviewType reviewType = ReviewType.valueOf(reviewTypeStr.toUpperCase());
                    predicates.add(cb.equal(root.get("reviewType"), reviewType));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid review type: {}", reviewTypeStr);
                }
            }
            
            // Date range filter (expects YYYY-MM-DD format from frontend)
            if (startDateStr != null && !startDateStr.isEmpty()) {
                try {
                    LocalDate startDate = LocalDate.parse(startDateStr);
                    LocalDateTime startDateTime = startDate.atStartOfDay();
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
                    log.info("✅ Start date filter applied: {} -> {}", startDateStr, startDateTime);
                } catch (Exception e) {
                    log.warn("❌ Invalid start date: {}", startDateStr, e);
                }
            }
            
            if (endDateStr != null && !endDateStr.isEmpty()) {
                try {
                    LocalDate endDate = LocalDate.parse(endDateStr);
                    LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
                    log.info("✅ End date filter applied: {} -> {}", endDateStr, endDateTime);
                } catch (Exception e) {
                    log.warn("❌ Invalid end date: {}", endDateStr, e);
                }
            }
            
            // Rating filters
            if (rating != null) {
                predicates.add(cb.equal(root.get("overallRating"), rating));
            }
            if (maxRating != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("overallRating"), maxRating));
            }
            
            // Search text filter (search in review text and job title)
            if (searchText != null && !searchText.isEmpty()) {
                String searchPattern = "%" + searchText.toLowerCase() + "%";
                Predicate reviewTextPredicate = cb.like(cb.lower(root.get("reviewText")), searchPattern);
                Predicate jobTitlePredicate = cb.like(cb.lower(root.get("jobApply").get("jobPosting").get("title")), searchPattern);
                predicates.add(cb.or(reviewTextPredicate, jobTitlePredicate));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CompanyReview> reviews = reviewRepo.findAll(spec, pageable);
        
        return reviews.map(this::mapToAdminResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRecruiterReviewStatistics(Integer recruiterId) {
        // Verify recruiter exists
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));
        
        Map<String, Object> stats = new HashMap<>();
        
        // Only count ACTIVE reviews (exclude hidden/removed)
        long totalActiveReviews = reviewRepo.countByRecruiterIdAndStatus(recruiterId, ReviewStatus.ACTIVE);
        stats.put("totalActiveReviews", totalActiveReviews);
        
        // Count by review type (active only)
        Arrays.stream(ReviewType.values()).forEach(type -> {
            long count = reviewRepo.countByRecruiterIdAndReviewTypeAndStatus(recruiterId, type, ReviewStatus.ACTIVE);
            stats.put(type.name().toLowerCase() + "Count", count);
        });
        
        // Average ratings (active only)
        Double avgOverallRating = reviewRepo.getAverageRatingByRecruiterAndStatus(
                recruiterId, ReviewStatus.ACTIVE.name());
        stats.put("averageOverallRating", avgOverallRating != null ? avgOverallRating : 0.0);
        
        // Reviews in time periods
        LocalDateTime now = LocalDateTime.now();
        stats.put("last24Hours", reviewRepo.countByRecruiterIdAndStatusAndCreatedAtAfter(
                recruiterId, ReviewStatus.ACTIVE, now.minusDays(1)));
        stats.put("last7Days", reviewRepo.countByRecruiterIdAndStatusAndCreatedAtAfter(
                recruiterId, ReviewStatus.ACTIVE, now.minusDays(7)));
        stats.put("last30Days", reviewRepo.countByRecruiterIdAndStatusAndCreatedAtAfter(
                recruiterId, ReviewStatus.ACTIVE, now.minusDays(30)));
        
        return stats;
    }
    
    private AdminReviewResponse mapToAdminResponse(CompanyReview review) {
        String candidateName = review.getIsAnonymous() 
                ? "Anonymous" 
                : (review.getCandidate().getFullName() != null ? review.getCandidate().getFullName() : "Unknown");
        String candidateEmail = review.getIsAnonymous() 
                ? null 
                : (review.getCandidate().getAccount() != null ? review.getCandidate().getAccount().getEmail() : null);
        String companyName = review.getRecruiter().getCompanyName();
        
        return AdminReviewResponse.builder()
                .id(review.getId())
                .candidateId(review.getCandidate().getCandidateId())
                .candidateName(candidateName)
                .candidateEmail(candidateEmail)
                .recruiterId(review.getRecruiter().getId())
                .companyName(companyName)
                .jobPostingId(review.getJobPosting().getId())
                .jobTitle(review.getJobPosting().getTitle())
                .jobApplyId(review.getJobApply().getId())
                .reviewType(review.getReviewType())
                .status(review.getStatus())
                .reviewText(review.getReviewText())
                .overallRating(review.getOverallRating())
                .communicationRating(review.getCommunicationRating())
                .responsivenessRating(review.getResponsivenessRating())
                .interviewProcessRating(review.getInterviewProcessRating())
                .workCultureRating(review.getWorkCultureRating())
                .managementRating(review.getManagementRating())
                .benefitsRating(review.getBenefitsRating())
                .workLifeBalanceRating(review.getWorkLifeBalanceRating())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .isAnonymous(review.getIsAnonymous())
                .isVerified(review.getIsVerified())
                .flagCount(0)  // Don't show flag count to recruiters
                .removalReason(null)  // Don't show removal reasons to recruiters
                .sentimentScore(review.getSentimentScore())
                .build();
    }
}
