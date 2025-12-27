package com.fpt.careermate.services.job_services.service.mapper;

import com.fpt.careermate.common.constant.InterviewStatus;
import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.services.job_services.domain.InterviewSchedule;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.service.dto.response.JobApplyResponse;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface JobApplyMapper {

    // Statuses that allow recruiter contact visibility
    Set<StatusJobApply> CONTACT_VISIBLE_STATUSES = Set.of(
            StatusJobApply.APPROVED,
            StatusJobApply.ACCEPTED,
            StatusJobApply.WORKING
    );

    @Mapping(target = "jobPostingId", source = "jobPosting.id")
    @Mapping(target = "jobTitle", source = "jobPosting.title")
    @Mapping(target = "jobDescription", source = "jobPosting.description")
    @Mapping(target = "expirationDate", source = "jobPosting.expirationDate")
    @Mapping(target = "candidateId", source = "candidate.candidateId")
    @Mapping(target = "companyName", ignore = true)
    @Mapping(target = "companyEmail", ignore = true)
    @Mapping(target = "recruiterPhone", ignore = true)
    @Mapping(target = "companyAddress", ignore = true)
    @Mapping(target = "contactPerson", ignore = true)
    @Mapping(target = "hasCancelledInterview", ignore = true)
    @Mapping(target = "cancelledInterviewNotes", ignore = true)
    @Mapping(target = "cancelledInterviewDate", ignore = true)
    @Mapping(target = "totalInterviewRounds", ignore = true)
    JobApplyResponse toJobApplyResponseBasic(JobApply jobApply);

    /**
     * Maps JobApply to JobApplyResponse with conditional recruiter contact visibility.
     * Contact info (companyName, email, phone, address, contactPerson) is only included
     * when the application status is APPROVED, ACCEPTED, or WORKING.
     */
    default JobApplyResponse toJobApplyResponse(JobApply jobApply) {
        JobApplyResponse response = toJobApplyResponseBasic(jobApply);
        
        // Only populate contact info if status allows visibility
        if (jobApply != null && shouldShowRecruiterContact(jobApply.getStatus())) {
            Recruiter recruiter = jobApply.getJobPosting().getRecruiter();
            if (recruiter != null) {
                response.setCompanyName(recruiter.getCompanyName());
                response.setCompanyEmail(recruiter.getCompanyEmail());
                response.setRecruiterPhone(recruiter.getPhoneNumber());
                response.setCompanyAddress(recruiter.getCompanyAddress());
                response.setContactPerson(recruiter.getContactPerson());
            }
        }
        
        return response;
    }
    
    /**
     * Maps JobApply to JobApplyResponse with interview history information.
     * Use this method when you have the interview list available to avoid N+1 queries.
     */
    default JobApplyResponse toJobApplyResponseWithInterviewHistory(JobApply jobApply, List<InterviewSchedule> interviews) {
        JobApplyResponse response = toJobApplyResponse(jobApply);
        
        if (interviews != null && !interviews.isEmpty()) {
            response.setTotalInterviewRounds(interviews.size());
            
            // Find the most recent cancelled interview to show notes
            interviews.stream()
                    .filter(i -> i.getStatus() == InterviewStatus.CANCELLED)
                    .reduce((first, second) -> second) // get the last cancelled one
                    .ifPresent(cancelledInterview -> {
                        response.setHasCancelledInterview(true);
                        response.setCancelledInterviewNotes(cancelledInterview.getInterviewerNotes());
                        response.setCancelledInterviewDate(cancelledInterview.getScheduledDate());
                    });
            
            // Default to false if no cancelled interview found
            if (response.getHasCancelledInterview() == null) {
                response.setHasCancelledInterview(false);
            }
        } else {
            response.setHasCancelledInterview(false);
            response.setTotalInterviewRounds(0);
        }
        
        return response;
    }

    /**
     * Determines if recruiter contact information should be shown based on application status.
     * Contact is visible only when candidate has been approved or is working.
     */
    default boolean shouldShowRecruiterContact(StatusJobApply status) {
        return status != null && CONTACT_VISIBLE_STATUSES.contains(status);
    }

    default String getCandidateName(JobApply jobApply) {
        if (jobApply.getCandidate() != null) {
            return jobApply.getCandidate().getFullName();
        }
        return null;
    }
}
