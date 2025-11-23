package com.fpt.careermate.services.order_services.service.mapper;

import com.fpt.careermate.services.order_services.domain.CandidatePackage;
import com.fpt.careermate.services.order_services.domain.RecruiterPackage;
import com.fpt.careermate.services.order_services.service.dto.request.PackageCreationRequest;
import com.fpt.careermate.services.order_services.service.dto.response.PackageResponse;
import com.fpt.careermate.services.order_services.service.dto.response.EntitlementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PackageMapper {
    @Mapping(target = "candidateInvoices", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "candidateEntitlementPackages", ignore = true)
    @Mapping(target = "priority", ignore = true)
    CandidatePackage toPackage(PackageCreationRequest request);

    @Mapping(target = "entitlements", expression = "java(mapCandidateEntitlements(pkg))")
    PackageResponse toPackageResponse(CandidatePackage pkg);

    @Mapping(target = "entitlements", expression = "java(mapRecruiterEntitlements(pkg))")
    PackageResponse toRecruiterPackageResponse(RecruiterPackage pkg);

    List<PackageResponse> toCandidatePackageResponseList(List<CandidatePackage> candidatePackages);
    List<PackageResponse> toRecruiterPackageResponseList(List<RecruiterPackage> recruiterPackages);

    default List<EntitlementResponse> mapCandidateEntitlements(CandidatePackage pkg) {
        if (pkg.getCandidateEntitlementPackages() == null) {
            return List.of();
        }
        return pkg.getCandidateEntitlementPackages().stream()
                .map(entPkg -> EntitlementResponse.builder()
                        .name(entPkg.getCandidateEntitlement().getName())
                        .code(entPkg.getCandidateEntitlement().getCode())
                        .unit(entPkg.getCandidateEntitlement().getUnit())
                        .hasLimit(entPkg.getCandidateEntitlement().isHasLimit())
                        .enabled(entPkg.isEnabled())
                        .limitValue(entPkg.getLimitValue())
                        .build())
                .toList();
    }

    default List<EntitlementResponse> mapRecruiterEntitlements(RecruiterPackage pkg) {
        if (pkg.getRecruiterEntitlementPackages() == null) {
            return List.of();
        }
        return pkg.getRecruiterEntitlementPackages().stream()
                .map(entPkg -> EntitlementResponse.builder()
                        .name(entPkg.getRecruiterEntitlement().getName())
                        .code(entPkg.getRecruiterEntitlement().getCode())
                        .unit(entPkg.getRecruiterEntitlement().getUnit())
                        .hasLimit(entPkg.getRecruiterEntitlement().isHasLimit())
                        .enabled(entPkg.isEnabled())
                        .limitValue(entPkg.getLimitValue())
                        .build())
                .toList();
    }
}
