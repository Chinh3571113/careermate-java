package com.fpt.careermate.web.rest;

import com.fpt.careermate.services.CertificateImp;
import com.fpt.careermate.services.dto.request.CertificateRequest;
import com.fpt.careermate.services.dto.response.ApiResponse;
import com.fpt.careermate.services.dto.response.CertificateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificate")
@Tag(name = "Certificate", description = "Certificate API")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CertificateController {
    CertificateImp certificateImp;
    @PostMapping
    @Operation(summary = "Add Certificate", description = "Add certificate to resume")
    public ApiResponse<CertificateResponse> addCertificate(@RequestBody @Valid CertificateRequest certificateRequest) {
        return ApiResponse.<CertificateResponse>builder()
                .result(certificateImp.addCertificationToResume(certificateRequest))
                .message("Add certificate successfully")
                .build();
    }

    @PutMapping("/{resumeId}/{certificateId}")
    @Operation(summary = "Update Certificate", description = "Update certificate in resume")
    public ApiResponse<CertificateResponse> updateCertificate(@PathVariable int resumeId,
                                                              @PathVariable int certificateId,
                                                              @RequestBody @Valid CertificateRequest certificateRequest) {
        return ApiResponse.<CertificateResponse>builder()
                .result(certificateImp.updateCertificationInResume(resumeId,certificateId, certificateRequest))
                .message("Update certificate successfully")
                .build();
    }

    @DeleteMapping("/{certificateId}")
    @Operation(summary = "Remove Certificate", description = "Remove certificate from resume")
    public ApiResponse<Void> removeCertificate(@PathVariable int certificateId) {
        certificateImp.removeCertificationFromResume(certificateId);
        return ApiResponse.<Void>builder()
                .message("Remove certificate successfully")
                .build();
    }

}
