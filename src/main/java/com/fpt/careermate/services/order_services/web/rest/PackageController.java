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

@Tag(name = "Package", description = "Manage package for candidate and recruiter")
@RestController
@RequestMapping("/api/package")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PackageController {

    PackageImp packageImp;

    @Operation(summary = "Get package list for candidate")
    @GetMapping("/candidate")
    public ApiResponse<List<PackageResponse>> getCandidatePackageList() {
        return ApiResponse.<List<PackageResponse>>builder()
                .result(packageImp.getCandidatePackageList())
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get package list for recruiter")
    @GetMapping("/recruiter")
    public ApiResponse<List<PackageResponse>> getRecruiterPackageList() {
        return ApiResponse.<List<PackageResponse>>builder()
                .result(packageImp.getRecruiterPackageList())
                .code(200)
                .message("success")
                .build();
    }

}
