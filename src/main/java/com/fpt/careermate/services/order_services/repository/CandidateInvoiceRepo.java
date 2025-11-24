package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.CandidateInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CandidateInvoiceRepo extends JpaRepository<CandidateInvoice,Integer> {
    Optional<CandidateInvoice> findByCandidate_CandidateIdAndIsActiveTrue(int candidateId);

    @Override
    Page<CandidateInvoice> findAll(Pageable pageable);

    @Query("SELECT ci FROM candidate_invoice ci " +
           "JOIN FETCH ci.candidate c " +
           "JOIN FETCH c.account a " +
           "JOIN FETCH ci.candidatePackage " +
           "WHERE (:status IS NULL OR ci.status = :status) " +
           "AND (:isActive IS NULL OR ci.isActive = :isActive) " +
           "ORDER BY ci.id DESC")
    Page<CandidateInvoice> findAllWithFilters(
            @Param("status") String status,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
