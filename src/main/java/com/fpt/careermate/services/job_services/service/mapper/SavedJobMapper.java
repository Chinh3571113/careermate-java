package com.fpt.careermate.services.job_services.service.mapper;

import com.fpt.careermate.services.job_services.domain.SavedJob;
import com.fpt.careermate.services.job_services.service.dto.response.PageSavedJobPostingResponse;
import com.fpt.careermate.services.job_services.service.dto.response.SavedJobPostingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SavedJobMapper {
    PageSavedJobPostingResponse toPageSavedJobPostingResponse(Page<SavedJob> savedJobs);

    @Mapping(source = "jobPosting.title", target = "title")
    @Mapping(source = "jobPosting.recruiter.companyName", target = "companyName")
    @Mapping(source = "jobPosting.recruiter.companyAddress", target = "companyAddress")
    @Mapping(source = "jobPosting.salaryRange", target = "salaryRange")
    @Mapping(source = "jobPosting.yearsOfExperience", target = "yearOfExperience")
    @Mapping(source = "jobPosting.workModel", target = "workModel")
    @Mapping(source = "jobPosting.expirationDate", target = "expirationDate")
    @Mapping(source = "id", target = "savedJobId")
    @Mapping(source = "jobPosting.id", target = "jobId")
    SavedJobPostingResponse toSavedJobPostingResponse(SavedJob savedJob);
}
