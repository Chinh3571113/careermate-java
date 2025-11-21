package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.order_services.service.RecruiterInvoiceImp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Tag(name = "Recruiter - Invoice", description = "Manage recruiter invoice")
@RestController
@RequestMapping("/api/recruiter-invoice")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterInvoiceController {

    RecruiterInvoiceImp recruiterInvoiceImp;

    @Operation(summary = """
            Cancel recruiter package by id
            input: invoice id
            output: success message
            """)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> cancelOrder(@Positive @PathVariable int id) {
        recruiterInvoiceImp.cancelInvoice(id);
        return ApiResponse.<Void>builder()
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = """
            Call this API before call POST /api/payment
            to check if recruiter has an active package.
            input: none
            output: true if recruiter has active package, false if not
            """)
    @GetMapping("/active-package")
    public ApiResponse<Boolean> hasActivePackage() {
        return ApiResponse.<Boolean>builder()
                .result(recruiterInvoiceImp.hasActivePackage())
                .code(200)
                .message("success")
                .build();
    }
}
