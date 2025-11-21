package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.Invoice;
import com.fpt.careermate.services.order_services.domain.RecruiterInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RecruiterInvoiceRepo extends JpaRepository<RecruiterInvoice,Integer> {
    Optional<RecruiterInvoice> findByRecruiter_IdAndIsActiveTrue(int recruiterId);

    @Override
    Page<RecruiterInvoice> findAll(Pageable pageable);
}
