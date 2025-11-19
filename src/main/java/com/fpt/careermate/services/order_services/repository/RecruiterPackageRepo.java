package com.fpt.careermate.services.order_services.repository;

import com.fpt.careermate.services.order_services.domain.RecruiterPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecruiterPackageRepo extends JpaRepository<RecruiterPackage, Integer> {
    RecruiterPackage findByName(String name);
}

