package com.fpt.careermate.services.profile_services.service.mapper;

import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.domain.IndustryExperiences;
import com.fpt.careermate.services.profile_services.domain.WorkModel;
import com.fpt.careermate.services.profile_services.service.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.profile_services.service.dto.response.CandidateProfileResponse;
import com.fpt.careermate.services.profile_services.service.dto.response.GeneralInfoResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CandidateMapper {

    @Mapping(target = "account", ignore = true)
    Candidate toCandidate(CandidateProfileRequest candidate);
    CandidateProfileResponse toCandidateProfileResponse(Candidate candidate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCandidateFromRequest(CandidateProfileRequest request, @MappingTarget Candidate candidate);

    // GeneralInfo mappings
    GeneralInfoResponse toGeneralInfoResponse(Candidate candidate);

    // Nested object mappings for responses only
    GeneralInfoResponse.IndustryExperienceResponse toIndustryExperienceResponse(IndustryExperiences industryExperiences);

    GeneralInfoResponse.WorkModelResponse toWorkModelResponse(WorkModel workModel);
}
