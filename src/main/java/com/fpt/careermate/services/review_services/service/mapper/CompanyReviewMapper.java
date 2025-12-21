package com.fpt.careermate.services.review_services.service.mapper;

import com.fpt.careermate.services.review_services.domain.CompanyReview;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewResponse;
import com.fpt.careermate.services.review_services.service.dto.response.PublicCompanyReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for CompanyReview entity to CompanyReviewResponse DTO
 * 
 * @since 1.0
 */
@Mapper(componentModel = "spring")
public interface CompanyReviewMapper {

    /**
     * Convert CompanyReview entity to internal response DTO.
     * Includes reviewer identifiers for candidate/admin use.
     */
    @Mapping(target = "candidateId", source = "candidate.candidateId")
    @Mapping(target = "candidateName", source = "candidate.fullName")
    @Mapping(target = "recruiterId", source = "recruiter.id")
    @Mapping(target = "companyName", source = "recruiter.companyName")
    @Mapping(target = "jobApplyId", source = "jobApply.id")
    @Mapping(target = "jobPostingId", source = "jobPosting.id")
    @Mapping(target = "jobTitle", source = "jobPosting.title")
    CompanyReviewResponse toResponse(CompanyReview review);

    /**
     * Convert CompanyReview entity to public response DTO.
     * Does not expose reviewer identifiers.
     */
    @Mapping(target = "recruiterId", source = "recruiter.id")
    @Mapping(target = "companyName", source = "recruiter.companyName")
    @Mapping(target = "jobPostingId", source = "jobPosting.id")
    @Mapping(target = "jobTitle", source = "jobPosting.title")
    @Mapping(target = "candidateName", expression = "java(Boolean.TRUE.equals(review.getIsAnonymous()) ? null : review.getCandidate().getFullName())")
    PublicCompanyReviewResponse toPublicResponse(CompanyReview review);
}
