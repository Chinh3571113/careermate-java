package com.fpt.careermate.services.job_services.service.mapper;

import com.fpt.careermate.services.job_services.domain.EmploymentVerification;
import com.fpt.careermate.services.job_services.service.dto.response.EmploymentVerificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Simplified mapper for employment verification entity and DTOs.
 */
@Mapper(componentModel = "spring")
public interface EmploymentVerificationMapper {
    
    @Mapping(target = "jobApplyId", source = "jobApply.id")
    @Mapping(target = "companyName", source = "jobApply.jobPosting.recruiter.companyName")
    @Mapping(target = "candidateName", source = "jobApply.fullName")
    @Mapping(target = "candidateEmail", source = "jobApply.candidate.account.email")
    @Mapping(target = "candidatePhone", source = "jobApply.phoneNumber")
    @Mapping(target = "candidateImage", source = "jobApply.candidate.image")
    @Mapping(target = "jobTitle", source = "jobApply.jobPosting.title")
    @Mapping(target = "employmentStatus", expression = "java(Boolean.TRUE.equals(entity.getIsActive()) ? \"ACTIVE\" : \"TERMINATED\")")
    @Mapping(target = "isEligibleForWorkReview", expression = "java(entity.isEligibleForWorkReview())")
    @Mapping(target = "isCurrentlyEmployed", expression = "java(entity.isCurrentlyEmployed())")
    @Mapping(target = "daysEmployed", expression = "java(entity.calculateDaysEmployed())")
    EmploymentVerificationResponse toResponse(EmploymentVerification entity);
}
