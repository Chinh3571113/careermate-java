package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.services.order_services.service.PackageImp;
import com.fpt.careermate.services.order_services.service.dto.request.PackageCreationRequest;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PackageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CandidatePackage", description = "Manage package")
@RestController
@RequestMapping("/api/package")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PackageController {

    PackageImp packageImp;

    @Operation(summary = "Create package")
    @PostMapping
    public ApiResponse<PackageResponse> createPackage(@Valid @RequestBody PackageCreationRequest request) {
        return ApiResponse.<PackageResponse>builder()
                .result(packageImp.createPackage(request))
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get package list for admin")
    @GetMapping
    public ApiResponse<List<PackageResponse>> getPackageList() {
        return ApiResponse.<List<PackageResponse>>builder()
                .result(packageImp.getPackageList())
                .code(200)
                .message("success")
                .build();
    }

}
