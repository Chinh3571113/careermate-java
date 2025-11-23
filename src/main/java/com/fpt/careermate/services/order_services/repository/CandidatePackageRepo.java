package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidatePackageRepo extends JpaRepository<CandidatePackage,Integer> {
    CandidatePackage findByName(String name);

    @Query("SELECT DISTINCT cp FROM candidate_package cp " +
           "LEFT JOIN FETCH cp.candidateEntitlementPackages cep " +
           "LEFT JOIN FETCH cep.candidateEntitlement")
    List<CandidatePackage> findAllWithEntitlements();
}
