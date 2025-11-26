package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.services.job_services.service.dto.request.CompleteInterviewRequest;
import com.fpt.careermate.services.job_services.service.dto.request.InterviewScheduleRequest;
import com.fpt.careermate.services.job_services.service.dto.request.RescheduleInterviewRequest;
import com.fpt.careermate.services.job_services.service.dto.response.InterviewScheduleResponse;
import com.fpt.careermate.services.job_services.service.InterviewScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for interview scheduling and management.
 * Handles interview creation, confirmation, rescheduling, and completion.
 * 
 * @since 1.0
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Interview Scheduling", description = "Interview scheduling and management endpoints")
public class InterviewScheduleController {

    InterviewScheduleService interviewScheduleService;

    /**
     * Schedule a new interview for a job application.
     * Recruiter creates interview with date, time, location, and interviewer details.
     * 
     * @param jobApplyId The job application ID
     * @param request Interview details (date, type, location, interviewer info)
     * @return Interview schedule response
     */
    @PostMapping("/job-applies/{jobApplyId}/schedule-interview")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Schedule interview", 
               description = "Recruiter schedules an interview for a job application")
    public ResponseEntity<InterviewScheduleResponse> scheduleInterview(
            @PathVariable Integer jobApplyId,
            @Valid @RequestBody InterviewScheduleRequest request) {
        
        log.info("Scheduling interview for job apply ID: {}", jobApplyId);
        
        InterviewScheduleResponse response = interviewScheduleService.scheduleInterview(jobApplyId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Candidate confirms they will attend the interview.
     * Updates confirmation flag and timestamp.
     * 
     * @param interviewId The interview schedule ID
     * @return Interview schedule response with confirmation
     */
    @PostMapping("/interviews/{interviewId}/confirm")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Confirm interview attendance", 
               description = "Candidate confirms they will attend the interview")
    public ResponseEntity<InterviewScheduleResponse> confirmInterview(
            @PathVariable Integer interviewId) {
        
        log.info("Candidate confirming interview ID: {}", interviewId);
        
        InterviewScheduleResponse response = interviewScheduleService.confirmInterview(interviewId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Request to reschedule an interview.
     * Either recruiter or candidate can request, may need consent from other party.
     * 
     * @param interviewId The interview schedule ID
     * @param request Reschedule details (new date, reason, requested by)
     * @return Interview schedule response with reschedule request
     */
    @PostMapping("/interviews/{interviewId}/reschedule")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
    @Operation(summary = "Request interview reschedule", 
               description = "Request to reschedule an interview to a different date/time")
    public ResponseEntity<InterviewScheduleResponse> requestReschedule(
            @PathVariable Integer interviewId,
            @Valid @RequestBody RescheduleInterviewRequest request) {
        
        log.info("Reschedule requested for interview ID: {}", interviewId);
        
        InterviewScheduleResponse response = interviewScheduleService.requestReschedule(interviewId, request);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Respond to a reschedule request (accept or reject).
     * Updates interview date if accepted, or rejects the request.
     * 
     * @param rescheduleRequestId The reschedule request ID
     * @param accepted Whether to accept (true) or reject (false)
     * @param responseNotes Optional notes for the response
     * @return Interview schedule response with updated details
     */
    @PostMapping("/interviews/reschedule-requests/{rescheduleRequestId}/respond")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
    @Operation(summary = "Respond to reschedule request", 
               description = "Accept or reject an interview reschedule request")
    public ResponseEntity<InterviewScheduleResponse> respondToReschedule(
            @PathVariable Integer rescheduleRequestId,
            @RequestParam boolean accepted,
            @RequestParam(required = false) String responseNotes) {
        
        log.info("Responding to reschedule request ID: {}, accepted: {}", rescheduleRequestId, accepted);
        
        InterviewScheduleResponse response = interviewScheduleService.respondToReschedule(
                rescheduleRequestId, accepted, responseNotes);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Mark interview as completed after it occurs.
     * Recruiter records outcome and interviewer notes.
     * 
     * @param interviewId The interview schedule ID
     * @param request Completion details (outcome, interviewer notes)
     * @return Interview schedule response with completion details
     */
    @PostMapping("/interviews/{interviewId}/complete")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Complete interview", 
               description = "Mark interview as completed with outcome and notes")
    public ResponseEntity<InterviewScheduleResponse> completeInterview(
            @PathVariable Integer interviewId,
            @Valid @RequestBody CompleteInterviewRequest request) {
        
        log.info("Completing interview ID: {}", interviewId);
        
        InterviewScheduleResponse response = interviewScheduleService.completeInterview(interviewId, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Mark interview as NO_SHOW when candidate doesn't attend.
     * 
     * @param interviewId The interview schedule ID
     * @param notes Optional notes about the no-show
     * @return Interview schedule response with NO_SHOW status
     */
    @PostMapping("/interviews/{interviewId}/no-show")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Mark as no-show", 
               description = "Mark interview as no-show when candidate doesn't attend")
    public ResponseEntity<InterviewScheduleResponse> markNoShow(
            @PathVariable Integer interviewId,
            @RequestParam(required = false) String notes) {
        
        log.info("Marking interview ID {} as no-show", interviewId);
        
        InterviewScheduleResponse response = interviewScheduleService.markNoShow(interviewId, notes);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel an interview.
     * 
     * @param interviewId The interview schedule ID
     * @param reason Reason for cancellation
     * @return Interview schedule response with CANCELLED status
     */
    @PostMapping("/interviews/{interviewId}/cancel")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Cancel interview", 
               description = "Cancel a scheduled interview")
    public ResponseEntity<InterviewScheduleResponse> cancelInterview(
            @PathVariable Integer interviewId,
            @RequestParam String reason) {
        
        log.info("Cancelling interview ID: {}", interviewId);
        
        InterviewScheduleResponse response = interviewScheduleService.cancelInterview(interviewId, reason);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Adjust interview duration.
     * 
     * @param interviewId The interview schedule ID
     * @param newDurationMinutes New duration in minutes
     * @return Interview schedule response with updated duration
     */
    @PatchMapping("/interviews/{interviewId}/adjust-duration")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Adjust interview duration", 
               description = "Update the duration of a scheduled interview")
    public ResponseEntity<InterviewScheduleResponse> adjustDuration(
            @PathVariable Integer interviewId,
            @RequestParam Integer newDurationMinutes) {
        
        log.info("Adjusting duration for interview ID: {} to {} minutes", interviewId, newDurationMinutes);
        
        InterviewScheduleResponse response = interviewScheduleService.adjustDuration(interviewId, newDurationMinutes);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Complete interview early (before expected end time).
     * 
     * @param interviewId The interview schedule ID
     * @param request Completion details
     * @return Interview schedule response with early completion
     */
    @PostMapping("/interviews/{interviewId}/complete-early")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Complete interview early", 
               description = "Mark interview as completed before expected end time")
    public ResponseEntity<InterviewScheduleResponse> completeEarly(
            @PathVariable Integer interviewId,
            @Valid @RequestBody CompleteInterviewRequest request) {
        
        log.info("Completing interview ID {} early", interviewId);
        
        InterviewScheduleResponse response = interviewScheduleService.completeEarly(interviewId, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get interview schedule by ID.
     * 
     * @param interviewId The interview schedule ID
     * @return Interview schedule details
     */
    @GetMapping("/interviews/{interviewId}")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
    @Operation(summary = "Get interview details", 
               description = "Get full details of an interview schedule")
    public ResponseEntity<InterviewScheduleResponse> getInterviewById(
            @PathVariable Integer interviewId) {
        
        log.info("Getting interview ID: {}", interviewId);
        
        InterviewScheduleResponse response = interviewScheduleService.getInterviewById(interviewId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get recruiter's upcoming interviews.
     * 
     * @param recruiterId The recruiter ID
     * @return List of upcoming interviews
     */
    @GetMapping("/interviews/recruiter/{recruiterId}/upcoming")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get recruiter's upcoming interviews", 
               description = "Get all upcoming interviews for a recruiter")
    public ResponseEntity<Map<String, Object>> getRecruiterUpcomingInterviews(
            @PathVariable Integer recruiterId) {
        
        log.info("Getting upcoming interviews for recruiter ID: {}", recruiterId);
        
        List<InterviewScheduleResponse> interviews = interviewScheduleService.getRecruiterUpcomingInterviews(recruiterId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", interviews.size());
        response.put("interviews", interviews);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get candidate's upcoming interviews.
     * 
     * @param candidateId The candidate ID
     * @return List of upcoming interviews
     */
    @GetMapping("/interviews/candidate/{candidateId}/upcoming")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get candidate's upcoming interviews", 
               description = "Get all upcoming interviews for a candidate")
    public ResponseEntity<Map<String, Object>> getCandidateUpcomingInterviews(
            @PathVariable Integer candidateId) {
        
        log.info("Getting upcoming interviews for candidate ID: {}", candidateId);
        
        List<InterviewScheduleResponse> interviews = interviewScheduleService.getCandidateUpcomingInterviews(candidateId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", interviews.size());
        response.put("interviews", interviews);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get candidate's past interviews.
     * 
     * @param candidateId The candidate ID
     * @return List of past interviews
     */
    @GetMapping("/interviews/candidate/{candidateId}/past")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get candidate's past interviews", 
               description = "Get all past interviews (completed, no-show, cancelled) for a candidate")
    public ResponseEntity<Map<String, Object>> getCandidatePastInterviews(
            @PathVariable Integer candidateId) {
        
        log.info("Getting past interviews for candidate ID: {}", candidateId);
        
        List<InterviewScheduleResponse> interviews = interviewScheduleService.getCandidatePastInterviews(candidateId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("count", interviews.size());
        response.put("interviews", interviews);
        
        return ResponseEntity.ok(response);
    }
}
