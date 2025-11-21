package com.fpt.careermate.services.resume_services.service;

import com.fpt.careermate.services.resume_services.domain.Certificate;
import com.fpt.careermate.services.resume_services.domain.Resume;
import com.fpt.careermate.services.resume_services.repository.CertificateRepo;
import com.fpt.careermate.services.resume_services.repository.ResumeRepo;
import com.fpt.careermate.services.resume_services.service.dto.request.CertificateRequest;
import com.fpt.careermate.services.resume_services.service.dto.response.CertificateResponse;
import com.fpt.careermate.services.resume_services.service.impl.CertificateService;
import com.fpt.careermate.services.resume_services.service.mapper.CertificateMapper;
import com.fpt.careermate.common.exception.AppException;
import com.fpt.careermate.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CertificateImp implements CertificateService {
    CertificateRepo certificateRepo;
    ResumeImp resumeImp;
    CertificateMapper certificateMapper;
    ResumeRepo resumeRepo;

    @Transactional
    @PreAuthorize("hasRole('CANDIDATE')")
    @Override
    public CertificateResponse addCertificationToResume(CertificateRequest certification) {
        Resume resume = resumeImp.getResumeEntityById(certification.getResumeId());

        if (certificateRepo.countCertificateByResumeId(resume.getResumeId()) >= 3) {
            throw new AppException(ErrorCode.OVERLOAD);
        }
        Certificate certificateInfo = certificateMapper.toEntity(certification);
        certificateInfo.setResume(resume);
        resume.getCertificates().add(certificateInfo);

        Certificate savedCertificate = certificateRepo.save(certificateInfo);

        return certificateMapper.toResponse(savedCertificate);
    }

    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public void removeCertificationFromResume(int certificationId) {
        certificateRepo.findById(certificationId)
                .orElseThrow(() -> new AppException(ErrorCode.CERTIFICATE_NOT_FOUND));
        certificateRepo.deleteById(certificationId);
    }

    @Transactional
    @Override
    @PreAuthorize("hasRole('CANDIDATE')")
    public CertificateResponse updateCertificationInResume(int resumeId, int certificationId, CertificateRequest certification) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        Certificate existingCertificate = certificateRepo.findById(certificationId)
                .orElseThrow(() -> new AppException(ErrorCode.CERTIFICATE_NOT_FOUND));

        certificateMapper.updateEntity(certification, existingCertificate);

        return certificateMapper.toResponse(certificateRepo.save(existingCertificate));
    }
}
