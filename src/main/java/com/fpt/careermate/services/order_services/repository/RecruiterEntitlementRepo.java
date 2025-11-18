package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.RecruiterEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruiterEntitlementRepo extends JpaRepository<RecruiterEntitlement, Integer> {
    RecruiterEntitlement findByCode(String code);
}

