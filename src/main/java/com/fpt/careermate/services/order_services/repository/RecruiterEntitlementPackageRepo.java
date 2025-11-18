package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.RecruiterEntitlementPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruiterEntitlementPackageRepo extends JpaRepository<RecruiterEntitlementPackage, Integer> {
    RecruiterEntitlementPackage findByRecruiterPackage_NameAndRecruiterEntitlement_Code(String packageName, String entitlementCode);
}
