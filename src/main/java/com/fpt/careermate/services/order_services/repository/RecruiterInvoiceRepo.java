package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.RecruiterInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RecruiterInvoiceRepo extends JpaRepository<RecruiterInvoice,Integer> {
    Optional<RecruiterInvoice> findByRecruiter_IdAndIsActiveTrue(int recruiterId);

    @Override
    Page<RecruiterInvoice> findAll(Pageable pageable);

    @Query("SELECT ri FROM recruiter_invoice ri " +
           "JOIN FETCH ri.recruiter r " +
           "JOIN FETCH r.account a " +
           "JOIN FETCH ri.recruiterPackage " +
           "WHERE (:status IS NULL OR ri.status = :status) " +
           "AND (:isActive IS NULL OR ri.isActive = :isActive) " +
           "AND (:companyName IS NULL OR LOWER(r.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))) " +
           "ORDER BY ri.id DESC")
    Page<RecruiterInvoice> findAllWithFilters(
            @Param("status") String status,
            @Param("isActive") Boolean isActive,
            @Param("companyName") String companyName,
            Pageable pageable
    );
}
