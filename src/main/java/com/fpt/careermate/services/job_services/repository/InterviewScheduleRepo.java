package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.common.constant.InterviewStatus;
import com.fpt.careermate.services.job_services.domain.InterviewSchedule;
import com.fpt.careermate.services.job_services.domain.JobApply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for InterviewSchedule entity
 *
 * @since 1.0
 */
@Repository
public interface InterviewScheduleRepo extends JpaRepository<InterviewSchedule, Integer> {

    /**
     * Find interview by job apply
     */
    Optional<InterviewSchedule> findByJobApply(JobApply jobApply);

    /**
     * Find interview by job apply ID
     */
    @Query("SELECT i FROM interview_schedule i WHERE i.jobApply.id = :jobApplyId")
    Optional<InterviewSchedule> findByJobApplyId(@Param("jobApplyId") Integer jobApplyId);
    
    /**
     * Check if interview exists for job apply
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM interview_schedule i WHERE i.jobApply.id = :jobApplyId")
    boolean existsByJobApplyId(@Param("jobApplyId") Integer jobApplyId);

    /**
     * Find upcoming interviews within date range
     */
    @Query("SELECT i FROM interview_schedule i WHERE i.scheduledDate BETWEEN :startDate AND :endDate ORDER BY i.scheduledDate ASC")
    List<InterviewSchedule> findUpcomingInterviews(@Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);

    /**
     * Find interviews by status and date range
     */
    List<InterviewSchedule> findByStatusAndScheduledDateBetween(InterviewStatus status, 
                                                                LocalDateTime startDate, 
                                                                LocalDateTime endDate);

    /**
     * Find expired unconfirmed interviews (scheduled but not confirmed and time passed)
     */
    @Query("SELECT i FROM interview_schedule i WHERE i.status = 'SCHEDULED' AND i.candidateConfirmed = false AND i.scheduledDate < :now")
    List<InterviewSchedule> findExpiredUnconfirmed(@Param("now") LocalDateTime now);

    /**
     * Find interviews needing 24h reminder (24-25 hours away, not sent yet)
     */
    @Query("SELECT i FROM interview_schedule i WHERE i.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND i.reminderSent24h = false " +
           "AND i.scheduledDate BETWEEN :start AND :end")
    List<InterviewSchedule> findNeedingReminder24h(@Param("start") LocalDateTime start, 
                                                    @Param("end") LocalDateTime end);

    /**
     * Find interviews needing 2h reminder (2-3 hours away, not sent yet)
     */
    @Query("SELECT i FROM interview_schedule i WHERE i.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND i.reminderSent2h = false " +
           "AND i.scheduledDate BETWEEN :start AND :end")
    List<InterviewSchedule> findNeedingReminder2h(@Param("start") LocalDateTime start, 
                                                   @Param("end") LocalDateTime end);

    /**
     * Find interviews by recruiter
     */
    @Query("SELECT i FROM interview_schedule i WHERE i.createdByRecruiter.id = :recruiterId ORDER BY i.scheduledDate DESC")
    List<InterviewSchedule> findByRecruiterId(@Param("recruiterId") Integer recruiterId);
    
    /**
     * Find recruiter's upcoming interviews
     */
    @Query("SELECT i FROM interview_schedule i " +
           "WHERE i.createdByRecruiter.id = :recruiterId " +
           "AND i.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND i.scheduledDate >= :now " +
           "ORDER BY i.scheduledDate ASC")
    List<InterviewSchedule> findUpcomingInterviewsByRecruiterId(@Param("recruiterId") Integer recruiterId, 
                                                                @Param("now") LocalDateTime now);

    /**
     * Find candidate's upcoming interviews
     */
    @Query("SELECT i FROM interview_schedule i " +
           "WHERE i.jobApply.candidate.id = :candidateId " +
           "AND i.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND i.scheduledDate >= :now " +
           "ORDER BY i.scheduledDate ASC")
    List<InterviewSchedule> findUpcomingInterviewsByCandidateId(@Param("candidateId") Integer candidateId, 
                                                                @Param("now") LocalDateTime now);

    /**
     * Find candidate's past interviews
     */
    @Query("SELECT i FROM interview_schedule i " +
           "WHERE i.jobApply.candidate.id = :candidateId " +
           "AND i.status IN ('COMPLETED', 'NO_SHOW', 'CANCELLED') " +
           "ORDER BY i.scheduledDate DESC")
    List<InterviewSchedule> findPastInterviewsByCandidateId(@Param("candidateId") Integer candidateId, 
                                                            @Param("now") LocalDateTime now);
    
    /**
     * Find interviews needing reminders (generic method)
     */
    @Query("SELECT i FROM interview_schedule i " +
           "WHERE i.status IN ('SCHEDULED', 'CONFIRMED') " +
           "AND i.scheduledDate BETWEEN :startTime AND :endTime " +
           "AND ((:is24h = true AND i.reminderSent24h = false) OR (:is24h = false AND i.reminderSent2h = false))")
    List<InterviewSchedule> findInterviewsNeedingReminder(@Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime,
                                                          @Param("is24h") boolean is24hReminder);

    /**
     * Find interviews by recruiter (through job posting)
     */
    @Query("SELECT i FROM interview_schedule i " +
           "WHERE i.jobApply.jobPosting.recruiter.id = :recruiterId " +
           "ORDER BY i.scheduledDate DESC")
    List<InterviewSchedule> findByRecruiterId(@Param("recruiterId") Long recruiterId);

