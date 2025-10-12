package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.JobDescription;
import com.fpt.careermate.domain.JobPosting;
import com.fpt.careermate.services.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.dto.response.JobPostingForRecruiterResponse;
import com.fpt.careermate.services.dto.response.JobPostingSkillResponse;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface JobPostingMapper {
    JobPosting toJobPosting(JobPostingCreationRequest request);

    List<JobPostingForRecruiterResponse> toJobPostingForRecruiterResponseList(List<JobPosting> jobPostings);
    JobPostingForRecruiterResponse toJobPostingDetailForRecruiterResponse(JobPosting jobPosting);

    Set<JobPostingSkillResponse> toJobPostingSkillResponseSet(Set<JobDescription> jobDescriptions);
}
