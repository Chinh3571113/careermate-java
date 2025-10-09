package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepo extends JpaRepository<Certificate, Integer> {
}
