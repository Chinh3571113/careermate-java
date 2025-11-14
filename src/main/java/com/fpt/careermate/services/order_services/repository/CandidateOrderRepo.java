package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CandidateOrderRepo extends JpaRepository<Invoice,Integer> {
    Optional<Invoice> findByCandidate_CandidateIdAndIsActiveTrue(int candidateId);

    @Override
    Page<Invoice> findAll(Pageable pageable);
}
