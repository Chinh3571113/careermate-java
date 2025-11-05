package com.fpt.careermate.services.recruiter_services.repository;

import com.fpt.careermate.services.recruiter_services.domain.RecruiterProfileUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruiterProfileUpdateRequestRepo extends JpaRepository<RecruiterProfileUpdateRequest, Integer> {

    // Find by status
    List<RecruiterProfileUpdateRequest> findByStatus(String status);
    Page<RecruiterProfileUpdateRequest> findByStatus(String status, Pageable pageable);

    // Find by recruiter
    List<RecruiterProfileUpdateRequest> findByRecruiterId(int recruiterId);
    Optional<RecruiterProfileUpdateRequest> findByRecruiterIdAndStatus(int recruiterId, String status);

    // Check if recruiter has pending request
    boolean existsByRecruiterIdAndStatus(int recruiterId, String status);

    // Search with filters
    @Query("SELECT r FROM RecruiterProfileUpdateRequest r WHERE " +
           "(:status IS NULL OR :status = '' OR r.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(r.newCompanyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.newCompanyEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.recruiter.account.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<RecruiterProfileUpdateRequest> searchUpdateRequests(
        @Param("status") String status,
        @Param("search") String search,
        Pageable pageable
    );
}

