package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.services.order_services.service.CandidateInvoiceImp;
import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyCandidateInvoiceResponse;
import com.fpt.careermate.services.order_services.service.dto.response.MyInvoiceListItemResponse;
import com.fpt.careermate.services.order_services.service.dto.response.PageMyInvoiceListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Tag(name = "Candidate - Invoice", description = "Manage candidate Invoice")
@RestController
@RequestMapping("/api/candidate-invoice")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CandidateInvoiceController {

    CandidateInvoiceImp candidateInvoiceImp;

    @Operation(summary = """
            Cancel candidate package by id
            input: none
            output: success message
            """)
    @DeleteMapping
    public ApiResponse<Void> cancelMyInvoice() {
        candidateInvoiceImp.cancelMyInvoice();
        return ApiResponse.<Void>builder()
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get my candidateInvoice for candidate")
    @GetMapping("/my-invoice")
    public ApiResponse<MyCandidateInvoiceResponse> myOrderList() {
        return ApiResponse.<MyCandidateInvoiceResponse>builder()
                .result(candidateInvoiceImp.getMyActiveInvoice())
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get my invoice history (paged)")
    @GetMapping("/my-invoices")
    public ApiResponse<PageMyInvoiceListResponse> myInvoiceHistory(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive int size
    ) {
        return ApiResponse.<PageMyInvoiceListResponse>builder()
                .result(candidateInvoiceImp.getMyInvoiceHistory(page, size))
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = "Get invoice detail by id (must be owned by current candidate)")
    @GetMapping("/my-invoices/{id}")
    public ApiResponse<MyInvoiceListItemResponse> myInvoiceDetail(
            @PathVariable("id") @Positive int id
    ) {
        return ApiResponse.<MyInvoiceListItemResponse>builder()
                .result(candidateInvoiceImp.getMyInvoiceById(id))
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = """
            Call this API before call POST /api/candidate-payment
            to check if candidate has an active package.
            input: none
            output: true if candidate has active package, false if not
            """)
    @GetMapping("/active-package")
    public ApiResponse<Boolean> hasActivePackage() {
        return ApiResponse.<Boolean>builder()
                .result(candidateInvoiceImp.hasActivePackage())
                .code(200)
                .message("success")
                .build();
    }
}
