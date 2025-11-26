package com.fpt.careermate.services.job_services.service.impl;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.constant.InterviewStatus;
import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.services.job_services.domain.InterviewRescheduleRequest;
import com.fpt.careermate.services.job_services.domain.InterviewRescheduleRequest.RescheduleStatus;
import com.fpt.careermate.services.job_services.domain.InterviewSchedule;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.repository.InterviewRescheduleRequestRepo;
import com.fpt.careermate.services.job_services.repository.InterviewScheduleRepo;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.job_services.service.InterviewCalendarService;
import com.fpt.careermate.services.job_services.service.dto.request.CompleteInterviewRequest;
import com.fpt.careermate.services.job_services.service.dto.request.InterviewScheduleRequest;
import com.fpt.careermate.services.job_services.service.dto.request.RescheduleInterviewRequest;
import com.fpt.careermate.services.job_services.service.dto.response.ConflictCheckResponse;
import com.fpt.careermate.services.job_services.service.dto.response.InterviewScheduleResponse;
import com.fpt.careermate.services.job_services.service.InterviewScheduleService;
import com.fpt.careermate.services.job_services.service.mapper.InterviewScheduleMapper;
import com.fpt.careermate.services.kafka.dto.NotificationEvent;
import com.fpt.careermate.services.kafka.producer.NotificationProducer;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service implementation for interview scheduling and management.
 * Handles the complete interview lifecycle from scheduling to completion.
 * 
 * @since 1.0
 */
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InterviewScheduleServiceImpl implements InterviewScheduleService {

    InterviewScheduleRepo interviewRepo;
    JobApplyRepo jobApplyRepo;
    InterviewRescheduleRequestRepo rescheduleRequestRepo;
    InterviewScheduleMapper interviewMapper;
    InterviewCalendarService calendarService;

    @Override
    @Transactional
    public InterviewScheduleResponse scheduleInterview(Integer jobApplyId, InterviewScheduleRequest request) {
        log.info("Scheduling interview for job apply ID: {}", jobApplyId);

        JobApply jobApply = jobApplyRepo.findById(jobApplyId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_APPLY_NOT_FOUND));

        if (interviewRepo.existsByJobApplyId(jobApplyId)) {
            throw new AppException(ErrorCode.INTERVIEW_ALREADY_SCHEDULED);
        }

        if (request.getScheduledDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_SCHEDULE_DATE);
        }

        // Check for scheduling conflicts using calendar service
        Integer recruiterId = jobApply.getJobPosting().getRecruiter().getId();
        Integer candidateId = jobApply.getCandidate().getCandidateId();
        Integer durationMinutes = request.getDurationMinutes() != null ? request.getDurationMinutes() : 60;
        
        ConflictCheckResponse conflictCheck = calendarService.checkConflict(
                recruiterId, 
                candidateId, 
                request.getScheduledDate(), 
                durationMinutes
        );
        
        if (conflictCheck.getHasConflict()) {
            log.warn("Scheduling conflict detected: {}", conflictCheck.getConflictReason());
            throw new AppException(ErrorCode.SCHEDULING_CONFLICT);
        }

        InterviewSchedule interview = InterviewSchedule.builder()
                .jobApply(jobApply)
                .interviewRound(request.getInterviewRound() != null ? request.getInterviewRound() : 1)
                .scheduledDate(request.getScheduledDate())
                .durationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60)
                .interviewType(request.getInterviewType())
                .location(request.getLocation())
                .interviewerName(request.getInterviewerName())
                .interviewerEmail(request.getInterviewerEmail())
                .interviewerPhone(request.getInterviewerPhone())
                .preparationNotes(request.getPreparationNotes())
                .meetingLink(request.getMeetingLink())
                .status(InterviewStatus.SCHEDULED)
                .candidateConfirmed(false)
                .reminderSent24h(false)
                .reminderSent2h(false)
                .build();

        interview = interviewRepo.save(interview);

        jobApply.setStatus(StatusJobApply.INTERVIEW_SCHEDULED);
        jobApplyRepo.save(jobApply);

        // TODO: Send notification to candidate

        log.info("Interview scheduled successfully with ID: {}", interview.getId());
        return interviewMapper.toResponse(interview);
    }

    @Override
    @Transactional
    public InterviewScheduleResponse confirmInterview(Integer interviewId) {
        log.info("Candidate confirming interview ID: {}", interviewId);

        InterviewSchedule interview = findInterviewById(interviewId);

        if (Boolean.TRUE.equals(interview.getCandidateConfirmed())) {
            throw new AppException(ErrorCode.INTERVIEW_ALREADY_CONFIRMED);
        }

        interview.setCandidateConfirmed(true);
        interview.setCandidateConfirmedAt(LocalDateTime.now());
        interview.setStatus(InterviewStatus.CONFIRMED);

        interview = interviewRepo.save(interview);

        // TODO: Notify recruiter

        log.info("Interview confirmed successfully");
        return interviewMapper.toResponse(interview);
    }

    @Override
    @Transactional
    public InterviewScheduleResponse requestReschedule(Integer interviewId, RescheduleInterviewRequest request) {
        log.info("Requesting reschedule for interview ID: {}", interviewId);

        InterviewSchedule interview = findInterviewById(interviewId);

        if (interview.getStatus() == InterviewStatus.COMPLETED || 
            interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new AppException(ErrorCode.CANNOT_RESCHEDULE_COMPLETED_INTERVIEW);
        }

        if (request.getNewRequestedDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_SCHEDULE_DATE);
        }

        long hoursUntilInterview = Duration.between(LocalDateTime.now(), interview.getScheduledDate()).toHours();
        if (hoursUntilInterview < 2) {
            throw new AppException(ErrorCode.RESCHEDULE_TOO_LATE);
        }

        InterviewRescheduleRequest rescheduleRequest = InterviewRescheduleRequest.builder()
                .interviewSchedule(interview)
                .originalDate(interview.getScheduledDate())
                .newRequestedDate(request.getNewRequestedDate())
                .reason(request.getReason())
                .requestedBy(request.getRequestedBy())
                .requiresConsent(request.getRequiresConsent() != null ? request.getRequiresConsent() : true)
                .status(RescheduleStatus.PENDING_CONSENT)
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();

        rescheduleRequest = rescheduleRequestRepo.save(rescheduleRequest);

        // TODO: Send notification to other party

        log.info("Reschedule request created with ID: {}", rescheduleRequest.getId());
        return interviewMapper.toResponse(interview);
    }

    @Override
    @Transactional
    public InterviewScheduleResponse respondToReschedule(Integer rescheduleRequestId, boolean accepted, String responseNotes) {
        log.info("Responding to reschedule request ID: {} - Accepted: {}", rescheduleRequestId, accepted);

        InterviewRescheduleRequest rescheduleRequest = rescheduleRequestRepo.findById(rescheduleRequestId.longValue())
                .orElseThrow(() -> new AppException(ErrorCode.RESCHEDULE_REQUEST_NOT_FOUND));

        InterviewSchedule interview = rescheduleRequest.getInterviewSchedule();

        if (rescheduleRequest.getStatus() != RescheduleStatus.PENDING_CONSENT) {
            throw new AppException(ErrorCode.RESCHEDULE_REQUEST_ALREADY_PROCESSED);
        }

        if (rescheduleRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
            rescheduleRequest.setStatus(RescheduleStatus.EXPIRED);
            rescheduleRequestRepo.save(rescheduleRequest);
            throw new AppException(ErrorCode.RESCHEDULE_REQUEST_EXPIRED);
        }

        if (accepted) {
            interview.setScheduledDate(rescheduleRequest.getNewRequestedDate());
            interview.setStatus(InterviewStatus.RESCHEDULED);
            interviewRepo.save(interview);

            rescheduleRequest.setStatus(RescheduleStatus.ACCEPTED);
            rescheduleRequest.setConsentGiven(true);
            rescheduleRequest.setConsentGivenAt(LocalDateTime.now());
            rescheduleRequestRepo.save(rescheduleRequest);

            log.info("Reschedule accepted - Interview moved to {}", interview.getScheduledDate());
        } else {
            rescheduleRequest.setStatus(RescheduleStatus.REJECTED);
            rescheduleRequest.setConsentGiven(false);
            rescheduleRequestRepo.save(rescheduleRequest);

            log.info("Reschedule rejected");
        }

        // TODO: Send notification

        return interviewMapper.toResponse(interview);
    }

    @Override
    @Transactional
    public InterviewScheduleResponse completeInterview(Integer interviewId, CompleteInterviewRequest request) {
        log.info("Completing interview ID: {}", interviewId);

        InterviewSchedule interview = findInterviewById(interviewId);

        LocalDateTime expectedEndTime = interview.getExpectedEndTime();
        if (expectedEndTime != null && LocalDateTime.now().isBefore(expectedEndTime)) {
            throw new AppException(ErrorCode.INTERVIEW_NOT_YET_COMPLETED);
        }

        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setInterviewCompletedAt(LocalDateTime.now());
        interview.setInterviewerNotes(request.getInterviewerNotes());
        interview.setOutcome(request.getOutcome());

        interview = interviewRepo.save(interview);

        JobApply jobApply = interview.getJobApply();
        jobApply.setStatus(StatusJobApply.INTERVIEWED);
        jobApplyRepo.save(jobApply);

        // TODO: Send notification

        log.info("Interview completed successfully");
        return interviewMapper.toResponse(interview);
    }

    @Override
    @Transactional
    public InterviewScheduleResponse markNoShow(Integer interviewId, String notes) {
        log.info("Marking interview ID: {} as NO_SHOW", interviewId);

        InterviewSchedule interview = findInterviewById(interviewId);

        if (LocalDateTime.now().isBefore(interview.getScheduledDate())) {
            throw new AppException(ErrorCode.CANNOT_MARK_NO_SHOW_BEFORE_TIME);
        }

        interview.setStatus(InterviewStatus.NO_SHOW);
        interview.setInterviewerNotes(notes != null ? notes : "Candidate did not attend interview");

        interview = interviewRepo.save(interview);

        JobApply jobApply = interview.getJobApply();
        jobApply.setStatus(StatusJobApply.REJECTED);
        jobApplyRepo.save(jobApply);

        // TODO: Send notification

        log.info("Interview marked as NO_SHOW");
        return interviewMapper.toResponse(interview);
    }

    @Override
    @Transactional
    public InterviewScheduleResponse cancelInterview(Integer interviewId, String reason) {
        log.info("Cancelling interview ID: {}", interviewId);

        InterviewSchedule interview = findInterviewById(interviewId);

        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_COMPLETED_INTERVIEW);
        }

        interview.setStatus(InterviewStatus.CANCELLED);
        interview.setInterviewerNotes("Cancelled: " + reason);

        interview = interviewRepo.save(interview);

        // TODO: Send notification

        log.info("Interview cancelled successfully");
        return interviewMapper.toResponse(interview);
    }

    @Override
    @Transactional
    public InterviewScheduleResponse adjustDuration(Integer interviewId, Integer newDurationMinutes) {
        log.info("Adjusting duration for interview ID: {} to {} minutes", interviewId, newDurationMinutes);

        InterviewSchedule interview = findInterviewById(interviewId);

        if (newDurationMinutes < 15) {
            throw new AppException(ErrorCode.INVALID_DURATION);
        }

        Integer originalDuration = interview.getDurationMinutes();
        interview.setDurationMinutes(newDurationMinutes);

        interview = interviewRepo.save(interview);

        log.info("Duration adjusted from {} to {} minutes", originalDuration, newDurationMinutes);
        return interviewMapper.toResponse(interview);
    }

    @Override
    @Transactional
    public InterviewScheduleResponse completeEarly(Integer interviewId, CompleteInterviewRequest request) {
        log.info("Completing interview ID: {} early", interviewId);

        InterviewSchedule interview = findInterviewById(interviewId);

        long minutesSinceStart = Duration.between(interview.getScheduledDate(), LocalDateTime.now()).toMinutes();
        long minimumDuration = interview.getDurationMinutes() / 2;

        if (minutesSinceStart < minimumDuration) {
            throw new AppException(ErrorCode.INTERVIEW_TOO_SHORT);
        }

        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setInterviewCompletedAt(LocalDateTime.now());
        interview.setInterviewerNotes("Completed early: " + request.getInterviewerNotes());
        interview.setOutcome(request.getOutcome());

        interview = interviewRepo.save(interview);

        JobApply jobApply = interview.getJobApply();
        jobApply.setStatus(StatusJobApply.INTERVIEWED);
        jobApplyRepo.save(jobApply);

        log.info("Interview completed early after {} minutes", minutesSinceStart);
        return interviewMapper.toResponse(interview);
    }

    @Override
    public InterviewScheduleResponse getInterviewById(Integer interviewId) {
        InterviewSchedule interview = findInterviewById(interviewId);
        return interviewMapper.toResponse(interview);
    }

    @Override
    public InterviewScheduleResponse getInterviewByJobApply(Integer jobApplyId) {
        return interviewRepo.findByJobApplyId(jobApplyId)
                .map(interviewMapper::toResponse)
                .orElse(null);
    }

    @Override
    public List<InterviewScheduleResponse> getRecruiterUpcomingInterviews(Integer recruiterId) {
        List<InterviewSchedule> interviews = interviewRepo.findUpcomingInterviewsByRecruiterId(
                recruiterId, 
                LocalDateTime.now()
        );
        return interviews.stream()
                .map(interviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InterviewScheduleResponse> getCandidateUpcomingInterviews(Integer candidateId) {
        List<InterviewSchedule> interviews = interviewRepo.findUpcomingInterviewsByCandidateId(
                candidateId, 
                LocalDateTime.now()
        );
        return interviews.stream()
                .map(interviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InterviewScheduleResponse> getCandidatePastInterviews(Integer candidateId) {
        List<InterviewSchedule> interviews = interviewRepo.findPastInterviewsByCandidateId(
                candidateId, 
                LocalDateTime.now()
        );
        return interviews.stream()
                .map(interviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Integer send24HourReminders() {
        log.info("Sending 24-hour interview reminders");

        LocalDateTime targetTime = LocalDateTime.now().plusHours(24);
        List<InterviewSchedule> interviews = interviewRepo.findInterviewsNeedingReminder(
                targetTime.minusMinutes(30),
                targetTime.plusMinutes(30),
                true  // is24hReminder
        );

        int sentCount = 0;
        for (InterviewSchedule interview : interviews) {
            try {
                // TODO: notificationService.send24HourInterviewReminder(interview);
                interview.setReminderSent24h(true);
                interviewRepo.save(interview);
                sentCount++;
            } catch (Exception e) {
                log.error("Failed to send 24h reminder for interview {}: {}", interview.getId(), e.getMessage());
            }
        }

        log.info("Sent {} 24-hour reminders", sentCount);
        return sentCount;
    }

    @Override
    @Transactional
    public Integer send2HourReminders() {
        log.info("Sending 2-hour interview reminders");

        LocalDateTime targetTime = LocalDateTime.now().plusHours(2);
        List<InterviewSchedule> interviews = interviewRepo.findInterviewsNeedingReminder(
                targetTime.minusMinutes(15),
                targetTime.plusMinutes(15),
                false  // is24hReminder (false = 2h reminder)
        );

        int sentCount = 0;
        for (InterviewSchedule interview : interviews) {
            try {
                // TODO: notificationService.send2HourInterviewReminder(interview);
                interview.setReminderSent2h(true);
                interviewRepo.save(interview);
                sentCount++;
            } catch (Exception e) {
                log.error("Failed to send 2h reminder for interview {}: {}", interview.getId(), e.getMessage());
            }
        }

        log.info("Sent {} 2-hour reminders", sentCount);
        return sentCount;
    }

    private InterviewSchedule findInterviewById(Integer interviewId) {
        return interviewRepo.findById(interviewId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERVIEW_NOT_FOUND));
    }
}
