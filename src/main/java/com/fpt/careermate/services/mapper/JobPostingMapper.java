package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.JobPosting;
import com.fpt.careermate.services.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.dto.response.JobPostingForRecruiterResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobPostingMapper {
    JobPosting toJobPosting(JobPostingCreationRequest request);

    List<JobPostingForRecruiterResponse> toJobPostingForRecruiterResponseList(List<JobPosting> jobPostings);
}
