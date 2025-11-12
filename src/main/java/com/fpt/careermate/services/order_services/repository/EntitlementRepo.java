package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.Entitlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface EntitlementRepo extends JpaRepository<Entitlement,Integer> {
    Entitlement findByCode(String code);
}
