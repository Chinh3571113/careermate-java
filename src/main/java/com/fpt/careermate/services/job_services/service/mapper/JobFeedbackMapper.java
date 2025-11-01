package com.fpt.careermate.services.job_services.service.mapper;

import com.fpt.careermate.services.job_services.domain.JobFeedback;
import com.fpt.careermate.services.job_services.service.dto.response.JobFeedbackResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobFeedbackMapper {

    @Mapping(target = "candidateId", source = "candidate.candidateId")
    @Mapping(target = "candidateName", source = "candidate.fullName")
    @Mapping(target = "jobId", source = "jobPosting.id")
    @Mapping(target = "jobTitle", source = "jobPosting.title")
    @Mapping(target = "createdAt", source = "createAt")
    JobFeedbackResponse toJobFeedbackResponse(JobFeedback jobFeedback);
}
