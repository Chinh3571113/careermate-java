package com.fpt.careermate.repository;

import com.fpt.careermate.domain.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CertificateRepo extends JpaRepository<Certificate, Integer> {
    @Query("SELECT COUNT(c) FROM certificate c WHERE c.resume.resumeId = :resumeId")
    long countCertificateByResumeId(int resumeId);
}
