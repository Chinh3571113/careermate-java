package com.fpt.careermate.services.review_services.service.impl;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import com.fpt.careermate.services.review_services.domain.CompanyReview;
import com.fpt.careermate.services.review_services.repository.CompanyReviewRepo;
import com.fpt.careermate.services.review_services.service.AdminReviewService;
import com.fpt.careermate.services.review_services.service.dto.request.AdminBulkReviewActionRequest;
import com.fpt.careermate.services.review_services.service.dto.request.AdminReviewFilterRequest;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {

    private final CompanyReviewRepo reviewRepo;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminReviewResponse> searchReviews(AdminReviewFilterRequest request) {
        Specification<CompanyReview> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Combined text search - searches across multiple fields
            if (request.getSearchText() != null && !request.getSearchText().isEmpty()) {
                String searchLower = "%" + request.getSearchText().toLowerCase() + "%";
                Predicate companyMatch = cb.like(cb.lower(root.get("recruiter").get("companyName")), searchLower);
                Predicate candidateNameMatch = cb.like(cb.lower(root.get("candidate").get("fullName")), searchLower);
                Predicate reviewTextMatch = cb.like(cb.lower(root.get("reviewText")), searchLower);
                Predicate jobTitleMatch = cb.like(cb.lower(root.get("jobPosting").get("title")), searchLower);

                predicates.add(cb.or(
                        companyMatch,
                        candidateNameMatch,
                        reviewTextMatch,
                        jobTitleMatch));
            }

            // Legacy: Company name filter (still works if searchText is not used)
            if (request.getSearchText() == null && request.getCompanyName() != null
                    && !request.getCompanyName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("recruiter").get("companyName")),
                        "%" + request.getCompanyName().toLowerCase() + "%"));
            }

            // Legacy: Candidate name filter
            if (request.getSearchText() == null && request.getCandidateName() != null
                    && !request.getCandidateName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("candidate").get("fullName")),
                        "%" + request.getCandidateName().toLowerCase() + "%"));
            }

            // Review type filter
            if (request.getReviewType() != null) {
                predicates.add(cb.equal(root.get("reviewType"), request.getReviewType()));
            }

            // Status filter
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            // Date range filter
            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getStartDate()));
            }
            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getEndDate()));
            }

            // Rating filters
            if (request.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("overallRating"), request.getMinRating()));
            }
            if (request.getMaxRating() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("overallRating"), request.getMaxRating()));
            }

            // Flagged reviews filter
            if (request.getFlaggedOnly() != null && request.getFlaggedOnly()) {
                predicates.add(cb.or(
                        cb.equal(root.get("status"), ReviewStatus.FLAGGED),
                        cb.greaterThan(root.get("flagCount"), 0)));
            }

            // Minimum flag count
            if (request.getMinFlagCount() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("flagCount"), request.getMinFlagCount()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = request.getSortDirection().equalsIgnoreCase("ASC")
                ? Sort.by(request.getSortBy()).ascending()
                : Sort.by(request.getSortBy()).descending();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<CompanyReview> reviews = reviewRepo.findAll(spec, pageable);

        return reviews.map(this::mapToAdminResponse);
    }

    @Override
    @Transactional
    public AdminReviewResponse updateReviewStatus(Integer reviewId, ReviewStatus newStatus, String reason) {
        CompanyReview review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        review.setStatus(newStatus);
        review.setUpdatedAt(LocalDateTime.now());

        if (reason != null && !reason.isEmpty()) {
            review.setRemovalReason(reason);
        }

        reviewRepo.save(review);

        log.info("Admin updated review {} to status {}", reviewId, newStatus);

        return mapToAdminResponse(review);
    }

    @Override
    @Transactional
    public int bulkUpdateReviewStatus(AdminBulkReviewActionRequest request) {
        List<CompanyReview> reviews = reviewRepo.findAllById(request.getReviewIds());

        if (reviews.isEmpty()) {
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }

        LocalDateTime now = LocalDateTime.now();

        reviews.forEach(review -> {
            review.setStatus(request.getNewStatus());
            review.setUpdatedAt(now);
            if (request.getReason() != null && !request.getReason().isEmpty()) {
                review.setRemovalReason(request.getReason());
            }
        });

        reviewRepo.saveAll(reviews);

        log.info("Admin bulk updated {} reviews to status {}", reviews.size(), request.getNewStatus());

        return reviews.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getReviewStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalReviews = reviewRepo.count();
        long activeReviews = reviewRepo.countByStatus(ReviewStatus.ACTIVE);
        long hiddenReviews = reviewRepo.countByStatus(ReviewStatus.HIDDEN);
        long flaggedReviews = reviewRepo.countByStatus(ReviewStatus.FLAGGED);
        long removedReviews = reviewRepo.countByStatus(ReviewStatus.REMOVED);

        stats.put("totalReviews", totalReviews);
        stats.put("activeReviews", activeReviews);
        stats.put("hiddenReviews", hiddenReviews);
        stats.put("flaggedReviews", flaggedReviews);
        stats.put("removedReviews", removedReviews);

        // Get review count by type
        Arrays.stream(ReviewType.values()).forEach(type -> {
            long count = reviewRepo.countByReviewType(type);
            stats.put(type.name().toLowerCase() + "Count", count);
        });

        // Get reviews created in last 24 hours, 7 days, 30 days
        LocalDateTime now = LocalDateTime.now();
        stats.put("last24Hours", reviewRepo.countByCreatedAtAfter(now.minusDays(1)));
        stats.put("last7Days", reviewRepo.countByCreatedAtAfter(now.minusDays(7)));
        stats.put("last30Days", reviewRepo.countByCreatedAtAfter(now.minusDays(30)));

        return stats;
    }

    private AdminReviewResponse mapToAdminResponse(CompanyReview review) {
        String candidateName = review.getCandidate().getFullName() != null ? review.getCandidate().getFullName() : "";
        String candidateEmail = review.getCandidate().getAccount() != null
                ? review.getCandidate().getAccount().getEmail()
                : "";
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
                .flagCount(review.getFlagCount())
                .removalReason(review.getRemovalReason())
                .sentimentScore(review.getSentimentScore())
                .build();
    }
}
