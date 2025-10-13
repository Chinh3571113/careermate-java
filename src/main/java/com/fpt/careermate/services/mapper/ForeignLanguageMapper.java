package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.ForeignLanguage;
import com.fpt.careermate.services.dto.request.ForeignLanguageRequest;
import com.fpt.careermate.services.dto.response.ForeignLanguageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ForeignLanguageMapper {
    @Mapping(target = "foreignLanguageId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    ForeignLanguage toEntity(ForeignLanguageRequest request);

    ForeignLanguageResponse toResponse(ForeignLanguage foreignLanguage);

    @Mapping(target = "foreignLanguageId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    void updateEntity(ForeignLanguageRequest request, @MappingTarget ForeignLanguage foreignLanguage);
}

