package com.fpt.careermate.services.admin_services.repository;

import com.fpt.careermate.services.admin_services.domain.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepo extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByAccount_Id(int accountId);
    Optional<Admin> findByAccount_Email(String email);
}

