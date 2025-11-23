package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.order_services.service.AdminInvoiceImp;
import com.fpt.careermate.services.order_services.service.dto.response.PageInvoiceListResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@Tag(name = "Admin - Invoice", description = "Manage invoice")
@RequestMapping("/admin/invoices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminInvoiceController {

    AdminInvoiceImp adminInvoiceService;

    /**
     * Admin lấy danh sách toàn bộ invoice của recruiter có filter
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recruiters")
    public ApiResponse<PageInvoiceListResponse> getAllRecruiterInvoices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        log.info("Admin getting recruiter invoices - status: {}, isActive: {}, page: {}, size: {}",
                status, isActive, page, size);

        PageInvoiceListResponse invoices = adminInvoiceService.getAllRecruiterInvoices(
                status, isActive, page, size
        );

        return ApiResponse.<PageInvoiceListResponse>builder()
                .code(200)
                .message("Get recruiter invoices successfully")
                .result(invoices)
                .build();
    }

    /**
     * Admin lấy danh sách toàn bộ invoice của candidate có filter
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/candidates")
    public ApiResponse<PageInvoiceListResponse> getAllCandidateInvoices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        PageInvoiceListResponse invoices = adminInvoiceService.getAllCandidateInvoices(
                status, isActive, page, size
        );

        return ApiResponse.<PageInvoiceListResponse>builder()
                .code(200)
                .message("Get candidate invoices successfully")
                .result(invoices)
                .build();
    }
}
