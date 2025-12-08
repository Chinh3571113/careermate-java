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
    @Mapping(target = "isEligibleForWorkReview", expression = "java(entity.isEligibleForWorkReview())")
    @Mapping(target = "isCurrentlyEmployed", expression = "java(entity.isCurrentlyEmployed())")
    @Mapping(target = "daysEmployed", expression = "java(entity.calculateDaysEmployed())")
    EmploymentVerificationResponse toResponse(EmploymentVerification entity);
}
