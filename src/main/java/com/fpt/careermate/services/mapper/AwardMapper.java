package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Award;
import com.fpt.careermate.services.dto.request.AwardRequest;
import com.fpt.careermate.services.dto.response.AwardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AwardMapper {
    @Mapping(target = "awardId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    Award toEntity(AwardRequest request);

    AwardResponse toResponse(Award award);

    @Mapping(target = "awardId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    void updateEntity(AwardRequest request, @MappingTarget Award award);
}

