package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.order_services.service.dto.request.PackageCreationRequest;
import com.fpt.careermate.services.order_services.service.dto.response.PackageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PackageMapper {
    @Mapping(target = "invoices", ignore = true)
    CandidatePackage toPackage(PackageCreationRequest request);

    PackageResponse toPackageResponse(CandidatePackage pkg);

    List<PackageResponse> toPackageResponseList(List<CandidatePackage> candidatePackages);
}
