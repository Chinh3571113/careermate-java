package com.fpt.careermate.services.order_services.service.impl;


import com.fpt.careermate.services.order_services.service.dto.response.PackageResponse;

import java.util.List;

public interface PackageService {
    List<PackageResponse> getCandidatePackageList();
    List<PackageResponse> getRecruiterPackageList();
}
