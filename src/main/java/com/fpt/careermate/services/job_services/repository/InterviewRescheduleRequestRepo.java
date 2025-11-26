package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.InterviewRescheduleRequest;
import com.fpt.careermate.services.job_services.domain.InterviewRescheduleRequest.RescheduleStatus;
import com.fpt.careermate.services.job_services.domain.InterviewSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for InterviewRescheduleRequest entity
 *
 * @since 1.0
 */
@Repository
public interface InterviewRescheduleRequestRepo extends JpaRepository<InterviewRescheduleRequest, Long> {

    /**
     * Find all reschedule requests for an interview
     */
    List<InterviewRescheduleRequest> findByInterviewScheduleOrderByCreatedAtDesc(InterviewSchedule interviewSchedule);

    /**
     * Find reschedule requests by interview ID
     */
    @Query("SELECT r FROM interview_reschedule_request r WHERE r.interviewSchedule.id = :interviewId ORDER BY r.createdAt DESC")
    List<InterviewRescheduleRequest> findByInterviewScheduleId(@Param("interviewId") Long interviewId);

    /**
     * Find pending reschedule requests
     */
    List<InterviewRescheduleRequest> findByStatus(RescheduleStatus status);

    /**
     * Find expired reschedule requests that need status update
     */
    @Query("SELECT r FROM interview_reschedule_request r " +
           "WHERE r.status = 'PENDING_CONSENT' " +
           "AND r.expiresAt < :now")
    List<InterviewRescheduleRequest> findExpiredPendingRequests(@Param("now") LocalDateTime now);

    /**
     * Find pending requests for a specific interview
     */
    @Query("SELECT r FROM interview_reschedule_request r " +
           "WHERE r.interviewSchedule.id = :interviewId " +
           "AND r.status = 'PENDING_CONSENT'")
    List<InterviewRescheduleRequest> findPendingByInterviewId(@Param("interviewId") Long interviewId);

    /**
     * Count reschedule requests by interview
     */
    Long countByInterviewSchedule(InterviewSchedule interviewSchedule);

    /**
     * Find reschedule requests by candidate (through interview schedule)
     */
    @Query("SELECT r FROM interview_reschedule_request r " +
           "WHERE r.interviewSchedule.jobApply.candidate.id = :candidateId " +
           "ORDER BY r.createdAt DESC")
    List<InterviewRescheduleRequest> findByCandidateId(@Param("candidateId") Long candidateId);

    /**
     * Find reschedule requests by recruiter (through interview schedule)
     */
    @Query("SELECT r FROM interview_reschedule_request r " +
           "WHERE r.interviewSchedule.createdByRecruiter.id = :recruiterId " +
           "ORDER BY r.createdAt DESC")
    List<InterviewRescheduleRequest> findByRecruiterId(@Param("recruiterId") Long recruiterId);
}
