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
import com.fpt.careermate.services.review_services.service.dto.request.AdminBulkReviewStatusRequest;
import com.fpt.careermate.services.review_services.service.dto.request.CompanyReviewRequest;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewResponse;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewStatsResponse;
import com.fpt.careermate.services.review_services.service.dto.response.PublicCompanyReviewResponse;
import com.fpt.careermate.services.review_services.service.dto.response.ReviewEligibilityResponse;
import com.fpt.careermate.services.review_services.service.dto.response.JobApplicationReviewStatusResponse;
import com.fpt.careermate.services.review_services.service.mapper.CompanyReviewMapper;
import com.fpt.careermate.services.review_services.service.CompanyReviewService;
import com.fpt.careermate.services.review_services.service.ReviewEligibilityService;
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
import java.time.LocalDateTime;

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
    private final ReviewEligibilityService reviewEligibilityService;

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

        // Validate that the candidate is eligible to submit this review type
        if (!reviewEligibilityService.canSubmitReviewType(jobApply, request.getReviewType())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_REVIEW);
        }

        // Check if already submitted this review type for THIS specific job application
        // Each job application can have up to 3 reviews: APPLICATION, INTERVIEW, WORK
        if (reviewRepo.existsByJobApplyIdAndReviewType(request.getJobApplyId(), request.getReviewType())) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_SUBMITTED);
        }

        // Validate review type eligibility (this should be checked by checkEligibility first)
        // Eligibility enforcement is handled above via ReviewEligibilityService

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
    @Transactional
    public CompanyReviewResponse updateReview(Integer reviewId, CompanyReviewRequest request, Integer candidateId) {
        log.info("Updating review {} for candidate {}", reviewId, candidateId);

        // Find the review
        CompanyReview existingReview = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        // Verify ownership - only the candidate who wrote the review can update it
        if (!candidateId.equals(existingReview.getCandidate().getCandidateId())) {
            log.warn("Candidate {} attempted to update review {} owned by candidate {}", 
                candidateId, reviewId, existingReview.getCandidate().getCandidateId());
            throw new AppException(ErrorCode.UNAUTHORIZED_REVIEW);
        }

        // Update fields
        existingReview.setOverallRating(request.getOverallRating());
        existingReview.setReviewText(request.getReviewText());
        
        // Update category ratings
        existingReview.setCommunicationRating(request.getCommunicationRating());
        existingReview.setResponsivenessRating(request.getResponsivenessRating());
        existingReview.setInterviewProcessRating(request.getInterviewProcessRating());
        existingReview.setWorkCultureRating(request.getWorkCultureRating());
        existingReview.setManagementRating(request.getManagementRating());
        existingReview.setBenefitsRating(request.getBenefitsRating());
        existingReview.setWorkLifeBalanceRating(request.getWorkLifeBalanceRating());
        
        existingReview.setIsAnonymous(request.getIsAnonymous());
        existingReview.setUpdatedAt(LocalDateTime.now());

        CompanyReview updated = reviewRepo.save(existingReview);
        log.info("Review {} updated successfully", reviewId);

        return reviewMapper.toResponse(updated);
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
                reviewRepo.existsByCandidateCandidateIdAndRecruiterIdAndReviewType(candidateId, recruiter.getId(), type));
        }

        var qualification = reviewEligibilityService.determineQualification(jobApply);
        var allowedTypes = reviewEligibilityService.getAllowedReviewTypes(jobApply);

        // Only return types the candidate can still submit (exclude already reviewed)
        var remainingTypes = allowedTypes.stream()
                .filter(type -> !alreadyReviewed.getOrDefault(type, false))
                .collect(Collectors.toSet());

        boolean canReview = !remainingTypes.isEmpty();
        String message;
        if (!canReview) {
            if (allowedTypes.isEmpty()) {
                message = reviewEligibilityService.getEligibilityMessage(jobApply);
            } else {
                message = "You have already submitted all eligible reviews for this application.";
            }
        } else {
            message = reviewEligibilityService.getEligibilityMessage(jobApply);
        }

        return ReviewEligibilityResponse.builder()
                .jobApplyId(jobApplyId)
                .candidateId(candidateId)
                .recruiterId(recruiter.getId())
                .companyName(recruiter.getCompanyName())
                .qualification(qualification)
                .allowedReviewTypes(remainingTypes)
                .alreadyReviewed(alreadyReviewed)
                .daysSinceApplication(jobApply.getDaysSinceApplication())
                .daysEmployed(jobApply.getDaysEmployed())
                .canReview(canReview)
                .message(message)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PublicCompanyReviewResponse> getCompanyReviews(Integer recruiterId, ReviewType reviewType,
                                                               int page, int size) {
        log.debug("Fetching reviews for recruiter {} with type {}", recruiterId, reviewType);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<CompanyReview> reviews;
        if (reviewType != null) {
            reviews = reviewRepo.findByRecruiterIdAndReviewTypeAndStatus(recruiterId, reviewType, ReviewStatus.ACTIVE, pageable);
        } else {
            reviews = reviewRepo.findByRecruiterIdAndStatus(recruiterId, ReviewStatus.ACTIVE, pageable);
        }

        return reviews.map(reviewMapper::toPublicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CompanyReviewResponse> adminGetReviews(Integer recruiterId,
                                                      ReviewStatus status,
                                                      ReviewType reviewType,
                                                      LocalDateTime from,
                                                      LocalDateTime to,
                                                      int page,
                                                      int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return reviewRepo.adminFindByFilters(recruiterId, status, reviewType, from, to, pageable)
                .map(reviewMapper::toResponse);
    }

    @Override
    @Transactional
    public void adminSetReviewStatus(Integer reviewId, ReviewStatus status, String reason) {
        CompanyReview review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        review.setStatus(status);
        if (status == ReviewStatus.REMOVED) {
            review.setRemovalReason(reason);
        }
        reviewRepo.save(review);
    }

    @Override
    @Transactional
    public void adminBulkSetReviewStatus(AdminBulkReviewStatusRequest request) {
        List<CompanyReview> reviews = reviewRepo.findAllById(request.getReviewIds());
        if (reviews.size() != request.getReviewIds().size()) {
            throw new AppException(ErrorCode.REVIEW_NOT_FOUND);
        }

        for (CompanyReview review : reviews) {
            review.setStatus(request.getStatus());
            if (request.getStatus() == ReviewStatus.REMOVED) {
                review.setRemovalReason(request.getReason());
            }
        }

        reviewRepo.saveAll(reviews);
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
    public PublicCompanyReviewResponse getReviewById(Integer reviewId) {
        log.debug("Fetching review {}", reviewId);

        CompanyReview review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        return reviewMapper.toPublicResponse(review);
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
    
    /**
     * Get all job applications that are eligible for review but haven't been reviewed yet
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReviewEligibilityResponse> getPendingReviews(Integer candidateId) {
        log.info("Getting pending reviews for candidate: {}", candidateId);
        
        // Get all job applications for this candidate
        List<JobApply> applications = jobApplyRepo.findByCandidateId(candidateId);
        
        return applications.stream()
            .map(jobApply -> {
                try {
                    // Get eligibility for this application
                    var allowedTypes = reviewEligibilityService.getAllowedReviewTypes(jobApply);
                    
                    if (allowedTypes.isEmpty()) {
                        return null; // Skip - no review types available
                    }
                    
                    // Check which types have already been reviewed
                    Map<ReviewType, Boolean> alreadyReviewed = new HashMap<>();
                    List<CompanyReview> existingReviews = reviewRepo.findByCandidateCandidateIdAndJobApplyId(
                        candidateId, jobApply.getId());
                    
                    for (ReviewType type : ReviewType.values()) {
                        boolean reviewed = existingReviews.stream()
                            .anyMatch(r -> r.getReviewType() == type);
                        alreadyReviewed.put(type, reviewed);
                    }
                    
                    // Filter out already reviewed types
                    var availableTypes = allowedTypes.stream()
                        .filter(type -> !alreadyReviewed.getOrDefault(type, false))
                        .collect(java.util.stream.Collectors.toSet());
                    
                    if (availableTypes.isEmpty()) {
                        return null; // Skip - all types already reviewed
                    }
                    
                    Recruiter recruiter = jobApply.getJobPosting().getRecruiter();
                    
                    return ReviewEligibilityResponse.builder()
                        .jobApplyId(jobApply.getId())
                        .candidateId(candidateId)
                        .recruiterId(recruiter.getId())
                        .companyName(recruiter.getCompanyName())
                        .qualification(reviewEligibilityService.determineQualification(jobApply))
                        .allowedReviewTypes(availableTypes)
                        .alreadyReviewed(alreadyReviewed)
                        .daysSinceApplication(jobApply.getDaysSinceApplication())
                        .daysEmployed(jobApply.getDaysEmployed())
                        .canReview(true)
                        .message(reviewEligibilityService.getEligibilityMessage(jobApply))
                        .build();
                } catch (Exception e) {
                    log.debug("Skip job apply {} - error checking eligibility: {}", 
                        jobApply.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(r -> r != null)
            .collect(Collectors.toList());
    }
    
    /**
     * Get all job applications with review status for each review type
     * Shows which reviews are submitted, available, or not yet eligible
     */
    @Override
    @Transactional(readOnly = true)
    public List<JobApplicationReviewStatusResponse> getApplicationsWithReviewStatus(Integer candidateId) {
        log.info("Getting applications with review status for candidate: {}", candidateId);
        
        // Get all job applications for this candidate
        List<JobApply> applications = jobApplyRepo.findByCandidateId(candidateId);
        
        return applications.stream()
            .<JobApplicationReviewStatusResponse>map(jobApply -> {
                try {
                    // Get all existing reviews for this application
                    List<CompanyReview> existingReviews = reviewRepo.findByCandidateCandidateIdAndJobApplyId(
                        candidateId, jobApply.getId());
                    
                    Map<ReviewType, CompanyReview> reviewMap = existingReviews.stream()
                        .collect(Collectors.toMap(CompanyReview::getReviewType, r -> r, (a, b) -> a));
                    
                    // Get allowed types based on eligibility
                    var allowedTypes = reviewEligibilityService.getAllowedReviewTypes(jobApply);
                    
                    Recruiter recruiter = jobApply.getJobPosting().getRecruiter();
                    
                    return JobApplicationReviewStatusResponse.builder()
                        .jobApplyId(jobApply.getId())
                        .jobTitle(jobApply.getJobPosting().getTitle())
                        .companyName(recruiter.getCompanyName())
                        .companyLogo(recruiter.getLogoUrl())
                        .appliedAt(jobApply.getCreateAt())
                        .interviewedAt(jobApply.getInterviewedAt())
                        .hiredAt(jobApply.getHiredAt())
                        .daysSinceApplication(jobApply.getDaysSinceApplication())
                        .daysEmployed(jobApply.getDaysEmployed())
                        .applicationReview(buildReviewTypeStatus(
                            ReviewType.APPLICATION_EXPERIENCE, 
                            reviewMap, 
                            allowedTypes,
                            jobApply
                        ))
                        .interviewReview(buildReviewTypeStatus(
                            ReviewType.INTERVIEW_EXPERIENCE, 
                            reviewMap, 
                            allowedTypes,
                            jobApply
                        ))
                        .workReview(buildReviewTypeStatus(
                            ReviewType.WORK_EXPERIENCE, 
                            reviewMap, 
                            allowedTypes,
                            jobApply
                        ))
                        .build();
                } catch (Exception e) {
                    log.debug("Skip job apply {} - error building status: {}", 
                        jobApply.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(r -> r != null)
            .collect(Collectors.toList());
    }
    
    /**
     * Build the status for a specific review type
     */
    private JobApplicationReviewStatusResponse.ReviewTypeStatus buildReviewTypeStatus(
            ReviewType type,
            Map<ReviewType, CompanyReview> reviewMap,
            java.util.Set<ReviewType> allowedTypes,
            JobApply jobApply) {
        
        // Check if already submitted
        CompanyReview existingReview = reviewMap.get(type);
        if (existingReview != null) {
            return JobApplicationReviewStatusResponse.ReviewTypeStatus.builder()
                .status("submitted")
                .reviewId(existingReview.getId())
                .rating(existingReview.getOverallRating())
                .build();
        }
        
        // Check if eligible
        if (allowedTypes.contains(type)) {
            return JobApplicationReviewStatusResponse.ReviewTypeStatus.builder()
                .status("available")
                .build();
        }
        
        // Not eligible - determine reason
        String reason = getNotEligibleReason(type, jobApply);
        return JobApplicationReviewStatusResponse.ReviewTypeStatus.builder()
            .status("not_eligible")
            .reason(reason)
            .build();
    }
    
    /**
     * Get human-readable reason why review type is not eligible
     */
    private String getNotEligibleReason(ReviewType type, JobApply jobApply) {
        switch (type) {
            case APPLICATION_EXPERIENCE:
                int daysSince = jobApply.getDaysSinceApplication();
                if (daysSince < 7) {
                    return String.format("Need %d more days (applied %d days ago)", 7 - daysSince, daysSince);
                }
                return "Not eligible";
                
            case INTERVIEW_EXPERIENCE:
                if (jobApply.getInterviewedAt() == null) {
                    return "No interview completed yet";
                }
                return "Not eligible";
                
            case WORK_EXPERIENCE:
                Integer daysEmployed = jobApply.getDaysEmployed();
                if (daysEmployed == null || daysEmployed < 30) {
                    int remaining = 30 - (daysEmployed != null ? daysEmployed : 0);
                    return String.format("Need %d more days employed", remaining);
                }
                return "Not eligible";
                
            default:
                return "Not eligible";
        }
    }
}
