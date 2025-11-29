package com.fpt.careermate.services.job_services.web.rest;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.job_services.service.dto.request.CompleteInterviewRequest;
import com.fpt.careermate.services.job_services.service.dto.request.InterviewScheduleRequest;
import com.fpt.careermate.services.job_services.service.dto.request.UpdateInterviewRequest;
import com.fpt.careermate.services.job_services.service.dto.response.InterviewScheduleResponse;
import com.fpt.careermate.services.job_services.service.InterviewScheduleService;
import com.fpt.careermate.services.profile_services.domain.Candidate;
import com.fpt.careermate.services.profile_services.repository.CandidateRepo;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    AuthenticationImp authenticationImp;
    RecruiterRepo recruiterRepo;
    CandidateRepo candidateRepo;
    JobApplyRepo jobApplyRepo;
    
    /**
     * Get current recruiter from JWT token.
     * Uses JWT claims first (efficient), falls back to DB lookup if needed.
     */
    private Recruiter getMyRecruiter() {
        Integer recruiterId = authenticationImp.getRecruiterIdFromToken();
        if (recruiterId != null) {
            return recruiterRepo.findById(recruiterId)
                    .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));
        }
        // Fallback for old tokens without recruiterId claim
        Account currentAccount = authenticationImp.findByEmail();
        return recruiterRepo.findByAccount_Id(currentAccount.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));
    }
    
    /**
     * Get current candidate from JWT token.
     * Uses JWT claims first (efficient), falls back to DB lookup if needed.
     */
    private Candidate getMyCandidate() {
        Integer candidateId = authenticationImp.getCandidateIdFromToken();
        if (candidateId != null) {
            return candidateRepo.findById(candidateId)
                    .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));
        }
        // Fallback for old tokens without candidateId claim
        Account currentAccount = authenticationImp.findByEmail();
        return candidateRepo.findByAccount_Id(currentAccount.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CANDIDATE_NOT_FOUND));
    }

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
               description = "Recruiter schedules an interview for a job application. If createdByRecruiterId is not provided, it will be extracted from JWT token.")
    public ResponseEntity<InterviewScheduleResponse> scheduleInterview(
            @PathVariable Integer jobApplyId,
            @Valid @RequestBody InterviewScheduleRequest request) {
        
        log.info("Scheduling interview for job apply ID: {}", jobApplyId);
        
        // Auto-fill recruiterId from JWT if not provided
        if (request.getCreatedByRecruiterId() == null) {
            Recruiter recruiter = getMyRecruiter();
            request.setCreatedByRecruiterId(recruiter.getId());
            log.info("Auto-filled recruiterId from JWT: {}", recruiter.getId());
        }
        
        InterviewScheduleResponse response = interviewScheduleService.scheduleInterview(jobApplyId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get existing interview for a job application.
     * - Recruiter: Can view interview for any job application they manage
     * - Candidate: Can only view interview for their own job applications
     * 
     * @param jobApplyId The job application ID
     * @return Interview schedule response or 404 if no interview exists
     */
    @GetMapping("/job-applies/{jobApplyId}/interview")
    @PreAuthorize("hasAnyRole('CANDIDATE', 'RECRUITER')")
    @Operation(summary = "Get interview by job application", 
               description = "Get existing interview for a job application. Candidate can only view their own applications.")
    public ResponseEntity<InterviewScheduleResponse> getInterviewByJobApply(
            @PathVariable Integer jobApplyId) {
        
        log.info("Getting interview for job apply ID: {}", jobApplyId);
        
        // Ownership validation for candidate role
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isCandidate = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_CANDIDATE"));
        
        if (isCandidate) {
            Candidate candidate = getMyCandidate();
            JobApply jobApply = jobApplyRepo.findById(jobApplyId)
                    .orElseThrow(() -> new AppException(ErrorCode.JOB_APPLY_NOT_FOUND));
            
            if (jobApply.getCandidate().getCandidateId() != candidate.getCandidateId()) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
        
        InterviewScheduleResponse response = interviewScheduleService.getInterviewByJobApply(jobApplyId);
        
        if (response == null) {
            throw new AppException(ErrorCode.INTERVIEW_NOT_FOUND);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing interview (for direct rescheduling by recruiter).
     * Unlike reschedule request, this directly updates the interview without consent workflow.
     * Suitable when recruiter needs to change interview details immediately.
     * 
     * @param interviewId The interview schedule ID
     * @param request Update details (date, time, location, interviewer, etc.)
     * @return Updated interview schedule response
     */
    @PutMapping("/interviews/{interviewId}")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Update interview", 
               description = "Directly update interview details. For rescheduling without consent workflow.")
    public ResponseEntity<InterviewScheduleResponse> updateInterview(
            @PathVariable Integer interviewId,
            @Valid @RequestBody UpdateInterviewRequest request) {
        
        log.info("Updating interview ID: {}", interviewId);
        
        InterviewScheduleResponse response = interviewScheduleService.updateInterview(interviewId, request);
        
        return ResponseEntity.ok(response);
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

    // ==================== JWT-Based Endpoints (Auto-detect ID from token) ====================

    /**
     * Get upcoming interviews for authenticated recruiter.
     * Recruiter ID is automatically extracted from JWT token.
     */
    @GetMapping("/interviews/recruiter/upcoming")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get recruiter's upcoming interviews", 
               description = "Get upcoming interviews for the authenticated recruiter. ID extracted from JWT token.")
    public ResponseEntity<Map<String, Object>> getRecruiterUpcomingInterviewsFromToken() {
        Recruiter recruiter = getMyRecruiter();
        log.info("Getting upcoming interviews for recruiter ID: {} (from JWT)", recruiter.getId());
        
        List<InterviewScheduleResponse> interviews = interviewScheduleService.getRecruiterUpcomingInterviews(recruiter.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("recruiterId", recruiter.getId());
        response.put("count", interviews.size());
        response.put("interviews", interviews);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending interviews for authenticated recruiter.
     * Recruiter ID is automatically extracted from JWT token.
     */
    @GetMapping("/interviews/recruiter/pending")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get recruiter's pending interviews", 
               description = "Get pending interviews for the authenticated recruiter. ID extracted from JWT token.")
    public ResponseEntity<Map<String, Object>> getRecruiterPendingInterviewsFromToken() {
        Recruiter recruiter = getMyRecruiter();
        log.info("Getting pending interviews for recruiter ID: {} (from JWT)", recruiter.getId());
        
        List<InterviewScheduleResponse> interviews = interviewScheduleService.getRecruiterPendingInterviews(recruiter.getId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("recruiterId", recruiter.getId());
        response.put("count", interviews.size());
        response.put("interviews", interviews);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get interview statistics for authenticated recruiter.
     * Recruiter ID is automatically extracted from JWT token.
     */
    @GetMapping("/interviews/recruiter/stats")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get recruiter's interview statistics", 
               description = "Get interview statistics for the authenticated recruiter. ID extracted from JWT token.")
    public ResponseEntity<Map<String, Object>> getRecruiterInterviewStatsFromToken() {
        Recruiter recruiter = getMyRecruiter();
        log.info("Getting interview statistics for recruiter ID: {} (from JWT)", recruiter.getId());
        
        Map<String, Object> stats = interviewScheduleService.getRecruiterInterviewStats(recruiter.getId());
        stats.put("recruiterId", recruiter.getId());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Get upcoming interviews for authenticated candidate.
     * Candidate ID is automatically extracted from JWT token.
     */
    @GetMapping("/interviews/candidate/upcoming")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get candidate's upcoming interviews", 
               description = "Get upcoming interviews for the authenticated candidate. ID extracted from JWT token.")
    public ResponseEntity<Map<String, Object>> getCandidateUpcomingInterviewsFromToken() {
        Candidate candidate = getMyCandidate();
        log.info("Getting upcoming interviews for candidate ID: {} (from JWT)", candidate.getCandidateId());
        
        List<InterviewScheduleResponse> interviews = interviewScheduleService.getCandidateUpcomingInterviews(candidate.getCandidateId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("candidateId", candidate.getCandidateId());
        response.put("count", interviews.size());
        response.put("interviews", interviews);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get past interviews for authenticated candidate.
     * Candidate ID is automatically extracted from JWT token.
     */
    @GetMapping("/interviews/candidate/past")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get candidate's past interviews", 
               description = "Get past interviews for the authenticated candidate. ID extracted from JWT token.")
    public ResponseEntity<Map<String, Object>> getCandidatePastInterviewsFromToken() {
        Candidate candidate = getMyCandidate();
        log.info("Getting past interviews for candidate ID: {} (from JWT)", candidate.getCandidateId());
        
        List<InterviewScheduleResponse> interviews = interviewScheduleService.getCandidatePastInterviews(candidate.getCandidateId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("candidateId", candidate.getCandidateId());
        response.put("count", interviews.size());
        response.put("interviews", interviews);
        
        return ResponseEntity.ok(response);
    }
}
