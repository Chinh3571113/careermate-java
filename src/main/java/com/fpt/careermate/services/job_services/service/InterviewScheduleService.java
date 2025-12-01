package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.services.job_services.service.dto.request.InterviewScheduleRequest;
import com.fpt.careermate.services.job_services.service.dto.request.CompleteInterviewRequest;
import com.fpt.careermate.services.job_services.service.dto.request.UpdateInterviewRequest;
import com.fpt.careermate.services.job_services.service.dto.response.InterviewScheduleResponse;

import java.util.List;
import java.util.Map;

/**
 * Service interface for interview scheduling and management.
 * Handles interview creation, confirmation, rescheduling, and completion.
 * 
 * @since 1.0
 */
public interface InterviewScheduleService {

    /**
     * Schedule a new interview for a job application.
     * Sets status to SCHEDULED and sends notification to candidate.
     * 
     * @param jobApplyId The job application ID
     * @param request Contains scheduled date, type, location, interviewer details
     * @return InterviewScheduleResponse with interview details
     */
    InterviewScheduleResponse scheduleInterview(Integer jobApplyId, InterviewScheduleRequest request);

    /**
     * Candidate confirms they will attend the interview.
     * Updates candidateConfirmed flag and sets confirmation timestamp.
     * 
     * @param interviewId The interview schedule ID
     * @return InterviewScheduleResponse with confirmation details
     */
    InterviewScheduleResponse confirmInterview(Integer interviewId);

    /**
     * Mark interview as completed after it occurs.
     * Records interviewer notes, outcome, and completion timestamp.
     * 
     * @param interviewId The interview schedule ID
     * @param request Contains interviewer notes, outcome (PASS/FAIL/PENDING/NEEDS_SECOND_ROUND)
     * @return InterviewScheduleResponse with completion details
     */
    InterviewScheduleResponse completeInterview(Integer interviewId, CompleteInterviewRequest request);

    /**
     * Mark interview as NO_SHOW when candidate doesn't attend.
     * Records no-show and may trigger follow-up actions.
     * 
     * @param interviewId The interview schedule ID
     * @param notes Optional notes about the no-show
     * @return InterviewScheduleResponse with updated status
     */
    InterviewScheduleResponse markNoShow(Integer interviewId, String notes);

    /**
     * Cancel an interview.
     * Updates status to CANCELLED and records cancellation reason.
     * 
     * @param interviewId The interview schedule ID
     * @param reason Reason for cancellation
     * @return InterviewScheduleResponse with cancellation details
     */
    InterviewScheduleResponse cancelInterview(Integer interviewId, String reason);

    /**
     * Adjust interview duration.
     * Updates the durationMinutes field.
     * 
     * @param interviewId The interview schedule ID
     * @param newDurationMinutes New duration in minutes
     * @return InterviewScheduleResponse with updated duration
     */
    InterviewScheduleResponse adjustDuration(Integer interviewId, Integer newDurationMinutes);

    /**
     * Complete interview early (before expected end time).
     * Marks as completed with actual completion time.
     * 
     * @param interviewId The interview schedule ID
     * @param request Contains interviewer notes and outcome
     * @return InterviewScheduleResponse with early completion details
     */
    InterviewScheduleResponse completeEarly(Integer interviewId, CompleteInterviewRequest request);

    /**
     * Get interview schedule by ID.
     * 
     * @param interviewId The interview schedule ID
     * @return InterviewScheduleResponse with full details
     */
    InterviewScheduleResponse getInterviewById(Integer interviewId);

    /**
     * Get interview schedule for a job application.
     * 
     * @param jobApplyId The job application ID
     * @return InterviewScheduleResponse if interview exists, null otherwise
     */
    InterviewScheduleResponse getInterviewByJobApply(Integer jobApplyId);

    /**
     * Update an existing interview schedule.
     * Allows updating date, time, type, location, and other details.
     * Only non-null fields in the request will be updated.
     * 
     * @param interviewId The interview schedule ID
     * @param request Contains fields to update
     * @return InterviewScheduleResponse with updated details
     */
    InterviewScheduleResponse updateInterview(Integer interviewId, UpdateInterviewRequest request);

    /**
     * Get upcoming interviews for a recruiter.
     * 
     * @param recruiterId The recruiter ID
     * @return List of upcoming interviews
     */
    List<InterviewScheduleResponse> getRecruiterUpcomingInterviews(Integer recruiterId);
    
    /**
     * Get all scheduled/confirmed interviews for a recruiter (regardless of date).
     * This includes past interviews that haven't been completed yet,
     * allowing recruiters to complete interviews after the scheduled time.
     * 
     * @param recruiterId The recruiter ID
     * @return List of scheduled/confirmed interviews (both past and future)
     */
    List<InterviewScheduleResponse> getRecruiterScheduledInterviews(Integer recruiterId);

    /**
     * Get candidate's upcoming interviews.
     * 
     * @param candidateId The candidate ID
     * @return List of upcoming interviews
     */
    List<InterviewScheduleResponse> getCandidateUpcomingInterviews(Integer candidateId);

    /**
     * Get candidate's past interviews.
     * 
     * @param candidateId The candidate ID
     * @return List of past interviews (completed, no-show, cancelled)
     */
    List<InterviewScheduleResponse> getCandidatePastInterviews(Integer candidateId);

    /**
     * Get pending interviews for a recruiter (awaiting candidate confirmation).
     * 
     * @param recruiterId The recruiter ID
     * @return List of pending interviews
     */
    List<InterviewScheduleResponse> getRecruiterPendingInterviews(Integer recruiterId);

    /**
     * Get interview statistics for a recruiter.
     * 
     * @param recruiterId The recruiter ID
     * @return Map containing interview stats (total, confirmed, pending, completed, etc.)
     */
    Map<String, Object> getRecruiterInterviewStats(Integer recruiterId);

    /**
     * Send 24-hour reminder notifications.
     * Called by scheduled job to find and notify interviews 24 hours away.
     * 
     * @return Number of reminders sent
     */
    Integer send24HourReminders();

    /**
     * Send 2-hour reminder notifications.
     * Called by scheduled job to find and notify interviews 2 hours away.
     * 
     * @return Number of reminders sent
     */
    Integer send2HourReminders();
}
