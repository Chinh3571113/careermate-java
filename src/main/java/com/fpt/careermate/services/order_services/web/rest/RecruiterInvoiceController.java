package com.fpt.careermate.services.order_services.web.rest;

import com.fpt.careermate.common.response.ApiResponse;
import com.fpt.careermate.services.order_services.service.RecruiterInvoiceImp;
import com.fpt.careermate.services.order_services.service.dto.response.MyRecruiterInvoiceResponse;
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
@Tag(name = "Recruiter - Invoice", description = "Manage recruiter candidateInvoice")
@RestController
@RequestMapping("/api/recruiter-invoice")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecruiterInvoiceController {

    RecruiterInvoiceImp recruiterInvoiceImp;

    @Operation(summary = """
            Cancel recruiter invoice
            input: none
            output: success message
            """)
    @DeleteMapping
    public ApiResponse<Void> cancelInvoice() {
        recruiterInvoiceImp.cancelMyInvoice();
        return ApiResponse.<Void>builder()
                .code(200)
                .message("success")
                .build();
    }

    @Operation(summary = """
            Call this API before call POST /api/recruiter-payment
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

    @Operation(summary = """
            API to get my active invoice
            input: none
            output: my active invoice information
            """)
    @GetMapping("/my-invoice")
    public ApiResponse<MyRecruiterInvoiceResponse> getMyActiveInvoice() {
        return ApiResponse.<MyRecruiterInvoiceResponse>builder()
                .result(recruiterInvoiceImp.getMyActiveInvoice())
                .code(200)
                .message("success")
                .build();
    }
}
