package com.fpt.careermate.services;

import com.fpt.careermate.domain.Certificate;
import com.fpt.careermate.domain.Resume;
import com.fpt.careermate.repository.CertificateRepo;
import com.fpt.careermate.repository.ResumeRepo;
import com.fpt.careermate.services.dto.request.CertificateRequest;
import com.fpt.careermate.services.dto.response.CertificateResponse;
import com.fpt.careermate.services.impl.CertificateService;
import com.fpt.careermate.services.mapper.CertificateMapper;
import com.fpt.careermate.web.exception.AppException;
import com.fpt.careermate.web.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    @Override
    public CertificateResponse addCertificationToResume(CertificateRequest certification) {
        Resume resume = resumeImp.generateResume();

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
    public void removeCertificationFromResume(int certificationId) {
        certificateRepo.findById(certificationId)
                .orElseThrow(() -> new AppException(ErrorCode.CERTIFICATE_NOT_FOUND));
        certificateRepo.deleteById(certificationId);
    }

    @Transactional
    @Override
    public CertificateResponse updateCertificationInResume(int resumeId, int certificationId, CertificateRequest certification) {
        resumeRepo.findById(resumeId).orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
        Certificate existingCertificate = certificateRepo.findById(certificationId)
                .orElseThrow(() -> new AppException(ErrorCode.CERTIFICATE_NOT_FOUND));

        certificateMapper.updateEntity(certification, existingCertificate);

        return certificateMapper.toResponse(certificateRepo.save(existingCertificate));
    }
}
