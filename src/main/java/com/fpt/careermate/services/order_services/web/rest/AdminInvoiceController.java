package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.order_services.service.AdminInvoiceImp;
import com.fpt.careermate.services.order_services.service.dto.response.AdminRecruiterInvoiceResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/recruiters")
    public ApiResponse<Page<AdminRecruiterInvoiceResponse>> getAllRecruiterInvoices(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String companyName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<AdminRecruiterInvoiceResponse> invoices = adminInvoiceService.getAllRecruiterInvoices(
                status, isActive, companyName, page, size
        );

        return ApiResponse.<Page<AdminRecruiterInvoiceResponse>>builder()
                .code(200)
                .message("Get recruiter invoices successfully")
                .result(invoices)
                .build();
    }
}

