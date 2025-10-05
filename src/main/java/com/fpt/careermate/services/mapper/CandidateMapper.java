package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Candidate;
import com.fpt.careermate.domain.IndustryExperiences;
import com.fpt.careermate.domain.WorkModel;
import com.fpt.careermate.services.dto.request.CandidateProfileRequest;
import com.fpt.careermate.services.dto.request.GeneralInfoRequest;
import com.fpt.careermate.services.dto.response.CandidateProfileResponse;
import com.fpt.careermate.services.dto.response.GeneralInfoResponse;
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
