package com.fpt.careermate.services.job_services.service.mapper;

import com.fpt.careermate.services.job_services.domain.RecruiterWorkingHours;
import com.fpt.careermate.services.job_services.service.dto.response.RecruiterWorkingHoursResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecruiterWorkingHoursMapper {

    @Mapping(source = "recruiter.id", target = "recruiterId")
    @Mapping(target = "totalWorkingMinutes", expression = "java(entity.getTotalWorkingMinutes())")
    RecruiterWorkingHoursResponse toResponse(RecruiterWorkingHours entity);
}
