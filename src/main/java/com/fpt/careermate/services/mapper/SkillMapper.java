package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Skill;
import com.fpt.careermate.services.dto.request.SkillRequest;
import com.fpt.careermate.services.dto.response.SkillResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    @Mapping(target = "skillId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    Skill toEntity(SkillRequest request);

    SkillResponse toResponse(Skill skill);

    @Mapping(target = "skillId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    void updateEntity(SkillRequest request, @MappingTarget Skill skill);
}

