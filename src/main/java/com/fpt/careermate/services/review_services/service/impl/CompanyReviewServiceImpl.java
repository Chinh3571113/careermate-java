package com.fpt.careermate.services.review_services.service.impl;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.review_services.constant.ReviewStatus;
import com.fpt.careermate.services.review_services.constant.ReviewType;
import com.fpt.careermate.services.review_services.domain.CompanyReview;
import com.fpt.careermate.services.review_services.repository.CompanyReviewRepo;
import com.fpt.careermate.services.review_services.service.dto.request.CompanyReviewRequest;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewResponse;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewStatsResponse;
import com.fpt.careermate.services.review_services.service.dto.response.ReviewEligibilityResponse;
import com.fpt.careermate.services.review_services.service.mapper.CompanyReviewMapper;
import com.fpt.careermate.services.review_services.service.CompanyReviewService;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.kafka.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of CompanyReviewService
 * Handles company review submission, eligibility checking, and statistics
 * 
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyReviewServiceImpl implements CompanyReviewService {

    private final CompanyReviewRepo reviewRepo;
    private final JobApplyRepo jobApplyRepo;
    private final CompanyReviewMapper reviewMapper;
    private final NotificationProducer notificationProducer;

    @Override
    @Transactional
    public CompanyReviewResponse submitReview(CompanyReviewRequest request, Integer candidateId) {
        log.info("Candidate {} submitting review for job apply {}", candidateId, request.getJobApplyId());

        // Validate job application exists
        JobApply jobApply = jobApplyRepo.findById(request.getJobApplyId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_APPLY_NOT_FOUND));

        // Verify the job apply belongs to this candidate
        if (!candidateId.equals(jobApply.getCandidate().getCandidateId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_REVIEW);
        }

        // Check if already reviewed this job application with same review type
        if (reviewRepo.existsByJobApplyIdAndReviewType(request.getJobApplyId(), request.getReviewType())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_SUBMITTED);
        }

        // Validate review type eligibility (this should be checked by checkEligibility first)
        // TODO: Add more sophisticated eligibility validation based on job apply status

        // Build review entity
        CompanyReview review = CompanyReview.builder()
                .candidate(jobApply.getCandidate())
                .recruiter(jobApply.getJobPosting().getRecruiter())
                .jobApply(jobApply)
                .jobPosting(jobApply.getJobPosting())
                .reviewType(request.getReviewType())
                .status(ReviewStatus.ACTIVE)
                .reviewText(request.getReviewText())
                .overallRating(request.getOverallRating())
                .communicationRating(request.getCommunicationRating())
                .responsivenessRating(request.getResponsivenessRating())
                .interviewProcessRating(request.getInterviewProcessRating())
                .workCultureRating(request.getWorkCultureRating())
                .managementRating(request.getManagementRating())
                .benefitsRating(request.getBenefitsRating())
                .workLifeBalanceRating(request.getWorkLifeBalanceRating())
                .isAnonymous(request.getIsAnonymous())
                .build();

        CompanyReview saved = reviewRepo.save(review);
        log.info("Review {} submitted successfully", saved.getId());

        // TODO: Notify company of new review
        // notificationService.notifyNewCompanyReview(recruiter.getId(), saved.getId());

        return reviewMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewEligibilityResponse checkEligibility(Integer candidateId, Integer jobApplyId) {
        log.debug("Checking review eligibility for candidate {} on job apply {}", candidateId, jobApplyId);

        JobApply jobApply = jobApplyRepo.findById(jobApplyId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_APPLY_NOT_FOUND));

        // Verify ownership
        if (!candidateId.equals(jobApply.getCandidate().getCandidateId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_REVIEW);
        }

        Recruiter recruiter = jobApply.getJobPosting().getRecruiter();

        // Check which review types have already been submitted
        Map<ReviewType, Boolean> alreadyReviewed = new HashMap<>();
        for (ReviewType type : ReviewType.values()) {
            alreadyReviewed.put(type, 
                reviewRepo.existsByJobApplyIdAndReviewType(jobApplyId, type));
        }

        // TODO: Implement full eligibility logic based on CandidateQualification
        // For now, return basic eligibility
        ReviewEligibilityResponse response = ReviewEligibilityResponse.builder()
                .jobApplyId(jobApplyId)
                .candidateId(candidateId)
                .recruiterId(recruiter.getId())
                .companyName(recruiter.getCompanyName())
                .alreadyReviewed(alreadyReviewed)
                .canReview(true) // Simplified - should check job apply status
                .message("You are eligible to review this company")
                .build();

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyReviewResponse> getCompanyReviews(Integer recruiterId, ReviewType reviewType, 
                                                          int page, int size) {
        log.debug("Fetching reviews for recruiter {} with type {}", recruiterId, reviewType);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<CompanyReview> reviews;
        if (reviewType != null) {
            // Custom query needed - for now, get all and filter
            reviews = reviewRepo.findByRecruiterIdAndStatus(recruiterId, ReviewStatus.ACTIVE, pageable);
        } else {
            reviews = reviewRepo.findByRecruiterIdAndStatus(recruiterId, ReviewStatus.ACTIVE, pageable);
        }

        return reviews.map(reviewMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyReviewResponse> getCandidateReviews(Integer candidateId, int page, int size) {
        log.debug("Fetching reviews by candidate {}", candidateId);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CompanyReview> reviews = reviewRepo.findByCandidateCandidateIdAndStatus(
            candidateId, ReviewStatus.ACTIVE, pageable);

        return reviews.map(reviewMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Integer recruiterId) {
        log.debug("Calculating average rating for recruiter {}", recruiterId);

        Double avgRating = reviewRepo.getAverageRatingByRecruiterId(recruiterId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyReviewStatsResponse getCompanyStatistics(Integer recruiterId) {
        log.debug("Calculating statistics for recruiter {}", recruiterId);

        Long totalReviews = reviewRepo.countByRecruiterIdAndStatus(recruiterId, ReviewStatus.ACTIVE);
        Double avgRating = reviewRepo.getAverageRatingByRecruiterId(recruiterId);

        // Count by type
        Long appReviews = reviewRepo.countByRecruiterIdAndReviewTypeAndStatus(
            recruiterId, ReviewType.APPLICATION_EXPERIENCE, ReviewStatus.ACTIVE);
        Long interviewReviews = reviewRepo.countByRecruiterIdAndReviewTypeAndStatus(
            recruiterId, ReviewType.INTERVIEW_EXPERIENCE, ReviewStatus.ACTIVE);
        Long workReviews = reviewRepo.countByRecruiterIdAndReviewTypeAndStatus(
            recruiterId, ReviewType.WORK_EXPERIENCE, ReviewStatus.ACTIVE);

        // Calculate rating distribution
        List<CompanyReview> allReviews = reviewRepo.findByRecruiterIdAndStatus(
            recruiterId, ReviewStatus.ACTIVE, Pageable.unpaged()).getContent();
        
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            long count = allReviews.stream()
                .filter(r -> r.getOverallRating().equals(rating))
                .count();
            distribution.put(rating, count);
        }

        // Calculate average aspect ratings
        Double avgComm = calculateAverageAspect(allReviews, CompanyReview::getCommunicationRating);
        Double avgResp = calculateAverageAspect(allReviews, CompanyReview::getResponsivenessRating);
        Double avgInterview = calculateAverageAspect(allReviews, CompanyReview::getInterviewProcessRating);
        Double avgCulture = calculateAverageAspect(allReviews, CompanyReview::getWorkCultureRating);
        Double avgMgmt = calculateAverageAspect(allReviews, CompanyReview::getManagementRating);
        Double avgBenefits = calculateAverageAspect(allReviews, CompanyReview::getBenefitsRating);
        Double avgWLB = calculateAverageAspect(allReviews, CompanyReview::getWorkLifeBalanceRating);

        long verified = allReviews.stream().filter(CompanyReview::getIsVerified).count();
        long anonymous = allReviews.stream().filter(CompanyReview::getIsAnonymous).count();

        Double avgSentiment = allReviews.stream()
            .map(CompanyReview::getSentimentScore)
            .filter(s -> s != null)
            .collect(Collectors.averagingDouble(Double::doubleValue));

        return CompanyReviewStatsResponse.builder()
                .recruiterId(recruiterId)
                .totalReviews(totalReviews)
                .averageOverallRating(avgRating)
                .applicationReviews(appReviews)
                .interviewReviews(interviewReviews)
                .workExperienceReviews(workReviews)
                .avgCommunication(avgComm)
                .avgResponsiveness(avgResp)
                .avgInterviewProcess(avgInterview)
                .avgWorkCulture(avgCulture)
                .avgManagement(avgMgmt)
                .avgBenefits(avgBenefits)
                .avgWorkLifeBalance(avgWLB)
                .ratingDistribution(distribution)
                .avgSentimentScore(avgSentiment)
                .verifiedReviews(verified)
                .anonymousReviews(anonymous)
                .build();
    }

    @Override
    @Transactional
    public void flagReview(Integer reviewId, Integer reporterId, String reason) {
        log.info("Flagging review {} by user {} for reason: {}", reviewId, reporterId, reason);

        CompanyReview review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        review.setFlagCount(review.getFlagCount() + 1);
        
        // Auto-remove if flagged too many times
        if (review.getFlagCount() >= 5) {
            review.setStatus(ReviewStatus.FLAGGED);
            log.warn("Review {} has been flagged {} times and marked for moderation", 
                reviewId, review.getFlagCount());
        }

        reviewRepo.save(review);

        // TODO: Notify moderators
        // notificationService.notifyModeratorReviewFlagged(reviewId, review.getFlagCount());
    }

    @Override
    @Transactional
    public void removeReview(Integer reviewId, String reason) {
        log.info("Removing review {} for reason: {}", reviewId, reason);

        CompanyReview review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        review.setStatus(ReviewStatus.REMOVED);
        review.setRemovalReason(reason);
        reviewRepo.save(review);

        log.info("Review {} removed successfully", reviewId);

        // TODO: Notify review author
        // notificationService.notifyReviewRemoved(review.getCandidate().getCandidateId(), reviewId, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyReviewResponse getReviewById(Integer reviewId) {
        log.debug("Fetching review {}", reviewId);

        CompanyReview review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        return reviewMapper.toResponse(review);
    }

    /**
     * Helper method to calculate average of aspect ratings
     */
    private Double calculateAverageAspect(List<CompanyReview> reviews, 
                                         java.util.function.Function<CompanyReview, Integer> ratingGetter) {
        return reviews.stream()
                .map(ratingGetter)
                .filter(rating -> rating != null)
                .collect(Collectors.averagingDouble(Integer::doubleValue));
    }
}
