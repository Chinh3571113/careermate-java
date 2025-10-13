package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.WorkExperience;
import com.fpt.careermate.services.dto.request.WorkExperienceRequest;
import com.fpt.careermate.services.dto.response.WorkExperienceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WorkExperienceMapper {
    @Mapping(target = "workExperienceId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    WorkExperience toEntity(WorkExperienceRequest request);

    WorkExperienceResponse toResponse(WorkExperience workExperience);

    @Mapping(target = "workExperienceId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    void updateEntity(WorkExperienceRequest request, @MappingTarget WorkExperience workExperience);
}