    /**
     * Count interviews by status for a recruiter
     */
    @Query("SELECT COUNT(i) FROM interview_schedule i " +
           "WHERE i.createdByRecruiter.id = :recruiterId " +
           "AND i.status = :status")
    Long countByRecruiterIdAndStatus(@Param("recruiterId") Integer recruiterId, 
                                     @Param("status") InterviewStatus status);

    /**
     * Find all interviews by status
     */
    List<InterviewSchedule> findByStatus(InterviewStatus status);
    
    // =====================================================
    // CALENDAR FEATURE - Enhanced Queries
    // =====================================================
    
    /**
     * Check if recruiter has conflicting interview at proposed time
     * Conflict exists if there's an overlap with any non-cancelled interview
     * Uses native query for PostgreSQL interval arithmetic
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM interview_schedule i " +
           "WHERE i.created_by_recruiter_id = :recruiterId " +
           "AND i.status NOT IN ('CANCELLED', 'NO_SHOW') " +
           "AND i.scheduled_date < :proposedEndTime " +
           "AND (i.scheduled_date + (i.duration_minutes * INTERVAL '1 minute')) > :proposedStartTime", nativeQuery = true)
    boolean hasConflict(@Param("recruiterId") Integer recruiterId,
                        @Param("proposedStartTime") LocalDateTime proposedStartTime,
                        @Param("proposedEndTime") LocalDateTime proposedEndTime);
    
    /**
     * Find all interviews for recruiter on specific date (for daily calendar view)
     */
    @Query("SELECT i FROM interview_schedule i " +
           "WHERE i.createdByRecruiter.id = :recruiterId " +
           "AND i.status NOT IN ('CANCELLED') " +
           "AND DATE(i.scheduledDate) = :date " +
           "ORDER BY i.scheduledDate ASC")
    List<InterviewSchedule> findByRecruiterIdAndDate(@Param("recruiterId") Integer recruiterId,
                                                      @Param("date") LocalDate date);
    
    /**
     * Find all interviews for recruiter in date range (for weekly/monthly calendar view)
     */
    @Query("SELECT i FROM interview_schedule i " +
           "WHERE i.createdByRecruiter.id = :recruiterId " +
           "AND i.status NOT IN ('CANCELLED') " +
           "AND DATE(i.scheduledDate) BETWEEN :startDate AND :endDate " +
           "ORDER BY i.scheduledDate ASC")
    List<InterviewSchedule> findByRecruiterIdAndDateRange(@Param("recruiterId") Integer recruiterId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);
    
    /**
     * Count active interviews for recruiter on specific date
     */
    @Query("SELECT COUNT(i) FROM interview_schedule i " +
           "WHERE i.createdByRecruiter.id = :recruiterId " +
           "AND i.status NOT IN ('CANCELLED', 'NO_SHOW') " +
           "AND DATE(i.scheduledDate) = :date")
    Long countInterviewsOnDate(@Param("recruiterId") Integer recruiterId, @Param("date") LocalDate date);
    
    /**
     * Find overlapping interviews (for conflict detection with duration)
     * Uses native query for PostgreSQL interval arithmetic
     */
    @Query(value = "SELECT * FROM interview_schedule i " +
           "WHERE i.created_by_recruiter_id = :recruiterId " +
           "AND i.status NOT IN ('CANCELLED', 'NO_SHOW') " +
           "AND i.scheduled_date < :proposedEndTime " +
           "AND (i.scheduled_date + (i.duration_minutes * INTERVAL '1 minute')) > :proposedStartTime " +
           "ORDER BY i.scheduled_date ASC", nativeQuery = true)
    List<InterviewSchedule> findOverlappingInterviews(@Param("recruiterId") Integer recruiterId,
                                                       @Param("proposedStartTime") LocalDateTime proposedStartTime,
                                                       @Param("proposedEndTime") LocalDateTime proposedEndTime);
    
    /**
     * Check if candidate has conflicting interview
     * Uses native query for PostgreSQL interval arithmetic
     */
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM interview_schedule i " +
           "JOIN job_apply ja ON i.job_apply_id = ja.id " +
           "WHERE ja.candidate_id = :candidateId " +
           "AND i.status NOT IN ('CANCELLED', 'NO_SHOW') " +
           "AND i.scheduled_date < :proposedEndTime " +
           "AND (i.scheduled_date + (i.duration_minutes * INTERVAL '1 minute')) > :proposedStartTime", nativeQuery = true)
    boolean candidateHasConflict(@Param("candidateId") Integer candidateId,
                                  @Param("proposedStartTime") LocalDateTime proposedStartTime,
                                  @Param("proposedEndTime") LocalDateTime proposedEndTime);
    
    /**
     * Find candidate's interviews on specific date
     */
    @Query("SELECT i FROM interview_schedule i " +
           "WHERE i.jobApply.candidate.id = :candidateId " +
           "AND i.status NOT IN ('CANCELLED') " +
           "AND DATE(i.scheduledDate) = :date " +
           "ORDER BY i.scheduledDate ASC")
    List<InterviewSchedule> findByCandidateIdAndDate(@Param("candidateId") Integer candidateId,
                                                      @Param("date") LocalDate date);
}

