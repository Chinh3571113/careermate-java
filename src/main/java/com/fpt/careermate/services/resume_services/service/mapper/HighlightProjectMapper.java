package com.fpt.careermate.services.resume_services.service.mapper;

import com.fpt.careermate.services.resume_services.domain.HighlightProject;
import com.fpt.careermate.services.resume_services.service.dto.request.HighlightProjectRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.HighlightProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface HighlightProjectMapper {
    @Mapping(target = "highlightProjectId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    HighlightProject toEntity(HighlightProjectRequest request);

    HighlightProjectResponse toResponse(HighlightProject highlightProject);

    @Mapping(target = "highlightProjectId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    void updateEntity(HighlightProjectRequest request, @MappingTarget HighlightProject highlightProject);
}

