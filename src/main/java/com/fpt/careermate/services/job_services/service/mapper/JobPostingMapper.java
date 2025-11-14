package com.fpt.careermate.services.job_services.service.mapper;

import com.fpt.careermate.services.job_services.domain.JobDescription;
import com.fpt.careermate.services.job_services.domain.JobPosting;
import com.fpt.careermate.services.job_services.service.dto.request.JobPostingCreationRequest;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForCandidateResponse;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingForRecruiterResponse;
import com.fpt.careermate.services.job_services.service.dto.response.JobPostingSkillResponse;
import com.fpt.careermate.services.job_services.service.dto.response.PageJobPostingForRecruiterResponse;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface JobPostingMapper {
    @Mapping(target = "workModel", ignore = true)
    JobPosting toJobPosting(JobPostingCreationRequest request);



    @Mapping(target = "postTime", source = "createAt")
    JobPostingForRecruiterResponse toJobPostingDetailForRecruiterResponse(JobPosting jobPosting);

    Set<JobPostingSkillResponse> toJobPostingSkillResponseSet(Set<JobDescription> jobDescriptions);

    // For candidate views
    @Mapping(target = "postTime", source = "createAt")
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "recruiterInfo", ignore = true)
    JobPostingForCandidateResponse toJobPostingForCandidateResponse(JobPosting jobPosting);

    List<JobPostingForCandidateResponse> toJobPostingForCandidateResponseList(List<JobPosting> jobPostings);

    PageJobPostingForRecruiterResponse toPageJobPostingForRecruiterResponse(Page<JobPosting> pageJobPosting);

    @Mapping(source = "id", target = "recruiterId")
    JobPostingForCandidateResponse.RecruiterCompanyInfo toRecruiterCompanyInfo(Recruiter recruiter);

}
