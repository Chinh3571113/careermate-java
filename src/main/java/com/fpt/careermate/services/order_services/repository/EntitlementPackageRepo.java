package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.EntitlementPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EntitlementPackageRepo extends JpaRepository<EntitlementPackage,Integer> {
    EntitlementPackage findByCandidatePackage_NameAndEntitlement_Code(String packageName, String entitlementCode);
}
