package com.fpt.careermate.services.impl;

import com.fpt.careermate.services.dto.request.CertificateRequest;
import com.fpt.careermate.services.dto.response.CertificateResponse;

public interface CertificateService {
    CertificateResponse addCertificationToResume(CertificateRequest certification);
    void removeCertificationFromResume(int certificationId);
    CertificateResponse updateCertificationInResume(int resumeId, int certificationId, CertificateRequest certification);

}
