package com.fpt.careermate.services.health_services.web;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.health_services.service.HealthService;
import com.fpt.careermate.services.health_services.service.dto.HealthStatusDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/health")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Health", description = "System health monitoring for admin dashboard")
@SecurityRequirement(name = "bearerToken")
public class AdminHealthController {

    private final HealthService healthService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get system health status",
        description = "Returns comprehensive health status of all system components including Kafka, Weaviate, Email, Database, and notification workers. Only accessible by admin users."
    )
    public ApiResponse<HealthStatusDTO> getSystemHealth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("🏥 Health check requested by: {} | Authorities: {}", 
            auth != null ? auth.getName() : "anonymous", 
            auth != null ? auth.getAuthorities() : "none");
        
        HealthStatusDTO health = healthService.getAggregatedHealth();
        log.info("✅ Health check completed. Overall status: {}", health.overallStatus());
        
        return ApiResponse.<HealthStatusDTO>builder()
                .code(1000)
                .result(health)
                .build();
    }
}
