package com.fpt.careermate.services.job_services.service.mapper;

import com.fpt.careermate.services.job_services.domain.InterviewSchedule;
import com.fpt.careermate.services.job_services.service.dto.response.InterviewScheduleResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface InterviewScheduleMapper {

    @Mapping(target = "jobApplyId", source = "jobApply.id")
    // Candidate information mappings
    @Mapping(target = "candidateId", source = "jobApply.candidate.candidateId")
    @Mapping(target = "candidateName", source = "jobApply.fullName")
    @Mapping(target = "candidatePhone", source = "jobApply.phoneNumber")
    @Mapping(target = "candidateEmail", source = "jobApply.candidate.account.email")
    @Mapping(target = "candidateImage", source = "jobApply.candidate.image")
    // Job information mapping
    @Mapping(target = "jobId", source = "jobApply.jobPosting.id")
    @Mapping(target = "jobTitle", source = "jobApply.jobPosting.title")
    // Company information mappings (for candidate view)
    @Mapping(target = "companyId", source = "jobApply.jobPosting.recruiter.id")
    @Mapping(target = "companyName", source = "jobApply.jobPosting.recruiter.companyName")
    @Mapping(target = "companyLogo", source = "jobApply.jobPosting.recruiter.logoUrl")
    @Mapping(target = "companyWebsite", source = "jobApply.jobPosting.recruiter.website")
    // Calculated fields
    @Mapping(target = "expectedEndTime", expression = "java(calculateExpectedEndTime(interviewSchedule))")
    @Mapping(target = "hasInterviewTimePassed", expression = "java(hasTimePassed(interviewSchedule))")
    @Mapping(target = "isInterviewInProgress", expression = "java(isInProgress(interviewSchedule))")
    @Mapping(target = "hoursUntilInterview", expression = "java(hoursUntil(interviewSchedule))")
    InterviewScheduleResponse toResponse(InterviewSchedule interviewSchedule);

    default LocalDateTime calculateExpectedEndTime(InterviewSchedule interview) {
        if (interview.getScheduledDate() != null && interview.getDurationMinutes() != null) {
            return interview.getScheduledDate().plusMinutes(interview.getDurationMinutes());
        }
        return null;
    }

    default Boolean hasTimePassed(InterviewSchedule interview) {
        LocalDateTime endTime = calculateExpectedEndTime(interview);
        return endTime != null && LocalDateTime.now().isAfter(endTime);
    }

    default Boolean isInProgress(InterviewSchedule interview) {
        if (interview.getScheduledDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTime = calculateExpectedEndTime(interview);
            return now.isAfter(interview.getScheduledDate()) && 
                   (endTime == null || now.isBefore(endTime));
        }
        return false;
    }

    default Long hoursUntil(InterviewSchedule interview) {
        if (interview.getScheduledDate() != null && !hasTimePassed(interview)) {
            return Duration.between(LocalDateTime.now(), interview.getScheduledDate()).toHours();
        }
        return null;
    }
}

