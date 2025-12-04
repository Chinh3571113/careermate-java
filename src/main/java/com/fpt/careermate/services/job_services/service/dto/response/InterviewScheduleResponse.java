package com.fpt.careermate.services.job_services.service.dto.response;

import com.fpt.careermate.common.constant.InterviewOutcome;
import com.fpt.careermate.common.constant.InterviewStatus;
import com.fpt.careermate.common.constant.InterviewType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * DTO for interview schedule response
 * 
 * @since 1.0
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InterviewScheduleResponse {

    Integer id;
    
    Integer jobApplyId;
    
    // Candidate information for recruiter view
    Integer candidateId;
    
    String candidateName;
    
    String candidatePhone;
    
    String candidateEmail;
    
    String candidateImage;
    
    // Job information
    Integer jobId;
    
    String jobTitle;
    
    // Company information for candidate view
    Integer companyId;
    
    String companyName;
    
    String companyLogo;
    
    String companyWebsite;
    
    Integer interviewRound;
    
    LocalDateTime scheduledDate;
    
    Integer durationMinutes;
    
    InterviewType interviewType;
    
    String location;
    
    String interviewerName;
    
    String interviewerEmail;
    
    String interviewerPhone;
    
    String preparationNotes;
    
    String meetingLink;
    
    InterviewStatus status;
    
    Boolean candidateConfirmed;
    
    LocalDateTime candidateConfirmedAt;
    
    Boolean reminderSent24h;
    
    Boolean reminderSent2h;
    
    LocalDateTime interviewCompletedAt;
    
    String interviewerNotes;
    
    InterviewOutcome outcome;
    
    Integer createdByRecruiterId;
    
    LocalDateTime createdAt;
    
    LocalDateTime updatedAt;
    
    /**
     * Calculated expected end time
     */
    LocalDateTime expectedEndTime;
    
    /**
     * Whether interview time has passed
     */
    Boolean hasInterviewTimePassed;
    
    /**
     * Whether interview is currently in progress
     */
    Boolean isInterviewInProgress;
    
    /**
     * Hours until interview
     */
    Long hoursUntilInterview;
}
