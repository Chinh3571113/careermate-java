package com.fpt.careermate.services.job_services.web;

import com.fpt.careermate.services.job_services.service.InterviewCalendarService;
import com.fpt.careermate.services.job_services.service.dto.request.BatchWorkingHoursRequest;
import com.fpt.careermate.services.job_services.service.dto.request.ConflictCheckRequest;
import com.fpt.careermate.services.job_services.service.dto.request.RecruiterWorkingHoursRequest;
import com.fpt.careermate.services.job_services.service.dto.request.TimeOffRequest;
import com.fpt.careermate.services.job_services.service.dto.response.*;
import com.fpt.careermate.services.job_services.service.impl.InterviewCalendarServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Interview Calendar", description = "Calendar management for interview scheduling")
public class InterviewCalendarController {

        private final InterviewCalendarService calendarService;

        // =====================================================
        // Working Hours Management
        // =====================================================

        @PostMapping("/recruiters/{recruiterId}/working-hours")
        @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
        @Operation(summary = "Set or update working hours", description = "Configure recruiter's working hours for a specific day of week")
        public ResponseEntity<RecruiterWorkingHoursResponse> setWorkingHours(
                        @PathVariable Integer recruiterId,
                        @Valid @RequestBody RecruiterWorkingHoursRequest request) {

                log.info("REST: Setting working hours for recruiter {}", recruiterId);
                RecruiterWorkingHoursResponse response = calendarService.setWorkingHours(recruiterId, request);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/recruiters/{recruiterId}/working-hours")
        @Operation(summary = "Get working hours configuration", description = "Get all working hours configuration (7 days) for recruiter")
        public ResponseEntity<List<RecruiterWorkingHoursResponse>> getWorkingHours(
                        @PathVariable Integer recruiterId) {

                log.info("REST: Getting working hours for recruiter {}", recruiterId);
                List<RecruiterWorkingHoursResponse> response = calendarService.getWorkingHours(recruiterId);
                return ResponseEntity.ok(response);
        }
        
        @PostMapping("/recruiters/working-hours/batch")
        @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
        @Operation(summary = "Set working hours for multiple days", 
                   description = "Batch operation to set working hours for multiple days at once. Reduces API calls for setting up weekly schedules.")
        public ResponseEntity<BatchWorkingHoursResponse> setBatchWorkingHours(
                        @Valid @RequestBody BatchWorkingHoursRequest request) {

                log.info("REST: Setting batch working hours for recruiter {}", request.getRecruiterId());
                BatchWorkingHoursResponse response = ((InterviewCalendarServiceImpl) calendarService).setBatchWorkingHours(request);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/recruiters/{recruiterId}/available")
        @Operation(summary = "Check availability", description = "Check if recruiter is available at specific date/time")
        public ResponseEntity<Boolean> checkAvailability(
                        @PathVariable Integer recruiterId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @Parameter(description = "Date and time to check (ISO 8601)", example = "2024-12-15T10:00:00") String dateTime,
                        @RequestParam Integer durationMinutes) {

                log.info("REST: Checking availability for recruiter {} at {}", recruiterId, dateTime);
                boolean isAvailable = calendarService.isAvailable(
                                recruiterId,
                                java.time.LocalDateTime.parse(dateTime),
                                durationMinutes);
                return ResponseEntity.ok(isAvailable);
        }

        // =====================================================
        // Time-Off Management
        // =====================================================

        @PostMapping("/recruiters/{recruiterId}/time-off")
        @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
        @Operation(summary = "Request time-off", description = "Submit a time-off request (requires admin approval)")
        public ResponseEntity<RecruiterTimeOffResponse> requestTimeOff(
                        @PathVariable Integer recruiterId,
                        @Valid @RequestBody TimeOffRequest request) {

                log.info("REST: Requesting time-off for recruiter {}", recruiterId);
                RecruiterTimeOffResponse response = calendarService.requestTimeOff(recruiterId, request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping("/recruiters/{recruiterId}/time-off")
        @Operation(summary = "Get time-off periods", description = "Get all time-off periods for recruiter (approved and pending)")
        public ResponseEntity<List<RecruiterTimeOffResponse>> getTimeOffPeriods(
                        @PathVariable Integer recruiterId,
                        @RequestParam(required = false) Boolean approved) {

                log.info("REST: Getting time-off periods for recruiter {}", recruiterId);
                List<RecruiterTimeOffResponse> response = calendarService.getTimeOffPeriods(recruiterId);

                // Filter by approval status if requested
                if (approved != null) {
                        response = response.stream()
                                        .filter(t -> t.getIsApproved().equals(approved))
                                        .toList();
                }

                return ResponseEntity.ok(response);
        }

        @PostMapping("/admin/time-off/{timeOffId}/approve")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Approve time-off", description = "Admin approves a time-off request")
        public ResponseEntity<RecruiterTimeOffResponse> approveTimeOff(
                        @PathVariable Integer timeOffId,
                        @RequestParam Integer adminId) {

                log.info("REST: Approving time-off {} by admin {}", timeOffId, adminId);
                RecruiterTimeOffResponse response = calendarService.approveTimeOff(timeOffId, adminId);
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/time-off/{timeOffId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'RECRUITER')")
        @Operation(summary = "Cancel time-off", description = "Cancel a time-off request")
        public ResponseEntity<Void> cancelTimeOff(@PathVariable Integer timeOffId) {
                log.info("REST: Cancelling time-off {}", timeOffId);
                calendarService.cancelTimeOff(timeOffId);
                return ResponseEntity.noContent().build();
        }

        // =====================================================
        // Conflict Detection
        // =====================================================

        @PostMapping("/check-conflict")
        @Operation(summary = "Check scheduling conflict", description = "Check if proposed interview time would create any conflicts")
        public ResponseEntity<ConflictCheckResponse> checkConflict(
                        @Valid @RequestBody ConflictCheckRequest request) {

                log.info("REST: Checking conflict for recruiter {} at {}",
                                request.getRecruiterId(), request.getProposedStartTime());

                ConflictCheckResponse response = calendarService.checkConflict(
                                request.getRecruiterId(),
                                request.getCandidateId(),
                                request.getProposedStartTime(),
                                request.getDurationMinutes());

                return ResponseEntity.ok(response);
        }

        @GetMapping("/recruiters/{recruiterId}/conflicts")
        @Operation(summary = "Find all conflicts", description = "Find all scheduling conflicts for recruiter in date range")
        public ResponseEntity<List<ConflictCheckResponse>> findConflicts(
                        @PathVariable Integer recruiterId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                log.info("REST: Finding conflicts for recruiter {} from {} to {}",
                                recruiterId, startDate, endDate);

                List<ConflictCheckResponse> response = calendarService.findConflicts(
                                recruiterId, startDate, endDate);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // Available Time Slots
        // =====================================================

        @GetMapping("/recruiters/{recruiterId}/available-slots")
        @Operation(summary = "Get available time slots", description = "Get list of available start times for specific date")
        public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
                        @PathVariable Integer recruiterId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Date to check", example = "2024-12-15") LocalDate date,
                        @RequestParam @Parameter(description = "Interview duration in minutes", example = "60") Integer durationMinutes) {

                log.info("REST: Getting available slots for recruiter {} on {}", recruiterId, date);

                List<LocalTime> slots = calendarService.getAvailableSlots(recruiterId, date, durationMinutes);

                AvailableSlotsResponse response = AvailableSlotsResponse.builder()
                                .recruiterId(recruiterId)
                                .date(date)
                                .durationMinutes(durationMinutes)
                                .availableSlots(slots)
                                .totalSlotsAvailable(slots.size())
                                .build();

                return ResponseEntity.ok(response);
        }

        @GetMapping("/recruiters/{recruiterId}/available-dates")
        @Operation(summary = "Get available dates", description = "Get list of dates that have at least one available slot")
        public ResponseEntity<AvailableDatesResponse> getAvailableDates(
                        @PathVariable Integer recruiterId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam Integer durationMinutes) {

                log.info("REST: Getting available dates for recruiter {} from {} to {}",
                                recruiterId, startDate, endDate);

                List<LocalDate> dates = calendarService.getAvailableDates(
                                recruiterId, startDate, endDate, durationMinutes);

                AvailableDatesResponse response = AvailableDatesResponse.builder()
                                .recruiterId(recruiterId)
                                .startDate(startDate)
                                .endDate(endDate)
                                .durationMinutes(durationMinutes)
                                .availableDates(dates)
                                .totalDatesAvailable(dates.size())
                                .build();

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // Calendar Views
        // =====================================================

        @GetMapping("/recruiters/{recruiterId}/daily")
        @Operation(summary = "Get daily calendar", description = "Get detailed view of interviews and availability for a single day")
        public ResponseEntity<DailyCalendarResponse> getDailyCalendar(
                        @PathVariable Integer recruiterId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Date to view", example = "2024-12-15") LocalDate date) {

                log.info("REST: Getting daily calendar for recruiter {} on {}", recruiterId, date);
                DailyCalendarResponse response = calendarService.getDailyCalendar(recruiterId, date);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/recruiters/{recruiterId}/weekly")
        @Operation(summary = "Get weekly calendar", description = "Get 7-day calendar view starting from Monday")
        public ResponseEntity<WeeklyCalendarResponse> getWeeklyCalendar(
                        @PathVariable Integer recruiterId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Week start date (Monday)", example = "2024-12-09") LocalDate weekStartDate) {

                log.info("REST: Getting weekly calendar for recruiter {} starting {}",
                                recruiterId, weekStartDate);

                WeeklyCalendarResponse response = calendarService.getWeeklyCalendar(
                                recruiterId, weekStartDate);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/recruiters/{recruiterId}/monthly")
        @Operation(summary = "Get monthly calendar", description = "Get month-level overview with interview counts per day")
        public ResponseEntity<MonthlyCalendarResponse> getMonthlyCalendar(
                        @PathVariable Integer recruiterId,
                        @RequestParam @Parameter(description = "Year", example = "2024") Integer year,
                        @RequestParam @Parameter(description = "Month (1-12)", example = "12") Integer month) {

                log.info("REST: Getting monthly calendar for recruiter {} for {}-{}",
                                recruiterId, year, month);

                MonthlyCalendarResponse response = calendarService.getMonthlyCalendar(
                                recruiterId, year, month);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/candidates/{candidateId}/calendar")
        @Operation(summary = "Get candidate calendar", description = "Get candidate's upcoming interviews across all companies")
        public ResponseEntity<CandidateCalendarResponse> getCandidateCalendar(
                        @PathVariable Integer candidateId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

                LocalDate start = startDate != null ? startDate : LocalDate.now();
                LocalDate end = endDate != null ? endDate : start.plusDays(30);

                log.info("REST: Getting candidate calendar for candidate {} from {} to {}",
                                candidateId, start, end);

                CandidateCalendarResponse response = calendarService.getCandidateCalendar(
                                candidateId, start, end);

                return ResponseEntity.ok(response);
        }

        // =====================================================
        // Statistics & Analytics
        // =====================================================

        @GetMapping("/recruiters/{recruiterId}/statistics")
        @Operation(summary = "Get scheduling statistics", description = "Get comprehensive scheduling analytics for recruiter")
        public ResponseEntity<RecruiterSchedulingStatsResponse> getSchedulingStats(
                        @PathVariable Integer recruiterId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "Start date for statistics", example = "2024-12-01") LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @Parameter(description = "End date for statistics", example = "2024-12-31") LocalDate endDate) {

                log.info("REST: Getting scheduling statistics for recruiter {} from {} to {}",
                                recruiterId, startDate, endDate);

                RecruiterSchedulingStatsResponse response = calendarService.getSchedulingStats(
                                recruiterId, startDate, endDate);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/recruiters/{recruiterId}/suggest-times")
        @Operation(summary = "Suggest optimal interview times", description = "Get AI-suggested optimal times based on historical data")
        public ResponseEntity<SuggestedTimesResponse> suggestOptimalTimes(
                        @PathVariable Integer recruiterId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam Integer durationMinutes) {

                log.info("REST: Suggesting optimal times for recruiter {} on {}", recruiterId, date);

                List<LocalTime> suggestedTimes = calendarService.suggestOptimalTimes(
                                recruiterId, date, durationMinutes);

                SuggestedTimesResponse response = SuggestedTimesResponse.builder()
                                .recruiterId(recruiterId)
                                .date(date)
                                .durationMinutes(durationMinutes)
                                .suggestedTimes(suggestedTimes)
                                .build();

                return ResponseEntity.ok(response);
        }
}
