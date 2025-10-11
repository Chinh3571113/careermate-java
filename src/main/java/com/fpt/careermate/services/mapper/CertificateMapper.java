package com.fpt.careermate.services.mapper;

import com.fpt.careermate.domain.Certificate;
import com.fpt.careermate.services.dto.request.CertificateRequest;
import com.fpt.careermate.services.dto.response.CertificateResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CertificateMapper {
    @Mapping(target = "certificateId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    Certificate toEntity(CertificateRequest request);

    CertificateResponse toResponse(Certificate certificate);

    @Mapping(target = "certificateId", ignore = true)
    @Mapping(target = "resume", ignore = true)
    void updateEntity(CertificateRequest request, @MappingTarget Certificate certificate);
}

