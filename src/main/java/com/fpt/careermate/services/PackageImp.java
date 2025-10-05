package com.fpt.careermate.services;

import com.fpt.careermate.repository.PackageRepo;
import com.fpt.careermate.services.dto.request.PackageCreationRequest;
import com.fpt.careermate.services.dto.response.PackageResponse;
import com.fpt.careermate.services.impl.PackageService;
import com.fpt.careermate.services.mapper.PackageMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PackageImp implements PackageService {

    PackageRepo packageRepo;
    PackageMapper packageMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public PackageResponse createPackage(PackageCreationRequest request) {
        return packageMapper.toPackageResponse(packageRepo.save(packageMapper.toPackage(request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public List<PackageResponse> getPackageList() {
        return packageMapper.toPackageResponseList(packageRepo.findAll());
    }

}
