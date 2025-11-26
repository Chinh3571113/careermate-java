package com.fpt.careermate.services.job_services.repository;

import com.fpt.careermate.services.job_services.domain.RecruiterWorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RecruiterWorkingHours
 * 
 * @since 1.1 - Calendar Feature
 */
@Repository
public interface RecruiterWorkingHoursRepo extends JpaRepository<RecruiterWorkingHours, Integer> {
    
    /**
     * Find all working hours configuration for a recruiter
     */
    @Query("SELECT w FROM recruiter_working_hours w WHERE w.recruiter.id = :recruiterId ORDER BY w.dayOfWeek")
    List<RecruiterWorkingHours> findByRecruiterId(@Param("recruiterId") Integer recruiterId);
    
    /**
     * Find working hours for specific day of week
     */
    @Query("SELECT w FROM recruiter_working_hours w WHERE w.recruiter.id = :recruiterId AND w.dayOfWeek = :dayOfWeek")
    Optional<RecruiterWorkingHours> findByRecruiterIdAndDayOfWeek(@Param("recruiterId") Integer recruiterId, 
                                                                    @Param("dayOfWeek") DayOfWeek dayOfWeek);
    
    /**
     * Find working days only
     */
    @Query("SELECT w FROM recruiter_working_hours w WHERE w.recruiter.id = :recruiterId AND w.isWorkingDay = true ORDER BY w.dayOfWeek")
    List<RecruiterWorkingHours> findWorkingDaysByRecruiterId(@Param("recruiterId") Integer recruiterId);
    
    /**
     * Check if recruiter has working hours configured
     */
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM recruiter_working_hours w WHERE w.recruiter.id = :recruiterId")
    boolean existsByRecruiterId(@Param("recruiterId") Integer recruiterId);
}
