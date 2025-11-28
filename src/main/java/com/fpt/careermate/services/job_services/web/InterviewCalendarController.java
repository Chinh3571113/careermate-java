package com.fpt.careermate.services.job_services.web;

import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import com.fpt.careermate.services.account_services.domain.Account;
import com.fpt.careermate.services.authentication_services.service.AuthenticationImp;
import com.fpt.careermate.services.job_services.service.InterviewCalendarService;
import com.fpt.careermate.services.job_services.service.dto.request.BatchWorkingHoursRequest;
import com.fpt.careermate.services.job_services.service.dto.request.ConflictCheckRequest;
import com.fpt.careermate.services.job_services.service.dto.request.ConflictCheckRequestSimple;
import com.fpt.careermate.services.job_services.service.dto.request.RecruiterWorkingHoursRequest;
import com.fpt.careermate.services.job_services.service.dto.response.*;
import com.fpt.careermate.services.job_services.service.impl.InterviewCalendarServiceImpl;
import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import com.fpt.careermate.services.recruiter_services.repository.RecruiterRepo;
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
        private final AuthenticationImp authenticationImp;
        private final RecruiterRepo recruiterRepo;
        
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

        // =====================================================
        // Working Hours Management
        // =====================================================

    @PostMapping("/recruiters/working-hours")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Set or update working hours", description = "Configure recruiter's working hours for a specific day of week (uses authenticated user)")
    public ResponseEntity<RecruiterWorkingHoursResponse> setWorkingHours(
                    @Valid @RequestBody RecruiterWorkingHoursRequest request) {

            log.info("REST: Setting working hours for authenticated recruiter");
            RecruiterWorkingHoursResponse response = calendarService.setWorkingHours(request);
            return ResponseEntity.ok(response);
    }    @GetMapping("/recruiters/working-hours")
    @PreAuthorize("hasRole('RECRUITER')")
    @Operation(summary = "Get working hours configuration", description = "Get all working hours configuration (7 days) for authenticated recruiter")
    public ResponseEntity<List<RecruiterWorkingHoursResponse>> getWorkingHours() {

            log.info("REST: Getting working hours for authenticated recruiter");
            List<RecruiterWorkingHoursResponse> response = calendarService.getWorkingHours();
            return ResponseEntity.ok(response);
    }        @PostMapping("/recruiters/working-hours/batch")
        @PreAuthorize("hasRole('RECRUITER')")
        @Operation(summary = "Set working hours for multiple days", 
                   description = "Batch operation to set working hours for multiple days at once. Uses authenticated user's recruiterId. Reduces API calls for setting up weekly schedules.")
        public ResponseEntity<BatchWorkingHoursResponse> setBatchWorkingHours(
                        @Valid @RequestBody BatchWorkingHoursRequest request) {

                log.info("REST: Setting batch working hours for authenticated recruiter");
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

        /**
         * JWT-based conflict check - recruiterId extracted from token.
         * Recommended for frontend use - no need to pass recruiterId in body.
         */
        @PostMapping("/recruiter/check-conflict")
        @PreAuthorize("hasRole('RECRUITER')")
        @Operation(summary = "Check scheduling conflict (JWT)", 
                   description = "Check if proposed interview time would create any conflicts. RecruiterId is extracted from JWT token.")
        public ResponseEntity<ConflictCheckResponse> checkConflictFromToken(
                        @Valid @RequestBody ConflictCheckRequestSimple request) {

                Recruiter recruiter = getMyRecruiter();
                log.info("REST: Checking conflict for recruiter {} (from JWT) at {}",
                                recruiter.getId(), request.getProposedStartTime());

                ConflictCheckResponse response = calendarService.checkConflict(
                                recruiter.getId(),
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

        // =====================================================
        // JWT-Based Endpoints (ID extracted from JWT token)
        // =====================================================

        @GetMapping("/recruiter/available-slots")
        @PreAuthorize("hasRole('RECRUITER')")
        @Operation(summary = "Get available time slots", 
                   description = "Get available time slots for the authenticated recruiter (ID from JWT)")
        public ResponseEntity<AvailableSlotsResponse> getAvailableSlotsFromToken(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam @Parameter(description = "Interview duration in minutes", example = "60") Integer durationMinutes) {

                Recruiter recruiter = getMyRecruiter();
                log.info("REST: Getting available slots for recruiter {} on {} (from JWT)", recruiter.getId(), date);

                List<LocalTime> slots = calendarService.getAvailableSlots(recruiter.getId(), date, durationMinutes);

                return ResponseEntity.ok(AvailableSlotsResponse.builder()
                                .recruiterId(recruiter.getId())
                                .date(date)
                                .durationMinutes(durationMinutes)
                                .availableSlots(slots)
                                .totalSlotsAvailable(slots.size())
                                .build());
        }

        @GetMapping("/recruiter/daily")
        @PreAuthorize("hasRole('RECRUITER')")
        @Operation(summary = "Get daily calendar", 
                   description = "Get daily calendar for authenticated recruiter (ID from JWT)")
        public ResponseEntity<DailyCalendarResponse> getDailyCalendarFromToken(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

                Recruiter recruiter = getMyRecruiter();
                log.info("REST: Getting daily calendar for recruiter {} on {} (from JWT)", recruiter.getId(), date);
                
                return ResponseEntity.ok(calendarService.getDailyCalendar(recruiter.getId(), date));
        }

        @GetMapping("/recruiter/weekly")
        @PreAuthorize("hasRole('RECRUITER')")
        @Operation(summary = "Get weekly calendar", 
                   description = "Get weekly calendar for authenticated recruiter (ID from JWT)")
        public ResponseEntity<WeeklyCalendarResponse> getWeeklyCalendarFromToken(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {

                Recruiter recruiter = getMyRecruiter();
                log.info("REST: Getting weekly calendar for recruiter {} starting {} (from JWT)", recruiter.getId(), weekStartDate);
                
                return ResponseEntity.ok(calendarService.getWeeklyCalendar(recruiter.getId(), weekStartDate));
        }

        @GetMapping("/recruiter/monthly")
        @PreAuthorize("hasRole('RECRUITER')")
        @Operation(summary = "Get monthly calendar", 
                   description = "Get monthly calendar for authenticated recruiter (ID from JWT)")
        public ResponseEntity<MonthlyCalendarResponse> getMonthlyCalendarFromToken(
                        @RequestParam Integer year,
                        @RequestParam Integer month) {

                Recruiter recruiter = getMyRecruiter();
                log.info("REST: Getting monthly calendar for recruiter {} for {}-{} (from JWT)", recruiter.getId(), year, month);
                
                return ResponseEntity.ok(calendarService.getMonthlyCalendar(recruiter.getId(), year, month));
        }

        @GetMapping("/recruiter/available-dates")
        @PreAuthorize("hasRole('RECRUITER')")
        @Operation(summary = "Get available dates", 
                   description = "Get available dates for authenticated recruiter (ID from JWT)")
        public ResponseEntity<AvailableDatesResponse> getAvailableDatesFromToken(
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam Integer durationMinutes) {

                Recruiter recruiter = getMyRecruiter();
                log.info("REST: Getting available dates for recruiter {} from {} to {} (from JWT)", 
                        recruiter.getId(), startDate, endDate);

                List<LocalDate> dates = calendarService.getAvailableDates(recruiter.getId(), startDate, endDate, durationMinutes);

                return ResponseEntity.ok(AvailableDatesResponse.builder()
                                .recruiterId(recruiter.getId())
                                .startDate(startDate)
                                .endDate(endDate)
                                .durationMinutes(durationMinutes)
                                .availableDates(dates)
                                .totalDatesAvailable(dates.size())
                                .build());
        }
}
