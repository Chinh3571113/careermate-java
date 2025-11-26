package com.fpt.careermate.services.review_services.service.mapper;

import com.fpt.careermate.services.review_services.domain.CompanyReview;
import com.fpt.careermate.services.review_services.service.dto.response.CompanyReviewResponse;
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
     * Convert CompanyReview entity to response DTO
     */
    @Mapping(target = "candidateId", source = "candidate.candidateId")
    @Mapping(target = "candidateName", expression = "java(review.getIsAnonymous() ? null : \"Anonymous\")")
    @Mapping(target = "recruiterId", source = "recruiter.id")
    @Mapping(target = "companyName", source = "recruiter.companyName")
    @Mapping(target = "jobApplyId", source = "jobApply.id")
    @Mapping(target = "jobPostingId", source = "jobPosting.id")
    @Mapping(target = "jobTitle", source = "jobPosting.title")
    CompanyReviewResponse toResponse(CompanyReview review);
}
