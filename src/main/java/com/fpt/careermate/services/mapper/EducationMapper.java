package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Education;
import com.fpt.careermate.services.dto.request.EducationRequest;
import com.fpt.careermate.services.dto.response.EducationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EducationMapper {
    @Mapping(target = "educationId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    Education toEntity(EducationRequest request);

    EducationResponse toResponse(Education education);

    @Mapping(target = "educationId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    void updateEntity(EducationRequest request, @MappingTarget Education education);
}
