package com.fpt.careermate.services.job_services.service.impl;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.common.constant.InterviewStatus;
import com.fpt.careermate.common.constant.StatusJobApply;
import com.fpt.careermate.services.job_services.domain.InterviewSchedule;
import com.fpt.careermate.services.job_services.domain.JobApply;
import com.fpt.careermate.services.job_services.repository.InterviewScheduleRepo;
import com.fpt.careermate.services.job_services.repository.JobApplyRepo;
import com.fpt.careermate.services.job_services.service.InterviewCalendarService;
import com.fpt.careermate.services.job_services.service.dto.request.CompleteInterviewRequest;
import com.fpt.careermate.services.job_services.service.dto.request.InterviewScheduleRequest;
import com.fpt.careermate.services.job_services.service.dto.request.UpdateInterviewRequest;
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
    InterviewScheduleMapper interviewMapper;
    InterviewCalendarService calendarService;
    NotificationProducer notificationProducer;

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
    @Transactional
    public InterviewScheduleResponse updateInterview(Integer interviewId, UpdateInterviewRequest request) {
        log.info("Updating interview ID: {}", interviewId);
        
        InterviewSchedule interview = findInterviewById(interviewId);
        
        // Cannot update completed, cancelled, or no-show interviews
        if (interview.getStatus() == InterviewStatus.COMPLETED ||
            interview.getStatus() == InterviewStatus.CANCELLED ||
            interview.getStatus() == InterviewStatus.NO_SHOW) {
            throw new AppException(ErrorCode.INTERVIEW_CANNOT_BE_MODIFIED);
        }
        
        // If updating scheduled date, validate it's in the future and check for conflicts
        if (request.getScheduledDate() != null) {
            if (request.getScheduledDate().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.INVALID_SCHEDULE_DATE);
            }
            
            // Check for scheduling conflicts
            Integer recruiterId = interview.getJobApply().getJobPosting().getRecruiter().getId();
            Integer candidateId = interview.getJobApply().getCandidate().getCandidateId();
            Integer durationMinutes = request.getDurationMinutes() != null ? 
                    request.getDurationMinutes() : interview.getDurationMinutes();
            
            ConflictCheckResponse conflictCheck = calendarService.checkConflict(
                    recruiterId, 
                    candidateId, 
                    request.getScheduledDate(), 
                    durationMinutes
            );
            
            // Ignore conflict with the same interview (self)
            if (conflictCheck.getHasConflict() && !isConflictWithSameInterview(conflictCheck, interviewId)) {
                log.warn("Scheduling conflict detected: {}", conflictCheck.getConflictReason());
                throw new AppException(ErrorCode.SCHEDULING_CONFLICT);
            }
            
            interview.setScheduledDate(request.getScheduledDate());
            
            // Reset candidate confirmation when date changes
            interview.setCandidateConfirmed(false);
            interview.setCandidateConfirmedAt(null);
            interview.setStatus(InterviewStatus.SCHEDULED);
            
            // Reset reminder flags
            interview.setReminderSent24h(false);
            interview.setReminderSent2h(false);
        }
        
        // Update other fields if provided
        if (request.getDurationMinutes() != null) {
            interview.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getInterviewType() != null) {
            interview.setInterviewType(request.getInterviewType());
        }
        if (request.getLocation() != null) {
            interview.setLocation(request.getLocation());
        }
        if (request.getInterviewerName() != null) {
            interview.setInterviewerName(request.getInterviewerName());
        }
        if (request.getInterviewerEmail() != null) {
            interview.setInterviewerEmail(request.getInterviewerEmail());
        }
        if (request.getInterviewerPhone() != null) {
            interview.setInterviewerPhone(request.getInterviewerPhone());
        }
        if (request.getPreparationNotes() != null) {
            interview.setPreparationNotes(request.getPreparationNotes());
        }
        if (request.getMeetingLink() != null) {
            interview.setMeetingLink(request.getMeetingLink());
        }
        if (request.getInterviewRound() != null) {
            interview.setInterviewRound(request.getInterviewRound());
        }
        
        interview = interviewRepo.save(interview);
        
        // Send notification to candidate about the update
        if (request.getScheduledDate() != null) {
            sendInterviewUpdateNotification(interview, request.getUpdateReason());
        }
        
        log.info("Interview updated successfully");
        return interviewMapper.toResponse(interview);
    }
    
    /**
     * Check if conflict is with the same interview being updated (not a real conflict)
     */
    private boolean isConflictWithSameInterview(ConflictCheckResponse conflictCheck, Integer interviewId) {
        // Simple check - if the only conflict is INTERVIEW_OVERLAP, 
        // it could be the same interview we're updating
        return conflictCheck.getConflicts() != null && 
               conflictCheck.getConflicts().size() == 1 &&
               "INTERVIEW_OVERLAP".equals(conflictCheck.getConflicts().get(0).getConflictType());
    }
    
    /**
     * Send notification about interview update
     */
    private void sendInterviewUpdateNotification(InterviewSchedule interview, String updateReason) {
        try {
            JobApply jobApply = interview.getJobApply();
            String candidateEmail = jobApply.getCandidate().getAccount().getEmail();
            String candidateId = String.valueOf(jobApply.getCandidate().getCandidateId());
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("jobTitle", jobApply.getJobPosting().getTitle());
            metadata.put("companyName", jobApply.getJobPosting().getRecruiter().getCompanyName());
            metadata.put("newScheduledDate", interview.getScheduledDate().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            metadata.put("interviewType", interview.getInterviewType().name());
            metadata.put("updateReason", updateReason != null ? updateReason : "Interview details have been updated");
            metadata.put("interviewId", interview.getId());
            
            NotificationEvent event = NotificationEvent.builder()
                    .eventType("EMAIL")
                    .recipientId(candidateId)
                    .recipientEmail(candidateEmail)
                    .subject("Interview Updated - " + jobApply.getJobPosting().getTitle())
                    .title("Interview Schedule Updated")
                    .message("Your interview has been updated. Please review the new details and confirm your attendance.")
                    .category("INTERVIEW_UPDATE")
                    .metadata(metadata)
                    .priority(1) // High priority
                    .build();
            
            notificationProducer.sendNotification("candidate-notifications", event);
            log.info("Interview update notification sent to: {}", candidateEmail);
        } catch (Exception e) {
            log.error("Failed to send interview update notification: {}", e.getMessage());
        }
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
                sendInterviewReminderNotification(interview, "24_HOUR");
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
                sendInterviewReminderNotification(interview, "2_HOUR");
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

    /**
     * Sends interview reminder notifications to both candidate and recruiter.
     * @param interview The interview to send reminders for
     * @param reminderType Either "24_HOUR" or "2_HOUR"
     */
    private void sendInterviewReminderNotification(InterviewSchedule interview, String reminderType) {
        JobApply jobApply = interview.getJobApply();
        String candidateEmail = jobApply.getCandidate().getAccount().getEmail();
        String recruiterEmail = jobApply.getJobPosting().getRecruiter().getAccount().getEmail();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy 'at' HH:mm");
        String scheduledTime = interview.getScheduledDate().format(formatter);
        String timeRemaining = "24_HOUR".equals(reminderType) ? "24 hours" : "2 hours";
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("interviewId", interview.getId());
        metadata.put("jobApplyId", jobApply.getId());
        metadata.put("jobTitle", jobApply.getJobPosting().getTitle());
        metadata.put("scheduledDate", scheduledTime);
        metadata.put("interviewType", interview.getInterviewType());
        metadata.put("location", interview.getLocation());
        metadata.put("meetingLink", interview.getMeetingLink());
        metadata.put("reminderType", reminderType);

        // Send notification to candidate
        String candidateMessage = String.format(
                "⏰ Interview Reminder: Your interview for '%s' is in %s!\n\n" +
                "📅 When: %s\n" +
                "📍 Type: %s\n" +
                "📍 Location: %s\n" +
                (interview.getMeetingLink() != null ? "🔗 Meeting Link: %s\n" : "") +
                "\nPlease be prepared and on time. Good luck!",
                jobApply.getJobPosting().getTitle(),
                timeRemaining,
                scheduledTime,
                interview.getInterviewType(),
                interview.getLocation() != null ? interview.getLocation() : "To be confirmed",
                interview.getMeetingLink()
        );

        NotificationEvent candidateEvent = NotificationEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .recipientEmail(candidateEmail)
                .recipientId(String.valueOf(jobApply.getCandidate().getCandidateId()))
                .category("CANDIDATE")
                .eventType("INTERVIEW_REMINDER_" + reminderType)
                .title("Interview Reminder - " + timeRemaining + " remaining")
                .subject("Interview Reminder: " + jobApply.getJobPosting().getTitle())
                .message(candidateMessage)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();

        notificationProducer.sendNotification("candidate-notifications", candidateEvent);
        log.info("Sent {} interview reminder to candidate: {}", reminderType, candidateEmail);

        // Send notification to recruiter
        String recruiterMessage = String.format(
                "⏰ Interview Reminder: Your interview with %s for '%s' is in %s!\n\n" +
                "📅 When: %s\n" +
                "👤 Candidate: %s\n" +
                "📍 Type: %s\n" +
                "\nPlease review the candidate's application before the interview.",
                jobApply.getFullName(),
                jobApply.getJobPosting().getTitle(),
                timeRemaining,
                scheduledTime,
                jobApply.getFullName(),
                interview.getInterviewType()
        );

        NotificationEvent recruiterEvent = NotificationEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .recipientEmail(recruiterEmail)
                .recipientId(String.valueOf(jobApply.getJobPosting().getRecruiter().getId()))
                .category("RECRUITER")
                .eventType("INTERVIEW_REMINDER_" + reminderType)
                .title("Interview Reminder - " + timeRemaining + " remaining")
                .subject("Interview with " + jobApply.getFullName() + " - " + timeRemaining + " remaining")
                .message(recruiterMessage)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();

        notificationProducer.sendNotification("recruiter-notifications", recruiterEvent);
        log.info("Sent {} interview reminder to recruiter: {}", reminderType, recruiterEmail);
    }

    private InterviewSchedule findInterviewById(Integer interviewId) {
        return interviewRepo.findById(interviewId)
                .orElseThrow(() -> new AppException(ErrorCode.INTERVIEW_NOT_FOUND));
    }

    @Override
    public List<InterviewScheduleResponse> getRecruiterPendingInterviews(Integer recruiterId) {
        log.info("Getting pending interviews for recruiter ID: {}", recruiterId);
        
        // Get all interviews by recruiter and filter for pending status
        List<InterviewSchedule> allInterviews = interviewRepo.findByRecruiterId(recruiterId);
        
        return allInterviews.stream()
                .filter(interview -> interview.getStatus() == InterviewStatus.SCHEDULED)
                .filter(interview -> interview.getScheduledDate().isAfter(LocalDateTime.now()))
                .map(interviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getRecruiterInterviewStats(Integer recruiterId) {
        log.info("Getting interview statistics for recruiter ID: {}", recruiterId);
        
        List<InterviewSchedule> allInterviews = interviewRepo.findByRecruiterId(recruiterId);
        
        long total = allInterviews.size();
        long scheduled = allInterviews.stream().filter(i -> i.getStatus() == InterviewStatus.SCHEDULED).count();
        long confirmed = allInterviews.stream().filter(i -> i.getStatus() == InterviewStatus.CONFIRMED).count();
        long completed = allInterviews.stream().filter(i -> i.getStatus() == InterviewStatus.COMPLETED).count();
        long cancelled = allInterviews.stream().filter(i -> i.getStatus() == InterviewStatus.CANCELLED).count();
        long noShow = allInterviews.stream().filter(i -> i.getStatus() == InterviewStatus.NO_SHOW).count();
        long rescheduled = allInterviews.stream().filter(i -> i.getStatus() == InterviewStatus.RESCHEDULED).count();
        
        long upcoming = allInterviews.stream()
                .filter(i -> i.getScheduledDate().isAfter(LocalDateTime.now()))
                .filter(i -> i.getStatus() == InterviewStatus.SCHEDULED || i.getStatus() == InterviewStatus.CONFIRMED)
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("scheduled", scheduled);
        stats.put("confirmed", confirmed);
        stats.put("completed", completed);
        stats.put("cancelled", cancelled);
        stats.put("noShow", noShow);
        stats.put("rescheduled", rescheduled);
        stats.put("upcoming", upcoming);
        
        return stats;
    }
}
