package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.JobPosting;
import com.fpt.careermate.services.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.dto.response.JobPostingForAdminResponse;
import com.fpt.careermate.services.dto.response.JobPostingForRecruiterResponse;
import com.fpt.careermate.services.dto.response.JobPostingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobPostingMapper {
    JobPosting toJobPosting(JobPostingCreationRequest request);

    @Mapping(source = "recruiter.account.username", target = "recruiterName")
    JobPostingForAdminResponse toJobPostingForAdminResponse(JobPosting jobPosting);

    List<JobPostingResponse> toJobPostingResponseList(List<JobPosting> jobPostings);
    List<JobPostingForAdminResponse> toJobPostingForAdminResponseList(List<JobPosting> jobPostings);
    List<JobPostingForRecruiterResponse> toJobPostingForRecruiterResponseList(List<JobPosting> jobPostings);
}
