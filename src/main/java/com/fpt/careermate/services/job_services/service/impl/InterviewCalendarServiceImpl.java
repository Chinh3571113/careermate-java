package com.fpt.careermate.services.job_services.service.impl;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.job_services.domain.InterviewSchedule;
import com.fpt.careermate.services.job_services.domain.RecruiterWorkingHours;
import com.fpt.careermate.services.job_services.repository.InterviewScheduleRepo;
import com.fpt.careermate.services.job_services.repository.RecruiterWorkingHoursRepo;
import com.fpt.careermate.services.job_services.service.InterviewCalendarService;
import com.fpt.careermate.services.job_services.service.dto.request.BatchWorkingHoursRequest;
import com.fpt.careermate.services.job_services.service.dto.request.RecruiterWorkingHoursRequest;
import com.fpt.careermate.services.job_services.service.dto.response.*;
import com.fpt.careermate.services.job_services.service.mapper.InterviewScheduleMapper;
import com.fpt.careermate.services.job_services.service.mapper.RecruiterWorkingHoursMapper;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterviewCalendarServiceImpl implements InterviewCalendarService {

    private final RecruiterWorkingHoursRepo workingHoursRepo;
    private final InterviewScheduleRepo interviewScheduleRepo;
    private final RecruiterRepo recruiterRepo;
    private final RecruiterWorkingHoursMapper workingHoursMapper;
    private final InterviewScheduleMapper interviewScheduleMapper;
    private final AuthenticationImp authenticationImp;

    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    public RecruiterWorkingHoursResponse setWorkingHours(RecruiterWorkingHoursRequest request) {
        Recruiter recruiter = getMyRecruiter();
        Integer recruiterId = recruiter.getId();
        
        log.info("Setting working hours for recruiter {} on {}", recruiterId, request.getDayOfWeek());

        RecruiterWorkingHours workingHours = workingHoursRepo
                .findByRecruiterIdAndDayOfWeek(recruiterId, request.getDayOfWeek())
                .orElse(new RecruiterWorkingHours());

        workingHours.setRecruiter(recruiter);
        workingHours.setDayOfWeek(request.getDayOfWeek());
        workingHours.setIsWorkingDay(request.getIsWorkingDay());
        workingHours.setStartTime(request.getStartTime());
        workingHours.setEndTime(request.getEndTime());
        workingHours.setLunchBreakStart(request.getLunchBreakStart());
        workingHours.setLunchBreakEnd(request.getLunchBreakEnd());
        workingHours.setBufferMinutesBetweenInterviews(
                request.getBufferMinutesBetweenInterviews() != null ? request.getBufferMinutesBetweenInterviews() : 15
        );
        workingHours.setMaxInterviewsPerDay(
                request.getMaxInterviewsPerDay() != null ? request.getMaxInterviewsPerDay() : 8
        );

        validateWorkingHours(workingHours);

        RecruiterWorkingHours saved = workingHoursRepo.save(workingHours);
        return workingHoursMapper.toResponse(saved);
    }
    
    /**
     * Get current authenticated recruiter
     * Same pattern as JobPostingImp.getMyRecruiter()
     */
    private Recruiter getMyRecruiter() {
        Account currentAccount = authenticationImp.findByEmail();
        Recruiter recruiter = recruiterRepo.findByAccount_Id(currentAccount.getId())
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_FOUND));

        // Check if recruiter is verified (APPROVED status)
        if (!"APPROVED".equals(recruiter.getVerificationStatus())) {
            throw new AppException(ErrorCode.RECRUITER_NOT_VERIFIED);
        }

        return recruiter;
    }
    
    /**
     * Validate working hours configuration
     */
    private void validateWorkingHours(RecruiterWorkingHours workingHours) {
        if (Boolean.TRUE.equals(workingHours.getIsWorkingDay())) {
            if (workingHours.getStartTime() == null || workingHours.getEndTime() == null) {
                throw new RuntimeException("Working day must have start and end times");
            }
            if (!workingHours.getEndTime().isAfter(workingHours.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }
            
            // Validate lunch break if configured
            if (workingHours.getLunchBreakStart() != null && workingHours.getLunchBreakEnd() != null) {
                if (!workingHours.getLunchBreakEnd().isAfter(workingHours.getLunchBreakStart())) {
                    throw new RuntimeException("Lunch break end time must be after start time");
                }
                if (workingHours.getLunchBreakStart().isBefore(workingHours.getStartTime()) ||
                    workingHours.getLunchBreakEnd().isAfter(workingHours.getEndTime())) {
                    throw new RuntimeException("Lunch break must be within working hours");
                }
            }
            
            // Validate buffer minutes
            if (workingHours.getBufferMinutesBetweenInterviews() != null && 
                (workingHours.getBufferMinutesBetweenInterviews() < 0 || 
                 workingHours.getBufferMinutesBetweenInterviews() > 60)) {
                throw new RuntimeException("Buffer minutes must be between 0 and 60");
            }
            
            // Validate max interviews per day
            if (workingHours.getMaxInterviewsPerDay() != null &&
                (workingHours.getMaxInterviewsPerDay() < 1 || 
                 workingHours.getMaxInterviewsPerDay() > 20)) {
                throw new RuntimeException("Max interviews per day must be between 1 and 20");
            }
        }
    }

    @PreAuthorize("hasRole('RECRUITER')")
    @Override
    @Transactional(readOnly = true)
    public List<RecruiterWorkingHoursResponse> getWorkingHours() {
        Recruiter recruiter = getMyRecruiter();
        Integer recruiterId = recruiter.getId();
        
        log.info("Getting working hours for recruiter {}", recruiterId);
        
        List<RecruiterWorkingHours> workingHours = workingHoursRepo.findByRecruiterId(recruiterId);
        return workingHours.stream()
                .map(workingHoursMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Set working hours for multiple days in one transaction
     * Production-ready batch operation to reduce API calls
     */
    @PreAuthorize("hasRole('RECRUITER')")
    public BatchWorkingHoursResponse setBatchWorkingHours(BatchWorkingHoursRequest request) {
        // Get recruiter from JWT token
        Recruiter recruiter = getMyRecruiter();
        Integer recruiterId = recruiter.getId();
        
        log.info("Setting batch working hours for recruiter {}", recruiterId);
        
        List<RecruiterWorkingHoursResponse> updatedConfigurations = new ArrayList<>();
        Map<String, String> errors = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        
        // Process each configuration
        for (RecruiterWorkingHoursRequest config : request.getWorkingHoursConfigurations()) {
            try {
                RecruiterWorkingHoursResponse response = setWorkingHoursInternal(recruiterId, config);
                updatedConfigurations.add(response);
                successCount++;
            } catch (Exception e) {
                errors.put(config.getDayOfWeek().toString(), e.getMessage());
                failCount++;
                log.warn("Failed to set working hours for day {}: {}", config.getDayOfWeek(), e.getMessage());
            }
        }
        
        // If replaceAll is true, mark unspecified days as non-working
        if (Boolean.TRUE.equals(request.getReplaceAll())) {
            Set<DayOfWeek> specifiedDays = request.getWorkingHoursConfigurations().stream()
                    .map(RecruiterWorkingHoursRequest::getDayOfWeek)
                    .collect(Collectors.toSet());
            
            for (DayOfWeek day : DayOfWeek.values()) {
                if (!specifiedDays.contains(day)) {
                    try {
                        RecruiterWorkingHoursRequest nonWorkingConfig = RecruiterWorkingHoursRequest.builder()
                                .dayOfWeek(day)
                                .isWorkingDay(false)
                                .build();
                        RecruiterWorkingHoursResponse response = setWorkingHoursInternal(recruiterId, nonWorkingConfig);
                        updatedConfigurations.add(response);
                        successCount++;
                    } catch (Exception e) {
                        log.warn("Failed to mark {} as non-working: {}", day, e.getMessage());
                    }
                }
            }
        }
        
        return BatchWorkingHoursResponse.builder()
                .recruiterId(recruiterId)
                .totalConfigurations(request.getWorkingHoursConfigurations().size())
                .successfulUpdates(successCount)
                .failedUpdates(failCount)
                .updatedConfigurations(updatedConfigurations)
                .errors(errors.isEmpty() ? null : errors)
                .build();
    }
    
    /**
     * Internal method for batch operations that accepts recruiterId
     * This allows admin operations to set hours for any recruiter
     */
    private RecruiterWorkingHoursResponse setWorkingHoursInternal(Integer recruiterId, RecruiterWorkingHoursRequest request) {
        log.info("Setting working hours for recruiter {} on {}", recruiterId, request.getDayOfWeek());
        
        Recruiter recruiter = recruiterRepo.findById(recruiterId)
                .orElseThrow(() -> new RuntimeException("Recruiter not found with id: " + recruiterId));

        RecruiterWorkingHours workingHours = workingHoursRepo
                .findByRecruiterIdAndDayOfWeek(recruiterId, request.getDayOfWeek())
                .orElse(new RecruiterWorkingHours());

        workingHours.setRecruiter(recruiter);
        workingHours.setDayOfWeek(request.getDayOfWeek());
        workingHours.setIsWorkingDay(request.getIsWorkingDay());
        workingHours.setStartTime(request.getStartTime());
        workingHours.setEndTime(request.getEndTime());
        workingHours.setLunchBreakStart(request.getLunchBreakStart());
        workingHours.setLunchBreakEnd(request.getLunchBreakEnd());
        workingHours.setBufferMinutesBetweenInterviews(
                request.getBufferMinutesBetweenInterviews() != null ? request.getBufferMinutesBetweenInterviews() : 15
        );
        workingHours.setMaxInterviewsPerDay(
                request.getMaxInterviewsPerDay() != null ? request.getMaxInterviewsPerDay() : 8
        );

        validateWorkingHours(workingHours);

        RecruiterWorkingHours saved = workingHoursRepo.save(workingHours);
        return workingHoursMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailable(Integer recruiterId, LocalDateTime dateTime, Integer durationMinutes) {
        LocalDate date = dateTime.toLocalDate();
        LocalTime time = dateTime.toLocalTime();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Optional<RecruiterWorkingHours> workingHoursOpt = workingHoursRepo
                .findByRecruiterIdAndDayOfWeek(recruiterId, dayOfWeek);
        
        if (workingHoursOpt.isEmpty() || !Boolean.TRUE.equals(workingHoursOpt.get().getIsWorkingDay())) {
            return false;
        }

        RecruiterWorkingHours workingHours = workingHoursOpt.get();
        
        if (!workingHours.isWithinWorkingHours(time)) {
            return false;
        }

        if (workingHours.isDuringLunchBreak(time)) {
            return false;
        }

        LocalDateTime endTime = dateTime.plusMinutes(durationMinutes);
        boolean hasConflict = interviewScheduleRepo.hasConflict(recruiterId, dateTime, endTime);
        
        return !hasConflict;
    }

    @Override
    @Transactional(readOnly = true)
    public ConflictCheckResponse checkConflict(Integer recruiterId, Integer candidateId,
                                              LocalDateTime proposedStartTime, Integer durationMinutes) {
        log.info("Checking conflict for recruiter {} at {}", recruiterId, proposedStartTime);

        List<ConflictCheckResponse.ConflictDetail> conflicts = new ArrayList<>();
        LocalDateTime proposedEndTime = proposedStartTime.plusMinutes(durationMinutes);
        LocalDate date = proposedStartTime.toLocalDate();
        LocalTime time = proposedStartTime.toLocalTime();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        Optional<RecruiterWorkingHours> workingHoursOpt = workingHoursRepo
                .findByRecruiterIdAndDayOfWeek(recruiterId, dayOfWeek);

        if (workingHoursOpt.isEmpty() || !Boolean.TRUE.equals(workingHoursOpt.get().getIsWorkingDay())) {
            conflicts.add(ConflictCheckResponse.ConflictDetail.builder()
                    .conflictType("NON_WORKING_DAY")
                    .conflictStart(proposedStartTime)
                    .conflictEnd(proposedEndTime)
                    .description("Recruiter does not work on " + dayOfWeek)
                    .build());
        }

        RecruiterWorkingHours workingHours = workingHoursOpt.orElse(null);

        if (workingHours != null && Boolean.TRUE.equals(workingHours.getIsWorkingDay())) {
            LocalTime endTime = proposedEndTime.toLocalTime();
            
            if (!workingHours.isWithinWorkingHours(time) || 
                !workingHours.isWithinWorkingHours(endTime.minusMinutes(1))) {
                conflicts.add(ConflictCheckResponse.ConflictDetail.builder()
                        .conflictType("OUTSIDE_WORKING_HOURS")
                        .conflictStart(proposedStartTime)
                        .conflictEnd(proposedEndTime)
                        .description(String.format("Interview must be within working hours (%s - %s)",
                                workingHours.getStartTime(), workingHours.getEndTime()))
                        .build());
            }

            if (workingHours.getLunchBreakStart() != null && workingHours.getLunchBreakEnd() != null) {
                if (workingHours.isDuringLunchBreak(time) || 
                    workingHours.isDuringLunchBreak(endTime.minusMinutes(1))) {
                    conflicts.add(ConflictCheckResponse.ConflictDetail.builder()
                            .conflictType("DURING_LUNCH_BREAK")
                            .conflictStart(proposedStartTime)
                            .conflictEnd(proposedEndTime)
                            .description(String.format("Interview overlaps with lunch break (%s - %s)",
                                    workingHours.getLunchBreakStart(), workingHours.getLunchBreakEnd()))
                            .build());
                }
            }

            Long interviewCount = interviewScheduleRepo.countInterviewsOnDate(recruiterId, date);
            if (interviewCount >= workingHours.getMaxInterviewsPerDay()) {
                conflicts.add(ConflictCheckResponse.ConflictDetail.builder()
                        .conflictType("MAX_INTERVIEWS_REACHED")
                        .conflictStart(proposedStartTime)
                        .conflictEnd(proposedEndTime)
                        .description(String.format("Maximum interviews per day reached (%d/%d)",
                                interviewCount, workingHours.getMaxInterviewsPerDay()))
                        .build());
            }
        }

        List<InterviewSchedule> overlappingInterviews = interviewScheduleRepo
                .findOverlappingInterviews(recruiterId, proposedStartTime, proposedEndTime);
        
        for (InterviewSchedule interview : overlappingInterviews) {
            conflicts.add(ConflictCheckResponse.ConflictDetail.builder()
                    .conflictType("INTERVIEW_OVERLAP")
                    .conflictStart(interview.getScheduledDate())
                    .conflictEnd(interview.getScheduledDate().plusMinutes(interview.getDurationMinutes()))
                    .conflictingInterviewId(interview.getId())
                    .description(String.format("Overlaps with existing interview (ID: %d, %s - %s)",
                            interview.getId(),
                            interview.getScheduledDate(),
                            interview.getScheduledDate().plusMinutes(interview.getDurationMinutes())))
                    .build());
        }

        boolean candidateHasConflict = interviewScheduleRepo.candidateHasConflict(
                candidateId, proposedStartTime, proposedEndTime);
        
        if (candidateHasConflict) {
            conflicts.add(ConflictCheckResponse.ConflictDetail.builder()
                    .conflictType("INTERVIEW_OVERLAP")
                    .conflictStart(proposedStartTime)
                    .conflictEnd(proposedEndTime)
                    .description("Candidate has another interview at this time")
                    .build());
        }

        boolean hasConflict = !conflicts.isEmpty();
        String conflictReason = hasConflict 
                ? "Conflicts detected: " + conflicts.stream()
                    .map(ConflictCheckResponse.ConflictDetail::getConflictType)
                    .distinct()
                    .collect(Collectors.joining(", "))
                : null;

        return ConflictCheckResponse.builder()
                .hasConflict(hasConflict)
                .conflictReason(conflictReason)
                .conflicts(conflicts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConflictCheckResponse> findConflicts(Integer recruiterId, LocalDate startDate, LocalDate endDate) {
        log.info("Finding conflicts for recruiter {} from {} to {}", recruiterId, startDate, endDate);

        List<InterviewSchedule> interviews = interviewScheduleRepo
                .findByRecruiterIdAndDateRange(recruiterId, startDate, endDate);

        return interviews.stream()
                .map(interview -> checkConflict(
                        recruiterId,
                        interview.getJobApply().getCandidate().getCandidateId(),
                        interview.getScheduledDate(),
                        interview.getDurationMinutes()
                ))
                .filter(response -> Boolean.TRUE.equals(response.getHasConflict()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(Integer recruiterId, LocalDate date, Integer durationMinutes) {
        log.info("Getting available slots for recruiter {} on {}", recruiterId, date);

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        Optional<RecruiterWorkingHours> workingHoursOpt = workingHoursRepo
                .findByRecruiterIdAndDayOfWeek(recruiterId, dayOfWeek);

        if (workingHoursOpt.isEmpty() || !Boolean.TRUE.equals(workingHoursOpt.get().getIsWorkingDay())) {
            return Collections.emptyList();
        }

        RecruiterWorkingHours workingHours = workingHoursOpt.get();

        List<InterviewSchedule> existingInterviews = interviewScheduleRepo
                .findByRecruiterIdAndDate(recruiterId, date);

        List<LocalTime> candidateSlots = new ArrayList<>();
        LocalTime current = workingHours.getStartTime();
        LocalTime end = workingHours.getEndTime();

        while (current.isBefore(end)) {
            candidateSlots.add(current);
            current = current.plusMinutes(15);
        }

        List<LocalTime> availableSlots = new ArrayList<>();
        Integer bufferMinutes = workingHours.getBufferMinutesBetweenInterviews();

        for (LocalTime slot : candidateSlots) {
            LocalDateTime proposedStart = LocalDateTime.of(date, slot);
            LocalDateTime proposedEnd = proposedStart.plusMinutes(durationMinutes);

            if (isSlotAvailable(slot, proposedEnd.toLocalTime(), workingHours, existingInterviews, bufferMinutes)) {
                availableSlots.add(slot);
            }
        }

        return availableSlots;
    }

    private boolean isSlotAvailable(LocalTime slotStart, LocalTime slotEnd,
                                   RecruiterWorkingHours workingHours, 
                                   List<InterviewSchedule> existingInterviews,
                                   Integer bufferMinutes) {
        if (!workingHours.isWithinWorkingHours(slotStart) || 
            !workingHours.isWithinWorkingHours(slotEnd.minusMinutes(1))) {
            return false;
        }

        if (workingHours.getLunchBreakStart() != null) {
            if (workingHours.isDuringLunchBreak(slotStart) || 
                workingHours.isDuringLunchBreak(slotEnd.minusMinutes(1))) {
                return false;
            }
        }

        for (InterviewSchedule interview : existingInterviews) {
            LocalTime interviewStart = interview.getScheduledDate().toLocalTime();
            LocalTime interviewEnd = interviewStart.plusMinutes(interview.getDurationMinutes());

            if (slotStart.isBefore(interviewEnd) && slotEnd.isAfter(interviewStart)) {
                return false;
            }

            if (slotStart.isAfter(interviewStart) && Duration.between(interviewEnd, slotStart).toMinutes() < bufferMinutes) {
                return false;
            }
            if (slotEnd.isBefore(interviewStart) && Duration.between(slotEnd, interviewStart).toMinutes() < bufferMinutes) {
                return false;
            }
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalDate> getAvailableDates(Integer recruiterId, LocalDate startDate, 
                                            LocalDate endDate, Integer durationMinutes) {
        log.info("Getting available dates for recruiter {} from {} to {}", recruiterId, startDate, endDate);

        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            List<LocalTime> slots = getAvailableSlots(recruiterId, current, durationMinutes);
            if (!slots.isEmpty()) {
                availableDates.add(current);
            }
            current = current.plusDays(1);
        }

        return availableDates;
    }

    @Override
    @Transactional(readOnly = true)
    public DailyCalendarResponse getDailyCalendar(Integer recruiterId, LocalDate date) {
        log.info("Getting daily calendar for recruiter {} on {}", recruiterId, date);

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        Optional<RecruiterWorkingHours> workingHoursOpt = workingHoursRepo
                .findByRecruiterIdAndDayOfWeek(recruiterId, dayOfWeek);

        RecruiterWorkingHours workingHours = workingHoursOpt.orElse(null);
        boolean isWorkingDay = workingHours != null && Boolean.TRUE.equals(workingHours.getIsWorkingDay());

        List<InterviewSchedule> interviews = interviewScheduleRepo.findByRecruiterIdAndDate(recruiterId, date);
        List<InterviewScheduleResponse> interviewResponses = interviews.stream()
                .map(interviewScheduleMapper::toResponse)
                .collect(Collectors.toList());

        List<LocalTime> availableSlots = isWorkingDay 
                ? getAvailableSlots(recruiterId, date, 60)
                : Collections.emptyList();

        return DailyCalendarResponse.builder()
                .recruiterId(recruiterId)
                .date(date)
                .dayOfWeek(dayOfWeek.toString())
                .isWorkingDay(isWorkingDay)
                .workStartTime(workingHours != null ? workingHours.getStartTime() : null)
                .workEndTime(workingHours != null ? workingHours.getEndTime() : null)
                .totalInterviews(interviews.size())
                .availableSlots(availableSlots.size())
                .interviews(interviewResponses)
                .availableTimeSlots(availableSlots)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyCalendarResponse getWeeklyCalendar(Integer recruiterId, LocalDate weekStartDate) {
        log.info("Getting weekly calendar for recruiter {} starting {}", recruiterId, weekStartDate);

        LocalDate monday = weekStartDate.with(DayOfWeek.MONDAY);
        LocalDate sunday = monday.plusDays(6);

        Map<LocalDate, DailyCalendarResponse> dailyCalendars = new LinkedHashMap<>();
        List<InterviewScheduleResponse> allInterviews = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            DailyCalendarResponse dailyCalendar = getDailyCalendar(recruiterId, date);
            dailyCalendars.put(date, dailyCalendar);
            allInterviews.addAll(dailyCalendar.getInterviews());
        }

        return WeeklyCalendarResponse.builder()
                .recruiterId(recruiterId)
                .weekStartDate(monday)
                .weekEndDate(sunday)
                .totalInterviews(allInterviews.size())
                .dailyCalendars(dailyCalendars)
                .allInterviews(allInterviews)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyCalendarResponse getMonthlyCalendar(Integer recruiterId, Integer year, Integer month) {
        log.info("Getting monthly calendar for recruiter {} for {}-{}", recruiterId, year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        List<InterviewSchedule> interviews = interviewScheduleRepo
                .findByRecruiterIdAndDateRange(recruiterId, firstDay, lastDay);

        Map<LocalDate, Integer> interviewCountByDate = new HashMap<>();
        for (InterviewSchedule interview : interviews) {
            LocalDate date = interview.getScheduledDate().toLocalDate();
            interviewCountByDate.merge(date, 1, Integer::sum);
        }

        List<RecruiterWorkingHours> workingHoursList = workingHoursRepo.findByRecruiterId(recruiterId);
        Map<DayOfWeek, Boolean> workingDaysConfig = workingHoursList.stream()
                .collect(Collectors.toMap(
                        RecruiterWorkingHours::getDayOfWeek,
                        RecruiterWorkingHours::getIsWorkingDay
                ));

        Map<LocalDate, Boolean> workingDays = new HashMap<>();
        LocalDate current = firstDay;
        while (!current.isAfter(lastDay)) {
            Boolean isWorking = workingDaysConfig.getOrDefault(current.getDayOfWeek(), false);
            workingDays.put(current, isWorking);
            current = current.plusDays(1);
        }

        return MonthlyCalendarResponse.builder()
                .recruiterId(recruiterId)
                .year(year)
                .month(month)
                .yearMonth(yearMonth)
                .totalInterviews(interviews.size())
                .interviewCountByDate(interviewCountByDate)
                .workingDays(workingDays)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateCalendarResponse getCandidateCalendar(Integer candidateId, 
                                                         LocalDate startDate, 
                                                         LocalDate endDate) {
        log.info("Getting candidate calendar for candidate {} from {} to {}", 
                candidateId, startDate, endDate);

        List<InterviewSchedule> interviews = interviewScheduleRepo
                .findByCandidateIdAndDate(candidateId, startDate);

        List<InterviewSchedule> upcomingInterviews = interviews.stream()
                .filter(i -> !i.getScheduledDate().toLocalDate().isBefore(startDate) &&
                            !i.getScheduledDate().toLocalDate().isAfter(endDate))
                .sorted(Comparator.comparing(InterviewSchedule::getScheduledDate))
                .collect(Collectors.toList());

        List<InterviewScheduleResponse> interviewResponses = upcomingInterviews.stream()
                .map(interviewScheduleMapper::toResponse)
                .collect(Collectors.toList());

        return CandidateCalendarResponse.builder()
                .candidateId(candidateId)
                .startDate(startDate)
                .endDate(endDate)
                .totalInterviews(upcomingInterviews.size())
                .upcomingInterviews(interviewResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RecruiterSchedulingStatsResponse getSchedulingStats(Integer recruiterId, 
                                                              LocalDate startDate, 
                                                              LocalDate endDate) {
        log.info("Getting scheduling stats for recruiter {} from {} to {}", 
                recruiterId, startDate, endDate);

        List<InterviewSchedule> interviews = interviewScheduleRepo
                .findByRecruiterIdAndDateRange(recruiterId, startDate, endDate);

        int totalScheduled = interviews.size();
        long completed = interviews.stream()
                .filter(i -> i.getStatus().name().equals("COMPLETED"))
                .count();
        long cancelled = interviews.stream()
                .filter(i -> i.getStatus().name().equals("CANCELLED"))
                .count();
        long noShow = interviews.stream()
                .filter(i -> i.getStatus().name().equals("NO_SHOW"))
                .count();

        double totalHours = interviews.stream()
                .mapToDouble(i -> i.getDurationMinutes() / 60.0)
                .sum();
        
        Integer totalInterviewHours = (int) totalHours;

        double avgDuration = interviews.stream()
                .mapToInt(InterviewSchedule::getDurationMinutes)
                .average()
                .orElse(0);

        Map<DayOfWeek, Long> byDayOfWeek = interviews.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getScheduledDate().getDayOfWeek(),
                        Collectors.counting()
                ));
        String busiestDay = byDayOfWeek.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey().toString())
                .orElse("N/A");

        long workingDays = workingHoursRepo.findWorkingDaysByRecruiterId(recruiterId).stream()
                .filter(RecruiterWorkingHours::getIsWorkingDay)
                .count();
        long daysInPeriod = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays() + 1;
        long weeksInPeriod = daysInPeriod / 7;
        double totalWorkingHours = workingDays * weeksInPeriod * 8;

        double utilizationRate = totalWorkingHours > 0 
                ? (totalHours / totalWorkingHours) * 100 
                : 0;
        
        Integer avgDurationMinutes = (int) avgDuration;

        return RecruiterSchedulingStatsResponse.builder()
                .recruiterId(recruiterId)
                .startDate(startDate)
                .endDate(endDate)
                .totalInterviewsScheduled(totalScheduled)
                .completedInterviews((int) completed)
                .cancelledInterviews((int) cancelled)
                .noShowInterviews((int) noShow)
                .totalInterviewHours(totalInterviewHours)
                .averageInterviewDurationMinutes(avgDurationMinutes)
                .utilizationRate(utilizationRate)
                .busiestDayOfWeek(busiestDay)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalTime> suggestOptimalTimes(Integer recruiterId, LocalDate date, Integer durationMinutes) {
        log.info("Suggesting optimal times for recruiter {} on {}", recruiterId, date);

        List<LocalTime> availableSlots = getAvailableSlots(recruiterId, date, durationMinutes);

        List<LocalTime> optimal = new ArrayList<>();
        LocalTime midMorning = LocalTime.of(10, 0);
        LocalTime midAfternoon = LocalTime.of(14, 0);

        for (LocalTime slot : availableSlots) {
            if (slot.equals(midMorning) || slot.equals(midAfternoon)) {
                optimal.add(slot);
            }
        }

        if (optimal.isEmpty()) {
            return availableSlots.stream().limit(3).collect(Collectors.toList());
        }

        return optimal;
    }
}
