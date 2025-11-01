package com.fpt.careermate.services.recruiter_services.repository;

import com.fpt.careermate.services.recruiter_services.domain.Recruiter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruiterRepo extends JpaRepository<Recruiter,Integer> {
    Optional<Recruiter> findByAccount_Id(int id);

    // Find recruiters by account status with pagination
    Page<Recruiter> findByAccount_Status(String status, Pageable pageable);

    // Find all recruiters with pagination
    Page<Recruiter> findAll(Pageable pageable);

    // Search recruiters by company name, email, or username with status filter
    @Query("SELECT r FROM Recruiter r WHERE " +
           "(:status IS NULL OR :status = '' OR r.account.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.account.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.account.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Recruiter> searchRecruiters(@Param("status") String status,
                                     @Param("search") String search,
                                     @NonNull Pageable pageable);
}
