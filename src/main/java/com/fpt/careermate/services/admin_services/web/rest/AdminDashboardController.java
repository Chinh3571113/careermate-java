package com.fpt.careermate.services.admin_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.admin_services.service.AdminDashboardService;
import com.fpt.careermate.services.admin_services.service.dto.response.DashboardStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "Simple admin dashboard APIs - one endpoint for everything")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    @Operation(summary = "Get all dashboard statistics", description = "Single endpoint that returns ALL admin dashboard data including user counts, content stats, moderation stats, and system health. "
            +
            "This is the ONLY endpoint the frontend needs to call for the entire dashboard.")
    public ApiResponse<DashboardStatsResponse> getAllDashboardStats() {
        log.info("Admin requesting complete dashboard statistics");

        DashboardStatsResponse stats = adminDashboardService.getAllDashboardStats();

        return ApiResponse.<DashboardStatsResponse>builder()
                .code(200)
                .message("Dashboard statistics retrieved successfully")
                .result(stats)
                .build();
    }
}
