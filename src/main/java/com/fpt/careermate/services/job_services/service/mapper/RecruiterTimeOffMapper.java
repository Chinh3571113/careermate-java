package com.fpt.careermate.services.job_services.service.mapper;

import com.fpt.careermate.services.job_services.domain.RecruiterTimeOff;
import com.fpt.careermate.services.job_services.service.dto.response.RecruiterTimeOffResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecruiterTimeOffMapper {

    @Mapping(source = "recruiter.id", target = "recruiterId")
    RecruiterTimeOffResponse toResponse(RecruiterTimeOff entity);
}
