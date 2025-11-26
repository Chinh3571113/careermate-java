package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.RecruiterTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for RecruiterTimeOff
 * 
 * @since 1.1 - Calendar Feature
 */
@Repository
public interface RecruiterTimeOffRepo extends JpaRepository<RecruiterTimeOff, Integer> {
    
    /**
     * Find all time-off periods for a recruiter
     */
    @Query("SELECT t FROM recruiter_time_off t WHERE t.recruiter.id = :recruiterId ORDER BY t.startDate DESC")
    List<RecruiterTimeOff> findByRecruiterId(@Param("recruiterId") Integer recruiterId);
    
    /**
     * Find approved time-off periods for a recruiter
     */
    @Query("SELECT t FROM recruiter_time_off t WHERE t.recruiter.id = :recruiterId AND t.isApproved = true ORDER BY t.startDate")
    List<RecruiterTimeOff> findApprovedTimeOffByRecruiterId(@Param("recruiterId") Integer recruiterId);
    
    /**
     * Find time-off periods overlapping with a date range
     */
    @Query("SELECT t FROM recruiter_time_off t WHERE t.recruiter.id = :recruiterId " +
           "AND t.isApproved = true " +
           "AND t.startDate <= :endDate " +
           "AND t.endDate >= :startDate")
    List<RecruiterTimeOff> findTimeOffInRange(@Param("recruiterId") Integer recruiterId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * Check if recruiter has time-off on specific date
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM recruiter_time_off t " +
           "WHERE t.recruiter.id = :recruiterId " +
           "AND t.isApproved = true " +
           "AND :date BETWEEN t.startDate AND t.endDate")
    boolean hasTimeOffOnDate(@Param("recruiterId") Integer recruiterId, @Param("date") LocalDate date);
    
    /**
     * Find pending time-off requests (not yet approved)
     */
    @Query("SELECT t FROM recruiter_time_off t WHERE t.isApproved = false ORDER BY t.createdAt")
    List<RecruiterTimeOff> findPendingTimeOffRequests();
}
