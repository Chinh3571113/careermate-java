package com.fpt.careermate.services.job_services.service;

import com.fpt.careermate.services.job_services.service.dto.request.RecruiterWorkingHoursRequest;
import com.fpt.careermate.services.job_services.service.dto.request.TimeOffRequest;
import com.fpt.careermate.services.job_services.service.dto.response.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Service interface for interview calendar management
 * Handles availability, conflict detection, and calendar views
 * 
 * @since 1.1 - Calendar Feature
 */
public interface InterviewCalendarService {
    
    // =====================================================
    // Working Hours Management
    // =====================================================
    
    /**
     * Set or update working hours for authenticated recruiter
     */
    RecruiterWorkingHoursResponse setWorkingHours(RecruiterWorkingHoursRequest request);
    
    /**
     * Get working hours configuration for authenticated recruiter
     */
    List<RecruiterWorkingHoursResponse> getWorkingHours();
    
    /**
     * Check if recruiter is available at specific date/time
     */
    boolean isAvailable(Integer recruiterId, LocalDateTime dateTime, Integer durationMinutes);
    
    // =====================================================
    // Conflict Detection
    // =====================================================
    
    /**
     * Check if scheduling would create a conflict
     */
    ConflictCheckResponse checkConflict(Integer recruiterId, Integer candidateId, 
                                        LocalDateTime proposedStartTime, Integer durationMinutes);
    
    /**
     * Find all conflicts for recruiter in date range
     */
    List<ConflictCheckResponse> findConflicts(Integer recruiterId, LocalDate startDate, LocalDate endDate);
    
    // =====================================================
    // Available Time Slots
    // =====================================================
    
    /**
     * Get available time slots for recruiter on specific date
     * Returns list of start times where interview can be scheduled
     */
    List<LocalTime> getAvailableSlots(Integer recruiterId, LocalDate date, Integer durationMinutes);
    
    /**
     * Get available dates for recruiter in date range
     * Returns list of dates that have at least one available slot
     */
    List<LocalDate> getAvailableDates(Integer recruiterId, LocalDate startDate, LocalDate endDate, Integer durationMinutes);
    
    // =====================================================
    // Calendar Views
    // =====================================================
    
    /**
     * Get daily calendar view for recruiter
     * Shows all interviews scheduled on specific date
     */
    DailyCalendarResponse getDailyCalendar(Integer recruiterId, LocalDate date);
    
    /**
     * Get weekly calendar view for recruiter
     * Shows interviews for the week containing the given date
     */
    WeeklyCalendarResponse getWeeklyCalendar(Integer recruiterId, LocalDate weekStartDate);
    
    /**
     * Get monthly calendar view for recruiter
     * Shows interview counts per day for the month
     */
    MonthlyCalendarResponse getMonthlyCalendar(Integer recruiterId, Integer year, Integer month);
    
    /**
     * Get candidate's calendar view (all upcoming interviews)
     */
    CandidateCalendarResponse getCandidateCalendar(Integer candidateId, LocalDate startDate, LocalDate endDate);
    
    // =====================================================
    // Statistics & Analytics
    // =====================================================
    
    /**
     * Get recruiter's scheduling statistics
     */
    RecruiterSchedulingStatsResponse getSchedulingStats(Integer recruiterId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Calculate optimal interview time based on historical data
     */
    List<LocalTime> suggestOptimalTimes(Integer recruiterId, LocalDate date, Integer durationMinutes);
}
