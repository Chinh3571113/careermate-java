package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.RecruiterPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecruiterPackageRepo extends JpaRepository<RecruiterPackage, Integer> {
    RecruiterPackage findByName(String name);

    @Query("SELECT DISTINCT rp FROM recruiter_package rp " +
           "LEFT JOIN FETCH rp.recruiterEntitlementPackages rep " +
           "LEFT JOIN FETCH rep.recruiterEntitlement")
    List<RecruiterPackage> findAllWithEntitlements();
}
